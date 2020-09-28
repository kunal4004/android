package za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.activityViewModels
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.account_in_arrears_alert_dialog_fragment.*
import kotlinx.android.synthetic.main.account_in_arrears_fragment_dialog.accountInArrearsDescriptionTextView
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.WFormatter
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class AccountInArrearsDialogFragment : AppCompatDialogFragment(), View.OnClickListener {

    private var mAccountCards: Pair<ApplyNowState, Account>? = null
    private val payMyAccountViewModel: PayMyAccountViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val accountInStringFormat = arguments?.getString(AccountSignedInPresenterImpl.MY_ACCOUNT_RESPONSE, "")
        mAccountCards = Gson().fromJson(accountInStringFormat, object : TypeToken<Pair<ApplyNowState, Account>>() {}.type)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.account_in_arrears_alert_dialog_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        accountInArrearsDescriptionTextView?.text = mAccountCards?.second?.amountOverdue?.let { totalAmountDue -> activity?.resources?.getString(R.string.payment_options_desc, Utils.removeNegativeSymbol(WFormatter.newAmountFormat(totalAmountDue))) }

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

    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.payNowButton -> {
                val cardDetail = payMyAccountViewModel.getCardDetail()
                ScreenManager.presentPayMyAccountActivity(activity, mAccountCards, Gson().toJson(cardDetail))
                dismiss()
            }

            R.id.closeIconImageButton, R.id.chatToUsButton -> dismiss()
        }
    }
}