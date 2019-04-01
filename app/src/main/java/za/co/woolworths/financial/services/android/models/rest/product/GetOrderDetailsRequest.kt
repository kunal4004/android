package za.co.woolworths.financial.services.android.models.rest.product

import android.content.Context
import android.text.TextUtils
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.OrderDetailsResponse
import za.co.woolworths.financial.services.android.util.HttpAsyncTask
import za.co.woolworths.financial.services.android.util.OnEventListener

class GetOrderDetailsRequest(val context: Context, val orderID: String, var callback: OnEventListener<OrderDetailsResponse>) : HttpAsyncTask<String, String, OrderDetailsResponse>() {

    var mException: String = ""
    var mWoolworthsApp: WoolworthsApplication = context.applicationContext as WoolworthsApplication
    override fun httpDoInBackground(vararg params: String?): OrderDetailsResponse {
        return mWoolworthsApp.api.getOrderDetails(orderID)
    }

    override fun httpError(errorMessage: String?, httpErrorCode: HttpErrorCode?): OrderDetailsResponse {
        this.mException = errorMessage!!
        callback.onFailure(this.mException)
        return OrderDetailsResponse()
    }

    override fun httpDoInBackgroundReturnType(): Class<OrderDetailsResponse> {
        return OrderDetailsResponse::class.java
    }

    override fun onPostExecute(result: OrderDetailsResponse?) {
        super.onPostExecute(result)
        if (TextUtils.isEmpty(mException)) {
            callback.onSuccess(result)
        }

    }


}