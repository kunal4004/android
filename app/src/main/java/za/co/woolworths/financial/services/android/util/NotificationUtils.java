package za.co.woolworths.financial.services.android.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.awfs.coordination.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApi;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.List;
import java.util.function.Consumer;

import retrofit2.Call;
import za.co.woolworths.financial.services.android.contracts.IResponseListener;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.CreateUpdateDevice;
import za.co.woolworths.financial.services.android.models.dto.CreateUpdateDeviceResponse;
import za.co.woolworths.financial.services.android.models.network.CompletionHandler;
import za.co.woolworths.financial.services.android.models.network.OneAppService;

public class NotificationUtils {

    private static String TAG = NotificationUtils.class.getSimpleName();
    private static NotificationUtils instance;

    private final Context appContext;


    public static NotificationUtils getInstance() {
        if (instance == null)
            instance = new NotificationUtils();

        return instance;
    }

    public NotificationUtils() {
        this.appContext = WoolworthsApplication.getInstance().getApplicationContext();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void createNotificationChannelIfNeeded(Context context){
        final String channelId = context.getResources().getString(R.string.default_notification_channel_id);
        final CharSequence channelName = context.getResources().getString(R.string.default_notification_channel_name);
        final String channelDescription = context.getResources().getString(R.string.default_notification_channel_description);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager.getNotificationChannel(channelId) == null){
            //this notification channel was not found, remove any other channels that exists
            List<NotificationChannel> notificationChannels = notificationManager.getNotificationChannels();

            notificationChannels.forEach(notificationChannel -> notificationManager.deleteNotificationChannel(notificationChannel.getId()));
        }

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build();

        //create notification channel
        NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.setDescription(channelDescription);
        notificationChannel.enableVibration(true);
        notificationChannel.setVibrationPattern(new long[]{0, 500, 500});
        notificationChannel.setSound(soundUri, audioAttributes);
        notificationManager.createNotificationChannel(notificationChannel);
    }

    // Clears notification tray messages
    public static void clearNotifications(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    public void sendRegistrationToServer(){
        if (isGooglePlayServicesAvailable()){
            FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
                if (task.isSuccessful())
                    sendRegistrationToServer(task.getResult().getToken());
            });
        }
	}

    public void sendRegistrationToServer(String token) {

        Log.d("FCM", token);
        // sending gcm token to server
        final CreateUpdateDevice device = new CreateUpdateDevice();
        device.appInstanceId = Utils.getUniqueDeviceID(WoolworthsApplication.getInstance().getApplicationContext());
        Utils.setToken(token);
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

    private Boolean isGooglePlayServicesAvailable() {
        // 1
        int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(appContext);
        // 2

        if (status != ConnectionResult.SUCCESS) {
            // ask user to update google play services and manage the error.
            return false;
        } else {
            // 3
            return true;
        }
    }
}
