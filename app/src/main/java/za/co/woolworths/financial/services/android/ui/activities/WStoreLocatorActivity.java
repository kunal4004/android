package za.co.woolworths.financial.services.android.ui.activities;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.fragments.StoresNearbyFragment1;
import za.co.woolworths.financial.services.android.util.Utils;


public class WStoreLocatorActivity extends AppCompatActivity {

	public static Toolbar mToolbar;
	public Fragment fragment = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wstore_locator_activity);
		Utils.updateStatusBarBackground(this);
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		fragment = new StoresNearbyFragment1();
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(R.id.stores_container_body, fragment);
		fragmentTransaction.commit();
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				return true;
		}
		return false;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.stay, R.anim.slide_down_anim);
	}
}
