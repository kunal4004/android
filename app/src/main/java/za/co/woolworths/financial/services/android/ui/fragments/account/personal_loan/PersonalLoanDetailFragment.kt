package za.co.woolworths.financial.services.android.ui.fragments.account.personal_loan

import android.os.Bundle
import android.view.View
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.account_detail_header_fragment.*
import kotlinx.android.synthetic.main.account_options_layout.*
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.AccountCardDetailFragment

class PersonalLoanDetailFragment : AccountCardDetailFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cardDetailImageView?.setImageResource(R.drawable.w_personal_loan_card)

        debitOrderViewGroup?.visibility = mCardPresenterImpl?.isDebitOrderActive() ?: 0
        myCardDetailTextView?.visibility = View.GONE
    }
}