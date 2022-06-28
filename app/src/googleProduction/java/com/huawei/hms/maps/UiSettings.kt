package com.huawei.hms.maps

/*
 * Dummy class to prevent build errors for "google" product flavor
 * since Huawei dependencies are restricted for "huawei"" flavor only
 */
class UiSettings {
    var isScrollGesturesEnabled: Boolean = false
    var isZoomControlsEnabled: Boolean = false
    fun setAllGesturesEnabled(var0: Boolean) {}
}