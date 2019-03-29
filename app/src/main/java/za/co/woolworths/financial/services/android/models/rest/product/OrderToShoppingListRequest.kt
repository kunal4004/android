package za.co.woolworths.financial.services.android.models.rest.product

import android.content.Context
import android.text.TextUtils
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.OrderToListReponse
import za.co.woolworths.financial.services.android.models.dto.OrderToShoppingListRequestBody
import za.co.woolworths.financial.services.android.util.HttpAsyncTask
import za.co.woolworths.financial.services.android.util.OnEventListener

class OrderToShoppingListRequest(val context: Context, val orderID: String, val requestObject: OrderToShoppingListRequestBody, var callback: OnEventListener<OrderToListReponse>) : HttpAsyncTask<String, String, OrderToListReponse>() {

    var mException: String = ""
    var mWoolworthsApp: WoolworthsApplication = context.applicationContext as WoolworthsApplication
    override fun httpDoInBackground(vararg params: String?): OrderToListReponse {
        return mWoolworthsApp.api.addOrderToList(orderID, requestObject)
    }

    override fun httpError(errorMessage: String?, httpErrorCode: HttpErrorCode?): OrderToListReponse {
        this.mException = errorMessage!!
        callback.onFailure(this.mException)
        return OrderToListReponse()
    }

    override fun httpDoInBackgroundReturnType(): Class<OrderToListReponse> {
        return OrderToListReponse::class.java
    }

    override fun onPostExecute(result: OrderToListReponse?) {
        super.onPostExecute(result)
        if (TextUtils.isEmpty(mException)) {
            callback.onSuccess(result)
        }

    }


}