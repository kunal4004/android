package za.co.woolworths.financial.services.android.util.pushnotification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.JsonObject
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.startup.view.StartupActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.PersistenceLayer
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager

class PushNotificationManager {
    companion object {
        private const val PAYLOAD_PARAMETERS = "parameters"
        private const val PAYLOAD_FEATURE = "feature"
        private const val PAYLOAD_TITLE = "title"
        private const val PAYLOAD_BODY = "body"
        const val PAYLOAD_STREAM_CHANNEL = "channel"
        const val PAYLOAD_STREAM_CHANNEL_ID = "id"
        const val PAYLOAD_STREAM_CHANNEL_TYPE = "type"
        private const val FEATURE_ORDER_DETAILS = "Order Details"
        private const val FEATURE_PRODUCT_LISTING = "Product Listing"

        fun sendNotification(
            context: Context,
            notificationTitle: String?,
            notificationBody: String?,
            payload: Map<String, String>?
        ) {
            if (payload == null) return

            val channelId = context.getString(R.string.default_notification_channel_id)
            val notificationBuilder =
                NotificationCompat.Builder(WoolworthsApplication.getAppContext(), channelId)
            var intent = Intent(context, StartupActivity::class.java)
            var pendingIntent: PendingIntent

            val payloadParameters = payload[PAYLOAD_PARAMETERS]
            val payloadFeature = payload[PAYLOAD_FEATURE]

            // This function will get called only when app is in Foreground
            // else it will send data to activity extras.
            if (payloadFeature != null && payloadFeature == FEATURE_ORDER_DETAILS && payloadParameters != null) {
                intent = Intent(context, BottomNavigationActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                for (item in payload.entries) {
                    intent.putExtra(item.key, item.value)
                }
                pendingIntent = PendingIntent.getActivity(
                    context, BottomNavigationActivity.DEEP_LINK_REQUEST_CODE, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            } else if (payload.contains(PAYLOAD_STREAM_CHANNEL)) {
                val streamChannelJson = payload[PAYLOAD_STREAM_CHANNEL]
                val streamChannelParameters = Gson().fromJson(
                    streamChannelJson,
                    JsonObject::class.java
                )
                // Stream Channel's cid needs to be in the format channelType:channelId. For example, messaging:123
                intent.putExtra(AppConstant.DP_LINKING_STREAM_CHAT_CHANNEL_ID, "${streamChannelParameters[PAYLOAD_STREAM_CHANNEL_TYPE].asString}:${streamChannelParameters[PAYLOAD_STREAM_CHANNEL_ID].asString}")
                pendingIntent = PendingIntent.getActivity(
                    context, BottomNavigationActivity.DEEP_LINK_REQUEST_CODE, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            } else {
                if (payloadFeature != null && payloadFeature == FEATURE_PRODUCT_LISTING && payloadParameters != null) {
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
                for (item in payload.entries) {
                    intent.putExtra(item.key, item.value)
                }
                pendingIntent = PendingIntent.getActivity(
                    context, BottomNavigationActivity.DEEP_LINK_REQUEST_CODE, intent,
                    PendingIntent.FLAG_ONE_SHOT
                )
            }

            var contentTitle: String? = notificationTitle ?: payload[PAYLOAD_TITLE]
            var contentText: String? = notificationBody ?: payload[PAYLOAD_BODY]
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

            val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
        }

        fun clearCachedConfig() {
            try {
                PersistenceLayer.getInstance()
                    .executeDeleteQuery("DELETE FROM ApiResponse WHERE ApiRequestId IN (SELECT id FROM ApiRequest WHERE endpoint = '/mobileconfigs');")
                PersistenceLayer.getInstance()
                    .executeDeleteQuery("DELETE FROM ApiRequest WHERE endpoint = '/mobileconfigs';")
            } catch (e: Exception) {
                FirebaseManager.logException(e)
            }
        }
    }
}