package za.co.woolworths.financial.services.android.dynamicyield.data.response.request

import za.co.woolworths.financial.services.android.dynamicyield.data.response.request.Data

data class Page(
    val `data`: List<Data>,
    val location: String,
    val type: String
)