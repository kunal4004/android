package za.co.woolworths.financial.services.android.ui.activities;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.awfs.coordination.R;

import java.util.HashMap;

import za.co.woolworths.financial.services.android.models.dto.CreateOfferResponse;
import za.co.woolworths.financial.services.android.ui.fragments.CLIAllStepsContainerFragment;
import za.co.woolworths.financial.services.android.ui.fragments.CLIEligibilityAndPermissionFragment;
import za.co.woolworths.financial.services.android.ui.fragments.DocumentFragment;
import za.co.woolworths.financial.services.android.ui.fragments.OfferCalculationFragment;
import za.co.woolworths.financial.services.android.ui.fragments.SupplyIncomeDetailFragment;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.FragmentUtils;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.controller.CLIStepIndicatorListener;
import za.co.woolworths.financial.services.android.util.controller.IncreaseLimitController;

public class CLIPhase2Activity extends AppCompatActivity {

	private WTextView tvDeclineOffer;
	private ProgressBar pbDecline;
	private CreateOfferResponse createOfferResponse;
	private String mOfferActivePayload;
	private boolean mOfferActive, mCloseButtonEnabled;
	private String mNextStep;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cli_phase2_activity);
		Utils.updateStatusBarBackground(this);
		Bundle mBundle = getIntent().getExtras();
		init();
		hideDeclineOffer();
		actionBar();
		if (mBundle != null) {
			mOfferActivePayload = mBundle.getString("OFFER_ACTIVE_PAYLOAD");
			mOfferActive = mBundle.getBoolean("OFFER_IS_ACTIVE");
			createOfferResponse = offerActiveObject();
			mNextStep = createOfferResponse.cli.nextStep;
			//mNextStep = "Consents";
			loadFragment(mNextStep);
		}
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
		if (nextStep.equalsIgnoreCase(getString(R.string.status_consents))) {
			CLIEligibilityAndPermissionFragment cLIEligibilityAndPermissionFragment = new CLIEligibilityAndPermissionFragment();
			openNextFragment(cLIEligibilityAndPermissionFragment);
		} else {
			moveToCLIAllStepsContainerFragment();
		}
	}

	public void initFragment(CLIStepIndicatorListener cliStepIndicatorListener) {
		String nextStep = mNextStep;
		boolean offerActive = mOfferActive;
		IncreaseLimitController increaseLimitController = new IncreaseLimitController(CLIPhase2Activity.this);
		Bundle offerBundle = new Bundle();
		if (nextStep.equalsIgnoreCase(getString(R.string.status_consents))) {
			CLIEligibilityAndPermissionFragment cLIEligibilityAndPermissionFragment = new CLIEligibilityAndPermissionFragment();
			openFragment(cLIEligibilityAndPermissionFragment);
			return;
		}

		if (nextStep.equalsIgnoreCase(getString(R.string.status_i_n_e)) && offerActive) {
			HashMap<String, String> incomeHashMap = increaseLimitController.incomeHashMap(createOfferResponse);
			HashMap<String, String> expenseHashMap = increaseLimitController.expenseHashMap(createOfferResponse);
			SupplyIncomeDetailFragment supplyIncomeDetailFragment = new SupplyIncomeDetailFragment();
			offerBundle.putSerializable(IncreaseLimitController.INCOME_DETAILS, incomeHashMap);
			offerBundle.putSerializable(IncreaseLimitController.EXPENSE_DETAILS, expenseHashMap);
			supplyIncomeDetailFragment.setStepIndicatorListener(cliStepIndicatorListener);
			supplyIncomeDetailFragment.setArguments(offerBundle);
			openFragment(supplyIncomeDetailFragment);
			return;
		}

		if (nextStep.equalsIgnoreCase(getString(R.string.status_i_n_e)) && !offerActive) {
			SupplyIncomeDetailFragment supplyIncomeDetailFragment = new SupplyIncomeDetailFragment();
			supplyIncomeDetailFragment.setStepIndicatorListener(cliStepIndicatorListener);
			openFragment(supplyIncomeDetailFragment);
			return;
		}

		if (nextStep.equalsIgnoreCase(getString(R.string.status_offer))) {
			HashMap<String, String> icomeHashMap = increaseLimitController.incomeHashMap(createOfferResponse);
			HashMap<String, String> expenseHashMap = increaseLimitController.expenseHashMap(createOfferResponse);
			offerBundle.putSerializable(IncreaseLimitController.INCOME_DETAILS, icomeHashMap);
			offerBundle.putSerializable(IncreaseLimitController.EXPENSE_DETAILS, expenseHashMap);
			OfferCalculationFragment offerCalculationFragment = new OfferCalculationFragment();
			offerCalculationFragment.setStepIndicatorListener(cliStepIndicatorListener);
			offerCalculationFragment.setArguments(offerBundle);
			openFragment(offerCalculationFragment);
			return;
		}

		if (nextStep.equalsIgnoreCase(getString(R.string.status_poi))) {
			DocumentFragment documentFragment = new DocumentFragment();
			documentFragment.setStepIndicatorListener(cliStepIndicatorListener);
			openFragment(documentFragment);
			return;
		}
	}

	@Override
	public void onBackPressed() {
		onBack();
	}

	private void onBack() {
		if (closeButtonEnabled()) {
			finishActivity();
		} else {
			if (getFragmentManager().getBackStackEntryCount() > 0) {
				getFragmentManager().popBackStack();
				overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
			} else {
				finishActivity();
			}
		}
	}

	public void actionBarCloseIcon() {
		setCloseButtonEnabled(true);
		getSupportActionBar().setHomeAsUpIndicator(R.drawable.close_24);
	}

	public void actionBarBackIcon() {
		setCloseButtonEnabled(false);
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

	public CreateOfferResponse offerActiveObject() {
		return (CreateOfferResponse) Utils.strToJson(mOfferActivePayload, CreateOfferResponse.class);
	}

	private void openNextFragment(Fragment fragment) {
		FragmentUtils fragmentUtils = new FragmentUtils();
		fragmentUtils.currentFragment(CLIPhase2Activity.this, fragment, R.id.cliMainFrame);
	}

	private void openFragment(Fragment fragment) {
		FragmentUtils fragmentUtils = new FragmentUtils();
		fragmentUtils.currentFragment(CLIPhase2Activity.this, getSupportFragmentManager(), fragment, R.id.cli_steps_container);
	}

	private void moveToCLIAllStepsContainerFragment() {
		CLIAllStepsContainerFragment cliAllStepsContainerFragment = new CLIAllStepsContainerFragment();
		openNextFragment(cliAllStepsContainerFragment);
	}

	public boolean closeButtonEnabled() {
		return mCloseButtonEnabled;
	}

	public void setCloseButtonEnabled(boolean mCloseButtonEnabled) {
		this.mCloseButtonEnabled = mCloseButtonEnabled;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBack();
				return true;
		}
		return false;
	}

	public void finishActivity() {
		finish();
		overridePendingTransition(R.anim.stay, R.anim.slide_down_anim);
	}

}
