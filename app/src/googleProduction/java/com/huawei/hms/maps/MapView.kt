package com.huawei.hms.maps

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.View

/*
 * Dummy class to prevent build errors for "google" product flavor
 * since Huawei dependencies are restricted for "huawei"" flavor only
 */
class MapView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    fun onCreate(var0: Bundle?) {}
    fun getMapAsync(var0: OnMapReadyCallback) {}
    fun onResume() {}
    fun onPause() {}
    fun onDestroy() {}
    fun onLowMemory() {}
    fun onSaveInstanceState(outState: Bundle) {}
}