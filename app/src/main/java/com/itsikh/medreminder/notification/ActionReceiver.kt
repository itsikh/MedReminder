package com.itsikh.medreminder.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.itsikh.medreminder.data.model.LogStatus
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

                if (logId == -1 || scheduleId == -1) return@launch

                when (action) {
                    NotificationHelper.ACTION_TAKEN -> {
                        repository.updateLogStatus(logId, LogStatus.TAKEN, System.currentTimeMillis())
                        notificationHelper.cancelNotification(notifId)
                    }

                    NotificationHelper.ACTION_SNOOZE_SLOT_1,
                    NotificationHelper.ACTION_SNOOZE_SLOT_2,
                    NotificationHelper.ACTION_SNOOZE_SLOT_3 -> {
                        val ms = intent.getLongExtra(NotificationHelper.EXTRA_SNOOZE_MS, 15 * 60_000L)
                        snooze(scheduleId, medicationId, medName, dosage, logId, notifId, scheduledTime, ms)
                    }

                    NotificationHelper.ACTION_SNOOZE_TONIGHT -> {
                        val tonight = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 20); set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                        }.timeInMillis
                        val delay = (tonight - System.currentTimeMillis()).coerceAtLeast(2 * 60_000L)
                        snooze(scheduleId, medicationId, medName, dosage, logId, notifId, scheduledTime, delay)
                    }

                    NotificationHelper.ACTION_SNOOZE_LOCATION -> {
                        repository.updateLogStatus(logId, LogStatus.SNOOZED, null)
                        geofenceManager.registerHomeGeofence(
                            logId, scheduleId, medicationId, medName, dosage, scheduledTime
                        )
                        notificationHelper.cancelNotification(notifId)
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
        repository.updateLogStatus(logId, LogStatus.SNOOZED, null)
        val schedule = repository.getScheduleById(scheduleId)
        val medication = repository.getMedicationById(medicationId)
        if (schedule != null && medication != null) {
            alarmScheduler.scheduleSnoozeAlarm(schedule, medication, logId, delayMs)
        }
        notificationHelper.cancelNotification(notifId)
    }
}
