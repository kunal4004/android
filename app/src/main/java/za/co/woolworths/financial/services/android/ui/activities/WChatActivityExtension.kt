package za.co.woolworths.financial.services.android.ui.activities

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.chat_activity.*
import za.co.woolworths.financial.services.android.contracts.RequestListener
import za.co.woolworths.financial.services.android.models.dto.ChatMessage
import za.co.woolworths.financial.services.android.models.dto.chat.UserTypingResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.adapters.WChatAdapter
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.WCountDownTimer

open class WChatActivityExtension : AppCompatActivity(), WCountDownTimer.TimerFinishListener {

    var adapter: WChatAdapter? = null
    var productOfferingId: String? = null
    var accountNumber: String? = null
    var disposablesAgentsAvailable: CompositeDisposable? = null
    var disposablesChatSessionState: CompositeDisposable? = null
    var chatId: String? = null
    var usersOfflineMessage: ChatMessage = ChatMessage(ChatMessage.Type.SENT, "")
    var isAgentOnline: Boolean = false // This becomes "true" when a agent picks the call -> (ChatStatus.state=STATUS_ONLINE)
    var pollingTimer: WCountDownTimer? = null

    enum class AgentDefaultMessage(val message: String) {
        AGENT_ONLINE("Hi " + SessionUtilities.getInstance().jwt?.name?.get(0) + ". How can we help you"),
        AGENT_OFFLINE("Our live chat service will be back online at 8:30am. \\n\" +\n" +
                "                \"\\n\" +\n" +
                "                \"If you have an urgent matter, contact us on +27 62 5960 496 or mail us at cards@woolworths.com."),
        GENERAL_ERROR("We are facing issues, please come back later"),
        AGENT_PICKED("You are now chatting to "),
        CONNECTING_AGENT("Please be patient while we connect you to an agent"),
        CHAT_ENDED_WITH_ANY_REASON("Unfortunately, your chat session has ended"),
        NO_AGENTS("Unfortunately there are no agents available, please try again later or call the call centre.")
    }

    companion object {
        const val POLLING_INTERVAL_AGENT_AVAILABLE: Long = 5  //seconds
        const val POLLING_INTERVAL_CHAT_SESSION_STATE: Long = 5 //seconds
        const val TYPING_INTERVAL: Long = 30000
        const val COUNTDOWN_TIMER: Long = 45000
        const val PRODUCT_OFFERING_ID = "productOfferingId"
        const val ACCOUNT_NUMBER = "accountNumber"
        const val STATUS_UNKNOWN = 0
        const val STATUS_ONLINE = 1
        const val STATUS_WAIT = 2
        const val STATUS_CLOSED = 3
        const val STATUS_CLEARED = 4
        const val STATUS_PNET_CLOSED = 5
        const val STATUS_DISCONNECT = 6
        const val STATUS_CLOSED_FORCED = 7
        const val STATUS_CLOSED_TIMEOUT = 8
        const val STATUS_RELOCATED = 9
    }

    fun actionBar() {
        setSupportActionBar(mToolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
            setHomeAsUpIndicator(R.drawable.back24)
        }
    }

    fun updateMessageList(message: ChatMessage) {
        runOnUiThread {
            adapter?.let {
                it.addMessage(message)
                reyclerview_message_list.scrollToPosition(it.itemCount - 1)
            }
        }
    }

    fun userStoppedTyping() {
        if (isAgentOnline) {
            chatId?.let {
                OneAppService.userStoppedTyping(it).enqueue(CompletionHandler(object : RequestListener<UserTypingResponse> {
                    override fun onSuccess(response: UserTypingResponse?) {
                    }

                    override fun onFailure(error: Throwable?) {
                    }

                }, UserTypingResponse::class.java))
            }
        }

    }

    fun userStartedTyping() {
        if (isAgentOnline) {
            chatId?.let {
                OneAppService.userTyping(it).enqueue(CompletionHandler(object : RequestListener<UserTypingResponse> {
                    override fun onSuccess(response: UserTypingResponse?) {
                    }

                    override fun onFailure(error: Throwable?) {
                    }

                }, UserTypingResponse::class.java))
            }
        }
    }

    fun stopAgentAvailablePolling() {
        disposablesAgentsAvailable?.clear()
        cancelPollingTimer()
    }

    fun stopChatSessionStatePolling() {
        disposablesChatSessionState?.clear()
        cancelPollingTimer()
    }

    fun startPollingTimer() {
        pollingTimer?.start()
    }

    private fun cancelPollingTimer() {
        pollingTimer?.cancel()
    }

    private fun stopAllOngoingPolling() {
        disposablesAgentsAvailable?.clear()
        disposablesChatSessionState?.clear()
    }

    fun showAgentsMessage(agentDefaultMessage: AgentDefaultMessage, moreMessage: String = "") {
        updateMessageList(ChatMessage(ChatMessage.Type.RECEIVED, agentDefaultMessage.message + moreMessage))
    }

    fun showErrorMessage() {
        stopAllOngoingPolling()
        showAgentsMessage(AgentDefaultMessage.GENERAL_ERROR)
        cancelPollingTimer()
        clearSessionValues()
    }

    fun showSessionEndedMessage() {
        showAgentsMessage(AgentDefaultMessage.CHAT_ENDED_WITH_ANY_REASON)
        stopChatSessionStatePolling()
        clearSessionValues()
    }

    override fun onTimerFinished() {
        if (chatId.isNullOrEmpty() || !isAgentOnline) {
            showAgentsMessage(AgentDefaultMessage.NO_AGENTS)
            if (chatId.isNullOrEmpty()) stopAgentAvailablePolling() else if (!isAgentOnline) stopChatSessionStatePolling()
        }
    }

    private fun clearSessionValues() {
        isAgentOnline = false
        chatId = null
        setEndSessionAvailable(false)
        setPageTitle(getString(R.string.chat_activity_title))
    }

    fun setEndSessionAvailable(isAvailable: Boolean) {
        endSession.visibility = if (isAvailable) View.VISIBLE else View.GONE
    }

    fun setPageTitle(name: String?) {
        if (name.isNullOrEmpty())
            agentName.text = name
    }

}