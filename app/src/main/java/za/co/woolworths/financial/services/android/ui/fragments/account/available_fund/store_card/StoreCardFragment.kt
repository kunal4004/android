package za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.store_card

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.navigation.Navigation
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.account_available_fund_overview_fragment.*
import kotlinx.android.synthetic.main.view_pay_my_account_button.*

import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity.Companion.PAY_MY_ACCOUNT_REQUEST_CODE
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.AvailableFundFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PMA3DSecureProcessRequestFragment.Companion.PMA_TRANSACTION_COMPLETED_RESULT_CODE
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.NetworkManager
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.util.Utils

class StoreCardFragment : AvailableFundFragment(), View.OnClickListener {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        availableFundBackground?.setBackgroundResource(R.drawable.store_card_background)

        payMyAccountViewModel.queryPaymentMethod.observe(viewLifecycleOwner, {
            isQueryPayUPaymentMethodComplete = false
            queryPaymentMethod()
        })

        incRecentTransactionButton?.setOnClickListener(this)
        incViewStatementButton?.setOnClickListener(this)
        incPayMyAccountButton?.setOnClickListener(this)

    }

    override fun onClick(view: View?) {
        KotlinUtils.avoidDoubleClicks(view)
        when (view?.id) {
            R.id.incRecentTransactionButton -> {
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTSSTORECARDTRANSACTIONS)
                navigateToRecentTransactionActivity(AccountsProductGroupCode.STORE_CARD.groupCode)
            }
            R.id.incViewStatementButton -> navigateToStatementActivity()
            R.id.incPayMyAccountButton -> {
                if (viewPaymentOptionImageShimmerLayout?.isShimmerStarted == true) return

                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTS_PMA_SC)

                if (payMyAccountViewModel.getPaymentMethodType() == PayMyAccountViewModel.PAYUMethodType.ERROR) {
                    navController?.navigate(R.id.payMyAccountRetryErrorFragment)
                    return
                }

                navigateToPayMyAccount {
                    navController?.navigate(StoreCardFragmentDirections.storeCardFragmentToDisplayVendorDetailFragmentAction())
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PAY_MY_ACCOUNT_REQUEST_CODE -> {
                if (resultCode == PMA_TRANSACTION_COMPLETED_RESULT_CODE) {
                    if (NetworkManager.getInstance().isConnectedToNetwork(context))
                        queryPaymentMethod()
                }
            }
        }
    }
}