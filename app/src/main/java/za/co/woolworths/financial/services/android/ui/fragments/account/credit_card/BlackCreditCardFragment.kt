package za.co.woolworths.financial.services.android.ui.fragments.account.credit_card

import android.os.Bundle
import android.view.View
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.account_available_fund_overview_fragment.*
import za.co.woolworths.financial.services.android.ui.fragments.account.AvailableFundFragment

class BlackCreditCardFragment : AvailableFundFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        accountOverviewRootLayout?.setBackgroundResource(R.drawable.black_credit_card_background)
    }
}