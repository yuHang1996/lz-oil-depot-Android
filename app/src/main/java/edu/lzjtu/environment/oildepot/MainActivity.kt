package edu.lzjtu.environment.oildepot

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import edu.lzjtu.environment.oildepot.notifications.AlertNotificationManager
import edu.lzjtu.environment.oildepot.ui.MainSection
import edu.lzjtu.environment.oildepot.ui.OilDepotApp

class MainActivity : ComponentActivity() {
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
            )
        }
    }
}
