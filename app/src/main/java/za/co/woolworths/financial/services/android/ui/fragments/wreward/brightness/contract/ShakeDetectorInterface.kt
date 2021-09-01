package za.co.woolworths.financial.services.android.ui.fragments.wreward.brightness.contract

import android.app.Activity
import android.view.View
import androidx.lifecycle.Lifecycle

interface ShakeDetectorInterface {
    fun registerLifeCycle(lifecycle : Lifecycle?)
    fun shakeDetectorInit(onShakeListener:(Int) -> Unit)
    fun onRegisterShake()
    fun onUnRegisterShake()
    fun setShakeToAnimateView(activity: Activity?, view: View?)
}