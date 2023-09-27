package za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Repository

import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Request.PrepareChangeAttributeRequestEvent
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Response.DyChangeAttributeResponse

interface DyReportEventRepository {
    suspend fun getDyReportEventResponse(reportEventRequest: PrepareChangeAttributeRequestEvent?): Resource<DyChangeAttributeResponse>
}