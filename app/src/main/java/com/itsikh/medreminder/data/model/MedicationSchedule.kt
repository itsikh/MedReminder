package com.itsikh.medreminder.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One time-slot for a medication.
 * daysOfWeek: bitmask where bit (calendarDay-1) is set.
 * Calendar.SUNDAY=1→bit0, MONDAY=2→bit1, …, SATURDAY=7→bit6
 * 0x7F = every day.
 */
@Entity(
    tableName = "medication_schedules",
    foreignKeys = [ForeignKey(
        entity = Medication::class,
        parentColumns = ["id"],
        childColumns = ["medicationId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("medicationId")]
)
data class MedicationSchedule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val medicationId: Int,
    val timeHour: Int,
    val timeMinute: Int,
    val daysOfWeek: Int = 0x7F,
    val isEnabled: Boolean = true
)
