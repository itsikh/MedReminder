package com.itsikh.medreminder.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val dosage: String = "",
    val color: Int = 0xFF4CAF50.toInt(),
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val stockQuantity: Int = -1,            // -1 = not tracking; >= 0 = current count
    val stockInitial: Int = -1,             // -1 = not tracking; >= 0 = count when last restocked
    val lowStockThresholdPct: Int = 20,     // warning alert when stockQuantity/stockInitial <= this %
    val criticalStockThresholdPct: Int = 10 // critical alert when stockQuantity/stockInitial <= this %
)
