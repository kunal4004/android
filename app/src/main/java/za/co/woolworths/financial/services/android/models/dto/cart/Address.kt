package za.co.woolworths.financial.services.android.models.dto.cart

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Address(val address1: String?, val latitude: String?, val placeId: String?, val id: String?, val longitude: String?, val nickname: String?): Parcelable
