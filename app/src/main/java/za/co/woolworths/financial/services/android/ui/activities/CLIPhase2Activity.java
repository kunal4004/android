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
		// if there is a fragment and the back stack of this fragment is not empty,
//		// then emulate 'onBackPressed' behaviour, because in default, it is not working
//		for (Fragment frag : getC().getFragments()) {
//			if (frag.isVisible()) {
//				FragmentManager childFm = frag.getChildFragmentManager();
//				if (childFm.getBackStackEntryCount() > 0) {
//					childFm.popBackStack();
//					return;
//				}
//			}
//		}
		super.onBackPressed();
	}
}
