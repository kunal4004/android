package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_device_security.data

import com.google.firebase.installations.FirebaseInstallations
import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.repository.AppStateRepository
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_device_security.navigation.DeviceSecurityActivityResult
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_device_security.navigation.IDeviceSecurityActivityResult
import za.co.woolworths.financial.services.android.util.PlatformAvailabilityCheckerImpl
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager.Companion.logException
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

interface DeviceSecurity {
    fun isLinkDeviceScreenNavigationNeeded(): Boolean
    fun isLinkConfirmationScreenShown(): Boolean
    fun isAppInstanceUnlinked(): Boolean
    fun getDeviceId(): String?
}

class DeviceSecurityImpl @Inject constructor(
    private val platform: PlatformAvailabilityCheckerImpl,
    private val activityResult : DeviceSecurityActivityResult
) : DeviceSecurity,
    IDeviceSecurityActivityResult by activityResult {

    override fun isLinkDeviceScreenNavigationNeeded(): Boolean {
        return !isLinkConfirmationScreenShown() &&
                isAppInstanceUnlinked() &&
                platform.isGooglePlayOrHuaweiMobileServicesAvailable()
    }

    override fun isLinkConfirmationScreenShown(): Boolean {
        val currentUserObject = AppInstanceObject.get().currentUserObject
        return currentUserObject.isLinkConfirmationScreenShown
    }

    /**
     * Checks if the current app instance is linked to any device.
     * @return `true` if the current app instance is not linked to any device, `false` otherwise.
     */
    override fun isAppInstanceUnlinked(): Boolean {
        val linkedDevices = AppStateRepository().getLinkedDevices() ?: arrayOf()
        val currentDeviceId = getDeviceId()
        return linkedDevices.none { it.appInstanceId == currentDeviceId }
    }

    /**
     * Gets a unique identifier for the current device, or null if the operation fails.
     * @return A unique device ID if available, or null if the ID could not be retrieved.
     */
    override fun getDeviceId(): String? {
        val cachedDeviceId = Utils.getSessionDaoValue(SessionDao.KEY.DEVICE_ID)
        if (cachedDeviceId != null) {
            // Use the cached device ID if available.
            return cachedDeviceId
        }
        // Otherwise, try to retrieve the device ID from Firebase Installations.
        val deviceIdRef = AtomicReference<String?>()
        FirebaseInstallations.getInstance().id.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                deviceIdRef.set(task.result)
                Utils.sessionDaoSave(SessionDao.KEY.DEVICE_ID, deviceIdRef.get())
            } else {
                logException(
                    "Failed to retrieve device ID from Firebase Installations ${task.exception}"
                )
            }
        }
        return deviceIdRef.get()
    }


}