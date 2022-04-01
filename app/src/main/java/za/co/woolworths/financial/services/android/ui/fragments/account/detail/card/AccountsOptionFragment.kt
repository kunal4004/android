package za.co.woolworths.financial.services.android.ui.fragments.account.detail.card

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.awfs.coordination.R
import com.facebook.shimmer.Shimmer
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.account_activate_credit_card_layout.*
import kotlinx.android.synthetic.main.account_card_detail_fragment.*
import kotlinx.android.synthetic.main.account_cart_item.*
import kotlinx.android.synthetic.main.account_detail_header_fragment.*
import kotlinx.android.synthetic.main.account_options_layout.*
import kotlinx.android.synthetic.main.bpi_covered_tag_layout.*
import kotlinx.android.synthetic.main.common_account_detail.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.PropertyNames.Companion.ACTION_LOWER_CASE
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.PropertyNames.Companion.activationInitiated
import za.co.woolworths.financial.services.android.contracts.IAccountCardDetailsContract
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.models.dto.account.BpiInsuranceApplication
import za.co.woolworths.financial.services.android.models.dto.account.BpiInsuranceApplicationStatusType
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.dto.account.CreditCardActivationState
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
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.MyAccountsRemoteApiViewModel
import za.co.woolworths.financial.services.android.ui.activities.credit_card_delivery.CreditCardDeliveryActivity
import za.co.woolworths.financial.services.android.ui.activities.loan.LoanWithdrawalActivity
import za.co.woolworths.financial.services.android.ui.extension.asEnumOrDefault
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.cancelRetrofitRequest
import za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.AvailableFundFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.MyAccountsFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.personal_loan.PersonalLoanFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.MyAccountsScreenNavigator
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.BalanceProtectionInsuranceActivity

import za.co.woolworths.financial.services.android.ui.fragments.credit_card_activation.CreditCardActivationAvailabilityDialogFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.ViewTreatmentPlanDialogFragment
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
import za.co.woolworths.financial.services.android.util.wenum.VocTriggerEvent

open class AccountsOptionFragment : Fragment(), OnClickListener, IAccountCardDetailsContract.AccountCardDetailView {


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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.account_card_detail_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        balanceProtectionInsuranceView?.setOnClickListener(this)
        cardImageRootView?.setOnClickListener(this)
        debitOrderView?.setOnClickListener(this)
        tvIncreaseLimit?.setOnClickListener(this)
        relIncreaseMyLimit?.setOnClickListener(this)
        llIncreaseLimitContainer?.setOnClickListener(this)
        withdrawCashView?.setOnClickListener(this)
        viewPaymentOptions?.setOnClickListener(this)
        setUpPaymentPlanOptions?.setOnClickListener(this)
        viewTreatmentPlanOptions?.setOnClickListener(this)
        activateCreditCard?.setOnClickListener(this)
        scheduleOrManageCreditCardDelivery?.setOnClickListener(this)
        AnimationUtilExtension.animateViewPushDown(cardDetailImageView)

