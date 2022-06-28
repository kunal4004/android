package com.huawei.hms.location

import android.app.Activity
import android.content.Context
import android.location.Location
import android.os.Looper
import com.huawei.hmf.tasks.Task

/*
 * Dummy class to prevent build errors for "google" product flavor
 * since Huawei dependencies are restricted for "huawei"" flavor only
 */
class FusedLocationProviderClient {

    val lastLocation: Task<Location> = Task()

    constructor(context: Context)

    constructor(activity: Activity)

    fun requestLocationUpdates(
        request: LocationRequest?,
        callback: LocationCallback?,
        looper: Looper?
    ): Task<Void> {
        return Task()
    }

    fun removeLocationUpdates(
        callback: LocationCallback?
    ): Task<Void> {
        return Task()
    }
}
