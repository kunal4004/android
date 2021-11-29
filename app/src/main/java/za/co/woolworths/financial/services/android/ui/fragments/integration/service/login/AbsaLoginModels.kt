package za.co.woolworths.financial.services.android.ui.fragments.integration.service.login

import za.co.absa.openbankingapi.woolworths.integration.dto.Header
import java.lang.StringBuilder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class LoginRequestProperty(
    private val aliasId: String,
    private val deviceId: String,
    private val credential: String,
    private val symmetricKey: String,
    private val symmetricKeyIV: String
) {
    private val sat = 5
    private val applicationId = "WCOBMOBAPP"
    private val aliasType = 2
    private val type = 2

    val urlEncodedFormData: String
        get() {
            val utfEncodingType = StandardCharsets.UTF_8.name()
            val sb = StringBuilder()
                .append("SAT=").append(sat)
                .append("&ApplicationId=").append(applicationId)
                .append("&aliasId=").append(URLEncoder.encode(aliasId, utfEncodingType))
                .append("&deviceid=").append(deviceId)
                .append("&credential=").append(URLEncoder.encode(credential, utfEncodingType))
                .append("&aliasType=").append(aliasType)
                .append("&type=").append(type)
                .append("&symmetricKeyIV=").append(this.symmetricKeyIV)
                .append("&symmetrickey=").append(URLEncoder.encode(symmetricKey, utfEncodingType))
            return sb.toString()
        }
}


class LoginResponseProperty {
    val header: Header? = null
    val result: String? = null
    val nonce: String? = null
    var esessionid: String? = null
    private val timestamp: String? = null
    private val lastLogin: String? = null
    private val surePhrase: String? = null
    private val limitsEnabled = false
    private val simSwapHold = false
    private val rvnBranchHold = false
    private val ficaStatus: String? = null
    private val userPreferredLanguage: String? = null
    private val registrationDate: String? = null
    val resultMessage: String? = null
    private val landingPage: String? = null
}