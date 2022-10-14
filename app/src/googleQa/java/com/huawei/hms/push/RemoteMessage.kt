package com.huawei.hms.push

/*
 * Dummy class to prevent build errors for "google" product flavor
 * since Huawei dependencies are restricted for "huawei"" flavor only
 */
class RemoteMessage {
    var notification: Notification? = null
    var dataOfMap: Map<String, String>? = null

    class Notification {
        var title: String? = null
        var body: String? = null
    }
}