package za.co.woolworths.financial.services.android.ui.fragments.integration.utils


import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import org.json.JSONException
import org.json.JSONObject
import za.co.absa.openbankingapi.woolworths.integration.dto.Header
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.integration.helper.AbsaResultWrapper
import za.co.woolworths.financial.services.android.ui.fragments.integration.helper.AbsaTemporaryDataSourceSingleton
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.cekd.CekdResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.create_alias.CreateAliasResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.get_all_balances.AbsaBalanceEnquiryResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.get_archive_statements.ArchivedStatementListResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.get_individual_statements.IndividualStatementResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.login.LoginResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.model.AbsaProxyResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.model.Response
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.register_credential.AbsaRegisterCredentialResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.validate_card_and_pin.ValidateCardAndPinResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.validate_sure_checks.ValidateSureCheckResponseProperty
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager.Companion.logException
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager.Companion.setCrashlyticsString
import kotlin.reflect.KClass

class AbsaApiResponse<W: Any>(isResponseBodyEncrypted: Boolean = false, resultFromNetwork: NetworkState<Any>, private val typeParameterClass:KClass<W>, private val outputResult: (Any?) -> Unit) : IAbsaApiResponseWrapper {

    companion object {
         val DOC_TYPE : String by lazy("<!DOCTYPE"::lowercase)
    }
    init {
        when (resultFromNetwork) {
            is NetworkState.Success -> {
                when (val data = resultFromNetwork.data) {
                    is AbsaProxyResponseProperty -> {
                        when (data.httpCode) {

                            AppConstant.HTTP_OK -> {
                                val proxy = data.proxy
                                var proxyPayload = proxy.payload

                                proxyPayload = decryptedPayloadInStringFormat(isResponseBodyEncrypted, proxyPayload)
                                try {
                                    val payloadJSONObject = JSONObject(proxyPayload ?: "")
                                    saveKeyId(payloadJSONObject)
                                    saveJSessionId(payloadJSONObject)

                                    outputResult(handleAbsaStatusCode(proxyPayload))
                                } catch (e: JSONException){
                                    val proxyPayloadResult = proxy.payload
                                    outputResult( when(proxyPayloadResult?.lowercase()?.contains(DOC_TYPE)) {
                                        true -> AbsaResultWrapper.Section.ListStatement.IndividualStatusCodeValid()
                                        else ->proxyPayloadResult?.toAes256DecryptBase64BodyToByteArray()
                                    })
                            }

                            }

                            AppConstant.HTTP_SESSION_TIMEOUT_440 -> {
                                data.response.stsParams?.let { stsParams -> outputResult(AbsaResultWrapper.Failure(AbsaApiFailureHandler.AppServerFailure.SessionTimeout(stsParams, AppConstant.HTTP_SESSION_TIMEOUT_440)))}
                            }

                            else -> {
                                // handle other application server cases
                                data.response.let { response -> outputResult(AbsaResultWrapper.Failure(AbsaApiFailureHandler.AppServerFailure.GeneralFailure(response.message, response.code?.toInt()))) } }
                        }
                    }

                    is Response -> { data.let { response -> outputResult(AbsaResultWrapper.Failure(AbsaApiFailureHandler.AppServerFailure.GeneralFailure(response.message, response.code?.toInt())))} } }
            }

            is NetworkState.Error -> outputResult(AbsaResultWrapper.Failure(resultFromNetwork.error))
            is NetworkState.Loading -> outputResult(AbsaResultWrapper.Loading)
        }
    }


    override fun decryptedPayloadInStringFormat(isResponseBodyEncrypted: Boolean, payload: String?): String? {
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
        if (payloadJSONObject.has("header")) {
            val header = payloadJSONObject.get("header") as? JSONObject
            AbsaTemporaryDataSourceSingleton.jsessionId = when (header?.has("jsessionid")) {
                true -> header.getString("jsessionid")
                else -> AbsaTemporaryDataSourceSingleton.jsessionId
            }
        }
    }

