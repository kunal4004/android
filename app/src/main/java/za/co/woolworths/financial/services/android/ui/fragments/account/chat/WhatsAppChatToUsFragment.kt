package za.co.woolworths.financial.services.android.ui.fragments.account.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.chat_activity.*
import kotlinx.android.synthetic.main.chat_to_us_via_whatsapp_fragment.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.ui.activities.WChatActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class WhatsAppChatToUsFragment : Fragment(), View.OnClickListener {

    private var appScreen: String? = null
    private var featureName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.apply {
            featureName = getString(WhatsAppChatToUsVisibility.FEATURE_NAME, "")
            appScreen = getString(WhatsAppChatToUsVisibility.APP_SCREEN, "")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.chat_to_us_via_whatsapp_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? WChatActivity)?.setChatState(false)

        (activity as? AppCompatActivity)?.apply {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            agentNameTextView?.text = bindString(R.string.whatsapp_chat_to_us_title)
        }

        with(WhatsAppChatToUsVisibility()) {
            whatsappNumberValueTextView?.text = whatsAppNumber
        }

        chatWithUsButton?.apply {
            AnimationUtilExtension.animateViewPushDown(this)
            setOnClickListener(this@WhatsAppChatToUsFragment)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.chatWithUsButton -> {
                if (NetworkManager.getInstance().isConnectedToNetwork(activity)) {
                    (activity as? WChatActivity)?.shouldDismissChatNavigationModel = true
                    Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.WHATSAPP_CHAT_WITH_US)
                    KotlinUtils.postOneAppEvent(appScreen ?: "", featureName
                            ?: OneAppEvents.FeatureName.WHATSAPP)
                    Utils.openBrowserWithUrl(WhatsAppChatToUsVisibility().whatsAppChatWithUsUrlBreakout)
                } else {
                    ErrorHandlerView(activity).showToast()
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> activity?.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
}