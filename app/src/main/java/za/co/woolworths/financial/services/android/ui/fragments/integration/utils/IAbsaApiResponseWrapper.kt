package za.co.woolworths.financial.services.android.ui.fragments.integration.utils

import org.json.JSONObject
import kotlin.reflect.KClass

interface IAbsaApiResponseWrapper{
    fun decryptedPayloadInStringFormat(isResponseBodyEncrypted: Boolean, payload : String?): String?
    fun saveKeyId(payloadJSONObject: JSONObject)
    fun saveJSessionId(payloadJSONObject: JSONObject)
    fun<T : Any>  convertDecryptedPayloadStringToObject(payload: String?, clazz: KClass<T>): T
}