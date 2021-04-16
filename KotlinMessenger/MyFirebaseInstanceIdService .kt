package com.example.kotlinmessenger

import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService

class MyFirebaseInstanceIdService : FirebaseInstanceIdService() {

    lateinit var name: String

    override fun onTokenRefresh() {
        val token = FirebaseInstanceId.getInstance().token

    }
}