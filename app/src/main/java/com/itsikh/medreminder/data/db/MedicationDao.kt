package com.itsikh.medreminder.data.db

import androidx.room.*
import com.itsikh.medreminder.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {

    // ── Medications ──────────────────────────────────────────────────────────

    @Transaction
    @Query("SELECT * FROM medications WHERE isActive = 1 ORDER BY name")
    fun getMedicationsWithSchedules(): Flow<List<MedicationWithSchedules>>

    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun getMedicationById(id: Int): Medication?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: Medication): Long

    @Update
    suspend fun updateMedication(medication: Medication)

    @Query("UPDATE medications SET isActive = 0 WHERE id = :id")
    suspend fun deactivateMedication(id: Int)

    @Query("UPDATE medications SET stockQuantity = stockQuantity - 1 WHERE id = :id AND stockQuantity > 0")
    suspend fun decrementStock(id: Int)

    // ── Schedules ────────────────────────────────────────────────────────────

    @Query("SELECT * FROM medication_schedules WHERE medicationId = :medId")
    suspend fun getSchedulesForMedication(medId: Int): List<MedicationSchedule>

    @Query("SELECT * FROM medication_schedules WHERE id = :id")
    suspend fun getScheduleById(id: Int): MedicationSchedule?

    @Query("SELECT ms.* FROM medication_schedules ms INNER JOIN medications m ON ms.medicationId = m.id WHERE ms.isEnabled = 1 AND m.isActive = 1")
    suspend fun getAllActiveSchedules(): List<MedicationSchedule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: MedicationSchedule): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<MedicationSchedule>)

    @Delete
    suspend fun deleteSchedule(schedule: MedicationSchedule)

    @Query("DELETE FROM medication_schedules WHERE medicationId = :medId")
    suspend fun deleteSchedulesForMedication(medId: Int)

    // ── Logs ─────────────────────────────────────────────────────────────────

    @Query("SELECT * FROM medication_logs ORDER BY scheduledTimeMillis DESC LIMIT 300")
    fun getAllLogs(): Flow<List<MedicationLog>>

    @Query("SELECT * FROM medication_logs WHERE scheduledTimeMillis >= :startMs AND scheduledTimeMillis < :endMs ORDER BY scheduledTimeMillis ASC")
    fun getLogsForDay(startMs: Long, endMs: Long): Flow<List<MedicationLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: MedicationLog): Long

    @Query("UPDATE medication_logs SET status = :status, takenTimeMillis = :takenMs WHERE id = :id")
    suspend fun updateLogStatus(id: Int, status: LogStatus, takenMs: Long?)

    @Query("SELECT * FROM medication_logs WHERE id = :id")
    suspend fun getLogById(id: Int): MedicationLog?
}
