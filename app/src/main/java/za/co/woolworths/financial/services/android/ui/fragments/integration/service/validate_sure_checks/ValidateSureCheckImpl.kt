package za.co.woolworths.financial.services.android.ui.fragments.integration.service.validate_sure_checks

import za.co.absa.openbankingapi.woolworths.integration.dto.SecurityNotificationType
import za.co.woolworths.financial.services.android.ui.extension.json
import za.co.woolworths.financial.services.android.ui.fragments.integration.helper.AbsaTemporaryDataSourceSingleton
import za.co.woolworths.financial.services.android.ui.fragments.integration.remote.AbsaRemoteApi
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.model.AbsaProxyResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class ValidateSureCheckImpl : IValidateSureCheck {

    override val POLLING_INTERVAL: Long
        get() = 10

    override var pollingCount: Int = 0

    override fun schedulePollingWithFixedDelay(func: () -> Unit): ScheduledFuture<*>? {
        return Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay({
            func()
        }, 0, POLLING_INTERVAL, TimeUnit.SECONDS)
    }

    override fun stopPolling(scheduledFuture: ScheduledFuture<*>?) {
        scheduledFuture?.apply {
            if (!isCancelled) {
                cancel(true)
            }
        }
    }

    override fun createAbsaValidateSureCheckRequestProperty(securityNotificationType: SecurityNotificationType, otpToBeVerified: String?): ValidateSureCheckRequestProperty {
        return ValidateSureCheckRequestProperty(securityNotificationType, otpToBeVerified)
    }

    override suspend fun fetchAbsaValidateSureCheck(securityNotificationType: SecurityNotificationType?): NetworkState<AbsaProxyResponseProperty> {
        val validateCardAndPinRequestProperty = createAbsaValidateSureCheckRequestProperty(
            securityNotificationType ?: SecurityNotificationType.SureCheck, null).json()
        validateCardAndPinRequestProperty.contentLength()
        val withEncryptedBody = validateCardAndPinRequestProperty.toAes256Encrypt()
        return resultOf(AbsaRemoteApi.service.queryAbsaServiceValidateSureCheck( AbsaTemporaryDataSourceSingleton.cookie,withEncryptedBody))
    }

    override suspend fun fetchAbsaValidateSureCheckOTP(
        securityNotificationType: SecurityNotificationType,
        otpToBeVerified: String?
    ): NetworkState<AbsaProxyResponseProperty> {
        val validateCardAndPinRequestProperty = createAbsaValidateSureCheckRequestProperty(
            securityNotificationType,
            otpToBeVerified ?: "null").json()
        validateCardAndPinRequestProperty.contentLength()
        val withEncryptedBody = validateCardAndPinRequestProperty.toAes256Encrypt()
        return resultOf(AbsaRemoteApi.service.queryAbsaServiceValidateSureCheck( AbsaTemporaryDataSourceSingleton.cookie,withEncryptedBody))
    }
}