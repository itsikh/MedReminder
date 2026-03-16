package com.itsikh.medreminder.ui.screens.snooze

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.itsikh.medreminder.data.preferences.SnoozePrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class SnoozeSettingsViewModel @Inject constructor(
    private val snoozePrefs: SnoozePrefs,
    @ApplicationContext private val context: Context
) : ViewModel() {

    var slot1 by mutableIntStateOf(snoozePrefs.slot1)
    var slot2 by mutableIntStateOf(snoozePrefs.slot2)
    var slot3 by mutableIntStateOf(snoozePrefs.slot3)
    var nagInterval by mutableIntStateOf(snoozePrefs.nagIntervalMinutes)

    var homeLat by mutableDoubleStateOf(snoozePrefs.homeLat)
    var homeLng by mutableDoubleStateOf(snoozePrefs.homeLng)
    val hasHomeLocation get() = !homeLat.isNaN() && !homeLng.isNaN()

    var locationLoading by mutableStateOf(false)
    var locationError   by mutableStateOf<String?>(null)

    fun saveSlots() {
        snoozePrefs.slot1 = slot1.coerceIn(1, 1440)
        snoozePrefs.slot2 = slot2.coerceIn(1, 1440)
        snoozePrefs.slot3 = slot3.coerceIn(1, 1440)
        snoozePrefs.nagIntervalMinutes = nagInterval.coerceIn(1, 1440)
    }

    fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    fun captureCurrentLocationAsHome() {
        if (!hasLocationPermission()) {
            locationError = "Location permission required"
            return
        }
        locationLoading = true
        locationError = null
        viewModelScope.launch {
            try {
                val client = LocationServices.getFusedLocationProviderClient(context)
                val loc = client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
                if (loc != null) {
                    snoozePrefs.homeLat = loc.latitude
                    snoozePrefs.homeLng = loc.longitude
                    homeLat = loc.latitude
                    homeLng = loc.longitude
                } else {
                    locationError = "Could not get location. Make sure GPS is enabled."
                }
            } catch (e: Exception) {
                locationError = "Error: ${e.localizedMessage}"
            } finally {
                locationLoading = false
            }
        }
    }

    fun clearHomeLocation() {
        snoozePrefs.clearHomeLocation()
        homeLat = Double.NaN
        homeLng = Double.NaN
    }
}
