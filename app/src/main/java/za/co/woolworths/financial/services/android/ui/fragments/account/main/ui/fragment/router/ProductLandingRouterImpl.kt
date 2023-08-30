package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.router

import android.app.Activity
import android.content.Intent
import androidx.appcompat .app.AppCompatActivity
import androidx.navigation.NavController
import com.awfs.coordination.R
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.ProductGroupCode
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.dto.account.ServerErrorResponse
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.models.dto.npc.Transition
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.VirtualCardStaffMemberMessage
import za.co.woolworths.financial.services.android.ui.activities.DebitOrderActivity
import za.co.woolworths.financial.services.android.ui.activities.GetAPaymentPlanActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.treatmentplan.OutSystemBuilder
import za.co.woolworths.financial.services.android.ui.activities.card.BlockMyCardActivity
import za.co.woolworths.financial.services.android.ui.activities.card.InstantStoreCardReplacementActivity
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity
import za.co.woolworths.financial.services.android.ui.activities.card.SelectStoreActivity
import za.co.woolworths.financial.services.android.ui.activities.cli.CLIPhase2Activity
import za.co.woolworths.financial.services.android.ui.activities.store_card.RequestOTPActivity
import za.co.woolworths.financial.services.android.ui.activities.temporary_store_card.GetTemporaryStoreCardPopupActivity
import za.co.woolworths.financial.services.android.ui.activities.temporary_store_card.HowToUseTemporaryStoreCardActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.card_not_received.StoreCardNotReceivedFragmentDirections
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.card.AccountsOptionFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.device_security.linkMyDeviceIfNecessary
import za.co.woolworths.financial.services.android.ui.fragments.account.freeze.TemporaryFreezeStoreCard
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.ToastFactory
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.AccountOptionsImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.StoreCardActivity.Companion.ACTIVATE_VIRTUAL_CARD_DETAIL
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.StoreCardActivity.Companion.BLOCK_CARD_DETAIL
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.StoreCardActivity.Companion.GET_REPLACEMENT_CARD_DETAIL
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_credit_limit_increase.CreditLimitIncreaseLanding
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card.ManageMyCardDetailsFragmentDirections
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card.PayWithCardListFragmentDirections
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main.ManageCardFunctionalRequirementImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.utils.showErrorDialog
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.landing.AccountProductsHomeFragmentDirections
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.landing.AccountProductsHomeViewModel
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.ViewTreatmentPlanDialogFragment
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import za.co.woolworths.financial.services.android.util.wenum.StoreCardViewType
import java.lang.Exception
import javax.inject.Inject

interface IProductLandingRouter {
    fun routeToDebitOrderActivity(activity: Activity?)
    fun routeToBalanceProtectionInsuranceActivity(intent: Intent, activity: Activity?)
    fun routeToPaymentOptions()
    fun routeToCreditLimitIncrease(
        activity: Activity?,
        creditLimitIncreaseLanding: CreditLimitIncreaseLanding
    )

    fun routeToLinkNewCard(activity: Activity?): CallBack
    fun routeToManageMyCard(activity: Activity): CallBack
    fun routeToActivateVirtualTempCard(activity: Activity, isDeviceLinked: Boolean): CallBack?
    fun routeToGetReplacementCard(activity: Activity?): CallBack?
    fun routeToBlockCard(activity: Activity, isDeviceLinked: Boolean = true): CallBack
    fun routeToHowItWorks(
        activity: Activity?,
        isStaffMemberAndHasTemporaryCard: Boolean,
        virtualCardStaffMemberMessage: VirtualCardStaffMemberMessage?
    )

    fun routeToOTPActivity(activity: Activity?)
    fun routeToScanToPayBarcode(
        findNavController: NavController,
        storeCardsResponse: StoreCardsResponse?
    )

    fun routeToServerErrorDialog(
        appCompatActivity: Activity?,
        serverErrorResponse: ServerErrorResponse?
    )

    fun routeToManageMyCardDetails(findNavController: NavController)
    fun routeToDefaultErrorMessageDialog(activity: Activity?)
    fun showNoConnectionToast(activity: Activity?)
    fun routeToAccountOptionsProductLanding(findNavController: NavController?)
    fun routeToSetupPaymentPlan(activity: Activity?, viewModel: AccountProductsHomeViewModel?)
    fun routeToViewTreatmentPlan(activity: Activity?, viewModel: AccountProductsHomeViewModel?)
    fun routeToStartNewElitePlan(activity: Activity?, viewModel: AccountProductsHomeViewModel?)
    fun routeToCardNotReceivedView(findNavController: NavController?, isManageMyCardSection : Boolean = false)
    fun routeToCardNotArrivedFailure(findNavController: NavController?, response: ServerErrorResponse?)
    fun routeToConfirmCardNotReceived(findNavController: NavController?)
}

sealed class CallBack {
    data class IntentCallBack(val intent: Intent?) : CallBack()
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

    override fun routeToLinkNewCard(activity: Activity?): CallBack {
        activity.apply {
            Intent(this, InstantStoreCardReplacementActivity::class.java).apply {
                putExtra(
                    MyCardDetailActivity.STORE_CARD_DETAIL,
                    Utils.objectToJson(manageCardImpl.getStoreCardsResponse())
                )
                return CallBack.IntentCallBack(this)
            }
        }
    }

