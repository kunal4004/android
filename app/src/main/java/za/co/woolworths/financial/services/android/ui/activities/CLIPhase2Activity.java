package za.co.woolworths.financial.services.android.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.awfs.coordination.R;

import java.util.HashMap;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.CreateOfferDecision;
import za.co.woolworths.financial.services.android.models.dto.OfferActive;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.models.rest.CLIOfferDecision;
import za.co.woolworths.financial.services.android.ui.fragments.CLIAllStepsContainerFragment;
import za.co.woolworths.financial.services.android.ui.fragments.CLIEligibilityAndPermissionFragment;
import za.co.woolworths.financial.services.android.ui.fragments.CLIPOIProblemFragment;
import za.co.woolworths.financial.services.android.ui.fragments.ContactUsFinancialServiceFragment;
import za.co.woolworths.financial.services.android.ui.fragments.DocumentFragment;
import za.co.woolworths.financial.services.android.ui.fragments.OfferCalculationFragment;
import za.co.woolworths.financial.services.android.ui.fragments.SupplyIncomeDetailFragment;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.FragmentUtils;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.SessionExpiredUtilities;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.binder.ContactUsFragmentChange;
import za.co.woolworths.financial.services.android.util.controller.CLIStepIndicatorListener;
import za.co.woolworths.financial.services.android.util.controller.EventStatus;
import za.co.woolworths.financial.services.android.util.controller.IncreaseLimitController;

public class CLIPhase2Activity extends AppCompatActivity implements ContactUsFragmentChange, View.OnClickListener {

