package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.router

import android.app.Activity
import android.content.Intent
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.ui.activities.DebitOrderActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl
import za.co.woolworths.financial.services.android.ui.activities.cli.CLIPhase2Activity
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.AccountOptionsImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_credit_limit_increase.CreditLimitIncreaseLanding
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

interface IProductLandingRouter {
    fun routeToDebitOrderActivity(activity: Activity?)
    fun routeToBalanceProtectionInsuranceActivity(intent: Intent, activity: Activity?)
    fun routeToPaymentOptions()
    fun routeToCreditLimitIncrease(
        activity: Activity?,
        creditLimitIncreaseLanding: CreditLimitIncreaseLanding
    )
}

class ProductLandingRouterImpl @Inject constructor(
    private var accountOptions: AccountOptionsImpl
) : IProductLandingRouter {

    override fun routeToDebitOrderActivity(activity: Activity?) {
        activity?.apply {
            val debitOrderIntent = Intent(this, DebitOrderActivity::class.java)
            debitOrderIntent.putExtra("DebitOrder", accountOptions.account?.getDebitOrder())
            startActivity(debitOrderIntent)
            overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
        }
    }

    override fun routeToBalanceProtectionInsuranceActivity(intent: Intent, activity: Activity?) {
        activity?.apply {
            startActivityForResult(
                intent,
                AppConstant.BALANCE_PROTECTION_INSURANCE_REQUEST_CODE
            )
            overridePendingTransition(0, 0)
        }
    }

    override fun routeToPaymentOptions() {

    }


    override fun routeToCreditLimitIncrease(
        activity: Activity?,
        creditLimitIncreaseLanding: CreditLimitIncreaseLanding
    ) {
        activity?.apply {
            with(creditLimitIncreaseLanding) {
                // TODO:: Remove productOfferingId from Application class
                WoolworthsApplication.getInstance()?.setProductOfferingId(productOfferingId)
                val openCLIIncrease = Intent(this@apply, CLIPhase2Activity::class.java)
                openCLIIncrease.putExtra(
                    OFFER_ACTIVE_PAYLOAD,
                    Utils.objectToJson(offerActive)
                )
                openCLIIncrease.putExtra(IS_OFFER_ACTIVE, offerActive?.offerActive)
                openCLIIncrease.putExtra(
                    AccountSignedInPresenterImpl.APPLY_NOW_STATE,
                    applyNowState
                )
                startActivityForResult(openCLIIncrease, 0)
                overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
            }
        }
    }

    companion object {
        const val OFFER_ACTIVE_PAYLOAD = "OFFER_ACTIVE_PAYLOAD"
        const val IS_OFFER_ACTIVE = "OFFER_IS_ACTIVE"
    }

}