    override fun routeToManageMyCard(activity: Activity): CallBack {
        return CallBack.IntentCallBack(navigateToTemporaryStoreCard(activity))
    }

    fun navigateToTemporaryStoreCard(activity: Activity): Intent {
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

    override fun routeToActivateVirtualTempCard(
        activity: Activity,
        isDeviceLinked: Boolean
    ): CallBack {
        var intent: Intent? = null
        linkMyDeviceIfNecessary(
            activity = activity,
            isDeviceLinked = isDeviceLinked,
            ApplyNowState.STORE_CARD,
            {
                ACTIVATE_VIRTUAL_CARD_DETAIL = true
            },
            {
                if (manageCardImpl.isActivateVirtualTempCard()) {
                    val storeCardResponse =
                        manageCardImpl.getStoreCardsResponse() ?: StoreCardsResponse()
                    intent = navigateToGetTemporaryStoreCardPopupActivity(
                        activity, storeCardResponse = storeCardResponse
                    )
                    intent
                }
            })
        return CallBack.IntentCallBack(intent)
    }

    override fun routeToGetReplacementCard(activity: Activity?): CallBack? {
        activity ?: return null

        var selectStoreActivity: Intent? = null
        KotlinUtils.linkDeviceIfNecessary(activity, ApplyNowState.STORE_CARD, {
            GET_REPLACEMENT_CARD_DETAIL = true
        }, {
            val storeCardResponse = manageCardImpl.getStoreCardsResponse() ?: StoreCardsResponse()
            Utils.triggerFireBaseEvents(
                FirebaseManagerAnalyticsProperties.MYACCOUNTS_ICR_GET_CARD,
                activity
            )
            selectStoreActivity = Intent(activity, SelectStoreActivity::class.java)
            selectStoreActivity?.putExtra(
                SelectStoreActivity.STORE_DETAILS,
                Gson().toJson(storeCardResponse)
            )
        })
        return CallBack.IntentCallBack(selectStoreActivity)
    }

    fun navigateToGetReplacementCard(
        activity: Activity?
    ) {
        val storeCardResponse = manageCardImpl.getStoreCardsResponse() ?: StoreCardsResponse()
        Utils.triggerFireBaseEvents(
            FirebaseManagerAnalyticsProperties.MYACCOUNTS_ICR_GET_CARD,
            activity
        )
        val selectStoreActivity = Intent(activity, SelectStoreActivity::class.java)
        selectStoreActivity.putExtra(
            SelectStoreActivity.STORE_DETAILS,
            Gson().toJson(storeCardResponse)
        )
        activity?.startActivity(selectStoreActivity)
    }

    override fun routeToBlockCard(activity: Activity, isDeviceLinked: Boolean): CallBack {
        val storeCardResponse = manageCardImpl.getStoreCardsResponse() ?: StoreCardsResponse()
        activity.apply {
            var openBlockMyCardActivity: Intent? = null
            linkMyDeviceIfNecessary(
                activity = activity,
                isDeviceLinked = isDeviceLinked,
                state = ApplyNowState.STORE_CARD,
                {
                    BLOCK_CARD_DETAIL = true
                },
                {
                    openBlockMyCardActivity = Intent(this, BlockMyCardActivity::class.java)
                    openBlockMyCardActivity?.putExtra(
                        MyCardDetailActivity.STORE_CARD_DETAIL,
                        Gson().toJson(storeCardResponse)
                    )
                })

            return CallBack.IntentCallBack(openBlockMyCardActivity)
        }
    }

    override fun routeToHowItWorks(
        activity: Activity?,
        isStaffMemberAndHasTemporaryCard: Boolean,
        virtualCardStaffMemberMessage: VirtualCardStaffMemberMessage?
    ) {
        activity?.apply {
            Intent(this, HowToUseTemporaryStoreCardActivity::class.java).let {
                it.putExtra(
                    HowToUseTemporaryStoreCardActivity.TRANSACTION_TYPE,
                    Transition.SLIDE_LEFT
                )
                if (isStaffMemberAndHasTemporaryCard) {
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
            try {
                findNavController.navigate(
                    PayWithCardListFragmentDirections.actionPayWithCardListFragmentToScanBarcodeToPayDialogFragment(
                        it
                    )
                )
            } catch (e: Exception) {
                FirebaseManager.logException(e)
            }
        }
    }

    private fun navigateToGetTemporaryStoreCardPopupActivity(
        activity: Activity,
        storeCardResponse: StoreCardsResponse,
        screenType: StoreCardViewType = StoreCardViewType.DEFAULT
    ): Intent {
        activity.apply {
            Intent(this, GetTemporaryStoreCardPopupActivity::class.java).apply {
                putExtra(
                    MyCardDetailActivity.STORE_CARD_DETAIL,
                    Utils.objectToJson(storeCardResponse)
                )
                if (screenType != StoreCardViewType.DEFAULT)
                    putExtra(MyCardDetailActivity.STORE_CARD_VIEW_TYPE, screenType)

                return this
            }
        }
    }

    private fun navigateToMyCardDetailActivity(
        activity: Activity,
        storeCardResponse: StoreCardsResponse,
        requestUnblockStoreCardCall: Boolean = false,
        screenType: StoreCardViewType = StoreCardViewType.DEFAULT
    ): Intent {
        activity.apply {
            return Intent(this, MyCardDetailActivity::class.java).apply {
                putExtra(
                    MyCardDetailActivity.STORE_CARD_DETAIL,
                    Utils.objectToJson(storeCardResponse)
                )
                putExtra(
                    TemporaryFreezeStoreCard.ACTIVATE_UNBLOCK_CARD_ON_LANDING,
                    requestUnblockStoreCardCall
                )
                if (screenType != StoreCardViewType.DEFAULT)
                    putExtra(MyCardDetailActivity.STORE_CARD_VIEW_TYPE, screenType)
            }
        }
    }

    override fun routeToServerErrorDialog(
        appCompatActivity: Activity?,
        serverErrorResponse: ServerErrorResponse?
    ) {
        serverErrorResponse?.let { response ->
            showErrorDialog(
                appCompatActivity as? androidx.appcompat.app.AppCompatActivity,
                response
            )
        }
    }

    override fun routeToManageMyCardDetails(findNavController: NavController) {
        findNavController.navigate(AccountProductsHomeFragmentDirections.actionAccountProductsHomeFragmentToManageMyCardDetailsFragment())
    }

    override fun routeToDefaultErrorMessageDialog(
        activity: Activity?
    ) {
        val serverErrorResponse = ServerErrorResponse()
        serverErrorResponse.desc = activity?.getString(R.string.oops_error_message) ?: ""
        showErrorDialog(activity as? androidx.appcompat.app.AppCompatActivity, serverErrorResponse)
    }

    override fun showNoConnectionToast(activity: Activity?) {
        ToastFactory.showNoConnectionFound(activity)
    }

    override fun routeToAccountOptionsProductLanding(findNavController: NavController?) {
        findNavController?.navigate(ManageMyCardDetailsFragmentDirections.actionManageMyCardDetailsFragmentToAccountProductsHomeFragment())
    }

    override fun routeToSetupPaymentPlan(
        activity: Activity?,
        viewModel: AccountProductsHomeViewModel?
    ) {
        activity ?: return
        viewModel ?: return
        val intent = Intent(activity, GetAPaymentPlanActivity::class.java)
        intent.putExtra(ViewTreatmentPlanDialogFragment.ELIGIBILITY_PLAN, viewModel.eligibilityPlan)
        activity.startActivityForResult(intent, AccountsOptionFragment.REQUEST_GET_PAYMENT_PLAN)
        activity.overridePendingTransition(R.anim.slide_from_right, R.anim.stay)
    }

    override fun routeToViewTreatmentPlan(
        activity: Activity?,
        viewModel: AccountProductsHomeViewModel?
    ) {
        activity ?: return
        viewModel ?: return
        val outSystemBuilder =
            OutSystemBuilder(activity, ProductGroupCode.SC, viewModel.eligibilityPlan)
        outSystemBuilder.build()
    }

    override fun routeToStartNewElitePlan(
        activity: Activity?,
        viewModel: AccountProductsHomeViewModel?
    ) {
        activity?.apply {
            val intent = Intent(this, GetAPaymentPlanActivity::class.java)
            intent.putExtra(
                ViewTreatmentPlanDialogFragment.ELIGIBILITY_PLAN,
                viewModel?.eligibilityPlan
            )
            startActivityForResult(intent, AccountsOptionFragment.REQUEST_ELITEPLAN)
            overridePendingTransition(R.anim.slide_from_right, R.anim.stay)
        }
    }

    override fun routeToCardNotReceivedView(findNavController: NavController?, isManageMyCardSection : Boolean) {
        try {
            val direction = if (isManageMyCardSection)
                ManageMyCardDetailsFragmentDirections.actionManageMyCardDetailsFragmentToStoreCardNotReceivedNavigation()
            else
                AccountProductsHomeFragmentDirections.actionAccountProductsHomeFragmentToStoreCardNotReceivedDialogFragment()
            findNavController?.navigate(direction)
        } catch (ex: Exception) {
          FirebaseManager.logException(ex)
        }
    }

    override fun routeToCardNotArrivedFailure(
        findNavController: NavController?,
        response: ServerErrorResponse?
    ) {
        findNavController?.navigate(
            StoreCardNotReceivedFragmentDirections.actionStoreCardNotReceivedFragmentToStoreCardNotReceivedDialogFragment(
                response
            )
        )
    }

    override fun routeToConfirmCardNotReceived(findNavController: NavController?) {
        findNavController?.navigate(StoreCardNotReceivedFragmentDirections.actionStoreCardNotReceivedFragmentToCardNotReceivedConfirmationFragment())
    }

    companion object {
        const val OFFER_ACTIVE_PAYLOAD = "OFFER_ACTIVE_PAYLOAD"
        const val IS_OFFER_ACTIVE = "OFFER_IS_ACTIVE"
    }

}