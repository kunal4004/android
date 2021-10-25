package za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.store_card

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.Navigation
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.available_funds_fragment.*
import kotlinx.android.synthetic.main.view_pay_my_account_button.*
import kotlinx.coroutines.*

import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity.Companion.PAY_MY_ACCOUNT_REQUEST_CODE
import za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.AvailableFundFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PMA3DSecureProcessRequestFragment.Companion.PMA_TRANSACTION_COMPLETED_RESULT_CODE
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity
import za.co.woolworths.financial.services.android.ui.extension.doAfterDelay

import za.co.woolworths.financial.services.android.ui.extension.navigateSafelyWithNavController
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatFloatingActionButtonBubbleView
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.AccountInArrearsDialogFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.AccountInArrearsDialogFragment.Companion.ARREARS_CHAT_TO_US_BUTTON
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.AccountInArrearsDialogFragment.Companion.ARREARS_PAY_NOW_BUTTON
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.ViewTreatmentPlanDialogFragment
import za.co.woolworths.financial.services.android.util.*

class StoreCardFragment : AvailableFundFragment(), View.OnClickListener {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        availableFundBackground?.setBackgroundResource(R.drawable.store_card_background)

        payMyAccountViewModel.queryPaymentMethod.observe(viewLifecycleOwner, {
            isQueryPayUPaymentMethodComplete = false
            queryPaymentMethod()
        })

        incRecentTransactionButton?.setOnClickListener(this)
        incViewStatementButton?.setOnClickListener(this)
        incPayMyAccountButton?.setOnClickListener(this)

        navigateToDeepLinkView()

        accountInArrearsResultListener {
            onPayMyAccountButtonTap()
        }
        setFragmentResultListener(AccountInArrearsDialogFragment::class.java.simpleName) { _, bundle ->
            CoroutineScope(Dispatchers.Main).doAfterDelay(AppConstant.DELAY_100_MS) {
                when (bundle.getString(AccountInArrearsDialogFragment::class.java.simpleName, "N/A")) {
                    ARREARS_PAY_NOW_BUTTON -> onStoreCardButtonTap()
                    ARREARS_CHAT_TO_US_BUTTON -> {
                        val chatBubble = payMyAccountViewModel.getApplyNowState()?.let { applyNowState ->
                            ChatFloatingActionButtonBubbleView(
                                    activity = activity as? AccountSignedInActivity,
                                    applyNowState = applyNowState,
                                    vocTriggerEvent = payMyAccountViewModel.getVocTriggerEventMyAccounts()
                            )
                        }
                        chatBubble?.navigateToChatActivity(activity, payMyAccountViewModel.getCardDetail()?.account?.second)
                    }
                }
            }
        }

        setFragmentResultListener(ViewTreatmentPlanDialogFragment::class.java.simpleName) { _, bundle ->
            CoroutineScope(Dispatchers.Main).doAfterDelay(AppConstant.DELAY_100_MS) {
                when (bundle.getString(ViewTreatmentPlanDialogFragment::class.java.simpleName)) {
                    ViewTreatmentPlanDialogFragment.VIEW_PAYMENT_PLAN_BUTTON -> {
                        activity?.apply {
                            val arguments = HashMap<String, String>()
                            arguments[FirebaseManagerAnalyticsProperties.PropertyNames.ACTION] = FirebaseManagerAnalyticsProperties.VIEW_PAYMENT_PLAN_STORE_CARD_ACTION
                            Utils.triggerFireBaseEvents(
                                FirebaseManagerAnalyticsProperties.VIEW_PAYMENT_PLAN_STORE_CARD,
                                arguments,
                                this)
                            when (WoolworthsApplication.getAccountOptions()?.showTreatmentPlanJourney?.renderMode){
                                NATIVE_BROWSER ->
                                    KotlinUtils.openUrlInPhoneBrowser(
                                    WoolworthsApplication.getAccountOptions()?.showTreatmentPlanJourney?.storeCard?.collectionsUrl, this)

                                else ->
                                KotlinUtils.openLinkInInternalWebView(activity,
                                    WoolworthsApplication.getAccountOptions()?.showTreatmentPlanJourney?.storeCard?.collectionsUrl,
                                    true,
                                    WoolworthsApplication.getAccountOptions()?.showTreatmentPlanJourney?.storeCard?.exitUrl
                                )
                            }
                        }
                    }
                    ViewTreatmentPlanDialogFragment.MAKE_A_PAYMENT_BUTTON -> onStoreCardButtonTap()
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
        if (viewPaymentOptionImageShimmerLayout?.isShimmerStarted == true) return

        activity?.apply { Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTS_PMA_SC, this) }
        payMyAccountViewModel.resetAmountEnteredToDefault()

        if (payMyAccountViewModel.getPaymentMethodType() == PayMyAccountViewModel.PAYUMethodType.ERROR) {
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