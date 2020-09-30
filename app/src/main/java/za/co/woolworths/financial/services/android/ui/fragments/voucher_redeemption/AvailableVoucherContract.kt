package za.co.woolworths.financial.services.android.ui.fragments.voucher_redeemption

import za.co.woolworths.financial.services.android.contracts.IGenericAPILoaderView
import za.co.woolworths.financial.services.android.models.dto.ShoppingCartResponse
import za.co.woolworths.financial.services.android.models.dto.voucher_redemption.SelectedVoucher
import za.co.woolworths.financial.services.android.models.dto.voucher_redemption.Voucher

interface AvailableVoucherContract {

    interface AvailableVoucherView {
        fun onVoucherRedeemSuccess(shoppingCartResponse: ShoppingCartResponse)
        fun onVoucherRedeemFailure(message: String)
        fun redeemVouchers()
        fun showRedeemVoucherProgress()
        fun showAvailableVouchers()
        fun hideRedeemVoucherProgress()
        fun onVoucherSelected()
    }

    interface AvailableVoucherPresenter {
        fun onDestroy()
        fun initRedeemVouchers(vouchers: List<SelectedVoucher>)
        fun getSelectedVouchersToApply(): List<SelectedVoucher>
        fun isVouchersSelectedToRedeem(): Boolean
        fun setVouchers(vouchers: ArrayList<Voucher>)
        fun getVouchers(): ArrayList<Voucher>?
    }

    interface AvailableVoucherInteractor {
        fun executeRedeemVouchers(vouchers: List<SelectedVoucher>, requestListener: IGenericAPILoaderView<Any>)
    }

}