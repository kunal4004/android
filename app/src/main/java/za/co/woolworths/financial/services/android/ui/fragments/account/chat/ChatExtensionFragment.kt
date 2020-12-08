package za.co.woolworths.financial.services.android.ui.fragments.account.chat

import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.chat_fragment.*
import za.co.woolworths.financial.services.android.models.dto.ChatMessage
import za.co.woolworths.financial.services.android.ui.adapters.WChatAdapter

open class ChatExtensionFragment : Fragment() {

    var mChatAdapter: WChatAdapter? = null
    var productOfferingId: String? = null
    var accountNumber: String? = null

    companion object {
        const val PRODUCT_OFFERING_ID = "productOfferingId"
        const val ACCOUNT_NUMBER = "accountNumber"
        const val ACCOUNTS : String = "accounts"
        const val CARD : String = "CARD"
        const val SESSION_TYPE = "SESSION_TYPE"
        const val FROM_ACTIVITY = "FROM_ACTIVITY"
        const val CHAT_TO_COLLECTION_AGENT = "CHAT_TO_COLLECTION_AGENT"
    }

    fun updateMessageList(message: ChatMessage) {
        activity?.runOnUiThread {
            mChatAdapter?.let {
                it.addMessage(message)
                messageListRecyclerView?.scrollToPosition(it.itemCount - 1)
            }
        }
    }

    fun showAgentsMessage(agentDefaultMessage: String, moreMessage: String = "") {
        updateMessageList(ChatMessage(ChatMessage.Type.RECEIVED, moreMessage + agentDefaultMessage))
    }
}