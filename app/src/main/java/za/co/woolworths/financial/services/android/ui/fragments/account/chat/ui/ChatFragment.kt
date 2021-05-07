package za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui

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
import kotlinx.android.synthetic.main.chat_fragment.*
import kotlinx.coroutines.GlobalScope
import za.co.woolworths.financial.services.android.contracts.IDialogListener
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SessionStateType
import za.co.woolworths.financial.services.android.ui.activities.WChatActivity
import za.co.woolworths.financial.services.android.ui.adapters.WChatAdapter
import za.co.woolworths.financial.services.android.ui.extension.doAfterDelay
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatAWSAmplify
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.WhatsAppChatToUsVisibility.Companion.APP_SCREEN
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.WhatsAppChatToUsVisibility.Companion.FEATURE_NAME
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.WhatsAppChatToUsVisibility.Companion.FEATURE_WHATSAPP
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.helper.LiveChatFollowMeService
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.model.ChatMessage
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.model.SendEmailIntentInfo
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.model.SendMessageResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.model.UserMessage
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
                activity?.let { act ->
                    ServiceTools.start(
                        act,
                        LiveChatFollowMeService::class.java
                    )
                }
                return@with
            }

            liveChatListAllAgentConversation.list({ chatList ->
                chatList?.forEach { item -> updateMessageList(item) }
                chatLoaderProgressBar?.visibility = GONE
            }, { exp ->
                Log.e("apiException", "apiException $exp")
            })
        }
    }

    private fun initView() {
        setupRecyclerview()
        isChatButtonEnabled(false)
        onClickListener()
//        autoConnectToNetwork()
        setAgentAvailableState(chatViewModel.isOperatingHoursForInAppChat())
        detectKeyboardVisibilityState()
    }

    private fun detectKeyboardVisibilityState() {
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
            if (absaCardToken.isEmpty()) {
                // show retrieve ABSA card token retry screen
                chatNavController?.navigate(R.id.chatRetrieveABSACardTokenFragment)
            } else {
                amplifyListener()
            }
        }
    }

    private fun amplifyListener() {
        chatLoaderProgressBar?.visibility = VISIBLE
        onChatStart()
    }

    fun subscribeResult(result: SendMessageResponse?) {
        activity?.runOnUiThread {
            when (result?.sessionState) {

                SessionStateType.CONNECT -> {
                    chatLoaderProgressBar?.visibility = GONE
                    showAgentsMessage(result)
                    isChatButtonEnabled(false)
                    isUserOnline(true)
                }

                SessionStateType.ONLINE -> {
                    chatLoaderProgressBar?.visibility = GONE
                    showAgentsMessage(result)
                    isChatButtonEnabled(true)
                    isUserOnline(true)
                    ChatAWSAmplify.addChatMessageToList(result)
                }

                SessionStateType.QUEUEING -> {
                    chatLoaderProgressBar?.visibility = GONE
                    showAgentsMessage(result)
                    isChatButtonEnabled(false)
                    isUserOnline(true)
                    ChatAWSAmplify.addChatMessageToList(result)
                }

                SessionStateType.DISCONNECT -> {
                    chatLoaderProgressBar?.visibility = GONE
                    showAgentsMessage(result)
                    isChatButtonEnabled(false)
                    isUserOnline(true)
                    ChatAWSAmplify.addChatMessageToList(result)
                }

                else -> {
                    chatLoaderProgressBar?.visibility = GONE
                }
            }
        }
    }

    private fun isChatButtonEnabled(isEnabled: Boolean) {
        sendMessageButton?.isEnabled = isEnabled
    }

    private fun isUserOnline(isVisible: Boolean) {
        (activity as? WChatActivity)?.setChatState(isVisible)
        edittext_chatbox?.isEnabled = isVisible
    }

    private fun setupRecyclerview() {
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
                    val message = edittext_chatbox?.text?.toString() ?: ""
                    if (TextUtils.isEmpty(message)) return
                    updateMessageList(UserMessage(message))
                    sendMessageImpl.send(SessionStateType.ONLINE, message)
                    edittext_chatbox?.setText("")
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
                        if (hasConnection) {
                            with(chatViewModel) {
                                liveChatListAllAgentConversation.list({ messagesByConversation ->

                                }, { apiException ->

                                })
                            }

                            // ----

                        }
                    }
                })
        }
    }

    fun subscribeErrorResponse() {
//        GlobalScope.doAfterDelay(AppConstant.DELAY_100_MS) {
//            val serviceUnavailable = chatViewModel.getServiceUnavailableMessage()
//            showAgentsMessage(serviceUnavailable.second, serviceUnavailable.first)
//            chatLoaderProgressBar?.visibility = GONE
//        }
    }

    private fun updateMessageList(message: ChatMessage) {
        activity?.runOnUiThread {
            mChatAdapter?.let {
                it.addMessage(message)
                messageListRecyclerView?.scrollToPosition(it.itemCount - 1)
            }
        }
    }

    private fun showAgentsMessage(agentDefaultMessage: ChatMessage) {
        updateMessageList(agentDefaultMessage)
    }

    fun showAgentsMessage(agentDefaultMessage: String, emailIntentInfo: SendEmailIntentInfo) {
        updateMessageList(
            SendMessageResponse(
                content = agentDefaultMessage,
                sendEmailIntentInfo = emailIntentInfo
            )
        )
    }
}