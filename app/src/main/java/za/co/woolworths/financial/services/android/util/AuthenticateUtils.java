package za.co.woolworths.financial.services.android.util;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.ui.activities.OnBoardingActivity;
import za.co.woolworths.financial.services.android.ui.activities.splash.WSplashScreenActivity;

import static android.content.Context.KEYGUARD_SERVICE;

/**
 * Created by W7099877 on 2018/05/03.
 */

public class AuthenticateUtils {

	public static AuthenticateUtils instance;
	public static Activity mContext;
	private String TAG = this.getClass().getSimpleName();

	public static AuthenticateUtils getInstance(Activity mActivity) {
		if (instance == null) {
			instance = new AuthenticateUtils();
		}
		mContext = mActivity;
		return instance;
	}

	/**
	 * method to return whether device has screen lock enabled or not
	 **/
	public boolean isDeviceSecure() {

		KeyguardManager keyguardManager = (KeyguardManager) mContext.getSystemService(KEYGUARD_SERVICE);
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && keyguardManager.isKeyguardSecure();

	}

	//method to authenticate app
	public void startAuthenticateApp(int requestCode) throws Exception {
		KeyguardManager keyguardManager = (KeyguardManager) mContext.getSystemService(KEYGUARD_SERVICE);

		//Check if the device version is greater than or equal to Lollipop(21)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Intent i = keyguardManager.createConfirmDeviceCredentialIntent("", "");
			try {
				//Start activity for result
				mContext.startActivityForResult(i, requestCode);
			} catch (Exception e) {
				throw e;
			}
		}
	}

	public boolean isAuthenticationEnabled() {
		SessionDao sessionDao = SessionDao.getByKey(SessionDao.KEY.BIOMETRIC_AUTHENTICATION_STATE);
		SessionDao.BIOMETRIC_AUTHENTICATION_STATE authenticationState;

		if (sessionDao.value == null) {
			authenticationState = SessionDao.BIOMETRIC_AUTHENTICATION_STATE.OFF;
		} else {
			authenticationState = SessionDao.BIOMETRIC_AUTHENTICATION_STATE.valueOf(sessionDao.value);
		}
		// check isDeviceSecure()
		return authenticationState.equals(SessionDao.BIOMETRIC_AUTHENTICATION_STATE.ON) ;
	}

	public void setUserAuthenticate(SessionDao.BIOMETRIC_AUTHENTICATION_STATE state) {
		SessionDao sessionDao = SessionDao.getByKey(SessionDao.KEY.BIOMETRIC_AUTHENTICATION_STATE);
		sessionDao.value = state.toString();
		try {
			sessionDao.save();
		} catch (Exception e) {

		}
	}

	public void enableBiometricForCurrentSession(boolean value){
		try {
			Utils.sessionDaoSave(mContext, SessionDao.KEY.BIOMETRIC_AUTHENTICATION_SESSION, value ? "1" : "0");
		} catch (Exception e) {
			Log.i(TAG, e.getMessage());
		}
	}

	public boolean isBiometricsEnabledForCurrentSession(){
		String result = Utils.getSessionDaoValue(mContext, SessionDao.KEY.BIOMETRIC_AUTHENTICATION_SESSION);
		boolean isEnabled;
		if (result == null)
			isEnabled = true;
		else
			isEnabled = result.equalsIgnoreCase("1") ? true : false;
		return isEnabled;
	}

	public boolean isBiometricAuthenticationRequired(){
		return isAuthenticationEnabled() && isBiometricsEnabledForCurrentSession() && isDeviceSecure();
	}

	public boolean isAppSupportsAuthentication(){
		return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) ;
	}
}
