package za.co.woolworths.financial.services.android.ui.fragments.wreward.brightness

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.ui.fragments.wreward.brightness.contract.ScreenBrightnessInterface
import za.co.woolworths.financial.services.android.util.FirebaseManager.Companion.logException
import java.lang.Exception
import kotlin.math.roundToInt

class ScreenBrightnessImpl : ScreenBrightnessInterface {

    companion object {
        const val MAXIMUM_BRIGHTNESS = 255 // 0 - 255
        const val HUNDRED_PERCENT_VALUE = 100
    }
    var appContext: Context? = null

    init {
        appContext = appContext()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun isSettingPermissionAllowedForOneApp(): Boolean {
        return Settings.System.canWrite(appContext())
    }

    override fun setScreenBrightness(percent: Int) {
        try {
            val brightnessLevel = convertBrightnessLevelFromPercent(percent)
            Settings.System.putInt(appContext?.contentResolver, Settings.System.SCREEN_BRIGHTNESS, brightnessLevel)
            appContext?.sendBroadcast(Intent("BrightnessReceiver"))
        } catch (ex: Exception) {
            logException(ex)
        }
    }

    override fun setBrightnessModeManual() {
        try {
            Settings.System.putInt(appContext?.contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
        } catch (ex: Exception) {
            logException(ex)
        }
    }

    override fun convertBrightnessLevelFromPercent(percent : Int): Int {
        return (percent.times(MAXIMUM_BRIGHTNESS)).div(HUNDRED_PERCENT_VALUE).toFloat().roundToInt()
    }

    override fun convertBrightnessLevelToPercent(brightnessLevel: Int): Int {
        return brightnessLevel.times(HUNDRED_PERCENT_VALUE).div(MAXIMUM_BRIGHTNESS).toFloat().roundToInt()
    }

    override fun getScreenBrightness(): Int {
        return try {
            Settings.System.getInt(appContext?.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
        } catch (ex: Exception) {
            0
        }
    }

    override fun appContext(): Context? {
        return WoolworthsApplication.getAppContext()
    }
}