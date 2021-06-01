package za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.store_card

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.Navigation
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.account_available_fund_overview_fragment.*
import kotlinx.android.synthetic.main.view_pay_my_account_button.*
import kotlinx.coroutines.GlobalScope

import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity.Companion.PAY_MY_ACCOUNT_REQUEST_CODE
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.AvailableFundFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PMA3DSecureProcessRequestFragment.Companion.PMA_TRANSACTION_COMPLETED_RESULT_CODE
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity
import za.co.woolworths.financial.services.android.ui.extension.doAfterDelay
import za.co.woolworths.financial.services.android.ui.extension.safeNavigateFromNavController
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatFloatingActionButtonBubbleView
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.AccountInArrearsDialogFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.AccountInArrearsDialogFragment.Companion.ARREARS_CHAT_TO_US_BUTTON
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.AccountInArrearsDialogFragment.Companion.ARREARS_PAY_NOW_BUTTON
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


        setFragmentResultListener(AccountInArrearsDialogFragment::class.java.simpleName) { _, bundle ->
            GlobalScope.doAfterDelay(AppConstant.DELAY_100_MS) {
                when (bundle.getString(AccountInArrearsDialogFragment::class.java.simpleName, "N/A")) {
                    ARREARS_PAY_NOW_BUTTON -> onStoreCardButtonTap()
                    ARREARS_CHAT_TO_US_BUTTON -> {
                        val chatBubble = payMyAccountViewModel.getApplyNowState()?.let { applyNowState -> ChatFloatingActionButtonBubbleView(activity = activity as? AccountSignedInActivity, applyNowState = applyNowState) }
                        chatBubble?.navigateToChatActivity(activity, payMyAccountViewModel.getCardDetail()?.account?.second)
                    }
                }
            }
        }
    }

    override fun onClick(view: View?) {
        KotlinUtils.avoidDoubleClicks(view)
        when (view?.id) {
            R.id.incRecentTransactionButton -> {
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTSSTORECARDTRANSACTIONS)
                navigateToRecentTransactionActivity(AccountsProductGroupCode.STORE_CARD.groupCode)
            }
            R.id.incViewStatementButton -> {
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTSSTORECARDSTATEMENTS)
                navigateToStatementActivity()
            }
            R.id.incPayMyAccountButton -> onStoreCardButtonTap()
        }
    }

    private fun onStoreCardButtonTap() {
        if (viewPaymentOptionImageShimmerLayout?.isShimmerStarted == true) return

        payMyAccountViewModel.resetAmountEnteredToDefault()

        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTS_PMA_SC)

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
                safeNavigateFromNavController(StoreCardFragmentDirections.storeCardFragmentToDisplayVendorDetailFragmentAction())

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