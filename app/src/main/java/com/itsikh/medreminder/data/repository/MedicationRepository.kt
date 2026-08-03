package com.itsikh.medreminder.data.repository

import com.itsikh.medreminder.data.db.MedicationDao
import com.itsikh.medreminder.data.model.*
import com.itsikh.medreminder.notification.AlarmScheduler
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicationRepository @Inject constructor(
    private val dao: MedicationDao
) {
    fun getMedicationsWithSchedules(): Flow<List<MedicationWithSchedules>> =
        dao.getMedicationsWithSchedules()

    suspend fun getMedicationById(id: Int): Medication? = dao.getMedicationById(id)
    suspend fun insertMedication(m: Medication): Long = dao.insertMedication(m)
    suspend fun updateMedication(m: Medication) = dao.updateMedication(m)
    suspend fun deactivateMedication(id: Int) = dao.deactivateMedication(id)
    suspend fun decrementStock(id: Int) = dao.decrementStock(id)

    suspend fun getScheduleById(id: Int): MedicationSchedule? = dao.getScheduleById(id)
    suspend fun getSchedulesForMedication(medId: Int): List<MedicationSchedule> =
        dao.getSchedulesForMedication(medId)
    suspend fun insertSchedule(s: MedicationSchedule): Long = dao.insertSchedule(s)
    suspend fun updateSchedule(s: MedicationSchedule) = dao.updateSchedule(s)
    suspend fun deleteSchedule(s: MedicationSchedule) = dao.deleteSchedule(s)
    suspend fun deleteSchedulesForMedication(medId: Int) = dao.deleteSchedulesForMedication(medId)

    fun getAllLogs(): Flow<List<MedicationLog>> = dao.getAllLogs()
    fun getLogsForDay(startMs: Long, endMs: Long): Flow<List<MedicationLog>> =
        dao.getLogsForDay(startMs, endMs)
    suspend fun insertLog(log: MedicationLog): Long = dao.insertLog(log)
    suspend fun getLogById(id: Int): MedicationLog? = dao.getLogById(id)
    suspend fun updateLogStatus(id: Int, status: LogStatus, takenMs: Long?) =
        dao.updateLogStatus(id, status, takenMs)
    suspend fun getLogForScheduleInRange(scheduleId: Int, startMs: Long, endMs: Long): MedicationLog? =
        dao.getLogForScheduleInRange(scheduleId, startMs, endMs)
    /**
     * Resolves doses nobody ever acted on, so they stop showing as pending forever.
     *
     * The grace period is deliberately wider than the longest possible snooze (24 h): a
     * dose snoozed across midnight is still legitimately waiting for its alarm, and
     * flipping it to MISSED would make [AlarmReceiver] discard that alarm on arrival.
     */
    suspend fun markAbandonedLogsMissed(): Int =
        dao.markStaleLogsMissed(System.currentTimeMillis() - STALE_LOG_GRACE_MS)

    private companion object {
        const val STALE_LOG_GRACE_MS = 36 * 60 * 60_000L
    }

    suspend fun rescheduleAllAlarms(scheduler: AlarmScheduler) {
        dao.getAllActiveSchedules().forEach { schedule ->
            val med = dao.getMedicationById(schedule.medicationId) ?: return@forEach
            scheduler.scheduleNextAlarm(schedule, med)
        }
    }
}
