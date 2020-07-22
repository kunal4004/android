package za.co.woolworths.financial.services.android.util;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.awfs.coordination.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.List;

import me.leolin.shortcutbadger.ShortcutBadger;
import retrofit2.Call;
import za.co.woolworths.financial.services.android.contracts.IResponseListener;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.CreateUpdateDevice;
import za.co.woolworths.financial.services.android.models.dto.CreateUpdateDeviceResponse;
import za.co.woolworths.financial.services.android.models.network.CompletionHandler;
import za.co.woolworths.financial.services.android.models.network.OneAppService;
import za.co.woolworths.financial.services.android.ui.activities.StartupActivity;

public class NotificationUtils {

    private static String TAG = NotificationUtils.class.getSimpleName();

    private static NotificationUtils instance;

    public static NotificationUtils getInstance() {
        if (instance == null)
            instance = new NotificationUtils();

        return instance;
    }

    // Clears notification tray messages
    public static void clearNotifications(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    public void sendRegistrationToServer(){
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
            if (task.isSuccessful())
                sendRegistrationToServer(task.getResult().getToken());
        });
	}

    public void sendRegistrationToServer(String token) {

        Log.d("FCM", token);
        // sending gcm token to server
        final CreateUpdateDevice device = new CreateUpdateDevice();
        device.appInstanceId = Utils.getUniqueDeviceID(WoolworthsApplication.getInstance().getApplicationContext());
        device.pushNotificationToken = token;

        //Don't update token if pushNotificationToken or appInstanceID NULL
        if(device.appInstanceId == null || device.pushNotificationToken==null)
            return;


        //Sending Token and app instance Id to App server
        //Need to be done after Login

        Call<CreateUpdateDeviceResponse> createUpdateDeviceCall = OneAppService.INSTANCE.getResponseOnCreateUpdateDevice(device);
        createUpdateDeviceCall.enqueue(new CompletionHandler<>(new IResponseListener<CreateUpdateDeviceResponse>() {
            @Override
            public void onSuccess(CreateUpdateDeviceResponse response) {

            }

            @Override
            public void onFailure(Throwable error) {

            }
        },CreateUpdateDeviceResponse.class));
    }
}
