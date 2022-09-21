package za.co.woolworths.financial.services.android.ui.fragments.integration.service.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

data class AbsaProxyResponseProperty(
    val proxy: ProxyPayloadAndHeaders,
    val response: Response,
    val keyId: String,
    val httpCode: Int
)

data class ProxyPayloadAndHeaders(var payload: String? = null, var headers: ProxyHeaders? = null)

data class ProxyHeaders(
    var date: String,
    var setCookie: String?,
    var contentLength: String?,
    var keepAlive: String?,
    var connection: String?,
    var contentType: String?
)

@Parcelize
data class Response(
    var code: String?,
    var desc: String?,
    var stsParams: String?,
    var message: String?,
    var version: String?
) : Parcelable