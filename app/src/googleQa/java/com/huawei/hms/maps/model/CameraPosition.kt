package com.huawei.hms.maps.model

/*
 * Dummy class to prevent build errors for "google" product flavor
 * since Huawei dependencies are restricted for "huawei"" flavor only
 */
class CameraPosition {
    companion object {
        fun builder(): CameraPosition {
            return CameraPosition()
        }
    }

    var target: LatLng? = null

    fun target(var0: LatLng): CameraPosition {
        return this
    }

    fun zoom(var0: Float): CameraPosition {
        return this
    }

    fun tilt(var0: Float): CameraPosition {
        return this
    }

    fun bearing(var0: Float): CameraPosition {
        return this
    }

    fun build(): CameraPosition {
        return this
    }
}