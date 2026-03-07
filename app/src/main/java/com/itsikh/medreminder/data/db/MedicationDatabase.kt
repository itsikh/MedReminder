package com.itsikh.medreminder.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.itsikh.medreminder.data.model.Medication
import com.itsikh.medreminder.data.model.MedicationLog
import com.itsikh.medreminder.data.model.MedicationSchedule

@Database(
    entities = [Medication::class, MedicationSchedule::class, MedicationLog::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MedicationDatabase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao
}
