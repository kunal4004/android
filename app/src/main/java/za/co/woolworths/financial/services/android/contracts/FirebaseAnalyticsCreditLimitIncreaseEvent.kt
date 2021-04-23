package za.co.woolworths.financial.services.android.contracts

import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.util.Utils

class FirebaseAnalyticsCreditLimitIncreaseEvent(private var applyNowState: ApplyNowState?) : FirebaseManagerAnalyticsProperties() {

    fun cliStart() {
        val eventName = when (applyNowState) {
            ApplyNowState.STORE_CARD -> storeCardCreditLimitIncreaseStart
            ApplyNowState.PERSONAL_LOAN -> personalLoanCreditLimitIncreaseStart
            ApplyNowState.BLACK_CREDIT_CARD -> blackCreditCardCreditLimitIncreaseStart
            ApplyNowState.GOLD_CREDIT_CARD -> goldCreditCardCreditLimitIncreaseStart
            ApplyNowState.SILVER_CREDIT_CARD -> silverCreditCardCreditLimitIncreaseStart
            else -> null
        }
        eventName?.let { name -> Utils.triggerFireBaseEvents(name) }
    }


    fun maritialStatus() {
        val eventName = when (applyNowState) {
            ApplyNowState.STORE_CARD -> storeCardCreditLimitIncreaseMaritalstatus
            ApplyNowState.PERSONAL_LOAN -> personalLoanCreditLimitIncreaseMaritalstatus
            ApplyNowState.BLACK_CREDIT_CARD -> blackCreditCardCreditLimitIncreaseMaritalstatus
            ApplyNowState.GOLD_CREDIT_CARD -> goldCreditCardCreditLimitIncreaseMaritalstatus
            ApplyNowState.SILVER_CREDIT_CARD -> silverCreditCardCreditLimitIncreaseMaritalstatus
            else -> null
        }
        eventName?.let { name -> Utils.triggerFireBaseEvents(name) }
    }


    fun incomeExpense() {
        val eventName = when (applyNowState) {
            ApplyNowState.STORE_CARD -> storeCardCreditLimitIncreaseIncomeExpense
            ApplyNowState.PERSONAL_LOAN -> personalLoanCreditLimitIncreaseIncomeExpense
            ApplyNowState.BLACK_CREDIT_CARD -> blackCreditCardCreditLimitIncreaseIncomeExpense
            ApplyNowState.GOLD_CREDIT_CARD -> goldCreditCardCreditLimitIncreaseIncomeExpense
            ApplyNowState.SILVER_CREDIT_CARD -> silverCreditCardCreditLimitIncreaseIncomeExpense
            else -> null
        }
        eventName?.let { name -> Utils.triggerFireBaseEvents(name) }
    }

    fun acceptOffer() {
        val eventName = when (applyNowState) {
            ApplyNowState.STORE_CARD -> storeCardCreditLimitIncreaseAcceptOffer
            ApplyNowState.PERSONAL_LOAN -> personalLoanCreditLimitIncreaseAcceptOffer
            ApplyNowState.BLACK_CREDIT_CARD -> blackCreditCardCreditLimitIncreaseAcceptOffer
            ApplyNowState.GOLD_CREDIT_CARD -> goldCreditCardCreditLimitIncreaseAcceptOffer
            ApplyNowState.SILVER_CREDIT_CARD -> silverCreditCardCreditLimitIncreaseAcceptOffer
            else -> null
        }
        eventName?.let { name -> Utils.triggerFireBaseEvents(name) }
    }

    fun deaOptin() {
        val eventName = when (applyNowState) {
            ApplyNowState.STORE_CARD -> storeCardCreditLimitIncreaseDeaOption
            ApplyNowState.PERSONAL_LOAN -> personalLoanCreditLimitIncreaseDeaOption
            ApplyNowState.BLACK_CREDIT_CARD -> blackCreditCardCreditLimitIncreaseDeaOption
            ApplyNowState.GOLD_CREDIT_CARD -> goldCreditCardCreditLimitIncreaseDeaOption
            ApplyNowState.SILVER_CREDIT_CARD -> silverCreditCardCreditLimitIncreaseDeaOption
            else -> null
        }
        eventName?.let { name -> Utils.triggerFireBaseEvents(name) }
    }

    fun poiConfirm() {
        val eventName = when (applyNowState) {
            ApplyNowState.STORE_CARD -> storeCardCreditLimitIncreasePoiConfirm
            ApplyNowState.PERSONAL_LOAN -> personalLoanCreditLimitIncreasePoiConfirm
            ApplyNowState.BLACK_CREDIT_CARD -> blackCreditCardCreditLimitIncreasePoiConfirm
            ApplyNowState.GOLD_CREDIT_CARD -> goldCreditCardCreditLimitIncreasePoiConfirm
            ApplyNowState.SILVER_CREDIT_CARD -> silverCreditCardCreditLimitIncreasePoiConfirm
            else -> null
        }
        eventName?.let { name -> Utils.triggerFireBaseEvents(name) }
    }
}