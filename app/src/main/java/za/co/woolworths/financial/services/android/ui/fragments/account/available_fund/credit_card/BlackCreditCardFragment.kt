package za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.credit_card

import android.os.Bundle
import android.view.View
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.account_available_fund_overview_fragment.*
import kotlinx.android.synthetic.main.view_pay_my_account_button.*
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.ui.extension.bindString

import za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.AvailableFundsFragment

class BlackCreditCardFragment : AvailableFundsFragment(), View.OnClickListener {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        availableFundBackground?.setBackgroundResource(R.drawable.black_credit_card_background)

        incViewStatementButton?.visibility = if (WoolworthsApplication.getAbsaBankingOpenApiServices()?.isEnabled == true) View.VISIBLE else View.GONE

        paymentOptionTextView?.text = bindString(R.string.payment_option_title)
        viewPaymentOptionImageView?.setImageResource(R.drawable.icon_money)

        incRecentTransactionButton?.setOnClickListener(this)
        incViewStatementButton?.setOnClickListener(this)
        incPayMyAccountButton?.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.incRecentTransactionButton -> navigateToRecentTransactionActivity("CC")
            R.id.incPayMyAccountButton -> navigateToPaymentOptionsActivity()
            R.id.incViewStatementButton -> navigateToABSAStatementActivity()
        }
    }
}