package za.co.woolworths.financial.services.android.ui.fragments.account.detail

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.card.AccountsOptionFragment
import za.co.woolworths.financial.services.android.util.KotlinUtils

class PersonalLoanDetailFragment : AccountsOptionFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            includeAccountDetailHeaderView.root.visibility = GONE
            if (mCardPresenterImpl?.isDebitOrderActive() == VISIBLE) {
                includeCommonAccountDetails.includeAccountPaymentOption.debitOrderViewGroup?.visibility = VISIBLE
                KotlinUtils.roundCornerDrawable(includeCommonAccountDetails.includeAccountPaymentOption.debitOrderIsActiveTextView, "#bad110")
            } else {
                includeCommonAccountDetails.includeAccountPaymentOption.debitOrderViewGroup?.visibility = GONE
            }
            includeAccountDetailHeaderView.myCardDetailTextView?.visibility = GONE

            // Hide withdraw cash row for accounts not in productOfferingGoodStanding
            includeCommonAccountDetails.includeAccountPaymentOption.withdrawCashViewGroup?.visibility =
                if (mCardPresenterImpl?.getAccount()?.productOfferingGoodStanding == true) VISIBLE else GONE
            activity?.findViewById<TextView>(R.id.topRoundedView)
                ?.setBackgroundResource(R.drawable.rounded_white_bg)
        }
    }
}