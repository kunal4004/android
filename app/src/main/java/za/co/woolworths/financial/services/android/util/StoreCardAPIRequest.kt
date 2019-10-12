package za.co.woolworths.financial.services.android.util

import za.co.woolworths.financial.services.android.contracts.RequestListener
import za.co.woolworths.financial.services.android.models.dto.npc.LinkNewCardOTP
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.UnblockStoreCardRequestBody
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.UnblockStoreCardResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService

class StoreCardAPIRequest {

    fun getOTP(otpMethodType: OTPMethodType, response: RequestListener<LinkNewCardOTP>) {
        OneAppService.getLinkNewCardOTP(otpMethodType).enqueue(CompletionHandler(object : RequestListener<LinkNewCardOTP> {
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

    fun unblockCard(productOfferingId: String, requestBody: UnblockStoreCardRequestBody, response: RequestListener<UnblockStoreCardResponse>) {
        OneAppService.unblockStoreCard(productOfferingId, requestBody).enqueue(CompletionHandler(object : RequestListener<UnblockStoreCardResponse> {
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
}