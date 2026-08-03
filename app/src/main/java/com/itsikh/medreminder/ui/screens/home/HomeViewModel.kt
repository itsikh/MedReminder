package com.itsikh.medreminder.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itsikh.medreminder.data.model.*
import com.itsikh.medreminder.data.repository.MedicationRepository
import com.itsikh.medreminder.notification.NotificationHelper
import com.itsikh.medreminder.notification.ReminderCleanup
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class TodayMedication(
    val medication: Medication,
    val schedule: MedicationSchedule,
    val log: MedicationLog?,
    /** Wall-clock time this dose is due, on the day being displayed. */
    val scheduledTimeMillis: Long
) {
    val isTaken: Boolean get() = log?.status == LogStatus.TAKEN
    val isSnoozed: Boolean get() = log?.status == LogStatus.SNOOZED
    val isSkipped: Boolean get() = log?.status == LogStatus.SKIPPED
    val isMissed: Boolean
        get() = !isTaken && !isSkipped &&
            System.currentTimeMillis() > scheduledTimeMillis + MISSED_GRACE_MS

    private companion object {
        const val MISSED_GRACE_MS = 30 * 60_000L
    }
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MedicationRepository,
    private val notificationHelper: NotificationHelper,
    private val reminderCleanup: ReminderCleanup
) : ViewModel() {

    /**
     * Start of the day currently being displayed.
     *
     * This has to be a flow rather than a value captured at construction: the ViewModel
     * outlives midnight whenever the app is left open, and a frozen window matched
     * yesterday's logs against today's schedules — showing today's doses as already taken.
     */
    private val _dayStart = MutableStateFlow(startOfToday())
    val dayStart: StateFlow<Long> = _dayStart

    /** In-flight "Took it" taps, so a double tap cannot log or decrement stock twice. */
    private val marking = mutableSetOf<Int>()

    init {
        // Roll the window over at midnight even if the screen is never re-entered.
        viewModelScope.launch {
            while (true) {
                delay(DAY_CHECK_INTERVAL_MS)
                refreshDay()
            }
        }
        // Doses left hanging on earlier days are never otherwise resolved.
        viewModelScope.launch { repository.markAbandonedLogsMissed() }
    }

    /** Re-reads the current day. Call from the screen on resume. */
    fun refreshDay() {
        val today = startOfToday()
        if (today != _dayStart.value) _dayStart.value = today
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val todayMedications: StateFlow<List<TodayMedication>> = _dayStart
        .flatMapLatest { dayStart ->
            combine(
                repository.getMedicationsWithSchedules(),
                repository.getLogsForDay(dayStart, dayStart + DAY_MS)
            ) { medsWithSchedules, logs ->
                val dow = dayOfWeek(dayStart)
                medsWithSchedules.flatMap { mws ->
                    mws.schedules
                        .filter { s -> s.isEnabled && isDayScheduled(s.daysOfWeek, dow) }
                        .map { s ->
                            TodayMedication(
                                medication = mws.medication,
                                schedule = s,
                                log = logs.firstOrNull { it.scheduleId == s.id },
                                scheduledTimeMillis = scheduledTimeOn(dayStart, s)
                            )
                        }
                }.sortedBy { it.scheduledTimeMillis }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun markTaken(item: TodayMedication) {
        if (item.isTaken || !marking.add(item.schedule.id)) return
        viewModelScope.launch {
            try {
                // Re-read rather than trusting the rendered snapshot: the alarm may have
                // created the log in the moment between this row being drawn and tapped,
                // and inserting again would leave two rows for one dose.
                val dayStart = startOfDay(item.scheduledTimeMillis)
                val current = repository.getLogForScheduleInRange(
                    item.schedule.id, dayStart, dayStart + DAY_MS
                )
                if (current?.status == LogStatus.TAKEN) return@launch

                val logId: Int = current?.id?.also { existingId ->
                    repository.updateLogStatus(existingId, LogStatus.TAKEN, System.currentTimeMillis())
                } ?: repository.insertLog(
                    MedicationLog(
                        medicationId = item.medication.id,
                        scheduleId = item.schedule.id,
                        medicationName = item.medication.name,
                        dosage = item.medication.dosage,
                        scheduledTimeMillis = item.scheduledTimeMillis,
                        takenTimeMillis = System.currentTimeMillis(),
                        status = LogStatus.TAKEN
                    )
                ).toInt()

                // The reminder notification is ongoing and cannot be swiped away, and its
                // "Took it" button would decrement stock a second time if left in the shade.
                // Passing the log id also drops a pending home-arrival geofence, which would
                // otherwise fire on arriving home and reset this dose back to pending.
                // Only today's dose is cleared — the daily repeat must stay armed.
                reminderCleanup.cancelPendingDose(item.schedule.id, logId)
                deductStockAndNotifyIfLow(item.medication.id)
            } finally {
                marking.remove(item.schedule.id)
            }
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

    private fun startOfToday() = startOfDay(System.currentTimeMillis())

    private fun startOfDay(timestamp: Long) = Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun dayOfWeek(dayStart: Long) = Calendar.getInstance().apply {
        timeInMillis = dayStart
    }.get(Calendar.DAY_OF_WEEK)

    private fun scheduledTimeOn(dayStart: Long, s: MedicationSchedule) = Calendar.getInstance().apply {
        timeInMillis = dayStart
        set(Calendar.HOUR_OF_DAY, s.timeHour); set(Calendar.MINUTE, s.timeMinute)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private companion object {
        const val DAY_MS = 24 * 60 * 60_000L
        const val DAY_CHECK_INTERVAL_MS = 60_000L
    }
}
