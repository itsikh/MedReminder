package com.itsikh.medreminder.ui.screens.medication

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.Calendar

private val MED_COLORS = listOf(
    0xFF4CAF50.toInt(), 0xFF2196F3.toInt(), 0xFFFF9800.toInt(),
    0xFF9C27B0.toInt(), 0xFFF44336.toInt(), 0xFF00BCD4.toInt(),
    0xFFFF69B4.toInt(), 0xFFFFEB3B.toInt()
)

// Calendar day values for Mon→Sun display
private val DAY_LABELS = listOf("M", "T", "W", "T", "F", "S", "S")
private val DAY_CAL_VALUES = listOf(2, 3, 4, 5, 6, 7, 1) // Calendar.MONDAY..SUNDAY

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    medId: Int?,
    onBack: () -> Unit,
    viewModel: AddEditViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isEditMode = medId != null && medId > 0
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(medId) {
        if (isEditMode) viewModel.loadMedication(medId!!)
    }
    LaunchedEffect(viewModel.isSaved) {
        if (viewModel.isSaved) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Medication" else "Add Medication", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                actions = {
                    if (isEditMode) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Name ────────────────────────────────────────────────────────
            OutlinedTextField(
                value = viewModel.name,
                onValueChange = { viewModel.name = it },
                label = { Text("Medication name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // ── Dosage ───────────────────────────────────────────────────────
            OutlinedTextField(
                value = viewModel.dosage,
                onValueChange = { viewModel.dosage = it },
                label = { Text("Dosage (optional, e.g. 500mg)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // ── Color ────────────────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Color", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MED_COLORS.forEach { c ->
                        val selected = viewModel.color == c
                        Box(
                            Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(c))
                                .then(if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier)
                                .clickable { viewModel.color = c }
                        )
                    }
                }
            }

            // ── Days of week ─────────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Repeat on", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    DAY_LABELS.forEachIndexed { i, label ->
                        val calDay = DAY_CAL_VALUES[i]
                        val isOn = (viewModel.daysOfWeek and (1 shl (calDay - 1))) != 0
                        FilterChip(
                            selected = isOn,
                            onClick = { viewModel.toggleDay(calDay) },
                            label = { Text(label) },
                            modifier = Modifier.size(38.dp),
                            shape = CircleShape
                        )
                    }
                }
            }

            // ── Time slots ───────────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Reminder times *", style = MaterialTheme.typography.labelLarge)
                viewModel.timeSlots.forEachIndexed { idx, (h, m) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⏰ %02d:%02d".format(h, m),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f))
                        IconButton(onClick = { viewModel.removeTimeSlot(idx) }) {
                            Icon(Icons.Default.Close, "Remove", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                }
                OutlinedButton(
                    onClick = {
                        val now = Calendar.getInstance()
                        TimePickerDialog(
                            context,
                            { _, h, m -> viewModel.addTimeSlot(h, m) },
                            now.get(Calendar.HOUR_OF_DAY),
                            now.get(Calendar.MINUTE),
                            true
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Add time")
                }
            }

            // ── Save ─────────────────────────────────────────────────────────
            Button(
                onClick = { viewModel.save(medId) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = viewModel.name.isNotBlank() && viewModel.timeSlots.isNotEmpty()
            ) {
                Text(if (isEditMode) "Save changes" else "Add medication",
                    style = MaterialTheme.typography.labelLarge)
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Remove medication?") },
            text = { Text("All schedules and future reminders will be cancelled.") },
            confirmButton = {
                TextButton(onClick = { medId?.let { viewModel.delete(it) }; showDeleteConfirm = false }) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } }
        )
    }
}
