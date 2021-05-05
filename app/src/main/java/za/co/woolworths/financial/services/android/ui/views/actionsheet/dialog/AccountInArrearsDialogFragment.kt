package za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.account_in_arrears_alert_dialog_fragment.*
import kotlinx.android.synthetic.main.account_in_arrears_fragment_dialog.accountInArrearsDescriptionTextView
import za.co.woolworths.financial.services.android.contracts.IShowChatBubble
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.util.CurrencyFormatter
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class AccountInArrearsDialogFragment : AppCompatDialogFragment(), View.OnClickListener {

    private val payMyAccountViewModel: PayMyAccountViewModel by activityViewModels()
    private var showChatBubbleInterface: IShowChatBubble? = null
    private val mClassName = AccountInArrearsDialogFragment::class.java.simpleName

    companion object {
        const val ARREARS_PAY_NOW_BUTTON = "payNowButton"
        const val ARREARS_CHAT_TO_US_BUTTON = "chatToUsButton"

    }

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

        accountInArrearsDescriptionTextView?.text = payMyAccountViewModel.getCardDetail()?.account?.second?.amountOverdue?.let { totalAmountDue -> activity?.resources?.getString(R.string.payment_overdue_error_desc, Utils.removeNegativeSymbol(CurrencyFormatter.formatAmountToRandAndCent(totalAmountDue))) }

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
                dismiss()
                setFragmentResult(mClassName, bundleOf(mClassName to ARREARS_PAY_NOW_BUTTON))
            }

            R.id.chatToUsButton -> {
                dismiss()
                setFragmentResult(mClassName, bundleOf(mClassName to ARREARS_CHAT_TO_US_BUTTON))
            }

            R.id.closeIconImageButton -> dismiss()
        }
    }

    override fun onDestroy() {
        showChatBubbleInterface?.showChatBubble()
        super.onDestroy()
    }
}