package za.co.woolworths.financial.services.android.ui.fragments.account.personal_loan

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.awfs.coordination.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.account_available_fund_overview_fragment.*
import kotlinx.android.synthetic.main.view_payment_option_button.*
import za.co.woolworths.financial.services.android.ui.activities.loan.LoanWithdrawalActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.AvailableFundsFragment

class PersonalLoanFragment : AvailableFundsFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        accountOverviewRootLayout?.setBackgroundResource(R.drawable.personal_loan_background)
        viewPaymentOptionTextView?.text = getString(R.string.withdrawal_options)

        incViewPaymentOptionButton?.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        super.onClick(view)
        when (view?.id) {
            R.id.incViewPaymentOptionButton -> {
                activity?.apply {
                    val intentWithdrawalActivity = Intent(this, LoanWithdrawalActivity::class.java)
                    intentWithdrawalActivity.putExtra("account_info", Gson().toJson(mAccount))
                    startActivityForResult(intentWithdrawalActivity, 0)
                    overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
                    return
                }
            }
        }
    }

}