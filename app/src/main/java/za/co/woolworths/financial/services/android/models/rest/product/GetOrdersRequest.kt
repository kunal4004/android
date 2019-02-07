package za.co.woolworths.financial.services.android.models.rest.product

import android.content.Context
import android.text.TextUtils
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.OrdersResponse
import za.co.woolworths.financial.services.android.util.HttpAsyncTask
import za.co.woolworths.financial.services.android.util.OnEventListener
import java.lang.ref.WeakReference

class GetOrdersRequest(val context: Context, var callback: OnEventListener<OrdersResponse>) : HttpAsyncTask<String, String, OrdersResponse>() {

    var mException: String = ""
    var mWoolworthsApp: WoolworthsApplication = context.applicationContext as WoolworthsApplication
    override fun httpDoInBackground(vararg params: String?): OrdersResponse {
        return mWoolworthsApp.api.orders
    }

    override fun httpError(errorMessage: String?, httpErrorCode: HttpErrorCode?): OrdersResponse {
        this.mException = errorMessage!!
        callback.onFailure(this.mException)
        return OrdersResponse()
    }

    override fun httpDoInBackgroundReturnType(): Class<OrdersResponse> {
        return OrdersResponse::class.java
    }

    override fun onPostExecute(result: OrdersResponse?) {
        super.onPostExecute(result)
        if (TextUtils.isEmpty(mException)) {
            callback.onSuccess(result)
        }

    }


}