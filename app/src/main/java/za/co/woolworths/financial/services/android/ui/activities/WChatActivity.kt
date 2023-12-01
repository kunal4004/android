package za.co.woolworths.financial.services.android.ui.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.viewModels
import androidx.annotation.IntegerRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ChatActivityBinding
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import za.co.woolworths.financial.services.android.contracts.IDialogListener
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SessionType
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.doAfterDelay
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatAWSAmplify
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.WhatsAppChatToUsVisibility.Companion.APP_SCREEN
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.WhatsAppChatToUsVisibility.Companion.CHAT_TO_COLLECTION_AGENT
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.WhatsAppChatToUsVisibility.Companion.CHAT_TYPE
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.WhatsAppChatToUsVisibility.Companion.FEATURE_NAME
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.WhatsAppChatToUsVisibility.Companion.FEATURE_WHATSAPP
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.helper.LiveChatService
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.model.SendMessageResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatFloatingActionButtonBubbleView.Companion.LIVE_CHAT_NO_INTERNET_RESULT
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatFloatingActionButtonBubbleView.Companion.LIVE_CHAT_PACKAGE
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatFloatingActionButtonBubbleView.Companion.LIVE_CHAT_SUBSCRIPTION_RESULT
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatOfflineFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatRetrieveABSACardTokenFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.WhatsAppChatToUsFragment
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

@AndroidEntryPoint
class WChatActivity : AppCompatActivity(), IDialogListener, View.OnClickListener {

    lateinit var binding: ChatActivityBinding
    private var mSubscribeToMessageReceiver: BroadcastReceiver? = null
    private var isChatToCollectionAgent: Boolean = false
    private var sessionType: SessionType? = null
    private var chatNavHostController: NavController? = null
    private var chatToCollectionAgent: Boolean = false
    private var chatScreenType: ChatType = ChatType.DEFAULT
    private var appScreen: String? = null
    val bundle = Bundle()
    var shouldDismissChatNavigationModel = false
    var shouldAnimateChatMessage = true

    enum class ChatType { AGENT_COLLECT, WHATSAPP_ONBOARDING, DEFAULT }

    private val chatViewModel: ChatViewModel by viewModels()

