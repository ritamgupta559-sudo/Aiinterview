package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class InterviewApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
                Log.d("InterviewApplication", "FirebaseApp initialized successfully")
            }
        } catch (e: Exception) {
            Log.w("InterviewApplication", "FirebaseApp auto-init skipped: ${e.message}")
        }
    }
}
