package za.co.woolworths.financial.services.android.models.rest.loan


import android.text.TextUtils

import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.IssueLoan
import za.co.woolworths.financial.services.android.models.dto.IssueLoanResponse
import za.co.woolworths.financial.services.android.util.HttpAsyncTask
import za.co.woolworths.financial.services.android.util.OnEventListener

class PostLoanIssue(private val issueLoan: IssueLoan, private val mCallBack: OnEventListener<IssueLoanResponse>?)
    : HttpAsyncTask<String, String, IssueLoanResponse>() {
    var mException: String? = null

    override fun httpDoInBackground(vararg params: String): IssueLoanResponse {
        return WoolworthsApplication.getInstance().api.issueLoan(issueLoan)
    }

    override fun httpError(errorMessage: String, httpErrorCode: HttpAsyncTask.HttpErrorCode): IssueLoanResponse {
        mException = errorMessage
        mCallBack!!.onFailure(errorMessage)
        return IssueLoanResponse()
    }

    override fun httpDoInBackgroundReturnType(): Class<IssueLoanResponse> {
        return IssueLoanResponse::class.java
    }

    override fun onPostExecute(issueLoanResponse: IssueLoanResponse) {
        super.onPostExecute(issueLoanResponse)
        if (mCallBack != null) {
            if (TextUtils.isEmpty(mException)) mCallBack.onSuccess(issueLoanResponse)
        }
    }
}
