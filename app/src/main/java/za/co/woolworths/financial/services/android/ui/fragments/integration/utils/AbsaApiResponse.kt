package za.co.woolworths.financial.services.android.ui.fragments.integration.utils

import com.awfs.coordination.R
import com.google.gson.Gson
import org.json.JSONObject
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.integration.helper.AbsaResultWrapper
import za.co.woolworths.financial.services.android.ui.fragments.integration.helper.AbsaTemporaryDataSourceSingleton
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.cekd.CekdResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.create_alias.CreateAliasResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.model.AbsaProxyResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.model.Response
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.register_credential.AbsaRegisterCredentialResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.validate_card_and_pin.ValidateCardAndPinResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.validate_sure_checks.ValidateSureCheckResponseProperty
import za.co.woolworths.financial.services.android.util.AppConstant
import kotlin.reflect.KClass
class AbsaApiResponse<W: Any>(
    isResponseBodyEncrypted: Boolean = false,
    result: NetworkState<Any>,
    typeParameterClass:W,
    private val outputResult: (AbsaResultWrapper?) -> Unit
) : IAbsaApiResponseWrapper {

    init {
        when (result) {
            is NetworkState.Success -> {
                when (val data = result.data) {
                    is AbsaProxyResponseProperty -> {
                        when (data.httpCode) {

                            AppConstant.HTTP_OK -> {
                                val proxy = data.proxy
                                var payload = proxy.payload

                                payload = decryptedPayloadInStringFormat(isResponseBodyEncrypted, payload)

                                val payloadJSONObject = JSONObject(payload ?: "")

                                saveKeyId(payloadJSONObject)
                                saveJSessionId(payloadJSONObject)

                              val resultWrapper =   when (val payloadObject = convertDecryptedPayloadStringToObject(payload,typeParameterClass::class)) {
                                    is CekdResponseProperty -> AbsaResultWrapper.Section.Cekd.StatusCodeValid(payloadObject)
                                    is ValidateCardAndPinResponseProperty -> when(payloadObject.header?.resultMessages?.first()?.responseMessage?.isEmpty()==true
                                            && payloadObject.header.statusCode?.equals("0", ignoreCase = true) == true
                                            && payloadObject.result?.lowercase() in mutableListOf("success", "processing")) { // in == contains
                                        true -> AbsaResultWrapper.Section.ValidateCardAndPin.ValidateCardAndPinStatusCodeValid(payloadObject)
                                        false -> AbsaResultWrapper.Section.ValidateCardAndPin.StatusCodeInvalid(AbsaApiFailureHandler.FeatureValidateCardAndPin.ValidateCardAndPinStatusCodeInvalid(payloadObject.header?.resultMessages?.first()?.responseMessage ?: bindString(R.string.absa_technical_error_occurred), false))
                                    }
                                    is ValidateSureCheckResponseProperty ->  when(payloadObject.header.resultMessages?.first()?.responseMessage?.isEmpty()==true && payloadObject.header.statusCode?.equals("0", ignoreCase = true) == true){
                                        true -> AbsaResultWrapper.Section.ValidateSureCheck.StatusCodeValid(payloadObject)
                                        false -> AbsaResultWrapper.Section.ValidateSureCheck.StatusCodeInvalid(AbsaApiFailureHandler.FeatureValidateCardAndPin.ValidateSureCheckStatusCodeInvalid(payloadObject.header.resultMessages?.first()?.responseMessage ?: bindString(R.string.absa_technical_error_occurred)))
                                    }

                                    is CreateAliasResponseProperty ->  when(payloadObject.header?.resultMessages?.first()?.responseMessage?.isEmpty()==true && payloadObject.header.statusCode?.equals("0", ignoreCase = true) == true &&  payloadObject.aliasId?.isNotEmpty() == true){
                                        true -> AbsaResultWrapper.Section.CreateAlias.StatusCodeValid(payloadObject)
                                        false -> AbsaResultWrapper.Section.CreateAlias.StatusCodeInValid(AbsaApiFailureHandler.FeatureValidateCardAndPin.InvalidAliasIdStatusCode(payloadObject.header?.resultMessages?.first()?.responseMessage?: bindString(R.string.absa_technical_error_occurred)))
                                    }

                                    is AbsaRegisterCredentialResponseProperty ->when(payloadObject.header?.resultMessages?.first()?.responseMessage?.isEmpty()==true && payloadObject.header.statusCode?.equals("0", ignoreCase = true) == true){
                                        true -> AbsaResultWrapper.Section.RegisterCredentials.StatusCodeValid(payloadObject)
                                        false -> AbsaResultWrapper.Section.RegisterCredentials.StatusCodeInValid(AbsaApiFailureHandler.FeatureValidateCardAndPin.InvalidAbsaRegisterCredentialStatusCode(payloadObject.header?.resultMessages?.first()?.responseMessage?: bindString(R.string.absa_technical_error_occurred)))
                                    }
                                  else -> null
                              }

                                outputResult(resultWrapper)
                            }

                            AppConstant.HTTP_SESSION_TIMEOUT_440 -> {
                                data.response.stsParams?.let { stsParams ->
                                    outputResult(AbsaResultWrapper.Failure(
                                            AbsaApiFailureHandler.AppServerFailure.SessionTimeout(
                                                stsParams,
                                                AppConstant.HTTP_SESSION_TIMEOUT_440
                                            )
                                        )
                                    )
                                }
                            }

                            else -> {
                                // handle other application server cases
                                data.response.let { response ->
                                    outputResult(AbsaResultWrapper.Failure(
                                            AbsaApiFailureHandler.AppServerFailure.GeneralFailure(
                                                response.message,
                                                response.code?.toInt()
                                            )
                                        )
                                    )
                                }
                            }
                        }
                    }

                    is Response -> {
                        data.let { response ->
                            outputResult(AbsaResultWrapper.Failure(
                                    AbsaApiFailureHandler.AppServerFailure.GeneralFailure(
                                        response.message,
                                        response.code?.toInt()
                                    )
                                )
                            )
                        }
                    }
                }
            }

            is NetworkState.Error ->   outputResult(AbsaResultWrapper.Failure(result.error))

            is NetworkState.Loading -> outputResult(AbsaResultWrapper.Loading)
        }
    }

    override fun decryptedPayloadInStringFormat(
        isResponseBodyEncrypted: Boolean,
        payload: String?
    ): String? {
        return when (isResponseBodyEncrypted) {
            true -> payload?.toAes256Decrypt()
            false -> payload
        }
    }

    override fun saveKeyId(payloadJSONObject: JSONObject) {
        AbsaTemporaryDataSourceSingleton.keyId = when (payloadJSONObject.has("keyId")) {
            true -> payloadJSONObject.getString("keyId")
            false -> AbsaTemporaryDataSourceSingleton.keyId
        }
    }

    override fun saveJSessionId(payloadJSONObject: JSONObject) {
        AbsaTemporaryDataSourceSingleton.jsessionId = when (payloadJSONObject.has("jsessionId")) {
            true -> payloadJSONObject.getString("jsessionId")
            false -> AbsaTemporaryDataSourceSingleton.jsessionId
        }
    }

    override fun <T : Any> convertDecryptedPayloadStringToObject(payload: String?, clazz: KClass<T>): T {
        return  Gson().fromJson(payload, clazz.java)
    }
}
