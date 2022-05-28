package com.huawei.hms.maps

/*
 * Dummy class to prevent build errors for "google" product flavor
 * since Huawei dependencies are restricted for "huawei"" flavor only
 */
class MapsInitializer {
    companion object {
        fun setApiKey(var0: String) {}
    }
}