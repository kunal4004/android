package za.co.woolworths.financial.services.android.ui.fragments.integration.helper

import za.co.woolworths.financial.services.android.ui.fragments.integration.service.cekd.CekdResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.create_alias.CreateAliasResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.get_all_balances.AbsaBalanceEnquiryResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.get_archive_statements.ArchivedStatementListResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.get_individual_statements.IndividualStatementResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.login.LoginResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.register_credential.AbsaRegisterCredentialResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.validate_card_and_pin.ValidateCardAndPinResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.validate_sure_checks.ValidateSureCheckResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.AbsaApiFailureHandler

sealed class AbsaResultWrapper {

    sealed class Section : AbsaResultWrapper()  {

        sealed class Cekd : Section(){
            data class StatusCodeValid(val validateCardAndPinResponseProperty: CekdResponseProperty) : Cekd()
        }

        sealed class ValidateCardAndPin : Section() {
            data class ValidateCardAndPinStatusCodeValid(val validateCardAndPinResponseProperty: ValidateCardAndPinResponseProperty) : ValidateCardAndPin()
            data class StatusCodeInvalid(val failure: AbsaApiFailureHandler?) : AbsaResultWrapper()
        }

        sealed class ValidateSureCheck : Section() {
            data class StatusCodeValid(val validateSureCheckResponseProperty: ValidateSureCheckResponseProperty) : ValidateCardAndPin()
            object Accepted : ValidateSureCheck()
            data class FailedMessage(var message: String, var isActivityRunning : Boolean = false) : ValidateSureCheck()
            data class ContinueValidateSureCheck(var message: String, var isActivityRunning : Boolean = false) : ValidateSureCheck()
            object PresentOtp : ValidateSureCheck()
            data class StatusCodeInvalid(var absaApiFailureHandler: AbsaApiFailureHandler?) : ValidateSureCheck()
        }

        sealed class CreateAlias : Section() {

            data class StatusCodeValid(val createAliasResponseProperty: CreateAliasResponseProperty) : CreateAlias()
            data class StatusCodeInValid(val failure: AbsaApiFailureHandler?) : CreateAlias()
        }

        sealed class RegisterCredentials : Section() {
            data class StatusCodeValid(val response: AbsaRegisterCredentialResponseProperty) : RegisterCredentials()
            data class StatusCodeInValid(val failure: AbsaApiFailureHandler?) : RegisterCredentials()
        }

        sealed class Login : Section() {
            data class StatusCodeValid(val response: LoginResponseProperty) : Login()
            data class StatusCodeInValid(val failure: AbsaApiFailureHandler?) : Login()
        }

        sealed class ListStatement : Section() {
            data class FacadeStatusCodeValid(val response: AbsaBalanceEnquiryResponseProperty?) : ListStatement()
            data class ArchivedStatusCodeValid(val response: ArchivedStatementListResponseProperty?) : ListStatement()
            class IndividualStatusCodeValid() : ListStatement()
            data class StatusCodeInValid(val failure: AbsaApiFailureHandler?) : ListStatement()
        }

        sealed class IndividualStatement : Section() {
            data class StatusCodeInValid(val failure: AbsaApiFailureHandler?) : IndividualStatement()
        }
    }

    data class Failure(val failure: AbsaApiFailureHandler?): AbsaResultWrapper()

    object Loading: AbsaResultWrapper()
}
