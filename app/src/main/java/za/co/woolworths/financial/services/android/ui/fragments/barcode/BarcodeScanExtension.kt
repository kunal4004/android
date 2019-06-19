package za.co.woolworths.financial.services.android.ui.fragments.barcode

import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.contracts.AsyncAPIResponse
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.ProductDetailResponse
import za.co.woolworths.financial.services.android.models.dto.ProductView
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams
import za.co.woolworths.financial.services.android.models.rest.product.GetProductDetails
import za.co.woolworths.financial.services.android.models.rest.product.ProductRequest
import za.co.woolworths.financial.services.android.models.rest.product.RetrieveProductIdAndSkuId
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.util.ConnectionBroadcastReceiver
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.Utils

abstract class BarcodeScanExtension : Fragment() {

    private var productsRequestParams: ProductsRequestParams? = null
    private var mProductDetailRequest: AsyncTask<String, String, ProductDetailResponse>? = null
    var mRetrieveProductDetail: AsyncTask<String, String, ProductView>? = null
    var networkNotAvailable: Boolean = true
    var getProductDetailAsyncTaskIsRunning = false

    internal open fun onSuccess() {}
    internal abstract fun networkConnectionState(isConnected: Boolean)
    abstract fun progressBarVisibility(progressBarIsVisible: Boolean)

    fun retrieveProductDetail(): AsyncTask<String, String, ProductView>? {
        progressBarVisibility(true)
        networkConnectivityStatus()
        asyncTaskIsRunning(true)
        return RetrieveProductIdAndSkuId(getProductRequestBody(), object : AsyncAPIResponse.ResponseDelegate<ProductView> {
            override fun onSuccess(response: ProductView) {
                if (isAdded && WoolworthsApplication.isApplicationInForeground()) {
                    when (response.httpCode) {
                        200 -> {
                            response.products?.apply {
                                when (size) {
                                    0 -> {
                                        activity?.let { Utils.displayValidationMessage(it, CustomPopUpWindow.MODAL_LAYOUT.BARCODE_ERROR, "") }
                                        progressBarVisibility(false)
                                        asyncTaskIsRunning(false)
                                    }
                                    else -> {
                                        val productRequest: ProductRequest? = response.products?.get(0)?.let { ProductRequest(it.productId, it.sku) }
                                        mProductDetailRequest = productRequest?.let { retrieveProductDetail(it) }
                                        asyncTaskIsRunning(true)
                                    }
                                }
                            }
                        }
                        else -> {
                            asyncTaskIsRunning(false)
                            progressBarVisibility(false)
                            response.response?.desc?.let { activity?.apply { Utils.displayValidationMessage(this, CustomPopUpWindow.MODAL_LAYOUT.ERROR, it) } }
                        }
                    }
                }
            }

            override fun onFailure(errorMessage: String) {
                apiFailure(errorMessage)
            }
        }).execute()
    }

    private fun asyncTaskIsRunning(status: Boolean) {
        getProductDetailAsyncTaskIsRunning = status
    }

    private fun retrieveProductDetail(productRequest: ProductRequest): AsyncTask<String, String, ProductDetailResponse>? {
        return GetProductDetails(productRequest.productId, productRequest.skuId, object : AsyncAPIResponse.ResponseDelegate<ProductDetailResponse> {
            override fun onSuccess(response: ProductDetailResponse) {
                if (isAdded && WoolworthsApplication.isApplicationInForeground()) {
                    when (response.httpCode) {
                        200 -> {
                            response.product?.apply {
                                productId?.let {
                                    val bundle = Bundle()
                                    bundle.apply {
                                        putString("strProductList", Gson().toJson(response.product))
                                        putString("strProductCategory", response.product?.productName)
                                        putString("productResponse", Gson().toJson(response))
                                        putBoolean("fetchFromJson", true)
                                    }
                                    activity?.let { ScreenManager.presentProductDetails(it, bundle) }
                                }
                            }
                        }
                        else -> response.response?.desc?.let { activity?.apply { Utils.displayValidationMessage(this, CustomPopUpWindow.MODAL_LAYOUT.ERROR, it) } }
                    }
                }
                progressBarVisibility(false)
                onSuccess()
                asyncTaskIsRunning(false)
            }

            override fun onFailure(errorMessage: String) {
                apiFailure(errorMessage)
            }
        }).execute()
    }

    fun setProductRequestBody(searchType: ProductsRequestParams.SearchType, searchTerm: String) {
        this.productsRequestParams = ProductsRequestParams(searchTerm, searchType, ProductsRequestParams.ResponseType.DETAIL, 0)
    }

    private fun getProductRequestBody(): ProductsRequestParams? = productsRequestParams

    private fun apiFailure(errorMessage: String) = activity?.runOnUiThread {
        progressBarVisibility(false)
        asyncTaskIsRunning(false)
        if (isAdded && WoolworthsApplication.isApplicationInForeground()) {
            if (errorMessage.contains("SocketTimeoutException") || errorMessage.contains("ConnectionException")) {
                networkNotAvailable = false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mRetrieveProductDetail?.apply {
            if (!isCancelled) {
                cancel(true)
            }
        }
        mProductDetailRequest?.apply {
            if (!isCancelled) {
                cancel(true)
            }
        }
    }

    private fun networkConnectivityStatus() {
        activity?.let {
            ConnectionBroadcastReceiver.registerToFragmentAndAutoUnregister(it, this, object : ConnectionBroadcastReceiver() {
                override fun onConnectionChanged(hasConnection: Boolean) {
                    networkConnectionState(hasConnection)
                }
            })
        }
    }
}