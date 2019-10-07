package za.co.woolworths.financial.services.android.models.dto.temporary_store_card

import za.co.woolworths.financial.services.android.models.dto.Response

class GetStoreCardsResponse {
    var httpCode: Int = 0
    var response: Response? = null
    var oneTimePinRequired: OneTimePinRequired? = null
    var storeCardsData: StoreCardsData? = null
}