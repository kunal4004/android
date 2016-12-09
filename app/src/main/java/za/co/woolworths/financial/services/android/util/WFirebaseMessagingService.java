package za.co.woolworths.financial.services.android.util;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.awfs.coordination.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import za.co.woolworths.financial.services.android.ui.activities.MessagesActivity;

import static android.R.attr.data;
import static android.R.attr.id;
import static android.R.id.message;
import static za.co.woolworths.financial.services.android.ui.fragments.WFragmentDrawer.getData;

/**
 * Created by W7099877 on 09/11/2016.
 */

public class WFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = WFirebaseMessagingService.class.getSimpleName();

    private NotificationUtils notificationUtils;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.e(TAG, "From: " + remoteMessage.getFrom());
        if (remoteMessage == null)
            return;

       /* // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.e(TAG, "Notification Body: " + remoteMessage.getNotification().getBody());
           // handleNotification(remoteMessage.getNotification().getBody());
            showNotification();
        }*/
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
             Log.e(TAG, "Data Payload: " + remoteMessage.getData().toString());
            //String message = remoteMessage.getData().get("body");
            //String title = remoteMessage.getData().get("title");
           // showNotification(message, title);
            try {
                JSONObject json = new JSONObject(remoteMessage.getData().toString());
              // handleDataMessage(json);
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
            }
        }
        // showNotification();
    }

    private void handleNotification(String message) {
        if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
            // app is in foreground, broadcast the push message
            Intent pushNotification = new Intent(Utils.PUSH_NOTIFICATION);
            pushNotification.putExtra("message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
            // play notification sound
            NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
            notificationUtils.playNotificationSound();
        } else {
            // If the app is in background, firebase itself handles the notification
        }
    }

    private void handleDataMessage(JSONObject json) {
        Log.e(TAG, "push json: " + json.toString());
        try {
            //  JSONObject data = json.getJSONObject("data");
            String title = json.getString("title");
            String message = json.getString("body");
          /*  boolean isBackground = data.getBoolean("is_background");
            String imageUrl = data.getString("image");
            String timestamp = data.getString("timestamp");
            JSONObject payload = data.getJSONObject("payload");*/
            Log.e(TAG, "title: " + title);
            Log.e(TAG, "message: " + message);
           /* Log.e(TAG, "isBackground: " + isBackground);
            Log.e(TAG, "payload: " + payload.toString());
            Log.e(TAG, "imageUrl: " + imageUrl);
            Log.e(TAG, "timestamp: " + timestamp);*/
            if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
                // app is in foreground, broadcast the push message
                Intent pushNotification = new Intent(Utils.PUSH_NOTIFICATION);
                pushNotification.putExtra("message", message);
                LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
                // play notification sound
                NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
                notificationUtils.playNotificationSound();

            } else {
               /* // app is in background, show the notification in notification tray
                Intent resultIntent = new Intent(getApplicationContext(), MessagesActivity.class);
                resultIntent.putExtra("message", message);

                // check for image attachment
                if (TextUtils.isEmpty(imageUrl)) {
                    showNotificationMessage(getApplicationContext(), title, message, timestamp, resultIntent);
                } else {
                    // image is present, show notification with image
                    showNotificationMessageWithBigImage(getApplicationContext(), title, message, timestamp, resultIntent, imageUrl);
                }*/
                showNotification(message, title);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Json Exception: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    /**
     * Showing notification with text only
     */
    private void showNotificationMessage(Context context, String title, String message, String timeStamp, Intent intent) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent);
    }

    /**
     * Showing notification with text and image
     */
    private void showNotificationMessageWithBigImage(Context context, String title, String message, String timeStamp, Intent intent, String imageUrl) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent, imageUrl);
    }

    public void showNotification(String msg, String title) {
        /*Intent resultIntent = new Intent(this, MessagesActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
// Adds the back stack
        stackBuilder.addParentStack(MessagesActivity.class);
// Adds the Intent to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
// Gets a PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);*/
        Intent myIntent = new Intent(this, MessagesActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.addLine(msg);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentIntent(contentIntent);
        builder.setContentTitle(title);
        builder.setContentText(msg);
        builder.setSmallIcon(R.drawable.appicon);
        builder.setStyle(inboxStyle);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(id, builder.build());
    }

    /*@Override
    public void onCreate() {
        super.onCreate();
        android.os.Debug.waitForDebugger();
    }*/
}
