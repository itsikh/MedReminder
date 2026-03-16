package com.itsikh.medreminder.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itsikh.medreminder.data.model.*
import com.itsikh.medreminder.data.repository.MedicationRepository
import com.itsikh.medreminder.notification.AlarmScheduler
import com.itsikh.medreminder.notification.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class TodayMedication(
    val medication: Medication,
    val schedule: MedicationSchedule,
    val log: MedicationLog?
) {
    val isTaken: Boolean get() = log?.status == LogStatus.TAKEN
    val isSnoozed: Boolean get() = log?.status == LogStatus.SNOOZED
    val isMissed: Boolean
        get() {
            if (isTaken) return false
            val schedMs = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, schedule.timeHour)
                set(Calendar.MINUTE, schedule.timeMinute)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            return System.currentTimeMillis() > schedMs + 30 * 60_000L
        }
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MedicationRepository,
    private val alarmScheduler: AlarmScheduler,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    val todayMedications: StateFlow<List<TodayMedication>> = combine(
        repository.getMedicationsWithSchedules(),
        repository.getLogsForDay(todayStart(), todayEnd())
    ) { medsWithSchedules, logs ->
        val todayDow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        medsWithSchedules.flatMap { mws ->
            mws.schedules
                .filter { s -> s.isEnabled && isDayScheduled(s.daysOfWeek, todayDow) }
                .map { s ->
                    val log = logs.find { it.scheduleId == s.id }
                    TodayMedication(mws.medication, s, log)
                }
        }.sortedBy { it.schedule.timeHour * 60 + it.schedule.timeMinute }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun markTaken(item: TodayMedication) {
        viewModelScope.launch {
            if (item.log != null) {
                repository.updateLogStatus(item.log.id, LogStatus.TAKEN, System.currentTimeMillis())
            } else {
                repository.insertLog(
                    MedicationLog(
                        medicationId = item.medication.id,
                        scheduleId = item.schedule.id,
                        medicationName = item.medication.name,
                        dosage = item.medication.dosage,
                        scheduledTimeMillis = schedTimeToday(item.schedule),
                        takenTimeMillis = System.currentTimeMillis(),
                        status = LogStatus.TAKEN
                    )
                )
            }
            deductStockAndNotifyIfLow(item.medication.id)
        }
    }

    private suspend fun deductStockAndNotifyIfLow(medicationId: Int) {
        val before = repository.getMedicationById(medicationId) ?: return
        if (before.stockQuantity < 0) return  // not tracking stock
        repository.decrementStock(medicationId)
        val after = repository.getMedicationById(medicationId) ?: return
        if (after.stockInitial > 0) {
            val pct = after.stockQuantity * 100 / after.stockInitial
            when {
                pct <= after.criticalStockThresholdPct -> {
                    notificationHelper.cancelNotification(after.id + NotificationHelper.STOCK_WARN_NOTIF_OFFSET)
                    notificationHelper.showLowStockNotification(after, isCritical = true)
                }
                pct <= after.lowStockThresholdPct -> {
                    notificationHelper.showLowStockNotification(after, isCritical = false)
                }
            }
        }
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun isDayScheduled(daysOfWeek: Int, calDay: Int) =
        (daysOfWeek and (1 shl (calDay - 1))) != 0

    private fun todayStart() = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun todayEnd() = todayStart() + 24 * 60 * 60_000L

    private fun schedTimeToday(s: MedicationSchedule) = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, s.timeHour); set(Calendar.MINUTE, s.timeMinute)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
