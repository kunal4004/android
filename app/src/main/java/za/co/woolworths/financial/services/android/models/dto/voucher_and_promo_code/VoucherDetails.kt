package za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code

data class VoucherDetails(
    val activeCashVouchersCount: Int = 0,
    val activeVouchersCount: Int = 0,
    val vouchers: ArrayList<Voucher>,
    val promoCodes: ArrayList<PromoCode>,
    val activeTotalVouchersCount: Int = 0,
    val cashBack : ArrayList<CashBackVouchers>
)