    companion object {
        const val DELAY: Long = 300
        const val EXTRA_VOC_TRIGGER_EVENT = "extraVocTriggerEvent"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ChatActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Utils.updateStatusBarBackground(this)
        getArguments()
        actionBar()
        initUI()

        with(chatViewModel) {
            isCustomerSignOut.observe(this@WChatActivity) { isSignOut ->
                when (topVisibleFragment) {
                    is ChatFragment -> {
                        when (isSignOut) {
                            true ->
                                GlobalScope.doAfterDelay(DELAY) {
                                    liveChatAuthentication.signOut {
                                        ServiceTools.stop(
                                            this@WChatActivity,
                                            LiveChatService::class.java
                                        )
                                        closeChatWindow()
                                    }
                                }
                            else -> {
                                // Nothing
                            }
                        }
                    }
                    is ChatRetrieveABSACardTokenFragment -> {
                        when (isSignOut) {
                            true -> GlobalScope.doAfterDelay(DELAY) { closeChatWindow() }
                            else -> {
                                // Nothing
                            }
                        }
                    }
                }
            }
        }

        mSubscribeToMessageReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                val extras = intent.extras ?: return
                val result: String? = extras.getString(LIVE_CHAT_SUBSCRIPTION_RESULT)
                val noInternet: String? = extras.getString(LIVE_CHAT_NO_INTERNET_RESULT, "")
                // display toast when network is unavailable
                if (!TextUtils.isEmpty(noInternet)) {
                    ErrorHandlerView(this@WChatActivity).showToast()
                    return
                }
                val sendMessage: SendMessageResponse? =
                    Gson().fromJson(result, SendMessageResponse::class.java)
                when (topVisibleFragment) {
                    is ChatFragment -> {
                        (topVisibleFragment as? ChatFragment)?.apply {
                            if (sendMessage == null) {
                                subscribeErrorResponse()
                            } else {
                                subscribeResult(sendMessage)
                            }
                        }
                    }
                }
            }
        }

        mSubscribeToMessageReceiver?.let { receiver ->
            registerReceiver(receiver, IntentFilter(LIVE_CHAT_PACKAGE))
        }
    }

    private fun getArguments() {
        chatViewModel.initAmplify()
        intent?.extras?.apply {
            appScreen = getString(APP_SCREEN)
            sessionType = getSerializable(ChatFragment.SESSION_TYPE) as? SessionType
            isChatToCollectionAgent = getBoolean(CHAT_TO_COLLECTION_AGENT, false)
            chatScreenType = getSerializable(CHAT_TYPE) as? ChatType ?: ChatType.DEFAULT
        }



        chatViewModel.setScreenType()
        chatViewModel.triggerFirebaseOnlineOfflineChatEvent(this)
        chatViewModel.postChatEventInitiateSession()
    }

    private fun initUI() {
        val chatNavHost =
            supportFragmentManager.findFragmentById(R.id.chatNavHost) as? NavHostFragment
        chatNavHostController = chatNavHost?.navController
        val chatNavGraph = chatNavHostController?.graph
        // add featureName app string
        with(bundle) {
            putBoolean(CHAT_TO_COLLECTION_AGENT, chatToCollectionAgent)
            putString(FEATURE_NAME, FEATURE_WHATSAPP)
            putString(APP_SCREEN, appScreen)
        }
        when (chatScreenType) {
            ChatType.AGENT_COLLECT -> {
                chatScreenType = ChatType.AGENT_COLLECT
                if (chatViewModel.isOperatingHoursForInAppChat()) {
                    chatNavGraph?.startDestination = R.id.chatFragment
                } else {
                    bundle.putString(FEATURE_NAME, FEATURE_WHATSAPP)
                    bundle.putString(APP_SCREEN, appScreen)
                    chatNavGraph?.startDestination = R.id.chatToCollectionAgentOfflineFragment
                }
            }
            ChatType.WHATSAPP_ONBOARDING -> {
                chatScreenType = ChatType.WHATSAPP_ONBOARDING
                bundle.putString(FEATURE_NAME, FEATURE_WHATSAPP)
                bundle.putString(APP_SCREEN, appScreen)
                chatNavGraph?.startDestination = R.id.chatToUsWhatsAppFragment
            }
            ChatType.DEFAULT -> {
                chatScreenType = ChatType.AGENT_COLLECT
                bundle.putString(FEATURE_NAME, FEATURE_WHATSAPP)
                bundle.putString(APP_SCREEN, appScreen)
                chatNavGraph?.startDestination = R.id.chatFragment
            }
        }
        chatNavGraph?.let { graph -> chatNavHostController?.setGraph(graph, bundle) }

        binding.endSessionTextView?.apply {
            setOnClickListener(this@WChatActivity)
            AnimationUtilExtension.animateViewPushDown(this)
        }
    }

    override fun onResume() {
        super.onResume()
        //  when user closes chat to go to WhatsApp or Email, dismiss Chat Navigation Modal and then jump out of the app
        ChatAWSAmplify.isChatActivityInForeground = true
        if (chatScreenType != ChatType.WHATSAPP_ONBOARDING && shouldDismissChatNavigationModel) {
            finish()
            overridePendingTransition(0, 0)
        }
    }

    fun actionBar() {
        setSupportActionBar(binding.mToolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
            setHomeAsUpIndicator(R.drawable.back24)
        }
    }

    fun displayEndSessionButton(isOnline: Boolean) {
        runOnUiThread {
            binding.endSessionTextView?.visibility = if (isOnline) VISIBLE else GONE
        }
    }


    fun chatDisconnectedByAgent(isDisconnected: Boolean) {
        binding.endSessionTextView?.apply {
            visibility = VISIBLE
            alpha = if (isDisconnected) 0.3f else 1.0f
            isEnabled = !isDisconnected
        }
    }


    override fun onBackPressed() {
        when (topVisibleFragment) {
            is WhatsAppChatToUsFragment -> chatNavHostController?.popBackStack()

            is ChatOfflineFragment -> closeChatWindow()

            is ChatRetrieveABSACardTokenFragment -> endSessionPopup()

            else -> {
                when (chatNavHostController?.graph?.startDestination) {

                    R.id.chatFragment -> closeChatWindow()

                    else -> chatNavHostController?.popBackStack()
                }
            }
        }
    }

    private fun endSessionPopup() {
        val navigateResId = when (topVisibleFragment) {
            is ChatFragment -> R.id.action_chatFragment_to_chatCustomerServiceEndSessionDialogFragment
            is ChatRetrieveABSACardTokenFragment -> R.id.action_chatRetrieveABSACardTokenFragment_to_chatCustomerServiceEndSessionDialogFragment
            else -> return
        }
        chatNavHostController?.navigate(navigateResId)
    }

    val topVisibleFragment: Fragment?
        get() = (supportFragmentManager.fragments.first()
                as? NavHostFragment)?.childFragmentManager?.findFragmentById(R.id.chatNavHost)

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(menuItem)
    }

    private fun closeChatWindow() {
        finish()
        overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.endSessionTextView -> endSessionPopup()
        }
    }

    fun setStartDestination(startDestination: Int) {
        val chatNavGraph = chatNavHostController?.graph
        chatNavGraph?.startDestination = startDestination
        chatNavGraph?.let { chatNavHostController?.setGraph(it, bundle) }
    }

    override fun onDestroy() {
        super.onDestroy()
        ChatAWSAmplify.isChatActivityInForeground = false
        mSubscribeToMessageReceiver?.let { unregisterReceiver(it) }
    }

    fun updateToolbarTitle(@IntegerRes title: Int?) {
        binding.agentNameTextView?.text = title?.let { name -> bindString(name) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppConstant.RESULT_OK_HUAWEI_REQUEST_CODE) {
            huaweiRatingsWindowResult(resultCode)
        }
    }
}