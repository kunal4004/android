package za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.size_guide

import za.co.woolworths.financial.services.android.contracts.IGenericAPILoaderView
import za.co.woolworths.financial.services.android.models.dto.size_guide.SizeGuideResponse

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
                        200 -> {
                            if (!this.content?.sizeGuideHtml.isNullOrEmpty())
                                mainView?.onSizeGuideContentSuccess(this.content?.sizeGuideHtml)
                            else
                                mainView?.onSizeGuideContentFailed()
                        }
                        else -> mainView?.onSizeGuideContentFailed()
                    }
                }
                else -> mainView?.onSizeGuideContentFailed()
            }
        }
    }

}