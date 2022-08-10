package com.huawei.hms.location

/*
 * Dummy class to prevent build errors for "google" product flavor
 * since Huawei dependencies are restricted for "huawei"" flavor only
 */
class LocationRequest {
    companion object {
        const val PRIORITY_HIGH_ACCURACY = 100

        @JvmStatic
        fun create(): LocationRequest {
            return LocationRequest()
        }
    }

    fun setPriority(var1: Int): LocationRequest {
        return this
    }

    @Throws(IllegalArgumentException::class)
    fun setInterval(var1: Long): LocationRequest {
        return this
    }

    @Throws(java.lang.IllegalArgumentException::class)
    fun setFastestInterval(var1: Long): LocationRequest {
        return this
    }
}