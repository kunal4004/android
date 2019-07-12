package za.co.woolworths.financial.services.android.util;

import android.content.Context;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;

import com.awfs.coordination.R;

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
		FragmentManager fragmentManager = activity.getSupportFragmentManager();
		fragmentManager.beginTransaction()
				.replace(fragmentId, nextFragment).commitAllowingStateLoss();
	}

	public void nextFragment(FragmentManager fragmentManager, Fragment nextFragment, int fragmentId) {
		fragmentManager.beginTransaction()
				.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up)
				.replace(fragmentId, nextFragment).commitAllowingStateLoss();
	}


	public void openFragment(FragmentManager fragmentManager, Fragment nextFragment, int fragmentId) {
		fragmentManager.beginTransaction()
				.replace(fragmentId, nextFragment).commitAllowingStateLoss();
	}

	public void currentFragment(AppCompatActivity activity, FragmentManager fm, Fragment newFragment, int fragmentId) {
		FragmentManager fragmentManager = activity.getSupportFragmentManager();
		fragmentManager.beginTransaction().replace(fragmentId, newFragment).commitAllowingStateLoss();
	}


	public void nextFragment(AppCompatActivity activity, FragmentTransaction fragmentTransaction, Fragment fragment, int fragmentId) {
		FragmentManager fragmentManager = activity.getSupportFragmentManager();
		if (animate) {
			fragmentTransaction
					.setCustomAnimations(R.anim.slide_from_right, R.anim.slide_to_left, R.anim.slide_from_left, R.anim.slide_to_right)
					.replace(fragmentId, fragment).addToBackStack(fragment.getClass().getSimpleName()).commitAllowingStateLoss();
		} else {
			fragmentTransaction
					.setTransition(FragmentTransaction.TRANSIT_NONE)
					.replace(fragmentId, fragment).addToBackStack(fragment.getClass().getSimpleName()).commitAllowingStateLoss();
		}
		fragmentManager.executePendingTransactions();
	}

	public void nextFragment(AppCompatActivity activity, FragmentManager fragmentManager, Fragment fragment, int fragmentId) {
		fragmentManager.beginTransaction()
				.setCustomAnimations(R.anim.slide_from_right, R.anim.slide_to_left, R.anim.slide_from_left, R.anim.slide_to_right)
				.replace(fragmentId, fragment).addToBackStack(fragment.getClass().getSimpleName()).commitAllowingStateLoss();
		fragmentManager.executePendingTransactions();
	}

}
