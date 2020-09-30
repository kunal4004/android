package za.co.woolworths.financial.services.android.ui.activities

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
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
import za.co.woolworths.financial.services.android.contracts.IDialogListener
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SessionType
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatCustomerServiceExtensionFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatCustomerServiceExtensionFragment.Companion.ACCOUNTS
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatCustomerServiceExtensionFragment.Companion.ACCOUNT_NUMBER
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatCustomerServiceExtensionFragment.Companion.PRODUCT_OFFERING_ID
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatCustomerServiceOfflineFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatCustomerServiceViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.WhatsAppChatToUsFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.WhatsAppChatToUsVisibility.Companion.APP_SCREEN
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.WhatsAppChatToUsVisibility.Companion.CHAT_TO_COLLECTION_AGENT
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.WhatsAppChatToUsVisibility.Companion.CHAT_TYPE
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.WhatsAppChatToUsVisibility.Companion.FEATURE_NAME
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.WhatsAppChatToUsVisibility.Companion.FEATURE_WHATSAPP
import za.co.woolworths.financial.services.android.util.Utils

class WChatActivity : AppCompatActivity(), IDialogListener {

    private var sessionType: SessionType? = null
    private var chatAccountProductLandingPage: String? = null
    private var chatNavHostController: NavController? = null
    private var chatToCollectionAgent: Boolean = false
    private var accountNumber: String? = null
    private var productOfferingId: String? = null
    private var chatScreenType: ChatType = ChatType.DEFAULT
    var shouldDismissChatNavigationModel = false
    var shouldAnimateChatMessage = true
    var appScreen: String? = null

    enum class ChatType { AGENT_COLLECT, WHATSAPP_ONBOARDING, DEFAULT }

    private val chatViewModel: ChatCustomerServiceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_activity)
        Utils.updateStatusBarBackground(this)
        actionBar()

        intent?.extras?.apply {
            productOfferingId = getString(PRODUCT_OFFERING_ID)
            accountNumber = getString(ACCOUNT_NUMBER)
            appScreen = getString(APP_SCREEN)
            sessionType = getSerializable(ChatCustomerServiceExtensionFragment.SESSION_TYPE) as? SessionType
            chatAccountProductLandingPage = getString(ACCOUNTS)
            chatViewModel.isChatToCollectionAgent.value = getBoolean(CHAT_TO_COLLECTION_AGENT, false)
            chatViewModel.setSessionType(sessionType ?: SessionType.Collections)
            chatScreenType = getSerializable(CHAT_TYPE) as? ChatType ?: ChatType.DEFAULT
        }
        chatViewModel.setAccount(Gson().fromJson(chatAccountProductLandingPage, Account::class.java))
        initUI()
    }

    private fun initUI() {
        val chatNavHost = supportFragmentManager.findFragmentById(R.id.chatNavHost) as? NavHostFragment
        chatNavHostController = chatNavHost?.navController
        val chatNavGraph = chatNavHostController?.graph
        val bundle = Bundle()
        // add featureName app string
        bundle.putString(PRODUCT_OFFERING_ID, productOfferingId)
        bundle.putString(ACCOUNT_NUMBER, accountNumber)
        bundle.putBoolean(CHAT_TO_COLLECTION_AGENT, chatToCollectionAgent)
        when (chatScreenType) {
            ChatType.AGENT_COLLECT -> {
                chatScreenType = ChatType.AGENT_COLLECT
                if (chatViewModel.isOperatingHoursForInAppChat() == true) {
                    chatNavGraph?.startDestination = R.id.chatFragment
                } else {
                    bundle.putString(FEATURE_NAME, FEATURE_WHATSAPP)
                    bundle.putString(APP_SCREEN, appScreen)
                    Context.BIND_DEBUG_UNBIND
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

        endSessionTextView?.setOnClickListener { endSessionPopup() }
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

            is ChatCustomerServiceOfflineFragment -> {
                finish()
                overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
            }
            else -> {
                when (chatNavHostController?.graph?.startDestination) {

                    R.id.chatFragment -> {
                        endSessionPopup()
                    }
                    else -> chatNavHostController?.popBackStack()
                }
            }
        }
    }

    private fun endSessionPopup() {
        chatNavHostController?.navigate(R.id.action_chatFragment_to_chatCustomerServiceEndSessionDialogFragment)
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

}