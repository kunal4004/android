package za.co.woolworths.financial.services.android.models.rest.shoppinglist

import za.co.woolworths.financial.services.android.contracts.AsyncAPIResponse
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.CreditCardTokenResponse
import za.co.woolworths.financial.services.android.util.HttpAsyncTask

class GetCreditCardToken(private val responseDelegate: AsyncAPIResponse.ResponseDelegate<CreditCardTokenResponse>?) : HttpAsyncTask<String, String, CreditCardTokenResponse>() {

    private var mException: String? = null

    override fun httpDoInBackground(vararg params: String?): CreditCardTokenResponse {
        return WoolworthsApplication.getInstance().api.creditCardToken
    }

    override fun httpError(errorMessage: String?, httpErrorCode: HttpErrorCode?): CreditCardTokenResponse {
        mException = errorMessage
        responseDelegate?.onFailure(mException!!)
        return CreditCardTokenResponse()
    }

    override fun httpDoInBackgroundReturnType(): Class<CreditCardTokenResponse> = CreditCardTokenResponse::class.java

    override fun onPostExecute(result: CreditCardTokenResponse) {
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
