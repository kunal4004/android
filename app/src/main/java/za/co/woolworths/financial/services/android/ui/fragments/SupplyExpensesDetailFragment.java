package za.co.woolworths.financial.services.android.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.awfs.coordination.R;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.LinkedHashMap;

import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpDialogManager;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WEditTextView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.CurrencyTextWatcher;
import za.co.woolworths.financial.services.android.util.StepIndicatorCallback;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.controller.IncreaseLimitController;


public class SupplyExpensesDetailFragment extends Fragment implements View.OnClickListener {

	private StepIndicatorCallback mStepIndicatorCallback;
	private View rootView;
	private HashMap<String, String> mHashIncomeDetail, mHashExpenseDetail;
	private WTextView tvMortgagePayments, tvRentalPayments, tvMaintainanceExpenses, tvMonthlyCreditPayments;
	private WEditTextView etMortgagePayments, etRentalPayments, etMaintainanceExpenses, etMonthlyCreditPayments;
	private boolean etMortgagePaymentsWasEdited, etRentalPaymentsWasEdited, etMaintainanceExpensesWasEdited, etMonthlyCreditPaymentsWasEdited;
	private LinearLayout llNextButtonLayout, llMortgagePayment, llRentalPayment, llMaintainanceExpenses, llMonthlyCreditPayments;
	private WButton btnContinue;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.supply_expense_detail_fragment, container, false);
		}
		return rootView;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mHashExpenseDetail = new HashMap<>();
		Bundle b = this.getArguments();
		if (b.getSerializable("INCOME_DETAILS") != null) {
			mHashIncomeDetail = (HashMap<String, String>) b.getSerializable("INCOME_DETAILS");
		}
		init(view);
		nextFocusEditText();
		mStepIndicatorCallback.onCurrentStep(2);
	}

	private void init(View view) {

		ImageView imInfo = (ImageView) view.findViewById(R.id.imInfo);
		imInfo.setOnClickListener(this);

		llMortgagePayment = (LinearLayout) view.findViewById(R.id.llMortgagePayment);
		llRentalPayment = (LinearLayout) view.findViewById(R.id.llRentalPayment);
		llMaintainanceExpenses = (LinearLayout) view.findViewById(R.id.llMaintainanceExpenses);
		llMonthlyCreditPayments = (LinearLayout) view.findViewById(R.id.llMonthlyCreditPayments);
		llNextButtonLayout = (LinearLayout) view.findViewById(R.id.llNextButtonLayout);

		llMortgagePayment.setOnClickListener(this);
		llRentalPayment.setOnClickListener(this);
		llMaintainanceExpenses.setOnClickListener(this);
		llMonthlyCreditPayments.setOnClickListener(this);
		llNextButtonLayout.setOnClickListener(this);

		etMortgagePayments = (WEditTextView) view.findViewById(R.id.etMortgagePayments);
		etRentalPayments = (WEditTextView) view.findViewById(R.id.etRentalPayments);
		etMaintainanceExpenses = (WEditTextView) view.findViewById(R.id.etMaintainanceExpenses);
		etMonthlyCreditPayments = (WEditTextView) view.findViewById(R.id.etMonthlyCreditPayments);

		etMortgagePayments.addTextChangedListener(new CurrencyTextWatcher(etMortgagePayments));
		etRentalPayments.addTextChangedListener(new CurrencyTextWatcher(etRentalPayments));
		etMaintainanceExpenses.addTextChangedListener(new CurrencyTextWatcher(etMaintainanceExpenses));
		etMonthlyCreditPayments.addTextChangedListener(new CurrencyTextWatcher(etMonthlyCreditPayments));

		etMortgagePayments.addTextChangedListener(new GenericTextWatcher(etMortgagePayments));
		etRentalPayments.addTextChangedListener(new GenericTextWatcher(etRentalPayments));
		etMaintainanceExpenses.addTextChangedListener(new GenericTextWatcher(etMaintainanceExpenses));
		etMonthlyCreditPayments.addTextChangedListener(new GenericTextWatcher(etMonthlyCreditPayments));

		tvMortgagePayments = (WTextView) view.findViewById(R.id.tvMortgagePayments);
		tvRentalPayments = (WTextView) view.findViewById(R.id.tvRentalPayments);
		tvMaintainanceExpenses = (WTextView) view.findViewById(R.id.tvMaintainanceExpenses);
		tvMonthlyCreditPayments = (WTextView) view.findViewById(R.id.tvMonthlyCreditPayments);

		llNextButtonLayout = (LinearLayout) view.findViewById(R.id.llNextButtonLayout);
		llNextButtonLayout.setOnClickListener(this);

		btnContinue = (WButton) view.findViewById(R.id.btnContinue);
		btnContinue.setOnClickListener(this);
		btnContinue.setText(getActivity().getResources().getString(R.string.next));
	}

	@Override
	public void onClick(View v) {
		llMortgagePayment.setOnClickListener(this);
		llRentalPayment.setOnClickListener(this);
		llMaintainanceExpenses.setOnClickListener(this);
		llMonthlyCreditPayments.setOnClickListener(this);
		llNextButtonLayout.setOnClickListener(this);

		switch (v.getId()) {
			case R.id.llMortgagePayment:
				IncreaseLimitController.focusEditView(etMortgagePayments, tvMortgagePayments, getActivity());
				break;

			case R.id.llRentalPayment:
				IncreaseLimitController.focusEditView(etRentalPayments, tvRentalPayments, getActivity());
				break;
			case R.id.llMaintainanceExpenses:
				IncreaseLimitController.focusEditView(etMaintainanceExpenses, tvMaintainanceExpenses, getActivity());
				break;
			case R.id.llMonthlyCreditPayments:
				IncreaseLimitController.focusEditView(etMonthlyCreditPayments, tvMonthlyCreditPayments, getActivity());
				break;

			case R.id.imInfo:
				String[] arrTitle = getResources().getStringArray(R.array.supply_info_expenses_title);
				String[] arrDescription = getResources().getStringArray(R.array.supply_info_expenses_desc);

				LinkedHashMap<String, String> incomeMap = new LinkedHashMap<>();
				for (int position = 0; position < arrTitle.length; position++) {
					incomeMap.put(arrTitle[position], arrDescription[position]);
				}

				Utils.displayValidationMessage(getActivity(),
						CustomPopUpDialogManager.VALIDATION_MESSAGE_LIST.SUPPLY_DETAIL_INFO, new Gson().toJson(incomeMap, LinkedHashMap.class));
				break;

			case R.id.llNextButtonLayout:
			case R.id.btnContinue:
				mHashExpenseDetail.put("MORTGAGE_PAYMENTS", IncreaseLimitController.removeNonDigit(etMortgagePayments));
				mHashExpenseDetail.put("RENTAL_PAYMENTS", IncreaseLimitController.removeNonDigit(etRentalPayments));
				mHashExpenseDetail.put("MAINTENANCE_EXPENSES", IncreaseLimitController.removeNonDigit(etMaintainanceExpenses));
				mHashExpenseDetail.put("MONTHLY_CREDIT_EXPENSES", IncreaseLimitController.removeNonDigit(etMonthlyCreditPayments));
				Bundle bundle = new Bundle();
				bundle.putSerializable("INCOME_DETAILS", mHashIncomeDetail);
				bundle.putSerializable("EXPENSE_DETAILS", mHashExpenseDetail);
				OfferCalculationFragment ocFragment = new OfferCalculationFragment();
				ocFragment.setStepIndicatorCallback(mStepIndicatorCallback);
				ocFragment.setArguments(bundle);
				getFragmentManager().beginTransaction()
						.setCustomAnimations(R.anim.slide_from_right, R.anim.slide_to_left, R.anim.slide_from_left, R.anim.slide_to_right)
						.replace(R.id.cli_steps_container, ocFragment).addToBackStack(null).commit();
				break;
		}
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
				case R.id.etMortgagePayments:
					etMortgagePaymentsWasEdited = IncreaseLimitController.editTextLength(currentAmount);
					enableNextButton();
					break;
				case R.id.etRentalPayments:
					etRentalPaymentsWasEdited = IncreaseLimitController.editTextLength(currentAmount);
					enableNextButton();
					break;
				case R.id.etMaintainanceExpenses:
					etMaintainanceExpensesWasEdited = IncreaseLimitController.editTextLength(currentAmount);
					enableNextButton();
					break;
				case R.id.etMonthlyCreditPayments:
					etMonthlyCreditPaymentsWasEdited = IncreaseLimitController.editTextLength(currentAmount);
					enableNextButton();
					break;
			}
		}
	}


	private void nextFocusEditText() {
		etMortgagePayments.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView exampleView, int actionId, KeyEvent event) {

				if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_GO || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
					llRentalPayment.performClick();
					return true;
				} else {
					return false;
				}
			}
		});
		etRentalPayments.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView exampleView, int actionId, KeyEvent event) {

				if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_GO || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
					llMaintainanceExpenses.performClick();
					return true;
				} else {
					return false;
				}
			}
		});
		etMaintainanceExpenses.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView exampleView, int actionId, KeyEvent event) {

				if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_GO || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
					llMonthlyCreditPayments.performClick();
					return true;
				} else {
					return false;
				}
			}
		});
	}

	private void enableNextButton() {
		if (etMortgagePaymentsWasEdited
				&& etRentalPaymentsWasEdited
				&& etMaintainanceExpensesWasEdited
				&& etMonthlyCreditPaymentsWasEdited) {
			llNextButtonLayout.setVisibility(View.VISIBLE);
		} else {
			llNextButtonLayout.setVisibility(View.GONE);
		}
	}

	public void setStepIndicatorCallback(StepIndicatorCallback mStepIndicatorCallback) {
		this.mStepIndicatorCallback = mStepIndicatorCallback;
	}
}