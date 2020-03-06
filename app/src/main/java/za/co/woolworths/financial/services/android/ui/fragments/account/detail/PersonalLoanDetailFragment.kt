package za.co.woolworths.financial.services.android.ui.fragments.account.detail

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.account_card_detail_fragment.*
import kotlinx.android.synthetic.main.account_detail_header_fragment.*
import kotlinx.android.synthetic.main.account_options_layout.*
import kotlinx.android.synthetic.main.common_account_detail.*
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.card.AccountCardDetailFragment
import za.co.woolworths.financial.services.android.util.KotlinUtils


class PersonalLoanDetailFragment : AccountCardDetailFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        includeAccountDetailHeaderView?.visibility = GONE
        if (mCardPresenterImpl?.isDebitOrderActive() == VISIBLE) {
            KotlinUtils.roundCornerDrawable(debitOrderIsActiveTextView, "#bad110")
        } else {
            debitOrderViewGroup?.visibility = GONE
        }
        myCardDetailTextView?.visibility = GONE

        val displayMetrics = DisplayMetrics()
        activity?.apply {
            windowManager?.defaultDisplay?.getMetrics(displayMetrics)
            val height = displayMetrics.heightPixels
            val overlayHeight =height -  height.div(3)
            val params = bottomSpaceView.layoutParams
            params.height = overlayHeight
            bottomSpaceView?.layoutParams = params
        }

        // Hide withdraw cash row for accounts not in productOfferingGoodStanding
        withdrawCashViewGroup?.visibility = if (mCardPresenterImpl?.getAccount()?.productOfferingGoodStanding == true) VISIBLE else GONE
        activity?.findViewById<TextView>(R.id.topRoundedView)?.setBackgroundResource(R.drawable.rounded_white_bg)
    }
}