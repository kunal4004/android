package za.co.woolworths.financial.services.android.ui.wfs.common.biometric

import android.annotation.TargetApi
import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.biometric.BiometricManager
import androidx.fragment.app.FragmentActivity


object BiometricUtils {

    fun Context.isBiometricHardWareAvailable(): Boolean {
        val biometricManager = BiometricManager.from(this)
        val isBiometricHardWareAvailable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            when (biometricManager.canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL or BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
                BiometricManager.BIOMETRIC_SUCCESS -> true
                BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> true
                BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> true
                else -> false
            }
        } else {
            when (biometricManager.canAuthenticate()) {
                BiometricManager.BIOMETRIC_SUCCESS -> true
                else -> false
            }
        }
        return isBiometricHardWareAvailable
    }

    fun Context.deviceHasPasswordPinLock(): Boolean {
        val km = getSystemService(FragmentActivity.KEYGUARD_SERVICE) as KeyguardManager
        if (km.isKeyguardSecure)
            return true
        return false
    }

    fun Context.isDeviceScreenLocked(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            isDeviceLocked()
        } else {
            isPatternSet() || isPassOrPinSet()
        }
    }

    /**
     * @return true if pattern set, false if not (or if an issue when checking)
     */
    private fun Context.isPatternSet(): Boolean {
        return try {
            val lockPatternEnable: Int =
                Settings.Secure.getInt(contentResolver, Settings.Secure.LOCK_PATTERN_ENABLED)
            lockPatternEnable == 1
        } catch (e: Settings.SettingNotFoundException) {
            false
        }
    }

    /**
     * @return true if pass or pin set
     */
    private fun Context.isPassOrPinSet(): Boolean {
        val keyguardManager =
            getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager //api 16+
        return keyguardManager.isKeyguardSecure
    }

    /**
     * @return true if pass or pin or pattern locks screen
     */
    @TargetApi(23)
    private fun Context.isDeviceLocked(): Boolean {
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager //api 23+
        return keyguardManager.isDeviceSecure
    }

}