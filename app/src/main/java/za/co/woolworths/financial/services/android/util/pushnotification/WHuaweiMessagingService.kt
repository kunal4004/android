package za.co.woolworths.financial.services.android.util.pushnotification

import android.os.Build
import com.huawei.hms.push.HmsMessageService
import com.huawei.hms.push.RemoteMessage
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.fcm.PushNotificationMessageType
import za.co.woolworths.financial.services.android.util.Utils

class WHuaweiMessagingService: HmsMessageService() {

    override fun onNewToken(token: String?) {
        token?.let {
            NotificationUtils.sendRegistrationToServer(it)
            Utils.setOCChatFCMToken(token)
            WoolworthsApplication.getInstance()?.userManager?.huaweiPushToken = it
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationUtils.createNotificationChannelIfNeeded(this)
        }
        PushNotificationManager.sendNotification(
            context = this,
            notificationTitle = remoteMessage?.notification?.title,
            notificationBody = remoteMessage?.notification?.body,
            payload = remoteMessage?.dataOfMap
        )
        remoteMessage?.dataOfMap?.let { data ->
            if (data.containsKey("type")) {
                when (data["type"]) {
                    PushNotificationMessageType.mcConfigClear -> {
                        PushNotificationManager.clearCachedConfig()
                    }
                }
            }
        }
    }
}