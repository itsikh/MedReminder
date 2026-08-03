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
    @Inject lateinit var geofenceManager: GeofenceManager
    @Inject lateinit var homeWifiDetector: HomeWifiDetector
    @Inject lateinit var alarmScheduler: AlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == GeofenceManager.ACTION_HOME_ARRIVAL_CHECK) {
            handleArrivalCheck(intent)
            return
        }

        @Suppress("DEPRECATION")
        val event = GeofencingEvent.fromIntent(intent) ?: return
        if (event.hasError()) return

        val transition = event.geofenceTransition
        if (transition != Geofence.GEOFENCE_TRANSITION_ENTER &&
            transition != Geofence.GEOFENCE_TRANSITION_DWELL
        ) return

        val delayMs = geofenceManager.arrivalDelayMs()
        val result = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                event.triggeringGeofences?.forEach { geo ->
                    val logId = geo.requestId.removePrefix("med_home_").toIntOrNull() ?: return@forEach
                    when {
                        // The user has now stayed home long enough, or no wait is configured.
                        transition == Geofence.GEOFENCE_TRANSITION_DWELL || delayMs <= 0L ->
                            fireReminder(logId)

                        // Already on the home network — definitely inside, skip the wait.
                        homeWifiDetector.isOnHomeWifi() -> fireReminder(logId)

                        else -> {
                            // Just crossed the boundary; could still be driving past.
                            // Restarts the clock on every entry, so leaving and returning re-waits.
                            snoozePrefs.setGeofenceEnteredAt(logId, System.currentTimeMillis())
                            geofenceManager.scheduleArrivalCheck(
                                logId, geofenceManager.nextCheckDelayMs(delayMs)
                            )
                        }
                    }
                }
            } finally {
                result.finish()
            }
        }
    }

    /**
     * Runs while the arrival wait is counting down, and once more when it expires.
     *
     * Home Wi-Fi is checked first and fires immediately — it is cheap and conclusive.
     * Only once the wait has expired does it fall back to a location fix, which must
     * place the device inside the home radius; an unknown location errs toward
     * notifying rather than dropping the reminder entirely.
     */
    private fun handleArrivalCheck(intent: Intent) {
        val logId = intent.getIntExtra(GeofenceManager.EXTRA_LOG_ID, -1)
        if (logId == -1) return
        val result = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                if (snoozePrefs.getPendingGeofence(logId) == null) return@launch

                if (homeWifiDetector.isOnHomeWifi()) {
                    fireReminder(logId)
                    return@launch
                }

                val enteredAt = snoozePrefs.getGeofenceEnteredAt(logId)
                val remaining = enteredAt + geofenceManager.arrivalDelayMs() - System.currentTimeMillis()
                if (enteredAt == 0L || remaining <= 0L) {
                    // Wait is over. Confirm by location, otherwise the user has left again —
                    // stop polling and let the still-armed geofence catch the next arrival.
                    if (geofenceManager.isAtHome() != false) fireReminder(logId)
                    else snoozePrefs.setGeofenceEnteredAt(logId, 0L)
                } else {
                    geofenceManager.scheduleArrivalCheck(logId, geofenceManager.nextCheckDelayMs(remaining))
                }
            } finally {
                result.finish()
            }
        }
    }

    /** No-ops if the reminder for this log has already been fired or cancelled. */
    private suspend fun fireReminder(logId: Int) {
        val info = snoozePrefs.getPendingGeofence(logId) ?: return

        val medication = repository.getMedicationById(info.medicationId)
        val schedule = repository.getScheduleById(info.scheduleId)
        if (medication == null || !medication.isActive || schedule == null) {
            // Removed while the snooze was pending — drop the geofence instead of
            // reminding about a medication that no longer exists.
            geofenceManager.removeGeofence(logId)
            return
        }

        // The dose may have been settled elsewhere while the snooze was pending — from the
        // app, or from another notification. Resetting it to PENDING below would otherwise
        // resurrect a dose the user already dealt with.
        val log = repository.getLogById(logId)
        if (log != null && log.status != LogStatus.PENDING && log.status != LogStatus.SNOOZED) {
            geofenceManager.removeGeofence(logId)
            return
        }

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

        // Follow up like any other reminder — arriving home and then getting distracted
        // is exactly the case that needs a re-alert.
        val nagIntervalMs = snoozePrefs.nagIntervalMinutes * 60_000L
        if (nagIntervalMs > 0 && snoozePrefs.nagRepeatLimit > 0) {
            alarmScheduler.scheduleNagAlarm(schedule, medication, logId, nagIntervalMs, nagCount = 1)
        }

        // Clears the pending record too, which makes this call idempotent.
        geofenceManager.removeGeofence(logId)
    }
}
