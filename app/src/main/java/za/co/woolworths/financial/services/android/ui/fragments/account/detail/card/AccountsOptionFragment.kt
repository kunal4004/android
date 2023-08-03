package za.co.woolworths.financial.services.android.ui.fragments.account.detail.card

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.view.View.*
import androidx.fragment.app.activityViewModels
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AccountCardDetailFragmentBinding
import com.facebook.shimmer.Shimmer
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.PropertyNames.Companion.ACTION_LOWER_CASE
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.PropertyNames.Companion.activationInitiated
import za.co.woolworths.financial.services.android.contracts.IAccountCardDetailsContract
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.dto.account.*
import za.co.woolworths.financial.services.android.models.dto.account.CreditCardDeliveryStatus.*
import za.co.woolworths.financial.services.android.models.dto.app_config.ConfigCreditCardDeliveryCardTypes
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.CreditCardDeliveryStatusResponse
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.DeliveryStatus
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.models.service.event.BusStation
import za.co.woolworths.financial.services.android.ui.activities.CreditCardActivationActivity
import za.co.woolworths.financial.services.android.ui.activities.GetAPaymentPlanActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.treatmentplan.OutSystemBuilder
import za.co.woolworths.financial.services.android.ui.activities.credit_card_delivery.CreditCardDeliveryActivity
import za.co.woolworths.financial.services.android.ui.activities.loan.LoanWithdrawalActivity
import za.co.woolworths.financial.services.android.ui.extension.asEnumOrDefault
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.cancelRetrofitRequest
import za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.personal_loan.PersonalLoanFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.MyAccountsScreenNavigator
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.device_security.verifyAppInstanceId
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.BalanceProtectionInsuranceActivity
import za.co.woolworths.financial.services.android.ui.fragments.credit_card_activation.CreditCardActivationAvailabilityDialogFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.ViewTreatmentPlanDialogFragment
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding


@AndroidEntryPoint
open class AccountsOptionFragment : BaseFragmentBinding<AccountCardDetailFragmentBinding>(AccountCardDetailFragmentBinding::inflate), OnClickListener, IAccountCardDetailsContract.AccountCardDetailView {

    private var userOfferActiveCallWasCompleted = false
    var mCardPresenterImpl: AccountCardDetailPresenterImpl? = null
    private val disposable: CompositeDisposable? = CompositeDisposable()
    private var cardWithPLCState: Card? = null
    private var creditCardDeliveryStatusResponse: CreditCardDeliveryStatusResponse? = null
    private val payMyAccountViewModel: PayMyAccountViewModel by activityViewModels()
    private var state: ApplyNowState? = null
    private var eligibilityPlan: EligibilityPlan? = null

