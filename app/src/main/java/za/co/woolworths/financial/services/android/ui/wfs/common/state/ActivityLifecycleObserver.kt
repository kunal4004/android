package za.co.woolworths.financial.services.android.ui.wfs.common.state

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

enum class LifecycleTransitionType {
    FOREGROUND, BACKGROUND, BACKGROUND_TO_FOREGROUND
}

class ActivityLifecycleObserver(private val listener: (LifecycleTransitionType) -> Unit) : DefaultLifecycleObserver {

     private var isApplicationInForeground = false
     private var isScreenInForeground = false
     private var previousState: LifecycleTransitionType? = null


    override fun onStart(owner: LifecycleOwner) {
        isScreenInForeground = true
        if (!isApplicationInForeground) {
            isApplicationInForeground = true
            if (previousState == LifecycleTransitionType.BACKGROUND) {
                listener(LifecycleTransitionType.BACKGROUND_TO_FOREGROUND)
            } else {
                listener(LifecycleTransitionType.FOREGROUND)
            }
            previousState =  LifecycleTransitionType.FOREGROUND
        }

        super.onStart(owner)
    }

    override fun onStop(owner: LifecycleOwner) {
        isScreenInForeground = false
        previousState = LifecycleTransitionType.BACKGROUND
        isApplicationInForeground = false

        super.onStop(owner)
    }

}
