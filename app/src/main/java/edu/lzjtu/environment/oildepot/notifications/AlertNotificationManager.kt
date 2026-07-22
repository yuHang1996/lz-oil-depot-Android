package edu.lzjtu.environment.oildepot.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import edu.lzjtu.environment.oildepot.MainActivity
import edu.lzjtu.environment.oildepot.R

class AlertNotificationManager(
    private val context: Context,
) {
    private val manager = context.getSystemService(NotificationManager::class.java)

    fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.notification_channel_description)
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)
    }

    fun showDemoAlert(): Boolean {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }

        val intent = Intent(
            Intent.ACTION_VIEW,
            "lzjtu-oildepot://alerts/ENV-20260722-001".toUri(),
            context,
            MainActivity::class.java,
        )
        val pendingIntent = PendingIntent.getActivity(
            context,
            1001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_app)
            .setContentTitle("橙色预警｜兰州原油库")
            .setContentText("苯浓度超限，预计 74 分钟后影响黄河岸线 S-01")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "事件 ENV-20260722-001：厂界东北大气点苯浓度为阈值的 1.31 倍，建议立即加密监测并复核源头。",
                ),
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(DEMO_NOTIFICATION_ID, notification)
        return true
    }

    private companion object {
        const val CHANNEL_ID = "environment_alerts"
        const val DEMO_NOTIFICATION_ID = 1001
    }
}
