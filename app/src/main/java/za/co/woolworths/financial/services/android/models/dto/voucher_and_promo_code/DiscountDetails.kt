package za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DiscountDetails(val totalDiscount: Double, val otherDiscount: Double, val voucherDiscount: Double, val promoCodeDiscount: Double, val totalOrderDiscount: Double, val wrewardsDiscount: Double, val companyDiscount: Double): Parcelable