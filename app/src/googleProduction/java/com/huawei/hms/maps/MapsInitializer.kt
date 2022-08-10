package com.huawei.hms.maps

import android.content.Context

/*
 * Dummy class to prevent build errors for "google" product flavor
 * since Huawei dependencies are restricted for "huawei"" flavor only
 */
class MapsInitializer {
    companion object {
        fun setApiKey(var0: String) {}
        fun initialize(var0: Context) {}
    }
}