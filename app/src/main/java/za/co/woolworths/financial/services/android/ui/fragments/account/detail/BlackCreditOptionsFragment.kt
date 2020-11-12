package za.co.woolworths.financial.services.android.ui.fragments.account.detail

import android.os.Bundle
import android.view.View
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.account_activate_credit_card_layout.*
import kotlinx.android.synthetic.main.account_detail_header_fragment.*
import kotlinx.android.synthetic.main.account_options_layout.*
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.card.AccountsOptionFragment

class BlackCreditOptionsFragment : AccountsOptionFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cardDetailImageView?.setImageResource(R.drawable.w_black_credit_card)
        // No Debit order for credit cards
        debitOrderViewGroup?.visibility = View.GONE
        myCardDetailTextView?.visibility = View.GONE
        paymentOptionLogoImageView?.setImageResource(R.drawable.icon_money)
        cardImage.setBackgroundResource(R.drawable.black_cc_envelope)
    }
}