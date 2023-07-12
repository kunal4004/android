package za.co.woolworths.financial.services.android.models.dto.cart

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class PriceInfo(
    var discounted: Boolean? = null,
    var amount: Double = 0.00,
    var rawTotalPrice: @RawValue Any = 0.00,
    var simplePromo: @RawValue Any = 0,
    var salePrice: @RawValue Any = 0,
    var itemSavings: @RawValue Any = 0.00,
    var wrewardsDiscount: @RawValue Any = 0,
    var staffDiscount: @RawValue Any = 0,
    var voucherDiscount: @RawValue Any = 0,
    var promoPrice: @RawValue Any = 0.00,
    var onSale: Boolean? = null,
    var totalDiscount: @RawValue Any = 0,
    var otherDiscount: @RawValue Any = 0,
    var listPrice: @RawValue Any = 0.00,
    var simplePromotion: Boolean? = null,
    var promoCodeDiscount: @RawValue Any = 0
) : Parcelable