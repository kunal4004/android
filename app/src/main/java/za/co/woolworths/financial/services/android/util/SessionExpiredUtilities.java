package za.co.woolworths.financial.services.android.util;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity;

public enum SessionExpiredUtilities {
	INSTANCE;

	public void showSessionExpireDialog(Activity activity) {
		Intent openMsg = new Intent(activity, CustomPopUpWindow.class);
		Bundle args = new Bundle();
		args.putSerializable("key", CustomPopUpWindow.MODAL_LAYOUT.SESSION_EXPIRED);
		args.putString("description", getGlobalState(activity).getNewSTSParams());
		openMsg.putExtras(args);
		activity.startActivity(openMsg);
		activity.overridePendingTransition(0, 0);
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

	private void openBottomNavigation(Activity activity) {
		Intent i = new Intent(activity, BottomNavigationActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		activity.startActivity(i);
		activity.overridePendingTransition(0, 0);
	}
}
