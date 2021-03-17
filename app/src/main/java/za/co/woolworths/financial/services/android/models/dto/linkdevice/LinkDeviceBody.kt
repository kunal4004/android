package za.co.woolworths.financial.services.android.models.dto.linkdevice

data class LinkDeviceBody(val appInstanceId: String, val location: String, val primaryDevice: Boolean, val pushNotificationToken: String)
