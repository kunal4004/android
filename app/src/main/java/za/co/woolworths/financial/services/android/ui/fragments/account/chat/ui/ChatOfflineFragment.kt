package za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.*
import android.view.View.VISIBLE
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.chat_to_collection_agent_offline_fragment.*
import kotlinx.coroutines.GlobalScope
import za.co.woolworths.financial.services.android.ui.activities.WChatActivity
import za.co.woolworths.financial.services.android.ui.extension.doAfterDelay
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.helper.ChatCustomerInfo
import za.co.woolworths.financial.services.android.util.DelayConstant.Companion.DELAY_300_MS
import za.co.woolworths.financial.services.android.util.KotlinUtils
import java.text.SimpleDateFormat
import java.util.*

class ChatOfflineFragment : Fragment() {

    private val chatViewModel: ChatViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.chat_to_collection_agent_offline_fragment,
            container,
            false
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? WChatActivity)?.apply {
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            setChatState(false)
        }

        hiClientTextView?.text = "Hi ${ChatCustomerInfo.getUsername()},"

        chatCollectionDescriptionTextView?.text = chatViewModel.offlineMessageTemplate { result ->
            KotlinUtils.sendEmail(activity, result.first, result.second, result.third)
            (activity as? WChatActivity)?.shouldDismissChatNavigationModel = true
        }

        val currentTime: String? =
            SimpleDateFormat("hh : mm a", Locale.getDefault()).format(Calendar.getInstance().time)
        timeTextView?.text = currentTime


        hiClientTextView?.visibility = VISIBLE
        image_message_profile?.visibility = VISIBLE

        chatCollectionDescriptionTextView?.movementMethod = LinkMovementMethod.getInstance()

        if ((activity as? WChatActivity)?.shouldAnimateChatMessage == true) {

            GlobalScope.doAfterDelay(DELAY_300_MS) {
                chatCollectionDescriptionTextView?.visibility = VISIBLE
            }

            (activity as? WChatActivity)?.shouldAnimateChatMessage = false
        } else {
            chatCollectionDescriptionTextView?.visibility = VISIBLE
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
}