        mCardPresenterImpl?.apply {
            getBpiInsuranceApplication()
            displayCardHolderName()
            creditLimitIncrease()?.showCLIProgress(logoIncreaseLimit, llCommonLayer, tvIncreaseLimit)
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

    override fun onResume() {
        super.onResume()
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

        if(PersonalLoanFragment.SHOW_PL_WITHDRAW_FUNDS_SCREEN){
            PersonalLoanFragment.SHOW_PL_WITHDRAW_FUNDS_SCREEN = false
            mCardPresenterImpl?.apply {
                cancelRequest()
                navigateToLoanWithdrawalActivity()
            }
        }
        else if (SHOW_CREDIT_CARD_ACTIVATION_SCREEN) {
            SHOW_CREDIT_CARD_ACTIVATION_SCREEN = false
            if (Utils.isCreditCardActivationEndpointAvailable())
                navigateToCreditCardActivation()
            else
                showCreditCardActivationUnavailableDialog()
        }else if (SHOW_CREDIT_CARD_SHECULE_OR_MANAGE){
            SHOW_CREDIT_CARD_SHECULE_OR_MANAGE = false
            navigateToScheduleOrManage()
        }
    }

    fun disableShimmer() {
        cardDetailImageShimmerFrameLayout?.setShimmer(null)
        myCardTextViewShimmerFrameLayout?.setShimmer(null)
        tempFreezeTextViewShimmerFrameLayout?.setShimmer(null)
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
        val shimmer = Shimmer.AlphaHighlightBuilder().build()
        cardDetailImageShimmerFrameLayout?.setShimmer(shimmer)
        myCardTextViewShimmerFrameLayout?.setShimmer(shimmer)
        tempFreezeTextViewShimmerFrameLayout?.setShimmer(shimmer)
        manageCardGroup?.visibility = GONE
        manageLinkNewCardGroup?.visibility = GONE
        bottomView?.visibility = VISIBLE
        manageMyCardTextView?.visibility = VISIBLE
        cardDetailImageShimmerFrameLayout?.startShimmer()
        myCardTextViewShimmerFrameLayout?.startShimmer()
        tempFreezeTextViewShimmerFrameLayout?.startShimmer()
        storeCardTagTextView?.visibility = GONE
        storeCardLoaderView?.visibility = VISIBLE
        includeManageMyCard?.isEnabled = false
        cardImageRootView?.isEnabled = false
    }

    @SuppressLint("DefaultLocale")
    override fun hideStoreCardProgress() {
        storeCardLoaderView?.visibility = GONE
        manageCardGroup?.visibility = VISIBLE
        cardDetailImageShimmerFrameLayout?.stopShimmer()
        cardDetailImageShimmerFrameLayout?.setShimmer(null)
        myCardTextViewShimmerFrameLayout?.stopShimmer()
        myCardTextViewShimmerFrameLayout?.setShimmer(null)
        tempFreezeTextViewShimmerFrameLayout?.stopShimmer()
        tempFreezeTextViewShimmerFrameLayout?.setShimmer(null)

        cardDetailImageShimmerFrameLayout?.invalidate()
        myCardTextViewShimmerFrameLayout?.invalidate()
        tempFreezeTextViewShimmerFrameLayout?.invalidate()

        // Boolean check will enable clickable event only when text is "view card"
        includeManageMyCard?.isEnabled = true
        cardImageRootView?.isEnabled = myCardDetailTextView?.text?.toString()?.toLowerCase()?.contains("view") == true
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
                    if(bpiCoveredTextView?.text != bindString(R.string.status_in_progress)){
                        navigateToBalanceProtectionInsurance()
                    }
                }
                R.id.cardImageRootView -> navigateToTemporaryStoreCard()
                R.id.debitOrderView -> navigateToDebitOrderActivity()
                R.id.includeManageMyCard, R.id.cardDetailImageView -> {
                    if (cardDetailImageShimmerFrameLayout?.isShimmerVisible == true) return
                    cancelRetrofitRequest(mOfferActiveCall)
                    navigateToTemporaryStoreCard()
                }
                R.id.tvIncreaseLimit, R.id.relIncreaseMyLimit, R.id.llIncreaseLimitContainer -> {
                    val applyNowState = mApplyNowAccountKeyPair?.first

                    if (applyNowState != null) {
                        if (!MyAccountsFragment.verifyAppInstanceId()) {
                            activity?.apply { onStartCreditLimitIncreaseFirebaseEvent(this) }
                            creditLimitIncrease()?.nextStep(getOfferActive(), getProductOfferingId()?.toString(),  applyNowState)
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
                        if (!MyAccountsFragment.verifyAppInstanceId())
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
        when (insuranceCovered){
            true -> {
                bpiCoveredTextView?.visibility = VISIBLE
                bpiNotCoveredGroup?.visibility = GONE
            }
            false -> {
                bpiCoveredTextView?.visibility = GONE
                bpiNotCoveredGroup?.visibility = VISIBLE
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
        when (bpiInsuranceApplication?.status) {
            BpiInsuranceApplicationStatusType.COVERED ,
            BpiInsuranceApplicationStatusType.OPTED_IN,
            BpiInsuranceApplicationStatusType.NOT_OPTED_IN-> {
                bpiCoveredTextView?.text = bpiInsuranceApplication.displayLabel
                KotlinUtils.roundCornerDrawable(bpiCoveredTextView, bpiInsuranceApplication.displayLabelColor)
                bpiCoveredTextView?.visibility = VISIBLE
                bpiNotCoveredGroup?.visibility = GONE
            }
            else  -> {
                bpiCoveredTextView?.visibility = GONE
                bpiNotCoveredGroup?.visibility = VISIBLE
            }
        }
    }

    override fun displayCardHolderName(name: String?) {
        userNameTextView?.text = name
    }

    override fun hideUserOfferActiveProgress() {
        llIncreaseLimitContainer?.isEnabled = true
        relIncreaseMyLimit?.isEnabled = true
        progressCreditLimit?.visibility = GONE
        tvIncreaseLimit?.visibility = VISIBLE
    }

    override fun showUserOfferActiveProgress() {
        cancelRetrofitRequest(mCardPresenterImpl?.mOfferActiveCall)
        llIncreaseLimitContainer?.isEnabled = false
        relIncreaseMyLimit?.isEnabled = false
        progressCreditLimit?.visibility = VISIBLE
        progressCreditLimit?.indeterminateDrawable?.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY)
        tvApplyNowIncreaseLimit?.visibility = GONE
        tvIncreaseLimit?.visibility = VISIBLE
    }

    override fun disableContentStatusUI() {
        relIncreaseMyLimit?.isEnabled = false
        llIncreaseLimitContainer?.isEnabled = false
        tvIncreaseLimit?.isEnabled = false
    }

    override fun enableContentStatusUI() {
        relIncreaseMyLimit?.isEnabled = true
        llIncreaseLimitContainer?.isEnabled = true
        tvIncreaseLimit?.isEnabled = true
    }

    override fun handleCreditLimitIncreaseTagStatus(offerActive: OfferActive) {
        activity?.runOnUiThread { mCardPresenterImpl?.creditLimitIncrease()?.cliStatus(llCommonLayer, tvIncreaseLimit, tvApplyNowIncreaseLimit, tvIncreaseLimitDescription, logoIncreaseLimit, offerActive) }
    }

    override fun hideProductNotInGoodStanding() {
        llIncreaseLimitContainer?.visibility = GONE
        increaseMyLimitSepartorView?.visibility = GONE
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
        mCardPresenterImpl?.creditLimitIncrease()?.showCLIProgress(llCommonLayer, tvIncreaseLimitDescription)
    }

    override fun executeCreditCardTokenService() {
        if (!mCardPresenterImpl?.getAccount()?.productGroupCode.equals(AccountsProductGroupCode.CREDIT_CARD.groupCode, true) || mCardPresenterImpl?.getAccount()?.productOfferingGoodStanding != true){
            // WOP-12148 - Hide manage my card option for credit card when productOfferingGoodStanding false
            manageCardGroup?.visibility  = GONE
            return
        }

        activity?.apply {
            includeAccountDetailHeaderView?.visibility = GONE
            creditCardActivationView?.visibility = GONE
            creditCardActivationPlaceHolder?.visibility = VISIBLE
            creditCardActivationPlaceHolder?.startShimmer()
            mCardPresenterImpl?.getCreditCardToken()
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
        when (status) {
            CreditCardActivationState.FAILED,
            CreditCardActivationState.ACTIVATED -> {
                stopCardActivationShimmer()
                includeAccountDetailHeaderView?.visibility = VISIBLE
                includeManageMyCard?.visibility = GONE
                myCardDetailTextView?.visibility = VISIBLE
            }
            CreditCardActivationState.UNAVAILABLE,
            CreditCardActivationState.AVAILABLE -> {
                stopCardActivationShimmer()
                creditCardActivationView?.visibility = VISIBLE
                activateCreditCard?.visibility = VISIBLE
                KotlinUtils.roundCornerDrawable(creditCardStatusTextView, if (status == CreditCardActivationState.AVAILABLE) "#bad110" else "#b2b2b2")
                creditCardStatusTextView?.text = status.value
            }
        }
    }

    private fun showOnlyCardVisibleState() {
        stopCardActivationShimmer()
        includeAccountDetailHeaderView?.visibility = VISIBLE
        includeManageMyCard?.visibility = GONE
        myCardDetailTextView?.visibility = VISIBLE
    }

    override fun stopCardActivationShimmer() {
        creditCardActivationPlaceHolder?.apply {
            stopShimmer()
            visibility = GONE
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
        stopCardActivationShimmer()
        includeAccountDetailHeaderView?.visibility = VISIBLE
        includeManageMyCard?.visibility = GONE
        myCardDetailTextView?.visibility = VISIBLE
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


        deliveryStatus.apply {
            if (!statusDescription.isNullOrEmpty() && !displayColour.isNullOrEmpty()) {
                KotlinUtils.roundCornerDrawable(creditCardStatusTextView, displayColour)
                creditCardStatusTextView?.text = displayTitle
            } else if (!statusDescription.isNullOrEmpty() && (deliveryStatus.statusDescription?.equals(CARD_DELIVERED.name) == true)) {
                KotlinUtils.roundCornerDrawable(creditCardStatusTextView, "#bad110")
                creditCardStatusTextView?.text = bindString(R.string.activate)
            } else creditCardStatusTextView?.visibility = INVISIBLE
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
        stopCardActivationShimmer()
        creditCardActivationView?.visibility = VISIBLE
        scheduleOrManageCreditCardDelivery?.visibility = VISIBLE
        tvScheduleOrMangeDelivery?.text = bindString(R.string.manage_my_delivery)
    }

    private fun showDefaultCreditCardStatusView() {
        stopCardActivationShimmer()
        includeAccountDetailHeaderView?.visibility = VISIBLE
    }

    private fun showScheduleYourDelivery() {
        stopCardActivationShimmer()
        creditCardActivationView?.visibility = VISIBLE
        scheduleOrManageCreditCardDelivery?.visibility = VISIBLE
        tvScheduleOrMangeDelivery?.text = bindString(R.string.schedule_your_delivery)
    }

    fun hideTreatmentPlanButtons() {
        setUpPaymentPlanGroup?.visibility = GONE
        viewTreatmentPlanGroup?.visibility = GONE
    }

    fun showSetUpPaymentPlanButton(state: ApplyNowState,
                                   eligibilityPlan: EligibilityPlan?) {
        setUpPaymentPlanGroup?.visibility = VISIBLE
        setUpPaymentPlanTextView?.text = eligibilityPlan?.displayText

        this.state = state
        this.eligibilityPlan = eligibilityPlan
    }

    fun showViewTreatmentPlanButton(state: ApplyNowState, eligibilityPlan: EligibilityPlan?) {
        viewTreatmentPlanGroup?.visibility = VISIBLE
        viewTreatmentPlanTextView?.text = eligibilityPlan?.displayText

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


