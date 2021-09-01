package za.co.woolworths.financial.services.android.ui.fragments.wreward.brightness

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import za.co.woolworths.financial.services.android.ui.fragments.wreward.brightness.contract.ShakeDetectorInterface
import androidx.lifecycle.OnLifecycleEvent
import za.co.woolworths.financial.services.android.ui.fragments.wreward.brightness.shake_detector.ShakeDetector
import android.app.Activity
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.awfs.coordination.R

class ShakeDetectorImpl(private val fragment: Fragment?) : LifecycleObserver, ShakeDetectorInterface {

    private var mAccelerometer: Sensor? = null
    private var mSensorManager: SensorManager? = null
    private var mShakeDetector: ShakeDetector? = null

    override fun registerLifeCycle(lifecycle: Lifecycle?) {
        lifecycle?.addObserver(this)
    }

    override fun shakeDetectorInit(onShakeListener: (Int) -> Unit) {
        fragment?.activity?.lifecycle?.addObserver(this)
        mSensorManager = fragment?.activity?.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        mAccelerometer = mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mShakeDetector = ShakeDetector { count -> onShakeListener(count) }
    }

    override fun setShakeToAnimateView(activity: Activity?, view: View?) {
        val shake: Animation = AnimationUtils.loadAnimation(activity, R.anim.shake_anim)
        view?.startAnimation(shake)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    override fun onRegisterShake() {
        mSensorManager?.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    override fun onUnRegisterShake() {
        mSensorManager?.unregisterListener(mShakeDetector)
    }
}