    companion object {
        const val PLC = "PLC"
        const val REQUEST_CREDIT_CARD_ACTIVATION = 1983
        const val REQUEST_GET_PAYMENT_PLAN = 1984
        const val REQUEST_ELITEPLAN= 9020
        const val REQUEST_ELITEPLAN_SUCCESS= 9021
        var SHOW_CREDIT_CARD_ACTIVATION_SCREEN = false
        var CREDIT_CARD_ACTIVATION_DETAIL = false
        var SHOW_CREDIT_CARD_SHECULE_OR_MANAGE = false
        var CREDIT_CARD_SHECULE_OR_MANAGE = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mCardPresenterImpl = AccountCardDetailPresenterImpl(this, AccountCardDetailModelImpl())
        mCardPresenterImpl?.setAccountDetailBundle(arguments)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            includeCommonAccountDetails.includeAccountPaymentOption.balanceProtectionInsuranceView?.setOnClickListener(this@AccountsOptionFragment)
            includeAccountDetailHeaderView.cardImageRootView?.setOnClickListener(this@AccountsOptionFragment)
            includeCommonAccountDetails.includeAccountPaymentOption.debitOrderView?.setOnClickListener(this@AccountsOptionFragment)
            tvIncreaseLimit?.setOnClickListener(this@AccountsOptionFragment)
            relIncreaseMyLimit?.setOnClickListener(this@AccountsOptionFragment)
            llIncreaseLimitContainer?.setOnClickListener(this@AccountsOptionFragment)
            includeCommonAccountDetails.includeAccountPaymentOption.withdrawCashView?.setOnClickListener(this@AccountsOptionFragment)
            includeCommonAccountDetails.includeAccountPaymentOption.viewPaymentOptions?.setOnClickListener(this@AccountsOptionFragment)
            includeCommonAccountDetails.includeAccountPaymentOption.setUpPaymentPlanOptions?.setOnClickListener(this@AccountsOptionFragment)
            includeCommonAccountDetails.includeAccountPaymentOption.viewTreatmentPlanOptions?.setOnClickListener(this@AccountsOptionFragment)
            creditCardActivationView.activateCreditCard?.setOnClickListener(this@AccountsOptionFragment)
            creditCardActivationView.scheduleOrManageCreditCardDelivery?.setOnClickListener(this@AccountsOptionFragment)
            AnimationUtilExtension.animateViewPushDown(includeAccountDetailHeaderView.cardDetailImageView)

            mCardPresenterImpl?.apply {
                getBpiInsuranceApplication()
                displayCardHolderName()
                creditLimitIncrease()?.showCLIProgress(
                    logoIncreaseLimit,
                    llCommonLayer,
                    tvIncreaseLimit
                )
                showBalanceProtectionInsuranceLead()
            }

            disposable?.add(WoolworthsApplication.getInstance()
                .bus()
                .toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { item ->
                    if (item is BusStation) {
                        val offerActive = item.offerActive
                        if (offerActive != null) {
                            hideCLIView()
                            handleCreditLimitIncreaseTagStatus(offerActive)
                        } else if (item.makeApiCall()) {
                            hideCLIView()
                            userOfferActiveCallWasCompleted = false
                            retryConnect()
                        }
                    }
                })

            autoConnectToNetwork()

            initCreditCardActivation()

            //Disable shimmer for non store card
            if (mCardPresenterImpl?.isProductCodeStoreCard() != true) {
                disableShimmer()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        binding.apply {
            activity?.apply {
                if (NetworkManager.getInstance().isConnectedToNetwork(this)) {
                    mCardPresenterImpl?.apply {
                        logoIncreaseLimit?.visibility = GONE
                        llCommonLayer?.visibility = GONE
                        tvIncreaseLimit?.text = ""
                        tvIncreaseLimit?.visibility = GONE
                        logoIncreaseLimit?.visibility = GONE
                        tvIncreaseLimitDescription?.visibility = GONE
                        getUserCLIOfferActive()
                    }
                }
            }

            if (PersonalLoanFragment.SHOW_PL_WITHDRAW_FUNDS_SCREEN) {
                PersonalLoanFragment.SHOW_PL_WITHDRAW_FUNDS_SCREEN = false
                mCardPresenterImpl?.apply {
                    cancelRequest()
                    navigateToLoanWithdrawalActivity()
                }
            } else if (SHOW_CREDIT_CARD_ACTIVATION_SCREEN) {
                SHOW_CREDIT_CARD_ACTIVATION_SCREEN = false
                if (Utils.isCreditCardActivationEndpointAvailable())
                    navigateToCreditCardActivation()
                else
                    showCreditCardActivationUnavailableDialog()
            } else if (SHOW_CREDIT_CARD_SHECULE_OR_MANAGE) {
                SHOW_CREDIT_CARD_SHECULE_OR_MANAGE = false
                navigateToScheduleOrManage()
            }
        }
    }

    fun disableShimmer() {
        binding.includeAccountDetailHeaderView.apply {
            cardDetailImageShimmerFrameLayout?.setShimmer(null)
            myCardTextViewShimmerFrameLt?.setShimmer(null)
            tempFreezeTextViewShimmerFrameLayout?.setShimmer(null)
        }
    }

    private fun autoConnectToNetwork() {
        activity?.let { activity ->
            ConnectionBroadcastReceiver.registerToFragmentAndAutoUnregister(activity, this, object : ConnectionBroadcastReceiver() {
                override fun onConnectionChanged(hasConnection: Boolean) {
                    if (hasConnection && !userOfferActiveCallWasCompleted) {
                        retryConnect()
                    }
                }
            })
        }
    }

    private fun retryConnect() {
        activity?.apply {
            if (NetworkManager.getInstance().isConnectedToNetwork(this)) {
                mCardPresenterImpl?.getUserCLIOfferActive()
            } else {
                ErrorHandlerView(this).showToast()
            }
        }
    }

    override fun showStoreCardProgress() {
        binding.apply {
            val shimmer = Shimmer.AlphaHighlightBuilder().build()
            includeAccountDetailHeaderView.includeManageMyCard.manageMyCardTextView?.visibility = VISIBLE
            includeAccountDetailHeaderView.apply {
                cardDetailImageShimmerFrameLayout?.setShimmer(shimmer)
                myCardTextViewShimmerFrameLt?.setShimmer(shimmer)
                tempFreezeTextViewShimmerFrameLayout?.setShimmer(shimmer)
                manageCardGroup?.visibility = GONE
                manageLinkNewCardGroup?.visibility = GONE
                bottomView?.visibility = VISIBLE
                cardDetailImageShimmerFrameLayout?.startShimmer()
                myCardTextViewShimmerFrameLt?.startShimmer()
                tempFreezeTextViewShimmerFrameLayout?.startShimmer()
                storeCardTagTextView?.visibility = GONE
                storeCardLoaderView?.visibility = VISIBLE
                includeManageMyCard.root.isEnabled = false
                cardImageRootView?.isEnabled = false
            }
        }
    }

    @SuppressLint("DefaultLocale")
    override fun hideStoreCardProgress() {
        binding.includeAccountDetailHeaderView.apply {
            storeCardLoaderView?.visibility = GONE
            manageCardGroup?.visibility = VISIBLE
            cardDetailImageShimmerFrameLayout?.stopShimmer()
            cardDetailImageShimmerFrameLayout?.setShimmer(null)
            myCardTextViewShimmerFrameLt?.stopShimmer()
            myCardTextViewShimmerFrameLt?.setShimmer(null)
            tempFreezeTextViewShimmerFrameLayout?.stopShimmer()
            tempFreezeTextViewShimmerFrameLayout?.setShimmer(null)

            cardDetailImageShimmerFrameLayout?.invalidate()
            myCardTextViewShimmerFrameLt?.invalidate()
            tempFreezeTextViewShimmerFrameLayout?.invalidate()

            // Boolean check will enable clickable event only when text is "view card"
            includeManageMyCard.root.isEnabled = true
            cardImageRootView?.isEnabled =
                myCardDetailTextView?.text?.toString()?.toLowerCase()?.contains("view") == true
        }
    }

    override fun handleUnknownHttpCode(description: String?) {
        activity?.supportFragmentManager?.let { fragmentManager -> Utils.showGeneralErrorDialog(fragmentManager, description) }
    }

    override fun handleSessionTimeOut(stsParams: String?) {
        (activity as? AccountSignedInActivity)?.let { accountSignedInActivity -> SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, stsParams, accountSignedInActivity) }
    }

