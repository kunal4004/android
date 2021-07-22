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
import kotlinx.coroutines.GlobalScope
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity
import za.co.woolworths.financial.services.android.ui.extension.doAfterDelay
import za.co.woolworths.financial.services.android.ui.extension.safeNavigateFromNavController
import za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.AvailableFundFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatFloatingActionButtonBubbleView
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PMA3DSecureProcessRequestFragment.Companion.PMA_TRANSACTION_COMPLETED_RESULT_CODE
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.AccountInArrearsDialogFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.AccountInArrearsDialogFragment.Companion.ARREARS_CHAT_TO_US_BUTTON
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.AccountInArrearsDialogFragment.Companion.ARREARS_PAY_NOW_BUTTON
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.NetworkManager
import za.co.woolworths.financial.services.android.util.Utils

class PersonalLoanFragment : AvailableFundFragment(), View.OnClickListener {

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

        setFragmentResultListener(AccountInArrearsDialogFragment::class.java.simpleName) { _, bundle ->
            GlobalScope.doAfterDelay(AppConstant.DELAY_100_MS) {
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
        if (viewPaymentOptionImageShimmerLayout?.isShimmerStarted == true) return

        activity?.apply { Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTS_PMA_PL, this) }

        if (payMyAccountViewModel.getPaymentMethodType() == PayMyAccountViewModel.PAYUMethodType.ERROR) {
            navController.navigate(R.id.payMyAccountRetryErrorFragment)
            return
        }

        payMyAccountViewModel.resetAmountEnteredToDefault()

        navigateToPayMyAccount {
            safeNavigateFromNavController(PersonalLoanFragmentDirections.actionPersonalLoanFragmentToEnterPaymentAmountDetailFragment())
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