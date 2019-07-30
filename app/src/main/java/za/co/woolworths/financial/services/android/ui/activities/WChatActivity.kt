package za.co.woolworths.financial.services.android.ui.activities

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import za.co.woolworths.financial.services.android.util.Utils
import kotlinx.android.synthetic.main.chat_activity.*
import za.co.woolworths.financial.services.android.contracts.IDialogListener
import za.co.woolworths.financial.services.android.contracts.RequestListener
import za.co.woolworths.financial.services.android.models.dto.ChatMessage
import za.co.woolworths.financial.services.android.models.dto.ChatState
import za.co.woolworths.financial.services.android.models.dto.chat.*
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.adapters.WChatAdapter
import za.co.woolworths.financial.services.android.ui.extension.afterTextChanged
import za.co.woolworths.financial.services.android.ui.extension.afterTypingStateChanged
import za.co.woolworths.financial.services.android.ui.extension.onAction
import za.co.woolworths.financial.services.android.ui.views.actionsheet.GotITDialogFragment
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.WCountDownTimer
import java.util.concurrent.TimeUnit


class WChatActivity : WChatActivityExtension(), IDialogListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_activity)
        Utils.updateStatusBarBackground(this)
        actionBar()
        if (savedInstanceState == null)
            getBundleArgument()
        initViewsAndListener()
    }


    private fun getBundleArgument() {
        intent?.extras?.apply {
            productOfferingId = getString(PRODUCT_OFFERING_ID)
            accountNumber = getString(ACCOUNT_NUMBER)
        }
    }

    private fun initViewsAndListener() {
        pollingTimer = WCountDownTimer(COUNTDOWN_TIMER, 1000, this)
        reyclerview_message_list.layoutManager = LinearLayoutManager(this)
        adapter = WChatAdapter()
        reyclerview_message_list.adapter = adapter
        button_send.setOnClickListener { onSendMessage() }
        edittext_chatbox.apply {
            afterTextChanged { onEditTextValueChanged(it) }
            onAction(EditorInfo.IME_ACTION_DONE) { onSendMessage() }
            afterTypingStateChanged(TYPING_INTERVAL) { if (it) userStartedTyping() else userStoppedTyping() }
        }
        endSession.setOnClickListener { confirmToEndChatSession() }
        setAgentAvailableState(Utils.isOperatingHoursForInAppChat())
        if (Utils.isOperatingHoursForInAppChat())
            checkAgentAvailable()

    }

    private fun onEditTextValueChanged(text: String) {
        button_send.isEnabled = text.isNotEmpty()

    }

    override fun onBackPressed() {
        stopAllPolling()
        finish()
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId) {
            android.R.id.home -> {
                if (isAgentOnline) confirmToEndChatSession() else onBackPressed()
            }
        }
        return false
    }


    private fun onSendMessage() {
        if (edittext_chatbox.text.isNotEmpty()) {
            val userMessage = edittext_chatbox.text.toString().trim()
            val chatMessage = ChatMessage(ChatMessage.Type.SENT, userMessage)
            updateMessageList(chatMessage)
            edittext_chatbox.text.clear()

            if (isAgentOnline)
                sendMessage(chatMessage)
            else {
                //update offline message
                usersOfflineMessage.let {
                    if (!it.isMessageSent)
                        it.message = if (it.message.isNotEmpty()) "${it.message},$userMessage" else userMessage
                }
            }

        }
    }

    private fun checkAgentAvailable() {
        startPollingTimer()
        disposablesAgentsAvailable = CompositeDisposable()
        disposablesAgentsAvailable?.add(Observable.interval(0, POLLING_INTERVAL_AGENT_AVAILABLE, TimeUnit.SECONDS)
                .flatMap { OneAppService.pollAgentsAvailable().takeUntil(Observable.timer(5, TimeUnit.SECONDS)) }
                .observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe(this::handleAgentAvailableSuccessResponse, this::handleErrorResponses))
    }

    private fun createChatSession() {
        val requestBody = SessionUtilities.getInstance().jwt?.let { CreateChatSession(it.C2Id, it.name?.get(0), it.family_name?.get(0), it.email?.get(0), productOfferingId, accountNumber, accountNumber) }
        requestBody?.let {
            OneAppService.createChatSession(it).enqueue(CompletionHandler(object : RequestListener<CreateChatSessionResponse> {
                override fun onSuccess(response: CreateChatSessionResponse?) {
                    when (response?.httpCode) {
                        200 -> response.chatId?.let { id ->
                            chatId = id
                            pollChatSessionState(chatId)
                        }
                        else -> showErrorMessage()
                    }

                }

                override fun onFailure(error: Throwable?) {
                    showErrorMessage()
                }

            }, CreateChatSessionResponse::class.java))
        }
    }

    private fun pollChatSessionState(chatId: String?) {
        startPollingTimer()
        chatId?.let {
            disposablesChatSessionState = CompositeDisposable()
            disposablesChatSessionState?.add(Observable.interval(0, POLLING_INTERVAL_CHAT_SESSION_STATE, TimeUnit.SECONDS)
                    .flatMap { OneAppService.pollChatSessionState(chatId).takeUntil(Observable.timer(5, TimeUnit.SECONDS)) }
                    .observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe(this::handleChatSessionStateSuccessResponse, this::handleErrorResponses))

        }
    }

    private fun sendMessage(chatMessage: ChatMessage) {
        chatId?.let {
            OneAppService.sendChatMessage(it, SendMessageRequestBody(chatMessage.message)).enqueue(CompletionHandler(object : RequestListener<SendChatMessageResponse> {
                override fun onSuccess(response: SendChatMessageResponse?) {
                    when (response?.httpCode) {
                        200 -> chatMessage.isMessageSent = true
                        else -> sendMessage(chatMessage)
                    }

                }

                override fun onFailure(error: Throwable?) {
                    showErrorMessage()
                }

            }, SendChatMessageResponse::class.java))
        }

    }

    private fun handleAgentAvailableSuccessResponse(result: AgentsAvailableResponse?) {
        result?.let {
            when (it.httpCode) {
                200 -> {
                    if (it.agentsAvailable) {
                        stopAgentAvailablePolling()
                        showAgentsMessage(AgentDefaultMessage.CONNECTING_AGENT)
                        createChatSession()
                    }
                }
                else -> {
                    showErrorMessage()
                }
            }
        }

    }

    private fun handleErrorResponses(error: Throwable) {
        showErrorMessage()
    }

    private fun handleChatSessionStateSuccessResponse(result: PollChatSessionStateResponse?) {
        result?.let { response ->
            when (response.httpCode) {
                200 -> {
                    response.chatState?.let {
                        when (it.state) {
                            STATUS_ONLINE -> {
                                handleAgentOnlineState(it)
                            }
                            STATUS_WAIT -> {
                            }
                            else -> showSessionEndedMessage()
                        }
                    }
                }
                else -> {
                    showErrorMessage()
                }
            }
        }

    }

    private fun updateUIWithReceivedMessages(messageList: List<String>?) {
        messageList?.forEach {
            this.updateMessageList(ChatMessage(ChatMessage.Type.RECEIVED, it))
        }
    }

    private fun stopAllPolling() {
        stopAgentAvailablePolling()
        stopChatSessionStatePolling()
    }

    private fun confirmToEndChatSession() {
        val openDialogFragment =
                GotITDialogFragment.newInstance(getString(R.string.chat_end_session_dialog_title),
                        getString(R.string.chat_end_session_dialog_desc), getString(R.string.chat_end_session_dialog_cancel_text),
                        this, getString(R.string.chat_end_session_dialog_action_text), true, R.drawable.ic_end_session)
        openDialogFragment.show(this.supportFragmentManager, GotITDialogFragment::class.java.simpleName)
    }

    private fun endChatSession() {
        chatId?.let { OneAppService.endChatSession(it) }
        onBackPressed()
    }

    override fun onDialogDismissed() {
    }

    override fun onDialogButtonAction() {
        endChatSession()
    }


    private fun setIsChatEditTextEditable(isEditable: Boolean) {
        edittext_chatbox.isEnabled = isEditable
    }

    private fun handleAgentOnlineState(chatState: ChatState) {
        if (!isAgentOnline) {
            isAgentOnline = true
            setEndSessionAvailable(isAgentOnline)
            chatState.agentNickName?.let { name ->
                setPageTitleWithAgentName(name)
                showAgentsMessage(AgentDefaultMessage.AGENT_PICKED, name)
            }
        }
        // Update received message with UI
        if (chatState.text?.isNotEmpty()!!)
            updateUIWithReceivedMessages(chatState.text)

        //Send offline messages
        usersOfflineMessage.let { it ->
            if (!it.isMessageSent && it.message.isNotEmpty())
                sendMessage(it)
        }
    }

    private fun setAgentAvailableState(isOnline: Boolean) {
        isOnline.apply {
            setIsChatEditTextEditable(this)
            chatState.let {
                it.isEnabled = isOnline
                it.text = if (isOnline) getString(R.string.chat_state_online) else getString(R.string.chat_state_offline)
            }
            offlineBanner.visibility = if (this) View.GONE else View.VISIBLE
            setEndSessionAvailable(this)
            if (!this) edittext_chatbox.text.clear()
            showAgentsMessage(if (this) AgentDefaultMessage.AGENT_ONLINE else AgentDefaultMessage.AGENT_OFFLINE)
        }
    }

    private fun setEndSessionAvailable(isAvailable: Boolean) {
        endSession.visibility = if (isAvailable) View.VISIBLE else View.GONE
    }

    private fun setPageTitleWithAgentName(name: String?) {
        agentName.text = name
    }


}