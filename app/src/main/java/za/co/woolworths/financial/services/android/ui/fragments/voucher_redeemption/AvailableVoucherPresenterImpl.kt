package za.co.woolworths.financial.services.android.ui.fragments.voucher_redeemption

import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.contracts.IGenericAPILoaderView
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.ShoppingCartResponse
import za.co.woolworths.financial.services.android.models.dto.voucher_redemption.SelectedVoucher
import za.co.woolworths.financial.services.android.models.dto.voucher_redemption.Voucher

class AvailableVoucherPresenterImpl(var mainView: AvailableVoucherContract.AvailableVoucherView?, var getInteractor: AvailableVoucherContract.AvailableVoucherInteractor?) : AvailableVoucherContract.AvailableVoucherPresenter, IGenericAPILoaderView<Any> {

    private var vouchers:ArrayList<Voucher>? = null

    override fun onDestroy() {
        mainView = null
    }

    override fun initRedeemVouchers(vouchers: List<SelectedVoucher>) {
        getInteractor?.executeRedeemVouchers(vouchers, this)
    }

    override fun onSuccess(response: Any?) {
        with(response) {
            when (this) {
                is ShoppingCartResponse -> {
                    when (httpCode) {
                        200 -> {
                            if (data[0]?.messages.isNullOrEmpty()) {
                                mainView?.onVoucherRedeemSuccess(this)
                            } else {
                                mainView?.onVoucherRedeemFailure(data[0].messages[0])
                            }
                        }
                        else -> mainView?.onVoucherRedeemFailure(WoolworthsApplication.getAppContext().getString(R.string.redeem_voucher_generic_error_message))
                    }
                }
                else -> throw RuntimeException("onSuccess:: unknown response $response")
            }
        }
    }

    override fun onFailure(error: Throwable?) {
        super.onFailure(error)
        mainView?.onVoucherRedeemFailure(WoolworthsApplication.getAppContext().getString(R.string.redeem_voucher_generic_error_message))
    }

    override fun getSelectedVouchersToApply(): List<SelectedVoucher> {
        val selectedVouchers: ArrayList<SelectedVoucher> = arrayListOf()
        getVouchers()?.forEach {
            if (it.isSelected) {
                selectedVouchers.add(SelectedVoucher(it.barcode, it.voucherTypeId))
            }
        }

        return selectedVouchers
    }

    override fun isVouchersSelectedToRedeem(): Boolean {
        return getVouchers()?.any { it.isSelected != it.voucherApplied } ?: true
    }

    override fun setVouchers(vouchers: ArrayList<Voucher>) {
        vouchers.forEach { it.isSelected = it.voucherApplied }
        this.vouchers = vouchers
    }

    override fun getVouchers(): ArrayList<Voucher>? {
        return vouchers
    }


}