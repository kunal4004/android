package za.co.woolworths.financial.services.android.util;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;

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
			Intent i = keyguardManager.createConfirmDeviceCredentialIntent(mContext.getString(R.string.enter_password), "");
			try {
				//Start activity for result
				mContext.startActivityForResult(i, requestCode);
			} catch (Exception e) {
				throw e;
			}
		}
	}

	public boolean isAuthenticationEnabled() {
		if (!SessionUtilities.getInstance().isUserAuthenticated())
			return false;
		SessionDao.BIOMETRIC_AUTHENTICATION_STATE authenticationState;
		AppInstanceObject.User currentUserObject = AppInstanceObject.get().getCurrentUserObject();
		authenticationState = currentUserObject.biometricAuthenticationState;
		if (authenticationState == null)
			authenticationState = SessionDao.BIOMETRIC_AUTHENTICATION_STATE.OFF;
		return authenticationState.equals(SessionDao.BIOMETRIC_AUTHENTICATION_STATE.ON);
	}

	public void setUserAuthenticate(SessionDao.BIOMETRIC_AUTHENTICATION_STATE state) {
		AppInstanceObject.User currentUserObject = AppInstanceObject.get().getCurrentUserObject();
		currentUserObject.biometricAuthenticationState = state;
		currentUserObject.save();
	}

	public void enableBiometricForCurrentSession(boolean value) {
		try {
			Utils.sessionDaoSave(SessionDao.KEY.BIOMETRIC_AUTHENTICATION_SESSION, value ? "1" : "0");
		} catch (Exception e) {
		}
	}

	public boolean isBiometricsEnabledForCurrentSession() {
		String result = Utils.getSessionDaoValue(SessionDao.KEY.BIOMETRIC_AUTHENTICATION_SESSION);
		boolean isEnabled;
		if (result == null)
			isEnabled = true;
		else
			isEnabled = result.equalsIgnoreCase("1") ? true : false;
		return isEnabled;
	}

	public boolean isBiometricAuthenticationRequired() {
		return isAuthenticationEnabled() && isBiometricsEnabledForCurrentSession() && isDeviceSecure();
	}

	public boolean isAppSupportsAuthentication() {
		return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
	}
}
