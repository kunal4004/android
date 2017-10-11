package za.co.woolworths.financial.services.android.util;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.awfs.coordination.R;

public class FragmentUtils {

	public void currentFragment(AppCompatActivity activity, Fragment nextFragment, int fragmentId) {
		FragmentManager fragmentManager = activity.getSupportFragmentManager();
		fragmentManager.beginTransaction()
				.replace(fragmentId, nextFragment).commit();
	}

	public void nextFragment(FragmentManager fragmentManager, Fragment nextFragment, int fragmentId) {
		fragmentManager.beginTransaction()
				.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
				.replace(fragmentId, nextFragment).commit();
	}

	public void currentFragment(AppCompatActivity activity, FragmentManager fm, Fragment newFragment, int fragmentId) {
		FragmentManager fragmentManager = activity.getSupportFragmentManager();
		fm.beginTransaction().replace(fragmentId, newFragment).commit();
		fragmentManager.beginTransaction();
	}

	public void nextFragment(AppCompatActivity activity, FragmentTransaction fragmentTransaction, Fragment fragment, int fragmentId) {
		FragmentManager fragmentManager = activity.getSupportFragmentManager();
		fragmentTransaction
				.setCustomAnimations(R.anim.slide_from_right, R.anim.slide_to_left, R.anim.slide_from_left, R.anim.slide_to_right)
				.replace(fragmentId, fragment).addToBackStack(fragment.getClass().getSimpleName()).commit();
		fragmentManager.executePendingTransactions();
	}

	public void nextFragment(AppCompatActivity activity, FragmentManager fragmentManager, Fragment fragment, int fragmentId) {
		fragmentManager.beginTransaction()
				.setCustomAnimations(R.anim.slide_from_right, R.anim.slide_to_left, R.anim.slide_from_left, R.anim.slide_to_right)
				.replace(fragmentId, fragment).addToBackStack(fragment.getClass().getSimpleName()).commit();
		fragmentManager.executePendingTransactions();
	}

	public void nextBottomUpFragment(AppCompatActivity activity, FragmentTransaction fragmentTransaction, Fragment fragment, int fragmentId) {
		FragmentManager fragmentManager = activity.getSupportFragmentManager();
		fragmentTransaction
				.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_top)
				.replace(fragmentId, fragment).addToBackStack(fragment.getClass().getSimpleName()).commit();
		fragmentManager.executePendingTransactions();
	}
}
