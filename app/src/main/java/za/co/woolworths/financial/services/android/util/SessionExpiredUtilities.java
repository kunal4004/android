package za.co.woolworths.financial.services.android.util;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import za.co.woolworths.financial.services.android.ui.views.actionsheet.SessionExpiredDialogFragment;

public class SessionExpiredUtilities {

	private static SessionExpiredUtilities instance;
	private String TAG = this.getClass().getSimpleName();

	public static synchronized SessionExpiredUtilities getInstance() {
		if (instance == null) {
			instance = new SessionExpiredUtilities();
		}
		return instance;
	}

	public void showSessionExpireDialog(AppCompatActivity activity) {
		try {
			FragmentManager fm = activity.getSupportFragmentManager();
			SessionExpiredDialogFragment sessionExpiredDialogFragment = SessionExpiredDialogFragment.newInstance(SessionUtilities.getInstance().getSTSParameters());
			sessionExpiredDialogFragment.show(fm, SessionExpiredDialogFragment.class.getSimpleName());
		} catch (NullPointerException ex) {
			Log.d(TAG, ex.getMessage());
		}
	}
}