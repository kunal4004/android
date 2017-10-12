package za.co.woolworths.financial.services.android.ui.activities;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.dto.OfferActive;
import za.co.woolworths.financial.services.android.ui.fragments.CLIEligibilityAndPermissionFragment;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.FragmentUtils;
import za.co.woolworths.financial.services.android.util.Utils;

public class CLIPhase2Activity extends AppCompatActivity {

	private WTextView tvDeclineOffer;
	private ProgressBar pbDecline;
	private boolean offerActive;
	private String strOfferActive;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cli_phase2_activity);
		Utils.updateStatusBarBackground(this);
		Bundle mBundle = getIntent().getExtras();
		strOfferActive = mBundle.getString("jsonOfferActive");
		init();
		actionBar();
		loadFragment("Consents");
		hideDeclineOffer();
	}

	private void init() {
		tvDeclineOffer = (WTextView) findViewById(R.id.tvDeclineOffer);
		pbDecline = (ProgressBar) findViewById(R.id.pbDecline);
	}

	private void actionBar() {
		Toolbar mToolbar = (Toolbar) findViewById(R.id.mToolbar);
		setSupportActionBar(mToolbar);
		getSupportActionBar().setTitle(null);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	public void loadFragment(String nextStep) {
		FragmentUtils fragmentUtils = new FragmentUtils();
		if (nextStep.equalsIgnoreCase("Consents")) {
			CLIEligibilityAndPermissionFragment cLIEligibilityAndPermissionFragment = new CLIEligibilityAndPermissionFragment();
			fragmentUtils.currentFragment(CLIPhase2Activity.this, cLIEligibilityAndPermissionFragment, R.id.cliMainFrame);
			return;
		}

		if (nextStep.equalsIgnoreCase("I&E") && offerActive) {
			return;
		}

		if (nextStep.equalsIgnoreCase("I&E") && !offerActive) {
			return;
		}

		if (nextStep.equalsIgnoreCase("Offer")) {
			CLIEligibilityAndPermissionFragment cLIEligibilityAndPermissionFragment = new CLIEligibilityAndPermissionFragment();
			fragmentUtils.currentFragment(CLIPhase2Activity.this, cLIEligibilityAndPermissionFragment, R.id.cliMainFrame);
			return;
		}

		if (nextStep.equalsIgnoreCase("POI")) {
			return;
		}

		if (nextStep.equalsIgnoreCase("Decline")) {
			return;
		}

		if (nextStep.equalsIgnoreCase("contactUs")) {
			return;
		}

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

	public void actionBarCloseIcon() {
		getSupportActionBar().setHomeAsUpIndicator(R.drawable.close_24);
	}

	public void actionBarBackIcon() {
		getSupportActionBar().setHomeAsUpIndicator(R.drawable.back24);
	}

	public void showDeclineOffer() {
		tvDeclineOffer.setVisibility(View.VISIBLE);
		pbDecline.setVisibility(View.GONE);
	}

	public void hideDeclineOffer() {
		tvDeclineOffer.setVisibility(View.GONE);
	}

	public void showDeclineProgressBar() {
		pbDecline.setVisibility(View.VISIBLE);
		tvDeclineOffer.setVisibility(View.GONE);
		pbDecline.getIndeterminateDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
	}

	public WTextView getTVDeclineOffer() {
		return tvDeclineOffer;
	}

	public Object offerActiveObject() {
		return Utils.strToJson(strOfferActive, OfferActive.class);
	}
}
