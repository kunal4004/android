package za.co.woolworths.financial.services.android.ui.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.fragments.BalanceInsuranceFragment;
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
		addFragment(new BalanceInsuranceFragment());
		setTitle(getString(R.string.balance_protection_title));
	}

	private void init() {
		mToolbar = (Toolbar) findViewById(R.id.mToolbar);
		mToolbarText = (WTextView) findViewById(R.id.toolbarText);
	}

	public void currentFragment(Fragment frag) {
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fm.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit, R.anim.fragment_pop_enter, R.anim.fragment_pop_exit);
		fragmentTransaction.replace(R.id.fragment_container, frag);
		fragmentTransaction.addToBackStack(this.getClass().getSimpleName());
		fragmentTransaction.show(frag);
		fragmentTransaction.commit();
	}

	public void addFragment(Fragment frag) {
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fm.beginTransaction();
		fragmentTransaction.add(R.id.fragment_container, frag);
		fragmentTransaction.addToBackStack(this.getClass().getSimpleName());
		fragmentTransaction.show(frag);
		fragmentTransaction.commit();
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
			overridePendingTransition(R.anim.stay, R.anim.slide_down_anim);
		}
	}
}
