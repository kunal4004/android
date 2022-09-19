package za.co.woolworths.financial.services.android.util.pushnotification

import android.os.Build
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.fcm.PushNotificationMessageType

class WFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        NotificationUtils.sendRegistrationToServer(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationUtils.createNotificationChannelIfNeeded(this)
        }
        if (WoolworthsApplication.isApplicationInForeground()) {
            PushNotificationManager.sendNotification(
                context = this,
                notificationTitle = remoteMessage.notification?.title,
                notificationBody = remoteMessage.notification?.body,
                payload = remoteMessage.data
            )
        }
        remoteMessage?.data?.let { data ->
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