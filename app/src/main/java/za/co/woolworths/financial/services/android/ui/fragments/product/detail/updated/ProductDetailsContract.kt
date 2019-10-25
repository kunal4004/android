package za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated

import android.content.Context
import za.co.woolworths.financial.services.android.contracts.RequestListener
import za.co.woolworths.financial.services.android.models.dto.ProductDetails
import za.co.woolworths.financial.services.android.models.dto.ProductRequest
import za.co.woolworths.financial.services.android.models.dto.Response
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse

interface ProductDetailsContract {

    interface ProductDetailsView {
        fun showProgressBar()
        fun hideProgressBar()
        fun onSessionTokenExpired()
        fun onProductDetailsSuccess(productDetails: ProductDetails)
        fun onProductDetailedFailed(response: Response)
        fun onFailureResponse(error: String)
        fun onStockAvailabilitySuccess(skusInventoryForStoreResponse: SkusInventoryForStoreResponse)
        fun getImageByWidth(imageUrl: String, context: Context): String
        fun updateDefaultUI()
        fun updateAuxiliaryImages(imagesList: List<String>)
        fun setPromotionalText(promotionValue: String)
    }

    interface ProductDetailsPresenter {

        fun onDestroy()
        fun loadStockAvailability(storeID: String, multiSKU: String)
        fun loadProductDetails(productRequest: ProductRequest)

    }

    interface ProductDetailsInteractor {

        interface OnFinishListener : RequestListener<Any>

        fun getProductDetails(productRequest: ProductRequest, onFinishListener: OnFinishListener)
        fun getCartSummary()
        fun getStockAvailability(storeID: String, multiSKU: String, onFinishListener: OnFinishListener)
        fun postAddItemToCart()
        fun getLocationItems()
    }

}