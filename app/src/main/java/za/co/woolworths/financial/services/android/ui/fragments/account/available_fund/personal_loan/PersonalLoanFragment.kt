package za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.personal_loan

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.observe
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.Navigation
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.available_funds_fragment.*
import kotlinx.android.synthetic.main.view_pay_my_account_button.*
import kotlinx.coroutines.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.ui.activities.GetAPaymentPlanActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity
import za.co.woolworths.financial.services.android.ui.extension.doAfterDelay

import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.ui.extension.navigateSafelyWithNavController
import za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.AvailableFundFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatFloatingActionButtonBubbleView
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.card.AccountsOptionFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PMA3DSecureProcessRequestFragment.Companion.PMA_TRANSACTION_COMPLETED_RESULT_CODE
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

class PersonalLoanFragment : AvailableFundFragment(), View.OnClickListener {

    companion object {
        var SHOW_PL_WITHDRAW_FUNDS_SCREEN = false
        var PL_WITHDRAW_FUNDS_DETAIL = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        availableFundBackground?.setBackgroundResource(R.drawable.personal_loan_background)

        navController = Navigation.findNavController(view)

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
                when (bundle.getString(
                    AccountInArrearsDialogFragment::class.java.simpleName,
                    "N/A"
                )) {
                    ARREARS_PAY_NOW_BUTTON -> onPayMyAccountButtonTap()
                    ARREARS_CHAT_TO_US_BUTTON -> {
                        val chatBubble = payMyAccountViewModel.getApplyNowState()?.let { applyNowState ->
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

        setFragmentResultListener(ViewTreatmentPlanDialogFragment::class.java.simpleName) { _, bundle ->
            CoroutineScope(Dispatchers.Main).doAfterDelay(AppConstant.DELAY_100_MS) {
                when (bundle.getString(ViewTreatmentPlanDialogFragment::class.java.simpleName)) {
                    VIEW_PAYMENT_PLAN_BUTTON -> {
                        activity?.apply {
                            val arguments = HashMap<String, String>()
                            arguments[FirebaseManagerAnalyticsProperties.PropertyNames.ACTION] = FirebaseManagerAnalyticsProperties.VIEW_PAYMENT_PLAN_PERSONAL_LOAN_ACTION
                            Utils.triggerFireBaseEvents(
                                FirebaseManagerAnalyticsProperties.VIEW_PAYMENT_PLAN_PERSONAL_LOAN,
                                arguments,
                                this)
                            when (AppConfigSingleton.accountOptions?.showTreatmentPlanJourney?.renderMode){
                                NATIVE_BROWSER ->
                                    KotlinUtils.openUrlInPhoneBrowser(
                                        AppConfigSingleton.accountOptions?.showTreatmentPlanJourney?.personalLoan?.collectionsUrl, this)

                                else ->
                                    KotlinUtils.openLinkInInternalWebView(activity,
                                        AppConfigSingleton.accountOptions?.showTreatmentPlanJourney?.personalLoan?.collectionsUrl,
                                        true,
                                        AppConfigSingleton.accountOptions?.showTreatmentPlanJourney?.personalLoan?.exitUrl)
                            }
                        }
                    }
                    CANNOT_AFFORD_PAYMENT_BUTTON -> {
                        val intent = Intent(context, GetAPaymentPlanActivity::class.java)
                        intent.putExtra(ViewTreatmentPlanDialogFragment.ELIGIBILITY_PLAN, bundle.getSerializable(ViewTreatmentPlanDialogFragment.ELIGIBILITY_PLAN))
                        startActivityForResult(intent,
                            AccountsOptionFragment.REQUEST_GET_PAYMENT_PLAN
                        )
                        activity?.overridePendingTransition(R.anim.slide_from_right, R.anim.stay)
                    }
                    MAKE_A_PAYMENT_BUTTON -> onPayMyAccountButtonTap()
                }
            }
        }

    }

    override fun onClick(view: View?) {
        KotlinUtils.avoidDoubleClicks(view)
        when (view?.id) {
            R.id.incPayMyAccountButton -> onPayMyAccountButtonTap()

            R.id.incRecentTransactionButton -> {
                activity?.apply { Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTSPERSONALLOANTRANSACTIONS, this) }
                navigateToRecentTransactionActivity(AccountsProductGroupCode.PERSONAL_LOAN.groupCode)
            }
            R.id.incViewStatementButton -> {
                activity?.apply { Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTSPERSONALLOANSTATEMENTS, this) }
                navigateToStatementActivity()
            }
        }
    }

    private fun onPayMyAccountButtonTap() {
        onPayMyAccountButtonTap(
            FirebaseManagerAnalyticsProperties.MYACCOUNTS_PMA_PL,
            PersonalLoanFragmentDirections.actionPersonalLoanFragmentToEnterPaymentAmountDetailFragment()
        )
        if (viewPaymentOptionImageShimmerLayout?.isShimmerStarted == true) return

        activity?.apply { Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTS_PMA_PL, this) }

        if (payMyAccountViewModel.getPaymentMethodType() == PayMyAccountViewModel.PAYUMethodType.ERROR) {
            navController.navigate(R.id.payMyAccountRetryErrorFragment)
            return
        }

        payMyAccountViewModel.resetAmountEnteredToDefault()

        navigateToPayMyAccount {
            navigateSafelyWithNavController(PersonalLoanFragmentDirections.actionPersonalLoanFragmentToEnterPaymentAmountDetailFragment())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PayMyAccountActivity.PAY_MY_ACCOUNT_REQUEST_CODE -> {
                if (resultCode == PMA_TRANSACTION_COMPLETED_RESULT_CODE) {
                    if (NetworkManager.getInstance().isConnectedToNetwork(context))
                        queryPaymentMethod()
                }
            }
        }
    }
}