    override fun onClick(v: View?) {
        KotlinUtils.avoidDoubleClicks(v)
        mCardPresenterImpl?.apply {
            when (v?.id) {
                R.id.balanceProtectionInsuranceView -> {
                    if(binding.includeCommonAccountDetails.includeAccountPaymentOption.includeBPICoveredTag.bpiCoveredTextView?.text != bindString(R.string.status_in_progress)){
                        navigateToBalanceProtectionInsurance()
                    }
                }
                R.id.cardImageRootView -> navigateToTemporaryStoreCard()
                R.id.debitOrderView -> navigateToDebitOrderActivity()
                R.id.includeManageMyCard, R.id.cardDetailImageView -> {
                    if (binding.includeAccountDetailHeaderView.cardDetailImageShimmerFrameLayout?.isShimmerVisible == true) return
                    cancelRetrofitRequest(mOfferActiveCall)
                    navigateToTemporaryStoreCard()
                }
                R.id.tvIncreaseLimit, R.id.relIncreaseMyLimit, R.id.llIncreaseLimitContainer -> {
                    val applyNowState = mApplyNowAccountKeyPair?.first

                    if (applyNowState != null) {
                        if (!verifyAppInstanceId()) {
                            activity?.apply { onStartCreditLimitIncreaseFirebaseEvent(this) }
                            creditLimitIncrease()?.nextStep(requireActivity(),getOfferActive(), getProductOfferingId()?.toString(),  applyNowState)
                        }
                    }
                }

                R.id.withdrawCashView, R.id.loanWithdrawalLogoImageView, R.id.withdrawCashTextView -> {
                    KotlinUtils.linkDeviceIfNecessary(activity, ApplyNowState.PERSONAL_LOAN, {
                        PersonalLoanFragment.PL_WITHDRAW_FUNDS_DETAIL = true
                    },{
                        cancelRequest()
                        navigateToLoanWithdrawalActivity()
                    })
                }
                R.id.viewPaymentOptions -> {
                    mCardPresenterImpl?.navigateToPaymentOptionActivity()
                }
                R.id.setUpPaymentPlanOptions -> {
                    openSetupPaymentPlanPage()
                }
                R.id.viewTreatmentPlanOptions -> {
                    openViewTreatmentPlanPage()
                }
                R.id.activateCreditCard -> {
                    handleActivateCreditCard {
                        if (Utils.isCreditCardActivationEndpointAvailable())
                            navigateToCreditCardActivation()
                        else
                            showCreditCardActivationUnavailableDialog()
                    }

                }
                R.id.scheduleOrManageCreditCardDelivery -> {
                    handleScheduleDeliveryCreditCard {
                        if (!verifyAppInstanceId())
                            navigateToScheduleOrManage()
                    }
                }
            }
        }
    }

