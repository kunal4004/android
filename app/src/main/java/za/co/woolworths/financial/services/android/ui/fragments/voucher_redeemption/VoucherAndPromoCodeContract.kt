package za.co.woolworths.financial.services.android.ui.fragments.voucher_redeemption

import za.co.woolworths.financial.services.android.contracts.IGenericAPILoaderView
import za.co.woolworths.financial.services.android.models.dto.ShoppingCartResponse
import za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code.SelectedVoucher
import za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code.Voucher
import za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code.VoucherErrorMessage

interface VoucherAndPromoCodeContract {

    interface AvailableVoucherView {
        fun onVoucherRedeemSuccess(shoppingCartResponse: ShoppingCartResponse, isPartialSuccess: Boolean)
        fun onVoucherRedeemFailure()
        fun redeemVouchers()
        fun showRedeemVoucherProgress()
        fun showAvailableVouchers()
        fun hideRedeemVoucherProgress()
        fun enableRedeemButton()
        fun onVoucherRedeemGeneralFailure(message: String)
        fun updateVouchersList()
    }

    interface AvailableVoucherPresenter {
        fun onDestroy()
        fun initRedeemVouchers(vouchers: List<SelectedVoucher>)
        fun getSelectedVouchersToApply(): List<SelectedVoucher>
        fun isVouchersSelectedToRedeem(): Boolean
        fun setVouchers(vouchers: ArrayList<Voucher>)
        fun getVouchers(): ArrayList<Voucher>?
        fun updateVouchersWithErrorMessages(message: ArrayList<VoucherErrorMessage>)
    }

    interface AvailableVoucherInteractor {
        fun executeRedeemVouchers(vouchers: List<SelectedVoucher>, requestListener: IGenericAPILoaderView<Any>)
    }

    interface ApplyPromoCodeView {
        fun onApplyPromoCodeSuccess(shoppingCartResponse: ShoppingCartResponse)
        fun applyPromoCode()
        fun showApplyPromoCodeProgress()
        fun hideApplyPromoCodeProgress()
        fun onApplyPromoCodeFailure(message: String)
        fun onPromoCodeTextChanged(promoCode: String)
    }

    interface ApplyPromoCoderPresenter {
        fun onDestroy()
        fun initApplyPromoCode(promoCode: String)
    }

    interface ApplyPromoCodeInteractor {
        fun executeApplyPromoCode(promoCode: String, requestListener: IGenericAPILoaderView<Any>)
    }

}