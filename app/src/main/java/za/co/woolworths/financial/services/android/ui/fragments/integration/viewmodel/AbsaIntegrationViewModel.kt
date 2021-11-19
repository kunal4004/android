package za.co.woolworths.financial.services.android.ui.fragments.integration.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.awfs.coordination.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import za.co.absa.openbankingapi.woolworths.integration.dto.ArchivedStatement
import za.co.absa.openbankingapi.woolworths.integration.dto.Header
import za.co.absa.openbankingapi.woolworths.integration.dto.SecurityNotificationType
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.integration.helper.AbsaResultWrapper
import za.co.woolworths.financial.services.android.ui.fragments.integration.helper.AbsaTemporaryDataSourceSingleton
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.cekd.AbsaContentEncryptionKeyIdImpl
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.cekd.CekdResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.common.SessionKeyGenerator
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.create_alias.AbsaCreateAliasImpl
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.create_alias.CreateAliasResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.get_all_balances.AbsaBalanceEnquiryFacadeGetAllBalancesImpl
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.get_all_balances.AbsaBalanceEnquiryResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.get_archive_statements.AbsaGetArchivedStatementListRequestImpl
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.get_archive_statements.ArchivedStatementListResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.get_individual_statements.AbsaGetIndividualStatementImpl
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.login.AbsaLoginImpl
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.login.LoginResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.register_credential.AbsaRegisterCredentialResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.register_credential.AbsaRegisterCredentialsImpl
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.validate_card_and_pin.ValidateCardAndPinImpl
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.validate_card_and_pin.ValidateCardAndPinResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.validate_sure_checks.ValidateSureCheckImpl
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.validate_sure_checks.ValidateSureCheckResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.AbsaApiFailureHandler
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.AbsaApiResponse
import java.io.ByteArrayInputStream
import java.util.concurrent.ScheduledFuture

class AbsaIntegrationViewModel : ViewModel() {

    private var acceptedResultMessages = mutableListOf("success", "processed")
    private val failedResultMessages = mutableListOf("failed", "rejected")
    private val continuePollingProcessResultMessage = mutableListOf("processing")
    private val presentOTPScreenResultMessage = mutableListOf("revertback")
    private var mCellNumber: String? = null
    private var mScheduleValidateSureCheck: ScheduledFuture<*>? = null

    private val _failureHandler = MutableLiveData<AbsaApiFailureHandler?>()
    val failureHandler: LiveData<AbsaApiFailureHandler?>
        get() = _failureHandler

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean?>
        get() = _isLoading

    private val _validateSureCheckResponseProperty = MutableLiveData<ValidateSureCheckResponseProperty?>()
    val validateSureCheckResponseProperty: LiveData<ValidateSureCheckResponseProperty?>
        get() = _validateSureCheckResponseProperty

    private val _createAliasId = MutableLiveData<String?>()
    val createAliasId: LiveData<String?>
        get() = _createAliasId

    private val _cellNumber = MutableLiveData<String?>()
    val cellNumber: LiveData<String?>
        get() = _cellNumber

    private val _registerCredentialResponse = MutableLiveData<AbsaRegisterCredentialResponseProperty?>()
    val registerCredentialResponse: LiveData<AbsaRegisterCredentialResponseProperty?>
        get() = _registerCredentialResponse

    private val _loginResponseProperty = MutableLiveData<LoginResponseProperty?>()
    val loginResponseProperty: LiveData<LoginResponseProperty?>
        get() = _loginResponseProperty

    private val _absaBalanceEnquiryResponseProperty = MutableLiveData<AbsaBalanceEnquiryResponseProperty?>()
    val absaBalanceEnquiryResponseProperty: LiveData<AbsaBalanceEnquiryResponseProperty?>
        get() = _absaBalanceEnquiryResponseProperty

    private val _archivedStatementResponse = MutableLiveData<ArchivedStatementListResponseProperty?>()
    val archivedStatementResponse: LiveData<ArchivedStatementListResponseProperty?>
        get() = _archivedStatementResponse

    private val _individualStatementResponseProperty = MutableLiveData<Any?>()
    val individualStatementResponseProperty: LiveData<Any?>
        get() = _individualStatementResponseProperty

    private val absaValidateCardAndPinDelegate = AbsaRegisterCardAndPinDelegateImpl(
        SessionKeyGenerator(),
        AbsaContentEncryptionKeyIdImpl(),
        ValidateCardAndPinImpl(SessionKeyGenerator()),
        ValidateSureCheckImpl(),
        AbsaCreateAliasImpl(SessionKeyGenerator()))

