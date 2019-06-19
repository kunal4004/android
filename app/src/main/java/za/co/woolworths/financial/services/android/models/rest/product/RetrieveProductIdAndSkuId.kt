package za.co.woolworths.financial.services.android.models.rest.product

import za.co.woolworths.financial.services.android.contracts.AsyncAPIResponse
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.ProductView
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams
import za.co.woolworths.financial.services.android.util.HttpAsyncTask

class RetrieveProductIdAndSkuId(private val requestParams: ProductsRequestParams?, private var responseDelegate: AsyncAPIResponse.ResponseDelegate<ProductView>?) : HttpAsyncTask<String, String, ProductView>() {

    private var mException: String? = null

    override fun httpDoInBackground(vararg params: String?): ProductView? {
        return WoolworthsApplication.getInstance().api.getProducts(requestParams)
    }

    override fun httpError(errorMessage: String, httpErrorCode: HttpErrorCode?): ProductView {
        mException = errorMessage
        responseDelegate?.onFailure(errorMessage)
        return ProductView()
    }

    override fun httpDoInBackgroundReturnType(): Class<ProductView> = ProductView::class.java

    override fun onPostExecute(result: ProductView?) {
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