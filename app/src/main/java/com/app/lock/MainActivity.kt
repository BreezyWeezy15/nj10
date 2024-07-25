package com.app.lock

import android.app.AppOpsManager
import android.content.Context
import android.provider.Settings
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.os.Process
import java.util.*
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ToggleButton
import com.app.lock.AppLockService


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toggleButton = findViewById<ToggleButton>(R.id.toggleButton)

        toggleButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (hasUsageStatsPermission() && hasOverlayPermission()) {
                    startService(Intent(this, AppLockService::class.java))
                } else {
                    requestPermissions()
                }
            } else {
                stopService(Intent(this, AppLockService::class.java))
            }
        }
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), packageName)
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun hasOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(this)
    }

    private fun requestPermissions() {
        if (!hasUsageStatsPermission()) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
        if (!hasOverlayPermission()) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivity(intent)
        }
    }
}