package com.huawei.hms.analytics

import android.os.Bundle

/*
 * Dummy class to prevent build errors for "google" product flavor
 * since Huawei dependencies are restricted for "huawei"" flavor only
 */
class HiAnalyticsInstance {
    fun onEvent(name: String, params: Bundle?) {}
    fun pageStart(screenName: String?, activityName: String) {}
    fun setUserProfile(name: String, value: String?) {}
    fun setUserId(id: String) {}
}