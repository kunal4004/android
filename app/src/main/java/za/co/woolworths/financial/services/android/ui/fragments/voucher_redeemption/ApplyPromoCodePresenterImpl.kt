package za.co.woolworths.financial.services.android.ui.fragments.voucher_redeemption

import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.contracts.IGenericAPILoaderView
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.ShoppingCartResponse
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_EXPECTATION_FAILED_502
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_OK

class ApplyPromoCodePresenterImpl(var mainView: VoucherAndPromoCodeContract.ApplyPromoCodeView?, var getInteractor: VoucherAndPromoCodeContract.ApplyPromoCodeInteractor?) : VoucherAndPromoCodeContract.ApplyPromoCoderPresenter, IGenericAPILoaderView<Any> {

    override fun onDestroy() {
        mainView = null
    }

    override fun initApplyPromoCode(promoCode: String) {
        getInteractor?.executeApplyPromoCode(promoCode, this)
    }

    override fun onSuccess(response: Any?) {
        with(response) {
            when (this) {
                is ShoppingCartResponse -> {
                    when (httpCode) {
                        HTTP_OK -> mainView?.onApplyPromoCodeSuccess(this)
                        HTTP_EXPECTATION_FAILED_502 -> mainView?.onApplyPromoCodeFailure(this.response.desc
                                ?: WoolworthsApplication.getAppContext().getString(R.string.promo_code_generic_error_message))
                        else -> mainView?.onApplyPromoCodeFailure(WoolworthsApplication.getAppContext().getString(R.string.promo_code_generic_error_message))
                    }
                }
                else -> throw RuntimeException("onSuccess:: unknown response $response")
            }
        }
    }

    override fun onFailure(error: Throwable?) {
        super.onFailure(error)
        mainView?.onApplyPromoCodeFailure(WoolworthsApplication.getAppContext().getString(R.string.promo_code_generic_error_message))
    }

}