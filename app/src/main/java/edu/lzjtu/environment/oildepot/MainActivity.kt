package edu.lzjtu.environment.oildepot

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import edu.lzjtu.environment.oildepot.notifications.AlertNotificationManager
import edu.lzjtu.environment.oildepot.ui.MainSection
import edu.lzjtu.environment.oildepot.ui.OilDepotApp

class MainActivity : ComponentActivity() {
    private lateinit var smsPermissionLauncher: ActivityResultLauncher<String>
    private var pendingSmsRecipients: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val notificationManager = AlertNotificationManager(this)
        notificationManager.createChannel()
        val notificationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { granted ->
            if (granted) notificationManager.showDemoAlert()
        }
        smsPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { granted ->
            val recipients = pendingSmsRecipients
            pendingSmsRecipients = emptyList()
            if (granted) {
                sendTestSms(recipients)
            } else {
                Toast.makeText(this, "未授予发送短信权限", Toast.LENGTH_SHORT).show()
            }
        }

        val initialSection = if (intent?.data?.host == "alerts") {
            MainSection.ALERTS
        } else {
            MainSection.OVERVIEW
        }

        setContent {
            OilDepotApp(
                initialSection = initialSection,
                onSendDemoNotification = {
                    if (
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.POST_NOTIFICATIONS,
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        false
                    } else {
                        notificationManager.showDemoAlert()
                    }
                },
                onSendTestSms = ::requestTestSms,
            )
        }
    }

    private fun requestTestSms(recipients: List<String>) {
        if (recipients.isEmpty()) return
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY_MESSAGING)) {
            Toast.makeText(this, "当前设备不支持发送短信", Toast.LENGTH_SHORT).show()
            return
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            sendTestSms(recipients)
        } else {
            pendingSmsRecipients = recipients
            smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
        }
    }

    private fun sendTestSms(recipients: List<String>) {
        val sentCount = recipients.count { recipient ->
            try {
                SmsManager.getDefault().sendTextMessage(recipient, null, TEST_SMS_BODY, null, null)
                true
            } catch (_: IllegalArgumentException) {
                false
            } catch (_: SecurityException) {
                false
            } catch (_: UnsupportedOperationException) {
                false
            }
        }
        Toast.makeText(
            this,
            if (sentCount == recipients.size) {
                "已提交 $sentCount 条短信发送请求"
            } else {
                "已提交 $sentCount/${recipients.size} 条短信发送请求"
            },
            Toast.LENGTH_SHORT,
        ).show()
    }

    private companion object {
        const val TEST_SMS_BODY = "【油库预警测试】兰州原油库苯浓度预警，请及时查看处置任务。"
    }
}
