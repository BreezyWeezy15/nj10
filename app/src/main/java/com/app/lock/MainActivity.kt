package com.app.lock

import android.app.AppOpsManager
import android.content.Context
import android.provider.Settings
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.os.Process
import java.util.*
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import android.widget.ToggleButton
import androidx.core.content.ContextCompat
import com.app.lock.AppLockService


class MainActivity : AppCompatActivity() {

    private val REQUEST_CODE_USAGE_STATS = 1001
    private val REQUEST_CODE_OVERLAY_PERMISSION = 1002
    private val REQUEST_CODE_NOTIFICATION_PERMISSION = 1003

    private lateinit var toggleButton: ToggleButton
    private lateinit var overlayPermissionStatus: ImageView
    private lateinit var usageAccessStatus: ImageView
    private lateinit var notificationPermissionStatus: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toggleButton = findViewById(R.id.toggleButton)
        overlayPermissionStatus = findViewById(R.id.overlayPermissionStatus)
        usageAccessStatus = findViewById(R.id.usageAccessStatus)
        notificationPermissionStatus = findViewById(R.id.notificationPermissionStatus)

        updatePermissionStatus()

        toggleButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (hasAllPermissions()) {
                    startAppLockService()
                } else {
                    Toast.makeText(this, "Please grant all permissions.", Toast.LENGTH_SHORT).show()
                }
            } else {
                stopAppLockService()
            }
        }

        findViewById<LinearLayout>(R.id.overlayPermissionRow).setOnClickListener {
            requestOverlayPermission()
        }

        findViewById<LinearLayout>(R.id.usageAccessRow).setOnClickListener {
            requestUsageStatsPermission()
        }

        findViewById<LinearLayout>(R.id.notificationPermissionRow).setOnClickListener {
            requestNotificationPermission()
        }


    }

    private fun updatePermissionStatus() {
        overlayPermissionStatus.setImageResource(if (hasOverlayPermission()) R.drawable.baseline_check_24 else R.drawable.uncheck)
        usageAccessStatus.setImageResource(if (hasUsageStatsPermission()) R.drawable.baseline_check_24 else R.drawable.uncheck)
        notificationPermissionStatus.setImageResource(if (isNotificationPermissionGranted()) R.drawable.baseline_check_24 else R.drawable.uncheck)
    }

    private fun startAppLockService() {
        val intent = Intent(this, AppLockService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopAppLockService() {
        val intent = Intent(this, AppLockService::class.java)
        stopService(intent)
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), packageName)
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun hasOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(this)
    }

    private fun isNotificationPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun hasAllPermissions(): Boolean {
        return hasUsageStatsPermission() && hasOverlayPermission() && isNotificationPermissionGranted()
    }

    private fun requestUsageStatsPermission() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        startActivityForResult(intent, REQUEST_CODE_USAGE_STATS)
    }

    private fun requestOverlayPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
        startActivityForResult(intent, REQUEST_CODE_OVERLAY_PERMISSION)
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            startActivityForResult(intent, REQUEST_CODE_NOTIFICATION_PERMISSION)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        updatePermissionStatus()
    }
}


