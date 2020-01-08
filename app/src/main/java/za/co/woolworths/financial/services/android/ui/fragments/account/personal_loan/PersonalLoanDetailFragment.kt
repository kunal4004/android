package za.co.woolworths.financial.services.android.ui.fragments.account.personal_loan

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.VISIBLE
import com.awfs.coordination.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.account_detail_header_fragment.*
import kotlinx.android.synthetic.main.account_options_layout.*
import za.co.woolworths.financial.services.android.ui.activities.loan.LoanWithdrawalActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.AccountCardDetailFragment

class PersonalLoanDetailFragment : AccountCardDetailFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cardDetailImageView?.setImageResource(R.drawable.w_personal_loan_card)

        withdrawCashGroup?.visibility = VISIBLE
        debitOrderViewGroup?.visibility = mCardPresenterImpl?.isDebitOrderActive() ?: 0

        withdrawCashView?.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        super.onClick(v)
        activity?.apply {
            when (v?.id) {
                R.id.withdrawCashView -> {
                    val intentWithdrawalActivity = Intent(this, LoanWithdrawalActivity::class.java)
                    intentWithdrawalActivity.putExtra("account_info", Gson().toJson(mCardPresenterImpl?.getAccount()))
                    startActivityForResult(intentWithdrawalActivity, 0)
                    overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
                }
            }
        }
    }
}