package za.co.woolworths.financial.services.android.ui.fragments.integration.service.validate_card_and_pin

import za.co.absa.openbankingapi.woolworths.integration.dto.Header
import za.co.absa.openbankingapi.woolworths.integration.dto.SecurityNotificationType

data class ValidateCardAndPinRequestProperty(
        val header: Header? = Header(),
        val cardToken: String?,
        val cardPIN: String?,
        val symmetricKey: String?,
        val symmetricKeyIV: String?
)

data class ValidateCardAndPinResponseProperty(
    val header: Header? = null,
    val result: String? = null,
    var securityNotificationType: SecurityNotificationType? = SecurityNotificationType.SureCheck,
    var cellNumber: String? = null,
    val resendsRemaining: Int = 0,
    val otpRetriesLeft: Int = 0
)