package za.co.woolworths.financial.services.android.models.dto.item_limits

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ProductCountMap(val totalProductCount: Int?, val quantityLimit: QuantityLimit?): Parcelable