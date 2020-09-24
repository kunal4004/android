package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.ccs_end_session_dialog_fragment.*
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatCustomerServiceViewModel
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class ChatCustomerServiceEndSessionDialogFragment : WBottomSheetDialogFragment(), View.OnClickListener {

    private val chatViewModel: ChatCustomerServiceViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.ccs_end_session_dialog_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        endSessionButton?.apply {
            AnimationUtilExtension.animateViewPushDown(this)
            setOnClickListener(this@ChatCustomerServiceEndSessionDialogFragment)
        }

        noContinueSessionButton?.apply {
            paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
            AnimationUtilExtension.animateViewPushDown(this)
            setOnClickListener(this@ChatCustomerServiceEndSessionDialogFragment)
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.endSessionButton -> {
                chatViewModel?.isCustomerSignOut.value = true
                dismiss()}
            R.id.noContinueSessionButton -> dismiss()
        }
    }
}