    private val absaRegisterCredentialDelegate = AbsaRegisterCredentialDelegateImpl(AbsaRegisterCredentialsImpl(SessionKeyGenerator()))

    private val absaLoginDelegate = AbsaLoginDelegateImpl(
        AbsaContentEncryptionKeyIdImpl(),
        AbsaLoginImpl(SessionKeyGenerator()))

    private val absaShowStatementDelegate = AbsaShowStatementDelegateImpl(
        AbsaBalanceEnquiryFacadeGetAllBalancesImpl(),
        AbsaGetArchivedStatementListRequestImpl(),
        AbsaGetIndividualStatementImpl())

    fun fetchAbsaContentEncryptionKeyId(cardPin: String?, cardToken: String?) {
        inProgress(true)
        with(absaValidateCardAndPinDelegate) {
            viewModelScope.launch(Dispatchers.IO) {
                val fetchAbsaContentEncryptionKeyId = fetchAbsaContentEncryptionKeyId()
                AbsaApiResponse(false, fetchAbsaContentEncryptionKeyId, CekdResponseProperty::class) { result ->
                    when(result){
                        is AbsaResultWrapper.Section.Cekd.StatusCodeValid ->  viewModelScope.launch(Dispatchers.IO) { executeValidateCardAndPin(cardPin, cardToken) }
                        is AbsaResultWrapper.Failure ->  failureHandler(result.failure)
                        else -> return@AbsaApiResponse
                    }
                }
            }
        }
    }

    private fun executeValidateCardAndPin(cardPin: String?, cardToken: String?) {
        with(absaValidateCardAndPinDelegate) {
            viewModelScope.launch(Dispatchers.IO) {
                val fetchValidateCardAndPin = fetchValidateCardAndPin(cardPin, cardToken)
                AbsaApiResponse(true, fetchValidateCardAndPin, ValidateCardAndPinResponseProperty::class) { result ->
                    when (result) {
                        is AbsaResultWrapper.Loading -> inProgress(true)
                        is AbsaResultWrapper.Section.ValidateCardAndPin.ValidateCardAndPinStatusCodeValid -> {
                            with(result.validateCardAndPinResponseProperty){
                                mCellNumber = cellNumber
                                fetchValidateSureCheck(securityNotificationType)
                            }
                        }
                        is AbsaResultWrapper.Section.ValidateCardAndPin.StatusCodeInvalid -> failureHandler(result.failure)
                        is AbsaResultWrapper.Failure -> failureHandler(result.failure)
                        else -> return@AbsaApiResponse
                    }
                }
            }
        }
    }

     private fun fetchValidateSureCheck(securityNotificationType: SecurityNotificationType?) {
        with(absaValidateCardAndPinDelegate) {
            mScheduleValidateSureCheck = schedulePollingWithFixedDelay {
                viewModelScope.launch(Dispatchers.IO) {
                    val fetchAbsaValidateSureCheck = fetchAbsaValidateSureCheck(securityNotificationType)
                    AbsaApiResponse(true, fetchAbsaValidateSureCheck, ValidateSureCheckResponseProperty::class) { result ->
                        when(result){
                           is AbsaResultWrapper.Loading -> inProgress(true)
                           is AbsaResultWrapper.Failure ->  {
                               failureHandler(result.failure)
                               stopPolling()
                           }
                           is AbsaResultWrapper.Section.ValidateSureCheck.StatusCodeInvalid -> {
                               failureHandler(result.absaApiFailureHandler)
                               stopPolling()
                           }
                           is AbsaResultWrapper.Section.ValidateSureCheck.StatusCodeValid -> {
                               val validateCardAndPinResponseProperty = result.validateCardAndPinResponseProperty

                               when (securityNotificationType){
                                   SecurityNotificationType.OTP -> { // handle OTP Scenarios
                                       stopPolling()
                                       _cellNumber.postValue(validateCardAndPinResponseProperty.cellNumber)
                                   }

                                   SecurityNotificationType.SureCheck -> { //handle Surechecks
                                       when(validateCardAndPinResponseProperty.result.lowercase()){
                                           in acceptedResultMessages -> {
                                               //SureCheck was accepted, continue with registration process
                                               stopPolling()
                                               this@AbsaIntegrationViewModel.fetchCreateAlias()
                                           }

                                           in failedResultMessages -> {
                                               // Sending of the SureCheck failed for some reason. Stop registration details.
                                               // Display an error message and advise to try again later
                                               failureHandler(AbsaApiFailureHandler.FeatureValidateCardAndPin.InvalidValidateSureCheckFailedMessage(bindString(R.string.absa_technical_error_occurred_try_again_later),true))
                                               stopPolling()
                                           }

                                           in continuePollingProcessResultMessage -> {
                                               // SureCheck was sent, no client response yet. If > 60 seconds,
                                               // prompt to resend. If resend, same polling as above.
                                               // (5 resends allowed)
                                               pollingCount += 1
                                               if (pollingCount > 5) {
                                                   failureHandler(AbsaApiFailureHandler.FeatureValidateCardAndPin.InvalidValidateSureCheckContinuePolling(bindString(R.string.absa_maximum_polling_rate_reached),true))
                                                   stopPolling()
                                               }
                                           }

                                           in presentOTPScreenResultMessage -> {
                                               // TODO:: Unable to send surecheck (USSD).
                                               // Present an input screen for the OTP,
                                               // as well as a different request payload.
                                               // #note: consider as rejected for now
                                               // failureHandler("An error has occurred. Please try again later.", true)
                                               stopPolling()
                                               fetchValidateSureCheckForOTP()
                                           }
                                       }
                                   }
                               }
                           }
                        }
                    }
                }
            }
        }
    }

