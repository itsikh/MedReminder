package com.itsikh.medreminder.notification

import com.itsikh.medreminder.data.repository.MedicationRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tears down every pending reminder for a medication or schedule.
 *
 * A medication's reminders live in four places: the daily alarm, a snooze alarm, a nag
 * alarm, and possibly a home-arrival geofence — plus whatever is already showing in the
 * notification shade. Cancelling only the daily alarm (which is all the delete paths used
 * to do) leaves the rest armed, so a removed medication keeps reminding the user.
 *
 * Call [cancelAllForMedication] before deactivating or re-writing a medication's schedules.
 */
@Singleton
class ReminderCleanup @Inject constructor(
    private val repository: MedicationRepository,
    private val alarmScheduler: AlarmScheduler,
    private val geofenceManager: GeofenceManager,
    private val notificationHelper: NotificationHelper
) {
    suspend fun cancelAllForMedication(medId: Int) {
        val scheduleIds = repository.getSchedulesForMedication(medId).map { it.id }.toSet()
        scheduleIds.forEach { cancelAllForSchedule(it) }
        geofenceManager.removeGeofencesForSchedules(scheduleIds)
    }

    /**
     * Cancels every alarm for one schedule, **including the daily repeat**. Only for
     * schedules that are going away — use [cancelPendingDose] when the schedule lives on.
     */
    fun cancelAllForSchedule(scheduleId: Int) {
        alarmScheduler.cancelAllAlarms(scheduleId)
        // The reminder notification is ongoing and non-dismissable, so it has to be
        // cancelled explicitly or it stays in the shade forever.
        notificationHelper.cancelNotification(scheduleId)
    }

    /**
     * Closes out today's dose — dismisses the notification and drops its snooze and nag
     * alarms — while leaving the daily repeat armed so tomorrow still fires.
     *
     * @param logId when known, also tears down a pending home-arrival geofence for the dose.
     *   Without it, a dose that was location-snoozed and then taken in the app still fires a
     *   reminder on arriving home.
     */
    fun cancelPendingDose(scheduleId: Int, logId: Int? = null) {
        alarmScheduler.cancelSnoozeAlarm(scheduleId)
        alarmScheduler.cancelNagAlarm(scheduleId)
        notificationHelper.cancelNotification(scheduleId)
        if (logId != null) geofenceManager.removeGeofence(logId)
    }
}
