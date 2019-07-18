package za.co.woolworths.financial.services.android.ui.activities;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.fragments.store.StoresNearbyFragment1;
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
