package za.co.woolworths.financial.services.android.util.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.awfs.coordination.R;

import java.util.HashMap;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.Application;
import za.co.woolworths.financial.services.android.models.dto.OfferActive;
import za.co.woolworths.financial.services.android.ui.activities.cli.CLIPhase2Activity;
import za.co.woolworths.financial.services.android.ui.activities.cli.FindOutMoreActivity;
import za.co.woolworths.financial.services.android.ui.activities.MyAccountCardsActivity;
import za.co.woolworths.financial.services.android.ui.views.WEditTextView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.Utils;

public class IncreaseLimitController {

	public static final String INCOME_DETAILS = "INCOME_DETAILS";
	public static final String EXPENSE_DETAILS = "EXPENSE_DETAILS";
	public static final String FROM_EXPENSE_SCREEN = "FROM_EXPENSE_SCREEN";

	public static final String YOUTUBE_API_KEY = "AIzaSyDY-DV5jz11iahZuIFGWcxQWtA37MgoSIA";

	private Context mContext;
	private String nextStep;
	private boolean offerActive;

	public IncreaseLimitController() {
	}

	public IncreaseLimitController(Context context) {
		this.mContext = context;
	}

	public static boolean editTextLength(String value) {
		return value.length() > 0;
	}

	public static void showKeyboard(WEditTextView wEditTextView, Context context) {
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		assert imm != null;
		imm.showSoftInput(wEditTextView, InputMethodManager.HIDE_NOT_ALWAYS);
	}

	public static String removeNonDigit(WEditTextView view) {
		return view.getText().toString().replaceAll("\\D+", "");
	}

	public void populateExpenseField(WEditTextView wEditText, WTextView wTextView, Context context) {
		wEditText.requestFocus();
		showKeyboard(wEditText, context);
		wTextView.setVisibility(View.VISIBLE);
		wEditText.setCursorVisible(true);
		wEditText.setSelection(wEditText.getText().length());
	}

	public void populateExpenseField(WEditTextView editTextView, String value, WTextView wTextView) {
		wTextView.setVisibility(View.VISIBLE);
		editTextView.setText(value);
		editTextView.clearFocus();
		editTextView.setSelection(editTextView.getText().length());
	}

	public void cliDefaultView(LinearLayout llCommonLayer, WTextView tvIncreaseLimitDescription) {
		hideView(llCommonLayer);
		hideView(tvIncreaseLimitDescription);
	}

