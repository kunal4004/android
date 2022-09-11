package za.co.woolworths.financial.services.android.onecartgetstream.service

import android.content.Intent
import androidx.lifecycle.LifecycleService
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.notifications.handler.ChatNotificationHandler
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.pushprovider.firebase.FirebasePushDeviceGenerator
import io.getstream.chat.android.pushprovider.huawei.HuaweiPushDeviceGenerator
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.onecartgetstream.common.constant.OCConstant.ACTION_PAUSE_OC_NOTIFICATION_SERVICE
import za.co.woolworths.financial.services.android.onecartgetstream.common.constant.OCConstant.ACTION_START_RESUME_OC_NOTIFICATION_SERVICE
import za.co.woolworths.financial.services.android.onecartgetstream.common.constant.OCConstant.ACTION_STOP_OC_NOTIFICATION_SERVICE
import za.co.woolworths.financial.services.android.util.Utils

class OCToastNotificationService : LifecycleService() {

    var isFirstServiceRun = true

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_RESUME_OC_NOTIFICATION_SERVICE -> {
                    if (isFirstServiceRun) {
                        isFirstServiceRun = false
                        //TODO: run service
                        initChatSdk()

                    } else{

                    }

                }
                ACTION_PAUSE_OC_NOTIFICATION_SERVICE -> {

                }
                ACTION_STOP_OC_NOTIFICATION_SERVICE -> {

                }
                else -> ""
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun initChatSdk() {
        val notificationConfig = NotificationConfig(
            pushDeviceGenerators = listOf(
                if (Utils.isGooglePlayServicesAvailable())
                    FirebasePushDeviceGenerator()
                else
                    HuaweiPushDeviceGenerator(
                        WoolworthsApplication.getAppContext(),
                        appId = "102461773" // TODO: move hardcoded huawei app ID somewhere else
                    )
            )
        )

        val client = ChatClient.Builder(AppConfigSingleton.dashConfig?.inAppChat?.apiKey.toString(), WoolworthsApplication.getAppContext())
            .logLevel(ChatLogLevel.ALL)
            .notifications(ChatNotificationHandler(WoolworthsApplication.getAppContext(), notificationConfig))
            .build()

        ChatDomain.Builder(client, WoolworthsApplication.getAppContext())
            .userPresenceEnabled()
            .offlineEnabled()
            .build()
    }
}