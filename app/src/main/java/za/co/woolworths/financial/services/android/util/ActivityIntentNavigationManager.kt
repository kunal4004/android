package za.co.woolworths.financial.services.android.util

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.dto.AddCardResponse
import za.co.woolworths.financial.services.android.models.dto.PaymentAmountCard
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountPresenterImpl
import za.co.woolworths.financial.services.android.util.wenum.PayMyAccountStartDestinationType

object ActivityIntentNavigationManager {

    fun presentPayMyAccountActivity(activity: Activity?, paymentAmountCard: PaymentAmountCard?) {
        val howToPayIntent = Intent(activity, PayMyAccountActivity::class.java)
        howToPayIntent.putExtra(PayMyAccountActivity.PAYMENT_DETAIL_CARD_UPDATE, paymentAmountCard)
        activity?.startActivityForResult(howToPayIntent, PayMyAccountActivity.PAY_MY_ACCOUNT_REQUEST_CODE)
        activity?.overridePendingTransition(R.anim.slide_up_fast_anim, R.anim.stay)
    }

    fun presentPayMyAccountActivity(activity: Activity?,cardResponse: AddCardResponse?, paymentAmountCard: PaymentAmountCard?, payMyAccountStartDestinationType: PayMyAccountStartDestinationType?, isDoneButtonEnabled: Boolean = false) {
        val payMyAccountIntent = Intent(activity, PayMyAccountActivity::class.java)
        val bundle = Bundle().apply {
            putSerializable(PayMyAccountPresenterImpl.GET_CARD_RESPONSE, cardResponse)
            putSerializable(PayMyAccountActivity.PAYMENT_DETAIL_CARD_UPDATE, paymentAmountCard)
            putSerializable(PayMyAccountPresenterImpl.SCREEN_TYPE, payMyAccountStartDestinationType)
            putBoolean(PayMyAccountPresenterImpl.IS_DONE_BUTTON_ENABLED, isDoneButtonEnabled)
        }
        payMyAccountIntent.putExtra(PayMyAccountActivity.PAY_MY_ACCOUNT_BUNDLE_EXTRAS, bundle)
        activity?.startActivityForResult(payMyAccountIntent, PayMyAccountActivity.PAY_MY_ACCOUNT_REQUEST_CODE)
        activity?.overridePendingTransition(R.anim.slide_up_fast_anim, R.anim.stay)
    }
}