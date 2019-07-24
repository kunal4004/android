package za.co.woolworths.financial.services.android.util;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import za.co.woolworths.financial.services.android.ui.views.actionsheet.SessionExpiredDialogFragment;

import static za.co.woolworths.financial.services.android.ui.views.actionsheet.ActionSheetDialogFragment.DIALOG_REQUEST_CODE;

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
		} catch (Exception ex) {
			Log.d(TAG, ex.getMessage());
		}
	}

	public void showSessionExpireDialog(AppCompatActivity activity, Fragment fragment) {
		try {
			Utils.clearCacheHistory(activity);
			FragmentManager fm = activity.getSupportFragmentManager();
			SessionExpiredDialogFragment sessionExpiredDialogFragment = SessionExpiredDialogFragment.newInstance(SessionUtilities.getInstance().getSTSParameters());
			sessionExpiredDialogFragment.setTargetFragment(fragment, DIALOG_REQUEST_CODE);
			sessionExpiredDialogFragment.show(fm, SessionExpiredDialogFragment.class.getSimpleName());
		} catch (NullPointerException ex) {
			Log.d(TAG, ex.getMessage());
		}
	}
}