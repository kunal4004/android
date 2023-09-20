package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.remove_dc_block

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import com.awfs.coordination.BuildConfig
import com.awfs.coordination.R
import com.awfs.coordination.databinding.RemoveBlockDcMainFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.onecartgetstream.common.navigateSafely
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.treatmentplan.OutSystemBuilder
import za.co.woolworths.financial.services.android.ui.extension.navigateSafelyWithNavController
import za.co.woolworths.financial.services.android.ui.extension.onClick
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatFloatingActionButtonBubbleView
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.InformationData
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.StoreCardActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.SystemBarCompat
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_account_options_list.PayMyAccountButtonTap
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_account_options_list.PayMyAccountScreen
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.overlay.DisplayInArrearsPopup
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.availablefunds.AvailableFundsCommand
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.availablefunds.AvailableFundsViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.landing.AccountProductsHomeViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.main.dialog.AccountLandingDialogFragment.Companion.requestKeyAccountLandingDialog
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.router.ProductLandingRouterImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.loadingState
import za.co.woolworths.financial.services.android.util.*
import java.net.ConnectException
import javax.inject.Inject

@AndroidEntryPoint
class AccountInDelinquencyFragment : Fragment(R.layout.remove_block_dc_main_fragment) {

    val payMyAccountViewModel: PayMyAccountViewModel by activityViewModels()

    @Inject
    lateinit var router: ProductLandingRouterImpl

    @Inject
    lateinit var statusBarCompat: SystemBarCompat

    @Inject
    lateinit var pmaButton: PayMyAccountButtonTap

    val availableFundViewModel : AvailableFundsViewModel by activityViewModels()
    val homeViewModel : AccountProductsHomeViewModel by activityViewModels()

    private lateinit var mOutSystemBuilder: OutSystemBuilder

    private lateinit var mDisplayInArrearsPopup: DisplayInArrearsPopup

