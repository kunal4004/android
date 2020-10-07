package za.co.woolworths.financial.services.android.ui.fragments.account.chat

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.*
import android.view.View.VISIBLE
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.chat_to_collection_agent_offline_fragment.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.WChatActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.whatsapp.WhatsAppChatToUs.Companion.APP_SCREEN
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.whatsapp.WhatsAppChatToUs.Companion.FEATURE_NAME
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.navOptions
import za.co.woolworths.financial.services.android.ui.extension.request
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.NetworkManager
import za.co.woolworths.financial.services.android.util.Utils
import java.text.SimpleDateFormat
import java.util.*


class ChatOfflineFragment : Fragment() {

    private var featureName: String? = null
    private var appScreen: String? = null
    private val chatViewModel: ChatViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            featureName = getString(FEATURE_NAME, "")
            appScreen = getString(APP_SCREEN, "")
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.chat_to_collection_agent_offline_fragment, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? WChatActivity)?.apply {
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            setChatState(false)
        }

        val chatCollectionsAgent = ChatBubbleVisibility()
        hiClientTextView?.text = "Hi ${chatCollectionsAgent.getUsername()},"

        chatCollectionDescriptionTextView?.text = chatViewModel.offlineMessageTemplate { result ->
            KotlinUtils.sendEmail(activity, result.first, result.second, result.third)
            (activity as? WChatActivity)?.shouldDismissChatNavigationModel = true
        }

        val currentTime: String? = SimpleDateFormat("hh : mm a", Locale.getDefault()).format(Calendar.getInstance().time)
        timeTextView?.text = currentTime

        val tapHereToChatWithWhatsApp = bindString(R.string.chat_tap_here_label)
        val spanTapHereLabel = SpannableString(tapHereToChatWithWhatsApp)
        spanTapHereLabel.setSpan(tapWhatsAppTemplate, 0, spanTapHereLabel.indexOf("Tap here") + "Tap here".length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        chatWithWhatsAppTextView?.text = spanTapHereLabel

        hiClientTextView?.visibility = VISIBLE
        image_message_profile?.visibility = VISIBLE

        chatCollectionDescriptionTextView?.movementMethod = LinkMovementMethod.getInstance()
        chatWithWhatsAppTextView?.movementMethod = LinkMovementMethod.getInstance()

        if ((activity as? WChatActivity)?.shouldAnimateChatMessage == true) {
            Handler().postDelayed({
                chatCollectionDescriptionTextView?.visibility = VISIBLE
            }, 600)

            Handler().postDelayed({
                chatWithWhatsAppTextView?.visibility = VISIBLE
            }, 800)
            (activity as? WChatActivity)?.shouldAnimateChatMessage = false
        } else {
            chatCollectionDescriptionTextView?.visibility = VISIBLE
            chatWithWhatsAppTextView?.visibility = VISIBLE
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.offline_chat_options_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.closeChatIcon -> {
                activity?.finish()
                activity?.overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
            }

        }
        return true
    }

    private var tapWhatsAppTemplate: ClickableSpan = object : ClickableSpan() {
        override fun updateDrawState(ds: TextPaint) {
            ds.color = Color.WHITE
            ds.isUnderlineText = true
        }

        override fun onClick(textView: View) {
            val appInstanceObject = AppInstanceObject.get()
            val inAppChatTipAcknowledgements = appInstanceObject.inAppChatTipAcknowledgements
            when (inAppChatTipAcknowledgements.isWhatsAppOnBoardingScreenVisible) {
                true -> {
                    activity?.apply {
                        if (NetworkManager.getInstance().isConnectedToNetwork(this)) {
                            Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.WHATSAPP_CHAT_WITH_US)
                            request(OneAppService.queryServicePostEvent(featureName, appScreen))
                            Utils.openBrowserWithUrl(WhatsAppChatToUsVisibility().whatsAppChatWithUsUrlBreakout)
                            finish()
                            overridePendingTransition(0, 0)
                        } else {
                            ErrorHandlerView(activity).showToast()
                        }
                    }
                }
                false -> {
                    appInstanceObject.inAppChatTipAcknowledgements.isWhatsAppOnBoardingScreenVisible = true
                    appInstanceObject.save()
                    val bundle = Bundle()
                    bundle.putString(FEATURE_NAME, WhatsAppChatToUsVisibility.FEATURE_WHATSAPP)
                    bundle.putString(APP_SCREEN, appScreen)
                    view?.findNavController()?.navigate(R.id.chatToUsWhatsAppFragment, bundle, navOptions())
                }
            }
        }
    }
}