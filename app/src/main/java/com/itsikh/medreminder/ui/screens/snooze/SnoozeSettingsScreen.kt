package com.itsikh.medreminder.ui.screens.snooze

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

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
                "Set three snooze lengths (in minutes) that appear as quick actions in your notifications.",
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
private fun SlotField(label: String, value: Int, modifier: Modifier, onChange: (Int) -> Unit) {
    OutlinedTextField(
        value = if (value <= 0) "" else value.toString(),
        onValueChange = { s -> s.toIntOrNull()?.let { onChange(it) } },
        label = { Text(label) },
        suffix = { Text("min") },
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
