package com.itsikh.medreminder.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.itsikh.medreminder.data.model.LogStatus
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenSettings: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val meds by viewModel.todayMedications.collectAsState()
    val dateLabel = remember {
        SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Today", fontWeight = FontWeight.Bold)
                        Text(dateLabel, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        if (meds.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("💊", style = MaterialTheme.typography.displayMedium)
                    Text("No medications scheduled today",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Summary row
                item {
                    val taken = meds.count { it.isTaken }
                    val total = meds.size
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("$taken of $total taken today",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Medium)
                        if (taken == total && total > 0) {
                            Text("✅ All done!", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }

                items(meds, key = { "${it.medication.id}_${it.schedule.id}" }) { item ->
                    TodayMedCard(
                        item = item,
                        onTaken = { viewModel.markTaken(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TodayMedCard(item: TodayMedication, onTaken: () -> Unit) {
    val timeFmt = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val schedTime = remember(item.schedule) {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, item.schedule.timeHour)
            set(Calendar.MINUTE, item.schedule.timeMinute)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.time
    }

    val containerColor = when {
        item.isTaken -> MaterialTheme.colorScheme.surfaceVariant
        item.isMissed -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
        else -> MaterialTheme.colorScheme.surface
    }
    val statusText = when {
        item.isTaken -> "✅ Taken at ${item.log?.takenTimeMillis?.let { timeFmt.format(Date(it)) } ?: ""}"
        item.isMissed -> "❌ Missed"
        item.isSnoozed -> "⏰ Snoozed"
        else -> "⏳ Scheduled ${timeFmt.format(schedTime)}"
    }
    val statusColor = when {
        item.isTaken -> Color(0xFF4CAF50)
        item.isMissed -> MaterialTheme.colorScheme.error
        item.isSnoozed -> Color(0xFFFF9800)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(if (item.isTaken) 0.dp else 2.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                Modifier.size(14.dp).clip(CircleShape)
                    .background(
                        if (item.isTaken) Color(item.medication.color).copy(alpha = 0.4f)
                        else Color(item.medication.color)
                    )
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(item.medication.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                if (item.medication.dosage.isNotBlank())
                    Text(item.medication.dosage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(statusText, style = MaterialTheme.typography.bodySmall, color = statusColor)
            }
            if (!item.isTaken) {
                Button(
                    onClick = onTaken,
                    modifier = Modifier.height(36.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text("Took it", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
