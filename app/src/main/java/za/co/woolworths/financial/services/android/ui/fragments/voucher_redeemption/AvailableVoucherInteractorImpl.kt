package za.co.woolworths.financial.services.android.ui.fragments.voucher_redeemption

import za.co.woolworths.financial.services.android.contracts.IGenericAPILoaderView
import za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code.SelectedVoucher
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.extension.request

class AvailableVoucherInteractorImpl : VoucherAndPromoCodeContract.AvailableVoucherInteractor {
    override fun executeRedeemVouchers(vouchers: List<SelectedVoucher>, requestListener: IGenericAPILoaderView<Any>) {
        request(OneAppService.applyVouchers(vouchers), requestListener)
    }
}