package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.router

import android.app.Activity
import android.content.Intent
import com.awfs.coordination.R
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.ui.activities.DebitOrderActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity
import za.co.woolworths.financial.services.android.ui.activities.card.SelectStoreActivity
import za.co.woolworths.financial.services.android.ui.activities.cli.CLIPhase2Activity
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.MyAccountsScreenNavigator
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.MyAccountsScreenNavigator.Companion.navigateToGetTemporaryStoreCardPopupActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.MyAccountsScreenNavigator.Companion.navigateToMyCardDetailActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.StoreCardOptionsFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.AccountOptionsImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_credit_limit_increase.CreditLimitIncreaseLanding
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main.ManageCardFunctionalRequirementImpl
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

interface IProductLandingRouter {
    fun routeToDebitOrderActivity(activity: Activity?)
    fun routeToBalanceProtectionInsuranceActivity(intent: Intent, activity: Activity?)
    fun routeToPaymentOptions()
    fun routeToCreditLimitIncrease(activity: Activity?, creditLimitIncreaseLanding: CreditLimitIncreaseLanding)
    fun routeToManageMyCard(activity: Activity?)
    fun routeToLinkNewCard(activity: Activity?)
    fun routeToActivateVirtualTempCard(activity: Activity?)
    fun routeToGetReplacementCard(activity: Activity?)
    fun routeToBlockCard(activity: Activity?)
}

class ProductLandingRouterImpl @Inject constructor(
    private var accountOptions: AccountOptionsImpl,
    private var manageCardImpl: ManageCardFunctionalRequirementImpl,
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

    override fun routeToManageMyCard(activity: Activity?) {
        navigateToTemporaryStoreCard(activity)
    }

    override fun routeToLinkNewCard(activity: Activity?) {
        val storeCardResponse = manageCardImpl.getStoreCardsResponse() ?: StoreCardsResponse()
        MyAccountsScreenNavigator.navigateToLinkNewCardActivity(
            activity,
            Utils.objectToJson(storeCardResponse)
        )
    }

    override fun routeToActivateVirtualTempCard(activity: Activity?) {
        KotlinUtils.linkDeviceIfNecessary(activity, ApplyNowState.STORE_CARD, {
            StoreCardOptionsFragment.ACTIVATE_VIRTUAL_CARD_DETAIL = true
        }, {
            navigateToTemporaryStoreCard(activity)
        })
    }

    private fun navigateToTemporaryStoreCard(activity: Activity?) {
        val storeCardResponse = manageCardImpl.getStoreCardsResponse() ?: StoreCardsResponse()
        when (manageCardImpl.isActivateVirtualTempCard()) {
            true -> navigateToGetTemporaryStoreCardPopupActivity(
                activity,
                storeCardResponse = storeCardResponse
            )
            false -> navigateToMyCardDetailActivity(activity, storeCardResponse = storeCardResponse)
        }
    }

    override fun routeToGetReplacementCard(activity: Activity?) {
        KotlinUtils.linkDeviceIfNecessary(activity, ApplyNowState.STORE_CARD, {
            StoreCardOptionsFragment.GET_REPLACEMENT_CARD_DETAIL = true
        },{
            navigateToInstantReplacementCard(activity)
        })
    }

    override fun routeToBlockCard(activity: Activity?) {

    }

    private fun navigateToInstantReplacementCard(activity: Activity?) {
        activity?.apply {
        val storeCardResponse = manageCardImpl.getStoreCardsResponse() ?: StoreCardsResponse()
        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTS_ICR_GET_CARD, this)
        Intent(this, SelectStoreActivity::class.java).apply {
            putExtra(
                SelectStoreActivity.STORE_DETAILS,
                Gson().toJson(storeCardResponse)
            )
            startActivityForResult(
                this,
                MyCardDetailActivity.REQUEST_CODE_GET_REPLACEMENT_CARD
            )
            overridePendingTransition(
                R.anim.slide_from_right,
                R.anim.slide_to_left
            )
        }
        }
    }

    companion object {
        const val OFFER_ACTIVE_PAYLOAD = "OFFER_ACTIVE_PAYLOAD"
        const val IS_OFFER_ACTIVE = "OFFER_IS_ACTIVE"
    }

}