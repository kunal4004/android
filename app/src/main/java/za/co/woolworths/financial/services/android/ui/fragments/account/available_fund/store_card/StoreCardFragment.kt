package za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.store_card

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.Navigation
import com.awfs.coordination.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.ProductGroupCode
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity.Companion.PAY_MY_ACCOUNT_REQUEST_CODE
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.treatmentplan.OutSystemBuilder
import za.co.woolworths.financial.services.android.ui.extension.doAfterDelay
import za.co.woolworths.financial.services.android.ui.extension.navigateSafelyWithNavController
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatFloatingActionButtonBubbleView
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PMA3DSecureProcessRequestFragment.Companion.PMA_TRANSACTION_COMPLETED_RESULT_CODE
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.availablefunds.AvailableFundsFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.AccountInArrearsDialogFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.AccountInArrearsDialogFragment.Companion.ARREARS_CHAT_TO_US_BUTTON
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.AccountInArrearsDialogFragment.Companion.ARREARS_PAY_NOW_BUTTON
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.ViewTreatmentPlanDialogFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.ViewTreatmentPlanDialogFragment.Companion.CANNOT_AFFORD_PAYMENT_BUTTON
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.ViewTreatmentPlanDialogFragment.Companion.MAKE_A_PAYMENT_BUTTON
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.ViewTreatmentPlanDialogFragment.Companion.VIEW_PAYMENT_PLAN_BUTTON
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.NetworkManager
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager

class StoreCardFragment : AvailableFundsFragment(), View.OnClickListener {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            navController = Navigation.findNavController(view)
            availableFundBackground?.setBackgroundResource(R.drawable.store_card_background)

            viewModel.queryPaymentMethod.observe(viewLifecycleOwner) {
                viewModel.isQueryPayUPaymentMethodComplete = false
                queryPaymentMethod()
            }

            incRecentTransactionButton?.root?.setOnClickListener(this@StoreCardFragment)
            incViewStatementButton?.root?.setOnClickListener(this@StoreCardFragment)
            incPayMyAccountButton?.root?.setOnClickListener(this@StoreCardFragment)

            navigateToDeepLinkView()

            accountInArrearsResultListener {
                onPayMyAccountButtonTap()
            }
            setFragmentResultListener(AccountInArrearsDialogFragment::class.java.simpleName) { _, bundle ->
                CoroutineScope(Dispatchers.Main).doAfterDelay(AppConstant.DELAY_100_MS) {
                    when (bundle.getString(
                        AccountInArrearsDialogFragment::class.java.simpleName,
                        "N/A"
                    )) {
                        ARREARS_PAY_NOW_BUTTON -> onStoreCardButtonTap()
                        ARREARS_CHAT_TO_US_BUTTON -> {
                            val chatBubble = viewModel.getApplyNowState()?.let { applyNowState ->
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

            setFragmentResultListener(ViewTreatmentPlanDialogFragment::class.java.simpleName) { _, bundle ->
                CoroutineScope(Dispatchers.Main).doAfterDelay(AppConstant.DELAY_100_MS) {
                    val outSystemWebUrl =
                        OutSystemBuilder(activity, ProductGroupCode.SC, bundle = bundle)
                    when (outSystemWebUrl.getBundleKey()) {
                        VIEW_PAYMENT_PLAN_BUTTON -> outSystemWebUrl.build()
                        CANNOT_AFFORD_PAYMENT_BUTTON -> startGetAPaymentPlanActivity(bundle)
                        MAKE_A_PAYMENT_BUTTON -> onStoreCardButtonTap()
                    }
                }
            }
        }
    }

    override fun onClick(view: View?) {
        KotlinUtils.avoidDoubleClicks(view)
        when (view?.id) {
            R.id.incRecentTransactionButton -> {
                activity?.apply { Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTSSTORECARDTRANSACTIONS, this) }
                navigateToRecentTransactionActivity(AccountsProductGroupCode.STORE_CARD.groupCode)
            }
            R.id.incViewStatementButton -> {
                activity?.apply { Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTSSTORECARDSTATEMENTS, this) }
                navigateToStatementActivity()
            }
            R.id.incPayMyAccountButton -> onPayMyAccountButtonTap()
        }
    }

    private fun onPayMyAccountButtonTap() {
        onPayMyAccountButtonTap(
            FirebaseManagerAnalyticsProperties.MYACCOUNTS_PMA_SC,
            StoreCardFragmentDirections.storeCardFragmentToDisplayVendorDetailFragmentAction()
        )
    }

    private fun onStoreCardButtonTap() {
        if (binding.incPayMyAccountButton.viewPaymentOptionImageShimmerLayout?.isShimmerStarted == true) return

        activity?.apply { Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTS_PMA_SC, this) }
        viewModel.resetAmountEnteredToDefault()

        if (viewModel.getPaymentMethodType() == PayMyAccountViewModel.PAYUMethodType.ERROR) {
            try {
                if (navController.currentDestination?.id == R.id.storeCardFragment) {
                    navController.navigate(R.id.payMyAccountRetryErrorFragment)
                }
            } catch (ex: IllegalStateException) {
                FirebaseManager.logException(ex)
            }
            return
        }

        navigateToPayMyAccount {
            try {
                navigateSafelyWithNavController(StoreCardFragmentDirections.storeCardFragmentToDisplayVendorDetailFragmentAction())
            } catch (ex: IllegalStateException) {
                FirebaseManager.logException(ex)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PAY_MY_ACCOUNT_REQUEST_CODE -> {
                if (resultCode == PMA_TRANSACTION_COMPLETED_RESULT_CODE) {
                    if (NetworkManager.getInstance().isConnectedToNetwork(context))
                        queryPaymentMethod()
                }
            }
        }
    }
}