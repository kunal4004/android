package za.co.woolworths.financial.services.android.models.dto.voucher_redemption

data class VoucherDetails(val activeVouchersCount: Int = 0, val vouchers: ArrayList<Voucher>, val promoCodes: ArrayList<PromoCode>)