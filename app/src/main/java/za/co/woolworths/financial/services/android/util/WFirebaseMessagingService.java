package za.co.woolworths.financial.services.android.util;

import static za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.DEEP_LINK_REQUEST_CODE;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.awfs.coordination.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.Serializable;
import java.util.Map;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.fcm.FCMMessageType;
import za.co.woolworths.financial.services.android.startup.view.StartupActivity;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity;

public class WFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = WFirebaseMessagingService.class.getSimpleName();
    private static final String FEATURE_ORDER_DETAILS = "Order Details";
    private static final String EXTRA_PAYLOAD = "payload";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);

        NotificationUtils.getInstance().sendRegistrationToServer(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationUtils.createNotificationChannelIfNeeded(this);
        }

        if (WoolworthsApplication.isApplicationInForeground()){
            Log.i(TAG, remoteMessage.getData().toString());
            sendNotification(remoteMessage.getNotification(), remoteMessage.getData());

            //maybe have a look at this LocalBroadcastManager
        }

        Map<String,String> data = remoteMessage.getData();
        if(data != null && data.containsKey("type")){
            String type = data.get("type");
            switch (type){
                case FCMMessageType
                        .mcConfigClear:{
                    mcConfigClear();
                }
            }
            return;
        }
    }

    private void sendNotification(RemoteMessage.Notification notification, @NonNull Map<String, String> payload){

        final String channelId = getResources().getString(R.string.default_notification_channel_id);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(WoolworthsApplication.getAppContext(), channelId);

        Intent intent = new Intent(this, StartupActivity.class);

        PendingIntent pendingIntent;
        String payloadParameters = payload.get("parameters");
        String payloadFeature = payload.get("feature");

        // This function will get called only when app is in Foreground
        // else it will send data to activity extras.
        if (payloadFeature != null &&
                payloadFeature.equals(FEATURE_ORDER_DETAILS) &&
                payloadParameters != null) {

            intent = new Intent(this, BottomNavigationActivity.class);
            for (Map.Entry<String, String> keyValue : payload.entrySet()) {
                intent.putExtra(keyValue.getKey(), keyValue.getValue());
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pendingIntent = PendingIntent.getActivity(this, DEEP_LINK_REQUEST_CODE, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        } else {

            if (payloadFeature != null &&
                    payloadFeature.equals("Product Listing") &&
                    payloadParameters != null) {

                String json = payloadParameters.replaceAll("\\\\", "");
                JsonObject parameters = new Gson().fromJson(json, JsonObject.class);

                intent.setData(Uri.parse(parameters.get("url").getAsString()));
                intent.setAction(Intent.ACTION_VIEW);
            }

            /*Deep link to PDP disabled*/
        /*else if (payload.get("feature").equals("Product Detail")){
            String json = payload.get("parameters").replaceAll("\\\\", "");
            JsonObject parameters = new Gson().fromJson(json, JsonObject.class);

            intent = new Intent(this, ProductDetailsDeepLinkActivity.class);
            intent.setData(Uri.parse(parameters.get("url").getAsString()));
            intent.setAction(Intent.ACTION_VIEW);
        }*/

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            pendingIntent = PendingIntent.getActivity(this, DEEP_LINK_REQUEST_CODE, intent,
                    PendingIntent.FLAG_ONE_SHOT);
        }

        String contentTitle = null;
        String contentText = null;

        if (notification == null) {
            contentTitle = payload.get("title");
            contentText = payload.get("body");
        } else {
            contentTitle = notification.getTitle();
            contentText = notification.getBody();
        }

        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(Color.BLACK)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    //    #region FCM Methods
    private void mcConfigClear() {
        try {
            PersistenceLayer.getInstance().executeDeleteQuery("DELETE FROM ApiResponse WHERE ApiRequestId IN (SELECT id FROM ApiRequest WHERE endpoint = '/mobileconfigs');");
            PersistenceLayer.getInstance().executeDeleteQuery("DELETE FROM ApiRequest WHERE endpoint = '/mobileconfigs';");
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }
//    #endregion


}
