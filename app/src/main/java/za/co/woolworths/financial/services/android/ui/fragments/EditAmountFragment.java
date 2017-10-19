package za.co.woolworths.financial.services.android.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.activities.CLIPhase2Activity;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpDialogManager;
import za.co.woolworths.financial.services.android.ui.views.WLoanEditTextView;
import za.co.woolworths.financial.services.android.util.CurrencyTextWatcher;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.controller.CLIFragment;

public class EditAmountFragment extends CLIFragment {

	private View view;
	private WLoanEditTextView etAmount;
	private int creditReqestMin = 0;
	private int creditRequestMax = 0;
	private int currCredit = 0;

	public EditAmountFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
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
			creditReqestMin = args.getInt("creditReqestMin");
			creditRequestMax = args.getInt("creditRequestMax");
			currCredit = args.getInt("currCredit");

			etAmount.setText(String.valueOf(slideAmount));
			etAmount.setSelection(etAmount.getText().toString().length());
		}
	}

	private void listener() {
		etAmount.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					retrieveNumber(etAmount.getText().toString());
					return true;
				}
				return false;
			}
		});

		WLoanEditTextView.OnKeyPreImeListener onKeyPreImeListener =
				new WLoanEditTextView.OnKeyPreImeListener() {
					@Override
					public void onBackPressed() {
						getActivity().onBackPressed();
					}
				};

		etAmount.setOnKeyPreImeListener(onKeyPreImeListener);
	}

	private void retrieveNumber(String number) {
		int newAmount = Utils.numericFieldOnly(number);
		if (newAmount < creditReqestMin) {
			Utils.displayValidationMessage(getActivity(), CustomPopUpDialogManager.VALIDATION_MESSAGE_LIST.ERROR, "newAmount < creditReqestMin");
		} else if (newAmount > creditRequestMax) {
			Utils.displayValidationMessage(getActivity(), CustomPopUpDialogManager.VALIDATION_MESSAGE_LIST.ERROR, "newAmount > creditRequestMax");
		} else {
			int progressValue = Utils.numericFieldOnly(etAmount.getText().toString()) - creditReqestMin;
			((CLIPhase2Activity) getActivity()).setEditNumberValue(progressValue);
			FragmentManager fm = getActivity().getSupportFragmentManager();
			fm.popBackStack(EditAmountFragment.class.getSimpleName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
		}
	}

	private void init() {
		etAmount = (WLoanEditTextView) view.findViewById(R.id.etAmount);
		etAmount.requestFocus();
		forceKeyboard(etAmount);
	}

	private void forceKeyboard(WLoanEditTextView etAmount) {
		InputMethodManager imm = (InputMethodManager) EditAmountFragment.this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(etAmount, InputMethodManager.SHOW_IMPLICIT);
		EditAmountFragment.this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
		etAmount.addTextChangedListener(new CurrencyTextWatcher(etAmount));
	}

	@Override
	public void onResume() {
		super.onResume();
		try {
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					etAmount.requestFocus();
					forceKeyboard(etAmount);
					etAmount.setSelection(etAmount.getText().toString().length());
				}
			});
		} catch (Exception ex) {
		}

		if (etAmount != null)
			etAmount.requestFocus();
	}
}
