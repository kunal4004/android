package za.co.woolworths.financial.services.android.ui.fragments.account.available_fund

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Guideline
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AvailableFundsFragmentBinding
import com.facebook.shimmer.Shimmer
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IAvailableFundsContract
import za.co.woolworths.financial.services.android.contracts.IBottomSheetBehaviourPeekHeightListener
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.PMACardPopupModel
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.models.dto.app_config.ConfigPayMyAccount
import za.co.woolworths.financial.services.android.ui.activities.ABSAOnlineBankingRegistrationActivity
import za.co.woolworths.financial.services.android.ui.activities.GetAPaymentPlanActivity
import za.co.woolworths.financial.services.android.ui.activities.StatementActivity
import za.co.woolworths.financial.services.android.ui.activities.WTransactionsActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity.Companion.ABSA_ONLINE_BANKING_REGISTRATION_REQUEST_CODE
import za.co.woolworths.financial.services.android.ui.activities.loan.LoanWithdrawalActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.doAfterDelay
import za.co.woolworths.financial.services.android.ui.extension.navigateSafelyWithNavController
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatFloatingActionButtonBubbleView
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatFragment.Companion.ACCOUNTS
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.card.AccountsOptionFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.helper.FirebaseEventDetailManager
import za.co.woolworths.financial.services.android.ui.views.actionsheet.ErrorMessageDialogWithTitleFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.AccountInArrearsDialogFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.AccountInArrearsDialogFragment.Companion.ARREARS_CHAT_TO_US_BUTTON
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.AccountInArrearsDialogFragment.Companion.ARREARS_PAY_NOW_BUTTON
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.ViewTreatmentPlanDialogFragment
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.DP_LINKING_MY_ACCOUNTS_PRODUCT_PAY_MY_ACCOUNT
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.DP_LINKING_MY_ACCOUNTS_PRODUCT_STATEMENT
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
import java.net.ConnectException

open class AvailableFundFragment : Fragment(R.layout.available_funds_fragment), IAvailableFundsContract.AvailableFundsView {

    protected lateinit var binding: AvailableFundsFragmentBinding
    private lateinit var mAvailableFundPresenter: AvailableFundsPresenterImpl
    private lateinit var bottomSheetBehaviourPeekHeightListener: IBottomSheetBehaviourPeekHeightListener
    var isQueryPayUPaymentMethodComplete: Boolean = false
    lateinit var navController: NavController

