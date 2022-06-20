package com.huawei.hms.maps

import com.huawei.hms.maps.model.VisibleRegion

/*
 * Dummy class to prevent build errors for "google" product flavor
 * since Huawei dependencies are restricted for "huawei"" flavor only
 */
class Projection {
    var visibleRegion: VisibleRegion? = null
}