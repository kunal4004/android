package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.router

import android.app.Activity
import android.content.Intent
import androidx.navigation.NavController
import com.awfs.coordination.R
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.dto.account.ServerErrorResponse
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.models.dto.npc.Transition
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.VirtualCardStaffMemberMessage
import za.co.woolworths.financial.services.android.ui.activities.DebitOrderActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl
import za.co.woolworths.financial.services.android.ui.activities.card.BlockMyCardActivity
import za.co.woolworths.financial.services.android.ui.activities.card.InstantStoreCardReplacementActivity
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity
import za.co.woolworths.financial.services.android.ui.activities.card.SelectStoreActivity
import za.co.woolworths.financial.services.android.ui.activities.cli.CLIPhase2Activity
import za.co.woolworths.financial.services.android.ui.activities.store_card.RequestOTPActivity
import za.co.woolworths.financial.services.android.ui.activities.temporary_store_card.GetTemporaryStoreCardPopupActivity
import za.co.woolworths.financial.services.android.ui.activities.temporary_store_card.HowToUseTemporaryStoreCardActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.StoreCardOptionsFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.freeze.TemporaryFreezeStoreCard
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.AccountOptionsImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.StoreCardActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.AccountOptionsManageCardFragmentDirections
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_credit_limit_increase.CreditLimitIncreaseLanding
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card.PayWithCardListFragmentDirections
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main.ManageCardFunctionalRequirementImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.landing.AccountProductsHomeFragmentDirections
import za.co.woolworths.financial.services.android.ui.fragments.npc.MyCardDetailFragment
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.KotlinUtils
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

    fun routeToManageMyCard(activity: Activity?)
    fun routeToLinkNewCard(activity: Activity?)
    fun routeToActivateVirtualTempCard(activity: Activity?)
    fun routeToGetReplacementCard(activity: Activity?)
    fun routeToBlockCard(activity: Activity?)
    fun routeToHowItWorks(
        requireActivity: Activity?,
        staffMemberAndHasTemporaryCard: Boolean,
        virtualCardStaffMemberMessage: VirtualCardStaffMemberMessage?
    )

    fun routeToOTPActivity(activity: Activity?)
    fun routeToScanToPayBarcode(
        findNavController: NavController,
        storeCardsResponse: StoreCardsResponse?
    )

    fun routeToServerErrorDialog(findNavController: NavController?, serverErrorResponse: ServerErrorResponse?)
    fun routeToManageMyCardDetails(findNavController: NavController)
    fun routeToDefaultErrorMessageDialog(activity: Activity?,findNavController: NavController)
    fun showNoConnectionToast(activity: Activity?)
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

    override fun routeToManageMyCard(activity: Activity): Intent {
        return navigateToTemporaryStoreCard(activity)
    }

    private fun navigateToTemporaryStoreCard(activity: Activity): Intent {
        val storeCardResponse =
            manageCardImpl.getStoreCardsResponse() ?: StoreCardsResponse()
        return when (manageCardImpl.isActivateVirtualTempCard()) {
            true -> navigateToGetTemporaryStoreCardPopupActivity(
                activity,
                storeCardResponse = storeCardResponse
            )
            false -> navigateToMyCardDetailActivity(activity, storeCardResponse = storeCardResponse)
        }
    }

    override fun routeToActivateVirtualTempCard(activity: Activity): Intent? {
        var rsult:Intent? = null
        KotlinUtils.linkDeviceIfNecessary(activity, ApplyNowState.STORE_CARD, {
            StoreCardOptionsFragment.ACTIVATE_VIRTUAL_CARD_DETAIL = true
        }, {

             rsult = navigateToTemporaryStoreCard(activity)
        })
        return rsult
    }

    override fun routeToGetReplacementCard(activity: Activity?): Intent {
        activity.apply {
            val storeCardResponse =
                manageCardImpl.getStoreCardsResponse() ?: StoreCardsResponse()
            Utils.triggerFireBaseEvents(
                FirebaseManagerAnalyticsProperties.MYACCOUNTS_ICR_GET_CARD,
                this
            )
            Intent(this, SelectStoreActivity::class.java).apply {
                putExtra(
                    SelectStoreActivity.STORE_DETAILS,
                    Gson().toJson(storeCardResponse)
                )
                return this
            }
        }
    }

    override fun routeToBlockCard(activity: Activity?) {
        val storeCardResponse = manageCardImpl.getStoreCardsResponse() ?: StoreCardsResponse()
        KotlinUtils.linkDeviceIfNecessary(activity, ApplyNowState.STORE_CARD, {
            MyCardDetailFragment.BLOCK_CARD_DETAIL = true
        }, {

            activity?.apply {
                val openBlockMyCardActivity = Intent(this, BlockMyCardActivity::class.java)
                openBlockMyCardActivity.putExtra(
                    MyCardDetailActivity.STORE_CARD_DETAIL,
                    Gson().toJson(storeCardResponse)
                )
                startActivityForResult(
                    openBlockMyCardActivity,
                    BlockMyCardActivity.REQUEST_CODE_BLOCK_MY_CARD
                )
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
            }
        })
    }

    override fun routeToHowItWorks(
        activity: Activity?,
        isVirtualCardEnabled: Boolean,
        virtualCardStaffMemberMessage: VirtualCardStaffMemberMessage?
    ) {
        activity?.apply {
            Intent(this, HowToUseTemporaryStoreCardActivity::class.java).let {
                it.putExtra(
                    HowToUseTemporaryStoreCardActivity.TRANSACTION_TYPE,
                    Transition.SLIDE_LEFT
                )
                if (isVirtualCardEnabled) {
                    it.putExtra(
                        HowToUseTemporaryStoreCardActivity.STAFF_DISCOUNT_INFO,
                        virtualCardStaffMemberMessage
                    )
                }
                startActivity(it)
            }
            overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
        }
    }

    override fun routeToOTPActivity(activity: Activity?) {
        activity?.apply {
            val intent = Intent(this, RequestOTPActivity::class.java)
            intent.putExtra(RequestOTPActivity.OTP_SENT_TO, OTPMethodType.SMS.name)
            startActivityForResult(intent, RequestOTPActivity.OTP_REQUEST_CODE)
            overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
        }
    }

    override fun routeToScanToPayBarcode(
        findNavController: NavController,
        storeCardsResponse: StoreCardsResponse?
    ) {
        storeCardsResponse?.let {
            findNavController.navigate(
                PayWithCardListFragmentDirections.actionPayWithCardListFragmentToScanBarcodeToPayDialogFragment(
                    it
                )
            )
        }
    }

    private fun navigateToInstantReplacementCard(activity: Activity?) {
        activity?.apply {
            val storeCardResponse = manageCardImpl.getStoreCardsResponse() ?: StoreCardsResponse()
            Utils.triggerFireBaseEvents(
                FirebaseManagerAnalyticsProperties.MYACCOUNTS_ICR_GET_CARD,
                this
            )
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

    override fun routeToServerErrorDialog( findNavController: NavController?, serverErrorResponse: ServerErrorResponse?) {
        serverErrorResponse?.let { findNavController?.navigate(
            AccountOptionsManageCardFragmentDirections.actionAccountOptionsManageCardFragmentToGeneralErrorDialogPopupFragment(it))}
    }

    override fun routeToManageMyCardDetails(findNavController: NavController) {
        findNavController.navigate(AccountProductsHomeFragmentDirections.actionAccountProductsHomeFragmentToManageMyCardDetailsFragment())
    }

    override fun routeToDefaultErrorMessageDialog(activity: Activity?, findNavController: NavController) {
        val serverErrorResponse = ServerErrorResponse()
        serverErrorResponse.desc = activity?.getString(R.string.oops_error_message) ?: ""
        findNavController.navigate(AccountOptionsManageCardFragmentDirections.actionAccountOptionsManageCardFragmentToGeneralErrorDialogPopupFragment(serverErrorResponse))
    }

    companion object {
        const val OFFER_ACTIVE_PAYLOAD = "OFFER_ACTIVE_PAYLOAD"
        const val IS_OFFER_ACTIVE = "OFFER_IS_ACTIVE"
    }

}