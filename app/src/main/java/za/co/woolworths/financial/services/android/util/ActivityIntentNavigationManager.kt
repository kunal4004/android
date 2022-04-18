package za.co.woolworths.financial.services.android.util

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import com.awfs.coordination.R
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.models.dto.PMACardPopupModel
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl.Companion.ELITE_PLAN_MODEL
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountPresenterImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.card.AccountsOptionFragment
import za.co.woolworths.financial.services.android.util.wenum.PayMyAccountStartDestinationType


object ActivityIntentNavigationManager {

    fun presentPayMyAccountActivity(activity: Activity?, pmaCardPopupModel: PMACardPopupModel?) {
        val payMyAccountIntent = Intent(activity, PayMyAccountActivity::class.java)
        payMyAccountIntent.putExtra(
            PayMyAccountActivity.PAYMENT_DETAIL_CARD_UPDATE,
            Gson().toJson(pmaCardPopupModel)
        )
        activity?.startActivityForResult(
            payMyAccountIntent,
            PayMyAccountActivity.PAY_MY_ACCOUNT_REQUEST_CODE
        )
        activity?.overridePendingTransition(R.anim.slide_up_fast_anim, R.anim.stay)
    }

    fun presentPayMyAccountActivity(
        activity: Activity?,
        pmaCardPopupModel: PMACardPopupModel?,
        payMyAccountStartDestinationType: PayMyAccountStartDestinationType?,
        isDoneButtonEnabled: Boolean = false,
        elitePlanModel: Parcelable? = null
    ) {
        val payMyAccountIntent = Intent(activity, PayMyAccountActivity::class.java).apply {
            putExtra(
                PayMyAccountActivity.PAYMENT_DETAIL_CARD_UPDATE,
                Gson().toJson(pmaCardPopupModel)
            )
            putExtra(PayMyAccountPresenterImpl.SCREEN_TYPE, payMyAccountStartDestinationType)
            putExtra(PayMyAccountPresenterImpl.IS_DONE_BUTTON_ENABLED, isDoneButtonEnabled)
            //Elite plan values
            putExtra(ELITE_PLAN_MODEL, elitePlanModel)

        }

        var requestCode = PayMyAccountActivity.PAY_MY_ACCOUNT_REQUEST_CODE

        if (elitePlanModel!=null){
            // for reloading eligibility when back from PMA
            requestCode = AccountsOptionFragment.REQUEST_ELITEPLAN
        }
        activity?.startActivityForResult(
            payMyAccountIntent,
            requestCode
        )
        activity?.overridePendingTransition(R.anim.slide_up_fast_anim, R.anim.stay)
    }
}