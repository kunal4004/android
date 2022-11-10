package za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.credit_card

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.Navigation
import com.awfs.coordination.R
import kotlinx.coroutines.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.ProductGroupCode
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.treatmentplan.OutSystemBuilder
import za.co.woolworths.financial.services.android.ui.extension.doAfterDelay
import za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.AvailableFundFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.helper.FirebaseEventDetailManager
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.ViewTreatmentPlanDialogFragment
import za.co.woolworths.financial.services.android.util.AppConstant

class GoldCreditCardFragment : AvailableFundFragment(), View.OnClickListener {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            availableFundBackground?.setBackgroundResource(R.drawable.gold_credit_card_background)

            navController = Navigation.findNavController(view)

            binding.initShimmer()
            binding.stopProgress()

            incViewStatementButton?.root?.visibility =
                if (AppConfigSingleton.absaBankingOpenApiServices?.isEnabled == true) View.VISIBLE else View.GONE
            incRecentTransactionButton?.root?.setOnClickListener(this@GoldCreditCardFragment)
            incPayMyAccountButton?.root?.setOnClickListener(this@GoldCreditCardFragment)
            incViewStatementButton?.root?.setOnClickListener(this@GoldCreditCardFragment)
        }

        navigateToDeepLinkView()

        accountInArrearsResultListener {
            onPayMyAccountButtonTap()
        }

        setFragmentResultListener(ViewTreatmentPlanDialogFragment::class.java.simpleName) { _, bundle ->
            CoroutineScope(Dispatchers.Main).doAfterDelay(AppConstant.DELAY_100_MS) {
                val outSystemBuilder = OutSystemBuilder(activity, ProductGroupCode.CC, bundle = bundle)
                when (outSystemBuilder.getBundleKey()) {
                    ViewTreatmentPlanDialogFragment.VIEW_PAYMENT_PLAN_BUTTON -> outSystemBuilder.build()
                    ViewTreatmentPlanDialogFragment.CANNOT_AFFORD_PAYMENT_BUTTON -> startGetAPaymentPlanActivity(bundle)
                    ViewTreatmentPlanDialogFragment.MAKE_A_PAYMENT_BUTTON -> navigateToPayMyAccountActivity()
                }
            }
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.incRecentTransactionButton -> {
                activity?.runOnUiThread {
                    activity?.apply { FirebaseEventDetailManager.tapped(FirebaseManagerAnalyticsProperties.MYACCOUNTSCREDITCARDTRANSACTIONS, this) }
                    navigateToRecentTransactionActivity(AccountsProductGroupCode.CREDIT_CARD.groupCode)
                }
            }
            R.id.incPayMyAccountButton -> onPayMyAccountButtonTap()

            R.id.incViewStatementButton -> navigateToABSAStatementActivity()
        }
    }

    private fun onPayMyAccountButtonTap() {
        onPayMyAccountButtonTap(
            FirebaseManagerAnalyticsProperties.MYACCOUNTS_PMA_CC,
            GoldCreditCardFragmentDirections.actionGoldCreditCardFragmentToEnterPaymentAmountDetailFragment())

    }

    }