    override fun handleAbsaStatusCode(payload: String?): AbsaResultWrapper? {
        val response: W?
        try {
            response = Gson().fromJson(payload, typeParameterClass.java)
        } catch (e: JsonSyntaxException) {
            setCrashlyticsString("handleAbsaStatusCode - JsonSyntaxException with payload",payload)
            logException(e)
            return null
        } catch (e: JSONException) {
            setCrashlyticsString("handleAbsaStatusCode - JSONException with payload",payload)
            logException(e)
            return null
        }
        with(response) {
            return when (this) {
                is CekdResponseProperty -> AbsaResultWrapper.Section.Cekd.StatusCodeValid(this)

                is ValidateCardAndPinResponseProperty -> {
                    when (isStatusCodeValid(header) && result?.lowercase() in mutableListOf(
                        "success",
                        "processing"
                    )) { // in == contains
                        true -> AbsaResultWrapper.Section.ValidateCardAndPin.ValidateCardAndPinStatusCodeValid(
                            this
                        )
                        false -> AbsaResultWrapper.Section.ValidateCardAndPin.StatusCodeInvalid(
                            AbsaApiFailureHandler.FeatureValidateCardAndPin.ValidateCardAndPinStatusCodeInvalid(
                                setErrorMessage(header),
                                false
                            )
                        )
                    }
                }

                is ValidateSureCheckResponseProperty -> when (isStatusCodeValid(header)) {
                    true -> AbsaResultWrapper.Section.ValidateSureCheck.StatusCodeValid(this)
                    false -> AbsaResultWrapper.Section.ValidateSureCheck.StatusCodeInvalid(
                        AbsaApiFailureHandler.FeatureValidateCardAndPin.ValidateSureCheckStatusCodeInvalid(
                            setErrorMessage(header)
                        )
                    )
                }

                is CreateAliasResponseProperty -> when (isStatusCodeValid(header) && aliasId?.isNotEmpty() == true) {
                    true -> AbsaResultWrapper.Section.CreateAlias.StatusCodeValid(this)
                    false -> AbsaResultWrapper.Section.CreateAlias.StatusCodeInValid(
                        AbsaApiFailureHandler.FeatureValidateCardAndPin.InvalidAliasIdStatusCode(
                            setErrorMessage(header)
                        )
                    )
                }

                is AbsaRegisterCredentialResponseProperty -> when (isStatusCodeValid(header)) {
                    true -> AbsaResultWrapper.Section.RegisterCredentials.StatusCodeValid(this)
                    false -> AbsaResultWrapper.Section.RegisterCredentials.StatusCodeInValid(
                        AbsaApiFailureHandler.FeatureValidateCardAndPin.InvalidAbsaRegisterCredentialStatusCode(
                            setErrorMessage(header)
                        )
                    )
                }

                is LoginResponseProperty -> {
                    when (nonce != null && nonce.isNotEmpty() && result?.lowercase() == "success") {
                        true -> {
                            AbsaResultWrapper.Section.Login.StatusCodeValid(this)
                        }
                        false -> AbsaResultWrapper.Section.Login.StatusCodeInValid(
                            AbsaApiFailureHandler.FeatureValidateCardAndPin.InvalidAbsaLoginStatusCode(resultMessage ?: bindString(R.string.absa_technical_error_occurred))
                        )
                    }
                }

                is AbsaBalanceEnquiryResponseProperty -> when (isStatusCodeValid(header)) {
                    true -> AbsaResultWrapper.Section.ListStatement.FacadeStatusCodeValid(this)
                    false -> AbsaResultWrapper.Section.ListStatement.StatusCodeInValid(
                        AbsaApiFailureHandler.ListStatement.FacadeStatusCodeInvalid(
                            setErrorMessage(header)
                        )
                    )
                }

                is ArchivedStatementListResponseProperty -> {
                    when (header?.resultMessages?.size == 0) {
                        true -> AbsaResultWrapper.Section.ListStatement.ArchivedStatusCodeValid(this)
                        false -> AbsaResultWrapper.Section.ListStatement.StatusCodeInValid(
                            AbsaApiFailureHandler.ListStatement.FacadeStatusCodeInvalid(setErrorMessage(header))
                        )
                    }
                }

                is IndividualStatementResponseProperty -> AbsaResultWrapper.Section.IndividualStatement.StatusCodeInValid(AbsaApiFailureHandler.ListStatement.FacadeStatusCodeInvalid(setErrorMessage(header)))
                else -> null
            }
       }
    }

    private fun setErrorMessage(header: Header?) = header?.resultMessages?.first()?.responseMessage ?: bindString(R.string.absa_technical_error_occurred)

    override fun isStatusCodeValid(header: Header?) : Boolean = header?.resultMessages?.size == 0 && header.statusCode?.equals("0", ignoreCase = true) == true
}
