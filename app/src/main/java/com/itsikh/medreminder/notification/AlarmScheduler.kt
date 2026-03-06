package com.itsikh.medreminder.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.itsikh.medreminder.data.model.Medication
import com.itsikh.medreminder.data.model.MedicationSchedule
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmManager: AlarmManager
) {
    companion object {
        const val EXTRA_SCHEDULE_ID = "schedule_id"
        const val EXTRA_MEDICATION_ID = "medication_id"
        const val EXTRA_MEDICATION_NAME = "medication_name"
        const val EXTRA_DOSAGE = "dosage"
        const val EXTRA_SCHEDULED_TIME = "scheduled_time"
        const val EXTRA_LOG_ID = "log_id"
        const val EXTRA_NOTIF_ID = "notif_id"
    }

    fun scheduleNextAlarm(schedule: MedicationSchedule, medication: Medication) {
        if (!schedule.isEnabled || !medication.isActive) return
        val triggerAt = computeNextAlarmTime(schedule) ?: return
        val pi = buildAlarmPendingIntent(schedule, medication, logId = 0, triggerAt, requestCode = schedule.id)
        setExact(pi, triggerAt)
    }

    fun scheduleSnoozeAlarm(
        schedule: MedicationSchedule,
        medication: Medication,
        logId: Int,
        snoozeMillis: Long
    ) {
        val triggerAt = System.currentTimeMillis() + snoozeMillis
        val pi = buildAlarmPendingIntent(
            schedule, medication, logId, triggerAt,
            requestCode = schedule.id + 10_000
        )
        setExact(pi, triggerAt)
    }

    fun cancelAlarm(scheduleId: Int) {
        val intent = Intent(context, AlarmReceiver::class.java)
        PendingIntent.getBroadcast(
            context, scheduleId, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )?.let { alarmManager.cancel(it) }
    }

    fun canScheduleExact(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) alarmManager.canScheduleExactAlarms()
        else true

    // ── private helpers ───────────────────────────────────────────────────────

    private fun buildAlarmPendingIntent(
        schedule: MedicationSchedule,
        medication: Medication,
        logId: Int,
        triggerAt: Long,
        requestCode: Int
    ): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_SCHEDULE_ID, schedule.id)
            putExtra(EXTRA_MEDICATION_ID, medication.id)
            putExtra(EXTRA_MEDICATION_NAME, medication.name)
            putExtra(EXTRA_DOSAGE, medication.dosage)
            putExtra(EXTRA_SCHEDULED_TIME, triggerAt)
            putExtra(EXTRA_LOG_ID, logId)
            putExtra(EXTRA_NOTIF_ID, schedule.id)
        }
        return PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun setExact(pi: PendingIntent, triggerAt: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }

    fun computeNextAlarmTime(schedule: MedicationSchedule): Long? {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, schedule.timeHour)
            set(Calendar.MINUTE, schedule.timeMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (target.timeInMillis <= now.timeInMillis) target.add(Calendar.DAY_OF_YEAR, 1)
        repeat(7) {
            if (isDayScheduled(schedule.daysOfWeek, target.get(Calendar.DAY_OF_WEEK))) {
                return target.timeInMillis
            }
            target.add(Calendar.DAY_OF_YEAR, 1)
        }
        return null
    }

    private fun isDayScheduled(daysOfWeek: Int, calDay: Int): Boolean =
        (daysOfWeek and (1 shl (calDay - 1))) != 0
}
