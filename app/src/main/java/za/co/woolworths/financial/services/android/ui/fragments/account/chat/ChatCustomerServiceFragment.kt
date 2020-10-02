package za.co.woolworths.financial.services.android.ui.fragments.account.chat


import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.chat_fragment.*
import za.co.woolworths.financial.services.android.contracts.IDialogListener
import za.co.woolworths.financial.services.android.models.dto.ChatMessage
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SessionStateType
import za.co.woolworths.financial.services.android.ui.activities.WChatActivity
import za.co.woolworths.financial.services.android.ui.adapters.WChatAdapter
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.WhatsAppChatToUsVisibility.Companion.APP_SCREEN

class ChatCustomerServiceFragment : ChatCustomerServiceExtensionFragment(), IDialogListener, View.OnClickListener {

    private var chatNavController: NavController? = null
    private var appScreen: String? = ChatCustomerServiceFragment::class.java.simpleName

    private val chatViewModel: ChatCustomerServiceViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            appScreen = getString(APP_SCREEN, ChatCustomerServiceFragment::class.java.simpleName)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.chat_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? WChatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        chatNavController = (activity?.supportFragmentManager?.findFragmentById(R.id.chatNavHost) as? NavHostFragment)?.navController

        chatViewModel.initAmplify()

        initView()
    }


    private fun initView() {
        setupRecyclerview()
        isChatButtonEnabled(false)
        onClickListener()
        setAgentAvailableState(chatViewModel.isOperatingHoursForInAppChat() ?: false)
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
                    Log.e("subscribeToMessage", Gson().toJson(result))
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
                chatLoaderProgressBar?.visibility = GONE
                showAgentsMessage(AgentDefaultMessage.GENERAL_ERROR)
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
                val message = edittext_chatbox?.text?.toString() ?: ""
                val chatMessage = ChatMessage(ChatMessage.Type.SENT, message)
                updateMessageList(chatMessage)
                chatViewModel.setSessionStateType(SessionStateType.ONLINE)
                chatViewModel.sendMessage(message)
                edittext_chatbox?.setText("")
                try {
                    val imm: InputMethodManager? = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager
                    imm?.hideSoftInputFromWindow(view?.windowToken, 0)
                } catch (ex: Exception) {
                }
            }
        }
    }

    private fun setAgentAvailableState(isOnline: Boolean) {
        when (chatViewModel.isChatToCollectionAgent.value) {
            true -> {
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
            false -> {
                isOnline.apply {
                    activity?.offlineBanner?.visibility = if (this) GONE else VISIBLE
                    if (!this) edittext_chatbox?.text?.clear()
                    showAgentsMessage(if (this) "Hi " + chatViewModel.getCustomerUsername() + ". How can I help you today?" else "You have reached us outside of our business hours. Please contact us between " + chatViewModel.getInAppTradingHoursForToday()?.opens + " and " + chatViewModel.getInAppTradingHoursForToday()?.closes + ".")
                }
            }
        }
    }
}
