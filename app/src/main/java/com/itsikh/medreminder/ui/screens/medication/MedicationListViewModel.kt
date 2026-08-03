package com.itsikh.medreminder.ui.screens.medication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itsikh.medreminder.data.model.MedicationWithSchedules
import com.itsikh.medreminder.data.repository.MedicationRepository
import com.itsikh.medreminder.notification.ReminderCleanup
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MedicationListViewModel @Inject constructor(
    private val repository: MedicationRepository,
    private val reminderCleanup: ReminderCleanup
) : ViewModel() {

    val medications: StateFlow<List<MedicationWithSchedules>> =
        repository.getMedicationsWithSchedules()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun delete(medId: Int) {
        viewModelScope.launch {
            // Snooze/nag alarms and home geofences outlive the schedule rows, so they have
            // to be torn down explicitly or a deleted medication keeps reminding the user.
            reminderCleanup.cancelAllForMedication(medId)
            repository.deactivateMedication(medId)
        }
    }
}
