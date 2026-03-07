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

private enum class PermStep { CORE, BACKGROUND_LOCATION, EXACT_ALARM, DONE }

private fun computeStep(context: Context): PermStep {
    val needsNotification = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    val needsFineLocation = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) != PackageManager.PERMISSION_GRANTED

    if (needsNotification || needsFineLocation) return PermStep.CORE

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED
    ) return PermStep.BACKGROUND_LOCATION

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        if (!alarmManager.canScheduleExactAlarms()) return PermStep.EXACT_ALARM
    }

    return PermStep.DONE
}

fun allPermissionsGranted(context: Context): Boolean = computeStep(context) == PermStep.DONE

@Composable
fun PermissionScreen(onAllGranted: () -> Unit) {
    val context = LocalContext.current
    var step by remember { mutableStateOf(computeStep(context)) }

    LaunchedEffect(step) {
        if (step == PermStep.DONE) onAllGranted()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                step = computeStep(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val corePermsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { step = computeStep(context) }

    val bgLocationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { step = computeStep(context) }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Permissions Required",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "MedReminder needs the following permissions to remind you about your medications.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(32.dp))

            when (step) {
                PermStep.CORE -> {
                    PermissionItem(
                        icon = Icons.Default.Notifications,
                        title = "Notifications",
                        description = "To alert you when it's time to take your medication."
                    )
                    Spacer(Modifier.height(12.dp))
                    PermissionItem(
                        icon = Icons.Default.LocationOn,
                        title = "Location",
                        description = "To snooze reminders until you arrive home."
                    )
                    Spacer(Modifier.height(32.dp))
                    Button(
                        onClick = {
                            val perms = buildList {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                                    add(Manifest.permission.POST_NOTIFICATIONS)
                                add(Manifest.permission.ACCESS_FINE_LOCATION)
                            }.toTypedArray()
                            corePermsLauncher.launch(perms)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Grant Permissions") }
                }

                PermStep.BACKGROUND_LOCATION -> {
                    PermissionItem(
                        icon = Icons.Default.LocationOn,
                        title = "Background Location",
                        description = "Required to detect when you arrive home and trigger snoozed medication reminders."
                    )
                    Spacer(Modifier.height(32.dp))
                    Button(
                        onClick = {
                            bgLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Grant Background Location") }
                    Spacer(Modifier.height(8.dp))
                    TextButton(
                        onClick = { step = computeStep(context) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Skip for now") }
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
                    TextButton(
                        onClick = { step = computeStep(context) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Already granted? Continue") }
                }

                PermStep.DONE -> Unit
            }
        }
    }
}

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
