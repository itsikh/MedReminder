package com.itsikh.medreminder.ui.screens.medication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.itsikh.medreminder.data.model.MedicationWithSchedules
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationListScreen(
    onAddNew: () -> Unit,
    onEdit: (Int) -> Unit,
    onOpenSettings: () -> Unit = {},
    viewModel: MedicationListViewModel = hiltViewModel()
) {
    val meds by viewModel.medications.collectAsState()
    var deleteTarget by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medications", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddNew) {
                Icon(Icons.Default.Add, contentDescription = "Add medication")
            }
        }
    ) { padding ->
        if (meds.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("💊", style = MaterialTheme.typography.displayMedium)
                    Text("No medications yet", style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(onClick = onAddNew) { Text("Add medication") }
                }
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(meds, key = { it.medication.id }) { item ->
                    MedListCard(
                        item = item,
                        onEdit = { onEdit(item.medication.id) },
                        onDelete = { deleteTarget = item.medication.id }
                    )
                }
            }
        }
    }

    deleteTarget?.let { id ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Remove medication?") },
            text = { Text("This will delete all schedules and cancel future reminders.") },
            confirmButton = {
                TextButton(onClick = { viewModel.delete(id); deleteTarget = null }) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun MedListCard(item: MedicationWithSchedules, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(14.dp).clip(CircleShape).background(Color(item.medication.color)))
            Column(Modifier.weight(1f)) {
                Text(item.medication.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (item.medication.dosage.isNotBlank())
                    Text(item.medication.dosage, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                val times = item.schedules
                    .filter { it.isEnabled }
                    .sortedBy { it.timeHour * 60 + it.timeMinute }
                    .joinToString(" · ") { "%02d:%02d".format(it.timeHour, it.timeMinute) }
                if (times.isNotBlank())
                    Text("⏰ $times", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                val daysLabel = daysLabel(item.schedules.firstOrNull()?.daysOfWeek ?: 0x7F)
                Text(daysLabel, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                val med = item.medication
                if (med.stockQuantity >= 0) {
                    val pct = if (med.stockInitial > 0) med.stockQuantity * 100 / med.stockInitial else 100
                    val isLow = med.stockInitial > 0 && pct <= med.lowStockThresholdPct
                    Text(
                        "📦 ${med.stockQuantity} left${if (med.stockInitial > 0) " ($pct%)" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isLow) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit") }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

private fun daysLabel(daysOfWeek: Int): String {
    if (daysOfWeek == 0x7F) return "Every day"
    val names = listOf("Su","Mo","Tu","We","Th","Fr","Sa")
    val calDays = listOf(1, 2, 3, 4, 5, 6, 7) // Calendar.SUNDAY..SATURDAY
    return calDays.filter { (daysOfWeek and (1 shl (it - 1))) != 0 }
        .joinToString(" ") { names[it - 1] }
}
