package za.co.woolworths.financial.services.android.ui.activities;


import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.util.Utils;

public class EStatementActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_e_statement_activity);
		Utils.updateStatusBarBackground(this);
		actionBar();
		initUI();
	}

	private void actionBar() {
		Toolbar mToolbar = (Toolbar) findViewById(R.id.mToolbar);
		setSupportActionBar(mToolbar);
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setDisplayShowTitleEnabled(false);
			actionBar.setDisplayUseLogoEnabled(false);
			actionBar.setHomeAsUpIndicator(R.drawable.close_24);
		}
	}

	private void initUI() {
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

	@Override
	public void onBackPressed() {
		handleBackButton();
	}

	private void handleBackButton() {
		finish();
		overridePendingTransition(R.anim.stay, R.anim.slide_down_anim);
	}
}
