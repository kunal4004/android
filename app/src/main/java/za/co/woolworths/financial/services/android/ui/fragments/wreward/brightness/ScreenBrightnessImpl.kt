package za.co.woolworths.financial.services.android.ui.fragments.wreward.brightness

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.ui.fragments.wreward.brightness.contract.ScreenBrightnessInterface
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager.Companion.logException
import java.lang.Exception
import kotlin.math.roundToInt

class ScreenBrightnessImpl : ScreenBrightnessInterface {

    private var mContentResolver: ContentResolver? = null

    companion object {
        const val MAXIMUM_BRIGHTNESS = 255 // 0 - 255
        const val HUNDRED_PERCENT_VALUE = 100
    }

    private var mContentObserver: ContentObserver? = null
    var appContext: Context? = null

    init {
        appContext = appContext()
    }

    override fun isSettingPermissionAllowedForOneApp(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.System.canWrite(appContext())
        } else {
            true
        }
    }

    override fun setScreenBrightness(percent: Int) {
        try {
            val brightnessLevel = convertBrightnessLevelFromPercent(percent)
            Settings.System.putInt(
                appContext?.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                brightnessLevel
            )
            appContext?.sendBroadcast(Intent("BrightnessReceiver"))
        } catch (ex: Exception) {
            logException(ex)
        }
    }

    override fun setBrightnessModeManual() {
        try {
            if (!isSettingPermissionAllowedForOneApp())
                return

                Settings.System.putInt(
                    appContext?.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
                )

        } catch (ex: Exception) {
            logException(ex)
        }
    }

    override fun convertBrightnessLevelFromPercent(percent: Int): Int {
        return (percent.times(MAXIMUM_BRIGHTNESS)).div(HUNDRED_PERCENT_VALUE).toFloat().roundToInt()
    }

    override fun convertBrightnessLevelToPercent(brightnessLevel: Int): Int {
        return brightnessLevel.times(HUNDRED_PERCENT_VALUE).div(MAXIMUM_BRIGHTNESS).toFloat()
            .roundToInt()
    }

    override fun getScreenBrightness(): Int {
        return try {
            Settings.System.getInt(appContext?.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
        } catch (ex: Exception) {
            0
        }
    }

    // register the brightness listener upon starting
    override fun registerContentObserverForBrightness() {
        mContentObserver?.let {
            WoolworthsApplication.getAppContext()?.contentResolver?.registerContentObserver(
                Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS), false, it
            )
        }
    }

    // unregister the listener when we're done (e.g. activity/fragment destroyed)
    override fun unregisterContentObserverForBrightness() {
        mContentObserver?.let { mContentResolver?.unregisterContentObserver(it) }

    }

    override fun initBrightnessChangeListener(onBrightnessChangeResult: (Int) -> Unit) {
        setBrightnessModeManual()
        mContentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                // get system brightness level
                val brightnessAmount = getScreenBrightness()
                onBrightnessChangeResult(brightnessAmount)
            }
        }
    }

    override fun appContext(): Context? {
        return WoolworthsApplication.getAppContext()
    }
}