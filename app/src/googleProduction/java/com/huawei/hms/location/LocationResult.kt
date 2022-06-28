package com.huawei.hms.location

import android.location.Location

/*
 * Dummy class to prevent build errors for "google" product flavor
 * since Huawei dependencies are restricted for "huawei"" flavor only
 */
class LocationResult {
    val lastLocation: Location? = null
}