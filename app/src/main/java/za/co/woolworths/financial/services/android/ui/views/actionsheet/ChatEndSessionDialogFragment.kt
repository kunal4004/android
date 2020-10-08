package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.ccs_end_session_dialog_fragment.*
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatViewModel
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class ChatEndSessionDialogFragment : WBottomSheetDialogFragment(), View.OnClickListener {

    private val chatViewModel: ChatViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chatViewModel.firebaseEventChatBreak()
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
                    firebaseEventEndSession()
                    isCustomerSignOut.value = true
                    postChatEventEndSession()
                }
                dismiss()
            }
            R.id.noContinueSessionButton -> dismiss()
        }
    }
}