package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.account_in_arrears_fragment_dialog.*
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.WFormatter

class AccountInArrearsFragmentDialog : WBottomSheetDialogFragment(), View.OnClickListener {

    private var mAccountCards: Pair<ApplyNowState, Account>? = null
    private val payMyAccountViewModel: PayMyAccountViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.account_in_arrears_fragment_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val accountInStringFormat =
                arguments?.getString(AccountSignedInPresenterImpl.MY_ACCOUNT_RESPONSE, "")
        mAccountCards =
                Gson().fromJson(accountInStringFormat, object : TypeToken<Pair<ApplyNowState, Account>>() {}.type)

        accountInArrearsDescriptionTextView?.text = mAccountCards?.second?.totalAmountDue?.let { totalAmountDue -> activity?.resources?.getString(R.string.payment_options_desc, Utils.removeNegativeSymbol(WFormatter.newAmountFormat(totalAmountDue))) }

        paymentOptionButton?.setOnClickListener(this)
        gotITButton?.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.gotITButton -> dismiss()
            R.id.paymentOptionButton -> {
                val cardDetail = payMyAccountViewModel.getCardDetail()
                ScreenManager.presentPayMyAccountActivity(activity, mAccountCards,Gson().toJson(cardDetail))
                dismiss()
            }
        }
    }
}