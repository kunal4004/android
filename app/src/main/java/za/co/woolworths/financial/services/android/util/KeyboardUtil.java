package za.co.woolworths.financial.services.android.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

public class KeyboardUtil {
	private Activity act;
	private int bottomViewHeight;
	private View decorView;
	private View contentView;

	public KeyboardUtil(Activity act, View contentView, int bottomViewHeight) {
		this.act = act;
		this.decorView = act.getWindow().getDecorView();
		this.contentView = contentView;
		this.bottomViewHeight = bottomViewHeight;

		//only required on newer android versions. it was working on API level 19
		if (Build.VERSION.SDK_INT >= 19) {
			decorView.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
		}
	}

	public void enable() {
		if (Build.VERSION.SDK_INT >= 19) {
			decorView.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
		}
	}


	public void enableGlobal() {
		if (Build.VERSION.SDK_INT >= 19) {
			decorView.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);
		}
	}

	public void disable() {
		if (Build.VERSION.SDK_INT >= 19) {
			decorView.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
		}
	}


	//a small helper to allow showing the editText focus
	ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
		@Override
		public void onGlobalLayout() {
			Rect r = new Rect();
			//r will be populated with the coordinates of your view that area still visible.
			decorView.getWindowVisibleDisplayFrame(r);

			//get screen height and calculate the difference with the useable area from the r
			int height = decorView.getContext().getResources().getDisplayMetrics().heightPixels;
			int diff = height - r.bottom - bottomViewHeight;

			//if it could be a keyboard add the padding to the view
			if (diff != 0) {
				// if the use-able screen height differs from the total screen height we assume that it shows a keyboard now
				//check if the padding is 0 (if yes set the padding for the keyboard)
				if (contentView.getPaddingBottom() != diff) {
					//set the padding of the contentView for the keyboard
					contentView.setPadding(0, 0, 0, diff);
				}
			} else {
				//check if the padding is != 0 (if yes reset the padding)
				if (contentView == null) return;
				if (contentView.getPaddingBottom() != 0) {
					//reset the padding of the contentView
					contentView.setPadding(0, 0, 0, 0);
				}
			}
		}
	};

	//a small helper to allow showing the editText focus
	ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
		@Override
		public void onGlobalLayout() {
			Rect r = new Rect();
			//r will be populated with the coordinates of your view that area still visible.
			decorView.getWindowVisibleDisplayFrame(r);

			//get screen height and calculate the difference with the useable area from the r
			int height = decorView.getContext().getResources().getDisplayMetrics().heightPixels;
			int diff = height - r.bottom + bottomViewHeight;

			int removePadding = height / 10 - Utils.dp2px( 8);
			//if it could be a keyboard add the padding to the view
			if (diff != 0) {
				// if the use-able screen height differs from the total screen height we assume that it shows a keyboard now
				//check if the padding is 0 (if yes set the padding for the keyboard)
				if (contentView.getPaddingBottom() != diff) {
					//set the padding of the contentView for the keyboard
					contentView.setPadding(0, 0, 0, diff);
				} else {
					contentView.setPadding(0, 0, 0, diff - removePadding);
				}
			} else {
				//check if the padding is != 0 (if yes reset the padding)
				if (contentView.getPaddingBottom() != 0) {
					//reset the padding of the contentView
					contentView.setPadding(0, 0, 0, 0);
				}
			}
		}
	};

	/**
	 * Helper to hide the keyboard
	 *
	 * @param act
	 */
	public void hideKeyboard(Activity act) {
		if (act != null && act.getCurrentFocus() != null) {
			InputMethodManager inputMethodManager = (InputMethodManager) act.getSystemService(Activity.INPUT_METHOD_SERVICE);
			inputMethodManager.hideSoftInputFromWindow(act.getCurrentFocus().getWindowToken(), 0);
			inputMethodManager.showSoftInputFromInputMethod(act.getCurrentFocus().getWindowToken(), 0);
		}
	}

	public static void hideSoftKeyboard(Activity activity) {
		try {
			if (activity != null) {
				activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
				if ((activity.getCurrentFocus() != null) && ((activity.getCurrentFocus().getWindowToken() != null))) {
					((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void showKeyboard(Activity activity) {
		if (activity != null) {
			((InputMethodManager) (activity).getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
		}
	}
}