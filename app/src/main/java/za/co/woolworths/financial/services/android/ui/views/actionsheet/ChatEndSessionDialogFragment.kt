package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.ccs_end_session_dialog_fragment.*
import za.co.woolworths.financial.services.android.ui.activities.WChatActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatViewModel
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
import za.co.woolworths.financial.services.android.util.voc.VoiceOfCustomerManager
import za.co.woolworths.financial.services.android.util.wenum.VocTriggerEvent

class ChatEndSessionDialogFragment : WBottomSheetDialogFragment(), View.OnClickListener {

    private val chatViewModel: ChatViewModel by activityViewModels()
    var vocTriggerEvent: VocTriggerEvent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chatViewModel.triggerFirebaseEventChatBreak()

        activity?.intent?.extras?.apply {
            vocTriggerEvent = getSerializable(WChatActivity.EXTRA_VOC_TRIGGER_EVENT) as? VocTriggerEvent
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.ccs_end_session_dialog_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        endSessionButton?.apply {
            AnimationUtilExtension.animateViewPushDown(this)
            setOnClickListener(this@ChatEndSessionDialogFragment)
        }

        noContinueSessionButton?.apply {
            paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
            AnimationUtilExtension.animateViewPushDown(this)
            setOnClickListener(this@ChatEndSessionDialogFragment)
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.endSessionButton -> {
                with(chatViewModel) {
                    triggerFirebaseEventEndSession()
                    isCustomerSignOut.value = true
                    postChatEventEndSession()
                }
                dismiss()
//                VoiceOfCustomerManager.showVocSurveyIfNeeded(context, vocTriggerEvent)
                VoiceOfCustomerManager.showVocSurveyIfNeeded(context, VocTriggerEvent.CHAT_PL_STATEMENT)
            }
            R.id.noContinueSessionButton -> dismiss()
        }
    }
}