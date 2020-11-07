package za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code

data class VoucherDetails(val activeVouchersCount: Int = 0, val vouchers: ArrayList<Voucher>, val promoCodes: ArrayList<PromoCode>)