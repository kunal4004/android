package za.co.woolworths.financial.services.android.ui.fragments.account.chat

import android.app.Activity
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.chat_fragment.*
import kotlinx.coroutines.GlobalScope
import za.co.woolworths.financial.services.android.contracts.IDialogListener
import za.co.woolworths.financial.services.android.models.dto.ChatMessage
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SessionStateType
import za.co.woolworths.financial.services.android.ui.activities.WChatActivity
import za.co.woolworths.financial.services.android.ui.adapters.WChatAdapter
import za.co.woolworths.financial.services.android.ui.extension.doAfterDelay
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.WhatsAppChatToUsVisibility.Companion.APP_SCREEN
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.keyboard.SoftKeyboardObserver

class ChatFragment : ChatExtensionFragment(), IDialogListener, View.OnClickListener {

    private var chatNavController: NavController? = null
    private var appScreen: String? = ChatFragment::class.java.simpleName

    private val chatViewModel: ChatViewModel by activityViewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            appScreen = getString(APP_SCREEN, ChatFragment::class.java.simpleName)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.chat_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? WChatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        chatNavController = (activity?.supportFragmentManager?.findFragmentById(R.id.chatNavHost) as? NavHostFragment)?.navController

        initView()
    }

    private fun initView() {
        setupRecyclerview()
        isChatButtonEnabled(false)
        onClickListener()
        autoConnectToNetwork()
        setAgentAvailableState(chatViewModel.isOperatingHoursForInAppChat())
        detectKeyboardVisibilityState()
    }

    private fun detectKeyboardVisibilityState() {
        activity?.let { activity ->
            SoftKeyboardObserver(activity)
                    .listen { isKeyboardVisible ->
                        if (isKeyboardVisible) {
                            mChatAdapter?.itemCount?.minus(1)?.let { messageListRecyclerView?.scrollToPosition(it) }
                        }
                    }
        }
    }

    private fun onClickListener() {
        button_send?.setOnClickListener(this)
    }

    private fun getUserTokenAndSignIn() {
        with(chatViewModel) {
            val absaCardToken = getABSACardToken()
            if (absaCardToken.isNullOrEmpty()) {
                // show retrieve ABSA card token retry screen
                chatNavController?.navigate(R.id.chatRetrieveABSACardTokenFragment)
            } else {
                amplifyListener()
            }
        }
    }

    private fun amplifyListener() {
        chatLoaderProgressBar?.visibility = VISIBLE
        with(chatViewModel) {
            signIn({
                subscribeToMessageByConversationId({ result ->

                    activity?.runOnUiThread {
                        when (result?.sessionState) {

                            SessionStateType.CONNECT -> {
                                chatLoaderProgressBar?.visibility = GONE
                                showAgentsMessage(result.content)
                                isChatButtonEnabled(false)
                                isUserOnline(true)
                            }

                            SessionStateType.ONLINE -> {
                                chatLoaderProgressBar?.visibility = GONE
                                showAgentsMessage(result.content)
                                isChatButtonEnabled(true)
                                isUserOnline(true)
                            }

                            SessionStateType.QUEUEING -> {
                                chatLoaderProgressBar?.visibility = GONE
                                showAgentsMessage(result.content)
                                isChatButtonEnabled(false)
                                isUserOnline(true)
                            }

                            SessionStateType.DISCONNECT -> {
                                chatLoaderProgressBar?.visibility = GONE
                                showAgentsMessage(result.content)
                                isChatButtonEnabled(false)
                                isUserOnline(true)
                            }

                            else -> {
                                chatLoaderProgressBar?.visibility = GONE
                            }
                        }
                    }
                }, {
                    chatLoaderProgressBar?.visibility = GONE
                })
            }, {
                GlobalScope.doAfterDelay(AppConstant.DELAY_100_MS) {
                    val serviceUnavailable = chatViewModel.getServiceUnavailableMessage()
                    showAgentsMessage(serviceUnavailable.second, serviceUnavailable.first)
                    chatLoaderProgressBar?.visibility = GONE
                }
            })
        }
    }

    private fun isChatButtonEnabled(isEnabled: Boolean) {
        button_send?.isEnabled = isEnabled
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
            R.id.button_send -> {
                if (NetworkManager.getInstance().isConnectedToNetwork(activity)) {
                    val message = edittext_chatbox?.text?.toString() ?: ""
                    if (TextUtils.isEmpty(message)) return
                    val chatMessage = ChatMessage(ChatMessage.Type.SENT, message)
                    updateMessageList(chatMessage)
                    chatViewModel.setSessionStateType(SessionStateType.ONLINE)
                    chatViewModel.sendMessage(message)
                    edittext_chatbox?.setText("")
                    try {
                        val imm: InputMethodManager? = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager
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
                    bundle.putString(WhatsAppChatToUsVisibility.FEATURE_NAME, WhatsAppChatToUsVisibility.FEATURE_WHATSAPP)
                    bundle.putString(APP_SCREEN, appScreen)
                    chatNavController?.navigate(R.id.chatToCollectionAgentOfflineFragment, bundle)
                }
            }
        }
    }

    private fun autoConnectToNetwork() {
        activity?.let { activity ->
            ConnectionBroadcastReceiver.registerToFragmentAndAutoUnregister(activity, this, object : ConnectionBroadcastReceiver() {
                override fun onConnectionChanged(hasConnection: Boolean) {
                    if (hasConnection) {
                        chatViewModel.getMessagesListByConversation { messagesByConversationList ->
                            if (messagesByConversationList?.size ?: 0 > 0) {
                                val messagesListFromAdapter = mChatAdapter?.getMessageList()

                                /**
                                 * filter messageByConversation List and list of message from adapter, and remove duplicates
                                 * groupBy creates a Map with a key as defined in the Lambda (id in this case), and a List of the items
                                 */

                                val updatedList: List<ChatMessage>? = messagesByConversationList?.let { messagesListFromAdapter?.plus(it)?.groupBy { item -> item.message } }?.entries?.map { it.value }?.flatten()?.distinctBy { it.message }

                                if (updatedList?.size ?: 0 > 0) {
                                    mChatAdapter?.clear()
                                    updatedList?.forEach { chat -> updateMessageList(chat) }
                                }
                            }
                        }
                    }
                }
            })
        }
    }
}
