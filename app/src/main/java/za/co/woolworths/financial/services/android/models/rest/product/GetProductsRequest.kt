package za.co.woolworths.financial.services.android.models.rest.product

import android.content.Context
import android.text.TextUtils
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.ProductView
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams
import za.co.woolworths.financial.services.android.util.HttpAsyncTask
import za.co.woolworths.financial.services.android.util.OnEventListener

class GetProductsRequest(val context: Context, var requestParams: ProductsRequestParams, var callback: OnEventListener<ProductView>) : HttpAsyncTask<String, String, ProductView>() {

    var mException: String = ""
    var mWoolworthsApp: WoolworthsApplication = context.applicationContext as WoolworthsApplication
    override fun httpDoInBackground(vararg params: String?): ProductView {
        return mWoolworthsApp.api.getProducts(requestParams)
    }

    override fun httpError(errorMessage: String?, httpErrorCode: HttpErrorCode?): ProductView {
        this.mException = errorMessage!!
        callback.onFailure(this.mException)
        return ProductView()
    }

    override fun httpDoInBackgroundReturnType(): Class<ProductView> {
        return ProductView::class.java
    }

    override fun onPostExecute(result: ProductView?) {
        super.onPostExecute(result)
        if (callback != null) {
            if (TextUtils.isEmpty(mException)) {
                callback.onSuccess(result)
            }
        }
    }


}