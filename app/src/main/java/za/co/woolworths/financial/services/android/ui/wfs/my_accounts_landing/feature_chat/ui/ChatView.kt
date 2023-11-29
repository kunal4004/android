package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_chat.ui

import android.os.Parcelable
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ChatCollectAgentFloatingButtonLayoutBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatBubbleVisibility
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatFloatingActionButtonBubbleView
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.ui.views.NotificationBadge
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.findActivity
import za.co.woolworths.financial.services.android.util.wenum.VocTriggerEvent
import javax.inject.Inject

@Preview
@Composable
fun SquareChatBubblePreview() {
    WfsChatView(accountList = mutableListOf())
}
@Composable
fun WfsChatView(accountList : List<Account>?) {

    AndroidView(
        factory = { context ->
            val view = LayoutInflater.from(context).inflate(R.layout.chat_collect_agent_floating_button_layout, null, false)
            val chatWithAgentFloatingButton =
                view.findViewById<FloatingActionButton>(R.id.chatBubbleFloatingButton)
            val notificationBadge = view.findViewById<NotificationBadge>(R.id.badge)
            val onlineIndicatorImageView =
                view.findViewById<ImageView>(R.id.onlineIndicatorImageView)

            val activity = context.findActivity()
            activity?.let {
                val inAppChatTipAcknowledgement = ChatFloatingActionButtonBubbleView(
                    activity = activity,
                    ChatBubbleVisibility(
                        accountList,
                        activity = activity
                    ),
                    chatWithAgentFloatingButton,
                    ApplyNowState.STORE_CARD,
                    activity,
                    notificationBadge,
                    onlineIndicatorImageView,
                    VocTriggerEvent.CHAT_SC_MYACCOUNTS
                )
                inAppChatTipAcknowledgement.build()
            }

            view },
        update = {}
    )
}

@Parcelize
data class ChatParams(val activity: @RawValue AppCompatActivity, val binding: @RawValue ChatCollectAgentFloatingButtonLayoutBinding, val applyNowState: ApplyNowState = ApplyNowState.STORE_CARD, val accountList: List<Account>?, val payMyAccountViewModel: @RawValue PayMyAccountViewModel) : Parcelable

class ChatView @Inject constructor() {
     fun chatToCollectionAgent(chatParams : ChatParams) {
         val (activity, binding, applyNowState, accountList, payMyAccountViewModel) = chatParams
         val chatToCollectionAgentView = ChatFloatingActionButtonBubbleView(activity,
                ChatBubbleVisibility(accountList, activity),
                binding.chatBubbleFloatingButton,
                applyNowState,
                notificationBadge = binding.badge,
                onlineChatImageViewIndicator = binding.onlineIndicatorImageView,
                vocTriggerEvent = payMyAccountViewModel.getVocTriggerEventMyAccounts())
        chatToCollectionAgentView.build()
    }

}