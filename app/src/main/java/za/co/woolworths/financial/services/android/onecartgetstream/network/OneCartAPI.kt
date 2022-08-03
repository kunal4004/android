package za.co.woolworths.financial.services.android.onecartgetstream.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface OneCartAPI {
    @POST("/v1/chat/woolworths/authenticate")
    fun authenticate(@Body payload: OCAuthenticationDto): Call<OCAuthenticationResponse>
}