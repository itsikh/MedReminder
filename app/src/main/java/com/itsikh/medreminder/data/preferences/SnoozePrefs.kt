package com.itsikh.medreminder.data.preferences

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Synchronous SharedPreferences wrapper for snooze durations and home location.
 * Intentionally synchronous — called from BroadcastReceiver / notification contexts.
 */
@Singleton
class SnoozePrefs @Inject constructor(@ApplicationContext context: Context) {

    private val prefs = context.getSharedPreferences("snooze_prefs", Context.MODE_PRIVATE)

    // ── Snooze duration slots (in minutes) ────────────────────────────────────

    var slot1: Int
        get() = prefs.getInt("slot1", 15)
        set(v) { prefs.edit().putInt("slot1", v).apply() }

    var slot2: Int
        get() = prefs.getInt("slot2", 30)
        set(v) { prefs.edit().putInt("slot2", v).apply() }

    var slot3: Int
        get() = prefs.getInt("slot3", 60)
        set(v) { prefs.edit().putInt("slot3", v).apply() }

    // ── Home location ─────────────────────────────────────────────────────────

    var homeLat: Double
        get() = java.lang.Double.longBitsToDouble(
            prefs.getLong("home_lat", java.lang.Double.doubleToLongBits(Double.NaN))
        )
        set(v) { prefs.edit().putLong("home_lat", java.lang.Double.doubleToLongBits(v)).apply() }

    var homeLng: Double
        get() = java.lang.Double.longBitsToDouble(
            prefs.getLong("home_lng", java.lang.Double.doubleToLongBits(Double.NaN))
        )
        set(v) { prefs.edit().putLong("home_lng", java.lang.Double.doubleToLongBits(v)).apply() }

    val hasHomeLocation: Boolean
        get() = !homeLat.isNaN() && !homeLng.isNaN()

    fun clearHomeLocation() {
        prefs.edit().remove("home_lat").remove("home_lng").apply()
    }

    // ── Pending geofence alarm info ───────────────────────────────────────────
    // Stored so GeofenceReceiver can fire the right notification when user arrives home.

    fun savePendingGeofence(
        logId: Int, scheduleId: Int, medicationId: Int,
        medName: String, dosage: String, scheduledTime: Long
    ) {
        prefs.edit()
            .putInt("gf_${logId}_sched", scheduleId)
            .putInt("gf_${logId}_med", medicationId)
            .putString("gf_${logId}_name", medName)
            .putString("gf_${logId}_dosage", dosage)
            .putLong("gf_${logId}_time", scheduledTime)
            .apply()
    }

    data class PendingGeofenceInfo(
        val scheduleId: Int, val medicationId: Int,
        val medName: String, val dosage: String, val scheduledTime: Long
    )

    fun getPendingGeofence(logId: Int): PendingGeofenceInfo? {
        val scheduleId = prefs.getInt("gf_${logId}_sched", -1)
        if (scheduleId == -1) return null
        return PendingGeofenceInfo(
            scheduleId = scheduleId,
            medicationId = prefs.getInt("gf_${logId}_med", -1),
            medName = prefs.getString("gf_${logId}_name", "") ?: "",
            dosage = prefs.getString("gf_${logId}_dosage", "") ?: "",
            scheduledTime = prefs.getLong("gf_${logId}_time", 0)
        )
    }

    fun clearPendingGeofence(logId: Int) {
        prefs.edit()
            .remove("gf_${logId}_sched").remove("gf_${logId}_med")
            .remove("gf_${logId}_name").remove("gf_${logId}_dosage")
            .remove("gf_${logId}_time")
            .apply()
    }

    // ── Nag interval ──────────────────────────────────────────────────────────────
    // How many minutes between re-notifications when a reminder is not acknowledged.

    var nagIntervalMinutes: Int
        get() = prefs.getInt("nag_interval_min", 10)
        set(v) { prefs.edit().putInt("nag_interval_min", v).apply() }

    // ── Notification sound ────────────────────────────────────────────────────────
    // Empty string means "system default". Non-empty is a URI string for the chosen ringtone.
    // null-URI (silent) is stored as the literal string "silent".

    var notificationSoundUri: String
        get() = prefs.getString("notif_sound_uri", "") ?: ""
        set(v) { prefs.edit().putString("notif_sound_uri", v).apply() }

    var notificationChannelVersion: Int
        get() = prefs.getInt("notif_channel_ver", 0)
        set(v) { prefs.edit().putInt("notif_channel_ver", v).apply() }

    /** The active medication notification channel ID — changes when the user picks a new sound. */
    val currentMedChannelId: String
        get() {
            val ver = notificationChannelVersion
            return if (ver == 0) "channel_medication" else "channel_medication_v$ver"
        }
}
