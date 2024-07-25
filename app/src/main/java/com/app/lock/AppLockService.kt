package com.app.lock

import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast

class AppLockService : Service() {

    private val handler = Handler()
    private val runnable = object : Runnable {
        override fun run() {
            checkForegroundApp()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handler.post(runnable)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun checkForegroundApp() {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 1000
        val usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
        if (usageStatsList != null && usageStatsList.isNotEmpty()) {
            val sortedMap = usageStatsList.sortedWith(compareByDescending { it.lastTimeUsed })
            val currentApp = sortedMap[0].packageName
            if (currentApp == "com.android.chrome") {
                showLockScreen()
            }
        }
    }

    private fun showLockScreen() {
        val lockIntent = Intent(this, LockScreenActivity::class.java)
        lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(lockIntent)
    }
}
