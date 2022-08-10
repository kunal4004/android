package com.huawei.hms.maps

import com.huawei.hms.maps.model.CameraPosition
import com.huawei.hms.maps.model.CameraUpdate
import com.huawei.hms.maps.model.LatLng

/*
 * Dummy class to prevent build errors for "google" product flavor
 * since Huawei dependencies are restricted for "huawei"" flavor only
 */
class CameraUpdateFactory {
    companion object {
        fun newLatLng(var0: LatLng?): CameraUpdate? {
            return null
        }

        fun newLatLngZoom(var0: LatLng?, var1: Float): CameraUpdate? {
            return null
        }

        fun newCameraPosition(var0: CameraPosition?): CameraUpdate? {
            return null
        }
    }
}