package za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.credit_card

import android.os.Bundle
import android.view.View
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.account_available_fund_overview_fragment.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode

import za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.AvailableFundFragment
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.DP_LINKING_MY_ACCOUNTS_PRODUCT_STATEMENT
import za.co.woolworths.financial.services.android.ui.fragments.account.helper.FirebaseEventDetailManager
import za.co.woolworths.financial.services.android.util.Utils

class GoldCreditCardFragment : AvailableFundFragment(), View.OnClickListener {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        availableFundBackground?.setBackgroundResource(R.drawable.gold_credit_card_background)

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
            R.id.incPayMyAccountButton -> {
                activity?.apply { Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTS_PMA_CC, this) }
                navigateToPayMyAccountActivity()
            }
            R.id.incViewStatementButton -> navigateToABSAStatementActivity()
        }
    }
}
