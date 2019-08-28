package za.co.woolworths.financial.services.android.ui.fragments.product.grid

import za.co.woolworths.financial.services.android.contracts.RequestListener
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService

class AddToCart {

    fun queryInventoryForStore(storeId: String, skuIds: String) {
        OneAppService.getInventorySkuForStore(storeId, skuIds).enqueue(CompletionHandler(object : RequestListener<SkusInventoryForStoreResponse> {
            override fun onSuccess(skusInventoryForStoreResponse: SkusInventoryForStoreResponse) {
                when (skusInventoryForStoreResponse.httpCode) {
                    200 -> {
                    }
                }
            }

            override fun onFailure(error: Throwable) {

            }
        }, SkusInventoryForStoreResponse::class.java))
    }
}