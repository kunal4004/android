package za.co.woolworths.financial.services.android.util;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;

import com.awfs.coordination.R;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.ui.activities.WSplashScreenActivity;

public class NotificationUtils {

    private static String TAG = NotificationUtils.class.getSimpleName();
    public static final String PUSH_NOTIFICATION_INTENT = "PUSH_NOTIFICATION_INTENT";


    public Context mContext;

    private static final String GROUP_KEY = "Woolworths";
    private static final int SUMMARY_ID = 0;
    private final NotificationManagerCompat notificationManager;
    private final PendingIntent contentIntent;

    public static NotificationUtils newInstance(Context context) {
        Context appContext = context.getApplicationContext();
        Context safeContext = ContextCompat.createDeviceProtectedStorageContext(appContext);
        if (safeContext == null) {
            safeContext = appContext;
        }
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(safeContext);

        Intent myIntent = new Intent(safeContext, WSplashScreenActivity.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        myIntent.setAction(Intent.ACTION_MAIN);
        myIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        myIntent.putExtra(PUSH_NOTIFICATION_INTENT, PUSH_NOTIFICATION_INTENT);
        PendingIntent contentIntent = PendingIntent.getActivity(safeContext, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationUtils(safeContext, notificationManager, contentIntent);
    }

    private NotificationUtils(Context context,
                              NotificationManagerCompat notificationManager,
                              PendingIntent contentIntent) {
        this.mContext = context.getApplicationContext();
        this.notificationManager = notificationManager;
        this.contentIntent = contentIntent;
    }

    public void sendBundledNotification(String title, String body) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Notification notification = buildKitKatNotification(title, body);
            notificationManager.notify(getNotificationId(), notification);
        } else {
            Notification notification = buildNotification(title, body, GROUP_KEY);
            notificationManager.notify(getNotificationId(), notification);
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            Notification summary = buildSummary(GROUP_KEY);
            notificationManager.notify(SUMMARY_ID, summary);
        }
    }

    private Notification buildKitKatNotification(String title, String body) {
        return new NotificationCompat.Builder(mContext)
                .setContentTitle(title)
                .setContentText(body)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.appicon))
                .setContentIntent(contentIntent)
                .setShowWhen(true)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                .build();
    }

    private Notification buildNotification(String title, String body, String groupKey) {
        return new NotificationCompat.Builder(mContext)
                .setContentTitle(title)
                .setContentText(body)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.appicon))
                .setContentIntent(contentIntent)
                .setShowWhen(true)
                .setAutoCancel(true)
                .setGroup(groupKey)
                .setPriority(Notification.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                .build();
    }

    private Notification buildSummary(String groupKey) {
        return new NotificationCompat.Builder(mContext)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.appicon))
                .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentIntent(contentIntent)
                .setShowWhen(true)
                .setGroup(groupKey)
                .setAutoCancel(true)
                .setGroupSummary(true)
                .build();
    }

    private int getNotificationId() {

        String bundleId = Utils.getSessionDaoValue(mContext, SessionDao.KEY.NOTIFICATION_ID);
        int id = bundleId == null ? SUMMARY_ID + 1 : Integer.parseInt(bundleId) + 1;
        while (id == SUMMARY_ID) {
            id++;
        }
        Utils.sessionDaoSave(mContext, SessionDao.KEY.NOTIFICATION_ID, String.valueOf(id));
        return id;
    }

    public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }

    // Clears notification tray messages
    public static void clearNotifications(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

}
