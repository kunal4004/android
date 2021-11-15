package za.co.woolworths.financial.services.android.ui.fragments.integration.utils

import org.json.JSONObject
import za.co.absa.openbankingapi.woolworths.integration.dto.Header
import za.co.woolworths.financial.services.android.ui.fragments.integration.helper.AbsaResultWrapper
import kotlin.reflect.KClass

interface IAbsaApiResponseWrapper{
    fun decryptedPayloadInStringFormat(isResponseBodyEncrypted: Boolean, payload : String?): String?
    fun saveKeyId(payloadJSONObject: JSONObject)
    fun saveJSessionId(payloadJSONObject: JSONObject)
    fun handleAbsaStatusCode(payload: String?): AbsaResultWrapper?
    fun isStatusCodeValid(header: Header?) : Boolean
}