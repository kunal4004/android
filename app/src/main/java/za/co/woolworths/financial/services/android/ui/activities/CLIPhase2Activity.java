package za.co.woolworths.financial.services.android.ui.activities;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.fragments.CLIEligibilityAndPermissionFragment;
import za.co.woolworths.financial.services.android.util.Utils;


public class CLIPhase2Activity extends AppCompatActivity {

	private Toolbar mToolbar;
	private CLIEligibilityAndPermissionFragment fragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cli_phase2_activity);
		Utils.updateStatusBarBackground(this);
		mToolbar = (Toolbar) findViewById(R.id.mToolbar);
		setSupportActionBar(mToolbar);
		getSupportActionBar().setTitle(null);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		loadFragment();

	}

	public void loadFragment() {
		fragment = new CLIEligibilityAndPermissionFragment();
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction()
				.replace(R.id.cliMainFrame, fragment).commit();
	}

	@Override
	public void onBackPressed() {
		if (getFragmentManager().getBackStackEntryCount() > 0) {
			getFragmentManager().popBackStack();
		} else {
			super.onBackPressed();
		}
		overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
	}
}
