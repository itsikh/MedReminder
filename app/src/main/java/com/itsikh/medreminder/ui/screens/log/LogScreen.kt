package com.itsikh.medreminder.ui.screens.log

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.itsikh.medreminder.data.model.LogStatus
import com.itsikh.medreminder.data.model.MedicationLog
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogScreen(viewModel: LogViewModel = hiltViewModel()) {
    val logs by viewModel.logs.collectAsState()

    val grouped = remember(logs) {
        logs.groupBy { log ->
            SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
                .format(Date(log.scheduledTimeMillis))
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("History", fontWeight = FontWeight.Bold) }) }
    ) { padding ->
        if (logs.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No history yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                grouped.forEach { (dateLabel, dayLogs) ->
                    item {
                        Text(
                            dateLabel,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                        )
                    }
                    items(dayLogs, key = { it.id }) { log ->
                        LogEntry(log)
                    }
                }
            }
        }
    }
}

@Composable
private fun LogEntry(log: MedicationLog) {
    val timeFmt = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val (statusColor, statusIcon) = when (log.status) {
        LogStatus.TAKEN    -> Color(0xFF4CAF50) to "✅"
        LogStatus.MISSED   -> Color(0xFFF44336) to "❌"
        LogStatus.SNOOZED  -> Color(0xFFFF9800) to "⏰"
        LogStatus.PENDING  -> Color(0xFF9E9E9E) to "⏳"
    }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(statusIcon, style = MaterialTheme.typography.titleMedium)
        Column(Modifier.weight(1f)) {
            Text(log.medicationName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            if (log.dosage.isNotBlank())
                Text(log.dosage, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("Scheduled: ${timeFmt.format(Date(log.scheduledTimeMillis))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (log.takenTimeMillis != null)
                Text("Taken: ${timeFmt.format(Date(log.takenTimeMillis))}",
                    style = MaterialTheme.typography.bodySmall, color = statusColor)
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
}
