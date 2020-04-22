package za.co.woolworths.financial.services.android.ui.fragments.account.available_fund

import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.IAvailableFundsContract
import za.co.woolworths.financial.services.android.contracts.IGenericAPILoaderView
import za.co.woolworths.financial.services.android.models.dto.CreditCardTokenResponse
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.extension.request

class AvailableFundsModelImpl : IAvailableFundsContract.AvailableFundsModel  {

    override fun queryABSAServiceGetUserCreditCardToken(requestListener: IGenericAPILoaderView<Any>): Call<CreditCardTokenResponse>? {
        return request(OneAppService.getCreditCardToken(), requestListener)
    }
}