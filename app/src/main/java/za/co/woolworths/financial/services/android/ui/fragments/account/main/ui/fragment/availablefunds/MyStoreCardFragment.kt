package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.availablefunds

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentAvailableFundBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.available_funds_fragment.*
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.PMACardPopupModel
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity
import za.co.woolworths.financial.services.android.ui.base.ViewBindingFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.component.WBottomSheetBehaviour
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.InformationData
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.SystemBarCompat
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.StoreCardActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.landing.AccountProductsHomeViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.main.AccountProductsMainFragmentDirections
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.loadingState
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.CurrencyFormatter
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils
import java.net.ConnectException
import javax.inject.Inject

@AndroidEntryPoint
class MyStoreCardFragment @Inject constructor() :
    ViewBindingFragment<FragmentAvailableFundBinding>(FragmentAvailableFundBinding::inflate),
    View.OnClickListener {

    val viewModel : AvailableFundsViewModel by activityViewModels()
    val homeViewModel by viewModels<AccountProductsHomeViewModel>()
    val payMyAccountViewModel: PayMyAccountViewModel by activityViewModels()

    @Inject
    lateinit var bottomSheet: WBottomSheetBehaviour

    @Inject
    lateinit var statusBarCompat: SystemBarCompat

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.availableFunds.setUpView()
        statusBarCompat.setLightStatusAndNavigationBar()
        setupToolbar()
        subscribeObserver()
        setGuideline()
        setAccountInArrearsUI()
        background()
        clickListeners()
        queryPaymentMethod()
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

    private fun background() {
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
            findNavController().navigate(
                AccountProductsMainFragmentDirections.actionAccountProductsMainFragmentToAccountInformationFragment(
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
                    product?.let { navigateToRecentTransactionActivity(activity, product,cardType =  it.productGroupCode) }
                }

                else -> Unit
            }
        }
    }

    /****** Pay My Account work *****/
    fun queryPaymentMethod() {
        when (!payMyAccountViewModel.isQueryPayUPaymentMethodComplete) {
            true -> {
                showProgress(true)
                val cardInfo = payMyAccountViewModel.getCardDetail()
                val amountEntered = homeViewModel.product?.let { amountDue -> Utils.removeNegativeSymbol(
                    CurrencyFormatter.formatAmountToRandAndCent(amountDue)) }
                val payUMethodType = PayMyAccountViewModel.PAYUMethodType.CREATE_USER
                val paymentMethodList = cardInfo?.paymentMethodList

                val card = PMACardPopupModel(amountEntered, paymentMethodList, Pair(ApplyNowState.STORE_CARD, homeViewModel.product ?: Account()), payUMethodType)
                payMyAccountViewModel.setPMACardInfo(card)

                payMyAccountViewModel.queryServicePayUPaymentMethod(
                    { // onSuccessResult
                        if (!isAdded) return@queryServicePayUPaymentMethod
                        showProgress(false)
                        (activity as? AccountSignedInActivity)?.mAccountSignedInPresenter?.pmaStatusImpl?.pmaSuccess()
                        payMyAccountViewModel.isQueryPayUPaymentMethodComplete = true
                        navigateToDeepLinkView(AppConstant.DP_LINKING_MY_ACCOUNTS_PRODUCT_PAY_MY_ACCOUNT, incPayMyAccountButton)
                    }, { onSessionExpired ->
                        if (!isAdded) return@queryServicePayUPaymentMethod
                        activity?.let {
                            showProgress(false)
                            payMyAccountViewModel.isQueryPayUPaymentMethodComplete = true
                            SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, onSessionExpired, it)

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
}

