package com.itsikh.medreminder.ui.screens.medication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itsikh.medreminder.data.model.MedicationWithSchedules
import com.itsikh.medreminder.data.repository.MedicationRepository
import com.itsikh.medreminder.notification.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MedicationListViewModel @Inject constructor(
    private val repository: MedicationRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    val medications: StateFlow<List<MedicationWithSchedules>> =
        repository.getMedicationsWithSchedules()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun delete(medId: Int) {
        viewModelScope.launch {
            repository.getSchedulesForMedication(medId).forEach {
                alarmScheduler.cancelAlarm(it.id)
            }
            repository.deactivateMedication(medId)
        }
    }
}
