package com.itsikh.medreminder.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class LogStatus { PENDING, TAKEN, SNOOZED, MISSED }

@Entity(tableName = "medication_logs")
data class MedicationLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val medicationId: Int,
    val scheduleId: Int,
    val medicationName: String,
    val dosage: String,
    val scheduledTimeMillis: Long,
    val takenTimeMillis: Long? = null,
    val status: LogStatus = LogStatus.PENDING
)
