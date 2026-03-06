package com.itsikh.medreminder.notification

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.itsikh.medreminder.data.preferences.SnoozePrefs
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeofenceManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val snoozePrefs: SnoozePrefs
) {
    private val client: GeofencingClient = LocationServices.getGeofencingClient(context)

    /** Returns true if location permission is available for geofencing. */
    fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

    /**
     * Registers a home-arrival geofence that fires a reminder notification
     * when the user enters the home radius (~150 m).
     */
    fun registerHomeGeofence(
        logId: Int, scheduleId: Int, medicationId: Int,
        medName: String, dosage: String, scheduledTime: Long
    ) {
        if (!hasLocationPermission() || !snoozePrefs.hasHomeLocation) return

        val geofence = Geofence.Builder()
            .setRequestId(geofenceId(logId))
            .setCircularRegion(snoozePrefs.homeLat, snoozePrefs.homeLng, 150f)
            .setExpirationDuration(24 * 60 * 60_000L)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        snoozePrefs.savePendingGeofence(logId, scheduleId, medicationId, medName, dosage, scheduledTime)

        try {
            client.addGeofences(request, pendingIntent(logId))
        } catch (_: SecurityException) {
            // permission revoked between check and call
        }
    }

    fun removeGeofence(logId: Int) {
        client.removeGeofences(listOf(geofenceId(logId)))
        snoozePrefs.clearPendingGeofence(logId)
    }

    private fun geofenceId(logId: Int) = "med_home_$logId"

    private fun pendingIntent(logId: Int): PendingIntent {
        val intent = Intent(context, GeofenceReceiver::class.java).apply {
            putExtra(EXTRA_LOG_ID, logId)
        }
        return PendingIntent.getBroadcast(
            context, logId + 20_000, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    companion object {
        const val EXTRA_LOG_ID = "gf_log_id"
    }
}
