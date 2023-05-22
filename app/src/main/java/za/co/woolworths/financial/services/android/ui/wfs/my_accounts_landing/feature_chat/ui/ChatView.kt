package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_chat.ui

import android.view.LayoutInflater
import android.widget.ImageView
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.awfs.coordination.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatBubbleVisibility
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatFloatingActionButtonBubbleView
import za.co.woolworths.financial.services.android.ui.views.NotificationBadge
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.findActivity
import za.co.woolworths.financial.services.android.util.wenum.VocTriggerEvent

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
