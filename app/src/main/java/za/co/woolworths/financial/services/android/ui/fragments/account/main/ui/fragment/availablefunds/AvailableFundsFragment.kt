package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.availablefunds

import android.os.Bundle
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Guideline
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AvailableFundsFragmentBinding
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.models.dto.app_config.ConfigPayMyAccount
import za.co.woolworths.financial.services.android.ui.activities.ABSAOnlineBankingRegistrationActivity
import za.co.woolworths.financial.services.android.ui.activities.GetAPaymentPlanActivity
import za.co.woolworths.financial.services.android.ui.activities.StatementActivity
import za.co.woolworths.financial.services.android.ui.activities.WTransactionsActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity
import za.co.woolworths.financial.services.android.ui.base.ViewBindingFragment
import za.co.woolworths.financial.services.android.ui.extension.navigateSafelyWithNavController
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatFloatingActionButtonBubbleView
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.card.AccountsOptionFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.helper.FirebaseEventDetailManager
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.loadingState
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.openActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.openActivityForResult
import za.co.woolworths.financial.services.android.ui.views.actionsheet.AccountsErrorHandlerFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.AccountInArrearsDialogFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.ViewTreatmentPlanDialogFragment
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

@AndroidEntryPoint
open class AvailableFundsFragment :
    ViewBindingFragment<AvailableFundsFragmentBinding>(AvailableFundsFragmentBinding::inflate) {

    val viewModel by viewModels<AvailableFundsViewModel>()
    val payMyAccountViewModel by viewModels<PayMyAccountViewModel>()

    lateinit var navController: NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        setPushViewDownAnimation()
        setupCommandObservers()
        bottomViewSetup(view)
        connectionBroadCastReceiver()

        viewModel.getNavigationResult().observe(viewLifecycleOwner) { result ->
            when (result) {
                PayMyAccountViewModel.OnNavigateBack.Retry -> {
                    activity?.runOnUiThread {
                        viewModel.isQueryPayUPaymentMethodComplete = false
                        queryPaymentMethod()
                    }
                }
                else -> return@observe
            }
        }
    }

    private fun connectionBroadCastReceiver() {
        activity?.let { act ->
            ConnectionBroadcastReceiver.registerToFragmentAndAutoUnregister(
                act,
                this,
                object : ConnectionBroadcastReceiver() {
                    override fun onConnectionChanged(hasConnection: Boolean) {
                        when (hasConnection || !viewModel.isQueryPayUPaymentMethodComplete) {
                            true -> queryPaymentMethod()
                            else -> ErrorHandlerView(act).showToast()
                        }

                    }
                })
        }
    }

    /*TODO: should be deleted after dimi's work*/
    private fun bottomViewSetup(view: View) {
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
                binding.paymentOverdueGroup.visibility = View.VISIBLE
                sliderGuidelineForArrears
            } else {
                binding.paymentOverdueGroup.visibility = View.INVISIBLE
                sliderGuidelineForGoodStanding
            }
        bottomViewGuideline.layoutParams = constParam

        binding.availableFundBackground.post {
            val dm = DisplayMetrics()
            (activity as? AppCompatActivity)?.windowManager?.defaultDisplay?.getMetrics(dm)
            val deviceHeight = dm.heightPixels
            val location = IntArray(2)
            binding.bottomSliderGuideline.getLocationOnScreen(location)
            val bottomGuidelineVerticalPosition = location[1]
            val displayBottomSheetBehaviorWithinRemainingHeight =
                deviceHeight - bottomGuidelineVerticalPosition + Utils.dp2px(20f)
//            bottomSheetBehaviourPeekHeightListener?.onBottomSheetPeekHeight(
//                displayBottomSheetBehaviorWithinRemainingHeight
//            )
        }
    }

    fun setupCommandObservers() {
        viewModel.command.observe(viewLifecycleOwner) {
            initShimmer(false)
            when (it) {
                is AvailableFundsCommand.DisplayCardNumberNotFound -> displayCardNumberNotFound()
                is AvailableFundsCommand.NavigateToOnlineBankingActivity -> navigateToOnlineBankingActivity(
                    it.isRegistered
                )
                is AvailableFundsCommand.NavigateToDeepLinkView -> {
                    navigateToDeepLinkView(
                        AppConstant.DP_LINKING_MY_ACCOUNTS_PRODUCT_PAY_MY_ACCOUNT,
                        ((activity as? AccountSignedInActivity)?.mAccountSignedInPresenter?.isProductInGoodStanding() == true)
                    )
                }
                is AvailableFundsCommand.SessionExpired -> {
                    val sessionData = it.onSessionData
                    activity?.let {
                        SessionUtilities.getInstance().setSessionState(
                            SessionDao.SESSION_STATE.INACTIVE,
                            sessionData,
                            it
                        )
                    }
                }
                is AvailableFundsCommand.SetViewDetails -> setUpView(it)
                is AvailableFundsCommand.PresentPayMyAccountActivity -> {
                    ActivityIntentNavigationManager.presentPayMyAccountActivity(
                        activity,
                        viewModel.getCardDetail()
                    )
                }
                is AvailableFundsCommand.PayMyAccountRetryErrorFragment -> {
                    try {
                        navController.navigate(R.id.payMyAccountRetryErrorFragment)
                    } catch (ex: IllegalStateException) {
                        FirebaseManager.logException(ex)
                    }
                }
                is AvailableFundsCommand.OpenPayMyAccountOptionOrEnterPaymentAmountDialogFragment -> {
                    viewModel.payMyAccountPresenter.openPayMyAccountOptionOrEnterPaymentAmountDialogFragment(
                        activity
                    ) {
                        try {
                            it.directions?.let { navigateSafelyWithNavController(it) }
                        } catch (ex: IllegalStateException) {
                            FirebaseManager.logException(ex)
                        }
                    }
                }
                is AvailableFundsCommand.SetPMAData -> {
                    /*TODO: this is a temp solution for setting pmaCardPopupModel.
                    *  this should be deleted after payMyAccountViewModel refactor */
                    payMyAccountViewModel.setPMACardInfo(viewModel.getCardDetail())
                }

            }
        }
    }

    fun queryPaymentMethod() {
        initShimmer(true)
        viewModel.queryServicePayUPaymentMethod()
//        viewModel.let { viewModel->
//            viewModel.paymentPAYUService.observe(viewLifecycleOwner, Observer {
//                when (it.status) {
//                    Result.Status.SUCCESS -> {
//                        it.data?.let { tokenResponse ->
//
//                        }
//                    }
//                    Result.Status.ERROR -> {
//                        when (it.apiError) {
//                            ApiError.SessionTimeOut -> it.data?.response?.stsParams?.let { stsParams ->
//                                handleSessionTimeOut(
//                                    stsParams
//                                )
//                            }
//                            ApiError.SomethingWrong -> onABSACreditCardFailureHandler()
//                            else -> handleUnknownHttpResponse(it.apiError?.value)
//                        }
//                    }
//                }
//            })
//        }
    }

    fun setPushViewDownAnimation() {
        AnimationUtilExtension.animateViewPushDown(binding.incRecentTransactionButton.root)
        AnimationUtilExtension.animateViewPushDown(binding.incViewStatementButton.root)
        AnimationUtilExtension.animateViewPushDown(binding.incPayMyAccountButton.root)
    }

    fun onABSACreditCardFailureHandler() {
        activity?.let { activity ->
            activity.runOnUiThread {
                ErrorHandlerView(activity).showToast()
            }
        }
    }

    private fun setUpView(data: AvailableFundsCommand.SetViewDetails) {
        activity?.apply {
            binding.availableFundAmountTextView.text = data.availableFund
            binding.currentBalanceAmountTextView.text = data.currentBalance
            binding.creditLimitAmountTextView.text = data.creditLimit
            binding.totalAmountDueAmountTextView.text = data.totalAmountDueAmount
            binding.nextPaymentDueDateTextView.text = data.paymentDueDate
            binding.amountPayableNowAmountTextView.text = data.amountOverdue
        }
    }

    fun navigateToStatementActivity() {
        activity?.apply {
            openActivity<StatementActivity>(
                ChatFragment.ACCOUNTS to Gson().toJson(viewModel.mAccountPair.value)
            )
            overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
        }
    }


    fun navigateToPayMyAccountActivity() {
        activity?.let { activity ->
            ActivityIntentNavigationManager.presentPayMyAccountActivity(
                activity,
                viewModel.getCardDetail()
            )
        }
    }

    fun navigateToOnlineBankingActivity(isRegistered: Boolean) {
        activity?.apply {
            openActivityForResult<ABSAOnlineBankingRegistrationActivity>(
                ABSAOnlineBankingRegistrationActivity.SHOULD_DISPLAY_LOGIN_SCREEN to isRegistered,
                BundleKeysConstants.CREDITCARD_TOKEN to viewModel.creditCardNumber.value,
                ChatFragment.ACCOUNTS to Gson().toJson(viewModel.mAccountPair.value),
                requestCode = AccountSignedInActivity.ABSA_ONLINE_BANKING_REGISTRATION_REQUEST_CODE
            )
            overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
        }
    }

    fun displayCardNumberNotFound() {
        if ((activity as? AccountSignedInActivity)?.bottomSheetIsExpanded() == true) return
        try {

            //credit_card_statement_unavailable ??
            val accountsErrorHandlerFragment =
                activity?.resources?.getString(R.string.credit_card_statement_unavailable_title)
                    ?.let { AccountsErrorHandlerFragment.newInstance(it) }
            activity?.supportFragmentManager?.let { supportFragmentManager ->
                accountsErrorHandlerFragment?.show(
                    supportFragmentManager,
                    AccountsErrorHandlerFragment::class.java.simpleName
                )
            }
        } catch (ex: IllegalStateException) {
            FirebaseManager.logException(ex)
        }
    }

    fun handleUnknownHttpResponse(desc: String?) {
        activity?.supportFragmentManager?.let { fragmentManager ->
            Utils.showGeneralErrorDialog(fragmentManager, desc)
        }
    }

    fun handleSessionTimeOut(stsParams: String) {
        (activity as? AccountSignedInActivity)?.let { accountSignedInActivity ->
            FirebaseEventDetailManager.timeout(
                FirebaseManagerAnalyticsProperties.ABSA_CC_VIEW_STATEMENTS,
                accountSignedInActivity
            )
            SessionUtilities.getInstance().setSessionState(
                SessionDao.SESSION_STATE.INACTIVE,
                stsParams,
                accountSignedInActivity
            )
        }
    }

    fun navigateToRecentTransactionActivity(cardType: String) {
        activity?.let { activity ->
            viewModel.product?.apply {
                activity.openActivityForResult<WTransactionsActivity>(
                    BundleKeysConstants.PRODUCT_OFFERINGID to productOfferingId.toString(),
                    BundleKeysConstants.ACCOUNT_NUMBER to if (cardType == AccountsProductGroupCode.CREDIT_CARD.groupCode && accountNumber?.isNotEmpty() == true) accountNumber.toString() else null,
                    ChatFragment.ACCOUNTS to Gson().toJson(
                        Pair(
                            viewModel.mAccountPair.value?.first,
                            this
                        )
                    ),
                    BundleKeysConstants.CARDTYPE to cardType
                )
                activity.overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
            }
        }
    }

    private fun initShimmer(state: Boolean) {
        binding.incPayMyAccountButton.viewPaymentOptionImageShimmerLayout.loadingState(state)
    //    binding.incPayMyAccountButton.viewPaymentOptionTextShimmerLayout.loadingState(state)
    }

    fun navigateToPayMyAccount(openCardOptionsDialog: () -> Unit) {
        val payMyAccountOption: ConfigPayMyAccount? = AppConfigSingleton.mPayMyAccount
        val isFeatureEnabled = payMyAccountOption?.isFeatureEnabled() ?: false
        val payUMethodType = viewModel.getCardDetail()?.payuMethodType
        when {
            (payUMethodType == PayMyAccountViewModel.PAYUMethodType.CARD_UPDATE) && isFeatureEnabled -> openCardOptionsDialog()
            else -> navigateToPayMyAccountActivity()
        }
    }

    fun navigateToDeepLinkView(
        destination: String = AppConstant.DP_LINKING_MY_ACCOUNTS_PRODUCT_STATEMENT,
        performClick: Boolean = true
    ) {
        if (activity is AccountSignedInActivity) {
            viewLifecycleOwner.lifecycleScope.launch {
                delay(AppConstant.DELAY_100_MS)
                (activity as? AccountSignedInActivity)?.mAccountSignedInPresenter?.apply {
                    val deepLinkingObject = getDeepLinkData()
                    when (deepLinkingObject?.get("feature")?.asString) {
                        destination -> {
                            deleteDeepLinkData()
                            if (performClick) {
                                binding.incViewStatementButton.root.performClick()
                            }
                        }
                    }
                }
            }
        }
    }

    fun onPayMyAccountButtonTap(eventName: String?, directions: NavDirections?) {
        if (binding.incPayMyAccountButton.viewPaymentOptionImageShimmerLayout.isShimmerStarted) return
        viewModel.onPayMyAccountButtonTap(
            eventName,
            navController.currentDestination?.id,
            directions
        )
    }

    fun accountInArrearsResultListener(onPayMyAccountButtonTap: () -> Unit) {
        setFragmentResultListener(AccountInArrearsDialogFragment::class.java.simpleName) { _, bundle ->
            viewLifecycleOwner.lifecycleScope.launch {
                delay(AppConstant.DELAY_100_MS)
                when (bundle.getString(
                    AccountInArrearsDialogFragment::class.java.simpleName,
                    "N/A"
                )) {
                    AccountInArrearsDialogFragment.ARREARS_PAY_NOW_BUTTON -> onPayMyAccountButtonTap()
                    AccountInArrearsDialogFragment.ARREARS_CHAT_TO_US_BUTTON -> {
                        val chatBubble =
                            viewModel.getApplyNowState()?.let { applyNowState ->
                                ChatFloatingActionButtonBubbleView(
                                    activity = activity as? AccountSignedInActivity,
                                    applyNowState = applyNowState,
                                    vocTriggerEvent = viewModel.getVocTriggerEventMyAccounts()
                                )
                            }
                        chatBubble?.navigateToChatActivity(
                            activity,
                            viewModel.getCardDetail()?.account?.second
                        )
                    }
                }
            }
        }
    }

    fun startGetAPaymentPlanActivity(bundle: Bundle) {
        activity?.apply {
            openActivityForResult<GetAPaymentPlanActivity>(
                ViewTreatmentPlanDialogFragment.ELIGIBILITY_PLAN to bundle.getSerializable(
                    ViewTreatmentPlanDialogFragment.ELIGIBILITY_PLAN
                ),
                requestCode = AccountsOptionFragment.REQUEST_GET_PAYMENT_PLAN
            )
            overridePendingTransition(R.anim.slide_from_right, R.anim.stay)
        }
    }
}