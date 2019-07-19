package za.co.woolworths.financial.services.android.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
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
import za.co.woolworths.financial.services.android.models.dto.chat.*
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.adapters.WChatAdapter
import za.co.woolworths.financial.services.android.ui.extension.afterTextChanged
import za.co.woolworths.financial.services.android.ui.extension.afterTypingStateChanged
import za.co.woolworths.financial.services.android.ui.extension.onAction
import za.co.woolworths.financial.services.android.ui.views.actionsheet.GotITDialogFragment
import za.co.woolworths.financial.services.android.util.SessionUtilities
import java.util.concurrent.TimeUnit


class WChatActivity : AppCompatActivity(), IDialogListener {

    private var adapter: WChatAdapter? = null
    var productOfferingId: String? = null
    var accountNumber: String? = null
    private var disposablesAgentsAvailable: CompositeDisposable? = null
    private var disposablesChatSessionState: CompositeDisposable? = null
    var chatId: String? = null
    var usersOfflineMessage: ChatMessage = ChatMessage(ChatMessage.Type.SENT, "")
    var isAgentOnline: Boolean = false


    companion object {
        private const val POLLING_INTERVAL_AGENT_AVAILABLE: Long = 5  //seconds
        private const val POLLING_INTERVAL_CHAT_SESSION_STATE: Long = 5 //seconds
        private const val TYPING_INTERVAL: Long = 30000
        private const val PRODUCT_OFFERING_ID = "productOfferingId"
        private const val ACCOUNT_NUMBER = "accountNumber"
        private const val STATUS_UNKNOWN = 0
        private const val STATUS_ONLINE = 1
        private const val STATUS_WAIT = 2
        private const val STATUS_CLOSED = 3
        private const val STATUS_CLEARED = 4
        private const val STATUS_PNET_CLOSED = 5
        private const val STATUS_DISCONNECT = 6
        private const val STATUS_CLOSED_FORCED = 7
        private const val STATUS_CLOSED_TIMEOUT = 8
        private const val STATUS_RELOCATED = 9
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_activity)
        Utils.updateStatusBarBackground(this)
        actionBar()
        if (savedInstanceState == null)
            getBundleArgument()
        initViews()
    }

    private fun actionBar() {
        setSupportActionBar(mToolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
            setHomeAsUpIndicator(R.drawable.back24)
        }
    }

    private fun getBundleArgument() {
        intent?.extras?.apply {
            productOfferingId = getString(PRODUCT_OFFERING_ID)
            accountNumber = getString(ACCOUNT_NUMBER)
        }
    }

    fun initViews() {
        reyclerview_message_list.layoutManager = LinearLayoutManager(this)
        adapter = WChatAdapter()
        reyclerview_message_list.adapter = adapter
        button_send.setOnClickListener { onSendMessage() }
        edittext_chatbox.afterTextChanged { onEditTextValueChanged(it) }
        edittext_chatbox.onAction(EditorInfo.IME_ACTION_DONE) { onSendMessage() }
        edittext_chatbox.afterTypingStateChanged(TYPING_INTERVAL) { if (it) userStartedTyping() else userStoppedTyping() }
        endSession.setOnClickListener { confirmToEndChatSession() }
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
                onBackPressed()
            }
        }
        return false
    }

    private fun updateMessageList(message: ChatMessage) {
        runOnUiThread {
            adapter?.let {
                it.addMessage(message)
                reyclerview_message_list.scrollToPosition(it.itemCount - 1)
            }
        }
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
                    }

                }

                override fun onFailure(error: Throwable?) {
                }

            }, CreateChatSessionResponse::class.java))
        }
    }

    private fun pollChatSessionState(chatId: String?) {
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
                        createChatSession()
                    }
                }
                else -> {

                }
            }
        }

    }

    private fun handleErrorResponses(error: Throwable) {

    }

    private fun handleChatSessionStateSuccessResponse(result: PollChatSessionStateResponse?) {
        result?.let {
            when (it.httpCode) {
                200 -> {
                    it.chatState?.let { chatState ->
                        when (chatState.state) {
                            STATUS_ONLINE -> {

                                isAgentOnline = true

                                // Update received message with UI
                                if (chatState.text?.isNotEmpty()!!)
                                    updateUIWithReceivedMessages(chatState.text)

                                //Send offline messages
                                usersOfflineMessage.let { it ->
                                    if (!it.isMessageSent && it.message.isNotEmpty())
                                        sendMessage(it)
                                }
                            }
                        }
                    }
                }
                else -> {

                }
            }
        }

    }

    private fun stopAgentAvailablePolling() {
        disposablesAgentsAvailable?.clear()
    }

    private fun stopChatSessionStatePolling() {
        disposablesChatSessionState?.clear()
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

    private fun userStoppedTyping() {
        if (isAgentOnline)
            chatId?.let { OneAppService.userStoppedTyping(it) }

    }

    private fun userStartedTyping() {
        if (isAgentOnline)
            chatId?.let { OneAppService.userTyping(it) }
    }

}