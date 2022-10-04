package za.co.woolworths.financial.services.android.onecartgetstream.common.constant
import android.content.Context
import android.content.Intent

import za.co.woolworths.financial.services.android.onecartgetstream.service.DashChatMessageListeningService
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager

class OCConstant {

    companion object {
        const val ORDER_PENDING_PICKING = "PENDING_PICKING"
        var ocChatMessageCount = 0
        var isOCChatBackgroundServiceRunning = false

        fun startOCChatService(context: Context?) {
            try {
                if (!isOCChatBackgroundServiceRunning) {
                    val chatListeningServiceIntent =
                        Intent(context, DashChatMessageListeningService::class.java)
                    context?.startService(chatListeningServiceIntent)
                }
            } catch (e: Exception) {
                FirebaseManager.logException(e)
            }
        }

        fun stopOCChatService(context: Context?){
            val chatListeningServiceIntent = Intent(context, DashChatMessageListeningService::class.java)
            context?.stopService(chatListeningServiceIntent)
        }


    }

}