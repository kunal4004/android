package za.co.woolworths.financial.services.android.dynamicyield.data.repository

import za.co.woolworths.financial.services.android.dynamicyield.data.response.getResponse.DynamicYieldChooseVariationResponse
import za.co.woolworths.financial.services.android.dynamicyield.data.response.request.DynamicVariantRequestEvent
import za.co.woolworths.financial.services.android.models.network.Resource

interface DynamicYieldRepository {
    suspend fun getDynamicYieldResponse(dynamicYieldRequest: DynamicVariantRequestEvent?): Resource<DynamicYieldChooseVariationResponse>
}