package za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated

import za.co.woolworths.financial.services.android.contracts.RequestListener
import za.co.woolworths.financial.services.android.models.dto.*

class ProductDetailsPresenterImpl(var mainView: ProductDetailsContract.ProductDetailsView?, var getInteractor: ProductDetailsContract.ProductDetailsInteractor) : ProductDetailsContract.ProductDetailsPresenter, ProductDetailsContract.ProductDetailsInteractor.OnFinishListener {

    var isDefaultRequest: Boolean = true

    override fun loadCartSummary() {

    }

    override fun onDestroy() {
        mainView = null
    }

    override fun loadStockAvailability(storeID: String, multiSKU: String, isDefaultRequest: Boolean) {
        this.isDefaultRequest = isDefaultRequest;
        mainView?.apply {
            showProgressBar()
            getInteractor.getStockAvailability(storeID, multiSKU, this@ProductDetailsPresenterImpl)
        }
    }

    override fun loadProductDetails(productRequest: ProductRequest) {
        getInteractor.getProductDetails(productRequest, this)
    }

    override fun postAddItemToCart(addItemToCart: List<AddItemToCart>) {
        getInteractor.postAddItemToCart(addItemToCart, this)
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
                            200 -> mainView?.onStockAvailabilitySuccess(this, isDefaultRequest)
                            else -> this.response?.let { mainView?.onProductDetailedFailed(it) }
                        }
                    }
                }
                is CartSummaryResponse -> {
                    this.apply {
                        when (this.httpCode) {
                            200 -> mainView?.onCartSummarySuccess(this)
                            440 -> {
                                if (this.response != null)
                                    mainView?.onSessionTokenExpired()
                            }
                            else -> mainView?.responseFailureHandler(this.response)
                        }

                    }
                }
                is AddItemToCartResponse -> {
                    this.apply {
                        when (this.httpCode) {
                            200 -> {
                                this.data?.let { data ->
                                    data[0]?.formexceptions?.get(0)?.let {
                                        if (it.message.toLowerCase().contains("some of the products chosen are out of stock"))
                                            this.response.desc = "Unfortunately this item is currently out of stock."
                                        else
                                            this.response.desc = it.message
                                        mainView?.responseFailureHandler(this.response)
                                        return
                                    }
                                    mainView?.onAddToCartSuccess(this)
                                }
                            }
                            440 -> {
                                if (this.response != null)
                                    mainView?.onSessionTokenExpired()
                            }
                            else -> mainView?.responseFailureHandler(this.response)
                        }

                    }

                }
            }
        }

    }

    override fun onFailure(error: Throwable?) {
    }

    override fun findStoresForSelectedSku(otherSkus: OtherSkus?) {

    }


}