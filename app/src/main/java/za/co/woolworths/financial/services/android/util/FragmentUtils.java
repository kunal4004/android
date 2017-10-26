package za.co.woolworths.financial.services.android.util;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.inputmethod.InputMethodManager;

import com.awfs.coordination.R;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class FragmentUtils {

	private Context mContext;
	private boolean animate = true;

	public FragmentUtils() {
	}

	public FragmentUtils(boolean animate) {
		this.animate = animate;
	}

	public FragmentUtils(Context context) {
		this.mContext = context;
	}

	public void currentFragment(AppCompatActivity activity, Fragment nextFragment, int fragmentId) {
		hideSoftKeyboard();
		FragmentManager fragmentManager = activity.getSupportFragmentManager();
		fragmentManager.beginTransaction()
				.replace(fragmentId, nextFragment).commit();
	}

	public void nextFragment(FragmentManager fragmentManager, Fragment nextFragment, int fragmentId) {
		hideSoftKeyboard();
		fragmentManager.beginTransaction()
				.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up)
				.replace(fragmentId, nextFragment).commit();
	}

	public void currentFragment(AppCompatActivity activity, FragmentManager fm, Fragment newFragment, int fragmentId) {
		hideSoftKeyboard();
		FragmentManager fragmentManager = activity.getSupportFragmentManager();
		fragmentManager.beginTransaction().replace(fragmentId, newFragment).commit();
	}


	public void nextFragment(AppCompatActivity activity, FragmentTransaction fragmentTransaction, Fragment fragment, int fragmentId) {
		hideSoftKeyboard();
		FragmentManager fragmentManager = activity.getSupportFragmentManager();
		if (animate) {
			fragmentTransaction
					.setCustomAnimations(R.anim.slide_from_right, R.anim.slide_to_left, R.anim.slide_from_left, R.anim.slide_to_right)
					.replace(fragmentId, fragment).addToBackStack(fragment.getClass().getSimpleName()).commit();
		} else {
			fragmentTransaction
					.setTransition(FragmentTransaction.TRANSIT_NONE)
					.replace(fragmentId, fragment).addToBackStack(fragment.getClass().getSimpleName()).commit();
		}
		fragmentManager.executePendingTransactions();
	}

	public void nextFragment(AppCompatActivity activity, FragmentManager fragmentManager, Fragment fragment, int fragmentId) {
		hideSoftKeyboard();
		fragmentManager.beginTransaction()
				.setCustomAnimations(R.anim.slide_from_right, R.anim.slide_to_left, R.anim.slide_from_left, R.anim.slide_to_right)
				.replace(fragmentId, fragment).addToBackStack(fragment.getClass().getSimpleName()).commit();
		fragmentManager.executePendingTransactions();
	}

	public void nextBottomUpFragment(AppCompatActivity activity, FragmentTransaction fragmentTransaction, Fragment fragment, int fragmentId) {
		hideSoftKeyboard();
		FragmentManager fragmentManager = activity.getSupportFragmentManager();
		fragmentTransaction
				.setCustomAnimations(R.anim.slide_from_right, R.anim.slide_to_left, R.anim.slide_from_left, R.anim.slide_to_right)
				.replace(fragmentId, fragment).addToBackStack(fragment.getClass().getSimpleName()).commit();
		fragmentManager.executePendingTransactions();
	}


	/**
	 * Hides the soft keyboard
	 */
	public void hideSoftKeyboard() {
		try {
//			final Activity activity = (Activity) mContext;
//			if (activity != null) {
//				if (activity.getCurrentFocus() != null) {
//					InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
//					inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
//				}
//			}
		} catch (Exception ignored) {
		}
	}

}
