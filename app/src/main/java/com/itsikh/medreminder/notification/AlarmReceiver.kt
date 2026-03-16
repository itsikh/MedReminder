package com.itsikh.medreminder.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.itsikh.medreminder.data.model.LogStatus
import com.itsikh.medreminder.data.model.MedicationLog
import com.itsikh.medreminder.data.preferences.SnoozePrefs
import com.itsikh.medreminder.data.repository.MedicationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var repository: MedicationRepository
    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var alarmScheduler: AlarmScheduler
    @Inject lateinit var snoozePrefs: SnoozePrefs

    override fun onReceive(context: Context, intent: Intent) {
        val result = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val scheduleId    = intent.getIntExtra(AlarmScheduler.EXTRA_SCHEDULE_ID, -1)
                val medicationId  = intent.getIntExtra(AlarmScheduler.EXTRA_MEDICATION_ID, -1)
                val medName       = intent.getStringExtra(AlarmScheduler.EXTRA_MEDICATION_NAME) ?: return@launch
                val dosage        = intent.getStringExtra(AlarmScheduler.EXTRA_DOSAGE) ?: ""
                val scheduledTime = intent.getLongExtra(AlarmScheduler.EXTRA_SCHEDULED_TIME, System.currentTimeMillis())
                val existingLogId = intent.getIntExtra(AlarmScheduler.EXTRA_LOG_ID, 0)

                if (scheduleId == -1 || medicationId == -1) return@launch

                val schedule = repository.getScheduleById(scheduleId)
                val medication = repository.getMedicationById(medicationId)

                val logId: Int = if (existingLogId > 0) {
                    // Snooze or nag alarm — only proceed if the log still needs attention
                    val log = repository.getLogById(existingLogId)
                    if (log == null || (log.status != LogStatus.PENDING && log.status != LogStatus.SNOOZED)) {
                        return@launch
                    }
                    // Cancel existing notification before re-posting so sound/vibration re-triggers
                    notificationHelper.cancelNotification(scheduleId)
                    existingLogId
                } else {
                    // New alarm — create a PENDING log entry and schedule next occurrence
                    val id = repository.insertLog(
                        MedicationLog(
                            medicationId = medicationId,
                            scheduleId = scheduleId,
                            medicationName = medName,
                            dosage = dosage,
                            scheduledTimeMillis = scheduledTime,
                            status = LogStatus.PENDING
                        )
                    ).toInt()
                    if (schedule != null && medication != null) {
                        alarmScheduler.scheduleNextAlarm(schedule, medication)
                    }
                    id
                }

                notificationHelper.showMedicationNotification(
                    scheduleId = scheduleId,
                    medicationId = medicationId,
                    medicationName = medName,
                    dosage = dosage,
                    logId = logId,
                    scheduledTime = scheduledTime
                )

                // Schedule a nag alarm to re-notify if the user does not acknowledge
                val nagIntervalMs = snoozePrefs.nagIntervalMinutes * 60_000L
                if (schedule != null && medication != null && nagIntervalMs > 0) {
                    alarmScheduler.scheduleNagAlarm(schedule, medication, logId, nagIntervalMs)
                }
            } finally {
                result.finish()
            }
        }
    }
}
