package za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code

data class DiscountDetails(val totalDiscount: Double, val otherDiscount: Double, val voucherDiscount: Double, val promoCodeDiscount: Double, val totalOrderDiscount: Double, val wrewardsDiscount: Double, val companyDiscount: Double)