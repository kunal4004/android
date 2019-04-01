package za.co.woolworths.financial.services.android.models.rest.shoppinglist

import za.co.woolworths.financial.services.android.contracts.AsyncAPIResponse
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.CreateList
import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse
import za.co.woolworths.financial.services.android.util.HttpAsyncTask

class CreateShoppingList(private val listName: CreateList?, private var responseDelegate: AsyncAPIResponse.ResponseDelegate<ShoppingListsResponse>?) : HttpAsyncTask<String, String, ShoppingListsResponse>() {

    private var mException: String? = null

    override fun httpDoInBackground(vararg params: String?): ShoppingListsResponse {
        return WoolworthsApplication.getInstance().api.createList(listName)
    }

    override fun httpError(errorMessage: String, httpErrorCode: HttpErrorCode?): ShoppingListsResponse {
        mException = errorMessage
        responseDelegate?.onFailure(errorMessage)
        return ShoppingListsResponse()
    }

    override fun httpDoInBackgroundReturnType(): Class<ShoppingListsResponse> = ShoppingListsResponse::class.java

    override fun onPostExecute(result: ShoppingListsResponse?) {
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