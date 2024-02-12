package za.co.woolworths.financial.services.android.models.dto.order_again

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OrderAgainRequestBody(val priceListId: String = "", val monetateId: String) : Parcelable