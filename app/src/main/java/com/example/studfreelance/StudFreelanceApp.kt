package com.example.studfreelance

import android.app.Application

class StudFreelanceApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ApplicationRepository.init(this)
        JobRepository.init(this)

        // Auto clear setiap run di debug mode
        val isDebug = applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0
        if (isDebug) {
            ApplicationRepository.clearAll()
            JobRepository.clearAll()
        }
    }
}