package za.co.woolworths.financial.services.android.ui.fragments.account.available_fund

import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.AvailableFundsContract
import za.co.woolworths.financial.services.android.contracts.ICommonView
import za.co.woolworths.financial.services.android.models.dto.CreditCardTokenResponse
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.extension.request

class AvailableFundsModelImpl : AvailableFundsContract.AvailableFundsModel  {

    override fun queryABSAServiceGetUserCreditCardToken(requestListener: ICommonView<Any>): Call<CreditCardTokenResponse>? {
        return request(OneAppService.getCreditCardToken(), requestListener)
    }
}