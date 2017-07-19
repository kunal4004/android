package za.co.woolworths.financial.services.android.models.dto;

import android.content.Context;
import android.text.TextUtils;

import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.util.Utils;

/**
 * Created by dimitrij on 04/06/2017.
 */

public class WGlobalState {

	private final Context mContext;

	public WGlobalState(Context context) {
		this.mContext = context;
	}

	public static final String EMPTY_FIELD = "";
	public static final String ON_CANCEL = "CANCEL";
	public static final String ON_SIGN_IN = "SIGNIN";

	private boolean cardGestureIsEnabled, onBackPressed, accountHasExpired, rewardHasExpired,
			FragmentIsReward;
	private String pressState, newSTSParams;

	public void setAccountSignInState(boolean accountSignInState) {
		setPersistentValue(SessionDao.KEY.ACCOUNT_IS_ACTIVE, accountSignInState);
	}

	public boolean getAccountSignInState() {
		return getPersistentValue(SessionDao.KEY.ACCOUNT_IS_ACTIVE);
	}

	public void setRewardSignInState(boolean rewardSignInState) {
		setPersistentValue(SessionDao.KEY.REWARD_IS_ACTIVE, rewardSignInState);
	}

	public boolean getRewardSignInState() {
		return getPersistentValue(SessionDao.KEY.REWARD_IS_ACTIVE);
	}

	public boolean getOnBackPressed() {
		return onBackPressed;
	}

	public void setOnBackPressed(boolean pOnBackPressed) {
		onBackPressed = pOnBackPressed;
	}

	public boolean cardGestureIsEnabled() {
		return cardGestureIsEnabled;
	}

	public void setCardGestureIsEnabled(boolean pCardGestureIsEnabled) {
		cardGestureIsEnabled = pCardGestureIsEnabled;
	}

	private void setPersistentValue(SessionDao.KEY key, boolean value) {
		Utils.sessionDaoSave(mContext,
				key, String.valueOf(value));
	}

	private boolean getPersistentValue(SessionDao.KEY key) {
		String value = Utils.getSessionDaoValue(mContext, key);
		if (TextUtils.isEmpty(value)) {
			return false;
		}
		return Boolean.valueOf(value);
	}

	public boolean accountHasExpired() {
		return accountHasExpired;
	}

	public void setAccountHasExpired(boolean pAccountHasExpired) {
		accountHasExpired = pAccountHasExpired;
	}

	public String getPressState() {
		return pressState;
	}

	public void setPressState(String pPressState) {
		pressState = pPressState;
	}

	public boolean rewardHasExpired() {
		return rewardHasExpired;
	}

	public void setRewardHasExpired(boolean pRewardHasExpired) {
		rewardHasExpired = pRewardHasExpired;
	}

	public boolean fragmentIsReward() {
		return FragmentIsReward;
	}

	public void setFragmentIsReward(boolean pFragmentIsReward) {
		FragmentIsReward = pFragmentIsReward;
	}

	public String getNewSTSParams() {
		return newSTSParams;
	}

	public void setNewSTSParams(String pNewSTSParams) {
		newSTSParams = pNewSTSParams;
	}

	public void resetStsParams() {
		setNewSTSParams("");
	}
}