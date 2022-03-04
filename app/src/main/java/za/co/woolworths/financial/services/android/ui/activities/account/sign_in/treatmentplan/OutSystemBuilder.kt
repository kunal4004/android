package za.co.woolworths.financial.services.android.ui.activities.account.sign_in.treatmentplan

import android.app.Activity
import android.os.Bundle
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlan
import za.co.woolworths.financial.services.android.models.dto.ProductGroupCode
import za.co.woolworths.financial.services.android.models.dto.app_config.account_options.ConfigShowTreatmentPlan
import za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.AvailableFundFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.ViewTreatmentPlanDialogFragment
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils

interface IOutSystemWebUrl {
    fun build()
    fun getBundleKey(): String
    fun triggerAnalyticsProperties(key: String, action: String)
    fun renderMode(eligibilityPlan: EligibilityPlan?)
    fun getFirebaseAnalyticKey(): String
    fun getFirebaseAnalyticAction(): String

}

class OutSystemBuilder(
    private val activity: Activity?,
    private val productGroupCode: ProductGroupCode? = null,
    private val eligibilityPlan: EligibilityPlan? = null,
    private val bundle: Bundle?  = null
) : IOutSystemWebUrl {

    override fun build() {
        val key: String = getFirebaseAnalyticKey()
        val action: String = getFirebaseAnalyticAction()
        val eligibilityPlan: EligibilityPlan? = getEligibilityPlan()
        triggerAnalyticsProperties(key, action)
        renderMode(eligibilityPlan = eligibilityPlan)
    }

    private fun getEligibilityPlan(): EligibilityPlan? =
        bundle?.getSerializable(ViewTreatmentPlanDialogFragment.VIEW_PAYMENT_PLAN_BUTTON) as? EligibilityPlan ?: eligibilityPlan

    override fun getFirebaseAnalyticAction(): String = when (productGroupCode) {
        ProductGroupCode.SC -> FirebaseManagerAnalyticsProperties.VIEW_PAYMENT_PLAN_STORE_CARD_ACTION
        ProductGroupCode.PL -> FirebaseManagerAnalyticsProperties.VIEW_PAYMENT_PLAN_PERSONAL_LOAN_ACTION
        else -> FirebaseManagerAnalyticsProperties.VIEW_PAYMENT_PLAN_CREDIT_CARD_ACTION
    }

    override fun getFirebaseAnalyticKey(): String = when (productGroupCode) {
        ProductGroupCode.SC -> FirebaseManagerAnalyticsProperties.VIEW_PAYMENT_PLAN_STORE_CARD
        ProductGroupCode.PL -> FirebaseManagerAnalyticsProperties.VIEW_PAYMENT_PLAN_PERSONAL_LOAN
        else -> FirebaseManagerAnalyticsProperties.VIEW_PAYMENT_PLAN_CREDIT_CARD
    }

    override fun getBundleKey(): String = bundle?.keySet()?.toList()?.get(0)?.toString() ?: ""

    override fun triggerAnalyticsProperties(key: String, action: String) {
        val arguments = HashMap<String, String>()
        arguments[FirebaseManagerAnalyticsProperties.PropertyNames.ACTION] = action
        Utils.triggerFireBaseEvents(key, arguments, activity)
    }

    override fun renderMode(eligibilityPlan: EligibilityPlan?) {

        val options: ConfigShowTreatmentPlan? = when (eligibilityPlan == null) {
            true -> AppConfigSingleton.accountOptions?.showTreatmentPlanJourney
            false -> AppConfigSingleton.accountOptions?.collectionsStartNewPlanJourney
        }

        options?.apply {

            var collectionUrlFromConfig = when (productGroupCode) {
                ProductGroupCode.SC -> storeCard.collectionsUrl
                ProductGroupCode.PL -> personalLoan.collectionsUrl
                else -> creditCard.collectionsUrl
            }

            val exitUrl = when (productGroupCode) {
                ProductGroupCode.SC -> storeCard.exitUrl
                ProductGroupCode.PL -> personalLoan.exitUrl
                else -> creditCard.exitUrl
            }

            if (eligibilityPlan != null) {
                collectionUrlFromConfig += eligibilityPlan.appGuid
            }

            when (renderMode) {
                AvailableFundFragment.NATIVE_BROWSER -> KotlinUtils.openUrlInPhoneBrowser(
                    collectionUrlFromConfig,
                    activity
                )
                else -> KotlinUtils.openLinkInInternalWebView(
                    activity,
                    collectionUrlFromConfig,
                    true,
                    exitUrl
                )
            }
        }
    }
}