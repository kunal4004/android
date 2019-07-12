package za.co.woolworths.financial.services.android.ui.activities;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.fragments.SubmitClaimFragment;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.Utils;

public class BalanceProtectionActivity extends AppCompatActivity {

	private Toolbar mToolbar;
	private WTextView mToolbarText;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.balance_insurance_activity);
		Utils.updateStatusBarBackground(this);
		init();
		setActionBar();
		addFragment(new SubmitClaimFragment());
		setTitle(getString(R.string.balance_protection_title));
	}

	private void init() {
		mToolbar = (Toolbar) findViewById(R.id.mToolbar);
		mToolbarText = (WTextView) findViewById(R.id.toolbarText);
	}

	public void currentFragment(Fragment frag) {
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fm.beginTransaction();
		fragmentTransaction.setCustomAnimations( R.anim.slide_in_from_right, R.anim.slide_out_to_left, R.anim.slide_in_from_left, R.anim.slide_out_to_right);
		fragmentTransaction.replace(R.id.fragment_container, frag);
		fragmentTransaction.addToBackStack(this.getClass().getSimpleName());
		fragmentTransaction.commitAllowingStateLoss();
	}

	public void addFragment(Fragment frag) {
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fm.beginTransaction();
		fragmentTransaction.add(R.id.fragment_container, frag);
		fragmentTransaction.addToBackStack(this.getClass().getSimpleName());
		fragmentTransaction.commitAllowingStateLoss();
	}

	private void setActionBar() {
		setSupportActionBar(mToolbar);
		ActionBar mActionBar = getSupportActionBar();
		if (mActionBar != null) {
			mActionBar.setDisplayHomeAsUpEnabled(true);
			mActionBar.setDisplayShowTitleEnabled(false);
			mActionBar.setDisplayUseLogoEnabled(false);
			mActionBar.setHomeAsUpIndicator(R.drawable.back24);
		}
		}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				handleBackButton();
				return true;
		}
		return false;
	}

	public void setTitle(String title) {
		mToolbarText.setText(title);
	}

	@Override
	public void onBackPressed() {
		handleBackButton();
	}

	private void handleBackButton() {
		if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
			getSupportFragmentManager().popBackStack();
		} else {
			finish();
			overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
		}
	}
}
