package za.co.woolworths.financial.services.android.ui.fragments.product.shop.usecase.fake

import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.recommendations.data.repository.RecommendationsRepository
import za.co.woolworths.financial.services.android.recommendations.data.response.getresponse.RecommendationResponse
import za.co.woolworths.financial.services.android.recommendations.data.response.request.RecommendationRequest
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.usecase.Constants
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.usecase.Constants.PRODUCT_ID_FOR_DISCOUNT
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.usecase.Constants.PRODUCT_ID_FOR_SHIPPING

class RecommendationsRepositoryImplFake : RecommendationsRepository {

    companion object {
        fun verifyValidRequest(request: RecommendationRequest?): Boolean {
            val mId = request?.monetateId
            val events = request?.events
            if (mId.isNullOrEmpty() || events.isNullOrEmpty() || events.size != 1) {
                return false
            }
            val event = events[0]
            if (event.eventType != Constants.EVENT_TYPE_PURCHASE || event.purchaseId.isNullOrEmpty() || event.purchaseLines.isNullOrEmpty()) {
                return false
            }

            event.purchaseLines!!.forEach { cartProduct ->
                if (cartProduct?.pid.isNullOrEmpty() || cartProduct?.pid != cartProduct?.sku || cartProduct?.currency != Constants.CURRENCY_VALUE || (cartProduct.pid == PRODUCT_ID_FOR_SHIPPING && cartProduct.quantity != 1) || (cartProduct.pid == PRODUCT_ID_FOR_DISCOUNT && cartProduct.quantity != 1)) {
                    return false
                }
            }

            val moreThatOneShippingObject =
                event.purchaseLines!!.filter { it?.pid == PRODUCT_ID_FOR_SHIPPING }.size > 1
            val moreThatOneDiscountObject =
                event.purchaseLines!!.filter { it?.pid == PRODUCT_ID_FOR_DISCOUNT }.size > 1

            if (moreThatOneShippingObject || moreThatOneDiscountObject) {
                return false
            }

            return true
        }
    }

    override suspend fun getRecommendationResponse(recommendationRequest: RecommendationRequest?): Resource<RecommendationResponse> {
        val validRequest = verifyValidRequest(recommendationRequest)
        return if (validRequest) {
            Resource.success(
                RecommendationResponse(
                    httpCode = 200, actions = null, monetateId = null, response = null, title = null
                )
            )
        } else {
            Resource.error(R.string.error_unknown, null)
        }
    }
}