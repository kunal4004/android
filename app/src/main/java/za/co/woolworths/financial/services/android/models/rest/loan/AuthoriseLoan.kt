package za.co.woolworths.financial.services.android.models.rest.loan


import android.text.TextUtils

import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.AuthoriseLoanRequest
import za.co.woolworths.financial.services.android.models.dto.AuthoriseLoanResponse
import za.co.woolworths.financial.services.android.util.HttpAsyncTask
import za.co.woolworths.financial.services.android.util.OnEventListener

class AuthoriseLoan(private val authoriseLoanRequest: AuthoriseLoanRequest, private val mCallBack: OnEventListener<AuthoriseLoanResponse>?)
    : HttpAsyncTask<String, String, AuthoriseLoanResponse>() {
    var mException: String? = null

    override fun httpDoInBackground(vararg params: String): AuthoriseLoanResponse {
        return WoolworthsApplication.getInstance().api.authoriseLoan(authoriseLoanRequest)
    }

    override fun httpError(errorMessage: String, httpErrorCode: HttpAsyncTask.HttpErrorCode): AuthoriseLoanResponse {
        mException = errorMessage
        mCallBack!!.onFailure(errorMessage)
        return AuthoriseLoanResponse()
    }

    override fun httpDoInBackgroundReturnType(): Class<AuthoriseLoanResponse> {
        return AuthoriseLoanResponse::class.java
    }

    override fun onPostExecute(authoriseLoanRequest: AuthoriseLoanResponse) {
        super.onPostExecute(authoriseLoanRequest)
        if (mCallBack != null) {
            if (TextUtils.isEmpty(mException)) mCallBack.onSuccess(authoriseLoanRequest)
        }
    }
}
