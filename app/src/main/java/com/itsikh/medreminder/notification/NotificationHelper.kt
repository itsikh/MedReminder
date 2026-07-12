package com.itsikh.medreminder.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
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
        const val ACTION_SKIP_TODAY      = "com.itsikh.medreminder.SKIP_TODAY"
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
        /** Notification ID for the test notification sent from Settings. */
        const val TEST_NOTIF_ID               = 90_000
    }

    /**
     * Posts a test notification on the same channel real medication reminders use,
     * so the user can verify sound, vibration, and visibility end-to-end.
     */
    fun showTestNotification() {
        val openAppPi = PendingIntent.getActivity(
            context, TEST_NOTIF_ID,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val body = "If you can see this, medication reminders can reach you."
        val builder = NotificationCompat.Builder(context, snoozePrefs.currentMedChannelId)
            .setSmallIcon(R.drawable.ic_notification_pill)
            .setContentTitle("🔔 Test reminder")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(openAppPi)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        context.getSystemService(NotificationManager::class.java)?.notify(TEST_NOTIF_ID, builder.build())
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

        val dosageText = if (dosage.isNotBlank()) " · $dosage" else ""
        val bodyText = "Time to take your $medicationName$dosageText"

        // The system notification template shows at most 3 action buttons, so the
        // expanded and heads-up views use a custom layout that fits 4:
        // Took it → slot1 snooze → At home (or slot2 snooze if no home set) → Skip today.
        // A fresh RemoteViews instance is required per view — they must not be shared.
        fun actionViews(): RemoteViews {
            val rv = RemoteViews(context.packageName, R.layout.notification_medication)
            rv.setTextViewText(R.id.notif_title, "💊 $medicationName")
            rv.setTextViewText(R.id.notif_text, bodyText)
            rv.setOnClickPendingIntent(R.id.btn_taken, actionPi(ACTION_TAKEN, notifId * 10 + 1))
            rv.setTextViewText(R.id.btn_snooze, "⏰ ${formatMin(snoozePrefs.slot1)}")
            rv.setOnClickPendingIntent(R.id.btn_snooze, actionPi(ACTION_SNOOZE_SLOT_1, notifId * 10 + 2, s1ms))
            if (snoozePrefs.hasHomeLocation) {
                rv.setTextViewText(R.id.btn_third, "📍 Home")
                rv.setOnClickPendingIntent(R.id.btn_third, actionPi(ACTION_SNOOZE_LOCATION, notifId * 10 + 6))
            } else {
                rv.setTextViewText(R.id.btn_third, "⏰ ${formatMin(snoozePrefs.slot2)}")
                rv.setOnClickPendingIntent(R.id.btn_third, actionPi(ACTION_SNOOZE_SLOT_2, notifId * 10 + 3, s2ms))
            }
            rv.setOnClickPendingIntent(R.id.btn_skip, actionPi(ACTION_SKIP_TODAY, notifId * 10 + 7))
            return rv
        }

        val builder = NotificationCompat.Builder(context, snoozePrefs.currentMedChannelId)
            .setSmallIcon(R.drawable.ic_notification_pill)
            .setContentTitle("💊 $medicationName")
            .setContentText(bodyText)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomBigContentView(actionViews())
            .setCustomHeadsUpContentView(actionViews())
            .setContentIntent(openAppPi)
            .setAutoCancel(false)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

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
