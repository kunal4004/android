package za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated

import za.co.woolworths.financial.services.android.contracts.RequestListener
import za.co.woolworths.financial.services.android.models.dto.ProductDetailResponse
import za.co.woolworths.financial.services.android.models.dto.ProductRequest
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse

class ProductDetailsPresenterImpl(var mainView: ProductDetailsContract.ProductDetailsView?, var getInteractor: ProductDetailsContract.ProductDetailsInteractor) : ProductDetailsContract.ProductDetailsPresenter, ProductDetailsContract.ProductDetailsInteractor.OnFinishListener {

    override fun onDestroy() {
        mainView = null
    }

    override fun loadStockAvailability(storeID: String, multiSKU: String) {
        mainView?.apply {
            showProgressBar()
            getInteractor.getStockAvailability(storeID, multiSKU, this@ProductDetailsPresenterImpl)
        }
    }

    override fun loadProductDetails(productRequest: ProductRequest) {
        getInteractor.getProductDetails(productRequest, this)
    }

    override fun onSuccess(response: Any?) {
        response?.apply {
            when (this) {
                is ProductDetailResponse -> {
                    (this).apply {
                        when (this.httpCode) {
                            200 -> mainView?.onProductDetailsSuccess(this.product)
                            else -> this.response?.let { mainView?.onProductDetailedFailed(it) }
                        }
                    }
                }
                is SkusInventoryForStoreResponse -> {
                    (this).apply {
                        when (this.httpCode) {
                            200 -> mainView?.onStockAvailabilitySuccess(this)
                            else -> this.response?.let { mainView?.onProductDetailedFailed(it) }
                        }
                    }
                }
            }
        }

    }

    override fun onFailure(error: Throwable?) {
    }


}