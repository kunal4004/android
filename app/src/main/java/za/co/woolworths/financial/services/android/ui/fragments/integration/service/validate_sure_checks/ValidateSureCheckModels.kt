package za.co.woolworths.financial.services.android.ui.fragments.integration.service.validate_sure_checks

import za.co.absa.openbankingapi.woolworths.integration.dto.Header
import za.co.absa.openbankingapi.woolworths.integration.dto.SecurityNotificationType

data class ValidateSureCheckRequestProperty(
     val securityNotificationType: SecurityNotificationType? = SecurityNotificationType.SureCheck,
     val otpToBeVerified: String?,
     val header: Header = Header()
)

data class ValidateSureCheckResponseProperty(
     val header: Header = Header(),
     val result: String,
     val securityNotificationType: SecurityNotificationType? ,
     val cellNumber: String,
     val resendsRemaining: Int,
     val otpRetriesLeft: Int
)


