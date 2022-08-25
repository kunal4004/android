package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.utils

import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.BalanceProtectionInsuranceActivity
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.voc.VoiceOfCustomerManager
import za.co.woolworths.financial.services.android.util.wenum.VocTriggerEvent
import javax.inject.Inject

interface StoreCardActivityResult {
    fun balanceProtectionInsuranceCallback(result: ActivityResult) : Account?
    fun linkNewCardCallback(result: ActivityResult): Boolean
}

class StoreCardActivityResultCallback @Inject constructor() : StoreCardActivityResult {

    override fun balanceProtectionInsuranceCallback(result: ActivityResult): Account? {
       return when (result.resultCode) {
            AppConstant.BALANCE_PROTECTION_INSURANCE_OPT_IN_SUCCESS_RESULT_CODE -> {
                val extras = result.data?.extras
                val response =
                    extras?.getString(BalanceProtectionInsuranceActivity.ACCOUNT_RESPONSE)
                return Gson().fromJson(response, Account::class.java)
            }
           else -> null
       }
    }

   override fun linkNewCardCallback(result: ActivityResult): Boolean {
        return when (result.resultCode) {
            MyCardDetailActivity.TEMPORARY_FREEZE_STORE_CARD_RESULT_CODE -> {
                with(result) {
                    val shouldRefreshCardDetails =
                        data?.extras?.getBoolean(
                            MyCardDetailActivity.REFRESH_MY_CARD_DETAILS,
                            false
                        )
                    if (shouldRefreshCardDetails == true) {
                        VoiceOfCustomerManager.pendingTriggerEvent =
                            VocTriggerEvent.MYACCOUNTS_BLOCKCARD_CONFIRM
                        return shouldRefreshCardDetails
                    }
                }
                false
            }
            MyCardDetailActivity.ACTIVATE_VIRTUAL_TEMP_CARD_RESULT_CODE -> {
                //ICR Journey success and When Get replacement card email confirmation is success and result ok
                VoiceOfCustomerManager.pendingTriggerEvent =
                    VocTriggerEvent.MYACCOUNTS_ICR_LINK_CONFIRM
                true
            }
            AppCompatActivity.RESULT_OK -> {
                true
            }
            else -> false
        }
    }
}