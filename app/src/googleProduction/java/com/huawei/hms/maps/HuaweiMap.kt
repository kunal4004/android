package com.huawei.hms.maps

import android.view.View
import com.huawei.hms.maps.model.CameraPosition
import com.huawei.hms.maps.model.CameraUpdate
import com.huawei.hms.maps.model.Marker
import com.huawei.hms.maps.model.MarkerOptions

/*
 * Dummy class to prevent build errors for "google" product flavor
 * since Huawei dependencies are restricted for "huawei"" flavor only
 */
class HuaweiMap {
    var cameraPosition: CameraPosition? = null
    var isMyLocationEnabled: Boolean = false
    var projection: Projection? = null
    var uiSettings: UiSettings? = null

    fun animateCamera(var0: CameraUpdate?, var1: Int, var2: CancelableCallback?) {}

    fun moveCamera(var0: CameraUpdate?) {}

    fun setOnCameraMoveListener(var0: () -> Unit) {}

    fun setOnCameraIdleListener(var0: () -> Unit) {}

    fun setOnMarkerClickListener(var0: OnMarkerClickListener) {}

    fun setInfoWindowAdapter(var0: InfoWindowAdapter) {}

    fun addMarker(var0: MarkerOptions): Marker? {
        return null
    }

    interface CancelableCallback {
        fun onFinish()
        fun onCancel()
    }

    interface OnMarkerClickListener {
        fun onMarkerClick(var1: Marker): Boolean
    }

    interface InfoWindowAdapter {
        fun getInfoContents(marker: Marker?): View?
        fun getInfoWindow(marker: Marker?): View?
    }
}