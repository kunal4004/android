package za.co.woolworths.financial.services.android.ui.fragments.account.detail

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.account_detail_header_fragment.*
import kotlinx.android.synthetic.main.account_options_layout.*
import kotlinx.android.synthetic.main.my_card_fragment.*
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.card.AccountCardDetailFragment
import za.co.woolworths.financial.services.android.util.KotlinUtils

class StoreCardDetailFragment : AccountCardDetailFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cardDetailImageView?.setImageResource(R.drawable.w_store_card)

        includeManageMyCard?.visibility = VISIBLE

        navigateToGetStoreCards()

        if (mCardPresenterImpl?.isDebitOrderActive() == VISIBLE) {
            debitOrderViewGroup?.visibility = VISIBLE
            KotlinUtils.roundCornerDrawable(debitOrderIsActiveTextView, "#bad110")
        } else {
            debitOrderViewGroup?.visibility = GONE
        }
        debitOrderViewGroup?.visibility = mCardPresenterImpl?.isDebitOrderActive() ?: 0
    }
}