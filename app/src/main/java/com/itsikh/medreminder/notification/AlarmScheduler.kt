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

        /** How many re-alerts have already fired for this log. Carried across nag alarms. */
        const val EXTRA_NAG_COUNT = "nag_count"

        private const val SNOOZE_REQUEST_OFFSET = 10_000
        private const val NAG_REQUEST_OFFSET = 20_000
    }

    /**
     * Schedules the next occurrence of [schedule].
     *
     * @param after Only occurrences strictly later than this instant are considered. When
     *   rescheduling from a fired alarm, pass that alarm's intended time — an inexact alarm
     *   can fire minutes early, and computing from "now" would then land on the same day
     *   again and re-fire immediately.
     */
    fun scheduleNextAlarm(
        schedule: MedicationSchedule,
        medication: Medication,
        after: Long = System.currentTimeMillis()
    ) {
        if (!schedule.isEnabled || !medication.isActive) return
        val triggerAt = computeNextAlarmTime(schedule, after) ?: return
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
            requestCode = schedule.id + SNOOZE_REQUEST_OFFSET
        )
        setExact(pi, triggerAt)
    }

    fun scheduleNagAlarm(
        schedule: MedicationSchedule,
        medication: Medication,
        logId: Int,
        nagDelayMs: Long,
        nagCount: Int
    ) {
        val triggerAt = System.currentTimeMillis() + nagDelayMs
        val pi = buildAlarmPendingIntent(
            schedule, medication, logId, triggerAt,
            requestCode = schedule.id + NAG_REQUEST_OFFSET,
            nagCount = nagCount
        )
        setExact(pi, triggerAt)
    }

    fun cancelAlarm(scheduleId: Int) = cancelByRequestCode(scheduleId)

    fun cancelSnoozeAlarm(scheduleId: Int) = cancelByRequestCode(scheduleId + SNOOZE_REQUEST_OFFSET)

    fun cancelNagAlarm(scheduleId: Int) = cancelByRequestCode(scheduleId + NAG_REQUEST_OFFSET)

    /**
     * Cancels every alarm that can still fire for one schedule. Cancelling only the daily
     * alarm used to leave snoozed and nagging reminders armed, so a deleted medication
     * kept reminding the user.
     */
    fun cancelAllAlarms(scheduleId: Int) {
        cancelAlarm(scheduleId)
        cancelSnoozeAlarm(scheduleId)
        cancelNagAlarm(scheduleId)
    }

    private fun cancelByRequestCode(requestCode: Int) {
        val intent = Intent(context, AlarmReceiver::class.java)
        PendingIntent.getBroadcast(
            context, requestCode, intent,
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
        requestCode: Int,
        nagCount: Int = 0
    ): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_SCHEDULE_ID, schedule.id)
            putExtra(EXTRA_MEDICATION_ID, medication.id)
            putExtra(EXTRA_MEDICATION_NAME, medication.name)
            putExtra(EXTRA_DOSAGE, medication.dosage)
            putExtra(EXTRA_SCHEDULED_TIME, triggerAt)
            putExtra(EXTRA_LOG_ID, logId)
            putExtra(EXTRA_NOTIF_ID, schedule.id)
            putExtra(EXTRA_NAG_COUNT, nagCount)
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

    /** Returns null when [schedule] has no enabled days, so nothing can ever fire. */
    fun computeNextAlarmTime(
        schedule: MedicationSchedule,
        after: Long = System.currentTimeMillis()
    ): Long? {
        if (schedule.daysOfWeek and ALL_DAYS_MASK == 0) return null
        val target = Calendar.getInstance().apply {
            timeInMillis = after
            set(Calendar.HOUR_OF_DAY, schedule.timeHour)
            set(Calendar.MINUTE, schedule.timeMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (target.timeInMillis <= after) target.add(Calendar.DAY_OF_YEAR, 1)
        repeat(8) {
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

/** Bits 0-6, one per Calendar day-of-week. A schedule with none set can never fire. */
const val ALL_DAYS_MASK = 0x7F
