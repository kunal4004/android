package za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.credit_card

import android.os.Bundle
import android.view.View
import androidx.navigation.Navigation
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.available_funds_fragment.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode

import za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.AvailableFundFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.helper.FirebaseEventDetailManager

class GoldCreditCardFragment : AvailableFundFragment(), View.OnClickListener {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        availableFundBackground?.setBackgroundResource(R.drawable.gold_credit_card_background)

        navController = Navigation.findNavController(view)

        initShimmer()
        stopProgress()

        incViewStatementButton?.visibility = if (WoolworthsApplication.getAbsaBankingOpenApiServices()?.isEnabled == true) View.VISIBLE else View.GONE
        incRecentTransactionButton?.setOnClickListener(this)
        incPayMyAccountButton?.setOnClickListener(this)
        incViewStatementButton?.setOnClickListener(this)

        navigateToDeepLinkView()

    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.incRecentTransactionButton -> {
                activity?.runOnUiThread {
                    activity?.apply { FirebaseEventDetailManager.tapped(FirebaseManagerAnalyticsProperties.MYACCOUNTSCREDITCARDTRANSACTIONS, this) }
                    navigateToRecentTransactionActivity(AccountsProductGroupCode.CREDIT_CARD.groupCode)
                }
            }
            R.id.incPayMyAccountButton -> onPayMyAccountButtonTap(
                FirebaseManagerAnalyticsProperties.MYACCOUNTS_PMA_CC,
                GoldCreditCardFragmentDirections.actionGoldCreditCardFragmentToEnterPaymentAmountDetailFragment())

            R.id.incViewStatementButton -> navigateToABSAStatementActivity()
        }
    }

}
