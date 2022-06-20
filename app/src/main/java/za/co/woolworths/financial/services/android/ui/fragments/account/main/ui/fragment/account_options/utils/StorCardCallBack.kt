package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.utils

import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AppCompatActivity
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity
import za.co.woolworths.financial.services.android.util.voc.VoiceOfCustomerManager
import za.co.woolworths.financial.services.android.util.wenum.VocTriggerEvent

class StorCardCallBack{

    fun linkNewCardCallBack(
        result: ActivityResult,
    ): Boolean {
        when (result.resultCode) {
            MyCardDetailActivity.TEMPORARY_FREEZE_STORE_CARD_RESULT_CODE -> {
                result.apply {
                    val shouldRefreshCardDetails =
                        result.data?.extras?.getBoolean(
                            MyCardDetailActivity.REFRESH_MY_CARD_DETAILS,
                            false
                        )
                    if (shouldRefreshCardDetails == true) {
                        VoiceOfCustomerManager.pendingTriggerEvent =
                            VocTriggerEvent.MYACCOUNTS_BLOCKCARD_CONFIRM
                        return shouldRefreshCardDetails
                    }
                }
            }
            MyCardDetailActivity.ACTIVATE_VIRTUAL_TEMP_CARD_RESULT_CODE -> {
                //ICR Journey success and When Get replacement card email confirmation is success and result ok
                VoiceOfCustomerManager.pendingTriggerEvent = VocTriggerEvent.MYACCOUNTS_ICR_LINK_CONFIRM
                return true
            }
            AppCompatActivity.RESULT_OK -> {
                return true
            }
        }
        return false
    }
}