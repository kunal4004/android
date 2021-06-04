package za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.chat_activity.*
import kotlinx.android.synthetic.main.chat_fragment.*
import kotlinx.coroutines.GlobalScope
import za.co.woolworths.financial.services.android.contracts.IDialogListener
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SessionStateType
import za.co.woolworths.financial.services.android.ui.activities.WChatActivity
import za.co.woolworths.financial.services.android.ui.adapters.WChatAdapter
import za.co.woolworths.financial.services.android.ui.extension.doAfterDelay
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.WhatsAppChatToUsVisibility.Companion.APP_SCREEN
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.WhatsAppChatToUsVisibility.Companion.FEATURE_NAME
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.WhatsAppChatToUsVisibility.Companion.FEATURE_WHATSAPP
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.helper.LiveChatService
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.model.ChatMessage
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.model.SendMessageResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.model.SenderMessage
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.request.LiveChatSendMessageImpl
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.keyboard.SoftKeyboardObserver

class ChatFragment : Fragment(), IDialogListener, View.OnClickListener {

    companion object {
        const val ACCOUNTS: String = "accounts"
        const val CARD: String = "CARD"
        const val SESSION_TYPE = "SESSION_TYPE"
    }

    var mChatAdapter: WChatAdapter? = null
    var productOfferingId: String? = null
    var accountNumber: String? = null
    private var chatNavController: NavController? = null
    private var appScreen: String? = ChatFragment::class.java.simpleName
    private var sendMessageImpl = LiveChatSendMessageImpl()
    var isConnectedToNetwork = true

