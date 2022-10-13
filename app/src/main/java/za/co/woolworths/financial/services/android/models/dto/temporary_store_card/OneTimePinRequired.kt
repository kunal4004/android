package za.co.woolworths.financial.services.android.models.dto.temporary_store_card

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class OneTimePinRequired(val unblockStoreCard: Boolean, val linkVirtualStoreCard: Boolean) : Parcelable