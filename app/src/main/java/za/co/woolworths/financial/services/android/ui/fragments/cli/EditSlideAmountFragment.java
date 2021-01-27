package za.co.woolworths.financial.services.android.ui.fragments.cli;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.awfs.coordination.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.service.event.BusStation;
import za.co.woolworths.financial.services.android.ui.activities.cli.CLIPhase2Activity;
import za.co.woolworths.financial.services.android.ui.views.WLoanEditTextView;
import za.co.woolworths.financial.services.android.util.CurrencyFormatter;
import za.co.woolworths.financial.services.android.util.KeyboardUtil;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WFormatter;
import za.co.woolworths.financial.services.android.util.controller.CLIFragment;

public class EditSlideAmountFragment extends CLIFragment {

	private View view;
	private WLoanEditTextView etAmount;
	private int currentCredit = 0;
	private int creditRequestMax = 0;
	private String TAG = EditSlideAmountFragment.class.getSimpleName();
	private String title;

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
					String slideAmount = etAmount.getText().toString();
					if (!TextUtils.isEmpty(slideAmount)) {
						hideKeyboard();
						retrieveNumber(slideAmount);
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
						KeyboardUtil.hideSoftKeyboard(activity);
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
			minAmountMessage(drawnDownAmount(newAmount),newAmount);
		} else if (newAmount > creditRequestMax) {
			maxAmountMessage(drawnDownAmount(newAmount),newAmount);
		}else {
            setDrawnDownOnSlider(newAmount);
        }
    }

	/**
	 * TODO :: Replace event bus by onActivityForResult()
	 */
	private void setDrawnDownOnSlider(int newAmount) {
		int progressValue = drawnDownAmount(newAmount);
		Activity activity = getActivity();
		if (activity instanceof CLIPhase2Activity) {
			CLIPhase2Activity cliPhase2Activity = ((CLIPhase2Activity) activity);
			((WoolworthsApplication) activity.getApplication())
					.bus()
					.send(new BusStation(progressValue,newAmount));
			FragmentManager fm = cliPhase2Activity.getSupportFragmentManager();
			fm.popBackStack(EditSlideAmountFragment.class.getSimpleName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
		}
	}

	private int drawnDownAmount(int amount){
		//round down to the nearest hundred
		amount -= amount % 100;
		return amount - currentCredit;
	}

    private void init() {
		etAmount = (WLoanEditTextView) view.findViewById(R.id.etAmount);
		etAmount.requestFocus();
		etAmount.addTextChangedListener(onTextChangedListener());

		forceKeyboard(etAmount);
	}

	private void forceKeyboard(WLoanEditTextView etAmount) {
		Activity activity = getActivity();
		if (activity != null) {
			etAmount.requestFocus();
			InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
			assert imm != null;
			activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
			imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
		}
	}

	private void hideKeyboard() {
		try {
			Activity activity = getActivity();
			if (activity != null) {
				View view = activity.getCurrentFocus();
				if (view != null) {
					InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
					assert imm != null;
					imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
				}
			}
		} catch (Exception e) {
			Log.d(TAG,e.getMessage());
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

	private void minAmountMessage(int progressValue, int drawnDownAmount) {
		EnterAmountToSlideFragment minAmountDialog = EnterAmountToSlideFragment.Companion.newInstance(progressValue,drawnDownAmount,title, getString(R.string.amount_too_low_modal_desc).replaceAll("#R", CurrencyFormatter.Companion.escapeDecimalFormat(currentCredit)));
		minAmountDialog.show(getFragmentManager(),EnterAmountToSlideFragment.class.getSimpleName());
	}

	private void maxAmountMessage(int progressValue, int drawnDownAmount) {
		EnterAmountToSlideFragment minAmountDialog = EnterAmountToSlideFragment.Companion.newInstance(progressValue,drawnDownAmount,title, getString(R.string.amount_too_high_modal_desc).replaceAll("#R", CurrencyFormatter.Companion.escapeDecimalFormat(creditRequestMax)));
		minAmountDialog.show(getFragmentManager(),EnterAmountToSlideFragment.class.getSimpleName());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Activity activity = getActivity();
		hideKeyboard();
		if (activity != null) {
			CLIPhase2Activity cliPhase2Activity = (CLIPhase2Activity) activity;
			cliPhase2Activity.actionBarCloseIcon();
		}
	}

	private TextWatcher onTextChangedListener() {
		return new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				etAmount.removeTextChangedListener(this);
				String retrieveDrawnDownAmount = s.toString().replaceAll("\\s+","");
				etAmount.setText(Utils.convertToCurrencyWithoutCent(Long.parseLong(TextUtils.isEmpty(retrieveDrawnDownAmount) ? "0" : retrieveDrawnDownAmount)));
				etAmount.setTextColor((etAmount.getText().toString().equalsIgnoreCase("0")) ? Color.TRANSPARENT : Color.BLACK);
				setFocusToEditText(etAmount.getText().length());
				etAmount.addTextChangedListener(this);
			}
		};
	}

	private void setFocusToEditText(int atPosition) {
		etAmount.setSelection(atPosition);
		etAmount.requestFocus();
		etAmount.setFocusable(true);
		forceKeyboard(etAmount);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		if (getActivity() != null) {
				getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		}
	}
}
