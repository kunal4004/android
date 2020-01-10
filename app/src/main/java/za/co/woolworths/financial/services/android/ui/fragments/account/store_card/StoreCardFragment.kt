package za.co.woolworths.financial.services.android.ui.fragments.account.store_card

import android.os.Bundle
import android.view.View
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.account_available_fund_overview_fragment.*
import za.co.woolworths.financial.services.android.ui.fragments.account.AvailableFundFragment

class StoreCardFragment : AvailableFundFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        accountOverviewRootLayout?.setBackgroundResource(R.drawable.store_card_background)

        incViewPaymentOptionButton?.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        super.onClick(view)
        when (view?.id) {
            R.id.incViewPaymentOptionButton -> navigateToPaymentOptionActivity()
        }
    }
}