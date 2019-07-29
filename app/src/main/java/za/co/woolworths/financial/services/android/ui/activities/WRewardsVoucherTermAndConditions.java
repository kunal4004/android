package za.co.woolworths.financial.services.android.ui.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.Utils;

public class WRewardsVoucherTermAndConditions extends AppCompatActivity {
	public Toolbar toolbar;
	public WTextView termsAndCondition;
	public WButton viewGeneralTermsAndCondtions;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wrewards_voucher_term_and_conditions);
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		termsAndCondition = (WTextView) findViewById(R.id.termsAndCondtions);
		viewGeneralTermsAndCondtions = (WButton) findViewById(R.id.generalTermsAndConditions);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(null);
		if (getIntent().hasExtra("TERMS"))
			termsAndCondition.setText(getIntent().getStringExtra("TERMS"));
		viewGeneralTermsAndCondtions.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.openExternalLink(WRewardsVoucherTermAndConditions.this, WoolworthsApplication.getWrewardsTCLink());
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		Utils.setScreenName(this, FirebaseManagerAnalyticsProperties.ScreenNames.WREWARDS_TERMS_CONDITIONS);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				overridePendingTransition(R.anim.stay, R.anim.slide_down_anim);
				break;
		}
		return true;
	}
}
