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
import com.itsikh.medreminder.notification.GeofenceManager
import com.itsikh.medreminder.notification.HomeWifiDetector
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class SnoozeSettingsViewModel @Inject constructor(
    private val snoozePrefs: SnoozePrefs,
    private val homeWifiDetector: HomeWifiDetector,
    @ApplicationContext private val context: Context
) : ViewModel() {

    var slot1 by mutableIntStateOf(snoozePrefs.slot1)
    var slot2 by mutableIntStateOf(snoozePrefs.slot2)
    var slot3 by mutableIntStateOf(snoozePrefs.slot3)
    var nagInterval by mutableIntStateOf(snoozePrefs.nagIntervalMinutes)
    var nagRepeatLimit by mutableIntStateOf(snoozePrefs.nagRepeatLimit)

    var quietHourEnabled by mutableStateOf(snoozePrefs.quietHourEnabled)
    var quietHour by mutableIntStateOf(snoozePrefs.quietHour)
    var quietHourEnd by mutableIntStateOf(snoozePrefs.quietHourEnd)

    var homeLat by mutableDoubleStateOf(snoozePrefs.homeLat)
    var homeLng by mutableDoubleStateOf(snoozePrefs.homeLng)
    val hasHomeLocation get() = !homeLat.isNaN() && !homeLng.isNaN()

    var homeArrivalDelay by mutableIntStateOf(snoozePrefs.homeArrivalDelayMinutes)
    var homeRadius by mutableIntStateOf(snoozePrefs.homeRadiusMeters)

    var homeWifiSsid by mutableStateOf(snoozePrefs.homeWifiSsid)
    var wifiLoading by mutableStateOf(false)
    var wifiError   by mutableStateOf<String?>(null)

    var locationLoading by mutableStateOf(false)
    var locationError   by mutableStateOf<String?>(null)

    fun saveSlots() {
        snoozePrefs.slot1 = slot1.coerceIn(1, 1440)
        snoozePrefs.slot2 = slot2.coerceIn(1, 1440)
        snoozePrefs.slot3 = slot3.coerceIn(1, 1440)
        snoozePrefs.nagIntervalMinutes = nagInterval.coerceIn(1, 1440)
        snoozePrefs.nagRepeatLimit = nagRepeatLimit.coerceIn(0, 50)
        snoozePrefs.quietHourEnabled = quietHourEnabled
        snoozePrefs.quietHour = quietHour.coerceIn(0, 23)
        snoozePrefs.quietHourEnd = quietHourEnd.coerceIn(0, 23)
        snoozePrefs.homeArrivalDelayMinutes = homeArrivalDelay.coerceIn(0, 120)
        snoozePrefs.homeRadiusMeters =
            homeRadius.coerceIn(GeofenceManager.MIN_RADIUS_METERS, GeofenceManager.MAX_RADIUS_METERS)
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

    /** Saves the Wi-Fi network the device is on right now as the home network. */
    fun captureCurrentWifiAsHome() {
        if (!hasLocationPermission()) {
            wifiError = "Location permission required to read the Wi-Fi name"
            return
        }
        wifiLoading = true
        wifiError = null
        viewModelScope.launch {
            try {
                val ssid = homeWifiDetector.currentSsid()
                if (ssid != null) {
                    snoozePrefs.homeWifiSsid = ssid
                    homeWifiSsid = ssid
                } else {
                    wifiError = "Not connected to Wi-Fi, or the network name is hidden. " +
                        "Make sure Wi-Fi and system location are both on."
                }
            } catch (e: Exception) {
                wifiError = "Error: ${e.localizedMessage}"
            } finally {
                wifiLoading = false
            }
        }
    }

    fun clearHomeWifi() {
        snoozePrefs.clearHomeWifi()
        homeWifiSsid = ""
        wifiError = null
    }
}
