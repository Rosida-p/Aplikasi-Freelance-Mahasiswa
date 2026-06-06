package com.example.studfreelance

import android.app.Application
import com.cloudinary.android.MediaManager

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Reset data setiap run baru
        val prefs = getSharedPreferences("app_session", MODE_PRIVATE)
        val lastBuildTime = prefs.getLong("build_time", 0L)
        val currentBuildTime = packageManager.getPackageInfo(packageName, 0).lastUpdateTime

        JobRepository.init(this)
        ApplicationRepository.init(this)
        WorkSubmissionRepository.init(this)

        if (lastBuildTime != currentBuildTime) {
            // Build baru → reset ke dummy
            JobRepository.clearAll()
            ApplicationRepository.clearAll()
            WorkSubmissionRepository.clearAll()
            prefs.edit().putLong("build_time", currentBuildTime).apply()
        }


        val config = mapOf(
            "cloud_name" to CloudinaryConfig.CLOUD_NAME,
            "api_key" to CloudinaryConfig.API_KEY,
            "api_secret" to CloudinaryConfig.API_SECRET
        )
        MediaManager.init(this, config)
    }
}