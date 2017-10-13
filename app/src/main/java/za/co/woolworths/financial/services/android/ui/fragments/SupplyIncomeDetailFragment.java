package za.co.woolworths.financial.services.android.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.awfs.coordination.R;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.LinkedHashMap;

import za.co.woolworths.financial.services.android.ui.activities.CLIPhase2Activity;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpDialogManager;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WEditTextView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.CurrencyTextWatcher;
import za.co.woolworths.financial.services.android.util.FragmentUtils;
import za.co.woolworths.financial.services.android.util.MultiClickPreventer;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.controller.CLIFragment;
import za.co.woolworths.financial.services.android.util.controller.IncreaseLimitController;

public class SupplyIncomeDetailFragment extends CLIFragment implements View.OnClickListener {

	private boolean grossMonthlyIncomeWasEdited, netMonthlyIncomeWasEdited, additionalMonthlyIncomeWasEdited;
	private WEditTextView etGrossMonthlyIncome, etNetMonthlyIncome, etAdditionalMonthlyIncome;
	private WTextView tvGrossMonthlyIncome, tvNetMonthlyIncome, tvAdditionalMonthlyIncome;
	private LinearLayout llNextButtonLayout, llGrossMonthlyIncomeLayout, llNetMonthlyIncomeLayout, llAdditionalMonthlyIncomeLayout;
	private WButton btnContinue;
	private View rootView;
	private CLIPhase2Activity mCliPhase2Activity;
	private HashMap<String, String> hmExpenseDetail;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.supply_income_detail_fragment, container, false);
		}
		return rootView;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		init(view);
		nextFocusEditText();
		cliStepIndicatorListener.onStepSelected(1);
		Bundle b = this.getArguments();
		if (b != null) {
			HashMap<String, String> mHashIncomeDetail = (HashMap<String, String>) b.getSerializable(IncreaseLimitController.INCOME_DETAILS);
			hmExpenseDetail = (HashMap<String, String>) b.getSerializable(IncreaseLimitController.EXPENSE_DETAILS);

			etGrossMonthlyIncome.setText(mHashIncomeDetail.get("GROSS_MONTHLY_INCOME"));
			etNetMonthlyIncome.setText(mHashIncomeDetail.get("NET_MONTHLY_INCOME"));
			etAdditionalMonthlyIncome.setText(mHashIncomeDetail.get("ADDITIONAL_MONTHLY_INCOME"));
			llGrossMonthlyIncomeLayout.performClick();
			llNetMonthlyIncomeLayout.performClick();
			llAdditionalMonthlyIncomeLayout.performClick();
			etGrossMonthlyIncome.clearFocus();
			etNetMonthlyIncome.clearFocus();
			etAdditionalMonthlyIncome.clearFocus();
		}
		getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}

	private void init(View view) {

		mCliPhase2Activity = (CLIPhase2Activity) getActivity();
		mCliPhase2Activity.actionBarBackIcon();

		llGrossMonthlyIncomeLayout = (LinearLayout) view.findViewById(R.id.llGrossMonthlyIncomeLayout);
		llNetMonthlyIncomeLayout = (LinearLayout) view.findViewById(R.id.llNetMonthlyIncomeLayout);
		llAdditionalMonthlyIncomeLayout = (LinearLayout) view.findViewById(R.id.llAdditionalMonthlyIncomeLayout);

		llGrossMonthlyIncomeLayout.setOnClickListener(this);
		llNetMonthlyIncomeLayout.setOnClickListener(this);
		llAdditionalMonthlyIncomeLayout.setOnClickListener(this);

		ImageView imInfo = (ImageView) view.findViewById(R.id.imInfo);

		imInfo.setOnClickListener(this);

		etGrossMonthlyIncome = (WEditTextView) view.findViewById(R.id.etGrossMonthlyIncome);
		etNetMonthlyIncome = (WEditTextView) view.findViewById(R.id.etNetMonthlyIncome);
		etAdditionalMonthlyIncome = (WEditTextView) view.findViewById(R.id.etAdditionalMonthlyIncome);

		etGrossMonthlyIncome.addTextChangedListener(new CurrencyTextWatcher(etGrossMonthlyIncome));
		etNetMonthlyIncome.addTextChangedListener(new CurrencyTextWatcher(etNetMonthlyIncome));
		etAdditionalMonthlyIncome.addTextChangedListener(new CurrencyTextWatcher(etAdditionalMonthlyIncome));

		etGrossMonthlyIncome.addTextChangedListener(new GenericTextWatcher(etGrossMonthlyIncome));
		etNetMonthlyIncome.addTextChangedListener(new GenericTextWatcher(etNetMonthlyIncome));
		etAdditionalMonthlyIncome.addTextChangedListener(new GenericTextWatcher(etAdditionalMonthlyIncome));

		tvGrossMonthlyIncome = (WTextView) view.findViewById(R.id.tvGrossMonthlyIncome);
		tvNetMonthlyIncome = (WTextView) view.findViewById(R.id.tvNetMonthlyIncome);
		tvAdditionalMonthlyIncome = (WTextView) view.findViewById(R.id.tvAdditionalMonthlyIncome);

		llNextButtonLayout = (LinearLayout) view.findViewById(R.id.llNextButtonLayout);
		llNextButtonLayout.setOnClickListener(this);

		btnContinue = (WButton) view.findViewById(R.id.btnContinue);
		btnContinue.setOnClickListener(this);
		btnContinue.setText(getActivity().getResources().getString(R.string.next));
	}

	private class GenericTextWatcher implements TextWatcher {

		private View view;

		private GenericTextWatcher(View view) {
			this.view = view;
		}

		public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
		}

		public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
		}

		public void afterTextChanged(Editable editable) {
			String currentAmount = editable.toString();
			switch (view.getId()) {
				case R.id.etGrossMonthlyIncome:
					grossMonthlyIncomeWasEdited = IncreaseLimitController.editTextLength(currentAmount);
					enableNextButton();
					break;
				case R.id.etNetMonthlyIncome:
					netMonthlyIncomeWasEdited = IncreaseLimitController.editTextLength(currentAmount);
					enableNextButton();
					break;
				case R.id.etAdditionalMonthlyIncome:
					additionalMonthlyIncomeWasEdited = IncreaseLimitController.editTextLength(currentAmount);
					enableNextButton();
					break;
			}
		}
	}

	private void enableNextButton() {
		if (grossMonthlyIncomeWasEdited
				&& netMonthlyIncomeWasEdited
				&& additionalMonthlyIncomeWasEdited) {
			llNextButtonLayout.setVisibility(View.VISIBLE);
		} else {
			llNextButtonLayout.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View v) {
		MultiClickPreventer.preventMultiClick(btnContinue);
		switch (v.getId()) {
			case R.id.imInfo:
				String[] arrTitle = getResources().getStringArray(R.array.supply_info_income_title);
				String[] arrDescription = getResources().getStringArray(R.array.supply_info_income_desc);
				LinkedHashMap<String, String> incomeMap = new LinkedHashMap<>();
				for (int position = 0; position < arrTitle.length; position++) {
					incomeMap.put(arrTitle[position], arrDescription[position]);
				}
				Utils.displayValidationMessage(getActivity(),
						CustomPopUpDialogManager.VALIDATION_MESSAGE_LIST.SUPPLY_DETAIL_INFO, new Gson().toJson(incomeMap, LinkedHashMap.class));
				break;
			case R.id.llGrossMonthlyIncomeLayout:
				IncreaseLimitController.focusEditView(etGrossMonthlyIncome, tvGrossMonthlyIncome, getActivity());
				break;

			case R.id.llNetMonthlyIncomeLayout:
				IncreaseLimitController.focusEditView(etNetMonthlyIncome, tvNetMonthlyIncome, getActivity());
				break;

			case R.id.llAdditionalMonthlyIncomeLayout:
				IncreaseLimitController.focusEditView(etAdditionalMonthlyIncome, tvAdditionalMonthlyIncome, getActivity());
				break;

			case R.id.btnContinue:
			case R.id.llNextButtonLayout:
				IncreaseLimitController increaseLimitController = new IncreaseLimitController(getActivity());
				HashMap<String, String> hmIncomeDetail = increaseLimitController.incomeHashMap(etGrossMonthlyIncome, etNetMonthlyIncome, etAdditionalMonthlyIncome);
				Bundle bundle = new Bundle();
				bundle.putSerializable(IncreaseLimitController.INCOME_DETAILS, hmIncomeDetail);
				bundle.putSerializable(IncreaseLimitController.EXPENSE_DETAILS, hmExpenseDetail);
				SupplyExpensesDetailFragment supplyExpensesDetailFragment = new SupplyExpensesDetailFragment();
				supplyExpensesDetailFragment.setArguments(bundle);
				supplyExpensesDetailFragment.setStepIndicatorListener(cliStepIndicatorListener);
				FragmentUtils fragmentUtils = new FragmentUtils();
				fragmentUtils.nextFragment((AppCompatActivity) SupplyIncomeDetailFragment.this.getActivity(), getFragmentManager(), supplyExpensesDetailFragment, R.id.cli_steps_container);
				break;
		}
	}

	private void nextFocusEditText() {
		etGrossMonthlyIncome.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView exampleView, int actionId, KeyEvent event) {

				if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_GO || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
					llNetMonthlyIncomeLayout.performClick();
					return true;
				} else {
					return false;
				}
			}
		});
		etNetMonthlyIncome.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView exampleView, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_GO || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
					llAdditionalMonthlyIncomeLayout.performClick();
					return true;
				} else {
					return false;
				}
			}
		});
	}

}