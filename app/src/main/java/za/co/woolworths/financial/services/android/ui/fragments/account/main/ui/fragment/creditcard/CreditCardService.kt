package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.creditcard

import retrofit2.Response
import retrofit2.http.GET
import za.co.woolworths.financial.services.android.models.dto.CreditCardTokenResponse

interface CreditCardService {

    @GET("wfs/app/v4/user/creditCardToken")
    suspend fun getCreditCardToken(): Response<CreditCardTokenResponse>

}