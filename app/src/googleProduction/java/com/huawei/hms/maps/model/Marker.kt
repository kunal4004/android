package com.huawei.hms.maps.model

/*
 * Dummy class to prevent build errors for "google" product flavor
 * since Huawei dependencies are restricted for "huawei"" flavor only
 */
class Marker {
    var id: String? = null
    var position: LatLng? = null
    var isVisible: Boolean = false
    fun setIcon(var0: BitmapDescriptor?) {}
}