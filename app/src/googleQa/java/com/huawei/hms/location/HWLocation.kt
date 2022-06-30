package com.huawei.hms.location

/*
 * Dummy class to prevent build errors for "google" product flavor
 * since Huawei dependencies are restricted for "huawei"" flavor only
 */
class HWLocation {
    var street: String? = null
    var city: String? = null
    var county: String? = null
    var suburb: String? = null
    var state: String? = null
    var countryName: String? = null
    var countryCode: String? = null
    var postalCode: String? = null
    var featureName: String? = null
}