package za.co.woolworths.financial.services.android.util

import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dto.npc.BlockCardRequestBody
import za.co.woolworths.financial.services.android.models.dto.npc.BlockMyCardResponse
import za.co.woolworths.financial.services.android.models.dto.npc.LinkNewCardOTP
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.UnblockStoreCardRequestBody
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.UnblockStoreCardResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService

class StoreCardAPIRequest {

    fun getOTP(otpMethodType: OTPMethodType, response: IResponseListener<LinkNewCardOTP>) {
        OneAppService.getLinkNewCardOTP(otpMethodType).enqueue(CompletionHandler(object : IResponseListener<LinkNewCardOTP> {
            override fun onSuccess(linkNewCardOTP: LinkNewCardOTP?) {
                linkNewCardOTP?.apply {
                    response.onSuccess(this)
                }
            }

            override fun onFailure(error: Throwable) {
                response.onFailure(error)
            }
        }, LinkNewCardOTP::class.java))
    }

    fun unblockCard(productOfferingId: String, requestBody: UnblockStoreCardRequestBody, response: IResponseListener<UnblockStoreCardResponse>) {
        OneAppService.unblockStoreCard(productOfferingId, requestBody).enqueue(CompletionHandler(object : IResponseListener<UnblockStoreCardResponse> {
            override fun onSuccess(unblockStoreCardResponse: UnblockStoreCardResponse?) {
                unblockStoreCardResponse?.apply {
                    response.onSuccess(this)
                }
            }

            override fun onFailure(error: Throwable) {
                response.onFailure(error)
            }
        }, UnblockStoreCardResponse::class.java))
    }

    fun blockCard(productOfferingId: String, requestBody: BlockCardRequestBody, response: IResponseListener<BlockMyCardResponse>) {
        OneAppService.postBlockMyCard(requestBody, productOfferingId).enqueue(CompletionHandler(object : IResponseListener<BlockMyCardResponse> {
            override fun onSuccess(blockStoreCardResponse: BlockMyCardResponse?) {
                blockStoreCardResponse?.apply {
                    response.onSuccess(this)
                }
            }

            override fun onFailure(error: Throwable) {
                response.onFailure(error)
            }
        }, BlockMyCardResponse::class.java))
    }
}