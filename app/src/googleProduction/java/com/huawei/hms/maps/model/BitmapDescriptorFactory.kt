package com.huawei.hms.maps.model

/*
 * Dummy class to prevent build errors for "google" product flavor
 * since Huawei dependencies are restricted for "huawei"" flavor only
 */
class BitmapDescriptorFactory {
    companion object {
        fun fromResource(var0: Int): BitmapDescriptor? {
            return null
        }
    }
}