	public void accountCLIStatus(LinearLayout llCommonLayer, WTextView tvIncreaseLimit, WTextView tvApplyNowIncreaseLimit, WTextView tvIncreaseLimitDescription, ImageView logoIncreaseLimit, OfferActive offerActive) {
		tvIncreaseLimit.setText(getString(R.string.increase_limit));
		hideView(llCommonLayer);
		hideView(tvIncreaseLimitDescription);
		boolean activeOffer = false;
		String nextStep = "";
		String messageSummary = "";
		String messageDetail = "";
		if (offerActive != null) {
			nextStep = offerActive.nextStep;
			messageSummary = offerActive.messageSummary;
			messageDetail = offerActive.messageDetail;
			setNextStep(nextStep);
			if (offerActive.offerActive != null) {
				activeOffer = offerActive.offerActive;
			}
			if (TextUtils.isEmpty(messageSummary)) {
				messageSummary = "";
				messageDetail = "";
			}
			if (TextUtils.isEmpty(nextStep)) {
				nextStep = "";
			}
		} else {
			return;
		}
		setOfferActive(activeOffer);
		if (nextStep.equalsIgnoreCase(getString(R.string.status_consents))) {
			hideView(logoIncreaseLimit);
			showView(llCommonLayer);
			setCLITag(messageSummary, offerActive.nextStepColour, tvApplyNowIncreaseLimit);
			tvIncreaseLimit.setText(getString(R.string.cli_credit_limit_increase));
			displayDescription(tvIncreaseLimitDescription, messageDetail);
		} else if (nextStep.equalsIgnoreCase(getString(R.string.status_i_n_e))) {
			showView(logoIncreaseLimit);
			cliIcon(logoIncreaseLimit);
			hideView(llCommonLayer);
			displayDescription(tvIncreaseLimitDescription, messageDetail);
			setCLITag(messageSummary, offerActive.nextStepColour, tvApplyNowIncreaseLimit);
		} else if (nextStep.equalsIgnoreCase(getString(R.string.status_offer))) {
			showView(logoIncreaseLimit);
			cliIcon(logoIncreaseLimit);
			hideView(llCommonLayer);
			displayDescription(tvIncreaseLimitDescription, messageDetail);
			setCLITag(messageSummary, offerActive.nextStepColour, tvApplyNowIncreaseLimit);

		} else if (nextStep.equalsIgnoreCase(getString(R.string.status_poi_required))) {
			showView(logoIncreaseLimit);
			cliIcon(logoIncreaseLimit);
			hideView(llCommonLayer);
			displayDescription(tvIncreaseLimitDescription, messageDetail);
			setCLITag(messageSummary, offerActive.nextStepColour, tvApplyNowIncreaseLimit);
		} else if (nextStep.equalsIgnoreCase(getString(R.string.decline))) {
			showView(logoIncreaseLimit);
			cliIcon(logoIncreaseLimit);
			hideView(llCommonLayer);
			displayDescription(tvIncreaseLimitDescription, messageDetail);
			setCLITag(messageSummary, offerActive.nextStepColour, tvApplyNowIncreaseLimit);
		} else if (nextStep.equalsIgnoreCase(getString(R.string.status_contact_us))) {
			showView(logoIncreaseLimit);
			cliIcon(logoIncreaseLimit);
			hideView(llCommonLayer);
			setCLITag(messageSummary, offerActive.nextStepColour, tvApplyNowIncreaseLimit);
			displayDescription(tvIncreaseLimitDescription, messageDetail);
		} /*else if (nextStep.equalsIgnoreCase(getString(R.string.status_poi_problem))) {
			showView(logoIncreaseLimit);
			cliIcon(logoIncreaseLimit);
			hideView(llCommonLayer);
			hideView(tvIncreaseLimitDescription);
			setCLITag(messageSummary, offerActive.nextStepColour, tvApplyNowIncreaseLimit);
		}*/ else if (nextStep.equalsIgnoreCase(getString(R.string.status_in_progress))) {
			showView(logoIncreaseLimit);
			cliIcon(logoIncreaseLimit);
			hideView(tvIncreaseLimitDescription);
			hideView(llCommonLayer);
			setCLITag(messageSummary, offerActive.nextStepColour, tvApplyNowIncreaseLimit);
			displayDescription(tvIncreaseLimitDescription, messageDetail);
		} /*else if (messageSummary.equalsIgnoreCase(getString(R.string.status_document_required))) {
			showView(logoIncreaseLimit);
			cliIcon(logoIncreaseLimit);
			hideView(llCommonLayer);
			setCLITag(messageSummary, offerActive.nextStepColour, tvApplyNowIncreaseLimit);
			displayDescription(tvIncreaseLimitDescription, messageDetail);
		}*/ else if (nextStep.equalsIgnoreCase(getString(R.string.status_complete))) {
			showView(logoIncreaseLimit);
			cliIcon(logoIncreaseLimit);
			hideView(llCommonLayer);
			setCLITag(messageSummary, offerActive.nextStepColour, tvApplyNowIncreaseLimit);
			displayDescription(tvIncreaseLimitDescription, messageDetail);
		} else {
			showView(logoIncreaseLimit);
			cliIcon(logoIncreaseLimit);
			hideView(llCommonLayer);
			hideView(tvIncreaseLimitDescription);
			messageSummary = getString(R.string.status_unavailable);
			setCLITag(messageSummary, R.drawable.status_grey, tvApplyNowIncreaseLimit);
		}
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


	private String getString(int stringId) {
		return mContext.getResources().getString(stringId);
	}

	private void setCLITag(String messageSummary, String nextStepColour, WTextView tvApplyNowIncreaseLimit) {
		tvApplyNowIncreaseLimit.setVisibility(View.VISIBLE);
		setStatusText(messageSummary, tvApplyNowIncreaseLimit);
		roundCornerDrawable(tvApplyNowIncreaseLimit, nextStepColour);
		tvApplyNowIncreaseLimit.setText(messageSummary);
	}

	private void setCLITag(String messageSummary, int nextStepColour, WTextView tvApplyNowIncreaseLimit) {
		tvApplyNowIncreaseLimit.setVisibility(View.VISIBLE);
		setStatusText(messageSummary, tvApplyNowIncreaseLimit);
		tvApplyNowIncreaseLimit.setBackground(ContextCompat.getDrawable(tvApplyNowIncreaseLimit.getContext(), nextStepColour));
		tvApplyNowIncreaseLimit.setText(messageSummary);
	}

	private void moveToCLIPhase(OfferActive offerActive, String productOfferingId) {
		AppCompatActivity activity = (AppCompatActivity) mContext;
		((WoolworthsApplication) activity.getApplication()).setProductOfferingId(Integer.valueOf(productOfferingId));
		Intent openCLIIncrease = new Intent(activity, CLIPhase2Activity.class);
		openCLIIncrease.putExtra("OFFER_ACTIVE_PAYLOAD", Utils.objectToJson(offerActive));
		openCLIIncrease.putExtra("OFFER_IS_ACTIVE", offerIsActive());
		activity.startActivityForResult(openCLIIncrease, 0);
		activity.overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
	}

	public static void populateExpenseField(WEditTextView wEditText, Context context) {
		wEditText.requestFocus();
		showKeyboard(wEditText, context);
	}

	private String getNextStep() {
		return nextStep;
	}

	private void setNextStep(String nextStep) {
		this.nextStep = nextStep;
	}

	private boolean offerIsActive() {
		return offerActive;
	}

	public void setOfferActive(boolean offerActive) {
		this.offerActive = offerActive;
	}

	public void nextStep(OfferActive offerActive, String productOfferingId) {
		try {
			String nextStep = getNextStep();
			if (nextStep.equalsIgnoreCase(getString(R.string.status_consents))) {
				moveToCLIPhase(offerActive, productOfferingId);
			} else if ((nextStep.equalsIgnoreCase(getString(R.string.status_in_progress)))
					|| nextStep.equalsIgnoreCase(getString(R.string.status_decline))
					|| nextStep.equalsIgnoreCase(getString(R.string.status_unavailable))
					|| nextStep.equalsIgnoreCase(getString(R.string.status_contact_us))
					|| (nextStep.equalsIgnoreCase(getString(R.string.status_complete)) && !offerActive.cliStatus.equalsIgnoreCase(getString(R.string.cli_status_concluded)))) {
				// Do nothing
			} else {
				moveToCLIPhase(offerActive, productOfferingId);
			}
		} catch (NullPointerException ignored) {
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

	public HashMap<String, String> incomeHashMap(OfferActive offerActive) {
		Application application = getApplication(offerActive);
		HashMap<String, String> incomeHashMap = new HashMap<>();
		String grossMonthlyIncomeAmount = nullToEmpty(application.grossMonthlyIncome);
		String netMonthlyIncomeAmount = nullToEmpty(application.netMonthlyIncome);
		String additionalIncomeAmount = nullToEmpty(application.additionalIncomeAmount);
		incomeHashMap.put("GROSS_MONTHLY_INCOME", grossMonthlyIncomeAmount);
		incomeHashMap.put("NET_MONTHLY_INCOME", netMonthlyIncomeAmount);
		incomeHashMap.put("ADDITIONAL_MONTHLY_INCOME", additionalIncomeAmount);
		return incomeHashMap;
	}

	public HashMap<String, String> expenseHashMap(OfferActive offerActive) {
		Application application = getApplication(offerActive);
		HashMap<String, String> expenseHashMap = new HashMap<>();
		String mortgagePaymentAmount = nullToEmpty(application.mortgagePaymentAmount);
		String rentalPaymentAmount = nullToEmpty(application.rentalPaymentAmount);
		String maintenanceExpenseAmount = nullToEmpty(application.maintenanceExpenseAmount);
		String totalCreditExpenseAmount = nullToEmpty(application.totalCreditExpenseAmount);
		String otherExpenseAmount = nullToEmpty(application.otherExpenseAmount);
		expenseHashMap.put("MORTGAGE_PAYMENTS", mortgagePaymentAmount);
		expenseHashMap.put("RENTAL_PAYMENTS", rentalPaymentAmount);
		expenseHashMap.put("MAINTENANCE_EXPENSES", maintenanceExpenseAmount);
		expenseHashMap.put("MONTHLY_CREDIT_EXPENSES", totalCreditExpenseAmount);
		expenseHashMap.put("OTHER_EXPENSES", otherExpenseAmount);
		return expenseHashMap;
	}

	private Application getApplication(OfferActive offerActive) {
		return offerActive.application;
	}

	private String toString(int value) {
		return String.valueOf(value);
	}

	public void disableView(View v) {
		v.setEnabled(false);
	}

	public void enableView(View v) {
		v.setEnabled(true);
	}

	private void cliIcon(ImageView logoIncreaseLimit) {
		logoIncreaseLimit.setImageResource(R.drawable.cli);
	}

	public int getScreenHeight(Activity activity) {
		Display display = activity.getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		return size.y;
	}

	public void setQuarterHeight(View view) {
		ViewGroup.LayoutParams params = view.getLayoutParams();
		params.height = getScreenHeight((Activity) mContext) / 4;
	}

	public void intentFindOutMore(Activity activity, OfferActive offerActive) {
		Intent openFindOutMore = new Intent(activity, FindOutMoreActivity.class);
		if (activity instanceof MyAccountCardsActivity) {
			openFindOutMore.putExtra("OFFER_ACTIVE_PAYLOAD", Utils.objectToJson(offerActive));
			openFindOutMore.putExtra("OFFER_IS_ACTIVE", offerActive.offerActive);
			activity.startActivity(openFindOutMore);
			activity.overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
		}
	}

	private void displayDescription(WTextView view, String messageDetail) {
		view.setVisibility(View.VISIBLE);
		view.setText(messageDetail);
	}

	public static void roundCornerDrawable(View v, String color) {
		if (TextUtils.isEmpty(color))
			return;

		int paddingDp = (int) (12 * v.getContext().getResources().getDisplayMetrics().density);

		GradientDrawable shape = new GradientDrawable();
		shape.setShape(GradientDrawable.RECTANGLE);
		shape.setCornerRadii(new float[]{paddingDp, paddingDp, paddingDp, paddingDp, paddingDp, paddingDp, paddingDp, paddingDp});
		shape.setColor(Color.parseColor(color));
		shape.setStroke(2, Color.parseColor(color));
		v.setBackground(shape);
	}

	private String nullToEmpty(Integer amount) {
		return amount == null ? "" : String.valueOf(amount);
	}

	/**
	 * Hides the soft keyboard
	 */
	public void hideSoftKeyboard(Context context) {
		Activity activity = (Activity) context;
		View focus = activity.getCurrentFocus();
		if (focus != null) {
			InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputMethodManager.hideSoftInputFromWindow(focus.getWindowToken(), 0);
		}
	}

	public void dynamicLayoutPadding(View view) {
		Context context = mContext;
		Activity activity = (Activity) context;
		if (activity != null) {
			int screenHeight = getScreenHeight(activity) / 4;
			view.setPadding(0, 0, 0, screenHeight);
		}
	}
}
