package za.co.woolworths.financial.services.android.ui.fragments.account

import za.co.woolworths.financial.services.android.contracts.RequestListener
import za.co.woolworths.financial.services.android.models.dto.AccountsResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService

open class AccountRequest {
    fun make(forceNetworkUpdate: Boolean, responseListener: RequestListener<AccountsResponse>?) {
        val oneAppService = OneAppService
        oneAppService.forceNetworkUpdate = forceNetworkUpdate
        return oneAppService.getAccounts().enqueue(CompletionHandler(object : RequestListener<AccountsResponse> {
            override fun onSuccess(accountsResponse: AccountsResponse?) {
                responseListener?.onSuccess(accountsResponse)
            }

            override fun onFailure(error: Throwable?) {
                responseListener?.onFailure(error)
            }

        }, AccountsResponse::class.java))
    }
}