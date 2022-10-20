package za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated


import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.RatingAndReviewData
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.ColourSizeVariants
import za.co.woolworths.financial.services.android.util.AppConstant

class ProductDetailsPresenterImpl(var mainView: ProductDetailsContract.ProductDetailsView?, var getInteractor: ProductDetailsContract.ProductDetailsInteractor) : ProductDetailsContract.ProductDetailsPresenter, ProductDetailsContract.ProductDetailsInteractor.OnFinishListener {

    var isDefaultRequest: Boolean = true

    override fun loadCartSummary() {
        mainView?.showProgressBar()
        getInteractor.getCartSummary(this)
    }

    override fun onDestroy() {
        mainView = null
    }

    override fun loadStockAvailability(
        storeID: String,
        multiSKU: String,
        isDefaultRequest: Boolean,
        isUserBrowsing: Boolean
    ) {
        this.isDefaultRequest = isDefaultRequest;
        mainView?.apply {
            showProgressBar()
            getInteractor.getStockAvailability(storeID, multiSKU, this@ProductDetailsPresenterImpl, isUserBrowsing)
        }
    }

    override fun loadProductDetails(productRequest: ProductRequest) {
        mainView?.showProductDetailsLoading()
        getInteractor.getProductDetails(productRequest, this)
    }

    override fun loadRatingNReview(productID: String, limit: Int, offset: Int) {
        getInteractor.getRaringNReview(productID, limit, offset, this)
    }

    override fun postAddItemToCart(addItemToCart: List<AddItemToCart>) {
        mainView?.showProgressBar()
        getInteractor.postAddItemToCart(addItemToCart, this)
    }

    override fun onSuccess(response: Any?) {
        response?.apply {
            when (this) {
                is ProductDetailResponse -> {
                    (this).apply {
                        when (this.httpCode) {
                            200 -> mainView?.onProductDetailsSuccess(this.product)
                            else -> this.response?.let {
                                mainView?.apply {
                                    onProductDetailedFailed(it, httpCode)
                                    hideProgressBar()
                                }
                            }
                        }
                    }
                }
                is SkusInventoryForStoreResponse -> {
                    (this).apply {
                        when (this.httpCode) {
                            200 -> mainView?.onStockAvailabilitySuccess(this, isDefaultRequest)
                            else -> this.response?.let {
                                mainView?.apply {
                                    onProductDetailedFailed(it, httpCode)
                                    hideProgressBar()
                                }
                            }
                        }
                    }
                }
                is CartSummaryResponse -> {
                    this.apply {
                        when (this.httpCode) {
                            200 -> {
                            mainView?.let {
                                it.onCartSummarySuccess(this)
                                it.updateStockAvailabilityLocation()
                            }
                        }
                            440 -> {
                                if (this.response != null)
                                    mainView?.onSessionTokenExpired()
                            }
                            else -> mainView?.responseFailureHandler(this.response)
                        }
                        mainView?.hideProgressBar()
                    }
                }
                is AddItemToCartResponse -> {
                    this.apply {
                        mainView?.hideProgressBar()
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

                            AppConstant.HTTP_EXPECTATION_FAILED_502 ->{
                                if (response != null) {
                                    mainView?.onAddToCartError(this)
                                }
                            }

                            else -> mainView?.responseFailureHandler(this.response)
                        }

                    }

                }
                is LocationResponse -> {
                    mainView?.hideProgressBar()
                    when (this.httpCode) {
                        200 -> {
                            val location = this.Locations
                            if (location != null && location.size > 0) {
                                mainView?.onFindStoresSuccess(location)
                            } else {
                                mainView?.showOutOfStockInStores()
                            }

                        }
                        else -> mainView?.responseFailureHandler(this.response)
                    }
                }
                is RatingAndReviewData -> {
                    (this).apply {
                        when (this.httpCode) {
                            200 -> mainView?.onGetRatingNReviewSuccess(this)
                            else -> this.response?.let {
                                mainView?.apply {
                                    onGetRatingNReviewFailed(it, httpCode)
                                    hideProgressBar()
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    override fun onFailure(error: Throwable?) {
    }

    override fun findStoresForSelectedSku(otherSkus: OtherSkus?) {
        getInteractor?.getLocationItems(otherSkus,this)
    }

    override fun isSizeGuideApplicable(colourSizeVariants: String?, sizeGuideId: String?): Boolean {
        val variant = ColourSizeVariants.find(colourSizeVariants ?: "")
        return (!sizeGuideId.isNullOrEmpty() && (variant == ColourSizeVariants.COLOUR_SIZE_VARIANT || variant == ColourSizeVariants.SIZE_VARIANT))
    }
}