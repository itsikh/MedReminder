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
    val createdAt: Long = System.currentTimeMillis()
)
