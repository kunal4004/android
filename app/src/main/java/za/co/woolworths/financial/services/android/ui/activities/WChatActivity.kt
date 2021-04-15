package za.co.woolworths.financial.services.android.ui.activities

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.awfs.coordination.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.chat_activity.*
import kotlinx.coroutines.GlobalScope
import za.co.woolworths.financial.services.android.contracts.IDialogListener
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SessionType
import za.co.woolworths.financial.services.android.ui.extension.doAfterDelay
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.*
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatExtensionFragment.Companion.ACCOUNTS
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatExtensionFragment.Companion.ACCOUNT_NUMBER
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatExtensionFragment.Companion.FROM_ACTIVITY
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatExtensionFragment.Companion.PRODUCT_OFFERING_ID
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.WhatsAppChatToUsVisibility.Companion.APP_SCREEN
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.WhatsAppChatToUsVisibility.Companion.CHAT_TO_COLLECTION_AGENT
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.WhatsAppChatToUsVisibility.Companion.CHAT_TYPE
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.WhatsAppChatToUsVisibility.Companion.FEATURE_NAME
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.WhatsAppChatToUsVisibility.Companion.FEATURE_WHATSAPP
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class WChatActivity : AppCompatActivity(), IDialogListener, View.OnClickListener {

    private var fromActivity: String? = null
    private var sessionType: SessionType? = null
    private var chatAccountProductLandingPage: String? = null
    private var chatNavHostController: NavController? = null
    private var chatToCollectionAgent: Boolean = false
    private var accountNumber: String? = null
    private var productOfferingId: String? = null
    private var chatScreenType: ChatType = ChatType.DEFAULT
    private var appScreen: String? = null
    val bundle = Bundle()
    var shouldDismissChatNavigationModel = false
    var shouldAnimateChatMessage = true
    enum class ChatType { AGENT_COLLECT, WHATSAPP_ONBOARDING, DEFAULT }

    private val chatViewModel: ChatViewModel by viewModels()

    companion object {
        const val DELAY: Long = 300
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_activity)
        Utils.updateStatusBarBackground(this)
        getArguments()
        actionBar()
        initUI()

        with(chatViewModel) {
            isCustomerSignOut.observe(this@WChatActivity, { isSignOut ->
                when (currentFragment) {
                    is ChatFragment -> {
                        when (isSignOut) {
                            true -> signOut { GlobalScope.doAfterDelay(DELAY) { closeChat() } }
                        }
                    }
                    is ChatRetrieveABSACardTokenFragment -> {
                        when (isSignOut) {
                            true -> GlobalScope.doAfterDelay(DELAY) { closeChat() }
                        }
                    }
                }
            })
        }
    }

    private fun getArguments() {
        chatViewModel.initAmplify()
        intent?.extras?.apply {
            productOfferingId = getString(PRODUCT_OFFERING_ID)
            accountNumber = getString(ACCOUNT_NUMBER)
            appScreen = getString(APP_SCREEN)
            fromActivity = getString(FROM_ACTIVITY)
            sessionType = getSerializable(ChatExtensionFragment.SESSION_TYPE) as? SessionType
            chatAccountProductLandingPage = getString(ACCOUNTS)
            chatViewModel.isChatToCollectionAgent.value = getBoolean(CHAT_TO_COLLECTION_AGENT, false)
            chatViewModel.setSessionType(sessionType ?: SessionType.Collections)
            chatScreenType = getSerializable(CHAT_TYPE) as? ChatType ?: ChatType.DEFAULT
        }

        chatViewModel.setScreenType(fromActivity)
        chatViewModel.setAccount(Gson().fromJson(chatAccountProductLandingPage, Account::class.java))
        chatViewModel.triggerFirebaseOnlineOfflineChatEvent()
        chatViewModel.postChatEventInitiateSession()
    }

    private fun initUI() {
        val chatNavHost = supportFragmentManager.findFragmentById(R.id.chatNavHost) as? NavHostFragment
        chatNavHostController = chatNavHost?.navController
        val chatNavGraph = chatNavHostController?.graph
        // add featureName app string
        with(bundle) {
            putString(PRODUCT_OFFERING_ID, productOfferingId)
            putString(ACCOUNT_NUMBER, accountNumber)
            putBoolean(CHAT_TO_COLLECTION_AGENT, chatToCollectionAgent)
            putString(FEATURE_NAME, FEATURE_WHATSAPP)
            putString(APP_SCREEN, appScreen)
        }

        chatScreenType = ChatType.AGENT_COLLECT
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

        endSessionTextView?.apply {
            setOnClickListener(this@WChatActivity)
            AnimationUtilExtension.animateViewPushDown(this)
        }
    }

    override fun onResume() {
        super.onResume()
        //  when user closes chat to go to WhatsApp or Email, dismiss Chat Navigation Modal and then jump out of the app
        if (chatScreenType != ChatType.WHATSAPP_ONBOARDING && shouldDismissChatNavigationModel) {
            finish()
            overridePendingTransition(0, 0)
        }
    }

    fun actionBar() {
        setSupportActionBar(mToolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
            setHomeAsUpIndicator(R.drawable.back24)
        }
    }

    fun setChatState(isOnline: Boolean) {
        runOnUiThread {
            setEndSessionTextView(isOnline)
        }
    }

    private fun setEndSessionTextView(isOnline: Boolean) {
        endSessionTextView?.visibility = if (isOnline) VISIBLE else GONE
    }

    override fun onBackPressed() {
        when (currentFragment) {
            is WhatsAppChatToUsFragment -> chatNavHostController?.popBackStack()

            is ChatOfflineFragment -> {
                finish()
                overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
            }

            is ChatRetrieveABSACardTokenFragment -> endSessionPopup()

            else -> {
                when (chatNavHostController?.graph?.startDestination) {

                    R.id.chatFragment -> endSessionPopup()

                    else -> chatNavHostController?.popBackStack()
                }
            }
        }
    }

    private fun endSessionPopup() {
        val navigationId = when (currentFragment) {
            is ChatFragment -> R.id.action_chatFragment_to_chatCustomerServiceEndSessionDialogFragment
            is ChatRetrieveABSACardTokenFragment -> R.id.action_chatRetrieveABSACardTokenFragment_to_chatCustomerServiceEndSessionDialogFragment
            else -> return
        }
        chatNavHostController?.navigate(navigationId)
    }

    val currentFragment: Fragment?
        get() = (supportFragmentManager.fragments.first() as? NavHostFragment)?.childFragmentManager?.findFragmentById(R.id.chatNavHost)

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(menuItem)
    }

    private fun closeChat() {
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
}