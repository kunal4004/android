package com.huawei.hms.location

import android.app.Activity
import android.content.Context

/*
 * Dummy class to prevent build errors for "google" product flavor
 * since Huawei dependencies are restricted for "huawei"" flavor only
 */
object LocationServices {
    fun getFusedLocationProviderClient(var0: Activity?): FusedLocationProviderClient {
        return FusedLocationProviderClient(var0!!)
    }

    fun getFusedLocationProviderClient(var0: Context?): FusedLocationProviderClient {
        return FusedLocationProviderClient(var0!!)
    }

    fun getSettingsClient(var0: Activity?): SettingsClient {
        return SettingsClient(var0)
    }

    fun getSettingsClient(var0: Context?): SettingsClient {
        return SettingsClient(var0)
    }

    fun getGeofenceService(var0: Activity?): GeofenceService {
        return GeofenceService(var0)
    }

    fun getGeofenceService(var0: Context?): GeofenceService {
        return GeofenceService(var0)
    }

    fun getLocationEnhanceService(var0: Context?): LocationEnhanceService {
        return LocationEnhanceService(var0)
    }

    fun getLocationEnhanceService(var0: Activity?): LocationEnhanceService {
        return LocationEnhanceService(var0)
    }
}