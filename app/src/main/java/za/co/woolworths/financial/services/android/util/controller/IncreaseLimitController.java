package za.co.woolworths.financial.services.android.util.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.awfs.coordination.R;

import java.util.HashMap;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.Application;
import za.co.woolworths.financial.services.android.models.dto.Cli;
import za.co.woolworths.financial.services.android.models.dto.CreateOfferResponse;
import za.co.woolworths.financial.services.android.models.dto.Offer;
import za.co.woolworths.financial.services.android.models.dto.OfferActive;
import za.co.woolworths.financial.services.android.ui.activities.CLIPhase2Activity;
import za.co.woolworths.financial.services.android.ui.views.WEditTextView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.Utils;

public class IncreaseLimitController {

	public static final String INCOME_DETAILS = "INCOME_DETAILS";
	public static final String EXPENSE_DETAILS = "EXPENSE_DETAILS";
	public static final String YOUTUBE_API_KEY = "AIzaSyB2o1ArKC7pf8lixwPNG98obPsdp5-UZZM";

	private Context mContext;
	private String nextStep;
	private boolean offerActive;

	public IncreaseLimitController() {
	}

	public IncreaseLimitController(Context context) {
		this.mContext = context;
	}

	public static boolean editTextLength(String value) {
		return value.toString().length() > 0 ? true : false;
	}

	public static void showKeyboard(WEditTextView wEditTextView, Context context) {
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(wEditTextView,
				InputMethodManager.SHOW_IMPLICIT);
	}

	public static void hideKeyboard(WEditTextView wEditTextView, Context context) {
		InputMethodManager imm = (InputMethodManager)
				context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(
				wEditTextView.getWindowToken(), 0);
	}

