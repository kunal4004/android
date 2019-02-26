package za.co.woolworths.financial.services.android.models.rest.shoppinglist

import za.co.woolworths.financial.services.android.contracts.AsyncAPIResponse
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.OrderToListReponse
import za.co.woolworths.financial.services.android.models.dto.OrderToShoppingListRequestBody
import za.co.woolworths.financial.services.android.util.HttpAsyncTask

class PostOrderToShoppingList(private val orderID: String?, private val requestObject: OrderToShoppingListRequestBody,
                              private var responseDelegate: AsyncAPIResponse.ResponseDelegate<OrderToListReponse>?) : HttpAsyncTask<String,
        String, OrderToListReponse>() {
    private var mException: String? = null

    override fun httpDoInBackground(vararg strings: String): OrderToListReponse {
        return WoolworthsApplication.getInstance().api.addOrderToList(orderID, requestObject)
    }

    override fun httpError(errorMessage: String, httpErrorCode: HttpAsyncTask.HttpErrorCode): OrderToListReponse {
        mException = errorMessage
        responseDelegate!!.onFailure(errorMessage)
        return OrderToListReponse()
    }

    override fun httpDoInBackgroundReturnType(): Class<OrderToListReponse> = OrderToListReponse::class.java

    override fun onPostExecute(result: OrderToListReponse?) {
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