package za.co.woolworths.financial.services.android.util;


import android.content.Context;
import android.text.TextUtils;

import za.co.woolworths.financial.services.android.models.JWTDecodedModel;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;

public class SessionManager {
	private int session;
	private Context mContext;
	public static int ACCOUNT_SESSION_EXPIRED = 1;
	public static int REWARD_SESSION_EXPIRED = 2;
	public static int PRODUCT_SESSION_EXPIRED = 3;
	public static int RELOAD_REWARD = 4;

	public SessionManager() {
	}

	public SessionManager(Context context) {
		mContext = context;
	}

	public SessionManager(int session) {
		this.session = session;
	}

	public boolean authenticationState() {
		if (getJWTDecoded() != null) {
			return getJWTDecoded().AtgSession != null && !accountHasExpired();
		}
		return false;
	}

	public boolean loadSignInView() {
		if (authenticationState()) {
			return getJWTDecoded().C2Id != null && !getJWTDecoded().C2Id.equals("") && !accountHasExpired();
		}
		return false;
	}

	public boolean isC2IdEnabled() {
		if (authenticationState()) {
			return getJWTDecoded().C2Id != null && !getJWTDecoded().C2Id.equals("");
		}
		return false;
	}

	public void setAccountHasExpired(boolean accountHasExpired) {
		setPersistentValue(SessionDao.KEY.ACCOUNT_IS_ACTIVE, accountHasExpired);
	}

	public boolean accountHasExpired() {
		return getPersistentValue(SessionDao.KEY.ACCOUNT_IS_ACTIVE);
	}

	public void setRewardSignInState(boolean state) {
		setPersistentValue(SessionDao.KEY.REWARD_IS_ACTIVE, state);
	}

	public boolean getRewardSignInState() {
		return getPersistentValue(SessionDao.KEY.REWARD_IS_ACTIVE);
	}

	private void setPersistentValue(SessionDao.KEY key, boolean value) {
		Utils.sessionDaoSave(mContext, key, String.valueOf(value));
	}

	private boolean getPersistentValue(SessionDao.KEY key) {
		String value = Utils.getSessionDaoValue(mContext, key);
		if (TextUtils.isEmpty(value)) {
			return false;
		}
		return Boolean.valueOf(value);
	}

	public JWTDecodedModel getJWTDecoded() {
		return Utils.getJWTDecoded(mContext);
	}

	public int getState() {
		return session;
	}

}
