package za.co.woolworths.financial.services.android.ui.fragments.account.detail

import android.os.Bundle
import android.view.View
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.card.AccountsOptionFragment

class BlackCreditOptionsFragment : AccountsOptionFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            includeAccountDetailHeaderView.cardDetailImageView?.setImageResource(R.drawable.w_black_credit_card)
            // No Debit order for credit cards
            includeCommonAccountDetails.includeAccountPaymentOption.debitOrderViewGroup?.visibility = View.GONE
            includeAccountDetailHeaderView.myCardDetailTextView?.visibility = View.GONE
            includeCommonAccountDetails.includeAccountPaymentOption.paymentOptionLogoImageView?.setImageResource(R.drawable.icon_money)
            creditCardActivationView.cardImage.setBackgroundResource(R.drawable.w_black_credit_card)
        }
    }
}