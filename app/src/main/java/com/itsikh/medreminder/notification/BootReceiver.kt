package com.itsikh.medreminder.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.itsikh.medreminder.data.repository.MedicationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var repository: MedicationRepository
    @Inject lateinit var alarmScheduler: AlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        val result = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                repository.rescheduleAllAlarms(alarmScheduler)
                // Alarms don't survive a reboot, so doses due while the device was off were
                // never resolved. Close them out rather than leaving them pending forever.
                repository.markAbandonedLogsMissed()
            } finally { result.finish() }
        }
    }
}
