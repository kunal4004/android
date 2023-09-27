package za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.repository

import za.co.woolworths.financial.services.android.dynamicyield.data.response.getResponse.DynamicYieldChooseVariationResponse
import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.HomePageRequestEvent

interface DyChooseVariationRepository {
    suspend fun getDyChooseVariationResponse(chooseVariationRequestEvent: HomePageRequestEvent?): Resource<DynamicYieldChooseVariationResponse>
}