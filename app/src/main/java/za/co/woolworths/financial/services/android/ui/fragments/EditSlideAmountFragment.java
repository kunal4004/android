package za.co.woolworths.financial.services.android.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.service.event.BusStation;
import za.co.woolworths.financial.services.android.ui.activities.CLIPhase2Activity;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.views.WLoanEditTextView;
import za.co.woolworths.financial.services.android.util.CurrencyTextWatcher;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WFormatter;
import za.co.woolworths.financial.services.android.util.controller.CLIFragment;

public class EditSlideAmountFragment extends CLIFragment {

	private View view;
	private WLoanEditTextView etAmount;
	private int currentCredit = 0;
	private int creditRequestMax = 0;
	String title;

	public EditSlideAmountFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		if (view == null) {
			view = inflater.inflate(R.layout.cli_edit_amount_fragment, container, false);
		}
		return view;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		init();
		listener();
		Bundle args = getArguments();
		if (args != null) {
			int slideAmount = args.getInt("slideAmount");
			currentCredit = args.getInt("currentCredit");
			creditRequestMax = args.getInt("creditRequestMax");
			etAmount.setText(String.valueOf(slideAmount));
			etAmount.setSelection(etAmount.getText().toString().length());
			title = getString(R.string.amount_too_low_modal_title);
		}
	}

	private void listener() {
		etAmount.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					hideKeyboard();
					String slideAmount = etAmount.getText().toString();
					if (!TextUtils.isEmpty(slideAmount)) {
						retrieveNumber(slideAmount);
					} else {
						minAmountMessage();
					}
					return true;
				}
				return false;
			}
		});

		WLoanEditTextView.OnKeyPreImeListener onKeyPreImeListener =
				new WLoanEditTextView.OnKeyPreImeListener() {
					@Override
					public void onBackPressed() {
						Activity activity = getActivity();
						if (activity instanceof CLIPhase2Activity) {
							activity.onBackPressed();
						}
					}
				};

		etAmount.setOnKeyPreImeListener(onKeyPreImeListener);
	}

	private void retrieveNumber(String slideAmount) {
		int newAmount = Utils.numericFieldOnly(slideAmount);
		if (newAmount < currentCredit) {
			minAmountMessage();
		} else if (newAmount > creditRequestMax) {
			maxAmountMessage();
		} else {

			//round down to the nearest hundred
			newAmount -= newAmount % 100;
			System.out.println(newAmount);
			int progressValue = (int) newAmount - currentCredit;
			Activity activity = getActivity();
			if (activity != null) {
				if (activity instanceof CLIPhase2Activity) {
					CLIPhase2Activity cliPhase2Activity = ((CLIPhase2Activity) activity);
					((WoolworthsApplication) activity.getApplication())
							.bus()
							.send(new BusStation(progressValue));
					FragmentManager fm = cliPhase2Activity.getSupportFragmentManager();
					fm.popBackStack(EditSlideAmountFragment.class.getSimpleName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
				}
			}
		}
	}

	private void init() {
		etAmount = (WLoanEditTextView) view.findViewById(R.id.etAmount);
		etAmount.requestFocus();
		forceKeyboard(etAmount);
	}

	private void forceKeyboard(WLoanEditTextView etAmount) {
		Activity activity = getActivity();
		if (activity != null) {
			InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
			assert imm != null;
			imm.showSoftInput(etAmount, InputMethodManager.SHOW_IMPLICIT);
			activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
			etAmount.addTextChangedListener(new CurrencyTextWatcher(etAmount));
		}
	}

	private void hideKeyboard() {
		Activity activity = getActivity();
		if (activity != null) {
			View view = activity.getCurrentFocus();
			if (view != null) {
				InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
				assert imm != null;
				imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		try {
			Activity activity = getActivity();
			if (activity != null) {
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						etAmount.requestFocus();
						forceKeyboard(etAmount);
						etAmount.setSelection(etAmount.getText().toString().length());
					}
				});
			}
		} catch (Exception ignored) {
		}

		if (etAmount != null)
			etAmount.requestFocus();
	}

	private void minAmountMessage() {
		Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.AMOUNT_STOCK, title, getString(R.string.amount_too_low_modal_desc).replaceAll("#R", WFormatter.escapeDecimalFormat(currentCredit)));
	}

	private void maxAmountMessage() {
		Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.AMOUNT_STOCK, title, getString(R.string.amount_too_high_modal_desc).replaceAll("#R", WFormatter.escapeDecimalFormat(creditRequestMax)));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Activity activity = getActivity();
		if (activity != null) {
			CLIPhase2Activity cliPhase2Activity = (CLIPhase2Activity) activity;
			cliPhase2Activity.actionBarCloseIcon();
		}
	}
}
