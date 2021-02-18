package za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.size_guide

import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.contracts.IGenericAPILoaderView
import za.co.woolworths.financial.services.android.models.dto.size_guide.SizeGuideResponse
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.util.AppConstant

class ProductSizeGuidePresenterImpl(var mainView: ProductSizeGuideContract.ProductSizeGuideView?, var getInteractor: ProductSizeGuideContract.ProductSizeGuideInteractor?) : ProductSizeGuideContract.ProductSizeGuidePresenter, IGenericAPILoaderView<Any> {
    override fun onDestroy() {
        mainView = null
    }

    override fun loadSizeGuideContent(sizeGuideId: String) {
        getInteractor?.querySizeGuideContent(sizeGuideId, this)
    }

    override fun onSuccess(response: Any?) {
        with(response) {
            when (this) {
                is SizeGuideResponse -> {
                    when (httpCode) {
                        AppConstant.HTTP_OK -> {
                            if (!this.content?.sizeGuideHtml.isNullOrEmpty())
                                mainView?.onSizeGuideContentSuccess(this.content?.sizeGuideHtml)
                            else
                                onError(this.response?.desc)
                        }
                        else -> onError()
                    }
                }
                else -> onError()
            }
        }
    }

    private fun onError(errorMessage: String? = null) {
        mainView?.onSizeGuideContentFailed(if (errorMessage.isNullOrEmpty()) bindString(R.string.general_error_desc) else errorMessage)
    }
}