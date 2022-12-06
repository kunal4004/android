package za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ChatToUsViaWhatsappFragmentBinding
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.ui.activities.WChatActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.WhatsAppChatToUsVisibility
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class WhatsAppChatToUsFragment : Fragment(R.layout.chat_to_us_via_whatsapp_fragment), View.OnClickListener {

    private lateinit var binding: ChatToUsViaWhatsappFragmentBinding
    private val chatViewModel: ChatViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ChatToUsViaWhatsappFragmentBinding.bind(view)

        (activity as? WChatActivity)?.displayEndSessionButton(false)

        (activity as? WChatActivity)?.apply {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            this.binding.agentNameTextView?.text = bindString(R.string.whatsapp_chat_to_us_title)
        }

        with(WhatsAppChatToUsVisibility()) {
            binding.whatsappNumberValueTextView?.text = whatsAppNumber
        }

        binding.chatWithUsButton?.apply {
            AnimationUtilExtension.animateViewPushDown(this)
            setOnClickListener(this@WhatsAppChatToUsFragment)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.chatWithUsButton -> {
                if (NetworkManager.getInstance().isConnectedToNetwork(activity)) {
                    (activity as? WChatActivity)?.shouldDismissChatNavigationModel = true
                    activity?.apply { Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.WHATSAPP_CHAT_WITH_US, this) }
                    chatViewModel.postEventChatOffline()
                    KotlinUtils.openUrlInPhoneBrowser(WhatsAppChatToUsVisibility().whatsAppChatWithUsUrlBreakout, activity)
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