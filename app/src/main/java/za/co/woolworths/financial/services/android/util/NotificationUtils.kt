package za.co.woolworths.financial.services.android.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.awfs.coordination.R
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject
import za.co.woolworths.financial.services.android.models.dto.CreateUpdateDevice
import za.co.woolworths.financial.services.android.models.dto.CreateUpdateDeviceResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService.getResponseOnCreateUpdateDevice
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.getUniqueDeviceID
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import javax.inject.Inject

class NotificationUtils @Inject constructor(@ApplicationContext private val context: Context) {

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun createNotificationChannelIfNeeded() {
        val channelId = context.resources.getString(R.string.default_notification_channel_id)
        val channelName: CharSequence = context.resources.getString(R.string.default_notification_channel_name)
        val channelDescription = context.resources.getString(R.string.default_notification_channel_description)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.getNotificationChannel(channelId) == null) {
            //this notification channel was not found, remove any other channels that exists
            val notificationChannels = notificationManager.notificationChannels

            notificationChannels.forEach {  notificationChannel ->
                try {
                    notificationManager.deleteNotificationChannel(notificationChannel.id)
                }catch (e : IllegalArgumentException){
                    // TODO:: Handle cannot delete default channel on android 11 and above
                    FirebaseManager.logException(e)
                }
            }
        }
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()

        //create notification channel
        val notificationChannel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
        notificationChannel.description = channelDescription
        notificationChannel.enableVibration(true)
        notificationChannel.vibrationPattern = longArrayOf(0, 500, 500)
        notificationChannel.setSound(soundUri, audioAttributes)
        notificationManager.createNotificationChannel(notificationChannel)
    }

    // Clears notification tray messages
    fun clearNotifications() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    fun sendRegistrationToServer(token: String?) {
        Log.d("FCM", token!!)
        // sending gcm token to server
        val device = CreateUpdateDevice()
        getUniqueDeviceID { deviceId: String? ->
            device.appInstanceId = deviceId
            Utils.setToken(token)
            device.pushNotificationToken = token
            device.deviceIdentityId =
                AppInstanceObject.get().currentUserObject.linkedDeviceIdentityId

            //Don't update token if pushNotificationToken or appInstanceID NULL
            if (device.appInstanceId == null || device.pushNotificationToken == null) return@getUniqueDeviceID

            //Sending Token and app instance Id to App server
            //Need to be done after Login
            val createUpdateDeviceCall =
                getResponseOnCreateUpdateDevice(device)
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

    private fun isGooglePlayServicesAvailable(): Boolean {
        // 1
        val status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
        // 2
        return status == ConnectionResult.SUCCESS
    }

    fun sendRegistrationToServer() {
        if (isGooglePlayServicesAvailable()) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task: Task<String?> ->
                if (task.isSuccessful) sendRegistrationToServer(task.result)
            }
        }
    }
}