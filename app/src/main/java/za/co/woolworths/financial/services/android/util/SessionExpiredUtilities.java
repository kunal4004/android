package za.co.woolworths.financial.services.android.util;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity;

public enum SessionExpiredUtilities {
	INSTANCE;

	public void showSessionExpireDialog(Activity activity) {
		Utils.clearSQLLiteSearchHistory(activity);
		Intent openMsg = new Intent(activity, CustomPopUpWindow.class);
		Bundle args = new Bundle();
		args.putSerializable("key", CustomPopUpWindow.MODAL_LAYOUT.SESSION_EXPIRED);
		args.putString("description", SessionUtilities.getInstance().getSTSParameters());
		openMsg.putExtras(args);
		activity.startActivityForResult(openMsg, SSOActivity.SSOActivityResult.LAUNCH.rawValue());
		activity.overridePendingTransition(0, 0);
	}
}