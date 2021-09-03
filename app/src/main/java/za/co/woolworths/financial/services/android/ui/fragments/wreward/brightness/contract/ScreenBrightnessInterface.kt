package za.co.woolworths.financial.services.android.ui.fragments.wreward.brightness.contract

import android.content.Context
interface ScreenBrightnessInterface {
    fun isSettingPermissionAllowedForOneApp(): Boolean
    fun setScreenBrightness(percent: Int)
    fun setBrightnessModeManual()
    fun getScreenBrightness(): Int
    fun appContext(): Context?
    fun convertBrightnessLevelFromPercent(percent: Int): Int
    fun convertBrightnessLevelToPercent(brightnessLevel: Int) : Int
}