package za.co.woolworths.financial.services.android.util

import com.google.gson.Gson
import za.co.woolworths.financial.services.android.models.dto.AccountsResponse

class MyAccountHelper {

    fun getAccountInfo(accountsResponse: AccountsResponse, desiredSection: String): String {
        val accountList = accountsResponse.accountList
        if (accountList != null) {
            for (account in accountList) {
                if (desiredSection == account.productGroupCode) {
                    return Gson().toJson(account)
                }
            }
        }
        return ""
    }
}