package za.co.woolworths.financial.services.android.util.controller;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.OfferActive;
import za.co.woolworths.financial.services.android.ui.activities.CLIPhase2Activity;
import za.co.woolworths.financial.services.android.ui.views.WEditTextView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.Utils;

public class IncreaseLimitController {

	private Context mContext;

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

	public static String removeNonDigit(WEditTextView view) {
		return view.getText().toString().replaceAll("\\D+", "");
	}

	public static void focusEditView(WEditTextView wEditText, WTextView wTextView, Context context) {
		wEditText.requestFocus();
		IncreaseLimitController.showKeyboard(wEditText, context);
		wTextView.setVisibility(View.VISIBLE);
	}

	public void offerActiveUIState(LinearLayout llCommonLayer, WTextView tvIncreaseLimit, WTextView tvApplyNowIncreaseLimit,
								   ImageView logoIncreaseLimit, OfferStatus status, OfferActive offerActive) {
		String messageSummary = "";
		try {
			messageSummary = offerActive.cli.messageSummary;
		} catch (Exception ex) {
		}
		if (TextUtils.isEmpty(messageSummary)) {
			messageSummary = status.toString();
		}
		switch (status) {
			case APPLY_NOW:
				hideView(logoIncreaseLimit);
				showView(llCommonLayer);
				setCLITag(messageSummary, R.drawable.cli_round_apply_now_tag, tvApplyNowIncreaseLimit);
				tvIncreaseLimit.setText(getString(R.string.cli_credit_limit_increase));
				break;

			case OFFER_AVAILABLE:
				showView(logoIncreaseLimit);
				hideView(llCommonLayer);
				setCLITag(messageSummary, R.drawable.cli_round_offer_available, tvApplyNowIncreaseLimit);
				break;

			case IN_PROGRESS:
				showView(logoIncreaseLimit);
				hideView(llCommonLayer);
				setCLITag(messageSummary, R.drawable.cli_round_inprogress_tag, tvApplyNowIncreaseLimit);
				break;

			case PLEASE_TRY_AGAIN:
				showView(logoIncreaseLimit);
				hideView(llCommonLayer);
				setCLITag(messageSummary, R.drawable.cli_round_inprogress_tag, tvApplyNowIncreaseLimit);
				break;

			case POI_REQUIRED:
				showView(logoIncreaseLimit);
				hideView(llCommonLayer);
				setCLITag(messageSummary, R.drawable.cli_round_inprogress_tag, tvApplyNowIncreaseLimit);
				break;

			case POI_PROBLEM:
				showView(logoIncreaseLimit);
				hideView(llCommonLayer);
				setCLITag(messageSummary, R.drawable.cli_round_offer_poi_problem, tvApplyNowIncreaseLimit);
				break;

			case UNAVAILABLE:
				showView(logoIncreaseLimit);
				hideView(llCommonLayer);
				setCLITag(messageSummary, R.drawable.cli_round_offer_unavailable, tvApplyNowIncreaseLimit);
				break;
			default:
				break;
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

	private void setStatusBackground(int drawable, WTextView wTextView) {
		wTextView.setBackgroundResource(drawable);
	}

	private String getString(int stringId) {
		return mContext.getResources().getString(stringId);
	}

	private void setCLITag(String messageSummary, int drawableId, WTextView tvApplyNowIncreaseLimit) {
		setStatusText(messageSummary, tvApplyNowIncreaseLimit);
		setStatusBackground(drawableId, tvApplyNowIncreaseLimit);
	}

	public void moveToCLIPhase(OfferActive offerActive, String productOfferingId) {
		AppCompatActivity activity = (AppCompatActivity) mContext;
		if (!offerActive.offerActive) {
			((WoolworthsApplication) activity.getApplication()).setProductOfferingId(Integer.valueOf(productOfferingId));
			Intent openCLIIncrease = new Intent(activity, CLIPhase2Activity.class);
			openCLIIncrease.putExtra("jsonOfferActive", Utils.objectToJson(offerActive));
			activity.startActivity(openCLIIncrease);
			activity.overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
		}
	}
}
