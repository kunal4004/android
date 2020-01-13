package za.co.woolworths.financial.services.android.ui.fragments.account.detail

import android.os.Bundle
import android.view.View
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.account_detail_header_fragment.*
import kotlinx.android.synthetic.main.account_options_layout.*
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.card.AccountCardDetailFragment

class StoreCardDetailFragment : AccountCardDetailFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cardDetailImageView?.setImageResource(R.drawable.w_store_card)
        if (mCardPresenterImpl?.isDebitOrderActive() == View.VISIBLE) {
            debitOrderIsActiveTextView?.setBackgroundResource(R.drawable.round_green_corner)
        } else {
            debitOrderViewGroup?.visibility = View.GONE
        }
        debitOrderViewGroup?.visibility = mCardPresenterImpl?.isDebitOrderActive() ?: 0
    }
}