    private fun handleActivateCreditCard(doCreditActivation: () -> Unit) {
        if(cardWithPLCState?.cardStatus.equals(PLC)){
            KotlinUtils.linkDeviceIfNecessary(activity,
                ApplyNowState.valueOf(
                    mCardPresenterImpl?.mApplyNowAccountKeyPair?.first.toString()),
                {
                    CREDIT_CARD_ACTIVATION_DETAIL = true
                },
                {
                    doCreditActivation()
                })
        }
        else{
            doCreditActivation()
        }
    }
    private fun handleScheduleDeliveryCreditCard(doScheduleOrManage: () -> Unit) {
        KotlinUtils.linkDeviceIfNecessary(activity,
            ApplyNowState.valueOf(
                mCardPresenterImpl?.mApplyNowAccountKeyPair?.first.toString()
            ),
            {
                CREDIT_CARD_SHECULE_OR_MANAGE = true
            },
            {
                doScheduleOrManage()
            })
    }
    private fun AccountCardDetailPresenterImpl.cancelRequest() {
        cancelRetrofitRequest(mOfferActiveCall)
        cancelRetrofitRequest(mStoreCardCall)
    }

    fun navigateToGetStoreCards() {
        activity?.apply {
            if (NetworkManager.getInstance().isConnectedToNetwork(this)) {
                mCardPresenterImpl?.getAccountStoreCardCards()
            } else {
                ErrorHandlerView(this).showToast()
            }
        }
    }

    override fun onDestroy() {
        disposable?.dispose()
        mCardPresenterImpl?.apply {
            onDestroy()
            cancelRequest()
        }
        super.onDestroy()
    }

    override fun navigateToGetTemporaryStoreCardPopupActivity(storeCardResponse: StoreCardsResponse) {
        MyAccountsScreenNavigator.navigateToGetTemporaryStoreCardPopupActivity(activity, storeCardResponse)
    }

    override fun navigateToMyCardDetailActivity(storeCardResponse: StoreCardsResponse, requestUnblockStoreCardCall: Boolean) {
        MyAccountsScreenNavigator.navigateToMyCardDetailActivity(activity, storeCardResponse, requestUnblockStoreCardCall)
    }

    override fun showBalanceProtectionInsurance(insuranceCovered: Boolean?) {
        binding.includeCommonAccountDetails.includeAccountPaymentOption.includeBPICoveredTag.apply {
            when (insuranceCovered) {
                true -> {
                    bpiCoveredTextView?.visibility = VISIBLE
                    bpiNotCoveredGroup?.visibility = GONE
                }
                false -> {
                    bpiCoveredTextView?.visibility = GONE
                    bpiNotCoveredGroup?.visibility = VISIBLE
                }
                else -> {}
            }
        }
    }

    override fun navigateToDebitOrderActivity(debitOrder: DebitOrder) {
        MyAccountsScreenNavigator.navigateToDebitOrderActivity(activity, debitOrder)
    }

    override fun navigateToBalanceProtectionInsuranceApplication(accountInfo: String?, bpiInsuranceStatus: BpiInsuranceApplicationStatusType?) {
        MyAccountsScreenNavigator.navigateToBalanceProtectionInsurance(activity, accountInfo, mCardPresenterImpl?.getAccount(), bpiInsuranceStatus)
    }

    override fun showBalanceProtectionInsuranceLead(bpiInsuranceApplication: BpiInsuranceApplication?) {
        binding.includeCommonAccountDetails.includeAccountPaymentOption.includeBPICoveredTag.apply {
            when (bpiInsuranceApplication?.status) {
                BpiInsuranceApplicationStatusType.COVERED,
                BpiInsuranceApplicationStatusType.OPTED_IN,
                BpiInsuranceApplicationStatusType.NOT_OPTED_IN -> {
                    bpiCoveredTextView?.text = bpiInsuranceApplication.displayLabel
                    KotlinUtils.roundCornerDrawable(
                        bpiCoveredTextView,
                        bpiInsuranceApplication.displayLabelColor
                    )
                    bpiCoveredTextView?.visibility = VISIBLE
                    bpiNotCoveredGroup?.visibility = GONE
                }
                else -> {
                    bpiCoveredTextView?.visibility = GONE
                    bpiNotCoveredGroup?.visibility = VISIBLE
                }
            }
        }
    }

