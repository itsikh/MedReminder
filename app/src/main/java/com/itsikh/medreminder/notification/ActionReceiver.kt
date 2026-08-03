package com.itsikh.medreminder.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.itsikh.medreminder.data.model.LogStatus
import com.itsikh.medreminder.data.preferences.SnoozePrefs
import com.itsikh.medreminder.data.repository.MedicationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class ActionReceiver : BroadcastReceiver() {

    @Inject lateinit var repository: MedicationRepository
    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var alarmScheduler: AlarmScheduler
    @Inject lateinit var geofenceManager: GeofenceManager
    @Inject lateinit var snoozePrefs: SnoozePrefs

    override fun onReceive(context: Context, intent: Intent) {
        val result = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val action       = intent.action ?: return@launch
                val scheduleId   = intent.getIntExtra(AlarmScheduler.EXTRA_SCHEDULE_ID, -1)
                val medicationId = intent.getIntExtra(AlarmScheduler.EXTRA_MEDICATION_ID, -1)
                val medName      = intent.getStringExtra(AlarmScheduler.EXTRA_MEDICATION_NAME) ?: ""
                val dosage       = intent.getStringExtra(AlarmScheduler.EXTRA_DOSAGE) ?: ""
                val logId        = intent.getIntExtra(AlarmScheduler.EXTRA_LOG_ID, -1)
                val notifId      = intent.getIntExtra(AlarmScheduler.EXTRA_NOTIF_ID, scheduleId)
                val scheduledTime = intent.getLongExtra(AlarmScheduler.EXTRA_SCHEDULED_TIME, System.currentTimeMillis())

                // Handle stock dismiss first — doesn't need logId/scheduleId
                if (action == NotificationHelper.ACTION_DISMISS_STOCK) {
                    val stockNotifId = intent.getIntExtra(AlarmScheduler.EXTRA_NOTIF_ID, -1)
                    if (stockNotifId != -1) notificationHelper.cancelNotification(stockNotifId)
                    return@launch
                }

                if (logId == -1 || scheduleId == -1) return@launch

                // Cancel any pending nag alarm since the user is responding
                alarmScheduler.cancelNagAlarm(scheduleId)

                when (action) {
                    NotificationHelper.ACTION_TAKEN -> {
                        // A stale notification can be tapped after the dose was already
                        // recorded in the app. Without this guard stock is decremented twice.
                        val log = repository.getLogById(logId)
                        if (log != null && log.status == LogStatus.TAKEN) {
                            notificationHelper.cancelNotification(notifId)
                            return@launch
                        }
                        repository.updateLogStatus(logId, LogStatus.TAKEN, System.currentTimeMillis())
                        notificationHelper.cancelNotification(notifId)
                        // Drop anything still queued for this dose, but not the daily repeat.
                        alarmScheduler.cancelSnoozeAlarm(scheduleId)
                        geofenceManager.removeGeofence(logId)
                        val med = repository.getMedicationById(medicationId)
                        if (med != null && med.stockQuantity >= 0) {
                            repository.decrementStock(medicationId)
                            val updated = repository.getMedicationById(medicationId)
                            if (updated != null && updated.stockInitial > 0) {
                                val pct = updated.stockQuantity * 100 / updated.stockInitial
                                when {
                                    pct <= updated.criticalStockThresholdPct -> {
                                        notificationHelper.cancelNotification(updated.id + NotificationHelper.STOCK_WARN_NOTIF_OFFSET)
                                        notificationHelper.showLowStockNotification(updated, isCritical = true)
                                    }
                                    pct <= updated.lowStockThresholdPct -> {
                                        notificationHelper.showLowStockNotification(updated, isCritical = false)
                                    }
                                }
                            }
                        }
                    }

                    NotificationHelper.ACTION_SKIP_TODAY -> {
                        // Intentionally skipped — no re-reminders today. The next daily
                        // occurrence was already scheduled when the alarm fired.
                        repository.updateLogStatus(logId, LogStatus.SKIPPED, null)
                        notificationHelper.cancelNotification(notifId)
                        alarmScheduler.cancelSnoozeAlarm(scheduleId)
                        geofenceManager.removeGeofence(logId)
                    }

                    NotificationHelper.ACTION_SNOOZE_SLOT_1,
                    NotificationHelper.ACTION_SNOOZE_SLOT_2,
                    NotificationHelper.ACTION_SNOOZE_SLOT_3 -> {
                        val ms = intent.getLongExtra(NotificationHelper.EXTRA_SNOOZE_MS, 15 * 60_000L)
                        snooze(scheduleId, medicationId, medName, dosage, logId, notifId, scheduledTime, ms)
                    }

                    NotificationHelper.ACTION_SNOOZE_TONIGHT -> {
                        // Rolls to tomorrow when it is already past the evening slot —
                        // otherwise "tonight" collapsed to a 2-minute snooze after 20:00.
                        val tonight = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, EVENING_HOUR); set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                            if (timeInMillis <= System.currentTimeMillis() + MIN_SNOOZE_MS) {
                                add(Calendar.DAY_OF_YEAR, 1)
                            }
                        }.timeInMillis
                        val delay = (tonight - System.currentTimeMillis()).coerceAtLeast(MIN_SNOOZE_MS)
                        snooze(scheduleId, medicationId, medName, dosage, logId, notifId, scheduledTime, delay)
                    }

                    NotificationHelper.ACTION_SNOOZE_LOCATION -> {
                        // Drop any time-based snooze so the two don't both fire.
                        alarmScheduler.cancelSnoozeAlarm(scheduleId)
                        val armed = geofenceManager.registerHomeGeofence(
                            logId, scheduleId, medicationId, medName, dosage, scheduledTime
                        )
                        if (armed) {
                            repository.updateLogStatus(logId, LogStatus.SNOOZED, null)
                            notificationHelper.cancelNotification(notifId)
                        } else {
                            // Nothing will wake us on arrival — fall back to a time snooze
                            // rather than dismissing the reminder into the void.
                            val fallbackMs = snoozePrefs.slot1.coerceAtLeast(1) * 60_000L
                            snooze(
                                scheduleId, medicationId, medName, dosage,
                                logId, notifId, scheduledTime, fallbackMs
                            )
                        }
                    }
                }
            } finally {
                result.finish()
            }
        }
    }

    private suspend fun snooze(
        scheduleId: Int, medicationId: Int, medName: String, dosage: String,
        logId: Int, notifId: Int, scheduledTime: Long, delayMs: Long
    ) {
        val schedule = repository.getScheduleById(scheduleId)
        val medication = repository.getMedicationById(medicationId)
        if (schedule == null || medication == null || !medication.isActive) {
            // The medication or time slot is gone. There is nothing left to remind about,
            // so close the reminder out instead of marking it snoozed and dropping the
            // notification with no alarm behind it.
            repository.updateLogStatus(logId, LogStatus.SKIPPED, null)
            notificationHelper.cancelNotification(notifId)
            alarmScheduler.cancelAllAlarms(scheduleId)
            geofenceManager.removeGeofence(logId)
            return
        }
        repository.updateLogStatus(logId, LogStatus.SNOOZED, null)
        // Switching to a time snooze supersedes any pending home-arrival geofence.
        geofenceManager.removeGeofence(logId)
        alarmScheduler.scheduleSnoozeAlarm(schedule, medication, logId, delayMs)
        notificationHelper.cancelNotification(notifId)
    }

    private companion object {
        const val EVENING_HOUR = 20
        const val MIN_SNOOZE_MS = 2 * 60_000L
    }
}
