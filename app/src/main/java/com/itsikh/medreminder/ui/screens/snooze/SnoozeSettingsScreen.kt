package com.itsikh.medreminder.ui.screens.snooze

import android.Manifest
import android.app.TimePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.itsikh.medreminder.notification.GeofenceManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnoozeSettingsScreen(
    onBack: () -> Unit,
    viewModel: SnoozeSettingsViewModel = hiltViewModel()
) {
    val locationPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            viewModel.captureCurrentLocationAsHome()
        }
    }

    // Only the toolbar arrow used to persist these fields, so leaving via the system back
    // gesture silently discarded every edit on this screen.
    DisposableEffect(Unit) {
        onDispose { viewModel.saveSlots() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Snooze & Location", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.saveSlots()
                        onBack()
                    }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ── Snooze durations ──────────────────────────────────────────────
            SectionHeader(Icons.Default.Timer, "Custom snooze durations")
            Text(
                "Set three snooze lengths (in minutes). All three appear as quick actions in your notifications.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SlotField("Slot 1", viewModel.slot1, Modifier.weight(1f)) { viewModel.slot1 = it }
                SlotField("Slot 2", viewModel.slot2, Modifier.weight(1f)) { viewModel.slot2 = it }
                SlotField("Slot 3", viewModel.slot3, Modifier.weight(1f)) { viewModel.slot3 = it }
            }

            Text(
                "Preview: ${formatMin(viewModel.slot1)}  ·  ${formatMin(viewModel.slot2)}  ·  ${formatMin(viewModel.slot3)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )

            Button(onClick = { viewModel.saveSlots() }, modifier = Modifier.fillMaxWidth()) {
                Text("Save snooze settings")
            }

            HorizontalDivider()

            // ── Nag interval ──────────────────────────────────────────────────
            SectionHeader(Icons.Default.Timer, "Re-notify if not acknowledged")
            Text(
                "If you don't respond to a reminder, the app will re-alert you at this interval until you take action.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SlotField("Interval", viewModel.nagInterval, Modifier.weight(1f)) {
                    viewModel.nagInterval = it
                }
                SlotField("Max times", viewModel.nagRepeatLimit, Modifier.weight(1f), suffix = "×") {
                    viewModel.nagRepeatLimit = it
                }
            }
            Text(
                if (viewModel.nagRepeatLimit <= 0) "Never re-alert"
                else "Re-alert every ${formatMin(viewModel.nagInterval)}, up to " +
                    "${viewModel.nagRepeatLimit} ${if (viewModel.nagRepeatLimit == 1) "time" else "times"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Without a limit an unanswered reminder keeps alerting all night.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider()

            // ── Quiet hour cutoff ─────────────────────────────────────────────
            SectionHeader(Icons.Default.NotificationsOff, "Quiet hours")
            Text(
                "Inside this window, reminders are silently marked as missed instead of notifying you. " +
                    "The window may cross midnight.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Switch(
                    checked = viewModel.quietHourEnabled,
                    onCheckedChange = { viewModel.quietHourEnabled = it }
                )
                Text(
                    if (viewModel.quietHourEnabled) "Enabled" else "Disabled",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (viewModel.quietHourEnabled) {
                val ctx = LocalContext.current
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Quiet from:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(
                        onClick = {
                            TimePickerDialog(
                                ctx,
                                { _, h, _ -> viewModel.quietHour = h },
                                viewModel.quietHour, 0, true
                            ).show()
                        }
                    ) { Text("%02d:00".format(viewModel.quietHour)) }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Quiet until:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(
                        onClick = {
                            TimePickerDialog(
                                ctx,
                                { _, h, _ -> viewModel.quietHourEnd = h },
                                viewModel.quietHourEnd, 0, true
                            ).show()
                        }
                    ) { Text("%02d:00".format(viewModel.quietHourEnd)) }
                }
                Text(
                    if (viewModel.quietHour == viewModel.quietHourEnd)
                        "⚠️ Start and end are the same — quiet hours will have no effect."
                    else
                        "Silent between %02d:00 and %02d:00".format(viewModel.quietHour, viewModel.quietHourEnd),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (viewModel.quietHour == viewModel.quietHourEnd)
                        MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }

            HorizontalDivider()

            // ── Home location ─────────────────────────────────────────────────
            SectionHeader(Icons.Default.LocationOn, "Snooze until I'm home")
            Text(
                "Save your home location so you can snooze a reminder until you arrive. Requires location permission.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (viewModel.hasHomeLocation) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("📍 Home location saved",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer)
                        Text("Lat: ${"%.5f".format(viewModel.homeLat)}  Lng: ${"%.5f".format(viewModel.homeLng)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            if (viewModel.hasLocationPermission()) viewModel.captureCurrentLocationAsHome()
                            else locationPermLauncher.launch(arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ))
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Update location") }
                    OutlinedButton(
                        onClick = { viewModel.clearHomeLocation() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) { Text("Remove") }
                }
            } else {
                Button(
                    onClick = {
                        if (viewModel.hasLocationPermission()) viewModel.captureCurrentLocationAsHome()
                        else locationPermLauncher.launch(arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.LocationOn, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Set current location as home")
                }
            }

            if (viewModel.hasHomeLocation) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Arrival detection",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Location alerts trigger at the edge of the radius, so the reminder can pop " +
                        "while you're still driving past. The delay waits that long after you " +
                        "arrive — and only notifies if you're still home. 0 notifies immediately.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SlotField("Radius", viewModel.homeRadius, Modifier.weight(1f), suffix = "m") {
                        viewModel.homeRadius = it
                    }
                    SlotField("Delay", viewModel.homeArrivalDelay, Modifier.weight(1f)) {
                        viewModel.homeArrivalDelay = it
                    }
                }
                Text(
                    buildString {
                        append("Trigger within ${viewModel.homeRadius} m")
                        if (viewModel.homeArrivalDelay <= 0) append(", notify as soon as I arrive")
                        else append(", notify ${formatMin(viewModel.homeArrivalDelay)} after I get home")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                if (viewModel.homeRadius < 100) {
                    Text(
                        "⚠️ Below 100 m, Android geofencing gets unreliable and may not fire at " +
                            "all. Prefer a wider radius plus a longer delay.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Text(
                    "Allowed: ${GeofenceManager.MIN_RADIUS_METERS}–${GeofenceManager.MAX_RADIUS_METERS} m. " +
                        "Changes apply to the next \"At home\" snooze.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // ── Home Wi-Fi ────────────────────────────────────────────────
                Spacer(Modifier.height(4.dp))
                Text(
                    "Home Wi-Fi (recommended)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Joining your home network proves you're inside, not driving past — so the " +
                        "reminder fires right away instead of waiting out the delay. The delay " +
                        "still applies when Wi-Fi is off or you're away from the house.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (viewModel.homeWifiSsid.isNotBlank()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "📶 ${viewModel.homeWifiSsid}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { viewModel.captureCurrentWifiAsHome() },
                            modifier = Modifier.weight(1f)
                        ) { Text("Update network") }
                        OutlinedButton(
                            onClick = { viewModel.clearHomeWifi() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) { Text("Remove") }
                    }
                } else {
                    OutlinedButton(
                        onClick = { viewModel.captureCurrentWifiAsHome() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Wifi, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Use current Wi-Fi network as home")
                    }
                }

                if (viewModel.wifiLoading) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Reading Wi-Fi name…", style = MaterialTheme.typography.bodySmall)
                    }
                }

                viewModel.wifiError?.let { err ->
                    Text(err, color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall)
                }
            }

            if (viewModel.locationLoading) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Getting location…", style = MaterialTheme.typography.bodySmall)
                }
            }

            viewModel.locationError?.let { err ->
                Text(err, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SlotField(
    label: String,
    value: Int,
    modifier: Modifier,
    suffix: String = "min",
    onChange: (Int) -> Unit
) {
    OutlinedTextField(
        value = if (value < 0) "" else value.toString(),
        // Clearing the field means 0 — the only way to type a leading zero, which the
        // home-arrival delay uses to mean "notify immediately".
        onValueChange = { s -> if (s.isEmpty()) onChange(0) else s.toIntOrNull()?.let { onChange(it) } },
        label = { Text(label) },
        suffix = { Text(suffix) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = modifier
    )
}

private fun formatMin(minutes: Int): String = when {
    minutes < 60 -> "$minutes min"
    minutes % 60 == 0 -> "${minutes / 60} hr"
    else -> "${minutes / 60}h ${minutes % 60}m"
}
