package za.co.woolworths.financial.services.android.common.notificationpermission

import android.Manifest
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import javax.inject.Inject

class NotificationPermission @Inject constructor(registry: ActivityResultRegistry) {

    private val getPermission = registry.register(REGISTRY_KEY,
        ActivityResultContracts.RequestPermission()) {
        // Do Nothing
    }

    fun launchNotificationPermission() {
        getPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    companion object {
        private const val REGISTRY_KEY = "Ask Notification Permission"
    }

}