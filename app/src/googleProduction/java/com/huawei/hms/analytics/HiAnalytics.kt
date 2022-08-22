package com.huawei.hms.analytics

import android.content.Context

/*
 * Dummy class to prevent build errors for "google" product flavor
 * since Huawei dependencies are restricted for "huawei"" flavor only
 */
class HiAnalytics {
    companion object {
        fun getInstance(context: Context?): HiAnalyticsInstance = HiAnalyticsInstance()
    }
}