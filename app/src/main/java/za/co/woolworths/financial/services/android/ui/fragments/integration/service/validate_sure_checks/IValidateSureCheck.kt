package za.co.woolworths.financial.services.android.ui.fragments.integration.service.validate_sure_checks

import za.co.absa.openbankingapi.woolworths.integration.dto.SecurityNotificationType
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.model.AbsaProxyResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.NetworkState
import java.util.concurrent.ScheduledFuture

interface IValidateSureCheck {

     val POLLING_INTERVAL: Long
     var pollingCount : Int

    fun schedulePollingWithFixedDelay(func : () -> Unit): ScheduledFuture<*>?
    fun stopPolling(scheduledFuture: ScheduledFuture<*>?)
    fun createAbsaValidateSureCheckRequestProperty(securityNotificationType: SecurityNotificationType, otpToBeVerified: String?): ValidateSureCheckRequestProperty
    suspend fun fetchAbsaValidateSureCheck(securityNotificationType: SecurityNotificationType?, otpToBeVerified: String? =null): NetworkState<AbsaProxyResponseProperty>
}