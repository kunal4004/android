package za.co.woolworths.financial.services.android.util;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import za.co.woolworths.financial.services.android.models.dao.SessionDao;

public class WFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = WFirebaseMessagingService.class.getSimpleName();

    private NotificationUtils notificationUtils;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage == null)
            return;

        try {
            String unreadCountValue = Utils.getSessionDaoValue(this, SessionDao.KEY.UNREAD_MESSAGE_COUNT);

            if (TextUtils.isEmpty(unreadCountValue) || unreadCountValue == null) {
                Utils.sessionDaoSave(this, SessionDao.KEY.UNREAD_MESSAGE_COUNT, "0");
                Utils.setBadgeCounter(this, 1);
            } else {
                int unreadCount = Integer.valueOf(unreadCountValue) + 1;
                Utils.setBadgeCounter(this, unreadCount);
            }
        } catch (NullPointerException ignored) {
        }

        Map<String, String> data = remoteMessage.getData();
        if (data.size() > 0 && NotificationUtils.isAppIsInBackground(getApplicationContext())) {// Check if message contains a data payload.
            notificationUtils=NotificationUtils.newInstance(this);
            notificationUtils.sendBundledNotification(data.get("title"),data.get("body"));
        } else if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
            Intent intent = new Intent("UpdateCounter");
            LocalBroadcastManager.
                    getInstance(getApplicationContext()).sendBroadcast(intent);
        }
    }
}
