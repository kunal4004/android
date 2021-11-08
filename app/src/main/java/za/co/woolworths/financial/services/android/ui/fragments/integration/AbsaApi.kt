package za.co.woolworths.financial.services.android.ui.fragments.integration

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.cekd.CekdRequestProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.model.AbsaProxyResponseProperty

interface AbsaApi {

    /** Absa Content Encryption */
    @POST("absa/cekd")
    suspend fun queryAbsaContentEncryptionKeyID(@Body cekdRequest: CekdRequestProperty?): AbsaProxyResponseProperty

    @Headers("action: validateCardAndPin", "Content-Type: text/plain")
    @POST("absa/validateCardAndPin")
    suspend fun queryAbsaServiceValidateCardAndPin(@Body body: String): AbsaProxyResponseProperty

    @Headers("action: validateSurecheck", "Content-Type: text/plain")
    @POST("absa/validateSureCheck")
    suspend fun queryAbsaServiceValidateSureCheck( @Body body: String): AbsaProxyResponseProperty

    @Headers("action: createAlias", "Content-Type: text/plain")
    @POST("absa/createAlias")
    suspend fun queryAbsaServiceCreateAlias(@Body body: String?): AbsaProxyResponseProperty

    @Headers("action: registerCredential", "Content-Type: text/plain")
    @POST("absa/registerCredentials")
    suspend fun queryAbsaServiceRegisterCredentials(@Body body: String?): AbsaProxyResponseProperty

    @Headers("action: login","Accept: application/json", "Content-Type: application/x-www-form-urlencoded")
    @POST("absa/login")
    suspend fun queryAbsaServiceLogin(@Body body: String?): AbsaProxyResponseProperty

    @Headers("Content-Type: text/plain")
    @POST("absa/getAllBalances")
    suspend fun queryAbsaServiceGetAllBalances(@Body body: String?): AbsaProxyResponseProperty

    @Headers("Content-Type: text/plain")
    @POST("absa/archivedStatement")
    suspend fun queryAbsaServiceGetArchivedStatement(@Body body: String?): AbsaProxyResponseProperty

    @Headers("Content-Type: text/plain")
    @POST("absa/getIndividualStatement")
    suspend fun queryAbsaServiceGetIndividualStatement(@Body body: String?): AbsaProxyResponseProperty

}