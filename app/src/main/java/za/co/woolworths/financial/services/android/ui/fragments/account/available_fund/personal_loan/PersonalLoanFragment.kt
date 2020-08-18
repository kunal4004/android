package za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.personal_loan

import android.os.Bundle
import android.view.View
import androidx.navigation.Navigation
import com.awfs.coordination.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.account_available_fund_overview_fragment.*
import kotlinx.android.synthetic.main.view_pay_my_account_button.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.AvailableFundFragment
import za.co.woolworths.financial.services.android.util.Utils

class PersonalLoanFragment : AvailableFundFragment(), View.OnClickListener {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        availableFundBackground?.setBackgroundResource(R.drawable.personal_loan_background)

        navController = Navigation.findNavController(view)

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
                    navigateToCardOptionsOrPayMyAccount(payUMethodType) {
                        val paymentMethods = Gson().toJson(mPaymentMethodsResponse?.paymentMethods)
                        val accountDetail: Pair<ApplyNowState, Account>? = mAvailableFundPresenter?.getAccountDetail()
                        navController?.navigate(PersonalLoanFragmentDirections.actionPersonalLoanFragmentToEnterPaymentAmountDetailFragment(Gson().toJson(accountDetail), paymentMethods))
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