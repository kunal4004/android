package za.co.woolworths.financial.services.android.util;

import android.content.Intent;

import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.fcm.FCMMessageType;

public class WFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = WFirebaseMessagingService.class.getSimpleName();

    private NotificationUtils notificationUtils;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (remoteMessage == null)
            return;

        Map<String,String> data = remoteMessage.getData();
        if(data.containsKey("type")){
            String type = data.get("type");
            switch (type){
                case FCMMessageType
                        .mcConfigClear:{
                    mcConfigClear();
                }
            }
            return;
        }

        //Push Notification Message Handler down onward i.e no data message
        String unreadCountString = Utils.getSessionDaoValue(this, SessionDao.KEY.UNREAD_MESSAGE_COUNT);
        int unreadCountValue;
        try{
            unreadCountValue = Integer.parseInt(unreadCountString);
        }catch (Exception e){
            unreadCountValue = 0;
        }

        if (data.size() > 0 && NotificationUtils.isAppIsInBackground(getApplicationContext())) {// Check if message contains a data payload.
            notificationUtils=NotificationUtils.newInstance(this);
            notificationUtils.sendBundledNotification(data.get("title"),data.get("body"), unreadCountValue);
        } else if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
            Intent intent = new Intent("UpdateCounter");
            LocalBroadcastManager.
                    getInstance(getApplicationContext()).sendBroadcast(intent);
        }
    }

    //#region FCM Methods
    private void mcConfigClear(){
        try {
            PersistenceLayer.getInstance().executeDeleteQuery("DELETE FROM ApiResponse WHERE ApiRequestId IN (SELECT id FROM ApiRequest WHERE endpoint = '/mobileconfigs');");
            PersistenceLayer.getInstance().executeDeleteQuery("DELETE FROM ApiRequest WHERE endpoint = '/mobileconfigs';");
        }catch (Exception e)
        {
            Log.e(TAG,e.getMessage());
        }
    }
    //#endregion


}
