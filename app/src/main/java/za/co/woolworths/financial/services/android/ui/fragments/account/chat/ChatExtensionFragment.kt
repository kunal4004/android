package za.co.woolworths.financial.services.android.ui.fragments.account.chat

import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.chat_fragment.*
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.ChatMessage
import za.co.woolworths.financial.services.android.models.dto.chat.TradingHours
import za.co.woolworths.financial.services.android.ui.adapters.WChatAdapter
import za.co.woolworths.financial.services.android.util.Utils

open class ChatExtensionFragment : Fragment() {

    var mChatAdapter: WChatAdapter? = null
    var productOfferingId: String? = null
    var accountNumber: String? = null

    enum class AgentDefaultMessage(val message: String) {
        GENERAL_ERROR("We’re currently experiencing technical issues, please try again later or call us on 0861 50 20 20."),
        AGENT_PICKED(" will be assisting you further, enjoy the rest of your day."),
        CONNECTING_AGENT("Please stay online for the next available consultant."),
        CHAT_ENDED_WITH_ANY_REASON("Your chat session has ended."),
        NO_AGENTS("There are no consultants available at the moment, please try again later" +
                " or call us on 0861 50 20 20.")
    }

    companion object {
        const val PRODUCT_OFFERING_ID = "productOfferingId"
        const val ACCOUNT_NUMBER = "accountNumber"
        const val ACCOUNTS : String = "accounts"
        const val SESSION_TYPE = "SESSION_TYPE"
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

    fun showAgentsMessage(agentDefaultMessage: AgentDefaultMessage, moreMessage: String = "") {
        updateMessageList(ChatMessage(ChatMessage.Type.RECEIVED, moreMessage + agentDefaultMessage.message))
    }

    fun showAgentsMessage(agentDefaultMessage: String, moreMessage: String = "") {
        updateMessageList(ChatMessage(ChatMessage.Type.RECEIVED, moreMessage + agentDefaultMessage))
    }
}