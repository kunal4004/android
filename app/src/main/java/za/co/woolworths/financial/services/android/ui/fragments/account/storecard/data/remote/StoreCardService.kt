package za.co.woolworths.financial.services.android.ui.fragments.account.storecard.data.remote

import com.squareup.okhttp.ResponseBody
import retrofit2.Response
import retrofit2.http.GET

interface StoreCardService {
    @GET("")
    suspend fun TODOService(): Response<ResponseBody>
}