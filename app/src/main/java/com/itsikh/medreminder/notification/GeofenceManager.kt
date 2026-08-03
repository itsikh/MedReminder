package com.itsikh.medreminder.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.itsikh.medreminder.data.preferences.SnoozePrefs
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeofenceManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val snoozePrefs: SnoozePrefs,
    private val alarmManager: AlarmManager
) {
    private val client: GeofencingClient = LocationServices.getGeofencingClient(context)

    /** Returns true if location permission is available for geofencing. */
    fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

    /**
     * Registers a home-arrival geofence that fires a reminder notification
     * once the user is home.
     *
     * With an arrival delay configured (the default), the geofence uses a DWELL
     * transition so it only fires after the user has stayed inside the radius for
     * that long — driving past home no longer pops the reminder. ENTER is also
     * registered so a fallback location check can be armed in case DWELL never
     * arrives (it is not equally reliable across devices).
     */
    fun registerHomeGeofence(
        logId: Int, scheduleId: Int, medicationId: Int,
        medName: String, dosage: String, scheduledTime: Long
    ): Boolean {
        // Returns false rather than silently doing nothing: the caller has already marked
        // the dose snoozed and dismissed the notification, so a failure here would lose the
        // reminder outright. Reachable when location permission is revoked while a reminder
        // is on screen.
        if (!hasLocationPermission() || !snoozePrefs.hasHomeLocation) return false

        val delayMs = arrivalDelayMs()
        val geofence = Geofence.Builder()
            .setRequestId(geofenceId(logId))
            .setCircularRegion(snoozePrefs.homeLat, snoozePrefs.homeLng, radiusMeters())
            .setExpirationDuration(24 * 60 * 60_000L)
            .apply {
                if (delayMs > 0) {
                    setTransitionTypes(
                        Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL
                    )
                    setLoiteringDelay(delayMs.toInt())
                } else {
                    setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                }
            }
            .build()

        val initialTrigger = if (delayMs > 0)
            GeofencingRequest.INITIAL_TRIGGER_ENTER or GeofencingRequest.INITIAL_TRIGGER_DWELL
        else
            GeofencingRequest.INITIAL_TRIGGER_ENTER

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(initialTrigger)
            .addGeofence(geofence)
            .build()

        snoozePrefs.savePendingGeofence(logId, scheduleId, medicationId, medName, dosage, scheduledTime)

        return try {
            client.addGeofences(request, pendingIntent(logId))
            true
        } catch (_: SecurityException) {
            // Permission revoked between the check and the call.
            snoozePrefs.clearPendingGeofence(logId)
            false
        }
    }

    fun removeGeofence(logId: Int) {
        client.removeGeofences(listOf(geofenceId(logId)))
        cancelArrivalCheck(logId)
        snoozePrefs.clearPendingGeofence(logId)
    }

    /**
     * Tears down every pending home geofence belonging to [scheduleIds]. Called when a
     * medication is edited or removed — otherwise the geofence survives and fires a
     * reminder for a medication that no longer exists.
     */
    fun removeGeofencesForSchedules(scheduleIds: Set<Int>) {
        // Runs even for an empty set so stale index entries (info == null) are still swept.
        snoozePrefs.pendingGeofenceLogIds.mapNotNull { it.toIntOrNull() }.forEach { logId ->
            val info = snoozePrefs.getPendingGeofence(logId)
            if (info == null || info.scheduleId in scheduleIds) removeGeofence(logId)
        }
    }

    /** Configured home-arrival wait, in milliseconds. 0 means notify on entry. */
    fun arrivalDelayMs(): Long = snoozePrefs.homeArrivalDelayMinutes.coerceAtLeast(0) * 60_000L

    /** Configured home radius, clamped to a range Play Services can actually honour. */
    fun radiusMeters(): Float =
        snoozePrefs.homeRadiusMeters.coerceIn(MIN_RADIUS_METERS, MAX_RADIUS_METERS).toFloat()

    // ── Arrival checks ────────────────────────────────────────────────────────
    // Armed on ENTER. If the DWELL transition arrives first the reminder fires from
    // there and this alarm is cancelled; otherwise the alarm re-checks whether the
    // user is home. With a home Wi-Fi network saved the check repeats cheaply every
    // [POLL_INTERVAL_MS] so association with the home AP is noticed promptly instead
    // of waiting out the full arrival delay.

    /**
     * How long to wait before the next arrival check, given how much of the arrival
     * delay is left. Wi-Fi polling only makes sense when a home network is configured.
     */
    fun nextCheckDelayMs(remainingMs: Long): Long =
        if (snoozePrefs.hasHomeWifi) minOf(POLL_INTERVAL_MS, remainingMs + CHECK_SLACK_MS)
        else remainingMs + CHECK_SLACK_MS

    fun scheduleArrivalCheck(logId: Int, delayMs: Long) {
        val triggerAt = System.currentTimeMillis() + delayMs.coerceAtLeast(0L)
        val pi = arrivalCheckPendingIntent(logId, PendingIntent.FLAG_UPDATE_CURRENT) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }

    fun cancelArrivalCheck(logId: Int) {
        arrivalCheckPendingIntent(logId, PendingIntent.FLAG_NO_CREATE)?.let { alarmManager.cancel(it) }
    }

    /**
     * Whether the device is currently inside the home radius.
     * Returns null when the location is unknown — callers decide what to do with that.
     */
    @SuppressLint("MissingPermission")
    suspend fun isAtHome(): Boolean? {
        if (!hasLocationPermission() || !snoozePrefs.hasHomeLocation) return null
        val fused = LocationServices.getFusedLocationProviderClient(context)
        // One timeout around both attempts: the lastLocation fallback used to sit outside it,
        // so a hang there could overrun the receiver's goAsync() window unbounded.
        val loc: Location? = try {
            withTimeoutOrNull(LOCATION_FIX_TIMEOUT_MS) {
                fused.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null).await()
                    ?: fused.lastLocation.await()
            }
        } catch (_: Exception) {
            null
        }
        if (loc == null) return null
        val out = FloatArray(1)
        Location.distanceBetween(
            loc.latitude, loc.longitude,
            snoozePrefs.homeLat, snoozePrefs.homeLng,
            out
        )
        // Accuracy slack: a fix reported 20 m outside a 150 m fence is still "home".
        return out[0] <= radiusMeters() + loc.accuracy.coerceAtMost(MAX_ACCURACY_SLACK_METERS)
    }

    // ── private helpers ───────────────────────────────────────────────────────

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

    private fun arrivalCheckPendingIntent(logId: Int, flag: Int): PendingIntent? {
        val intent = Intent(ACTION_HOME_ARRIVAL_CHECK, null, context, GeofenceReceiver::class.java)
            .apply { putExtra(EXTRA_LOG_ID, logId) }
        return PendingIntent.getBroadcast(
            context, logId + 30_000, intent,
            flag or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val EXTRA_LOG_ID = "gf_log_id"

        /** Alarm action for the fallback "are we actually home yet?" check. */
        const val ACTION_HOME_ARRIVAL_CHECK = "com.itsikh.medreminder.HOME_ARRIVAL_CHECK"

        /** Bounds on the user-configurable home radius, in metres. */
        const val MIN_RADIUS_METERS = 50
        const val MAX_RADIUS_METERS = 1000

        /** Cap on how much reported GPS error is forgiven by the fallback check. */
        private const val MAX_ACCURACY_SLACK_METERS = 50f

        /** Extra margin so the DWELL transition gets a chance to arrive first. */
        private const val CHECK_SLACK_MS = 60_000L

        /** Gap between Wi-Fi arrival polls while the arrival delay is still running. */
        private const val POLL_INTERVAL_MS = 2 * 60_000L

        /**
         * Upper bound on waiting for a fresh fix inside a broadcast receiver. Together with
         * the Wi-Fi lookup this has to stay well inside goAsync()'s ~10s window, or the
         * receiver is killed before it can post the reminder.
         */
        private const val LOCATION_FIX_TIMEOUT_MS = 5_000L
    }
}
