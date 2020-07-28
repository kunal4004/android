package za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.personal_loan

import android.os.Bundle
import android.view.View
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.account_available_fund_overview_fragment.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.AvailableFundsFragment
import za.co.woolworths.financial.services.android.util.Utils

class PersonalLoanFragment : AvailableFundsFragment(), View.OnClickListener {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        availableFundBackground?.setBackgroundResource(R.drawable.personal_loan_background)

        incRecentTransactionButton?.setOnClickListener(this)
        incViewStatementButton?.setOnClickListener(this)
        incViewPayMyAccountButton?.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.incRecentTransactionButton -> {
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTSPERSONALLOANTRANSACTIONS)
                navigateToRecentTransactionActivity("PL")
            }
            R.id.incViewStatementButton -> navigateToStatementActivity()
            R.id.incViewPaymentOptionButton -> {
                val personalLoanAccount = mAvailableFundPresenter?.getAccount()
                if (personalLoanAccount?.productOfferingGoodStanding != true){
                    personalLoanAccount?.let { account ->  (activity as? AccountSignedInActivity)?.showAccountInArrears(account) }
                }else {
                    navigateToLoanWithdrawalActivity()}
            }
        }
    }
}