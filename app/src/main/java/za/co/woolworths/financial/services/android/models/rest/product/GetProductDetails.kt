package za.co.woolworths.financial.services.android.models.rest.product

import za.co.woolworths.financial.services.android.contracts.AsyncAPIResponse
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.ProductDetailResponse
import za.co.woolworths.financial.services.android.util.HttpAsyncTask

class GetProductDetails(private val productId: String?, private val skuId: String?, private var responseDelegate: AsyncAPIResponse.ResponseDelegate<ProductDetailResponse>?) : HttpAsyncTask<String, String, ProductDetailResponse>() {

    private var mException: String? = null

    override fun httpDoInBackground(vararg params: String?): ProductDetailResponse? {
        return WoolworthsApplication.getInstance().api.productDetail(productId, skuId)
    }

    override fun httpError(errorMessage: String, httpErrorCode: HttpErrorCode?): ProductDetailResponse {
        mException = errorMessage
        responseDelegate?.onFailure(errorMessage)
        return ProductDetailResponse()
    }

    override fun httpDoInBackgroundReturnType(): Class<ProductDetailResponse> = ProductDetailResponse::class.java

    override fun onPostExecute(result: ProductDetailResponse?) {
        super.onPostExecute(result)
        responseDelegate?.apply {
            if (mException == null) {
                result?.let {
                    onSuccess(it)
                }
            }
        }
    }
}