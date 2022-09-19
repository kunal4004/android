package com.huawei.hms.push

import android.app.Service
import android.content.Intent
import android.os.IBinder

/*
 * Dummy class to prevent build errors for "google" product flavor
 * since Huawei dependencies are restricted for "huawei"" flavor only
 */
open class HmsMessageService: Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    open fun onNewToken(token: String?) {}

    open fun onMessageReceived(remoteMessage: RemoteMessage?) {}
}