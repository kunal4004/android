package za.co.woolworths.financial.services.android.ui.wfs.common.state

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner


enum class LifecycleTransitionType {
    FOREGROUND, BACKGROUND, BACKGROUND_TO_FOREGROUND, BIOMETRIC_AUTHENTICATION_WAS_ACTIVE
}

class AppLifeCycleObserver(private val biometricSingleton: BiometricSingleton?, private val listener: (LifecycleTransitionType) -> Unit) : DefaultLifecycleObserver {

     private var isApplicationInForeground : Boolean = false
     private var previousState: LifecycleTransitionType? = null

    override fun onStart(owner: LifecycleOwner) {
        if (!isApplicationInForeground) {
            if (previousState == LifecycleTransitionType.BACKGROUND
                && biometricSingleton?.isCurrentActivityBottomNavigationActivity() == true
                && !biometricSingleton.wasBiometricAuthenticationScreenActive) {
                listener(LifecycleTransitionType.BACKGROUND_TO_FOREGROUND)
            } else {
                listener(LifecycleTransitionType.FOREGROUND)
                previousState = LifecycleTransitionType.FOREGROUND
            }
        }
        isApplicationInForeground = true
        super.onStart(owner)
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        when (biometricSingleton?.currentScreenType) {
             CurrentScreenType.BOTTOM_NAVIGATION_ACTIVITY -> {
                 previousState = LifecycleTransitionType.BACKGROUND
            }
            else -> Unit
        }
        isApplicationInForeground = false
    }
}

