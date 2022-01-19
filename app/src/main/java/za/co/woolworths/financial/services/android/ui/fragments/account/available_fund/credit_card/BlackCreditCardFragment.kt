package za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.credit_card

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.available_funds_fragment.*
import kotlinx.coroutines.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.ui.activities.GetAPaymentPlanActivity
import za.co.woolworths.financial.services.android.ui.extension.doAfterDelay

import za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.AvailableFundFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.helper.FirebaseEventDetailManager

import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.ViewTreatmentPlanDialogFragment
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils

class BlackCreditCardFragment : AvailableFundFragment(), View.OnClickListener {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        availableFundBackground?.setBackgroundResource(R.drawable.black_credit_card_background)

        initShimmer()
        stopProgress()

        incViewStatementButton?.visibility = if (WoolworthsApplication.getAbsaBankingOpenApiServices()?.isEnabled == true) View.VISIBLE else View.GONE

        incRecentTransactionButton?.setOnClickListener(this)
        incViewStatementButton?.setOnClickListener(this)
        incPayMyAccountButton?.setOnClickListener(this)

        navigateToDeepLinkView()

        accountInArrearsResultListener {
            onPayMyAccountButtonTap()
        }

        setFragmentResultListener(ViewTreatmentPlanDialogFragment::class.java.simpleName) { _, bundle ->
            CoroutineScope(Dispatchers.Main).doAfterDelay(AppConstant.DELAY_100_MS) {
                when (bundle.getString(ViewTreatmentPlanDialogFragment::class.java.simpleName)) {
                    ViewTreatmentPlanDialogFragment.VIEW_PAYMENT_PLAN_BUTTON -> {
                        activity?.apply {
                            val arguments = HashMap<String, String>()
                            arguments[FirebaseManagerAnalyticsProperties.PropertyNames.ACTION] = FirebaseManagerAnalyticsProperties.VIEW_PAYMENT_PLAN_CREDIT_CARD_ACTION
                            Utils.triggerFireBaseEvents(
                                FirebaseManagerAnalyticsProperties.VIEW_PAYMENT_PLAN_CREDIT_CARD,
                                arguments,
                                this)
                            when (WoolworthsApplication.getAccountOptions()?.showTreatmentPlanJourney?.renderMode){
                                NATIVE_BROWSER ->
                                    KotlinUtils.openUrlInPhoneBrowser(
                                        WoolworthsApplication.getAccountOptions()?.showTreatmentPlanJourney?.creditCard?.collectionsUrl, this)

                                else ->
                                    KotlinUtils.openLinkInInternalWebView(activity,
                                        WoolworthsApplication.getAccountOptions()?.showTreatmentPlanJourney?.creditCard?.collectionsUrl,
                                        true,
                                        WoolworthsApplication.getAccountOptions()?.showTreatmentPlanJourney?.creditCard?.exitUrl)
                            }
                        }
                    }

                    ViewTreatmentPlanDialogFragment.CANNOT_AFFORD_PAYMENT_BUTTON -> {
                        val intent = Intent(context, GetAPaymentPlanActivity::class.java)
                        intent.putExtra(ViewTreatmentPlanDialogFragment.ELIGIBILITY_PLAN, bundle.getSerializable(ViewTreatmentPlanDialogFragment.ELIGIBILITY_PLAN))
                        startActivity(intent)
                        activity?.overridePendingTransition(R.anim.slide_from_right, R.anim.stay)
                    }

                    ViewTreatmentPlanDialogFragment.MAKE_A_PAYMENT_BUTTON -> navigateToPayMyAccountActivity()
                }
            }
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.incRecentTransactionButton -> {
                activity?.runOnUiThread {
                    activity?.runOnUiThread {
                        activity?.apply { FirebaseEventDetailManager.tapped(FirebaseManagerAnalyticsProperties.MYACCOUNTSCREDITCARDTRANSACTIONS, this) }
                        navigateToRecentTransactionActivity(AccountsProductGroupCode.CREDIT_CARD.groupCode)
                    }
                }
            }
            R.id.incPayMyAccountButton -> onPayMyAccountButtonTap()
            R.id.incViewStatementButton -> navigateToABSAStatementActivity()
        }
    }

    private fun onPayMyAccountButtonTap() {
        onPayMyAccountButtonTap(
            FirebaseManagerAnalyticsProperties.MYACCOUNTS_PMA_CC,
            BlackCreditCardFragmentDirections.actionBlackCreditCardFragmentToEnterPaymentAmountDetailFragment()
        )
    }
}