    private val chatViewModel: ChatViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            appScreen = getString(APP_SCREEN, ChatFragment::class.java.simpleName)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.chat_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? WChatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        chatNavController =
            (activity?.supportFragmentManager?.findFragmentById(R.id.chatNavHost) as? NavHostFragment)?.navController
        initView()
    }

    private fun onChatStart() {
        with(chatViewModel) {
            if (!isChatServiceRunning(activity)) {
                activity?.let { act -> ServiceTools.start(act, LiveChatService::class.java) }
                return@with
            }

            listAllMessages()
        }
    }

    private fun ChatViewModel.listAllMessages() {
        liveChatListAllAgentConversation.messageListFromAgent({ chatList ->
            mChatAdapter?.clear()
            chatList.first?.forEach { item ->
                val content = when (item) {
                    is SenderMessage -> item.message
                    is SendMessageResponse -> item.content
                }
                if (!TextUtils.isEmpty(content))
                    showMessage(item)
            }
            activity?.runOnUiThread {
                if (isAdded) {
                    chatLoaderProgressBar?.visibility = GONE
                    subscribeResult(chatList.second, false)
                }
            }
        }, {
            chatLoaderProgressBar?.visibility = GONE
        })
    }

    private fun initView() {
        configureRecyclerview()
        toggleSendMessageButton(false)
        onClickListener()
        autoConnectToNetwork()
        setAgentAvailableState(chatViewModel.isOperatingHoursForInAppChat())
        keyboardVisibilityState()
    }

    private fun keyboardVisibilityState() {
        activity?.let { activity ->
            SoftKeyboardObserver(activity)
                .listen { isKeyboardVisible ->
                    if (isKeyboardVisible) {
                        mChatAdapter?.itemCount?.minus(1)
                            ?.let { messageListRecyclerView?.scrollToPosition(it) }
                    }
                }
        }
    }

    private fun onClickListener() {
        sendMessageButton?.setOnClickListener(this)
    }

    private fun getUserTokenAndSignIn() {
        with(chatViewModel) {
            val absaCardToken = liveChatDBRepository.getABSACardToken()
            if (absaCardToken?.isEmpty() == true) {
                // show retrieve ABSA card token retry screen
                chatNavController?.navigate(R.id.chatRetrieveABSACardTokenFragment)
            } else {
                amplifyListener()
            }
        }
    }

    @SuppressLint("ResourceType")
    private fun amplifyListener() {
        (activity as? WChatActivity)?.updateToolbarTitle(R.string.chat_title)
        chatLoaderProgressBar?.visibility = VISIBLE
        onChatStart()
    }

    fun subscribeResult(result: SendMessageResponse?, isAgentMessageVisible: Boolean = true) {
        activity?.runOnUiThread {
            // do not allow null messages
            if (result?.content?.isEmpty() == true && result.sessionState == SessionStateType.ONLINE) return@runOnUiThread

            chatLoaderProgressBar?.visibility = GONE

            if (isAgentMessageVisible)
                result?.let { showMessage(it) }

            when (result?.sessionState) {
                SessionStateType.CONNECT,
                SessionStateType.QUEUEING -> {
                    toggleSendMessageButton(false)
                    chatBoxEditText?.isEnabled = false
                    displayEndSessionButton(true)
                }
                SessionStateType.DISCONNECT -> {
                    ServiceTools.stop(activity, LiveChatService::class.java)
                    chatBoxEditText?.isEnabled = false
                    toggleSendMessageButton(false)
                    displayEndSessionButton(false)
                    isAgentDisconnected(true)
                }
                SessionStateType.ONLINE -> {
                    chatBoxEditText?.isEnabled = true
                    isAgentDisconnected(false)
                    toggleSendMessageButton(true)
                    displayEndSessionButton(true)
                }

                else -> {
                    chatLoaderProgressBar?.visibility = GONE
                }
            }
        }
    }

    private fun toggleSendMessageButton(isEnabled: Boolean) {
        sendMessageButton?.isEnabled = isEnabled
    }

    private fun displayEndSessionButton(state: Boolean) {
        (activity as? WChatActivity)?.displayEndSessionButton(state)
    }

    private fun isAgentDisconnected(isDisconnected: Boolean) {
        (activity as? WChatActivity)?.chatDisconnectedByAgent(isDisconnected)
    }

    private fun configureRecyclerview() {
        mChatAdapter = WChatAdapter()
        messageListRecyclerView?.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = mChatAdapter
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.sendMessageButton -> {
                if (NetworkManager.getInstance().isConnectedToNetwork(activity)) {
                    val message = chatBoxEditText?.text?.toString() ?: ""
                    if (TextUtils.isEmpty(message)) return
                    showMessage(SenderMessage(message))
                    sendMessageImpl.send(SessionStateType.ONLINE, message)
                    chatBoxEditText?.setText("")
                    try {
                        val imm: InputMethodManager? =
                            activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager
                        imm?.hideSoftInputFromWindow(view?.windowToken, 0)
                    } catch (ex: Exception) {
                        FirebaseManager.logException(ex)
                    }
                } else {
                    ErrorHandlerView(activity).showToast()
                }
            }
        }
    }

    private fun setAgentAvailableState(isOnline: Boolean) {
        activity?.apply {
            when (isOnline) {
                true -> if (chatViewModel.isCreditCardAccount()) getUserTokenAndSignIn() else amplifyListener()
                else -> {
                    val bundle = Bundle()
                    bundle.putString(
                        FEATURE_NAME,
                        FEATURE_WHATSAPP
                    )
                    bundle.putString(APP_SCREEN, appScreen)
                    chatNavController?.navigate(R.id.chatToCollectionAgentOfflineFragment, bundle)
                }
            }
        }
    }

    private fun autoConnectToNetwork() {
        activity?.let { activity ->
            ConnectionBroadcastReceiver.registerToFragmentAndAutoUnregister(
                activity,
                this,
                object : ConnectionBroadcastReceiver() {
                    override fun onConnectionChanged(hasConnection: Boolean) {
                        if (hasConnection && !isConnectedToNetwork) {
                            isConnectedToNetwork = true
                            with(chatViewModel) {
                                liveChatListAllAgentConversation.messageListFromAgent({ messagesByConversation ->
                                    mChatAdapter?.clear()
                                    messagesByConversation.first?.forEach { item ->
                                        showMessage(item)
                                    }
                                    activity.runOnUiThread {
                                        if (isAdded) {
                                            chatLoaderProgressBar?.visibility = GONE
                                            subscribeResult(messagesByConversation.second, false)
                                        }
                                    }
                                }, {
                                    chatLoaderProgressBar?.visibility = GONE
                                })
                            }
                        } else {
                            isConnectedToNetwork = false
                        }

                    }
                })
        }
    }

    fun subscribeErrorResponse() {
        GlobalScope.doAfterDelay(AppConstant.DELAY_100_MS) {
            activity ?: return@doAfterDelay
            ServiceTools.stop(activity, LiveChatService::class.java)
            chatLoaderProgressBar?.visibility = GONE
        }
    }

    private fun showMessage(message: ChatMessage) {
        activity?.runOnUiThread {
            mChatAdapter?.let {
                val content = when (message) {
                    is SenderMessage -> message.message
                    is SendMessageResponse -> message.content
                }
                if (!TextUtils.isEmpty(content)) {
                    it.addMessage(message)
                    messageListRecyclerView?.scrollToPosition(it.itemCount - 1)
                }
            }
        }
    }
}