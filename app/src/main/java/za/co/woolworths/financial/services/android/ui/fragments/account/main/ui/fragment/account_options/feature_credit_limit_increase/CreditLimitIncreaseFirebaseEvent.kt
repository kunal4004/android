package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_credit_limit_increase

import android.app.Activity
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.AccountProductLandingDao
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

interface ICreditLimitIncreaseFirebaseEvent {
    val onStartData: MutableList<String>
    fun triggerFirebaseEventForCliStartDestination(activity: Activity?)
    fun sendEvent(eventName: MutableList<String>?, activity: Activity?)
}

class CreditLimitIncreaseFirebaseEvent @Inject constructor(private val landingDao: AccountProductLandingDao) :
    ICreditLimitIncreaseFirebaseEvent {

    //Customer selects MyAccounts/ Credit Card / Increase My Limit
    override val onStartData: MutableList<String>
        get() = mutableListOf(
            FirebaseManagerAnalyticsProperties.storeCardCreditLimitIncreaseStart,
            FirebaseManagerAnalyticsProperties.personalLoanCreditLimitIncreaseStart,
            FirebaseManagerAnalyticsProperties.blackCreditCardCreditLimitIncreaseStart,
            FirebaseManagerAnalyticsProperties.goldCreditCardCreditLimitIncreaseStart,
            FirebaseManagerAnalyticsProperties.silverCreditCardCreditLimitIncreaseStart
        )

    override fun triggerFirebaseEventForCliStartDestination(activity: Activity?) = sendEvent(onStartData, activity)

    override fun sendEvent(eventName: MutableList<String>?, activity: Activity?) {
        val firebaseName = when (landingDao.getApplyNowState()) {
            ApplyNowState.STORE_CARD -> eventName?.get(0)
            ApplyNowState.PERSONAL_LOAN -> eventName?.get(1)
            ApplyNowState.BLACK_CREDIT_CARD -> eventName?.get(2)
            ApplyNowState.GOLD_CREDIT_CARD -> eventName?.get(3)
            ApplyNowState.SILVER_CREDIT_CARD -> eventName?.get(4)
            else -> null
        }
        firebaseName?.let { name -> Utils.triggerFireBaseEvents(name, activity) }
    }
}