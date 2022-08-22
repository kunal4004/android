package com.huawei.hms.aaid

import android.content.Context

/*
 * Dummy class to prevent build errors for "google" product flavor
 * since Huawei dependencies are restricted for "huawei"" flavor only
 */
class HmsInstanceId {
    companion object {
        fun getInstance(context: Context): HmsInstanceId {
            return HmsInstanceId()
        }
    }

    fun getToken(appId: String, scope: String): String? {
        return null
    }
}