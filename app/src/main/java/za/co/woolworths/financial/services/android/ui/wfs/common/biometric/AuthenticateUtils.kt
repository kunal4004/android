package za.co.woolworths.financial.services.android.ui.wfs.common.biometric

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dao.SessionDao.BIOMETRIC_AUTHENTICATION_STATE
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager

class AuthenticateUtils  {
    companion object {
         fun isDeviceSecure(context: Context): Boolean {
            val keyguardManager =
                context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            return keyguardManager.isKeyguardSecure
        }


         fun startAuthenticateApp(activity: Activity, requestCode: Int) {
            val keyguardManager = activity.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            val i = keyguardManager.createConfirmDeviceCredentialIntent(
                activity.getString(R.string.enter_password), ""
            )
            try {
                //Start activity for result
                activity.startActivityForResult(i, requestCode)
            } catch (e: Exception) {
                FirebaseManager.logException(e)
            }
        }

        fun isAuthenticationEnabled(): Boolean {
            if (!SessionUtilities.getInstance().isUserAuthenticated) {
                return false
            }

            val currentUserObject: AppInstanceObject.User? = AppInstanceObject.get().currentUserObject
            val authenticationState = currentUserObject?.biometricAuthenticationState ?: BIOMETRIC_AUTHENTICATION_STATE.OFF

            return authenticationState == BIOMETRIC_AUTHENTICATION_STATE.ON
        }

        fun setUserAuthenticate(state: BIOMETRIC_AUTHENTICATION_STATE?) {
            val currentUserObject = AppInstanceObject.get().currentUserObject
            currentUserObject.biometricAuthenticationState = state
            currentUserObject.save()
        }

        fun enableBiometricForCurrentSession(isEnabled: Boolean) {
            try {
                Utils.sessionDaoSave(
                    SessionDao.KEY.BIOMETRIC_AUTHENTICATION_SESSION,
                    if (isEnabled) "1" else "0"
                )
            } catch (e: Exception) {
                FirebaseManager.logException(e)
            }
        }

        private fun isBiometricsEnabledForCurrentSession(): Boolean {
            val result = Utils.getSessionDaoValue(SessionDao.KEY.BIOMETRIC_AUTHENTICATION_SESSION)
            return result?.equals("1", ignoreCase = true) ?: true
        }

        fun isBiometricAuthenticationAvailable(context: Context): Boolean {
            return SessionUtilities.getInstance().isUserAuthenticated
                    && isBiometricsEnabledForCurrentSession()
                    && isAuthenticationEnabled()
                    && isDeviceSecure(context)
        }

        fun isBiometricAuthenticationSupported(context: Context): Boolean {
            return SessionUtilities.getInstance().isUserAuthenticated
                    && isAuthenticationEnabled()
                    && isDeviceSecure(context)
        }
    }
}