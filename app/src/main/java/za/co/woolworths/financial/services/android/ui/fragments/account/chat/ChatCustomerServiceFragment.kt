package za.co.woolworths.financial.services.android.ui.fragments.account.chat


import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.chat_fragment.*
import za.co.woolworths.financial.services.android.contracts.IDialogListener
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.ChatMessage
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SessionStateType
import za.co.woolworths.financial.services.android.ui.activities.WChatActivity
import za.co.woolworths.financial.services.android.ui.adapters.WChatAdapter
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.WhatsAppChatToUsVisibility.Companion.APP_SCREEN
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import za.co.woolworths.financial.services.android.util.SessionUtilities
import java.net.ConnectException

class ChatCustomerServiceFragment : ChatCustomerServiceExtensionFragment(), IDialogListener, View.OnClickListener {

    private var chatNavController: NavController? = null
    private var appScreen: String? = ChatCustomerServiceFragment::class.java.simpleName

    private val chatViewModel: ChatCustomerServiceViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        arguments?.apply {
            appScreen = getString(APP_SCREEN, ChatCustomerServiceFragment::class.java.simpleName)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.chat_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatNavController = (activity?.supportFragmentManager?.findFragmentById(R.id.chatNavHost) as? NavHostFragment)?.navController

        with(chatViewModel) {
            initAmplify()
            isCustomerSignOut.observe(viewLifecycleOwner) { shouldSignOut ->
                when (shouldSignOut) {
                    true -> {
                        signOut({ closeChat() }, {
                            closeChat()
                            showAgentsMessage(AgentDefaultMessage.GENERAL_ERROR)
                        })
                    }

                }
            }
        }

        initView()
    }

    private fun closeChat() {
        activity?.apply {
            finish()
            overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
        }
    }

    private fun initView() {
        setupRecyclerview()
        inputListener()
        isChatButtonEnabled(false)
        onClickListener()
        setAgentAvailableState(chatViewModel.isOperatingHoursForInAppChat() ?: false)
    }

    private fun onClickListener() {
        button_send?.setOnClickListener(this)
    }

    private fun getUserTokenAndSignIn() {
        chatLoaderProgressBar?.visibility = VISIBLE
        with(chatViewModel) {
            val absaCardToken = getAmplify()?.getABSACardToken()
            if (absaCardToken.isNullOrEmpty()) {
                getCreditCardToken({ result ->
                    when (result?.httpCode) {
                        200 -> {
                            val cards = result.cards
                            if (cards.isNullOrEmpty()) {
                                chatNavController?.navigate(R.id.retryErrorFragment)
                            } else {
                                chatViewModel.absaCardToken.value = cards
                                amplifyListener()
                            }
                        }
                        440 -> activity?.let { SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, result.response.stsParams, it) }

                        else -> {
                            chatNavController?.navigate(R.id.retryErrorFragment)
                        }
                    }
                }, { error ->
                    chatLoaderProgressBar?.visibility = GONE
                    when (error) {
                        is ConnectException -> {
                            activity?.let { ErrorHandlerView(it).showToast() }
                        }
                        else -> {
                            showAgentsMessage(AgentDefaultMessage.GENERAL_ERROR)
                        }
                    }
                })
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
                                isChatButtonEnabled(true)
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
                    showAgentsMessage(AgentDefaultMessage.GENERAL_ERROR)
                })
            }, {
                chatLoaderProgressBar?.visibility = GONE
                showAgentsMessage(AgentDefaultMessage.GENERAL_ERROR)
            })
        }
    }

    private fun inputListener() {
        edittext_chatbox?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                chatViewModel.userStoppedTyping()
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                chatViewModel.userStartedTyping()
            }
        })
    }

    private fun isChatButtonEnabled(isEnabled: Boolean) {
        button_send?.isEnabled = isEnabled
    }

    private fun isUserOnline(visible: Boolean) {
        (activity as? WChatActivity)?.setChatState(visible)
        edittext_chatbox?.isEnabled = visible
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
                    showAgentsMessage(if (this) "Hi " + chatViewModel.getAmplify()?.getCustomerUsername() + ". How can I help you today?" else "You have reached us outside of our business hours. Please contact us between " + chatViewModel.getInAppTradingHoursForToday()?.opens + " and " + chatViewModel.getInAppTradingHoursForToday()?.closes + ".")
                }
            }
        }
    }
}
