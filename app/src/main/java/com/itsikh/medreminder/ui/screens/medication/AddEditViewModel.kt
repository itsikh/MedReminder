package com.itsikh.medreminder.ui.screens.medication

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itsikh.medreminder.data.model.Medication
import com.itsikh.medreminder.data.model.MedicationSchedule
import com.itsikh.medreminder.data.repository.MedicationRepository
import com.itsikh.medreminder.notification.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditViewModel @Inject constructor(
    private val repository: MedicationRepository,
    private val alarmScheduler: AlarmScheduler
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

    var isLoading by mutableStateOf(false)
    var isSaved by mutableStateOf(false)

    fun loadMedication(medId: Int) {
        viewModelScope.launch {
            isLoading = true
            val med = repository.getMedicationById(medId) ?: return@launch
            name = med.name
            dosage = med.dosage
            color = med.color
            stockQuantityText = if (med.stockQuantity >= 0) med.stockQuantity.toString() else ""
            lowStockThresholdPct = med.lowStockThresholdPct
            val schedules = repository.getSchedulesForMedication(medId)
            timeSlots = schedules.map { it.timeHour to it.timeMinute }
            if (schedules.isNotEmpty()) daysOfWeek = schedules.first().daysOfWeek
            isLoading = false
        }
    }

    fun addTimeSlot(hour: Int, minute: Int) {
        timeSlots = timeSlots + (hour to minute)
    }

    fun removeTimeSlot(index: Int) {
        timeSlots = timeSlots.toMutableList().also { it.removeAt(index) }
    }

    fun toggleDay(calDay: Int) {
        val bit = 1 shl (calDay - 1)
        daysOfWeek = daysOfWeek xor bit
    }

    fun save(medId: Int?) {
        if (name.isBlank() || timeSlots.isEmpty()) return
        viewModelScope.launch {
            val stockQty = stockQuantityText.trim().toIntOrNull() ?: -1
            val stockInit = if (stockQty >= 0) stockQty else -1
            val finalId: Int = if (medId != null && medId > 0) {
                val old = repository.getMedicationById(medId) ?: return@launch
                repository.updateMedication(old.copy(
                    name = name.trim(), dosage = dosage.trim(), color = color,
                    stockQuantity = stockQty, stockInitial = stockInit,
                    lowStockThresholdPct = lowStockThresholdPct
                ))
                repository.getSchedulesForMedication(medId).forEach { alarmScheduler.cancelAlarm(it.id) }
                repository.deleteSchedulesForMedication(medId)
                medId
            } else {
                repository.insertMedication(
                    Medication(
                        name = name.trim(), dosage = dosage.trim(), color = color,
                        stockQuantity = stockQty, stockInitial = stockInit,
                        lowStockThresholdPct = lowStockThresholdPct
                    )
                ).toInt()
            }

            val medication = repository.getMedicationById(finalId) ?: return@launch
            timeSlots.forEach { (h, m) ->
                val schedId = repository.insertSchedule(
                    MedicationSchedule(medicationId = finalId, timeHour = h, timeMinute = m, daysOfWeek = daysOfWeek)
                ).toInt()
                val sched = repository.getScheduleById(schedId) ?: return@forEach
                alarmScheduler.scheduleNextAlarm(sched, medication)
            }
            isSaved = true
        }
    }

    fun delete(medId: Int) {
        viewModelScope.launch {
            repository.getSchedulesForMedication(medId).forEach { alarmScheduler.cancelAlarm(it.id) }
            repository.deactivateMedication(medId)
            isSaved = true
        }
    }
}