    override fun displayCardHolderName(name: String?) {
        binding.includeAccountDetailHeaderView.userNameTextView?.text = name
    }

    override fun hideUserOfferActiveProgress() {
        binding.apply {
            llIncreaseLimitContainer?.isEnabled = true
            relIncreaseMyLimit?.isEnabled = true
            progressCreditLimit?.visibility = GONE
            tvIncreaseLimit?.visibility = VISIBLE
        }
    }

    override fun showUserOfferActiveProgress() {
        binding.apply {
            cancelRetrofitRequest(mCardPresenterImpl?.mOfferActiveCall)
            llIncreaseLimitContainer?.isEnabled = false
            relIncreaseMyLimit?.isEnabled = false
            progressCreditLimit?.visibility = VISIBLE
            progressCreditLimit?.indeterminateDrawable?.setColorFilter(
                Color.BLACK,
                PorterDuff.Mode.MULTIPLY
            )
            tvApplyNowIncreaseLimit?.visibility = GONE
            tvIncreaseLimit?.visibility = VISIBLE
        }
    }

    override fun disableContentStatusUI() {
        binding.apply {
            relIncreaseMyLimit?.isEnabled = false
            llIncreaseLimitContainer?.isEnabled = false
            tvIncreaseLimit?.isEnabled = false
        }
    }

    override fun enableContentStatusUI() {
        binding.apply {
            relIncreaseMyLimit?.isEnabled = true
            llIncreaseLimitContainer?.isEnabled = true
            tvIncreaseLimit?.isEnabled = true
        }
    }

    override fun handleCreditLimitIncreaseTagStatus(offerActive: OfferActive) {
        binding.apply {
            activity?.runOnUiThread {
                mCardPresenterImpl?.creditLimitIncrease()?.cliStatus(
                    llCommonLayer,
                    tvIncreaseLimit,
                    tvApplyNowIncreaseLimit,
                    tvIncreaseLimitDescription,
                    logoIncreaseLimit,
                    offerActive
                )
            }
        }
    }

    override fun hideProductNotInGoodStanding() {
        binding.apply {
            llIncreaseLimitContainer?.visibility = GONE
            includeCommonAccountDetails.increaseMyLimitSepartorView?.visibility = GONE
        }
    }

    override fun onOfferActiveSuccessResult() {
        userOfferActiveCallWasCompleted = true
    }

