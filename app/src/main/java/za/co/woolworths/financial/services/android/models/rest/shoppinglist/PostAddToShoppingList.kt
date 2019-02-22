package za.co.woolworths.financial.services.android.models.rest.shoppinglist

import za.co.woolworths.financial.services.android.contracts.AsyncAPIResponse
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.AddToListRequest
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItemsResponse
import za.co.woolworths.financial.services.android.util.HttpAsyncTask


class PostAddToShoppingList(private val listId: String?, private val addToList: List<AddToListRequest>?, private var responseDelegate: AsyncAPIResponse.ResponseDelegate<ShoppingListItemsResponse>?) : HttpAsyncTask<String, String, ShoppingListItemsResponse>() {
    private var mException: String? = null

    override fun httpDoInBackground(vararg strings: String): ShoppingListItemsResponse {
        return WoolworthsApplication.getInstance().api.addToList(addToList, listId)
    }

    override fun httpError(errorMessage: String, httpErrorCode: HttpAsyncTask.HttpErrorCode): ShoppingListItemsResponse {
        mException = errorMessage
        responseDelegate!!.onFailure(errorMessage)
        return ShoppingListItemsResponse()
    }

    override fun httpDoInBackgroundReturnType(): Class<ShoppingListItemsResponse> = ShoppingListItemsResponse::class.java

    override fun onPostExecute(result: ShoppingListItemsResponse?) {
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
