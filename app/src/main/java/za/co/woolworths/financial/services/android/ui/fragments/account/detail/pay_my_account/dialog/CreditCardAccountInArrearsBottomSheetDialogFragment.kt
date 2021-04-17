package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.dialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.account_in_arrears_fragment_dialog.*
import kotlinx.android.synthetic.main.account_in_arrears_fragment_dialog.accountInArrearsDescriptionTextView
import za.co.woolworths.financial.services.android.contracts.IShowChatBubble
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.ActivityIntentNavigationManager
import za.co.woolworths.financial.services.android.util.CurrencyFormatter
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class CreditCardAccountInArrearsBottomSheetDialogFragment : WBottomSheetDialogFragment(), View.OnClickListener {

    private val payMyAccountViewModel: PayMyAccountViewModel by activityViewModels()
    private var showChatBubbleInterface: IShowChatBubble? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AccountSignedInActivity)
            showChatBubbleInterface = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.account_in_arrears_fragment_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        accountInArrearsDescriptionTextView?.text = payMyAccountViewModel.getCardDetail()?.account?.second?.totalAmountDue?.let { totalAmountDue -> bindString(R.string.payment_options_desc, Utils.removeNegativeSymbol(CurrencyFormatter.formatAmountToRandAndCent(totalAmountDue))) }
        paymentOptionButton?.apply {
            setOnClickListener(this@CreditCardAccountInArrearsBottomSheetDialogFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }
        gotITButton?.apply {
            setOnClickListener(this@CreditCardAccountInArrearsBottomSheetDialogFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.paymentOptionButton -> {
                val cardDetail = payMyAccountViewModel.getCardDetail()
                ActivityIntentNavigationManager.presentPayMyAccountActivity(activity, cardDetail)
                dismiss()
            }
            R.id.gotITButton -> dismiss()
        }
    }

    override fun onDestroy() {
        showChatBubbleInterface?.showChatBubble()
        super.onDestroy()
    }
}