    override fun navigateToLoanWithdrawalActivity() {
        activity?.apply {
            Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.personalLoanDrawdownStart, this)
            val intentWithdrawalActivity = Intent(this, LoanWithdrawalActivity::class.java)
            intentWithdrawalActivity.putExtra("account_info", Gson().toJson(mCardPresenterImpl?.getAccount()))
            startActivityForResult(intentWithdrawalActivity, 0)
            overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
        }
    }

    override fun navigateToPaymentOptionActivity() {
        activity?.let { activity -> ActivityIntentNavigationManager.presentPayMyAccountActivity(activity, payMyAccountViewModel.getCardDetail()) }
    }

    override fun navigateToPayMyAccountActivity() {
        activity?.let { activity -> ActivityIntentNavigationManager.presentPayMyAccountActivity(activity, payMyAccountViewModel.getCardDetail()) }
    }

    private fun hideCLIView() {
        binding.apply {
            mCardPresenterImpl?.creditLimitIncrease()
                ?.showCLIProgress(llCommonLayer, tvIncreaseLimitDescription)
        }
    }

    override fun executeCreditCardTokenService() {
        if (!mCardPresenterImpl?.getAccount()?.productGroupCode.equals(AccountsProductGroupCode.CREDIT_CARD.groupCode, true) || mCardPresenterImpl?.getAccount()?.productOfferingGoodStanding != true){
            // WOP-12148 - Hide manage my card option for credit card when productOfferingGoodStanding false
            binding.includeAccountDetailHeaderView.manageCardGroup?.visibility  = GONE
            return
        }

        binding.apply {
            activity?.apply {
                includeAccountDetailHeaderView.root.visibility = GONE
                creditCardActivationView.root.visibility = GONE
                creditCardActivationPlaceHolder.visibility = VISIBLE
                creditCardActivationPlaceHolder.startShimmer()
                mCardPresenterImpl?.getCreditCardToken()
            }
        }
    }

    override fun onGetCreditCArdTokenSuccess(creditCardTokenResponse: CreditCardTokenResponse) {
        creditCardTokenResponse.apply {
            if (cards.isNullOrEmpty()) {
                showGetCreditCardActivationStatus(CreditCardActivationState.ACTIVATED)
            } else {
                cardWithPLCState = mCardPresenterImpl?.getCardWithPLCState(cards)
                cards?.get(0)?.apply {
                    when (envelopeNumber.isNullOrEmpty()) {
                        true -> {
                            when (cardStatus) {
                                "PLC" -> {
                                    when (isPLCInGoodStanding()) {
                                        true -> executeCreditCardDeliveryStatusService()
                                        false -> showGetCreditCardActivationStatus(if (Utils.isCreditCardActivationEndpointAvailable()) CreditCardActivationState.AVAILABLE else CreditCardActivationState.UNAVAILABLE)
                                    }
                                }
                                "AAA" -> showGetCreditCardActivationStatus(CreditCardActivationState.ACTIVATED)
                            }
                        }
                        false -> {
                            // envelope not null, call to get delivery status then show delivery journey()
                            executeCreditCardDeliveryStatusService()
                        }
                    }

                }
            }
        }
    }

    override fun onGetCreditCardTokenFailure() {
        showGetCreditCardActivationStatus(CreditCardActivationState.FAILED)
    }

    override fun showGetCreditCardActivationStatus(status: CreditCardActivationState) {
        binding.apply {
            when (status) {
                CreditCardActivationState.FAILED,
                CreditCardActivationState.ACTIVATED -> {
                    stopCardActivationShimmer()
                    includeAccountDetailHeaderView.root.visibility = VISIBLE
                    includeAccountDetailHeaderView.includeManageMyCard.root.visibility = GONE
                    includeAccountDetailHeaderView.myCardDetailTextView?.visibility = VISIBLE
                }
                CreditCardActivationState.UNAVAILABLE,
                CreditCardActivationState.AVAILABLE -> {
                    stopCardActivationShimmer()
                    creditCardActivationView.root.visibility = VISIBLE
                    creditCardActivationView.activateCreditCard?.visibility = VISIBLE
                    KotlinUtils.roundCornerDrawable(
                        creditCardActivationView.creditCardStatusTextView,
                        if (status == CreditCardActivationState.AVAILABLE) "#bad110" else "#b2b2b2"
                    )
                    creditCardActivationView.creditCardStatusTextView?.text = status.value
                }
            }
        }
    }

    private fun showOnlyCardVisibleState() {
        binding.apply {
            stopCardActivationShimmer()
            includeAccountDetailHeaderView.root.visibility = VISIBLE
            includeAccountDetailHeaderView.includeManageMyCard.root.visibility = GONE
            includeAccountDetailHeaderView.myCardDetailTextView?.visibility = VISIBLE
        }
    }

    override fun stopCardActivationShimmer() {
        binding.apply {
            creditCardActivationPlaceHolder?.apply {
                stopShimmer()
                visibility = GONE
            }
        }
    }

    private fun navigateToCreditCardActivation() {
        activity?.apply {
            Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.CC_ACTIVATE_NEW_CARD, hashMapOf(Pair(ACTION_LOWER_CASE, activationInitiated)), this)
            val mIntent = Intent(this, CreditCardActivationActivity::class.java)
            val mBundle = Bundle()
            mBundle.putString("absaCardToken", cardWithPLCState?.absaCardToken)
            mBundle.putString(BundleKeysConstants.PRODUCT_OFFERINGID, mCardPresenterImpl?.getAccount()?.productOfferingId.toString())
            mIntent.putExtra("bundle", mBundle)
            startActivityForResult(mIntent, REQUEST_CREDIT_CARD_ACTIVATION)
            overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
        }
    }

    private fun navigateToScheduleOrManage() {
        activity?.apply {
            val intent = Intent(this, CreditCardDeliveryActivity::class.java)
            val mBundle = Bundle()
            mBundle.putString(BundleKeysConstants.ENVELOPE_NUMBER, cardWithPLCState?.envelopeNumber)
            mBundle.putString(
                BundleKeysConstants.ACCOUNTBI_NNUMBER,
                mCardPresenterImpl?.getAccount()?.accountNumberBin
            )
            mBundle.putParcelable(
                BundleKeysConstants.STATUS_RESPONSE,
                creditCardDeliveryStatusResponse?.statusResponse
            )
            mBundle.putString(
                BundleKeysConstants.PRODUCT_OFFERINGID,
                mCardPresenterImpl?.getAccount()?.productOfferingId.toString()
            )
            mBundle.putSerializable(
                AccountSignedInPresenterImpl.APPLY_NOW_STATE,
                mCardPresenterImpl?.mApplyNowAccountKeyPair?.first
            )
            intent.putExtra(BundleKeysConstants.BUNDLE, mBundle)
            startActivity(intent)
        }
    }
    private fun showCreditCardActivationUnavailableDialog() {
        activity?.supportFragmentManager?.let { CreditCardActivationAvailabilityDialogFragment.newInstance(mCardPresenterImpl?.getAccount()?.accountNumberBin).show(it, CreditCardActivationAvailabilityDialogFragment::class.java.simpleName) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode){
            AppConstant.BALANCE_PROTECTION_INSURANCE_REQUEST_CODE -> {
                if (resultCode == AppConstant.BALANCE_PROTECTION_INSURANCE_OPT_IN_SUCCESS_RESULT_CODE){
                    val extras = data?.extras
                    val response  = extras?.getString(BalanceProtectionInsuranceActivity.ACCOUNT_RESPONSE)
                    val accounts = Gson().fromJson(response, Account::class.java)
                    mCardPresenterImpl?.apply {
                        showAccount(accounts)
                    }
                }
            }
            REQUEST_CREDIT_CARD_ACTIVATION -> {
                if (resultCode == RESULT_OK) {
                    executeCreditCardTokenService()
                }
            }
            REQUEST_GET_PAYMENT_PLAN -> {
                if (resultCode == RESULT_OK) {
                    onTreatmentPlanStatusUpdateRequired()
                }
            }
        }
    }

    private fun onTreatmentPlanStatusUpdateRequired() {
        (activity as? AccountSignedInActivity)?.let { it.onTreatmentPlanStatusUpdateRequired() }
    }

    private fun showAccount(accounts: Account?) {
        mCardPresenterImpl?.refreshAccount(accounts)
    }

    private fun initCreditCardActivation() {
        AppConfigSingleton.creditCardActivation?.apply {
            if (isEnabled) {
                executeCreditCardTokenService()
            }
        }
    }

    override fun executeCreditCardDeliveryStatusService() {
        activity?.apply {
            mCardPresenterImpl?.getCreditCardDeliveryStatus(cardWithPLCState?.envelopeNumber)
        }
    }

    override fun onGetCreditCardDeliveryStatusSuccess(creditCardDeliveryStatusResponse: CreditCardDeliveryStatusResponse) {
        this.creditCardDeliveryStatusResponse = creditCardDeliveryStatusResponse
        when (creditCardDeliveryStatusResponse.statusResponse?.deliveryStatus?.statusDescription?.asEnumOrDefault(DEFAULT)) {
            CARD_DELIVERED -> {
                if (cardWithPLCState?.cardStatus.equals("AAA")) {
                    showOnlyCardVisibleState()
                } else {
                    creditCardDeliveryStatusResponse.statusResponse?.deliveryStatus?.let { showGetCreditCardDeliveryStatus(it) }
                }
            }
            CARD_NOT_RECEIVED, AWAITING_INSTRUCTION -> {
                showOnlyCardVisibleState()
            }
            else -> {
                creditCardDeliveryStatusResponse.statusResponse?.deliveryStatus?.let { showGetCreditCardDeliveryStatus(it) }
            }
        }
    }

    override fun onGetCreditCardDeliveryStatusFailure() {
        binding.apply {
            stopCardActivationShimmer()
            includeAccountDetailHeaderView.root.visibility = VISIBLE
            includeAccountDetailHeaderView.includeManageMyCard.root.visibility = GONE
            includeAccountDetailHeaderView. myCardDetailTextView?.visibility = VISIBLE
        }
    }

    override fun showGetCreditCardDeliveryStatus(deliveryStatus: DeliveryStatus) {
        when (deliveryStatus.statusDescription?.asEnumOrDefault(DEFAULT)) {
            CARD_RECEIVED -> {
                showScheduleYourDelivery()
            }
            CARD_DELIVERED -> {
                showGetCreditCardActivationStatus(CreditCardActivationState.AVAILABLE)
            }
            APPOINTMENT_SCHEDULED, CANCELLED, CARD_SHREDDED -> {
                showManageMyDelivery()
            }
            AWAITING_INSTRUCTION -> {
                showDefaultCreditCardStatusView()
            }
            CARD_NOT_RECEIVED -> {
                showGetCreditCardActivationStatus(CreditCardActivationState.AVAILABLE)
            }
            else -> {
                showDefaultCreditCardStatusView()
            }
        }

        binding.creditCardActivationView.apply {
            deliveryStatus.apply {
                if (!statusDescription.isNullOrEmpty() && !displayColour.isNullOrEmpty()) {
                    KotlinUtils.roundCornerDrawable(creditCardStatusTextView, displayColour)
                    creditCardStatusTextView?.text = displayTitle
                } else if (!statusDescription.isNullOrEmpty() && (deliveryStatus.statusDescription?.equals(
                        CARD_DELIVERED.name
                    ) == true)
                ) {
                    KotlinUtils.roundCornerDrawable(creditCardStatusTextView, "#bad110")
                    creditCardStatusTextView?.text = bindString(R.string.activate)
                } else creditCardStatusTextView?.visibility = INVISIBLE
            }
        }
    }

    private fun isPLCInGoodStanding(): Boolean {
        var isEnable = false
        if (!cardWithPLCState?.envelopeNumber.isNullOrEmpty()) {
            val cardTypes: List<ConfigCreditCardDeliveryCardTypes> = AppConfigSingleton.creditCardDelivery?.cardTypes ?: arrayListOf()
            for ((binNumber, minimumSupportedAppBuildNumber) in cardTypes) {
                if (binNumber.equals(mCardPresenterImpl?.getAccount()?.accountNumberBin, ignoreCase = true)
                    && Utils.isFeatureEnabled(minimumSupportedAppBuildNumber)) {
                    isEnable = true
                }
            }
        }
        return isEnable
    }

    private fun showManageMyDelivery() {
        binding.apply {
            stopCardActivationShimmer()
            creditCardActivationView.root.visibility = VISIBLE
            creditCardActivationView.scheduleOrManageCreditCardDelivery?.visibility = VISIBLE
            creditCardActivationView.tvScheduleOrMangeDelivery?.text = bindString(R.string.manage_my_delivery)
        }
    }

    private fun showDefaultCreditCardStatusView() {
        binding.apply {
            stopCardActivationShimmer()
            includeAccountDetailHeaderView.root.visibility = VISIBLE
        }
    }

    private fun showScheduleYourDelivery() {
        binding.apply {
            stopCardActivationShimmer()
            creditCardActivationView.root.visibility = VISIBLE
            creditCardActivationView.scheduleOrManageCreditCardDelivery?.visibility = VISIBLE
            creditCardActivationView.tvScheduleOrMangeDelivery?.text = bindString(R.string.schedule_your_delivery)
        }
    }

    fun hideTreatmentPlanButtons() {
        binding.includeCommonAccountDetails.includeAccountPaymentOption.apply {
            setUpPaymentPlanGroup?.visibility = GONE
            viewTreatmentPlanGroup?.visibility = GONE
        }
    }

    fun showSetUpPaymentPlanButton(state: ApplyNowState,
                                   eligibilityPlan: EligibilityPlan?) {
        binding.includeCommonAccountDetails.includeAccountPaymentOption.apply {
            setUpPaymentPlanGroup?.visibility = VISIBLE
            setUpPaymentPlanTextView?.text = eligibilityPlan?.displayText
        }

        this.state = state
        this.eligibilityPlan = eligibilityPlan
    }

    fun showViewTreatmentPlanButton(state: ApplyNowState, eligibilityPlan: EligibilityPlan?) {
        binding.includeCommonAccountDetails.includeAccountPaymentOption.apply {
            viewTreatmentPlanGroup?.visibility = VISIBLE
            viewTreatmentPlanTextView?.text = eligibilityPlan?.displayText
        }

        this.state = state
        this.eligibilityPlan = eligibilityPlan
    }

    private fun openSetupPaymentPlanPage() {
        activity?.apply {
            val intent = Intent(context, GetAPaymentPlanActivity::class.java)
            intent.putExtra(ViewTreatmentPlanDialogFragment.ELIGIBILITY_PLAN, eligibilityPlan)
            startActivityForResult(intent, REQUEST_GET_PAYMENT_PLAN)
            overridePendingTransition(R.anim.slide_from_right, R.anim.stay)
        }
    }


    private fun openViewTreatmentPlanPage(){
        val productGroupCode : ProductGroupCode = when(state){
            ApplyNowState.STORE_CARD -> ProductGroupCode.SC
            ApplyNowState.PERSONAL_LOAN -> ProductGroupCode.PL
            else -> ProductGroupCode.CC
        }
        val outSystemBuilder = OutSystemBuilder(activity,productGroupCode, eligibilityPlan)
        outSystemBuilder.build()
    }
}


