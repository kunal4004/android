package za.co.woolworths.financial.services.android.ui.fragments.account.personal_loan

import android.os.Bundle
import android.view.View
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.account_available_fund_overview_fragment.*
import kotlinx.android.synthetic.main.view_payment_option_button.*
import za.co.woolworths.financial.services.android.ui.fragments.account.AvailableFundFragment

class PersonalLoanFragment : AvailableFundFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        accountOverviewRootLayout?.setBackgroundResource(R.drawable.personal_loan_background)
        viewPaymentOptionTextView?.text = getString(R.string.withdrawal_options)
    }

}