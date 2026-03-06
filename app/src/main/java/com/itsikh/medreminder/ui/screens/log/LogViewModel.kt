package com.itsikh.medreminder.ui.screens.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itsikh.medreminder.data.model.MedicationLog
import com.itsikh.medreminder.data.repository.MedicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class LogViewModel @Inject constructor(repository: MedicationRepository) : ViewModel() {
    val logs: StateFlow<List<MedicationLog>> =
        repository.getAllLogs()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
