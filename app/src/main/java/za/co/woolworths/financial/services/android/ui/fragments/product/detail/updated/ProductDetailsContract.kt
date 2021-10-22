package za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated

import RatingReviewResopnse
import android.content.Context
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.ui.activities.product.ProductInformationActivity
import za.co.woolworths.financial.services.android.ui.views.actionsheet.QuantitySelectorFragment

interface ProductDetailsContract {

    interface ProductDetailsView : QuantitySelectorFragment.IQuantitySelector {
        fun showProgressBar()
        fun hideProgressBar()
        fun onSessionTokenExpired()
        fun onProductDetailsSuccess(productDetails: ProductDetails)
        fun onProductDetailedFailed(response: Response, httpCode: Int)
        fun onFailureResponse(error: String)
        fun onStockAvailabilitySuccess(skusInventoryForStoreResponse: SkusInventoryForStoreResponse, isDefaultRequest: Boolean)
        fun getImageByWidth(imageUrl: String?, context: Context): String
        fun updateDefaultUI(isInventoryCalled: Boolean)
        fun updateAuxiliaryImages(imagesList: List<String>)
        fun onSizeSelection(selectedSku: OtherSkus)
        fun onColorSelection(selectedColor: String?)
        fun setSelectedSku(selectedSku: OtherSkus?)
        fun getSelectedSku(): OtherSkus?
        fun setSelectedQuantity(selectedQuantity: Int?)
        fun getSelectedQuantity(): Int?
        fun onCartSummarySuccess(cartSummaryResponse: CartSummaryResponse)
        fun responseFailureHandler(response: Response)
        fun onAddToCartSuccess(addItemToCartResponse: AddItemToCartResponse)
        fun showOutOfStockInStores()
        fun onFindStoresSuccess(location: List<StoreDetails>)
        fun showProductDetailsLoading()
        fun hideProductDetailsLoading()
        fun updateStockAvailabilityLocation()
        fun updateDeliveryLocation()
        fun showDetailsInformation(productInformationType: ProductInformationActivity.ProductInformationType)
        fun loadPromotionalImages()
        fun setUniqueIds()
        fun clearSelectedOnLocationChange()
        fun showProductNotAvailableForCollection()
        fun clearStockAvailability()
        fun shareProduct()
        fun onGetRatingNReviewSuccess(ratingNReview: RatingReviewResopnse)
        fun onGetRatingNReviewFailed(response: za.co.woolworths.financial.services.android.models.dto.rating_n_reviews.Response, httpCode: Int)
    }

    interface ProductDetailsPresenter {

        fun onDestroy()
        fun loadStockAvailability(storeID: String, multiSKU: String, isDefaultRequest: Boolean)
        fun loadProductDetails(productRequest: ProductRequest)
        fun loadCartSummary()
        fun postAddItemToCart(addItemToCart: List<AddItemToCart>)
        fun findStoresForSelectedSku(otherSkus: OtherSkus?)
        fun isSizeGuideApplicable(colourSizeVariants: String?, sizeGuideId: String?): Boolean
        fun loadRatingNReview(productID: String, limit: Int, offset: Int)
    }

    interface ProductDetailsInteractor {

        interface OnFinishListener : IResponseListener<Any>

        fun getProductDetails(productRequest: ProductRequest, onFinishListener: OnFinishListener)
        fun getCartSummary(onFinishListener: OnFinishListener)
        fun getStockAvailability(storeID: String, multiSKU: String, onFinishListener: OnFinishListener)
        fun postAddItemToCart(addItemToCart: List<AddItemToCart>, onFinishListener: OnFinishListener)
        fun getLocationItems(otherSkus: OtherSkus?, onFinishListener: OnFinishListener)
        fun getRaringNReview(productID: String, limit: Int, offset: Int, onFinishListener: OnFinishListener)
    }

}