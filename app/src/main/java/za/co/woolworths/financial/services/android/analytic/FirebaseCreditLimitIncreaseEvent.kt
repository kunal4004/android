package za.co.woolworths.financial.services.android.analytic

import android.app.Activity
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.util.Utils

class FirebaseCreditLimitIncreaseEvent(private var applyNowState: ApplyNowState?, var activity: Activity) : FirebaseManagerAnalyticsProperties() {

    //Customer selects MyAccounts/ Credit Card / Increase My Limit
    private val cLIStart = mutableListOf(
            storeCardCreditLimitIncreaseStart,
            personalLoanCreditLimitIncreaseStart,
            blackCreditCardCreditLimitIncreaseStart,
            goldCreditCardCreditLimitIncreaseStart,
            silverCreditCardCreditLimitIncreaseStart)

    //Customer started Credit Card limit increase and confirmed marital status
    private val cliMaritialStatus = mutableListOf(
            storeCardCreditLimitIncreaseMaritalstatus,
            personalLoanCreditLimitIncreaseMaritalstatus,
            blackCreditCardCreditLimitIncreaseMaritalstatus,
            goldCreditCardCreditLimitIncreaseMaritalstatus,
            silverCreditCardCreditLimitIncreaseMaritalstatus)

    //Customer confirmed marital status and provided income / expenses
    private val incomeExpense = mutableListOf(
            storeCardCreditLimitIncreaseIncomeExpense,
            personalLoanCreditLimitIncreaseIncomeExpense,
            blackCreditCardCreditLimitIncreaseIncomeExpense,
            goldCreditCardCreditLimitIncreaseIncomeExpense,
            silverCreditCardCreditLimitIncreaseIncomeExpense)

    //Store Card Customer received offer and accepted
    private val acceptOffer = mutableListOf(
            storeCardCreditLimitIncreaseAcceptOffer,
            personalLoanCreditLimitIncreaseAcceptOffer,
            blackCreditCardCreditLimitIncreaseAcceptOffer,
            goldCreditCardCreditLimitIncreaseAcceptOffer,
            silverCreditCardCreditLimitIncreaseAcceptOffer)

    //Store Card Customer accepted and Opt-is for DEA
    private val deaOptin = mutableListOf(
            storeCardCreditLimitIncreaseDeaOption,
            personalLoanCreditLimitIncreaseDeaOption,
            blackCreditCardCreditLimitIncreaseDeaOption,
            goldCreditCardCreditLimitIncreaseDeaOption,
            silverCreditCardCreditLimitIncreaseDeaOption)

    //Store Card Customer accepted and selected POI
    private val poiConfirm = mutableListOf(
            storeCardCreditLimitIncreasePoiConfirm,
            personalLoanCreditLimitIncreasePoiConfirm,
            blackCreditCardCreditLimitIncreasePoiConfirm,
            goldCreditCardCreditLimitIncreasePoiConfirm,
            silverCreditCardCreditLimitIncreasePoiConfirm)

    fun forCLIStart() = sendEvent(cLIStart)

    fun forMaritialStatus() = sendEvent(cliMaritialStatus)

    fun forIncomeExpense() = sendEvent(incomeExpense)

    fun forAcceptOffer() = sendEvent(acceptOffer)

    fun forDeaOptin() = sendEvent(deaOptin)

    fun forPOIConfirm() = sendEvent(poiConfirm)

    private fun sendEvent(eventName: MutableList<String>?) {
        val name = when (applyNowState) {
            ApplyNowState.STORE_CARD -> eventName?.get(0)
            ApplyNowState.PERSONAL_LOAN -> eventName?.get(1)
            ApplyNowState.BLACK_CREDIT_CARD -> eventName?.get(2)
            ApplyNowState.GOLD_CREDIT_CARD -> eventName?.get(3)
            ApplyNowState.SILVER_CREDIT_CARD -> eventName?.get(4)
            else -> null
        }
        name?.let { n -> Utils.triggerFireBaseEvents(n,activity) }
    }
}