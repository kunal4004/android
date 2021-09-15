package za.co.woolworths.financial.services.android.analytic

import android.app.Activity
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.util.Utils

class FirebaseCreditCardDeliveryEvent(private var applyNowState: ApplyNowState?, var activity:Activity) : FirebaseManagerAnalyticsProperties() {

    private val loginCreditCardDelivery = Triple(loginGoldCreditCardDelivery, loginBlackCreditCardDelivery, loginSilverCreditCardDelivery)
    private val loginCreditCardDeliveryLater = Triple(loginGoldCreditCardDeliveryLater, loginBlackCreditCardDeliveryLater, loginSilverCreditCardDeliveryLater)
    private val myAccountCreditCardDelivery = Triple(myAccountGoldCreditCardDelivery, myAccountBlackCreditCardDelivery, myAccountSilverCreditCardDelivery)
    private val creditCardDeliveryConfirm = Triple(goldCreditCardDeliveryConfirm, blackCreditCardDeliveryConfirm, silverCreditCardDeliveryConfirm)
    private val creditCardDeliveryScheduled = Triple(goldCreditCardDeliveryScheduled, blackCreditCardDeliveryScheduled, silverCreditCardDeliveryScheduled)
    private val creditCardManageDelivery = Triple(goldCreditCardManageDelivery, blackCreditCardManageDelivery, silverCreditCardManageDelivery)
    private val creditCardDeliveryCancel = Triple(goldCreditCardDeliveryCancel, blackCreditCardDeliveryCancel, silverCreditCardDeliveryCancel)

    // Customer selects logs into OneApp profile, is prompted to schedule delivery and selects to schedule delivery
    fun forLoginCreditCardDelivery() = sendEvent(loginCreditCardDelivery)

    //Customer selects logs into OneApp profile, is prompted to schedule delivery and elects not to schedule delivery
    fun forLoginCreditCardDeliveryLater() = sendEvent(loginCreditCardDeliveryLater)

    //Customer selects MyAccounts / Black Credit Card / Schedule Your Delivery / Set Up Delivery Now
    fun forMyAccountCreditCardDelivery() = sendEvent(myAccountCreditCardDelivery)

    //Customer selects MyAccounts / Silver Credit Card / Schedule Your Delivery / Set Up Delivery Now / Confirms Delivery Location
    fun forCreditCardDeliveryConfirm() = sendEvent(creditCardDeliveryConfirm)

    //Customer confirmed delivery slot and Delivery was successfully scheduled
    fun forCreditCardDeliveryScheduled() = sendEvent(creditCardDeliveryScheduled)

    //Customer has scheduled delivery and wants to edit the scheduled delivery
    fun forCreditCardManageDelivery() = sendEvent(creditCardManageDelivery)

    //Customer scheduled delivery and wants to cancel scheduled delivery
    fun forCreditCarDeliveryCancel() = sendEvent(creditCardDeliveryCancel)

    private fun sendEvent(eventName: Triple<String, String, String>?) = Utils.triggerFireBaseEvents(when (applyNowState) {
        ApplyNowState.GOLD_CREDIT_CARD -> eventName?.first
        ApplyNowState.BLACK_CREDIT_CARD -> eventName?.second
        else -> eventName?.third
    }, activity)
}