package za.co.woolworths.financial.services.android.util.pushnotification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.huawei.hms.push.HmsMessageService
import com.huawei.hms.push.RemoteMessage
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.fcm.PushNotificationMessageType
import za.co.woolworths.financial.services.android.startup.view.StartupActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.util.NotificationUtils
import za.co.woolworths.financial.services.android.util.PersistenceLayer

class HuaweiMessagingService: HmsMessageService() {

    override fun onNewToken(token: String?) {
        Log.i("HuaweiPush", "new token from onNewToken: $token")
        token?.let {
            NotificationUtils.getInstance().sendRegistrationToServer(it)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationUtils.createNotificationChannelIfNeeded(this)
        }
        sendNotification(remoteMessage?.notification, remoteMessage?.dataOfMap)
        remoteMessage?.dataOfMap?.let { data ->
            if (data.containsKey("type")) {
                when (data["type"]) {
                    PushNotificationMessageType.mcConfigClear -> {
                        mcConfigClear()
                    }
                }
            }
        }
    }

    private fun sendNotification(
        notification: RemoteMessage.Notification?,
        payload: Map<String, String>?
    ) {
        if (payload == null) return

        val channelId = resources.getString(R.string.default_notification_channel_id)
        val notificationBuilder =
            NotificationCompat.Builder(WoolworthsApplication.getAppContext(), channelId)
        val intent = Intent(this, StartupActivity::class.java)
        val payloadParameters = payload["parameters"]
        val payloadFeature = payload["feature"]
        if (payloadFeature != null && payloadFeature == "Product Listing" && payloadParameters != null) {
            val json = payloadParameters.replace("\\\\".toRegex(), "")
            val parameters = Gson().fromJson(
                json,
                JsonObject::class.java
            )
            intent.data = Uri.parse(parameters["url"].asString)
            intent.action = Intent.ACTION_VIEW
        }

        /*Deep link to PDP disabled*/
        /*else if (payload.get("feature").equals("Product Detail")){
            String json = payload.get("parameters").replaceAll("\\\\", "");
            JsonObject parameters = new Gson().fromJson(json, JsonObject.class);

            intent = new Intent(this, ProductDetailsDeepLinkActivity.class);
            intent.setData(Uri.parse(parameters.get("url").getAsString()));
            intent.setAction(Intent.ACTION_VIEW);
        }*/
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, BottomNavigationActivity.DEEP_LINK_REQUEST_CODE, intent,
            PendingIntent.FLAG_ONE_SHOT
        )
        var contentTitle: String? = notification?.title ?: payload["title"]
        var contentText: String? = notification?.body ?: payload["body"]
        notificationBuilder.setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(Color.BLACK)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }

    private fun mcConfigClear() {
        try {
            PersistenceLayer.getInstance()
                .executeDeleteQuery("DELETE FROM ApiResponse WHERE ApiRequestId IN (SELECT id FROM ApiRequest WHERE endpoint = '/mobileconfigs');")
            PersistenceLayer.getInstance()
                .executeDeleteQuery("DELETE FROM ApiRequest WHERE endpoint = '/mobileconfigs';")
        } catch (_: Exception) {}
    }
}