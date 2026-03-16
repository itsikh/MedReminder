package com.itsikh.medreminder.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.itsikh.medreminder.AppConfig
import com.itsikh.medreminder.MainActivity
import com.itsikh.medreminder.R
import com.itsikh.medreminder.data.model.Medication
import com.itsikh.medreminder.data.preferences.SnoozePrefs
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val snoozePrefs: SnoozePrefs
) {
    companion object {
        const val ACTION_TAKEN           = "com.itsikh.medreminder.TAKEN"
        const val ACTION_SNOOZE_SLOT_1   = "com.itsikh.medreminder.SNOOZE_1"
        const val ACTION_SNOOZE_SLOT_2   = "com.itsikh.medreminder.SNOOZE_2"
        const val ACTION_SNOOZE_SLOT_3   = "com.itsikh.medreminder.SNOOZE_3"
        const val ACTION_SNOOZE_TONIGHT  = "com.itsikh.medreminder.SNOOZE_TONIGHT"
        const val ACTION_SNOOZE_LOCATION = "com.itsikh.medreminder.SNOOZE_LOCATION"
        const val ACTION_DISMISS_STOCK   = "com.itsikh.medreminder.DISMISS_STOCK"

        /** Duration in milliseconds, carried in each snooze PendingIntent. */
        const val EXTRA_SNOOZE_MS = "snooze_ms"

        /** Notification ID offset for warning-level stock notifications. */
        const val STOCK_WARN_NOTIF_OFFSET     = 50_000
        /** Notification ID offset for critical-level stock notifications. */
        const val STOCK_CRITICAL_NOTIF_OFFSET = 60_000
    }

    fun showMedicationNotification(
        scheduleId: Int,
        medicationId: Int,
        medicationName: String,
        dosage: String,
        logId: Int,
        scheduledTime: Long
    ) {
        val notifId = scheduleId

        val openAppPi = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        fun actionPi(action: String, reqCode: Int, snoozeMs: Long = 0L): PendingIntent =
            PendingIntent.getBroadcast(
                context, reqCode,
                Intent(action, null, context, ActionReceiver::class.java).apply {
                    putExtra(AlarmScheduler.EXTRA_SCHEDULE_ID, scheduleId)
                    putExtra(AlarmScheduler.EXTRA_MEDICATION_ID, medicationId)
                    putExtra(AlarmScheduler.EXTRA_MEDICATION_NAME, medicationName)
                    putExtra(AlarmScheduler.EXTRA_DOSAGE, dosage)
                    putExtra(AlarmScheduler.EXTRA_LOG_ID, logId)
                    putExtra(AlarmScheduler.EXTRA_SCHEDULED_TIME, scheduledTime)
                    putExtra(AlarmScheduler.EXTRA_NOTIF_ID, notifId)
                    if (snoozeMs > 0) putExtra(EXTRA_SNOOZE_MS, snoozeMs)
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        val s1ms = snoozePrefs.slot1 * 60_000L
        val s2ms = snoozePrefs.slot2 * 60_000L
        val s3ms = snoozePrefs.slot3 * 60_000L

        val dosageText = if (dosage.isNotBlank()) " · $dosage" else ""
        val bodyText = "Time to take your $medicationName$dosageText"

        // Android notification shade typically shows only 3 action buttons.
        // Order: Took it → slot1 → At home (if set, so it's always visible) → slot2 → slot3 → Tonight
        val builder = NotificationCompat.Builder(context, snoozePrefs.currentMedChannelId)
            .setSmallIcon(R.drawable.ic_notification_pill)
            .setContentTitle("💊 $medicationName")
            .setContentText(bodyText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bodyText))
            .setContentIntent(openAppPi)
            .setAutoCancel(false)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(R.drawable.ic_notification_pill, "✅ Took it",
                actionPi(ACTION_TAKEN, notifId * 10 + 1))
            .addAction(R.drawable.ic_notification_pill, "⏰ ${formatMin(snoozePrefs.slot1)}",
                actionPi(ACTION_SNOOZE_SLOT_1, notifId * 10 + 2, s1ms))

        if (snoozePrefs.hasHomeLocation) {
            builder.addAction(R.drawable.ic_notification_pill, "📍 At home",
                actionPi(ACTION_SNOOZE_LOCATION, notifId * 10 + 6))
        }

        builder
            .addAction(R.drawable.ic_notification_pill, "⏰ ${formatMin(snoozePrefs.slot2)}",
                actionPi(ACTION_SNOOZE_SLOT_2, notifId * 10 + 3, s2ms))
            .addAction(R.drawable.ic_notification_pill, "⏰ ${formatMin(snoozePrefs.slot3)}",
                actionPi(ACTION_SNOOZE_SLOT_3, notifId * 10 + 4, s3ms))
            .addAction(R.drawable.ic_notification_pill, "🌙 Tonight",
                actionPi(ACTION_SNOOZE_TONIGHT, notifId * 10 + 5))

        context.getSystemService(NotificationManager::class.java)?.notify(notifId, builder.build())
    }

    fun cancelNotification(notifId: Int) {
        context.getSystemService(NotificationManager::class.java)?.cancel(notifId)
    }

    fun showLowStockNotification(medication: Medication, isCritical: Boolean = false) {
        val notifId = medication.id + if (isCritical) STOCK_CRITICAL_NOTIF_OFFSET else STOCK_WARN_NOTIF_OFFSET
        val openAppPi = PendingIntent.getActivity(
            context, notifId,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val dismissPi = PendingIntent.getBroadcast(
            context, notifId,
            Intent(ACTION_DISMISS_STOCK, null, context, ActionReceiver::class.java).apply {
                putExtra(AlarmScheduler.EXTRA_NOTIF_ID, notifId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val qty = medication.stockQuantity
        val body = if (isCritical)
            "Critical: only $qty ${if (qty == 1) "dose" else "doses"} of ${medication.name} left! Reorder immediately."
        else
            "Only $qty ${if (qty == 1) "dose" else "doses"} of ${medication.name} left. Time to reorder!"
        val channelId = if (isCritical) AppConfig.NOTIFICATION_CHANNEL_STOCK_CRITICAL else AppConfig.NOTIFICATION_CHANNEL_STOCK
        val priority = if (isCritical) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT
        val title = if (isCritical) "Critical stock: ${medication.name}" else "Low stock: ${medication.name}"
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification_pill)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(openAppPi)
            .setAutoCancel(false)
            .setOngoing(true)
            .setPriority(priority)
            .addAction(R.drawable.ic_notification_pill, "Dismiss", dismissPi)
        context.getSystemService(NotificationManager::class.java)?.notify(notifId, builder.build())
    }

    private fun formatMin(minutes: Int): String = when {
        minutes < 60 -> "$minutes min"
        minutes % 60 == 0 -> "${minutes / 60} hr"
        else -> "${minutes / 60}h ${minutes % 60}m"
    }
}
