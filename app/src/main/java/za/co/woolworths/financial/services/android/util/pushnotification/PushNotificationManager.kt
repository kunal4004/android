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
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import za.co.woolworths.financial.services.android.util.PersistenceLayer

class PushNotificationManager {
    companion object {
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
            val intent = Intent(context, StartupActivity::class.java)

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
                context, BottomNavigationActivity.DEEP_LINK_REQUEST_CODE, intent,
                PendingIntent.FLAG_ONE_SHOT
            )
            var contentTitle: String? = notificationTitle ?: payload["title"]
            var contentText: String? = notificationBody ?: payload["body"]
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