package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.availablefunds

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentAvailableFundBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.PMACardPopupModel
import za.co.woolworths.financial.services.android.models.dto.ProductGroupCode
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.onecartgetstream.common.navigateSafely
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.toolbar.AccountProductsToolbarHelper
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.treatmentplan.OutSystemBuilder
import za.co.woolworths.financial.services.android.ui.base.onClick
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatFloatingActionButtonBubbleView
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.component.WBottomSheetBehaviour
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.InformationData
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.SystemBarCompat
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.StoreCardActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_account_options_list.PayMyAccountButtonTap
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_account_options_list.PayMyAccountScreen
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.overlay.DisplayInArrearsPopup
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.landing.AccountProductsHomeFragmentDirections
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.landing.AccountProductsHomeViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.main.dialog.AccountLandingDialogFragment.Companion.requestKeyAccountLandingDialog
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.router.ProductLandingRouterImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.loadingState
import za.co.woolworths.financial.services.android.util.*
import java.net.ConnectException
import javax.inject.Inject

@AndroidEntryPoint
class MyStoreCardFragment @Inject constructor() : Fragment(R.layout.fragment_available_fund){

    private lateinit var mOutSystemBuilder: OutSystemBuilder
    private lateinit var mDisplayInArrearsPopup: DisplayInArrearsPopup
    val viewModel: AvailableFundsViewModel by activityViewModels()
    val homeViewModel: AccountProductsHomeViewModel by activityViewModels()
    val payMyAccountViewModel: PayMyAccountViewModel by activityViewModels()

    @Inject lateinit var router : ProductLandingRouterImpl
    var mToolbarHelper : AccountProductsToolbarHelper? = null

    @Inject
    lateinit var pmaButton: PayMyAccountButtonTap

    @Inject
    lateinit var bottomSheet: WBottomSheetBehaviour

