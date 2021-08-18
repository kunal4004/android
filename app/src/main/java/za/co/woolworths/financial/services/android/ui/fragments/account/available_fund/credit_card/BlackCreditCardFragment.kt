package za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.credit_card

import android.os.Bundle
import android.view.View
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.available_funds_fragment.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode

import za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.AvailableFundFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.helper.FirebaseEventDetailManager

class BlackCreditCardFragment : AvailableFundFragment(), View.OnClickListener {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        availableFundBackground?.setBackgroundResource(R.drawable.black_credit_card_background)

        initShimmer()
        stopProgress()

        incViewStatementButton?.visibility = if (WoolworthsApplication.getAbsaBankingOpenApiServices()?.isEnabled == true) View.VISIBLE else View.GONE

        incRecentTransactionButton?.setOnClickListener(this)
        incViewStatementButton?.setOnClickListener(this)
        incPayMyAccountButton?.setOnClickListener(this)

        navigateToDeepLinkView()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.incRecentTransactionButton -> {
                activity?.runOnUiThread {
                    activity?.runOnUiThread {
                        activity?.apply { FirebaseEventDetailManager.tapped(FirebaseManagerAnalyticsProperties.MYACCOUNTSCREDITCARDTRANSACTIONS, this) }
                        navigateToRecentTransactionActivity(AccountsProductGroupCode.CREDIT_CARD.groupCode)
                    }
                }
            }
            R.id.incPayMyAccountButton ->  onPayMyAccountButtonTap(
            FirebaseManagerAnalyticsProperties.MYACCOUNTS_PMA_CC,
                BlackCreditCardFragmentDirections.actionBlackCreditCardFragmentToEnterPaymentAmountDetailFragment())
            R.id.incViewStatementButton -> navigateToABSAStatementActivity()
        }
    }
}