    private var binding: RemoveBlockDcMainFragmentBinding?  = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = RemoveBlockDcMainFragmentBinding.bind(view)
        statusBarCompat.setLightStatusAndNavigationBar()
        setupToolbar()
        binding?.apply {
            setInArrearsPopup(this)
            setupView()
            subscribeObservers()
            setListeners()
            setFragmentResultListener()
            autoConnectPMA()
        }
    }

    private fun setInArrearsPopup(
        binding: RemoveBlockDcMainFragmentBinding) {
        mDisplayInArrearsPopup = homeViewModel.initPopup(viewLifecycleOwner, router = router) { dialogData, eligibilityPlan ->
           viewLifecycleOwner.lifecycleScope.launch {
                mOutSystemBuilder = OutSystemBuilder(requireActivity(), ProductGroupCode.SC, eligibilityPlan = homeViewModel.eligibilityPlan)
                if (homeViewModel.showAccountInArrearsPopup) {
                    navigateSafelyWithNavController(
                        AccountInDelinquencyFragmentDirections.actionRemoveBlockOnCollectionFragmentToAccountLandingDialogFragment(
                            homeViewModel.product,
                            dialogData,
                            eligibilityPlan
                        )
                    )
                }
                    binding.setHelpWithPaymentViewLabel(eligibilityPlan)
                    binding.setHelpWithPaymentViewVisibility(eligibilityPlan)
                }
            }

        homeViewModel.eligibilityPlan?.let {
            binding.setHelpWithPaymentViewLabel(it)
            binding.setHelpWithPaymentViewVisibility(it)
        }
    }

    private fun RemoveBlockDcMainFragmentBinding.setHelpWithPaymentViewVisibility(plan: EligibilityPlan?) {
        helpWithPayment.visibility = if(homeViewModel.viewTreatmentPlan?.isViewElitePlanEnabled(plan)==true || homeViewModel.viewTreatmentPlan?.isElitePlanEnabled(plan) == true ) VISIBLE else GONE
    }

    private fun RemoveBlockDcMainFragmentBinding.setupView() {

        showProgress(false)

        with(availableFundViewModel){
            val callCenterContact = getCallCenterContact("0861502020")
            setUnderlineText(callCenterContact, contactCallCenterNowTextview)
        }
    }

    private fun RemoveBlockDcMainFragmentBinding.subscribeObservers() {
        availableFundViewModel.availableFunds.setUpView()
        availableFundViewModel.command.observe(viewLifecycleOwner){item ->
            when (item) {
                is AvailableFundsCommand.SetViewDetails -> setBalances(item)
                else -> return@observe
            }
        }

        with(mDisplayInArrearsPopup) {
            collectCheckEligibilityResult{}
            setupInArrearsPopup(homeViewModel.showAccountInArrearsPopup)
        }
    }

    private fun setupToolbar() {
        (activity as? StoreCardActivity)?.getToolbarHelper()?.apply {
            setHomeLandingToolbar(homeViewModel) { view ->
                when (view.id) {
                    R.id.infoIconImageView -> navigateToInformation()
                    R.id.navigateBackImageButton -> activity?.finish()
                }
            }

            setOnAccountInArrearsTapListener {
                if (BuildConfig.DEBUG) {
                    homeViewModel.showAccountInArrearsPopup = true
                    mDisplayInArrearsPopup.setupInArrearsPopup(homeViewModel.showAccountInArrearsPopup)
                }
            }
        }
    }

    private fun navigateToInformation() {
        navigateSafely(
            AccountInDelinquencyFragmentDirections.actionRemoveBlockOnCollectionFragmentToAccountInfoFragment(
                InformationData.Arrears()
            )
        )
    }

    private fun RemoveBlockDcMainFragmentBinding.setBalances(data: AvailableFundsCommand.SetViewDetails) {
        currentBalanceAmountTextview.text = data.currentBalance
        totalAmountDueAmountTextview.text = data.totalAmountDueAmount
    }

    private fun RemoveBlockDcMainFragmentBinding.setListeners() {
        with(homeViewModel) {
            incRecentTransactionButton.root.onClick {
                product?.let {
                    navigateToRecentTransactionActivity(
                        activity = requireActivity(), product = product,
                        cardType = it.productGroupCode
                    )
                }
            }

            incViewStatementButton.root.onClick { navigateToStatementActivity(requireActivity(), product) }

            helpWithPayment.onClick { mDisplayInArrearsPopup.onTap(requireActivity()) }

            incPayMyAccountButton.root.onClick { onPayMyAccountButtonTap() }
        }
    }

    private fun RemoveBlockDcMainFragmentBinding.setHelpWithPaymentViewLabel(eligibilityPlan: EligibilityPlan?) {
                helpWithPayment.text = when (homeViewModel.viewTreatmentPlan?.isViewElitePlanEnabled(eligibilityPlan) == true) {
                        true -> getString(R.string.view_your_payment_plan)
                        false -> getString(R.string.get_help_repayment)
                    }
    }

    private fun RemoveBlockDcMainFragmentBinding.setFragmentResultListener() {
        setFragmentResultListener(requestKeyAccountLandingDialog) { _, bundle ->
            when(bundle.getInt(requestKeyAccountLandingDialog, 0)){
                R.string.view_payment_plan_button_label -> mOutSystemBuilder.build()
                R.string.make_payment_now_button_label -> onPayMyAccountButtonTap()
                R.string.cannot_afford_payment_button_label -> mDisplayInArrearsPopup.onTap(requireActivity())
                R.string.chat_to_us_label ->  navigateToChatToUs()
            }
        }
    }

    private fun navigateToChatToUs(){
        val chatBubble = ChatFloatingActionButtonBubbleView(
                activity = activity as? AccountSignedInActivity,
                applyNowState = ApplyNowState.STORE_CARD,
                vocTriggerEvent = payMyAccountViewModel.getVocTriggerEventMyAccounts()
            )

        chatBubble.navigateToChatActivity(activity, homeViewModel.product)
    }

    private fun RemoveBlockDcMainFragmentBinding.onPayMyAccountButtonTap() {
        pmaButton.payMyAccountViewModel = payMyAccountViewModel
        pmaButton.isShimmerEnabled = incPayMyAccountButton.viewPaymentOptionImageShimmerLayout.isShimmerStarted == true
        pmaButton.onTap(
            FirebaseManagerAnalyticsProperties.MYACCOUNTS_PMA_SC
        ) { navigateFrom ->
            viewLifecycleOwner.lifecycleScope.launch {
                delay(AppConstant.DELAY_200_MS)
                navigateSafely(
                    when (navigateFrom) {
                        PayMyAccountScreen.RetryOnErrorScreen -> AccountInDelinquencyFragmentDirections.actionRemoveBlockOnCollectionFragmentToPayMyAccountRetryErrorFragment()
                        PayMyAccountScreen.OpenAccountOptionsOrEnterPaymentAmountDialog -> AccountInDelinquencyFragmentDirections.actionRemoveBlockOnCollectionFragmentToToCardDetailFragmentDialog()
                    }
                )
            }
        }
  }

    private fun RemoveBlockDcMainFragmentBinding.queryPaymentMethod() {
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
            false -> return
        }
    }

    private fun navigateToDeepLinkView(destination: String, view: View?) {
        if (activity is StoreCardActivity) {
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

    private fun RemoveBlockDcMainFragmentBinding.showProgress(isLoading: Boolean) {
        incPayMyAccountButton.viewPaymentOptionTextShimmerLayout.loadingState(isLoading)
        incPayMyAccountButton.viewPaymentOptionImageShimmerLayout.loadingState(isLoading)
    }

    private fun RemoveBlockDcMainFragmentBinding.autoConnectPMA() {
        ConnectionBroadcastReceiver.registerToFragmentAndAutoUnregister(
            requireActivity(),
            this@AccountInDelinquencyFragment,
            object : ConnectionBroadcastReceiver() {
                override fun onConnectionChanged(hasConnection: Boolean) {
                    when (hasConnection || !payMyAccountViewModel.isQueryPayUPaymentMethodComplete) {
                        true -> queryPaymentMethod()
                        else -> ErrorHandlerView(requireActivity()).showToast()
                    }
                }
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

}


