package za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.activityViewModels
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.account_in_arrears_alert_dialog_fragment.*
import kotlinx.android.synthetic.main.account_in_arrears_fragment_dialog.accountInArrearsDescriptionTextView
import za.co.woolworths.financial.services.android.contracts.IShowChatBubble
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatFloatingActionButtonBubbleView
import za.co.woolworths.financial.services.android.util.ActivityIntentNavigationManager
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.WFormatter
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class AccountInArrearsDialogFragment : AppCompatDialogFragment(), View.OnClickListener {

    private val payMyAccountViewModel: PayMyAccountViewModel by activityViewModels()
    private var showChatBubbleInterface: IShowChatBubble? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AccountSignedInActivity)
            showChatBubbleInterface = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.account_in_arrears_alert_dialog_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        accountInArrearsDescriptionTextView?.text = payMyAccountViewModel.getCardDetail()?.account?.second?.amountOverdue?.let { totalAmountDue -> activity?.resources?.getString(R.string.payment_overdue_error_desc, Utils.removeNegativeSymbol(WFormatter.newAmountFormat(totalAmountDue))) }

        payNowButton?.apply {
            setOnClickListener(this@AccountInArrearsDialogFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }

        chatToUsButton?.apply {
            setOnClickListener(this@AccountInArrearsDialogFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }

        closeIconImageButton?.apply {
            setOnClickListener(this@AccountInArrearsDialogFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }

        chatToUsButton?.apply {
            setOnClickListener(this@AccountInArrearsDialogFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }


    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.payNowButton -> {
                val cardDetail = payMyAccountViewModel.getCardDetail()
                ActivityIntentNavigationManager.presentPayMyAccountActivity(activity, cardDetail)
                dismiss()
            }

            R.id.chatToUsButton -> {
                val chatBubble = payMyAccountViewModel.getApplyNowState()?.let { applyNowState -> ChatFloatingActionButtonBubbleView(activity = activity, applyNowState = applyNowState) }
                chatBubble?.navigateToChatActivity(activity, payMyAccountViewModel.getCardDetail()?.account?.second)
                dismiss()
            }

            R.id.closeIconImageButton -> dismiss()
        }
    }

    override fun onDestroy() {
        showChatBubbleInterface?.showChatBubble()
        super.onDestroy()
    }
}