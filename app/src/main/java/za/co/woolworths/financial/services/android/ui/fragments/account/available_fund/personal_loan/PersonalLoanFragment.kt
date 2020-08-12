package za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.personal_loan

import android.os.Bundle
import android.view.View
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.account_available_fund_overview_fragment.*
import kotlinx.android.synthetic.main.view_pay_my_account_button.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.AvailableFundFragment
import za.co.woolworths.financial.services.android.util.Utils

class PersonalLoanFragment : AvailableFundFragment(), View.OnClickListener {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        availableFundBackground?.setBackgroundResource(R.drawable.personal_loan_background)

        incRecentTransactionButton?.setOnClickListener(this)
        incViewStatementButton?.setOnClickListener(this)
        incPayMyAccountButton?.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.incPayMyAccountButton -> {
                if (viewPaymentOptionImageShimmerLayout?.isShimmerStarted == true) return
                val personalLoanAccount = mAvailableFundPresenter?.getAccount()
                if (personalLoanAccount?.productOfferingGoodStanding != true) {
                    personalLoanAccount?.let { account -> (activity as? AccountSignedInActivity)?.showAccountInArrears(account) }
                } else {
                    when (payUMethodType) {
                        PAYUMethodType.CREATE_USER -> {
                            navigateToPayMyAccountActivity()
                        }
                        PAYUMethodType.CARD_UPDATE -> {

                        }
                    }
                }
            }
            R.id.incRecentTransactionButton -> {
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTSPERSONALLOANTRANSACTIONS)
                navigateToRecentTransactionActivity("PL")
            }
            R.id.incViewStatementButton -> navigateToStatementActivity()
        }
    }
}