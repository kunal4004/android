package za.co.woolworths.financial.services.android.shoppinglist.service.network

import za.co.woolworths.financial.services.android.models.dto.Response

data class CopyListResponse(
    val httpCode: Int,
    val response: Response
)