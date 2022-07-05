package com.huawei.hms.location

import android.app.Activity
import android.content.Context
import java.util.*

/*
 * Dummy class to prevent build errors for "google" product flavor
 * since Huawei dependencies are restricted for "huawei"" flavor only
 */
object LocationServices {
    fun getGeocoderService(context: Context, locale: Locale): GeocoderService {
        return GeocoderService()
    }

    fun getFusedLocationProviderClient(var0: Activity?): FusedLocationProviderClient {
        return FusedLocationProviderClient(var0!!)
    }
}