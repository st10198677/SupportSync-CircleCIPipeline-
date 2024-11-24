package com.example.assetsync

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        // Optional: Enable disk persistence for Realtime Database
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}

