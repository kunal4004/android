package za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code

data class CashBackVouchers(
    val barcode: String,
    val description: String,
    val details: String,
    val expiryDate: String,
    val statusID: Int,
    val termAndCondition: List<Any>,
    val threshold: Int,
    val validFrom: String,
    val voucherApplied: Boolean,
    val voucherTypeId: Int,
)