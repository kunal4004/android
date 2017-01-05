package za.co.woolworths.financial.services.android.util;


import android.app.Notification;
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

import java.util.Map;

import za.co.woolworths.financial.services.android.ui.activities.MessagesActivity;
import za.co.woolworths.financial.services.android.ui.activities.WSplashScreenActivity;

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

        Map<String, String> data = remoteMessage.getData();
        if (data.size() > 0 && NotificationUtils.isAppIsInBackground(getApplicationContext())) {// Check if message contains a data payload.
            Intent myIntent = new Intent(this, WSplashScreenActivity.class);

            myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            myIntent.setAction(Intent.ACTION_MAIN);
            myIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            inboxStyle.addLine(data.get("body"));
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            builder.setContentIntent(contentIntent);
            builder.setContentTitle(data.get("title"));
            builder.setContentText(data.get("body"));
            builder.setSmallIcon(R.drawable.appicon);
            builder.setStyle(inboxStyle);
            builder.setPriority(Notification.PRIORITY_HIGH);
            builder.setDefaults(Notification.DEFAULT_ALL);
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(id, builder.build());
        }
    }
}
