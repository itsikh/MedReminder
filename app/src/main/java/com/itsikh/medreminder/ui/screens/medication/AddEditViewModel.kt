package com.itsikh.medreminder.ui.screens.medication

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itsikh.medreminder.data.model.Medication
import com.itsikh.medreminder.data.model.MedicationSchedule
import com.itsikh.medreminder.data.repository.MedicationRepository
import com.itsikh.medreminder.notification.ALL_DAYS_MASK
import com.itsikh.medreminder.notification.AlarmScheduler
import com.itsikh.medreminder.notification.GeofenceManager
import com.itsikh.medreminder.notification.ReminderCleanup
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditViewModel @Inject constructor(
    private val repository: MedicationRepository,
    private val alarmScheduler: AlarmScheduler,
    private val geofenceManager: GeofenceManager,
    private val reminderCleanup: ReminderCleanup
) : ViewModel() {

    var name by mutableStateOf("")
    var dosage by mutableStateOf("")
    var color by mutableIntStateOf(0xFF4CAF50.toInt())
    // daysOfWeek bitmask: bit(calDay-1). 0x7F = every day
    var daysOfWeek by mutableIntStateOf(0x7F)
    // list of (hour, minute) pairs
    var timeSlots by mutableStateOf<List<Pair<Int, Int>>>(emptyList())
    // Stock tracking: empty string = not tracking
    var stockQuantityText by mutableStateOf("")
    var lowStockThresholdPct by mutableIntStateOf(20)
    var criticalStockThresholdPct by mutableIntStateOf(10)

    var isLoading by mutableStateOf(false)
    var isSaved by mutableStateOf(false)

    fun loadMedication(medId: Int) {
        viewModelScope.launch {
            isLoading = true
            try {
                loadInto(medId)
            } finally {
                isLoading = false
            }
        }
    }

    private suspend fun loadInto(medId: Int) {
        val med = repository.getMedicationById(medId) ?: return
        name = med.name
        dosage = med.dosage
        color = med.color
        stockQuantityText = if (med.stockQuantity >= 0) med.stockQuantity.toString() else ""
        lowStockThresholdPct = med.lowStockThresholdPct
        criticalStockThresholdPct = med.criticalStockThresholdPct
        val schedules = repository.getSchedulesForMedication(medId)
            .sortedBy { it.timeHour * 60 + it.timeMinute }
        timeSlots = schedules.map { it.timeHour to it.timeMinute }
        if (schedules.isNotEmpty()) daysOfWeek = schedules.first().daysOfWeek
    }

    /** Ignores a time that is already in the list — duplicates would create twin reminders. */
    fun addTimeSlot(hour: Int, minute: Int) {
        val slot = hour to minute
        if (slot in timeSlots) return
        timeSlots = (timeSlots + slot).sortedBy { it.first * 60 + it.second }
    }

    /** A schedule with no days selected can never fire, so saving one must be blocked. */
    val isValid: Boolean
        get() = name.isNotBlank() && timeSlots.isNotEmpty() && daysOfWeek and ALL_DAYS_MASK != 0

    fun removeTimeSlot(index: Int) {
        timeSlots = timeSlots.toMutableList().also { it.removeAt(index) }
    }

    fun toggleDay(calDay: Int) {
        val bit = 1 shl (calDay - 1)
        daysOfWeek = daysOfWeek xor bit
    }

    fun save(medId: Int?) {
        if (!isValid) return
        viewModelScope.launch {
            val stockQty = stockQuantityText.trim().toIntOrNull() ?: -1
            val slots = timeSlots.sortedBy { it.first * 60 + it.second }

            val finalId: Int = if (medId != null && medId > 0) {
                val old = repository.getMedicationById(medId) ?: return@launch
                repository.updateMedication(old.copy(
                    name = name.trim(), dosage = dosage.trim(), color = color,
                    stockQuantity = stockQty,
                    stockInitial = resolveStockInitial(old, stockQty),
                    lowStockThresholdPct = lowStockThresholdPct,
                    criticalStockThresholdPct = criticalStockThresholdPct
                ))
                medId
            } else {
                repository.insertMedication(
                    Medication(
                        name = name.trim(), dosage = dosage.trim(), color = color,
                        stockQuantity = stockQty,
                        stockInitial = if (stockQty >= 0) stockQty else -1,
                        lowStockThresholdPct = lowStockThresholdPct,
                        criticalStockThresholdPct = criticalStockThresholdPct
                    )
                ).toInt()
            }

            val medication = repository.getMedicationById(finalId) ?: return@launch
            syncSchedules(finalId, medication, slots)
            isSaved = true
        }
    }

    /**
     * Rewrites [medId]'s schedules to match [slots].
     *
     * Rows are matched to slots **by time**, so an unchanged time keeps its schedule ID and
     * with it today's log — previously every edit deleted and re-inserted all schedules,
     * orphaning `MedicationLog.scheduleId` and making a dose already taken look untaken.
     *
     * A time that actually moved gets a fresh row rather than inheriting an existing one.
     * Reusing an ID across a time change would transplant that slot's log onto a different
     * time of day, which can mark a dose taken that never was — the one direction of error
     * that matters here.
     */
    private suspend fun syncSchedules(
        medId: Int,
        medication: Medication,
        slots: List<Pair<Int, Int>>
    ) {
        val existing = repository.getSchedulesForMedication(medId)
        val keptIds = mutableSetOf<Int>()

        slots.forEach { (h, m) ->
            val match = existing.firstOrNull {
                it.timeHour == h && it.timeMinute == m && it.id !in keptIds
            }
            val schedule = if (match != null) {
                keptIds += match.id
                val updated = match.copy(daysOfWeek = daysOfWeek, isEnabled = true)
                repository.updateSchedule(updated)
                updated
            } else {
                val id = repository.insertSchedule(
                    MedicationSchedule(
                        medicationId = medId, timeHour = h, timeMinute = m, daysOfWeek = daysOfWeek
                    )
                ).toInt()
                keptIds += id
                repository.getScheduleById(id) ?: return@forEach
            }
            // Re-arm the daily alarm — the days, and so the next occurrence, may have moved.
            // This replaces the existing alarm in place (same PendingIntent), so a reminder
            // already showing and its snooze/nag alarms are deliberately left alone.
            alarmScheduler.scheduleNextAlarm(schedule, medication)
        }

        existing.filter { it.id !in keptIds }.forEach { removed ->
            reminderCleanup.cancelAllForSchedule(removed.id)
            geofenceManager.removeGeofencesForSchedules(setOf(removed.id))
            repository.deleteSchedule(removed)
        }
    }

    fun delete(medId: Int) {
        viewModelScope.launch {
            // Cancels the daily, snooze and nag alarms, dismisses any visible reminder, and
            // tears down pending home geofences — all of which used to outlive the deletion.
            reminderCleanup.cancelAllForMedication(medId)
            repository.deactivateMedication(medId)
            isSaved = true
        }
    }

    /**
     * Keeps the "full pack" reference used for low-stock percentages. Only a quantity
     * increase counts as a restock; editing a name previously reset the baseline to the
     * current count, silently pushing the med back to 100% and muting its alerts.
     */
    private fun resolveStockInitial(old: Medication, newQty: Int): Int = when {
        newQty < 0 -> -1                                   // tracking turned off
        old.stockQuantity < 0 || old.stockInitial <= 0 -> newQty  // tracking turned on
        newQty > old.stockQuantity -> newQty                // restocked
        else -> old.stockInitial                            // unchanged or consumed
    }
}
