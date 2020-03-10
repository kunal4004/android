package za.co.woolworths.financial.services.android.ui.fragments.account.detail.card

import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.IAccountCardDetailsContract
import za.co.woolworths.financial.services.android.contracts.IGenericAPILoaderView
import za.co.woolworths.financial.services.android.models.dto.OfferActive
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsRequestBody
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.extension.request

class AccountCardDetailModelImpl : IAccountCardDetailsContract.AccountCardDetailModel {

    override fun queryServiceGetAccountStoreCardCards(storeCardsRequestBody: StoreCardsRequestBody?, requestListener: IGenericAPILoaderView<Any>): Call<StoreCardsResponse>? {
        return request(storeCardsRequestBody?.let { body -> OneAppService.getStoreCards(body) }, requestListener)
    }

    override fun queryServiceGetUserCLIOfferActive(productOfferingId: String, requestListener: IGenericAPILoaderView<Any>): Call<OfferActive>? {
        return request(OneAppService.getActiveOfferRequest(productOfferingId), requestListener)
    }
}