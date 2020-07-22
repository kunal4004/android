package za.co.woolworths.financial.services.android.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.awfs.coordination.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.fcm.FCMMessageType;
import za.co.woolworths.financial.services.android.ui.activities.CartActivity;
import za.co.woolworths.financial.services.android.ui.activities.StartupActivity;

public class WFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = WFirebaseMessagingService.class.getSimpleName();
    private static final String DEFAULT_NOTIFICATION_CHANNEL_NAME = "Woolworths";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);

        NotificationUtils.getInstance().sendRegistrationToServer(token);

        Log.i(TAG, token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {

        Log.i(TAG, remoteMessage.getData().toString());
        sendNotification(remoteMessage.getNotification(), remoteMessage.getData());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void buildNotificationChannel(){
        final String DEFAULT_NOTIFICATION_CHANNEL = getResources().getString(R.string.default_notification_channel_id);
        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.deleteNotificationChannel("wfs_channel_id");
        notificationManager.deleteNotificationChannel(DEFAULT_NOTIFICATION_CHANNEL);

        AudioAttributes soundAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build();

        NotificationChannel channel = new NotificationChannel(DEFAULT_NOTIFICATION_CHANNEL, DEFAULT_NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(DEFAULT_NOTIFICATION_CHANNEL_NAME.concat(" default notification channel"));
        channel.enableVibration(true);
        channel.setVibrationPattern(new long[] { 0, 1000, 500, 1000 });
//        channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), soundAttributes);
        notificationManager.createNotificationChannel(channel);
    }

    private void sendNotification(RemoteMessage.Notification notification, @NonNull Map<String, String> payload){

        NotificationCompat.Builder notificationBuilder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            buildNotificationChannel();
            final String DEFAULT_NOTIFICATION_CHANNEL = getResources().getString(R.string.default_notification_channel_id);
            notificationBuilder = new NotificationCompat.Builder(WoolworthsApplication.getAppContext(), DEFAULT_NOTIFICATION_CHANNEL);
        } else {
            notificationBuilder = new NotificationCompat.Builder(WoolworthsApplication.getAppContext());
        }

        Intent intent = null;
        if (payload.get("feature").equals("Product Listing")){
            intent = new Intent(this, CartActivity.class);
        } else{
            intent = new Intent(this, StartupActivity.class);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.appicon)
                .setContentText(notification.getBody())
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                .setContentIntent(pendingIntent);

        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

//    private void sendNotification(RemoteMessage.Notification notification){
//
//        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        NotificationCompat.Builder notificationBuilder;
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            notificationBuilder = new NotificationCompat.Builder(this, DEFAULT_NOTIFICATION_CHANNEL);
//
//            NotificationChannel channel = new NotificationChannel(DEFAULT_NOTIFICATION_CHANNEL, DEFAULT_NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
//            channel.setDescription(notification.getBody());
//            notificationManager.createNotificationChannel(channel);
//        } else {
//            Context appContext = WoolworthsApplication.getAppContext();
//            Context safeContext = ContextCompat.createDeviceProtectedStorageContext(appContext);
//
//            if (safeContext == null)
//                safeContext = appContext;
//
//            Intent intent = new Intent(safeContext, StartupActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//
//            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
//            notificationBuilder = new NotificationCompat.Builder(this, null);
//            notificationBuilder.setContentIntent(contentIntent);
//        }
//
//        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//
//        notificationBuilder.setAutoCancel(true)
//                .setDefaults(Notification.DEFAULT_ALL)
//                .setWhen(System.currentTimeMillis())
//                .setSmallIcon(R.drawable.appicon)
//                .setContentText(notification.getBody())
//                .setSound(soundUri)
//                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });
//
//        notificationManager.notify((int)System.currentTimeMillis(), notificationBuilder.build());
//    }

    //    @Override
//    public void onMessageReceived(RemoteMessage remoteMessage) {
//
//        if (remoteMessage == null)
//            return;
//
//        Map<String,String> data = remoteMessage.getData();
//        if(data.containsKey("type")){
//            String type = data.get("type");
//            switch (type){
//                case FCMMessageType
//                        .mcConfigClear:{
//                    mcConfigClear();
//                }
//            }
//            return;
//        }
//
//        //Push Notification Message Handler down onward i.e no data message
//        String unreadCountString = Utils.getSessionDaoValue(SessionDao.KEY.UNREAD_MESSAGE_COUNT);
//        int unreadCountValue;
//        try{
//            unreadCountValue = Integer.parseInt(unreadCountString);
//        }catch (Exception e){
//            unreadCountValue = 0;
//        }
//
//        if (data.size() > 0 && NotificationUtils.isAppIsInBackground(getApplicationContext())) {// Check if message contains a data payload.
//            //notificationUtils = NotificationUtils.newInstance(this);
//            NotificationUtils.getInstance().sendBundledNotification(data.get("title"),data.get("body"), unreadCountValue);
//        } else if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
//            Intent intent = new Intent("UpdateCounter");
//            LocalBroadcastManager.
//                    getInstance(getApplicationContext()).sendBroadcast(intent);
//        }
//    }

    //#region FCM Methods
//    private void mcConfigClear(){
//        try {
//            PersistenceLayer.getInstance().executeDeleteQuery("DELETE FROM ApiResponse WHERE ApiRequestId IN (SELECT id FROM ApiRequest WHERE endpoint = '/mobileconfigs');");
//            PersistenceLayer.getInstance().executeDeleteQuery("DELETE FROM ApiRequest WHERE endpoint = '/mobileconfigs';");
//        }catch (Exception e)
//        {
//            Log.e(TAG,e.getMessage());
//        }
//    }
    //#endregion


}
