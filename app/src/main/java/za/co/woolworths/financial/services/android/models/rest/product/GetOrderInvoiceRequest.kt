package za.co.woolworths.financial.services.android.models.rest.product

import za.co.woolworths.financial.services.android.contracts.AsyncAPIResponse
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.OrderTaxInvoiceResponse
import za.co.woolworths.financial.services.android.util.HttpAsyncTask

class GetOrderInvoiceRequest(val taxNoteNumber: String, private val responseDelegate: AsyncAPIResponse.ResponseDelegate<OrderTaxInvoiceResponse>?) : HttpAsyncTask<String, String, OrderTaxInvoiceResponse>() {

    private var mException: String? = null

    override fun httpDoInBackground(vararg params: String?): OrderTaxInvoiceResponse {
        return WoolworthsApplication.getInstance().api.getOrderTaxInvoice(taxNoteNumber)
    }

    override fun httpError(errorMessage: String?, httpErrorCode: HttpErrorCode?): OrderTaxInvoiceResponse {
        mException = errorMessage
        responseDelegate?.onFailure(mException!!)
        return OrderTaxInvoiceResponse()
    }

    override fun httpDoInBackgroundReturnType(): Class<OrderTaxInvoiceResponse> = OrderTaxInvoiceResponse::class.java

    override fun onPostExecute(result: OrderTaxInvoiceResponse?) {
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