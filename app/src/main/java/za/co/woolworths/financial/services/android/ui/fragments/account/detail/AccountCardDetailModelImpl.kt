package za.co.woolworths.financial.services.android.ui.fragments.account.detail

import za.co.woolworths.financial.services.android.contracts.AccountPaymentOptionsContract
import za.co.woolworths.financial.services.android.contracts.ICommonView
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsRequestBody
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.extension.request

class AccountCardDetailModelImpl : AccountPaymentOptionsContract.AccountCardDetailModel {

    override fun queryServiceGetAccountStoreCardCards(storeCardsRequestBody: StoreCardsRequestBody?, requestListener: ICommonView<Any>) {
        return request(storeCardsRequestBody?.let { body -> OneAppService.getStoreCards(body) }, requestListener)
    }

    override fun queryServiceGetUserCLIOfferActive(productOfferingId: String, requestListener: ICommonView<Any>) {
        return request(OneAppService.getActiveOfferRequest(productOfferingId), requestListener)
    }
}