	public static void hideSoftKeyboard(Activity activity) {
		// Check if no view has focus:
		View view = activity.getCurrentFocus();
		if (view != null) {
			InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}

	public static String removeNonDigit(WEditTextView view) {
		return view.getText().toString().replaceAll("\\D+", "");
	}

	public static void focusEditView(WEditTextView wEditText, WTextView wTextView, Context context) {
		wEditText.requestFocus();
		IncreaseLimitController.showKeyboard(wEditText, context);
		wTextView.setVisibility(View.VISIBLE);
	}

	public void offerActiveUIState(LinearLayout llCommonLayer, WTextView tvIncreaseLimit, WTextView tvApplyNowIncreaseLimit,
								   ImageView logoIncreaseLimit, OfferActive offerActive) {
		Cli cli = getCLI(offerActive);
		String nextStep = cli.nextStep;
		String messageSummary = cli.messageSummary;
		String messageDetail = cli.messageDetail;
		setNextStep(nextStep);
		setOfferActive(offerActive.offerActive);
		if (messageSummary.equalsIgnoreCase(getString(R.string.status_offer_available))) {
			showView(logoIncreaseLimit);
			hideView(llCommonLayer);
			setCLITag(messageSummary, R.drawable.cli_round_offer_available, tvApplyNowIncreaseLimit);
		} else if (messageSummary.equalsIgnoreCase(getString(R.string.status_retry))) {
			showView(logoIncreaseLimit);
			hideView(llCommonLayer);
			messageSummary = getString(R.string.status_please_try_again);
			setCLITag(messageSummary, R.drawable.cli_round_inprogress_tag, tvApplyNowIncreaseLimit);
		} else if (messageSummary.equalsIgnoreCase(getString(R.string.status_poi_required))) {
			showView(logoIncreaseLimit);
			hideView(llCommonLayer);
			setCLITag(messageSummary, R.drawable.cli_round_inprogress_tag, tvApplyNowIncreaseLimit);
		} else {
			showView(logoIncreaseLimit);
			hideView(llCommonLayer);
			messageSummary = getString(R.string.status_unavailable);
			setCLITag(messageSummary, R.drawable.cli_round_offer_unavailable, tvApplyNowIncreaseLimit);
		}
//
//		switch (status) {
//			case APPLY_NOW:
//				hideView(logoIncreaseLimit);
//				showView(llCommonLayer);
//				setCLITag(messageSummary, R.drawable.cli_round_apply_now_tag, tvApplyNowIncreaseLimit);
//				tvIncreaseLimit.setText(getString(R.string.cli_credit_limit_increase));
//				break;
//
//			case IN_PROGRESS:
//				showView(logoIncreaseLimit);
//				hideView(llCommonLayer);
//				setCLITag(messageSummary, R.drawable.cli_round_inprogress_tag, tvApplyNowIncreaseLimit);
//				break;
//
//
//			case POI_PROBLEM:
//				showView(logoIncreaseLimit);
//				hideView(llCommonLayer);
//				setCLITag(messageSummary, R.drawable.cli_round_offer_poi_problem, tvApplyNowIncreaseLimit);
//				break;
//
//			case UNAVAILABLE:
//				break;
//			default:
//				break;
//		}
	}

	private void hideView(View view) {
		view.setVisibility(View.GONE);
	}

	public void showView(View view) {
		view.setVisibility(View.VISIBLE);
	}

	private void setStatusText(String messageSummary, WTextView wTextView) {
		wTextView.setText(messageSummary);
	}

	private void setStatusBackground(int drawable, WTextView wTextView, String messageSummary) {
		wTextView.setBackgroundResource(drawable);
		wTextView.setText(messageSummary);
	}

	private String getString(int stringId) {
		return mContext.getResources().getString(stringId);
	}

	private void setCLITag(String messageSummary, int drawableId, WTextView tvApplyNowIncreaseLimit) {
		setStatusText(messageSummary, tvApplyNowIncreaseLimit);
		setStatusBackground(drawableId, tvApplyNowIncreaseLimit, messageSummary);
	}

	public void moveToCLIPhase(OfferActive offerActive, String productOfferingId) {
		AppCompatActivity activity = (AppCompatActivity) mContext;
		((WoolworthsApplication) activity.getApplication()).setProductOfferingId(Integer.valueOf(productOfferingId));
		Intent openCLIIncrease = new Intent(activity, CLIPhase2Activity.class);
		openCLIIncrease.putExtra("OFFER_ACTIVE_PAYLOAD", Utils.objectToJson(offerActive));
		openCLIIncrease.putExtra("OFFER_IS_ACTIVE", offerIsActive());
		activity.startActivity(openCLIIncrease);
		activity.overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
	}

	private Offer getOffer(OfferActive offerActive) {
		Cli cliOffer = getCLI(offerActive);
		if (cliOffer != null) {
			return cliOffer.offer;
		}
		return new Offer();
	}

	private Cli getCLI(OfferActive active) {
		return active.cli;
	}

	public static void focusEditView(WEditTextView wEditText, Context context) {
		wEditText.requestFocus();
		IncreaseLimitController.showKeyboard(wEditText, context);
	}

	public String getNextStep() {
		return nextStep;
	}

	private void setNextStep(String nextStep) {
		this.nextStep = nextStep;
	}

	public boolean offerIsActive() {
		return offerActive;
	}

	public void setOfferActive(boolean offerActive) {
		this.offerActive = offerActive;
	}

	public void nextStep(OfferActive offerActive, String productOfferingId) {
		String nextStep = getNextStep();
		if (nextStep.equalsIgnoreCase(getString(R.string.status_consents))) {
			moveToCLIPhase(offerActive, productOfferingId);
		} else {
			moveToCLIPhase(offerActive, productOfferingId);
		}
	}

	public void defaultIncreaseLimitView(ImageView logoIncreaseLimit, LinearLayout llCommonLayer, WTextView tvIncreaseLimit) {
		showView(logoIncreaseLimit);
		hideView(llCommonLayer);
		String messageSummary = getString(R.string.increase_limit);
		tvIncreaseLimit.setText(messageSummary);
	}

	public HashMap<String, String> expenseHashMap(WEditTextView etMortgagePayments, WEditTextView etRentalPayments, WEditTextView etMaintainanceExpenses, WEditTextView etMonthlyCreditPayments, WEditTextView etOtherExpenses) {
		HashMap<String, String> mHashExpenseDetail = new HashMap<>();
		mHashExpenseDetail.put("MORTGAGE_PAYMENTS", removeNonDigit(etMortgagePayments));
		mHashExpenseDetail.put("RENTAL_PAYMENTS", removeNonDigit(etRentalPayments));
		mHashExpenseDetail.put("MAINTENANCE_EXPENSES", removeNonDigit(etMaintainanceExpenses));
		mHashExpenseDetail.put("MONTHLY_CREDIT_EXPENSES", removeNonDigit(etMonthlyCreditPayments));
		mHashExpenseDetail.put("OTHER_EXPENSES", removeNonDigit(etOtherExpenses));
		return mHashExpenseDetail;
	}

	public HashMap<String, String> incomeHashMap(WEditTextView etGrossMonthlyIncome, WEditTextView etNetMonthlyIncome, WEditTextView etAdditionalMonthlyIncome) {
		HashMap<String, String> mHmSupplyIncomeDetail = new HashMap<>();
		mHmSupplyIncomeDetail.put("GROSS_MONTHLY_INCOME", IncreaseLimitController.removeNonDigit(etGrossMonthlyIncome));
		mHmSupplyIncomeDetail.put("NET_MONTHLY_INCOME", IncreaseLimitController.removeNonDigit(etNetMonthlyIncome));
		mHmSupplyIncomeDetail.put("ADDITIONAL_MONTHLY_INCOME", IncreaseLimitController.removeNonDigit(etAdditionalMonthlyIncome));
		return mHmSupplyIncomeDetail;
	}

	public HashMap<String, String> incomeHashMap(CreateOfferResponse offerActive) {
		Application application = getApplication(offerActive);
		HashMap<String, String> incomeHashMap = new HashMap<>();
		incomeHashMap.put("GROSS_MONTHLY_INCOME", toString(application.grossMonthlyIncome));
		incomeHashMap.put("NET_MONTHLY_INCOME", toString(application.netMonthlyIncome));
		incomeHashMap.put("ADDITIONAL_MONTHLY_INCOME", toString(application.additionalIncomeAmount));
		return incomeHashMap;
	}

	public HashMap<String, String> expenseHashMap(CreateOfferResponse offerActive) {
		Application application = getApplication(offerActive);
		HashMap<String, String> expenseHashMap = new HashMap<>();
		expenseHashMap.put("MORTGAGE_PAYMENTS", toString(application.mortgagePaymentAmount));
		expenseHashMap.put("RENTAL_PAYMENTS", toString(application.rentalPaymentAmount));
		expenseHashMap.put("MAINTENANCE_EXPENSES", toString(application.maintenanceExpenseAmount));
		expenseHashMap.put("MONTHLY_CREDIT_EXPENSES", toString(application.totalCreditExpenseAmount));
		expenseHashMap.put("OTHER_EXPENSES", toString(application.otherExpenseAmount));
		return expenseHashMap;
	}

	private Application getApplication(CreateOfferResponse offerActive) {
		return offerActive.cli.application;
	}

	private String toString(int value) {
		return String.valueOf(value);
	}
}
