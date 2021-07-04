package za.co.woolworths.financial.services.android.ui.fragments.account.chat.content

import android.content.Context
import androidx.core.app.NotificationCompat
import za.co.woolworths.financial.services.android.models.dto.VoucherCount
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.model.SendMessageResponse

interface ILiveChatNotification {
    fun createNotificationChannel(context: Context?): NotificationCompat.Builder?
    fun headUpNotification(unreadMessageCount: Int,messageResponse: SendMessageResponse, context: Context?)
    fun broadcastResultToAmplifySubscribe(context: Context?, result: String?)
    fun broadcastMessageCountResult(context: Context?)
    fun broadcastResultShowNoConnectionToast(context: Context?)
}