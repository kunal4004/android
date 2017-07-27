package za.co.woolworths.financial.services.android.util;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpDialogManager;
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity;
import za.co.woolworths.financial.services.android.ui.activities.WOneAppBaseActivity;

public enum SessionExpiredUtilities {
	INSTANCE;

	public void setAccountSessionExpired(Activity activity, String token) {
		onSessionExpired(activity, token);
		Intent i = new Intent(activity, WOneAppBaseActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		activity.setResult(SSOActivity.SSOActivityResult.EXPIRED.rawValue(), i);
		activity.startActivity(i);
		activity.overridePendingTransition(0, 0);
	}

	public void setWRewardSessionExpired(Activity activity, String token) {
		onSessionExpired(activity, token);
	}

	public void showSessionExpireDialog(Activity activity) {
		getGlobalState(activity).setAccountHasExpired(true);
		Intent openMsg = new Intent(activity, CustomPopUpDialogManager.class);
		Bundle args = new Bundle();
		args.putSerializable("key", CustomPopUpDialogManager.VALIDATION_MESSAGE_LIST.SESSION_EXPIRED);
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

}
