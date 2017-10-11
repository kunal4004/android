package za.co.woolworths.financial.services.android.util.controller;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.views.WEditTextView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;

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
								   ImageView logoIncreaseLimit, OfferStatus status) {
		switch (status) {
			case APPLY_NOW:
				tvIncreaseLimit.setText(getString(R.string.cli_credit_limit_increase));
				hideView(logoIncreaseLimit);
				showView(llCommonLayer);
				setCLITag(R.string.apply_now, R.drawable.cli_round_apply_now_tag, tvApplyNowIncreaseLimit);
				break;

			case OFFER_AVAILABLE:
				showView(logoIncreaseLimit);
				hideView(llCommonLayer);
				setCLITag(R.string.offer_available, R.drawable.cli_round_offer_available, tvApplyNowIncreaseLimit);
				break;

			case IN_PROGRESS:
				showView(logoIncreaseLimit);
				hideView(llCommonLayer);
				setCLITag(R.string.status_in_progress, R.drawable.cli_round_inprogress_tag, tvApplyNowIncreaseLimit);
				break;

			case PLEASE_TRY_AGAIN:
				showView(logoIncreaseLimit);
				hideView(llCommonLayer);
				setCLITag(R.string.status_please_try_again, R.drawable.cli_round_inprogress_tag, tvApplyNowIncreaseLimit);
				break;

			case POI_REQUIRED:
				showView(logoIncreaseLimit);
				hideView(llCommonLayer);
				setCLITag(R.string.status_poi_required, R.drawable.cli_round_inprogress_tag, tvApplyNowIncreaseLimit);
				break;

			case POI_PROBLEM:
				showView(logoIncreaseLimit);
				hideView(llCommonLayer);
				setCLITag(R.string.status_poi_problem, R.drawable.cli_round_offer_poi_problem, tvApplyNowIncreaseLimit);
				break;

			case UNAVAILABLE:
				showView(logoIncreaseLimit);
				hideView(llCommonLayer);
				setCLITag(R.string.status_unavailable, R.drawable.cli_round_offer_unavailable, tvApplyNowIncreaseLimit);
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

	private void setStatusText(int id, WTextView wTextView) {
		wTextView.setText(getString(id));
	}

	private void setStatusBackground(int drawable, WTextView wTextView) {
		wTextView.setBackgroundResource(drawable);
	}

	private String getString(int stringId) {
		return mContext.getResources().getString(stringId);
	}

	private void setCLITag(int stringId, int drawableId, WTextView tvApplyNowIncreaseLimit) {
		setStatusText(stringId, tvApplyNowIncreaseLimit);
		setStatusBackground(drawableId, tvApplyNowIncreaseLimit);
	}
}
