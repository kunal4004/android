package za.co.woolworths.financial.services.android.ui.activities

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.chat_activity.*
import za.co.woolworths.financial.services.android.contracts.RequestListener
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.ChatMessage
import za.co.woolworths.financial.services.android.models.dto.chat.TradingHours
import za.co.woolworths.financial.services.android.models.dto.chat.UserTypingResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.adapters.WChatAdapter
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils
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
    var sendMessageRetryCounter = 0

    enum class AgentDefaultMessage(val message: String) {
        AGENT_ONLINE("Hi " + SessionUtilities.getInstance().jwt?.name?.get(0) + ". How can I help you today?"),
        AGENT_OFFLINE("You have reached us outside of our business hours. Please contact us between " + getInAppTradingHoursForToday().opens + " and " + getInAppTradingHoursForToday().closes + "."),
        GENERAL_ERROR("We’re currently experiencing technical issues, please try again later or call us on 0861 50 20 20."),
        AGENT_PICKED(" will be assisting you further, enjoy the rest of your day."),
        CONNECTING_AGENT("Please stay online for the next available consultant."),
        CHAT_ENDED_WITH_ANY_REASON("Your chat session has ended."),
        NO_AGENTS("There are no consultants available at the moment, please try again later" +
                " or call us on 0861 50 20 20.")
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
        const val MAX_RETRY_FOR_SEND_MESSAGE = 3

         fun getInAppTradingHoursForToday(): TradingHours {

            var tradingHoursForToday: TradingHours? = null
            WoolworthsApplication.getPresenceInAppChat()?.tradingHours?.let {
                it.forEach { tradingHours ->
                    if (tradingHours.day.equals(Utils.getCurrentDay(), true)) {
                        tradingHoursForToday = tradingHours
                        return tradingHours
                    }
                }
            }

            return tradingHoursForToday ?: TradingHours("sunday", "00:00", "00:00")
        }

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
        updateMessageList(ChatMessage(ChatMessage.Type.RECEIVED, moreMessage + agentDefaultMessage.message))
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
            if (chatId.isNullOrEmpty()) stopAgentAvailablePolling()
            else
                if (!isAgentOnline) {
                    clearChatId()
                    stopChatSessionStatePolling()
                }
        }
    }

    private fun clearSessionValues() {
        isAgentOnline = false
        clearChatId()
        setEndSessionAvailable(false)
        setPageTitle(getString(R.string.chat_activity_title))
    }

    fun setEndSessionAvailable(isAvailable: Boolean) {
        endSession.visibility = if (isAvailable) View.VISIBLE else View.GONE
    }

    fun setPageTitle(name: String?) {
        if (!name.isNullOrEmpty())
            agentName.text = name
    }

    fun clearChatId(){
        chatId = null
    }

}