    val payMyAccountViewModel: PayMyAccountViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAvailableFundPresenter = AvailableFundsPresenterImpl(this, AvailableFundsModelImpl())
        mAvailableFundPresenter.setBundle(arguments)
    }

    companion object {
        const val WEBVIEW = "webview"
        const val NATIVE_BROWSER = "nativeBrowser"
    }

    @Throws(RuntimeException::class)
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is IBottomSheetBehaviourPeekHeightListener) {
            bottomSheetBehaviourPeekHeightListener = context
        } else {
            throw RuntimeException("AvailableFundsFragment context value $context must implement BottomSheetBehaviourPeekHeightListener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.available_funds_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = AvailableFundsFragmentBinding.bind(view)

        binding.apply {
            setUpView()

            setPushViewDownAnimation(incRecentTransactionButton.root)
            setPushViewDownAnimation(incViewStatementButton.root)
            setPushViewDownAnimation(incPayMyAccountButton.root)

            val bottomViewGuideline = view.findViewById<Guideline>(R.id.bottomSliderGuideline)
            val constParam: ConstraintLayout.LayoutParams =
                bottomViewGuideline.layoutParams as ConstraintLayout.LayoutParams

            val sliderGuidelineArrearsTypeValue = TypedValue()
            activity?.resources?.getValue(
                R.dimen.slider_guideline_percent_for_arrears_account_product,
                sliderGuidelineArrearsTypeValue,
                true
            )
            val sliderGuidelineForArrears: Float = sliderGuidelineArrearsTypeValue.float

            val sliderGuidelineTypeValue = TypedValue()
            activity?.resources?.getValue(
                R.dimen.slider_guideline_percent_for_account_product,
                sliderGuidelineTypeValue,
                true
            )
            val sliderGuidelineForGoodStanding: Float = sliderGuidelineTypeValue.float

            constParam.guidePercent =
                if ((activity as? AccountSignedInActivity)?.mAccountSignedInPresenter?.isAccountInArrearsState() == true) {
                    paymentOverdueGroup?.visibility = VISIBLE
                    sliderGuidelineForArrears
                } else {
                    paymentOverdueGroup?.visibility = INVISIBLE
                    sliderGuidelineForGoodStanding
                }
            bottomViewGuideline.layoutParams = constParam

            availableFundBackground?.post {
                val dm = DisplayMetrics()
                (activity as? AppCompatActivity)?.windowManager?.defaultDisplay?.getMetrics(dm)
                val deviceHeight = dm.heightPixels
                val location = IntArray(2)
                bottomSliderGuideline?.getLocationOnScreen(location)
                val bottomGuidelineVerticalPosition = location[1]
                val displayBottomSheetBehaviorWithinRemainingHeight =
                    deviceHeight - bottomGuidelineVerticalPosition + Utils.dp2px(20f)
                bottomSheetBehaviourPeekHeightListener?.onBottomSheetPeekHeight(
                    displayBottomSheetBehaviorWithinRemainingHeight
                )

            }
        }

        activity?.let { act ->
            ConnectionBroadcastReceiver.registerToFragmentAndAutoUnregister(act, this, object : ConnectionBroadcastReceiver() {
                override fun onConnectionChanged(hasConnection: Boolean) {
                    when (hasConnection || !isQueryPayUPaymentMethodComplete) {
                        true -> queryPaymentMethod()
                        else -> ErrorHandlerView(act).showToast()
                    }

                }
            })
        }

        payMyAccountViewModel.getNavigationResult().observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { result ->
                when (result) {
                    PayMyAccountViewModel.OnNavigateBack.Retry -> {
                        activity?.runOnUiThread {
                            payMyAccountViewModel.isQueryPayUPaymentMethodComplete = false
                            queryPaymentMethod()
                        }
                    }
                    else -> return@observe
                }
            }
        }
    }

    fun queryPaymentMethod() {
        when (!payMyAccountViewModel.isQueryPayUPaymentMethodComplete) {
            true -> {
                binding.initShimmer()
                binding.startProgress()

                val cardInfo = payMyAccountViewModel.getCardDetail()
                val account = mAvailableFundPresenter.getAccountDetail()
                val amountEntered = account?.second?.amountOverdue?.let { amountDue -> Utils.removeNegativeSymbol(CurrencyFormatter.formatAmountToRandAndCent(amountDue)) }
                val payUMethodType = PayMyAccountViewModel.PAYUMethodType.CREATE_USER
                val paymentMethodList = cardInfo?.paymentMethodList

                val card = PMACardPopupModel(amountEntered, paymentMethodList, account, payUMethodType)
                payMyAccountViewModel.setPMACardInfo(card)

                payMyAccountViewModel.queryServicePayUPaymentMethod(
                    { // onSuccessResult
                        if (!isAdded) return@queryServicePayUPaymentMethod
                        binding.stopProgress()
                        (activity as? AccountSignedInActivity)?.mAccountSignedInPresenter?.pmaStatusImpl?.pmaSuccess()
                        payMyAccountViewModel.isQueryPayUPaymentMethodComplete = true
                        navigateToDeepLinkView(DP_LINKING_MY_ACCOUNTS_PRODUCT_PAY_MY_ACCOUNT, binding.incPayMyAccountButton.root)
                    }, { onSessionExpired ->
                        if (!isAdded) return@queryServicePayUPaymentMethod
                        activity?.let {
                            binding.stopProgress()
                            payMyAccountViewModel.isQueryPayUPaymentMethodComplete = true
                            SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, onSessionExpired, it)

                        }
                    }, { // on unknown http error / general error
                        if (!isAdded) return@queryServicePayUPaymentMethod
                        binding.stopProgress()
                        payMyAccountViewModel.isQueryPayUPaymentMethodComplete = true

                    }, { throwable ->
                        if (!isAdded) return@queryServicePayUPaymentMethod
                        activity?.runOnUiThread {
                            binding.stopProgress()
                        }
                        payMyAccountViewModel.isQueryPayUPaymentMethodComplete = true
                        if (throwable is ConnectException) {
                            payMyAccountViewModel.isQueryPayUPaymentMethodComplete = false
                        }
                    })
            }
            false -> return
        }
    }


    override fun setPushViewDownAnimation(view: View) {
        AnimationUtilExtension.animateViewPushDown(view)
    }

    override fun onABSACreditCardFailureHandler(error: Throwable?) {
        activity?.let { activity ->
            activity.runOnUiThread {
                if (error is ConnectException) {
                    ErrorHandlerView(activity).showToast()
                }
            }
        }
    }

    private fun setUpView() {
        mAvailableFundPresenter.getAccount()?.apply {
            activity?.apply {
                val availableFund = Utils.removeNegativeSymbol(FontHyperTextParser.getSpannable(CurrencyFormatter.formatAmountToRandAndCentNoSpace(availableFunds), 1))
                val currentBalance = Utils.removeNegativeSymbol(CurrencyFormatter.formatAmountToRandAndCentWithSpace(currentBalance))
                val creditLimit = Utils.removeNegativeSymbol(FontHyperTextParser.getSpannable(CurrencyFormatter.formatAmountToRandAndCentWithSpace(creditLimit), 1))
                val paymentDueDate = paymentDueDate?.let { paymentDueDate -> WFormatter.addSpaceToDate(WFormatter.newDateFormat(paymentDueDate)) }
                    ?: "N/A"
                val amountOverdue = Utils.removeNegativeSymbol(FontHyperTextParser.getSpannable(CurrencyFormatter.formatAmountToRandAndCentWithSpace(amountOverdue), 1))

                val totalAmountDueAmount = Utils.removeNegativeSymbol(CurrencyFormatter.formatAmountToRandAndCentWithSpace(totalAmountDue))

                binding.apply {
                    availableFundAmountTextView?.text = availableFund
                    currentBalanceAmountTextView?.text = currentBalance
                    creditLimitAmountTextView?.text = creditLimit
                    totalAmountDueAmountTextView?.text = totalAmountDueAmount
                    nextPaymentDueDateTextView?.text = paymentDueDate
                    amountPayableNowAmountTextView?.text = amountOverdue
                }
            }
        }
    }

    override fun navigateToStatementActivity() {
        if (fragmentAlreadyAdded()) return
        activity?.apply {
            val openStatement = Intent(this, StatementActivity::class.java)
            openStatement.putExtra(ACCOUNTS, Gson().toJson(Pair(mAvailableFundPresenter?.getApplyNowState(), mAvailableFundPresenter?.getAccount())))
            startActivity(openStatement)
            overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
        }
    }

    override fun navigateToPaymentOptionsActivity() {
        activity?.let { activity -> ActivityIntentNavigationManager.presentPayMyAccountActivity(activity, payMyAccountViewModel.getCardDetail()) }
    }

    override fun navigateToPayMyAccountActivity() {
        if (fragmentAlreadyAdded()) return
        activity?.let { activity -> ActivityIntentNavigationManager.presentPayMyAccountActivity(activity, payMyAccountViewModel.getCardDetail()) }
    }

    override fun navigateToOnlineBankingActivity(creditCardNumber: String?, isRegistered: Boolean) {
        if (fragmentAlreadyAdded()) return
        activity?.apply {
            val openABSAOnlineBanking = Intent(this, ABSAOnlineBankingRegistrationActivity::class.java)
            openABSAOnlineBanking.putExtra(ABSAOnlineBankingRegistrationActivity.SHOULD_DISPLAY_LOGIN_SCREEN, isRegistered)
            openABSAOnlineBanking.putExtra("creditCardToken", creditCardNumber)
            openABSAOnlineBanking.putExtra(ACCOUNTS, Gson().toJson(mAvailableFundPresenter?.getBundle()))
            startActivityForResult(openABSAOnlineBanking, ABSA_ONLINE_BANKING_REGISTRATION_REQUEST_CODE)
            overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
        }
    }

    override fun displayCardNumberNotFound() {
        if (fragmentAlreadyAdded()) return
        if ((activity as? AccountSignedInActivity)?.bottomSheetIsExpanded() == true) return
        try {
            activity?.apply {
                val dialog =
                    ErrorMessageDialogWithTitleFragment.newInstance(
                        title = bindString(R.string.credit_card_statement_unavailable_title),
                        description = bindString(R.string.credit_card_statement_unavailable_description),
                        actionButtonTitle = bindString(R.string.got_it),
                        shouldFinishActivity = false
                    )
                dialog?.show(supportFragmentManager.beginTransaction(), ErrorMessageDialogWithTitleFragment::class.java.simpleName)
            }
        } catch (ex: IllegalStateException) {
            FirebaseManager.logException(ex)
        }
    }

    override fun handleUnknownHttpResponse(desc: String?) {
        if (fragmentAlreadyAdded()) return
        activity?.supportFragmentManager?.let { fragmentManager ->
            Utils.showGeneralErrorDialog(fragmentManager, desc)
        }
    }

    override fun handleSessionTimeOut(stsParams: String) {
        if (fragmentAlreadyAdded()) return
        (activity as? AccountSignedInActivity)?.let {
                accountSignedInActivity ->
            FirebaseEventDetailManager.timeout(FirebaseManagerAnalyticsProperties.ABSA_CC_VIEW_STATEMENTS, accountSignedInActivity)
            SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, stsParams, accountSignedInActivity)
        }
    }

    override fun showABSAServiceGetUserCreditCardTokenProgressBar() {
        if (fragmentAlreadyAdded()) return
        binding.incViewStatementButton.statementProgressBarGroup?.visibility = VISIBLE
    }

    override fun hideABSAServiceGetUserCreditCardTokenProgressBar() {
        if (fragmentAlreadyAdded()) return
        activity?.runOnUiThread {
            binding.incViewStatementButton.statementProgressBarGroup?.visibility = GONE
        }
    }

    private fun fragmentAlreadyAdded(): Boolean {
        if (!isAdded) return true
        return false
    }

    override fun onDestroy() {
        mAvailableFundPresenter?.onDestroy()
        super.onDestroy()
    }

    override fun navigateToLoanWithdrawalActivity() {
        activity?.apply {
            val intentWithdrawalActivity = Intent(this, LoanWithdrawalActivity::class.java)
            intentWithdrawalActivity.putExtra("account_info", Gson().toJson(mAvailableFundPresenter?.getAccount()))
            startActivityForResult(intentWithdrawalActivity, 0)
            overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
        }
    }

    override fun navigateToRecentTransactionActivity(cardType: String) {
        activity?.let { activity ->
            mAvailableFundPresenter?.getAccount()?.apply {
                val intent = Intent(activity, WTransactionsActivity::class.java)
                intent.putExtra(BundleKeysConstants.PRODUCT_OFFERINGID, productOfferingId.toString())
                if (cardType == AccountsProductGroupCode.CREDIT_CARD.groupCode && accountNumber?.isNotEmpty() == true) {
                    intent.putExtra("accountNumber", accountNumber.toString())
                }
                intent.putExtra(ACCOUNTS, Gson().toJson(Pair(mAvailableFundPresenter?.getApplyNowState(), this)))
                intent.putExtra("cardType", cardType)
                activity.startActivityForResult(intent, 0)
                activity.overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
            }
        }
    }

    override fun navigateToABSAStatementActivity() {
        activity?.apply {
            FirebaseEventDetailManager.tapped(FirebaseManagerAnalyticsProperties.ABSA_CC_VIEW_STATEMENTS, this)
            if (NetworkManager().isConnectedToNetwork(this)) {
                mAvailableFundPresenter?.queryABSAServiceGetUserCreditCardToken()
            } else {
                ErrorHandlerView(this).showToast()
            }
        }
    }

    fun AvailableFundsFragmentBinding.initShimmer() {
        val shimmer = Shimmer.AlphaHighlightBuilder().build()
        incPayMyAccountButton.viewPaymentOptionImageShimmerLayout?.setShimmer(shimmer)
        incPayMyAccountButton.viewPaymentOptionTextShimmerLayout?.setShimmer(shimmer)
    }

    private fun AvailableFundsFragmentBinding.startProgress() {
        incPayMyAccountButton.viewPaymentOptionImageShimmerLayout?.startShimmer()
        incPayMyAccountButton.viewPaymentOptionTextShimmerLayout?.startShimmer()
    }

    fun AvailableFundsFragmentBinding.stopProgress() {
        incPayMyAccountButton.viewPaymentOptionImageShimmerLayout?.setShimmer(null)
        incPayMyAccountButton.viewPaymentOptionImageShimmerLayout?.stopShimmer()

        incPayMyAccountButton.viewPaymentOptionTextShimmerLayout?.setShimmer(null)
        incPayMyAccountButton.viewPaymentOptionTextShimmerLayout?.stopShimmer()
    }

    fun navigateToPayMyAccount(openCardOptionsDialog: () -> Unit) {
        val payMyAccountOption: ConfigPayMyAccount? = AppConfigSingleton.mPayMyAccount
        val isFeatureEnabled = payMyAccountOption?.isFeatureEnabled() ?: false
        val payUMethodType = payMyAccountViewModel.getCardDetail()?.payuMethodType
        when {
            (payUMethodType == PayMyAccountViewModel.PAYUMethodType.CARD_UPDATE) && isFeatureEnabled -> openCardOptionsDialog()
            else -> navigateToPayMyAccountActivity()
        }
    }

    fun navigateToDeepLinkView() {
        if (activity is AccountSignedInActivity) {
            GlobalScope.doAfterDelay(AppConstant.DELAY_100_MS) {
                (activity as? AccountSignedInActivity)?.mAccountSignedInPresenter?.apply {
                    val deepLinkingObject = getDeepLinkData()
                    when (deepLinkingObject?.get("feature")?.asString) {
                        DP_LINKING_MY_ACCOUNTS_PRODUCT_STATEMENT -> {
                            deleteDeepLinkData()
                            binding.incViewStatementButton?.root?.performClick()
                        }
                    }
                }
            }
        }
    }

    private fun navigateToDeepLinkView(destination: String, view: View?) {
        if (activity is AccountSignedInActivity) {
            GlobalScope.doAfterDelay(AppConstant.DELAY_100_MS) {
                (activity as? AccountSignedInActivity)?.mAccountSignedInPresenter?.apply {
                    val deepLinkingObject = getDeepLinkData()
                    when (deepLinkingObject?.get("feature")?.asString) {
                        destination -> {
                            deleteDeepLinkData()
                            if (isProductInGoodStanding())
                                view?.performClick()
                        }
                    }
                }
            }
        }
    }

    fun onPayMyAccountButtonTap(eventName: String?, directions: NavDirections?) {
        if (binding.incPayMyAccountButton.viewPaymentOptionImageShimmerLayout?.isShimmerStarted == true) return

        payMyAccountViewModel.apply {
            //Redirect to payment options when  ABSA cards array is empty for credit card products
            if (getProductGroupCode().equals(AccountsProductGroupCode.CREDIT_CARD.groupCode, ignoreCase = true)) {
                if (getAccount()?.cards?.isEmpty() == true) {
                    ActivityIntentNavigationManager.presentPayMyAccountActivity(activity, payMyAccountViewModel.getCardDetail())
                    return
                }
            }

            payMyAccountPresenter.apply {
                triggerFirebaseEvent(eventName, activity)
                resetAmountEnteredToDefault()
                when (isPaymentMethodOfTypeError()) {
                    true -> {
                        when (navController.currentDestination?.id) {
                            R.id.storeCardFragment,
                            R.id.blackCreditCardFragment,
                            R.id.goldCreditCardFragment,
                            R.id.silverCreditCardFragment,
                            R.id.personalLoanFragment -> {
                                try {
                                    navController.navigate(R.id.payMyAccountRetryErrorFragment)
                                } catch (ex: IllegalStateException) {
                                    FirebaseManager.logException(ex)
                                }
                            }
                        }
                    }
                    false -> {
                        openPayMyAccountOptionOrEnterPaymentAmountDialogFragment(activity)
                        {
                            try {
                                directions?.let { navigateSafelyWithNavController(it) }
                            } catch (ex: IllegalStateException) {
                                FirebaseManager.logException(ex)
                            }
                        }
                    }
                }
            }
        }

    }

    fun accountInArrearsResultListener(onPayMyAccountButtonTap: () -> Unit) {
        setFragmentResultListener(AccountInArrearsDialogFragment::class.java.simpleName) { _, bundle ->
            GlobalScope.doAfterDelay(AppConstant.DELAY_100_MS) {
                when (bundle.getString(
                    AccountInArrearsDialogFragment::class.java.simpleName,
                    "N/A"
                )) {
                    ARREARS_PAY_NOW_BUTTON -> onPayMyAccountButtonTap()
                    ARREARS_CHAT_TO_US_BUTTON -> {
                        val chatBubble =
                            payMyAccountViewModel.getApplyNowState()?.let { applyNowState ->
                                ChatFloatingActionButtonBubbleView(
                                    activity = activity as? AccountSignedInActivity,
                                    applyNowState = applyNowState,
                                    vocTriggerEvent = payMyAccountViewModel.getVocTriggerEventMyAccounts()
                                )
                            }
                        chatBubble?.navigateToChatActivity(
                            activity,
                            payMyAccountViewModel.getCardDetail()?.account?.second
                        )
                    }
                }
            }
        }
    }


    fun startGetAPaymentPlanActivity(bundle: Bundle) {
        activity?.apply {
            val intent = Intent(context, GetAPaymentPlanActivity::class.java)
            intent.putExtra(ViewTreatmentPlanDialogFragment.ELIGIBILITY_PLAN, bundle.getSerializable(ViewTreatmentPlanDialogFragment.ELIGIBILITY_PLAN))
            startActivityForResult(intent, AccountsOptionFragment.REQUEST_GET_PAYMENT_PLAN)
            overridePendingTransition(R.anim.slide_from_right, R.anim.stay)
        }
    }
}