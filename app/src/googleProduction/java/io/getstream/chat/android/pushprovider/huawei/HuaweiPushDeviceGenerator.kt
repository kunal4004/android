package io.getstream.chat.android.pushprovider.huawei

import android.content.Context
import io.getstream.chat.android.client.models.Device
import io.getstream.chat.android.client.notifications.handler.PushDeviceGenerator

/*
 * Dummy class to prevent build errors for "google" product flavor
 * since Huawei dependencies are restricted for "huawei"" flavor only
 */
class HuaweiPushDeviceGenerator(context: Context, private val appId: String) :
    PushDeviceGenerator {
    override fun asyncGenerateDevice(onDeviceGenerated: (device: Device) -> Unit) {}

    override fun isValidForThisDevice(context: Context): Boolean = false
}