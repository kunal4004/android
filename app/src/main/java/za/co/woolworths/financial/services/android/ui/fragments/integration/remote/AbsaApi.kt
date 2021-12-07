package za.co.woolworths.financial.services.android.ui.fragments.integration.remote

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import za.co.woolworths.financial.services.android.ui.fragments.integration.helper.AbsaTemporaryDataSourceSingleton
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.cekd.CekdRequestProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.model.AbsaProxyResponseProperty

interface AbsaApi {

    /** Absa Content Encryption */
    @POST("absa/cekd")
    suspend fun queryAbsaContentEncryptionKeyID(
        @Body cekdRequest: CekdRequestProperty?
    ): AbsaProxyResponseProperty

    @Headers("action: validateCardAndPin", "Content-Type: text/plain")
    @POST("absa/validateCardAndPin")
    suspend fun queryAbsaServiceValidateCardAndPin(
        @Header("Cookie") cookie: String?,
        @Body body: String?
    ): AbsaProxyResponseProperty

    @Headers("action: validateSurecheck", "Content-Type: text/plain")
    @POST("absa/validateSureCheck")
    suspend fun queryAbsaServiceValidateSureCheck(
        @Header("Cookie") cookie: String?,
        @Body body: String
    ): AbsaProxyResponseProperty

    @Headers("action: createAlias", "Content-Type: text/plain")
    @POST("absa/createAlias")
    suspend fun queryAbsaServiceCreateAlias(
        @Header("Cookie") cookie: String?,
        @Body body: String?
    ): AbsaProxyResponseProperty

    @Headers("action: registerCredential", "Content-Type: text/plain", "accept:application/json")
    @POST("absa/registerCredentials")
    suspend fun queryAbsaServiceRegisterCredentials(
        @Header("Cookie") cookie: String?,
        @Body body: String?
    ): AbsaProxyResponseProperty

    @Headers(
        "action: login",
        "Accept: application/json",
        "Content-Type: application/x-www-form-urlencoded"
    )
    @POST("absa/login")
    suspend fun queryAbsaServiceLogin(
        @Header("Cookie") cookie: String?,
        @Body body: String?
    ): AbsaProxyResponseProperty

    @Headers("Content-Type: text/plain")
    @POST("absa/getAllBalances")
    suspend fun queryAbsaServiceGetAllBalances(
        @Header("Cookie") cookie: String?,
        @Body body: String?
    ): AbsaProxyResponseProperty

    @Headers("Content-Type: text/plain")
    @POST("absa/archivedStatement")
    suspend fun queryAbsaServiceGetArchivedStatement(
        @Header("Cookie") cookie: String?,
        @Body body: String?
    ): AbsaProxyResponseProperty

    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("absa/getIndividualStatement")
    suspend fun queryAbsaServiceGetIndividualStatement(
        @Header("Cookie") cookie: String?,
        @Body body: String?
    ): AbsaProxyResponseProperty

}