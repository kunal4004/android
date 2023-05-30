package za.co.woolworths.financial.services.android.util;

import static za.co.woolworths.financial.services.android.ui.views.actionsheet.ActionSheetDialogFragment.DIALOG_REQUEST_CODE;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import za.co.woolworths.financial.services.android.ui.views.actionsheet.SessionExpiredDialogFragment;
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager;

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
			if(activity == null || fragment == null || !fragment.isAdded()) {
				return;
			}
			Utils.clearCacheHistory();
			FragmentManager fm = activity.getSupportFragmentManager();

			fm.setFragmentResultListener("" + DIALOG_REQUEST_CODE,
					fragment.getViewLifecycleOwner(), (requestKey, result) -> {
						fragment.onActivityResult(DIALOG_REQUEST_CODE, DIALOG_REQUEST_CODE, null);
					});

			SessionExpiredDialogFragment sessionExpiredDialogFragment = SessionExpiredDialogFragment.newInstance(SessionUtilities.getInstance().getSTSParameters());
			sessionExpiredDialogFragment.show(fm, SessionExpiredDialogFragment.class.getSimpleName());
		} catch (NullPointerException | IllegalStateException ex) {
			FirebaseManager.logException(ex);
		}
	}
}