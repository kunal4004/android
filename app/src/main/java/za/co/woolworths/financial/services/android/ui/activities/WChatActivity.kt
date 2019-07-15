package za.co.woolworths.financial.services.android.ui.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import com.awfs.coordination.R
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import za.co.woolworths.financial.services.android.util.Utils
import kotlinx.android.synthetic.main.chat_activity.*
import za.co.woolworths.financial.services.android.contracts.RequestListener
import za.co.woolworths.financial.services.android.models.JWTDecodedModel
import za.co.woolworths.financial.services.android.models.dto.ChatMessage
import za.co.woolworths.financial.services.android.models.dto.chat.AgentsAvailableResponse
import za.co.woolworths.financial.services.android.models.dto.chat.CreateChatSession
import za.co.woolworths.financial.services.android.models.dto.chat.CreateChatSessionResponse
import za.co.woolworths.financial.services.android.models.dto.chat.PollChatSessionStateResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.adapters.WChatAdapter
import za.co.woolworths.financial.services.android.ui.extension.afterTextChanged
import za.co.woolworths.financial.services.android.ui.extension.onAction
import za.co.woolworths.financial.services.android.util.SessionUtilities
import java.util.concurrent.TimeUnit


class WChatActivity : AppCompatActivity() {

    private var adapter: WChatAdapter? = null
    var productOfferingId: String? = null
    var accountNumber: String? = null
    var disposablesAgentsAvailable: CompositeDisposable? = null
    var disposablesChatSessionState: CompositeDisposable? = null
    var chatId: String? = null


    companion object {
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
        button_send.setOnClickListener { checkAgentAvailable() }
        edittext_chatbox.afterTextChanged { button_send.isEnabled = it.isNotEmpty() }
        edittext_chatbox.onAction(EditorInfo.IME_ACTION_DONE) { sendMessage() }

    }

    override fun onBackPressed() {
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

    private fun sendMessage() {
        if (edittext_chatbox.text.isNotEmpty()) {
            val message = edittext_chatbox.text.toString().trim()
            updateMessageList(ChatMessage(if (message.contains("R:")) ChatMessage.Type.RECEIVED else ChatMessage.Type.SENT, if (message.contains("R:")) message.replace("R:", "") else message))
            edittext_chatbox.text.clear()
        }
    }

    private fun checkAgentAvailable() {
        disposablesAgentsAvailable = CompositeDisposable()
        disposablesAgentsAvailable?.add(Observable.interval(0, 5, TimeUnit.SECONDS)
                .flatMap { OneAppService.pollAgentsAvailable().takeUntil(Observable.timer(5, TimeUnit.SECONDS)) }
                .observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe(this::handleAgentAvailableSuccessResponse, this::handleErrorResponses))
    }

    private fun createChatSession() {
        val requestBody = SessionUtilities.getInstance().jwt?.let { CreateChatSession(it.C2Id, it.name?.get(0), it.family_name?.get(0), it.email?.get(0), productOfferingId, accountNumber, accountNumber) }
        requestBody?.let {
            OneAppService.createChatSession(it).enqueue(CompletionHandler(object : RequestListener<CreateChatSessionResponse> {
                override fun onSuccess(response: CreateChatSessionResponse?) {
                    when (response?.httpCode) {
                        200 -> response.chatId?.let {
                            chatId = it
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
            disposablesChatSessionState?.add(Observable.interval(0, 10, TimeUnit.SECONDS)
                    .flatMap { OneAppService.pollChatSessionState(chatId).takeUntil(Observable.timer(5, TimeUnit.SECONDS)) }
                    .observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe(this::handleChatSessionStateSuccessResponse, this::handleErrorResponses))

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
                    when(it.chatState?.state){
                        STATUS_ONLINE->{

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
        disposablesAgentsAvailable?.clear()
    }
}