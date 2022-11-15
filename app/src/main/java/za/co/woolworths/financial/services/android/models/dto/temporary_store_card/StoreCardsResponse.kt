package za.co.woolworths.financial.services.android.models.dto.temporary_store_card

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import za.co.woolworths.financial.services.android.models.dto.account.ServerErrorResponse
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.model.Response

@Parcelize
data class StoreCardsResponse(
    var httpCode: Int = 0,
    var response: ServerErrorResponse? = null,
    var oneTimePinRequired: OneTimePinRequired? = null,
    var storeCardsData: StoreCardsData? = null
) : Parcelable