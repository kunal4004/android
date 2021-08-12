package za.co.woolworths.financial.services.android.util;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import za.co.woolworths.financial.services.android.models.JWTDecodedModel;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity;

/**
 * Created by eesajacobs on 2018/04/04.
 */

public class SessionUtilities {
	private final String TAG = "SessionUtilities";

	private static SessionUtilities instance;
	private String bottomNavigationPosition;

	public static SessionUtilities getInstance() {
		if (instance == null) {
			instance = new SessionUtilities();
		}
		return instance;
	}

	public boolean isUserAuthenticated() {
		//check a stored value to validate whether the
		//user is authenticated or not.
		//This flag/detail needs to be set post login

		boolean isUserAuthenticated = false;

		SessionDao sessionDao = SessionDao.getByKey(SessionDao.KEY.SESSION_STATE);
		SessionDao.SESSION_STATE sessionState;
		if (sessionDao.value == null) {
			sessionState = SessionDao.SESSION_STATE.INACTIVE;
		} else
			sessionState = SessionDao.SESSION_STATE.valueOf(sessionDao.value);

		return sessionState.equals(SessionDao.SESSION_STATE.ACTIVE);
	}

	public boolean isC2User() {
		//use this check to help
		//identify that this user is linked
		//to WFS product(s) or not i.e.
		//WFS + Online User or just Online User.

		JWTDecodedModel jwt = this.getJwt();
		return (jwt.C2Id == null ? false : !jwt.C2Id.isEmpty());
	}

	public JWTDecodedModel getJwt() {
		String sessionToken = getSessionToken();

		if (!sessionToken.isEmpty()) {
			return JWTHelper.decode(sessionToken);
		} else {
			return new JWTDecodedModel();
		}
	}

	public String getSessionToken() {
		SessionDao sessionDao = SessionDao.getByKey(SessionDao.KEY.USER_TOKEN);
		return sessionDao.value == null ? "" : sessionDao.value;
	}

	public void setDeviceIdentityToken(String deviceIdentityToken) {
		SessionDao sessionDao = SessionDao.getByKey(SessionDao.KEY.DEVICE_IDENTITY_TOKEN);
		sessionDao.value = deviceIdentityToken;
		try {
			sessionDao.save();
		} catch (Exception e) {
			Log.e(SessionDao.KEY.DEVICE_IDENTITY_TOKEN.toString(), e.getMessage());
		}
	}

	public String getDeviceIdentityToken() {
		SessionDao sessionDao = SessionDao.getByKey(SessionDao.KEY.DEVICE_IDENTITY_TOKEN);
		return sessionDao.value == null ? "" : sessionDao.value;
	}

	public void setSessionState(SessionDao.SESSION_STATE state) {
		setSessionState(state, null);
	}


	public void setSessionState(SessionDao.SESSION_STATE state, String stsParams) {
		SessionDao sessionDao = SessionDao.getByKey(SessionDao.KEY.SESSION_STATE);
		sessionDao.value = state.toString();

		try {
			sessionDao.save();
		} catch (Exception e) {
			//Analytics.logEvent("setSessionState"...)
		}

		sessionDao = SessionDao.getByKey(SessionDao.KEY.STS_PARAMS);
		try {
			sessionDao.value = decodeSTSParams(stsParams);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		try {
			sessionDao.save();
		} catch (Exception e) {
			//Analytics.logEvent("setSessionState Params"...)
		}
	}


	public void setSessionState(SessionDao.SESSION_STATE state, String stsParams, Activity activity) {
		SessionDao sessionDao = SessionDao.getByKey(SessionDao.KEY.SESSION_STATE);
		sessionDao.value = state.toString();

		try {
			sessionDao.save();
		} catch (Exception e) {
			//Analytics.logEvent("setSessionState"...)
		}

		sessionDao = SessionDao.getByKey(SessionDao.KEY.STS_PARAMS);
		try {
			sessionDao.value = decodeSTSParams(stsParams);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		try {
			sessionDao.save();
		} catch (Exception e) {
			//Analytics.logEvent("setSessionState Params"...)
		}

		if (activity == null) return;
		// clear all activity stack until bottomNavigationActivity is reached
		Intent intNavigateToBottomActivity = new Intent(activity, BottomNavigationActivity.class);
		intNavigateToBottomActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intNavigateToBottomActivity.putExtra("sessionExpiredAtTabSection", String.valueOf(getBottomNavigationPosition()));
		activity.startActivity(intNavigateToBottomActivity);
		activity.overridePendingTransition(0, 0);
	}

	public String getSTSParameters() {
		SessionDao sessionDao = SessionDao.getByKey(SessionDao.KEY.STS_PARAMS);
		return sessionDao.value;
	}

	public void setSTSParameters(String stsParameters) {
		//retain the session state and only set the
		//sts params to null.
		try {
			stsParameters = decodeSTSParams(stsParameters);
		} catch (UnsupportedEncodingException e) {
		}
		SessionDao.SESSION_STATE sessionState = (isUserAuthenticated() ? SessionDao.SESSION_STATE.ACTIVE : SessionDao.SESSION_STATE.INACTIVE);
		setSessionState(sessionState, stsParameters);
	}

	private String decodeSTSParams(String stsParams) throws UnsupportedEncodingException {
		if (stsParams == null) return "";
		String decodeSTSParams = URLDecoder.decode(stsParams, "UTF-8");
		String removeScope = decodeSTSParams.replace("scope=", "");
		return removeScope;
	}

	public void setBottomNavigationPosition(String index) {
		this.bottomNavigationPosition = index;
	}

	public String getBottomNavigationPosition() {
		return bottomNavigationPosition;
	}
}
