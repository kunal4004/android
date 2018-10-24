package za.co.woolworths.financial.services.android.ui.extensions

import android.content.Context
import za.co.woolworths.financial.services.android.models.dto.ProductView
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams
import za.co.woolworths.financial.services.android.models.rest.product.GetProductsRequest
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.OnRefineProductsResult
import za.co.woolworths.financial.services.android.util.OnEventListener

fun refineProducts(context: Context, productsRequestParams: ProductsRequestParams): GetProductsRequest {
    var resultListner: OnRefineProductsResult = context as OnRefineProductsResult
    productsRequestParams.responseType = ProductsRequestParams.ResponseType.SUMMARY
    return GetProductsRequest(context, productsRequestParams, object : OnEventListener<ProductView> {
        override fun onSuccess(productView: ProductView?) {
            if (productView?.httpCode == 200) {
                resultListner.onProductRefineSuccess(productView!!, productsRequestParams.refinement)
            } else {
                resultListner.onProductRefineFailure(productView?.response?.desc!!)
            }
        }

        override fun onFailure(e: String?) {
            resultListner.onProductRefineFailure(e!!)
        }

    })

}