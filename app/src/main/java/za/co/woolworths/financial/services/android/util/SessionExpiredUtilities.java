package za.co.woolworths.financial.services.android.util;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity;

public enum SessionExpiredUtilities {
	INSTANCE;

	public static final String ACCOUNT = "ACCOUNT";
	public static final String REWARD = "REWARD";
	public static final String PRODUCT = "PRODUCT";


	public void setAccountSessionExpired(Activity activity, String token) {
		onSessionExpired(activity, token);
		getGlobalState().setSection(ACCOUNT);
		openBottomNavigation(activity);
		Utils.sendBus(new SessionManager(SessionManager.ACCOUNT_SESSION_EXPIRED));
	}

	private void openBottomNavigation(Activity activity) {
		Intent i = new Intent(activity, BottomNavigationActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		activity.startActivity(i);
		activity.overridePendingTransition(0, 0);
	}

	public void setWRewardSessionExpired(Activity activity, String token) {
		onSessionExpired(activity, token);
		getGlobalState().setSection(REWARD);
		openBottomNavigation(activity);
		Utils.sendBus(new SessionManager(SessionManager.REWARD_SESSION_EXPIRED));
	}

	public void setProductExpired(Activity activity, String token) {
		onSessionExpired(activity, token);
		getGlobalState().setSection(PRODUCT);
		openBottomNavigation(activity);
		Utils.sendBus(new SessionManager(SessionManager.PRODUCT_SESSION_EXPIRED));
	}

	public void showSessionExpireDialog(Activity activity) {
		Intent openMsg = new Intent(activity, CustomPopUpWindow.class);
		Bundle args = new Bundle();
		args.putSerializable("key", CustomPopUpWindow.MODAL_LAYOUT.SESSION_EXPIRED);
		args.putString("description", getGlobalState(activity).getNewSTSParams());
		openMsg.putExtras(args);
		activity.startActivity(openMsg);
		activity.overridePendingTransition(0, 0);
	}

	private void onSessionExpired(Activity activity, String stsParams) {
		getGlobalState(activity).setNewSTSParams(stsParams);
	}

	public WGlobalState getGlobalState(Activity activity) {
		return ((WoolworthsApplication) activity.getApplication()).getWGlobalState();
	}

	private WGlobalState getGlobalState() {
		WoolworthsApplication woolworthsApplication = WoolworthsApplication.getInstance();
		if (woolworthsApplication != null) {
			return woolworthsApplication.getWGlobalState();
		}
		return null;
	}
}
