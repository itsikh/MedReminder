package com.itsikh.medreminder.data.preferences

import android.content.Context
import com.itsikh.medreminder.AppConfig
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
        get() = prefs.getInt("slot1", 30)
        set(v) { prefs.edit().putInt("slot1", v).apply() }

    var slot2: Int
        get() = prefs.getInt("slot2", 60)
        set(v) { prefs.edit().putInt("slot2", v).apply() }

    var slot3: Int
        get() = prefs.getInt("slot3", 120)
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

    /**
     * How long the user must stay inside the home radius before an "At home" snooze
     * actually notifies. Geofences trigger on the edge of the radius, so driving past
     * home — or stopping at the corner — used to pop the reminder. 0 disables the wait.
     */
    var homeArrivalDelayMinutes: Int
        get() = prefs.getInt("home_arrival_delay_min", 10)
        set(v) { prefs.edit().putInt("home_arrival_delay_min", v).apply() }

    /**
     * Radius of the home geofence, in metres. Play Services becomes unreliable below
     * ~100 m because it falls back to coarse location sources, so keep it generous
     * and use [homeArrivalDelayMinutes] to filter out drive-bys.
     */
    var homeRadiusMeters: Int
        get() = prefs.getInt("home_radius_m", 150)
        set(v) { prefs.edit().putInt("home_radius_m", v).apply() }

    /**
     * Name of the home Wi-Fi network. Empty means not configured. Being associated with
     * it proves the user is inside the house, so the arrival wait can be skipped.
     */
    var homeWifiSsid: String
        get() = prefs.getString("home_wifi_ssid", "") ?: ""
        set(v) { prefs.edit().putString("home_wifi_ssid", v).apply() }

    val hasHomeWifi: Boolean
        get() = homeWifiSsid.isNotBlank()

    fun clearHomeWifi() {
        prefs.edit().remove("home_wifi_ssid").apply()
    }

    // ── Pending geofence alarm info ───────────────────────────────────────────
    // Stored so GeofenceReceiver can fire the right notification when user arrives home.

    // The gf_ids index is read-modify-written, and both a geofence firing and a user action
    // can touch it concurrently from different receivers. Serialised so a concurrent
    // save/clear cannot drop an entry and leave a geofence with nothing tracking it.
    private val geofenceIndexLock = Any()

    fun savePendingGeofence(
        logId: Int, scheduleId: Int, medicationId: Int,
        medName: String, dosage: String, scheduledTime: Long
    ) {
        synchronized(geofenceIndexLock) {
            prefs.edit()
                .putInt("gf_${logId}_sched", scheduleId)
                .putInt("gf_${logId}_med", medicationId)
                .putString("gf_${logId}_name", medName)
                .putString("gf_${logId}_dosage", dosage)
                .putLong("gf_${logId}_time", scheduledTime)
                .putStringSet("gf_ids", pendingGeofenceLogIds + logId.toString())
                .apply()
        }
    }

    /**
     * Log IDs with a registered home geofence. Kept as an explicit index so pending
     * geofences can be found and torn down when their medication is edited or deleted —
     * without it, a deleted medication still reminds you when you get home.
     */
    val pendingGeofenceLogIds: Set<String>
        get() = prefs.getStringSet("gf_ids", emptySet()) ?: emptySet()

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
        synchronized(geofenceIndexLock) {
            prefs.edit()
                .remove("gf_${logId}_sched").remove("gf_${logId}_med")
                .remove("gf_${logId}_name").remove("gf_${logId}_dosage")
                .remove("gf_${logId}_time").remove("gf_${logId}_entered")
                .putStringSet("gf_ids", pendingGeofenceLogIds - logId.toString())
                .apply()
        }
    }

    /**
     * When the user last crossed into the home radius, which starts the arrival wait.
     * Re-entering restarts it. 0 means no entry is currently being tracked.
     */
    fun setGeofenceEnteredAt(logId: Int, timestamp: Long) {
        prefs.edit().putLong("gf_${logId}_entered", timestamp).apply()
    }

    fun getGeofenceEnteredAt(logId: Int): Long = prefs.getLong("gf_${logId}_entered", 0L)

    // ── Nag interval ──────────────────────────────────────────────────────────────
    // How many minutes between re-notifications when a reminder is not acknowledged.

    var nagIntervalMinutes: Int
        get() = prefs.getInt("nag_interval_min", 10)
        set(v) { prefs.edit().putInt("nag_interval_min", v).apply() }

    /**
     * How many times an unacknowledged reminder re-alerts before giving up. Without a cap
     * a dose you never respond to wakes you every [nagIntervalMinutes] indefinitely.
     * 0 disables re-alerting entirely.
     */
    var nagRepeatLimit: Int
        get() = prefs.getInt("nag_repeat_limit", 6)
        set(v) { prefs.edit().putInt("nag_repeat_limit", v).apply() }

    // ── Notification sound ────────────────────────────────────────────────────────
    // Empty string means "system default". Non-empty is a URI string for the chosen ringtone.
    // null-URI (silent) is stored as the literal string "silent".

    var notificationSoundUri: String
        get() = prefs.getString("notif_sound_uri", "") ?: ""
        set(v) { prefs.edit().putString("notif_sound_uri", v).apply() }

    var notificationChannelVersion: Int
        get() = prefs.getInt("notif_channel_ver", 0)
        set(v) { prefs.edit().putInt("notif_channel_ver", v).apply() }

    /**
     * The active medication notification channel ID — changes when the user picks a new sound.
     *
     * Derived from [AppConfig.NOTIFICATION_CHANNEL_MEDICATION] rather than a literal so it
     * cannot drift from the ID [ui.screens.settings.SettingsViewModel] creates. A mismatch
     * would post reminders to a channel that does not exist, and Android drops those silently.
     */
    val currentMedChannelId: String
        get() {
            val base = AppConfig.NOTIFICATION_CHANNEL_MEDICATION
            val ver = notificationChannelVersion
            return if (ver == 0) base else "${base}_v$ver"
        }

    // ── Quiet hours ───────────────────────────────────────────────────────────────
    // When enabled, reminders that fall inside the window are silently marked MISSED.

    var quietHourEnabled: Boolean
        get() = prefs.getBoolean("quiet_hour_enabled", false)
        set(v) { prefs.edit().putBoolean("quiet_hour_enabled", v).apply() }

    /** Hour the quiet window opens, 0-23. */
    var quietHour: Int
        get() = prefs.getInt("quiet_hour", 22)
        set(v) { prefs.edit().putInt("quiet_hour", v).apply() }

    /**
     * Hour the quiet window closes, 0-23. The window wraps midnight when this is
     * less than [quietHour] — without an end hour, a 22:00 cutoff compared with
     * `hour >= 22` never matched at 01:00, so overnight reminders still fired.
     */
    var quietHourEnd: Int
        get() = prefs.getInt("quiet_hour_end", 7)
        set(v) { prefs.edit().putInt("quiet_hour_end", v).apply() }

    /** Whether [hour] falls inside the configured quiet window, handling midnight wrap. */
    fun isQuietHour(hour: Int): Boolean {
        if (!quietHourEnabled) return false
        val start = quietHour
        val end = quietHourEnd
        if (start == end) return false
        return if (start < end) hour in start until end else hour >= start || hour < end
    }
}
