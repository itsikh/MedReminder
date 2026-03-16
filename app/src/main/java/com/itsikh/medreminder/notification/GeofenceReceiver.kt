package com.itsikh.medreminder.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.itsikh.medreminder.data.model.LogStatus
import com.itsikh.medreminder.data.preferences.SnoozePrefs
import com.itsikh.medreminder.data.repository.MedicationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GeofenceReceiver : BroadcastReceiver() {

    @Inject lateinit var repository: MedicationRepository
    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var snoozePrefs: SnoozePrefs

    override fun onReceive(context: Context, intent: Intent) {
        @Suppress("DEPRECATION")
        val event = GeofencingEvent.fromIntent(intent) ?: return
        if (event.hasError()) return
        if (event.geofenceTransition != Geofence.GEOFENCE_TRANSITION_ENTER) return

        val result = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                event.triggeringGeofences?.forEach { geo ->
                    val logId = geo.requestId.removePrefix("med_home_").toIntOrNull() ?: return@forEach
                    val info = snoozePrefs.getPendingGeofence(logId) ?: return@forEach
                    // Reset log status to PENDING so the notification shows correctly
                    repository.updateLogStatus(logId, LogStatus.PENDING, null)
                    notificationHelper.showMedicationNotification(
                        scheduleId   = info.scheduleId,
                        medicationId = info.medicationId,
                        medicationName = info.medName,
                        dosage       = info.dosage,
                        logId        = logId,
                        scheduledTime = info.scheduledTime
                    )
                    snoozePrefs.clearPendingGeofence(logId)
                }
            } finally {
                result.finish()
            }
        }
    }
}