    fun fetchValidateSureCheckForOTP(otpToBeVerified : String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            with(absaValidateCardAndPinDelegate) {
                val fetchAbsaValidateSureCheck =
                    fetchAbsaValidateSureCheckOTP(SecurityNotificationType.OTP, otpToBeVerified)
                AbsaApiResponse(true, fetchAbsaValidateSureCheck, ValidateSureCheckResponseProperty::class) { result ->
                    when (result) {
                        is AbsaResultWrapper.Loading -> inProgress(true)
                        is AbsaResultWrapper.Failure -> {
                            failureHandler(result.failure)
                            stopPolling()
                        }
                        is AbsaResultWrapper.Section.ValidateSureCheck.StatusCodeValid -> {
                            when(otpToBeVerified == null){
                                true -> {
                                    mCellNumber = result.validateCardAndPinResponseProperty.cellNumber
                                    _cellNumber.postValue(mCellNumber)
                                }
                                false -> {
                                    _validateSureCheckResponseProperty.postValue(result.validateCardAndPinResponseProperty)
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    fun fetchCreateAlias() {
        viewModelScope.launch(Dispatchers.IO) {
            with(absaValidateCardAndPinDelegate) {
                val fetchCreateAlias = fetchCreateAlias()
                AbsaApiResponse(true, fetchCreateAlias, CreateAliasResponseProperty::class)
                { result ->
                    when (result) {
                        is AbsaResultWrapper.Loading -> inProgress(true)
                        is AbsaResultWrapper.Failure -> failureHandler(result.failure)
                        is AbsaResultWrapper.Section.CreateAlias.StatusCodeInValid -> failureHandler(result.failure)
                        is AbsaResultWrapper.Section.CreateAlias.StatusCodeValid -> {
                            val aliasId = handleCreateAliasResult(result.createAliasResponseProperty)
                            _createAliasId.postValue(aliasId)
                        }
                    }
                }
            }
        }
    }

    fun fetchRegisterCredentials(aliasId: String?, passcode: String?) {
        with(absaRegisterCredentialDelegate) {
            viewModelScope.launch(Dispatchers.IO) {
                val fetchRegisterCredentialResponse =
                    fetchAbsaRegisterCredentials(aliasId, passcode)
                AbsaApiResponse(
                    true,
                    fetchRegisterCredentialResponse,
                    AbsaRegisterCredentialResponseProperty::class
                ) { registerCredentials ->
                    when (registerCredentials) {
                        is AbsaResultWrapper.Loading -> inProgress(true)
                        is AbsaResultWrapper.Failure -> failureHandler(registerCredentials.failure)
                        is AbsaResultWrapper.Section.RegisterCredentials.StatusCodeValid -> {
                            _registerCredentialResponse.postValue(registerCredentials.response)
                        }
                        is AbsaResultWrapper.Section.RegisterCredentials.StatusCodeInValid -> {
                            failureHandler(registerCredentials.failure)
                        }

                    }
                }
            }
        }
    }

    fun fetchLogin(passcode: String){
        with(absaLoginDelegate){
        viewModelScope.launch(Dispatchers.IO) {
            inProgress(true)
            val fetchAbsaContentEncryptionKeyId = fetchAbsaContentEncryptionKeyId()
            AbsaApiResponse(
                false,
                fetchAbsaContentEncryptionKeyId,
                CekdResponseProperty::class) { result ->
                when(result){
                    AbsaResultWrapper.Loading -> inProgress(true)
                    is AbsaResultWrapper.Failure -> failureHandler(result.failure)
                    is AbsaResultWrapper.Section.Cekd.StatusCodeValid->  viewModelScope.launch(Dispatchers.IO) {
                        val fetchRegisterCredentialResponse =  fetchAbsaLogin(passcode)
                        AbsaApiResponse(true, fetchRegisterCredentialResponse, LoginResponseProperty::class) { login ->
                            when(login){
                                is AbsaResultWrapper.Loading -> inProgress(true)
                                is AbsaResultWrapper.Failure -> failureHandler(login.failure)
                                is AbsaResultWrapper.Section.Login.StatusCodeValid -> { _loginResponseProperty.postValue(login.response)}
                                is AbsaResultWrapper.Section.Login.StatusCodeInValid -> { failureHandler(login.failure)}
                            }
                            inProgress(false)
                        }
                    }
                }
            }
        }
        }
    }

    fun fetchBalanceEnquiryFacadeGetAllBalances(eSessionId: String?, nonce: String?, timestampAsString: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            inProgress(true)
            val  fetchAbsaBalanceEnquiryFacadeGetAllBalance =  absaShowStatementDelegate.fetchAbsaBalanceEnquiryFacadeGetAllBalance(eSessionId, nonce, timestampAsString)
            AbsaApiResponse(
                true,
                fetchAbsaBalanceEnquiryFacadeGetAllBalance,
                AbsaBalanceEnquiryResponseProperty::class){ resultWrapper ->
                when(resultWrapper){
                    is AbsaResultWrapper.Loading -> inProgress(true)
                    is AbsaResultWrapper.Failure -> _failureHandler.postValue(resultWrapper.failure)
                    is AbsaResultWrapper.Section.ListStatement.StatusCodeInValid -> _failureHandler.postValue(resultWrapper.failure)
                    is AbsaResultWrapper.Section.ListStatement.FacadeStatusCodeValid ->_absaBalanceEnquiryResponseProperty.postValue(resultWrapper.response)
                }
                inProgress(false)
            }
        }
    }

    fun fetchArchivedStatement(header: Header?, number: String?) {
        inProgress(true)
        viewModelScope.launch(Dispatchers.IO) {
            val fetchArchivedStatement = absaShowStatementDelegate.fetchAbsaArchivedStatement(header,getCookieWithXFPTAndWFPT(), number)
            AbsaApiResponse(
                true,
                fetchArchivedStatement,
                ArchivedStatementListResponseProperty::class){ resultWrapper ->
                when(resultWrapper){
                    is AbsaResultWrapper.Loading -> inProgress(true)
                    is AbsaResultWrapper.Failure -> _failureHandler.postValue(resultWrapper.failure)
                    is AbsaResultWrapper.Section.ListStatement.StatusCodeInValid -> _failureHandler.postValue(resultWrapper.failure)
                    is AbsaResultWrapper.Section.ListStatement.ArchivedStatusCodeValid ->_archivedStatementResponse.postValue(resultWrapper.response)
                }
                inProgress(false)
            }
        }
    }

    fun fetchIndividualStatement(archivedStatement: ArchivedStatement) {
        viewModelScope.launch(Dispatchers.IO) {
            inProgress(true)
            val fetchIndividualStatement = absaShowStatementDelegate.fetchAbsaIndividualStatement(getCookieWithXFPTAndWFPT(), archivedStatement)
            AbsaApiResponse(
                true,
                fetchIndividualStatement,
                ByteArrayInputStream::class
            ) { resultWrapper ->
                when (resultWrapper) {
                    is AbsaResultWrapper.Loading -> inProgress(true)
                    is AbsaResultWrapper.Failure -> _failureHandler.postValue(resultWrapper.failure)
                    is AbsaResultWrapper.Section.ListStatement.StatusCodeInValid -> _failureHandler.postValue(resultWrapper.failure)
                    is AbsaResultWrapper.Section.ListStatement.IndividualStatusCodeValid -> _individualStatementResponseProperty.postValue(resultWrapper.response)
                    is ByteArray -> _individualStatementResponseProperty.postValue(this)
                }
                inProgress(false)
            }
        }
    }

    private fun getCookieWithXFPTAndWFPT() = "${AbsaTemporaryDataSourceSingleton.cookie};${AbsaTemporaryDataSourceSingleton.xfpt};${AbsaTemporaryDataSourceSingleton.wfpt}"

    private fun stopPolling() {
        absaValidateCardAndPinDelegate.apply {
            pollingCount = 0
            stopPolling(mScheduleValidateSureCheck)
        }
    }

    fun inProgress(state: Boolean) {
        _isLoading.postValue(state)
    }

    private fun failureHandler(appFailureHandler: AbsaApiFailureHandler?){
        _failureHandler.postValue(appFailureHandler)
    }
}
