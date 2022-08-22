package za.co.woolworths.financial.services.android.util

import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.getUniqueDeviceID
import za.co.woolworths.financial.services.android.models.network.OneAppService.getResponseOnCreateUpdateDevice
import com.google.firebase.messaging.FirebaseMessaging
import za.co.woolworths.financial.services.android.models.dto.CreateUpdateDevice
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject
import za.co.woolworths.financial.services.android.models.dto.CreateUpdateDeviceResponse
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.util.NotificationUtils
import androidx.annotation.RequiresApi
import android.app.NotificationManager
import android.app.NotificationChannel
import android.content.Context
import android.media.RingtoneManager
import android.media.AudioAttributes
import android.os.Build
import android.text.TextUtils
import com.awfs.coordination.R
import com.google.android.gms.tasks.Task
import com.huawei.hms.aaid.HmsInstanceId
import com.huawei.hms.common.ApiException
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import java.util.function.Consumer

class NotificationUtils {
    companion object {
        private const val TOKEN_PROVIDER_FIREBASE = "firebase"
        private const val TOKEN_PROVIDER_HMS = "hms"

        fun sendRegistrationToServer(context: Context) {
            if (Utils.isGooglePlayServicesAvailable()) {
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task: Task<String?> ->
                    if (task.isSuccessful) {
                        sendRegistrationToServer(task.result)
                    }
                }
            } else if (Utils.isHuaweiMobileServicesAvailable()) {
                object : Thread() {
                    override fun run() {
                        try {
                            val token = HmsInstanceId
                                .getInstance(context)
                                .getToken(context.getString(R.string.huawei_app_id).replace("appid=", ""), "HCM")
                            if (!TextUtils.isEmpty(token)) {
                                sendRegistrationToServer(token)
                            }
                        } catch (e: ApiException) {
                            FirebaseManager.logException(e)
                        }
                    }
                }.start()
            }
        }

        fun sendRegistrationToServer(token: String?) {
            // sending FCM/HMS token to server
            val device = CreateUpdateDevice()
            getUniqueDeviceID { deviceId: String? ->
                device.appInstanceId = deviceId
                Utils.setToken(token)
                device.pushNotificationToken = token
                device.deviceIdentityId =
                    AppInstanceObject.get().currentUserObject.linkedDeviceIdentityId
                device.tokenProvider = if (Utils.isGooglePlayServicesAvailable()) TOKEN_PROVIDER_FIREBASE else TOKEN_PROVIDER_HMS

                //Don't update token if pushNotificationToken or appInstanceID NULL
                if (device.appInstanceId == null || device.pushNotificationToken == null) return@getUniqueDeviceID

                //Sending Token and app instance Id to App server
                //Need to be done after Login
                val createUpdateDeviceCall = getResponseOnCreateUpdateDevice(device)
                createUpdateDeviceCall.enqueue(
                    CompletionHandler(
                        object : IResponseListener<CreateUpdateDeviceResponse> {
                            override fun onSuccess(response: CreateUpdateDeviceResponse?) {}
                            override fun onFailure(error: Throwable?) {}
                        }, CreateUpdateDeviceResponse::class.java
                    )
                )
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        fun createNotificationChannelIfNeeded(context: Context) {
            val channelId = context.resources.getString(R.string.default_notification_channel_id)
            val channelName: CharSequence =
                context.resources.getString(R.string.default_notification_channel_name)
            val channelDescription =
                context.resources.getString(R.string.default_notification_channel_description)
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (notificationManager.getNotificationChannel(channelId) == null) {
                //this notification channel was not found, remove any other channels that exists
                val notificationChannels = notificationManager.notificationChannels
                notificationChannels.forEach(Consumer { notificationChannel: NotificationChannel ->
                    notificationManager.deleteNotificationChannel(
                        notificationChannel.id
                    )
                })
            }
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            //create notification channel
            val notificationChannel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.description = channelDescription
            notificationChannel.enableVibration(true)
            notificationChannel.vibrationPattern = longArrayOf(0, 500, 500)
            notificationChannel.setSound(soundUri, audioAttributes)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        // Clears notification tray messages
        @JvmStatic
        fun clearNotifications(context: Context) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancelAll()
        }
    }

}