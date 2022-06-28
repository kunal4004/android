package com.huawei.hms.api

import android.content.Context
import com.google.android.gms.common.ConnectionResult

/*
 * Dummy class to prevent build errors for "google" product flavor
 * since Huawei dependencies are restricted for "huawei"" flavor only
 */
class HuaweiApiAvailability {
    companion object {
        @JvmStatic
        fun getInstance(): HuaweiApiAvailability {
            return HuaweiApiAvailability()
        }

        @JvmStatic
        fun isHuaweiMobileServicesAvailable(context: Context): Int {
            return ConnectionResult.API_UNAVAILABLE
        }
    }
}