    @Inject
    lateinit var statusBarCompat: SystemBarCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homeViewModel.showAccountInArrearsPopup = true
        mToolbarHelper =  (activity as? StoreCardActivity)?.getToolbarHelper()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentAvailableFundBinding.bind(view)
        with(binding) {
            viewModel.availableFunds.setUpView()
            statusBarCompat.setLightStatusAndNavigationBar()
            KotlinUtils.setTransparentStatusBar(requireActivity() as? StoreCardActivity)
            setupToolbar()
            subscribeObserver()
            setGuideline()
            setAccountInArrearsUI()
            setBackground()
            setInArrearsPopup()
            autoConnectPMA()
            setFragmentResultListener()
            navigateToDeepLinkView()
            with(mDisplayInArrearsPopup) {
                collectCheckEligibilityResult {}
                viewLifecycleOwner.lifecycleScope.launch {
                    setupInArrearsPopup(homeViewModel.showAccountInArrearsPopup)
                }
            }

            onItemClick()
        }
    }

    private fun FragmentAvailableFundBinding.onItemClick() {
        with(homeViewModel) {
            incViewStatementButton.root.onClick {
                navigateToStatementActivity(activity, product)
            }

            incRecentTransactionButton.root.onClick {
                product?.let { navigateToRecentTransactionActivity(activity, it, cardType = it.productGroupCode) }
            }
            incPayMyAccountButton.root.onClick { onPayMyAccountButtonTap() }
        }
    }

    private fun FragmentAvailableFundBinding.showProgress(isLoading: Boolean) {
        incPayMyAccountButton.viewPaymentOptionTextShimmerLayout.loadingState(isLoading)
        incPayMyAccountButton.viewPaymentOptionImageShimmerLayout.loadingState(isLoading)
    }

    private fun setupToolbar() {
        viewLifecycleOwner.lifecycleScope.launch {
            mToolbarHelper?.setHomeLandingToolbar(homeViewModel) { view ->
                when (view.id) {
                    R.id.infoIconImageView -> navigateToInformation()
                    R.id.navigateBackImageButton -> activity?.finish()
                }
            }
        }
        mToolbarHelper?.setOnAccountInArrearsTapListener {}
    }



    private fun FragmentAvailableFundBinding.setBackground() {
       availableFundBackground.setBackgroundResource(R.drawable.store_card_background)
    }

    private fun FragmentAvailableFundBinding.setAccountInArrearsUI() {
        paymentOverdueGroup.visibility = homeViewModel.isUiVisible()
    }

    private fun FragmentAvailableFundBinding.subscribeObserver() {
        with(viewModel) {
            command.observe(viewLifecycleOwner) { item ->
                when (item) {
                    is AvailableFundsCommand.SetViewDetails -> setBalances(item)
                    else -> return@observe
                }
            }
        }

        payMyAccountViewModel.queryPaymentMethod.observe(viewLifecycleOwner) {
            payMyAccountViewModel.isQueryPayUPaymentMethodComplete = false
            queryPaymentMethod()
        }

    }


    /**
     * Set dynamic guideline when overdue amount, payable now is visible
     */
    private fun FragmentAvailableFundBinding.setGuideline() {
        bottomStartGuide.setGuidelinePercent(bottomSheet.buttonsTopGuideline)
        bottomSliderGuideline.setGuidelinePercent(bottomSheet.buttonsBottomGuideline)
    }

    private fun navigateToInformation() {
        with(homeViewModel) {
            (activity as? StoreCardActivity)?.landingNavController()?.navigate(
                AccountProductsHomeFragmentDirections.actionAccountProductsHomeFragmentToAccountInfoFragment(
                    if (isProductInGoodStanding()) InformationData.GoodStanding() else InformationData.Arrears()
                )
            )
        }
    }

    private fun FragmentAvailableFundBinding.setBalances(setViewDetails: AvailableFundsCommand.SetViewDetails) {
        with(setViewDetails) {
            availableFundAmountTextView.text = availableFund
            currentBalanceAmountTextView.text = currentBalance
            creditLimitAmountTextView.text = creditLimit
            totalAmountDueAmountTextView.text = totalAmountDueAmount
            nextPaymentDueDateTextView.text = paymentDueDate
            amountPayableNowAmountTextView.text = amountOverdue
        }
    }

    private fun FragmentAvailableFundBinding.onPayMyAccountButtonTap() {
        pmaButton.payMyAccountViewModel = payMyAccountViewModel
        pmaButton.isShimmerEnabled = incPayMyAccountButton.viewPaymentOptionImageShimmerLayout.isShimmerStarted == true
        pmaButton.onTap(
            FirebaseManagerAnalyticsProperties.MYACCOUNTS_PMA_SC
        ) { navigateFrom ->
            navigateSafely(
                when (navigateFrom) {
                    PayMyAccountScreen.RetryOnErrorScreen -> MyStoreCardFragmentDirections.actionMyStoreCardFragmentToPayMyAccountRetryErrorFragment()
                    PayMyAccountScreen.OpenAccountOptionsOrEnterPaymentAmountDialog -> MyStoreCardFragmentDirections.actionMyStoreCardFragmentToToCardDetailFragmentDialog()
                }
            )
    }
}

    private fun FragmentAvailableFundBinding.queryPaymentMethod() {
        when (!payMyAccountViewModel.isQueryPayUPaymentMethodComplete) {
            true -> {
                showProgress(true)
                val cardInfo = payMyAccountViewModel.getCardDetail()
                val account: Pair<ApplyNowState, Account>? = Pair(ApplyNowState.STORE_CARD, homeViewModel.product ?: Account())
                val amountEntered = account?.second?.amountOverdue?.let { amountDue ->
                    Utils.removeNegativeSymbol(
                        CurrencyFormatter.formatAmountToRandAndCent(amountDue)
                    )
                }
                val payUMethodType = PayMyAccountViewModel.PAYUMethodType.CREATE_USER
                val paymentMethodList = cardInfo?.paymentMethodList

                val card =
                    PMACardPopupModel(amountEntered, paymentMethodList, account, payUMethodType)
                payMyAccountViewModel.setPMACardInfo(card)

                payMyAccountViewModel.queryServicePayUPaymentMethod(
                    { // onSuccessResult
                        if (!isAdded) return@queryServicePayUPaymentMethod
                        showProgress(false)
                        (activity as? AccountSignedInActivity)?.mAccountSignedInPresenter?.pmaStatusImpl?.pmaSuccess()
                        payMyAccountViewModel.isQueryPayUPaymentMethodComplete = true
                        navigateToDeepLinkView(
                            AppConstant.DP_LINKING_MY_ACCOUNTS_PRODUCT_PAY_MY_ACCOUNT,
                            incPayMyAccountButton.root
                        )
                    }, { onSessionExpired ->
                        if (!isAdded) return@queryServicePayUPaymentMethod
                        activity?.let {
                            showProgress(false)
                            payMyAccountViewModel.isQueryPayUPaymentMethodComplete = true
                            SessionUtilities.getInstance().setSessionState(
                                SessionDao.SESSION_STATE.INACTIVE,
                                onSessionExpired,
                                it
                            )

                        }
                    }, { // on unknown http error / general error
                        if (!isAdded) return@queryServicePayUPaymentMethod
                        showProgress(false)
                        payMyAccountViewModel.isQueryPayUPaymentMethodComplete = true

                    }, { throwable ->
                        if (!isAdded) return@queryServicePayUPaymentMethod
                        activity?.runOnUiThread {
                            showProgress(false)
                        }
                        payMyAccountViewModel.isQueryPayUPaymentMethodComplete = true
                        if (throwable is ConnectException) {
                            payMyAccountViewModel.isQueryPayUPaymentMethodComplete = false
                        }
                    })
            }
            false -> showProgress(false)
        }
    }

    private fun navigateToDeepLinkView(destination: String, view: View?) {
        if (activity is AccountSignedInActivity) {
            viewLifecycleOwner.lifecycleScope.launch {
                delay(AppConstant.DELAY_100_MS)
                (activity as? StoreCardActivity)?.apply {
                    val deepLinkingObject = homeViewModel.mDeepLinkingObject
                    when (deepLinkingObject?.get("feature")?.asString) {
                        destination -> {
                           homeViewModel.clearDeepLinkParams()
                            if (homeViewModel.isProductInGoodStanding())
                                view?.performClick()
                        }
                    }
                }
            }
        }
    }

    fun FragmentAvailableFundBinding.navigateToDeepLinkView() {
        viewLifecycleOwner.lifecycleScope.launch {
            delay(AppConstant.DELAY_100_MS)
            homeViewModel.mDeepLinkingObject?.let {
                when (it.get("feature")?.asString) {
                    AppConstant.DP_LINKING_MY_ACCOUNTS_PRODUCT_STATEMENT -> {
                        homeViewModel.clearDeepLinkParams()
                        incViewStatementButton.root.performClick()
                    }
                }
            }
        }
    }

    private fun FragmentAvailableFundBinding.autoConnectPMA() {
            ConnectionBroadcastReceiver.registerToFragmentAndAutoUnregister(
                requireActivity(),
                this@MyStoreCardFragment,
                object : ConnectionBroadcastReceiver() {
                    override fun onConnectionChanged(hasConnection: Boolean) {
                        when (hasConnection || !payMyAccountViewModel.isQueryPayUPaymentMethodComplete) {
                            true -> queryPaymentMethod()
                            else -> ErrorHandlerView(requireActivity()).showToast()
                        }

                    }
                })

    }

    private fun setInArrearsPopup() {
        mDisplayInArrearsPopup = homeViewModel.initPopup(viewLifecycleOwner, router = router) { dialogData, eligibilityPlan ->
           viewLifecycleOwner.lifecycleScope.launch {
                mOutSystemBuilder = OutSystemBuilder(requireActivity(), ProductGroupCode.SC, eligibilityPlan = homeViewModel.eligibilityPlan)
               if (homeViewModel.showAccountInArrearsPopup) {
                   navigateSafely(
                       MyStoreCardFragmentDirections.actionMyStoreCardFragmentToAccountLandingDialogFragment(
                           homeViewModel.product,
                           dialogData,
                           eligibilityPlan
                       )
                   )
               }
            }
        }
    }

    private fun FragmentAvailableFundBinding.setFragmentResultListener() {
        viewLifecycleOwner.lifecycleScope.launch {
            setFragmentResultListener(requestKeyAccountLandingDialog) { _, bundle ->
                when (bundle.getInt(requestKeyAccountLandingDialog, 0)) {
                    R.string.view_payment_plan_button_label -> mOutSystemBuilder.build()
                    R.string.make_payment_now_button_label -> autoTapPayMyAccountButton()
                    R.string.cannot_afford_payment_button_label -> mDisplayInArrearsPopup.onTap(
                        requireActivity()
                    )
                    R.string.chat_to_us_label -> navigateToChatToUs()
                }
            }
        }
    }

    /**
     *   Delay added to give android enough time to process the dismissal of inArrears popup.
         Then proceed with pma journey routine. Otherwise, pma journey will not launch.
      */

    private fun FragmentAvailableFundBinding.autoTapPayMyAccountButton() {
        viewLifecycleOwner.lifecycleScope.launch {
            delay(AppConstant.DELAY_200_MS)
            onPayMyAccountButtonTap()
        }
    }

    private fun navigateToChatToUs() {
        val chatBubble = ChatFloatingActionButtonBubbleView(
                activity = activity as? AccountSignedInActivity,
                applyNowState = ApplyNowState.STORE_CARD,
                vocTriggerEvent = payMyAccountViewModel.getVocTriggerEventMyAccounts()
            )

        chatBubble.navigateToChatActivity(activity, homeViewModel.product)
    }


}

