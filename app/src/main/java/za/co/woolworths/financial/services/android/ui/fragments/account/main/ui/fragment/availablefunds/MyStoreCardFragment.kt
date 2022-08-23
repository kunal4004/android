package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.availablefunds

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentAvailableFundBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.available_funds_fragment.*
import kotlinx.android.synthetic.main.view_pay_my_account_button.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.PMACardPopupModel
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity
import za.co.woolworths.financial.services.android.ui.base.ViewBindingFragment
import za.co.woolworths.financial.services.android.ui.extension.navigateSafelyWithNavController
import za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.store_card.StoreCardFragmentDirections
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.component.WBottomSheetBehaviour
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.InformationData
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.SystemBarCompat
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.StoreCardActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.landing.AccountProductsHomeFragmentDirections
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.landing.AccountProductsHomeViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.loadingState
import za.co.woolworths.financial.services.android.util.*
import java.net.ConnectException
import javax.inject.Inject

@AndroidEntryPoint
class MyStoreCardFragment @Inject constructor() :
    ViewBindingFragment<FragmentAvailableFundBinding>(FragmentAvailableFundBinding::inflate),
    View.OnClickListener {

    val viewModel: AvailableFundsViewModel by activityViewModels()
    val homeViewModel: AccountProductsHomeViewModel by activityViewModels()
    val payMyAccountViewModel: PayMyAccountViewModel by activityViewModels()

    @Inject
    lateinit var bottomSheet: WBottomSheetBehaviour

    @Inject
    lateinit var statusBarCompat: SystemBarCompat

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.availableFunds.setUpView()
        statusBarCompat.setLightStatusAndNavigationBar()
        KotlinUtils.setTransparentStatusBar(requireActivity() as? StoreCardActivity)
        setupToolbar()
        subscribeObserver()
        setGuideline()
        setAccountInArrearsUI()
        setBackground()
        clickListeners()
        autoConnectPMA()
        navigateToDeepLinkView()
    }

    private fun showProgress(isLoading: Boolean) {
        binding.incPayMyAccountButton.viewPaymentOptionTextShimmerLayout.loadingState(isLoading)
        binding.incPayMyAccountButton.viewPaymentOptionImageShimmerLayout.loadingState(isLoading)
    }

    private fun setupToolbar() {
        (activity as? StoreCardActivity)?.apply {
            getToolbarHelper()?.setHomeLandingToolbar(homeViewModel) { view ->
                when (view.id) {
                    R.id.infoIconImageView -> navigateToInformation()
                    R.id.navigateBackImageButton -> activity?.finish()
                }
            }
        }
    }

    private fun clickListeners() {
        binding.incViewStatementButton.root.setOnClickListener(this)
        binding.incRecentTransactionButton.root.setOnClickListener(this)
        binding.incPayMyAccountButton.root.setOnClickListener(this)
    }

    private fun setBackground() {
        binding.availableFundBackground.setBackgroundResource(R.drawable.store_card_background)
    }

    private fun setAccountInArrearsUI() {
        binding.paymentOverdueGroup.visibility = homeViewModel.isUiVisible()
    }

    private fun subscribeObserver() {
        with(viewModel) {
            command.observe(viewLifecycleOwner) { item ->
                when (item) {
                    is AvailableFundsCommand.SetViewDetails -> binding.setBalances(item)
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
    private fun setGuideline() {
        with(binding) {
            bottomStartGuide.setGuidelinePercent(bottomSheet.buttonsTopGuideline)
            bottomSliderGuideline.setGuidelinePercent(bottomSheet.buttonsBottomGuideline)
        }
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

    override fun onClick(view: View?) {
        with(homeViewModel) {
            when (view) {
                binding.incViewStatementButton.root -> {
                    navigateToStatementActivity(activity, product)
                }
                binding.incRecentTransactionButton.root -> {
                    product?.let {
                        navigateToRecentTransactionActivity(
                            activity,
                            product,
                            cardType = it.productGroupCode
                        )
                    }
                }

                binding.incPayMyAccountButton.root -> {
                    onPayMyAccountButtonTap()
                }

                else -> Unit
            }
        }
    }

    private fun onPayMyAccountButtonTap() {
        onPayMyAccountButtonTap(
            FirebaseManagerAnalyticsProperties.MYACCOUNTS_PMA_SC,
            StoreCardFragmentDirections.storeCardFragmentToDisplayVendorDetailFragmentAction()
        )
    }

    private fun onPayMyAccountButtonTap(eventName: String?, directions: NavDirections?) {
        if (viewPaymentOptionImageShimmerLayout?.isShimmerStarted == true) return

        payMyAccountViewModel.apply {
            //Redirect to payment options when  ABSA cards array is empty for credit card products
            if (getProductGroupCode().equals(
                    AccountsProductGroupCode.CREDIT_CARD.groupCode,
                    ignoreCase = true
                )
            ) {
                if (getAccount()?.cards?.isEmpty() == true) {
                    ActivityIntentNavigationManager.presentPayMyAccountActivity(
                        activity,
                        payMyAccountViewModel.getCardDetail()
                    )
                    return
                }
            }

            payMyAccountPresenter.apply {
                triggerFirebaseEvent(eventName, activity)
                resetAmountEnteredToDefault()
                when (isPaymentMethodOfTypeError()) {
                    true -> {
                        try {
                            findNavController().navigate(R.id.payMyAccountRetryErrorFragment)
                        } catch (ex: IllegalStateException) {
                            FirebaseManager.logException(ex)
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

    private fun queryPaymentMethod() {
        when (!payMyAccountViewModel.isQueryPayUPaymentMethodComplete) {
            true -> {
                showProgress(true)
                val cardInfo = payMyAccountViewModel.getCardDetail()
                val account: Pair<ApplyNowState, Account>? =
                    Pair(ApplyNowState.STORE_CARD, homeViewModel.product ?: Account())
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
                            incPayMyAccountButton
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
            false -> return
        }
    }

    private fun navigateToDeepLinkView(destination: String, view: View?) {
        if (activity is AccountSignedInActivity) {
            viewLifecycleOwner.lifecycleScope.launch {
                delay(AppConstant.DELAY_100_MS)
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

    fun navigateToDeepLinkView() {
        if (activity is AccountSignedInActivity) {
            viewLifecycleOwner.lifecycleScope.launch {
                delay(AppConstant.DELAY_100_MS)
                (activity as? AccountSignedInActivity)?.mAccountSignedInPresenter?.apply {
                    val deepLinkingObject = getDeepLinkData()
                    when (deepLinkingObject?.get("feature")?.asString) {
                        AppConstant.DP_LINKING_MY_ACCOUNTS_PRODUCT_STATEMENT -> {
                            deleteDeepLinkData()
                            incViewStatementButton?.performClick()
                        }
                    }
                }
            }
        }
    }

    fun autoConnectPMA() {
            ConnectionBroadcastReceiver.registerToFragmentAndAutoUnregister(
                requireActivity(),
                this,
                object : ConnectionBroadcastReceiver() {
                    override fun onConnectionChanged(hasConnection: Boolean) {
                        when (hasConnection || !payMyAccountViewModel.isQueryPayUPaymentMethodComplete) {
                            true -> queryPaymentMethod()
                            else -> ErrorHandlerView(requireActivity()).showToast()
                        }

                    }
                })

    }

}

