package com.example.studfreelance

import android.app.Application
import com.cloudinary.android.MediaManager

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        JobRepository.init(this)
        ApplicationRepository.init(this)
        WorkSubmissionRepository.init(this)  // ← tambah ini

        val config = mapOf(
            "cloud_name" to CloudinaryConfig.CLOUD_NAME,
            "api_key" to CloudinaryConfig.API_KEY,
            "api_secret" to CloudinaryConfig.API_SECRET
        )
        MediaManager.init(this, config)
    }
}