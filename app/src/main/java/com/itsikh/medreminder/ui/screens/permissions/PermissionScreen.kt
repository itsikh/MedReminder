package com.itsikh.medreminder.ui.screens.permissions

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

private enum class PermStep { NOTIFICATIONS, LOCATION, BACKGROUND_LOCATION, EXACT_ALARM, DONE }

private fun isGranted(context: Context, permission: String): Boolean =
    ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

private fun needsNotifications(context: Context): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        !isGranted(context, Manifest.permission.POST_NOTIFICATIONS)

private fun needsExactAlarms(context: Context): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
        context.getSystemService(AlarmManager::class.java)?.canScheduleExactAlarms() == false

/**
 * The next permission to ask for, skipping anything the user has declined this session.
 *
 * [skipped] is what makes the "Skip for now" buttons work: without it they recomputed the
 * same step and did nothing, so declining an optional permission left the user stranded
 * on this screen with no way into the app.
 */
private fun computeStep(context: Context, skipped: Set<PermStep>): PermStep {
    if (needsNotifications(context) && PermStep.NOTIFICATIONS !in skipped) return PermStep.NOTIFICATIONS

    if (!isGranted(context, Manifest.permission.ACCESS_FINE_LOCATION) &&
        PermStep.LOCATION !in skipped
    ) return PermStep.LOCATION

    // Only meaningful once foreground location is granted.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
        isGranted(context, Manifest.permission.ACCESS_FINE_LOCATION) &&
        !isGranted(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) &&
        PermStep.BACKGROUND_LOCATION !in skipped
    ) return PermStep.BACKGROUND_LOCATION

    if (needsExactAlarms(context) && PermStep.EXACT_ALARM !in skipped) return PermStep.EXACT_ALARM

    return PermStep.DONE
}

/**
 * Whether the app can start straight at the home screen.
 *
 * Only notification delivery is genuinely required. Location powers the optional "snooze
 * until I'm home" action and must never block access to the app — it can be granted later
 * from Snooze & Location settings.
 */
fun allPermissionsGranted(context: Context): Boolean =
    !needsNotifications(context) && !needsExactAlarms(context)

@Composable
fun PermissionScreen(onAllGranted: () -> Unit) {
    val context = LocalContext.current
    val skipped = remember { mutableStateOf(emptySet<PermStep>()) }
    var step by remember { mutableStateOf(computeStep(context, skipped.value)) }

    fun recompute() { step = computeStep(context, skipped.value) }
    fun skipCurrent() {
        skipped.value = skipped.value + step
        recompute()
    }

    LaunchedEffect(step) {
        if (step == PermStep.DONE) onAllGranted()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) recompute()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val notifPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        // A denial here is terminal — Android stops showing the dialog after the second
        // refusal, so treat it as skipped rather than looping on the same step.
        if (!granted) skipped.value = skipped.value + PermStep.NOTIFICATIONS
        recompute()
    }

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms[Manifest.permission.ACCESS_FINE_LOCATION] != true) {
            skipped.value = skipped.value + PermStep.LOCATION
        }
        recompute()
    }

    val bgLocationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) skipped.value = skipped.value + PermStep.BACKGROUND_LOCATION
        recompute()
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Two of the four steps are optional now, so the heading follows the step
            // rather than claiming everything is required.
            val optionalStep = step == PermStep.LOCATION || step == PermStep.BACKGROUND_LOCATION
            Text(
                if (optionalStep) "Optional Permission" else "Permission Required",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                if (optionalStep)
                    "This one only powers the \"snooze until I'm home\" action. You can skip it and set it up later in Snooze & Location."
                else
                    "MedReminder needs this to remind you about your medications.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(32.dp))

            when (step) {
                PermStep.NOTIFICATIONS -> {
                    PermissionItem(
                        icon = Icons.Default.Notifications,
                        title = "Notifications",
                        description = "Required — this is how reminders reach you when it's time to take your medication."
                    )
                    Spacer(Modifier.height(32.dp))
                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Allow Notifications") }
                    Spacer(Modifier.height(8.dp))
                    TextButton(
                        onClick = { context.startActivity(appSettingsIntent(context)) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Open app settings instead") }
                    Spacer(Modifier.height(4.dp))
                    TextButton(onClick = { skipCurrent() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Continue without notifications")
                    }
                }

                PermStep.LOCATION -> {
                    PermissionItem(
                        icon = Icons.Default.LocationOn,
                        title = "Location (optional)",
                        description = "Only used for the \"snooze until I'm home\" action. Skip this if you don't want it — everything else works without it."
                    )
                    Spacer(Modifier.height(32.dp))
                    Button(
                        onClick = {
                            locationLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Allow Location") }
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { skipCurrent() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Skip — I don't need home reminders")
                    }
                }

                PermStep.BACKGROUND_LOCATION -> {
                    PermissionItem(
                        icon = Icons.Default.LocationOn,
                        title = "Background Location (optional)",
                        description = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                            "Needed to notice you've arrived home while the app is closed. Android only allows this from app settings — choose Permissions → Location → \"Allow all the time\"."
                        else
                            "Needed to notice you've arrived home while the app is closed."
                    )
                    Spacer(Modifier.height(32.dp))
                    Button(
                        onClick = {
                            // From Android 11 a runtime request for background location is
                            // auto-denied without any UI; app settings is the only route.
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                context.startActivity(appSettingsIntent(context))
                            } else {
                                bgLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) "Open App Settings"
                            else "Grant Background Location"
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { skipCurrent() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Skip for now")
                    }
                }

                PermStep.EXACT_ALARM -> {
                    PermissionItem(
                        icon = Icons.Default.Alarm,
                        title = "Exact Alarms",
                        description = "Required to deliver medication reminders at the exact scheduled time."
                    )
                    Spacer(Modifier.height(32.dp))
                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                context.startActivity(
                                    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                        data = Uri.parse("package:${context.packageName}")
                                    }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Open Settings") }
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { skipCurrent() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Continue anyway (reminders may be delayed)")
                    }
                }

                PermStep.DONE -> Unit
            }
        }
    }
}

/** This app's entry in system Settings, where permissions Android won't prompt for live. */
private fun appSettingsIntent(context: Context): Intent =
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))

@Composable
private fun PermissionItem(icon: ImageVector, title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