	private WTextView tvDeclineOffer, mToolbarText;
	private ProgressBar pbDecline;
	private OfferActive mCLICreateOfferResponse;
	private String mOfferActivePayload;
	private boolean mOfferActive, mCloseButtonEnabled;
	private String mNextStep;
	private final int DECLINE_OFFER_CODE = 123;
	WoolworthsApplication woolworthsApplication;
	private WGlobalState wGlobalState;
	private int editNumberValue;
	public EventStatus eventStatus;
	public ImageView imBack;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cli_phase2_activity);
		Utils.updateStatusBarBackground(this);
		Bundle mBundle = getIntent().getExtras();
		init();
		hideDeclineOffer();
		actionBar();
		listener();
		if (mBundle != null) {
			mOfferActivePayload = mBundle.getString("OFFER_ACTIVE_PAYLOAD");
			mOfferActive = mBundle.getBoolean("OFFER_IS_ACTIVE");
			mCLICreateOfferResponse = offerActiveObject();
			mNextStep = mCLICreateOfferResponse.nextStep;
			//mNextStep = getString(R.string.status_offer);
			loadFragment(mNextStep);
		}
	}

	private void listener() {
		tvDeclineOffer.setOnClickListener(this);
		imBack.setOnClickListener(this);
	}

	private void init() {
		woolworthsApplication = (WoolworthsApplication) CLIPhase2Activity.this.getApplication();
		wGlobalState = woolworthsApplication.getWGlobalState();
		tvDeclineOffer = (WTextView) findViewById(R.id.tvDeclineOffer);
		pbDecline = (ProgressBar) findViewById(R.id.pbDecline);
		imBack = (ImageView) findViewById(R.id.imBack);
	}

	private void actionBar() {
		Toolbar mToolbar = (Toolbar) findViewById(R.id.mToolbar);
		mToolbarText = (WTextView) findViewById(R.id.toolbarText);
		setSupportActionBar(mToolbar);
		getSupportActionBar().setTitle(null);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
	}

	public void loadFragment(String nextStep) {
		if (nextStep.equalsIgnoreCase(getString(R.string.status_consents))) {
			CLIEligibilityAndPermissionFragment cLIEligibilityAndPermissionFragment = new CLIEligibilityAndPermissionFragment();
			openNextFragment(cLIEligibilityAndPermissionFragment);
		} else if (nextStep.equalsIgnoreCase(getString(R.string.status_poi_problem))) {
			CLIPOIProblemFragment clipoiProblem = new CLIPOIProblemFragment();
			openNextFragment(clipoiProblem);
		} else if (nextStep.equalsIgnoreCase(getString(R.string.status_contact_us))) {
			ContactUsFinancialServiceFragment contactUsFinancialServiceFragment = new ContactUsFinancialServiceFragment();
			openNextFragment(contactUsFinancialServiceFragment);
			setTitle(getString(R.string.contact_us_financial_services));
			actionBarCloseIcon();
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
			SupplyIncomeDetailFragment cLIEligibilityAndPermissionFragment = new SupplyIncomeDetailFragment();
			cLIEligibilityAndPermissionFragment.setStepIndicatorListener(cliStepIndicatorListener);
			openFragment(cLIEligibilityAndPermissionFragment);
			return;
		}

		if (nextStep.equalsIgnoreCase(getString(R.string.status_i_n_e)) && offerActive) {
			HashMap<String, String> incomeHashMap = increaseLimitController.incomeHashMap(mCLICreateOfferResponse);
			HashMap<String, String> expenseHashMap = increaseLimitController.expenseHashMap(mCLICreateOfferResponse);
			SupplyIncomeDetailFragment supplyIncomeDetailFragment = new SupplyIncomeDetailFragment();
			offerBundle.putSerializable(IncreaseLimitController.INCOME_DETAILS, incomeHashMap);
			offerBundle.putSerializable(IncreaseLimitController.EXPENSE_DETAILS, expenseHashMap);
			supplyIncomeDetailFragment.setStepIndicatorListener(cliStepIndicatorListener);
			supplyIncomeDetailFragment.setArguments(offerBundle);
			setEventStatus(EventStatus.UPDATE_OFFER);
			openFragment(supplyIncomeDetailFragment);
			return;
		}

		if (nextStep.equalsIgnoreCase(getString(R.string.status_i_n_e)) && !offerActive) {
			SupplyIncomeDetailFragment supplyIncomeDetailFragment = new SupplyIncomeDetailFragment();
			supplyIncomeDetailFragment.setStepIndicatorListener(cliStepIndicatorListener);
			setEventStatus(EventStatus.CREATE_OFFER);
			openFragment(supplyIncomeDetailFragment);
			return;
		}

		if (nextStep.equalsIgnoreCase(getString(R.string.status_offer))) {
			HashMap<String, String> icomeHashMap = increaseLimitController.incomeHashMap(mCLICreateOfferResponse);
			HashMap<String, String> expenseHashMap = increaseLimitController.expenseHashMap(mCLICreateOfferResponse);
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
			if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
				hideSoftKeyboard();
				getSupportFragmentManager().popBackStack();
				overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
			} else {
				finishActivity();
			}
		}
	}

	public void actionBarCloseIcon() {
		setCloseButtonEnabled(true);
		imBack.setImageResource(R.drawable.close_24);
	}

	public void actionBarBackIcon() {
		setCloseButtonEnabled(false);
		imBack.setImageResource(R.drawable.back24);
	}

	public void showDeclineOffer() {
		tvDeclineOffer.setVisibility(View.VISIBLE);
		pbDecline.setVisibility(View.GONE);
	}

	public void hideDeclineOffer() {
		tvDeclineOffer.setVisibility(View.GONE);
		pbDecline.setVisibility(View.GONE);
	}

	public void showDeclineProgressBar() {
		pbDecline.setVisibility(View.VISIBLE);
		tvDeclineOffer.setVisibility(View.GONE);
		pbDecline.getIndeterminateDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
	}

	public OfferActive offerActiveObject() {
		return (OfferActive) Utils.strToJson(mOfferActivePayload, OfferActive.class);
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.tvDeclineOffer:
				Utils.displayValidationMessage(CLIPhase2Activity.this, CustomPopUpWindow.MODAL_LAYOUT.CLI_DANGER_ACTION_MESSAGE_VALIDATION, DECLINE_OFFER_CODE);
				break;
			case R.id.imBack:
				onBack();
				break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (resultCode) {
			case RESULT_OK:
				switch (requestCode) {
					case DECLINE_OFFER_CODE:
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								cliDelcineOfferRequest(wGlobalState.getDeclineDecision());
							}
						});
						break;
					default:
						break;
				}
				break;
			default:
				break;
		}
	}

	private void cliDelcineOfferRequest(CreateOfferDecision createOfferDecision) {
		onDeclineLoad();
		CLIOfferDecision cliOfferDecision = new CLIOfferDecision(CLIPhase2Activity.this, createOfferDecision, String.valueOf(mCLICreateOfferResponse.cliId), new OnEventListener() {

			@Override
			public void onSuccess(Object object) {
				OfferActive mObjOffer = ((OfferActive) object);
				switch (mObjOffer.httpCode) {
					case 200:
						finishActivity();
						break;
					case 440:
						SessionExpiredUtilities.INSTANCE.setAccountSessionExpired(CLIPhase2Activity.this, mObjOffer.response.stsParams);
						break;
					default:
						Utils.displayValidationMessage(CLIPhase2Activity.this, CustomPopUpWindow.MODAL_LAYOUT.ERROR, mObjOffer.response.desc);
						break;
				}
				onDeclineComplete();
			}

			@Override
			public void onFailure(String e) {
				onDeclineComplete();
			}
		});

		cliOfferDecision.execute();
	}

	private void onDeclineLoad() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				showDeclineProgressBar();
			}
		});
	}

	private void onDeclineComplete() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				showDeclineOffer();
			}
		});
	}

	public int getEditNumberValue() {
		return editNumberValue;
	}

	public void setEditNumberValue(int editNumberValue) {
		this.editNumberValue = editNumberValue;
	}

	@Override
	public void onFragmentChanged(String title) {
		//contact us change title listener
	}

	private void setTitle(String text) {
		mToolbarText.setText(text);
	}

	public void hideBurgerButton() {
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
	}

	public static void toggle(Activity activity) {
		InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
		if (imm.isActive()) {
			imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0); // hide
		} else {
			imm.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY); // show
		}
	}//end method

	/**
	 * Hides the soft keyboard
	 */
	private void hideSoftKeyboard() {
		try {
			final Activity activity = CLIPhase2Activity.this;
			if (activity != null) {
				if (activity.getCurrentFocus() != null) {
					InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
					inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
				}
			}
		} catch (Exception ignored) {
		}
	}

	public EventStatus getEventStatus() {
		return eventStatus;
	}

	public void setEventStatus(EventStatus eventStatus) {
		this.eventStatus = eventStatus;
	}
}
