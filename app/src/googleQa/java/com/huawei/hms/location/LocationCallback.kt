package com.huawei.hms.location

/*
 * Dummy class to prevent build errors for "google" product flavor
 * since Huawei dependencies are restricted for "huawei"" flavor only
 */
open class LocationCallback {
    open fun onLocationResult(var1: LocationResult?) {}
    open fun onLocationAvailability(var1: LocationAvailability?) {}
}