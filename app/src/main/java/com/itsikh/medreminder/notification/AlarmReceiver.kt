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
import java.util.Calendar
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
                val nagCount = intent.getIntExtra(AlarmScheduler.EXTRA_NAG_COUNT, 0)

                if (scheduleId == -1 || medicationId == -1) return@launch

                val schedule = repository.getScheduleById(scheduleId)
                val medication = repository.getMedicationById(medicationId)

                // A deleted or disabled medication — or a time slot the user removed — must
                // not keep reminding. Snooze and nag alarms can outlive both rows, so this
                // is the last line of defence.
                if (medication == null || !medication.isActive || schedule == null) {
                    notificationHelper.cancelNotification(scheduleId)
                    alarmScheduler.cancelAllAlarms(scheduleId)
                    return@launch
                }

                val isQuietTime = snoozePrefs.isQuietHour(
                    Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                )

                val logId: Int = if (existingLogId > 0) {
                    // Snooze or nag alarm — only proceed if the log still needs attention
                    val log = repository.getLogById(existingLogId)
                    if (log == null || (log.status != LogStatus.PENDING && log.status != LogStatus.SNOOZED)) {
                        return@launch
                    }
                    // Cancel existing notification before re-posting so sound/vibration re-triggers
                    notificationHelper.cancelNotification(scheduleId)
                    if (isQuietTime) {
                        repository.updateLogStatus(existingLogId, LogStatus.MISSED, null)
                        alarmScheduler.cancelNagAlarm(scheduleId)
                        return@launch
                    }
                    existingLogId
                } else {
                    // New alarm — schedule the next occurrence first. Pass the intended time
                    // rather than "now": an inexact alarm can fire early and computing from
                    // now would land on today again and re-fire immediately. The floor keeps
                    // a very late alarm (device off for days) from walking forward one day
                    // per firing, each one logging and notifying on the way.
                    val rescheduleAfter = maxOf(
                        scheduledTime,
                        System.currentTimeMillis() - EARLY_FIRE_TOLERANCE_MS
                    )
                    alarmScheduler.scheduleNextAlarm(schedule, medication, after = rescheduleAfter)

                    // The dose may already have been recorded from the app before this fired.
                    val dayStart = startOfDay(scheduledTime)
                    val existing = repository.getLogForScheduleInRange(
                        scheduleId, dayStart, dayStart + DAY_MS
                    )
                    if (existing != null &&
                        (existing.status == LogStatus.TAKEN || existing.status == LogStatus.SKIPPED)
                    ) {
                        // Already dealt with today — do not log or notify again.
                        return@launch
                    }
                    if (isQuietTime) {
                        when {
                            existing == null -> repository.insertLog(
                                newLog(medicationId, scheduleId, medName, dosage, scheduledTime, LogStatus.MISSED)
                            )
                            // A snoozed dose still has an alarm coming — marking it missed
                            // would make that alarm discard itself on arrival.
                            existing.status == LogStatus.PENDING ->
                                repository.updateLogStatus(existing.id, LogStatus.MISSED, null)
                        }
                        return@launch
                    }
                    existing?.id ?: repository.insertLog(
                        newLog(medicationId, scheduleId, medName, dosage, scheduledTime, LogStatus.PENDING)
                    ).toInt()
                }

                notificationHelper.showMedicationNotification(
                    scheduleId = scheduleId,
                    medicationId = medicationId,
                    medicationName = medName,
                    dosage = dosage,
                    logId = logId,
                    scheduledTime = scheduledTime
                )

                // Re-notify if the user does not acknowledge — but only up to the configured
                // limit, so an ignored dose cannot nag every interval indefinitely.
                val nagIntervalMs = snoozePrefs.nagIntervalMinutes * 60_000L
                val nagsSoFar = if (existingLogId > 0) nagCount else 0
                if (nagIntervalMs > 0 && nagsSoFar < snoozePrefs.nagRepeatLimit) {
                    alarmScheduler.scheduleNagAlarm(
                        schedule, medication, logId, nagIntervalMs, nagCount = nagsSoFar + 1
                    )
                }
            } finally {
                result.finish()
            }
        }
    }

    private fun newLog(
        medicationId: Int, scheduleId: Int, medName: String,
        dosage: String, scheduledTime: Long, status: LogStatus
    ) = MedicationLog(
        medicationId = medicationId,
        scheduleId = scheduleId,
        medicationName = medName,
        dosage = dosage,
        scheduledTimeMillis = scheduledTime,
        status = status
    )

    private fun startOfDay(timestamp: Long): Long = Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private companion object {
        const val DAY_MS = 24 * 60 * 60_000L

        /** How early an inexact alarm is assumed to be able to fire. */
        const val EARLY_FIRE_TOLERANCE_MS = 15 * 60_000L
    }
}
