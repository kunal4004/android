package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.helper

import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.util.Utils

class PMATrackFirebaseEvent {

    fun sendFirebaseEventForAmountEdit(productGroupCode: String) {
        Utils.triggerFireBaseEvents(when (productGroupCode) {
            "sc" -> FirebaseManagerAnalyticsProperties.PMA_SC_AMTEDIT
            "cc" -> FirebaseManagerAnalyticsProperties.PMA_CC_AMTEDIT
            "pl" -> FirebaseManagerAnalyticsProperties.PMA_PL_AMTEDIT
            else -> ""
        })
    }

    fun sendFirebaseEventForPaymentComplete(productGroupCode: String) {
        Utils.triggerFireBaseEvents(when (productGroupCode) {
            "sc" -> FirebaseManagerAnalyticsProperties.PMA_SC_PAY_CMPLT
            "pl" -> FirebaseManagerAnalyticsProperties.PMA_PL_PAY_CMPLT
            "cc" -> FirebaseManagerAnalyticsProperties.PMA_CC_PAY_CMPLT
            else -> ""
        })
    }
}