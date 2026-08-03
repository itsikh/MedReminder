package com.itsikh.medreminder.notification

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.itsikh.medreminder.data.preferences.SnoozePrefs
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Reads the connected Wi-Fi network name so "am I home?" can be answered without GPS.
 *
 * Being associated with the home access point is a far stronger signal than a geofence —
 * it means the user is physically inside the house, not driving past it. Wi-Fi cannot
 * *wake* the app on its own though (implicit connectivity broadcasts are blocked for
 * manifest receivers since Android 8), so the geofence still provides the wake-ups and
 * this only confirms them.
 */
@Singleton
class HomeWifiDetector @Inject constructor(
    @ApplicationContext private val context: Context,
    private val snoozePrefs: SnoozePrefs
) {
    /** True only when the device is positively confirmed to be on the saved home network. */
    suspend fun isOnHomeWifi(): Boolean {
        val home = snoozePrefs.homeWifiSsid
        if (home.isBlank()) return false
        return currentSsid()?.equals(home, ignoreCase = true) == true
    }

    /**
     * SSID of the connected Wi-Fi network, or null when not on Wi-Fi or the name is
     * withheld. Android only reveals the SSID to apps holding ACCESS_FINE_LOCATION
     * *and* with system location services switched on.
     */
    @SuppressLint("MissingPermission")
    suspend fun currentSsid(): String? {
        val cm = context.getSystemService(ConnectivityManager::class.java) ?: return null
        val active = cm.activeNetwork ?: return null
        val caps = cm.getNetworkCapabilities(active) ?: return null
        if (!caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // On API 31+ the synchronous capabilities above have the SSID redacted;
            // only a callback registered with FLAG_INCLUDE_LOCATION_INFO exposes it.
            return ssidFromCallback(cm)
        }

        @Suppress("DEPRECATION")
        return normalize(
            (context.getSystemService(Context.WIFI_SERVICE) as? WifiManager)?.connectionInfo?.ssid
        )
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private suspend fun ssidFromCallback(cm: ConnectivityManager): String? {
        var registered: ConnectivityManager.NetworkCallback? = null
        return try {
            withTimeoutOrNull(CALLBACK_TIMEOUT_MS) {
                suspendCancellableCoroutine { cont ->
                    val callback = object : ConnectivityManager.NetworkCallback(
                        FLAG_INCLUDE_LOCATION_INFO
                    ) {
                        override fun onCapabilitiesChanged(
                            network: Network,
                            caps: NetworkCapabilities
                        ) {
                            if (cont.isActive) cont.resume((caps.transportInfo as? WifiInfo)?.ssid)
                        }
                    }
                    registered = callback
                    val request = NetworkRequest.Builder()
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                        .build()
                    cm.registerNetworkCallback(request, callback)
                }
            }.let(::normalize)
        } catch (_: Exception) {
            null
        } finally {
            registered?.let { runCatching { cm.unregisterNetworkCallback(it) } }
        }
    }

    /** Strips the quotes Android wraps SSIDs in and filters out the withheld placeholder. */
    private fun normalize(raw: String?): String? {
        val ssid = raw?.trim()?.removeSurrounding("\"")?.trim().orEmpty()
        if (ssid.isEmpty() || ssid == UNKNOWN_SSID || ssid == "0x") return null
        return ssid
    }

    companion object {
        private const val UNKNOWN_SSID = "<unknown ssid>"

        /**
         * Kept short on purpose. This runs inside a BroadcastReceiver's goAsync() window,
         * which is only ~10s in total and is also shared with a location fix — overrunning
         * it means the receiver is killed and the reminder is lost entirely.
         */
        private const val CALLBACK_TIMEOUT_MS = 2_000L
    }
}
