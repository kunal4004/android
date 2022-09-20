package za.co.woolworths.financial.services.android.onecartgetstream.common.constant
import android.content.Context
import android.content.Intent
import za.co.woolworths.financial.services.android.onecartgetstream.service.DashChatMessageListeningService

object OCConstant {

    const val ORDER_PENDING_PICKING = "PENDING_PICKING"
    var OC_MESSAGE_COUNT = 0

    fun startOCChatService(context: Context?){
        val chatListeningServiceIntent = Intent(context, DashChatMessageListeningService::class.java)
        context?.startService(chatListeningServiceIntent)

    }
    fun stopOCChatService(context: Context?){
        val chatListeningServiceIntent = Intent(context, DashChatMessageListeningService::class.java)
        context?.stopService(chatListeningServiceIntent)
    }
}