package za.co.woolworths.financial.services.android.util;

import za.co.woolworths.financial.services.android.models.JWTDecodedModel;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;

/**
 * Created by eesajacobs on 2018/04/04.
 */

public class SessionUtilities {
	private final String TAG = "SessionUtilities";

	private static SessionUtilities instance;

	public static SessionUtilities getInstance(){
		if(instance == null){
			instance = new SessionUtilities();
		}
		return instance;
	}

	public boolean isUserAuthenticated(){
		//check a stored value to validate whether the
		//user is authenticated or not.
		//This flag/detail needs to be set post login

		boolean isUserAuthenticated = false;

		SessionDao sessionDao = SessionDao.getByKey(SessionDao.KEY.SESSION_STATE);
		SessionDao.SESSION_STATE sessionState;
		if (sessionDao.value == null){
			sessionState = SessionDao.SESSION_STATE.INACTIVE;
		}else
			sessionState = SessionDao.SESSION_STATE.valueOf(sessionDao.value);

		return sessionState.equals(SessionDao.SESSION_STATE.ACTIVE);
	}

	public boolean isC2User(){
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
		}else{
			return new JWTDecodedModel();
		}
	}

	public String getSessionToken() {
		SessionDao sessionDao = SessionDao.getByKey(SessionDao.KEY.USER_TOKEN);
		return sessionDao.value == null ? "" : sessionDao.value;
	}

	public void setSessionState(SessionDao.SESSION_STATE state){
		setSessionState(state, null);
	}

	public void setSessionState(SessionDao.SESSION_STATE state, String stsParams){
		SessionDao sessionDao = SessionDao.getByKey(SessionDao.KEY.SESSION_STATE);
		sessionDao.value = state.toString();

		try {
			sessionDao.save();
		} catch (Exception e) {
			//Analytics.logEvent("setSessionState"...)
		}

		sessionDao = SessionDao.getByKey(SessionDao.KEY.STS_PARAMS);
		sessionDao.value = stsParams;

		try {
			sessionDao.save();
		} catch (Exception e) {
			//Analytics.logEvent("setSessionState Params"...)
		}
	}

	public String getSTSParameters(){
		SessionDao sessionDao = SessionDao.getByKey(SessionDao.KEY.STS_PARAMS);
		return sessionDao.value;
	}
}
