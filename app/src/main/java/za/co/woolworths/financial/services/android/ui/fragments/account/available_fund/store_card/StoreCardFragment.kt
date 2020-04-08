package za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.store_card

import android.os.Bundle
import android.view.View
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.account_available_fund_overview_fragment.*

import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.AvailableFundsFragment
import za.co.woolworths.financial.services.android.util.Utils

class StoreCardFragment : AvailableFundsFragment(), View.OnClickListener {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        availableFundBackground?.setBackgroundResource(R.drawable.store_card_background)

        incRecentTransactionButton?.setOnClickListener(this)
        incViewStatementButton?.setOnClickListener(this)
        incViewPaymentOptionButton?.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.incRecentTransactionButton -> {
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTSSTORECARDTRANSACTIONS)
                navigateToRecentTransactionActivity("SC")
            }
            R.id.incViewStatementButton -> navigateToStatementActivity()
            R.id.incViewPaymentOptionButton -> navigateToPaymentOptionActivity()
        }
    }
}