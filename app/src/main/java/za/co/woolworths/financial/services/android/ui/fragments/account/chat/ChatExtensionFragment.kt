package za.co.woolworths.financial.services.android.ui.fragments.account.chat

import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.chat_fragment.*
import za.co.woolworths.financial.services.android.models.dto.ChatMessage
import za.co.woolworths.financial.services.android.models.dto.SendEmailIntentInfo
import za.co.woolworths.financial.services.android.ui.adapters.WChatAdapter

open class ChatExtensionFragment : Fragment() {

    var mChatAdapter: WChatAdapter? = null
    var productOfferingId: String? = null
    var accountNumber: String? = null

    companion object {
        const val ACCOUNTS : String = "accounts"
        const val CARD : String = "CARD"
        const val SESSION_TYPE = "SESSION_TYPE"
    }

    fun updateMessageList(message: ChatMessage) {
        activity?.runOnUiThread {
            mChatAdapter?.let {
                it.addMessage(message)
                messageListRecyclerView?.scrollToPosition(it.itemCount - 1)
            }
        }
    }

    fun showAgentsMessage(agentDefaultMessage: String) {
        updateMessageList(ChatMessage(ChatMessage.Type.RECEIVED, message = agentDefaultMessage))
    }

    fun showAgentsMessage(agentDefaultMessage: String, emailIntentInfo: SendEmailIntentInfo) {
        updateMessageList(ChatMessage(ChatMessage.Type.RECEIVED, message = agentDefaultMessage, sendEmailIntentInfo = emailIntentInfo))
    }
}