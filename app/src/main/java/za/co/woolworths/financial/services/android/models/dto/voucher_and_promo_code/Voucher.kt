package za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code

class Voucher {
    val voucherTypeId: String = ""
    val barcode: String = ""
    var voucherApplied: Boolean = false
    val description: String = ""
    var isSelected = this.voucherApplied
    var errorMessage: String = ""
}