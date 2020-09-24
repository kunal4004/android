package za.co.woolworths.financial.services.android.ui.fragments.account.chat


import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SessionStateType
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.chat_fragment.*
import za.co.woolworths.financial.services.android.contracts.IDialogListener
import za.co.woolworths.financial.services.android.models.dto.ChatMessage
import za.co.woolworths.financial.services.android.ui.activities.WChatActivity
import za.co.woolworths.financial.services.android.ui.adapters.WChatAdapter
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.WhatsAppChatToUsVisibility.Companion.APP_SCREEN
import za.co.woolworths.financial.services.android.util.Utils
import java.lang.Exception


class ChatCustomerServiceFragment : ChatCustomerServiceExtensionFragment(), IDialogListener, View.OnClickListener {

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
        chatViewModel.initAmplify()
        chatViewModel.isCustomerSignOut.observe(viewLifecycleOwner) { shouldSignOut ->
            when (shouldSignOut) {
                true -> {
                    chatViewModel.signOut({
                        activity?.apply {
                            finish()
                            overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
                        }
                    }, {
                        showAgentsMessage(AgentDefaultMessage.GENERAL_ERROR)
                    })
                }
            }
        }

        initView()
    }

    private fun initView() {
        setupRecyclerview()
        inputListener()
        enableChatButton(false)
        onClickListener()
        setAgentAvailableState(Utils.isOperatingHoursForInAppChat())
    }

    private fun onClickListener() {
        button_send?.setOnClickListener(this)
    }

    private fun amplifyListener() {
        with(chatViewModel) {
            signIn({
                subscribeToMessageByConversationId({ result ->
                    activity?.runOnUiThread {
                        when (result?.sessionState) {

                            SessionStateType.CONNECT -> enableChatButton(false)

                            SessionStateType.ONLINE -> {
                                if (!result.content.startsWith("Hello"))
                                    showAgentsMessage(result.content)
                                enableChatButton(true)
                            }

                            SessionStateType.DISCONNECT -> enableChatButton(false)

                            else -> {
                            }
                        }
                    }
                }, { showAgentsMessage(AgentDefaultMessage.GENERAL_ERROR) })
            }, { showAgentsMessage(AgentDefaultMessage.GENERAL_ERROR) })
        }
    }

    private fun inputListener() {
        edittext_chatbox?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                chatViewModel.userStoppedTyping()
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                chatViewModel.userStartedTyping()
            }
        })
    }

    private fun enableChatButton(isEnabled: Boolean) {
        (activity as? WChatActivity)?.setChatState(isEnabled)
        edittext_chatbox?.isEnabled = isEnabled
        button_send?.isEnabled = isEnabled
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
                        true -> {
                            showAgentsMessage("Hey ${chatViewModel.getAmplify()?.getCustomerUsername()}")
                            showAgentsMessage(bindString(R.string.chat_greeting_description))
                            amplifyListener()
                        }
                        else -> {
                            val bundle = Bundle()
                            bundle.putString(WhatsAppChatToUsVisibility.FEATURE_NAME, WhatsAppChatToUsVisibility.FEATURE_WHATSAPP)
                            bundle.putString(APP_SCREEN, appScreen)
                            val chatNavHost = supportFragmentManager.findFragmentById(R.id.chatNavHost) as? NavHostFragment
                            val chatNavHostController = chatNavHost?.navController
                            chatNavHostController?.navigate(R.id.chatToCollectionAgentOfflineFragment, bundle)
                        }
                    }
                }
            }
            false -> {
                isOnline.apply {
                    activity?.offlineBanner?.visibility = if (this) View.GONE else View.VISIBLE
                    if (!this) edittext_chatbox?.text?.clear()
                    showAgentsMessage(if (this) "Hi " + chatViewModel.getAmplify()?.getCustomerUsername() + ". How can I help you today?" else "You have reached us outside of our business hours. Please contact us between " + getInAppTradingHoursForToday().opens + " and " + getInAppTradingHoursForToday().closes + ".")
                }
            }
        }
    }
}
