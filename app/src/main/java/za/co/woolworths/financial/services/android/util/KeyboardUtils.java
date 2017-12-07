package za.co.woolworths.financial.services.android.util;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import za.co.woolworths.financial.services.android.ui.views.WEditTextView;

public class KeyboardUtils {

	private Context mContext;

	public KeyboardUtils(Context context) {
		this.mContext = context;
	}

	public void showKeyboard(WEditTextView editTextView) {
		Activity activity = (Activity) mContext;
		if (activity != null) {
			InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
			assert imm != null;
			imm.showSoftInput(editTextView, InputMethodManager.SHOW_IMPLICIT);
			activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
		}
	}

	public void hideKeyboard() {
		if (mContext != null) {
			Activity activity = (Activity) mContext;
			if (activity != null) {
				View view = activity.getCurrentFocus();
				if (view != null) {
					InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
					assert imm != null;
					imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
				}
			}
		}
	}
}
