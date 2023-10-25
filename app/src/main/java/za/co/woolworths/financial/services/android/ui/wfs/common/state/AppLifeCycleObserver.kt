package za.co.woolworths.financial.services.android.ui.wfs.common.state

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

enum class LifecycleTransitionType {
    FOREGROUND, BACKGROUND, BACKGROUND_TO_FOREGROUND
}

class AppLifeCycleObserver(private val biometricSingleton: BiometricSingleton?, private val listener: (LifecycleTransitionType) -> Unit) : DefaultLifecycleObserver {

     private var isApplicationInForeground : Boolean = false
     private var previousState: LifecycleTransitionType? = null

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        if (!isApplicationInForeground) {
            isApplicationInForeground = true
            if (previousState == LifecycleTransitionType.BACKGROUND && biometricSingleton?.isCurrentActivityBottomNavigationActivity()==true) {
                listener(LifecycleTransitionType.BACKGROUND_TO_FOREGROUND)
            } else {
                listener(LifecycleTransitionType.FOREGROUND)
            }
            previousState =  LifecycleTransitionType.FOREGROUND
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        when (biometricSingleton?.currentScreenType) {
             CurrentScreenType.BOTTOM_NAVIGATION_ACTIVITY -> {
                previousState = LifecycleTransitionType.BACKGROUND
                isApplicationInForeground = false
            }
            else -> Unit
        }
    }
}

