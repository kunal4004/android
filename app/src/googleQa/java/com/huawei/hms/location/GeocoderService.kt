package com.huawei.hms.location

import com.huawei.hmf.tasks.Task

/*
 * Dummy class to prevent build errors for "google" product flavor
 * since Huawei dependencies are restricted for "huawei"" flavor only
 */
class GeocoderService {
    fun getFromLocation(var0: GetFromLocationRequest): Task<List<HWLocation>> {
        return Task<List<HWLocation>>()
    }
}