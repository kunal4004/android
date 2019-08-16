package za.co.woolworths.financial.services.android.ui.fragments;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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

import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.ui.activities.cli.CLIPhase2Activity;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.fragments.cli.OfferCalculationFragment;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WEditTextView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.CurrencyTextWatcher;
import za.co.woolworths.financial.services.android.util.FragmentUtils;
import za.co.woolworths.financial.services.android.util.MultiClickPreventer;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.controller.CLIFragment;
import za.co.woolworths.financial.services.android.util.controller.IncreaseLimitController;

public class SupplyExpensesDetailFragment extends CLIFragment implements View.OnClickListener, View.OnFocusChangeListener {
	private View rootView;
	private HashMap<String, String> mHashIncomeDetail, mHashExpenseDetail;
	private WTextView tvMortgagePayments, tvRentalPayments, tvMaintainanceExpenses, tvMonthlyCreditPayments, tvOtherExpenses;
	private WEditTextView etMortgagePayments, etRentalPayments, etMaintainanceExpenses, etMonthlyCreditPayments, etOtherExpenses;
	private boolean etMortgagePaymentsWasEdited, etRentalPaymentsWasEdited, etMaintainanceExpensesWasEdited, etMonthlyCreditPaymentsWasEdited, etOtherExpensesWasEdited;
	private LinearLayout llNextButtonLayout, llMortgagePayment, llRentalPayment, llMaintainanceExpenses, llMonthlyCreditPayments, llOtherExpensesContainer, llSupplyExpenseContainer;
	private NestedScrollView nsSupplyExpense;
	private boolean etOtherExpensesWasTouched = false;
	private IncreaseLimitController mIncreaseLimitController;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.supply_expense_detail_fragment, container, false);
		}
		return rootView;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mIncreaseLimitController = new IncreaseLimitController(getActivity());
		Bundle b = this.getArguments();
		if (b.getSerializable(IncreaseLimitController.INCOME_DETAILS) != null) {
			mHashIncomeDetail = (HashMap<String, String>) b.getSerializable(IncreaseLimitController.INCOME_DETAILS);
			mHashExpenseDetail = (HashMap<String, String>) b.getSerializable(IncreaseLimitController.EXPENSE_DETAILS);
		}
		init(view);
		nextFocusEditText();
		mCliStepIndicatorListener.onStepSelected(2);
		if (mHashExpenseDetail != null) {
			mIncreaseLimitController.populateExpenseField(etMortgagePayments, mHashExpenseDetail.get("MORTGAGE_PAYMENTS"), tvMortgagePayments);
			mIncreaseLimitController.populateExpenseField(etRentalPayments, mHashExpenseDetail.get("RENTAL_PAYMENTS"), tvRentalPayments);
			mIncreaseLimitController.populateExpenseField(etMaintainanceExpenses, mHashExpenseDetail.get("MAINTENANCE_EXPENSES"), tvMaintainanceExpenses);
			mIncreaseLimitController.populateExpenseField(etMonthlyCreditPayments, mHashExpenseDetail.get("MONTHLY_CREDIT_EXPENSES"), tvMonthlyCreditPayments);
			mIncreaseLimitController.populateExpenseField(etOtherExpenses, mHashExpenseDetail.get("OTHER_EXPENSES"), tvOtherExpenses);
		}
		mIncreaseLimitController.dynamicLayoutPadding(llSupplyExpenseContainer);
		Activity activity = getActivity();
		if (activity instanceof CLIPhase2Activity) {
			activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		}
		llMortgagePayment.requestFocus();
	}

	private void init(View view) {

		ImageView imInfo = (ImageView) view.findViewById(R.id.imInfo);
		imInfo.setOnClickListener(this);

		llMortgagePayment = (LinearLayout) view.findViewById(R.id.llMortgagePayment);
		llRentalPayment = (LinearLayout) view.findViewById(R.id.llRentalPayment);
		llMaintainanceExpenses = (LinearLayout) view.findViewById(R.id.llMaintainanceExpenses);
		llMonthlyCreditPayments = (LinearLayout) view.findViewById(R.id.llMonthlyCreditPayments);
		llNextButtonLayout = (LinearLayout) view.findViewById(R.id.llNextButtonLayout);
		llOtherExpensesContainer = (LinearLayout) view.findViewById(R.id.llOtherExpensesContainer);
		llSupplyExpenseContainer = (LinearLayout) view.findViewById(R.id.llSupplyExpenseContainer);
		IncreaseLimitController increaseLimitController = new IncreaseLimitController(getActivity());
		increaseLimitController.setQuarterHeight(view.findViewById(R.id.llEmptyLayout));

		nsSupplyExpense = (NestedScrollView) view.findViewById(R.id.nsSupplyExpense);

		llMortgagePayment.setOnClickListener(this);
		llRentalPayment.setOnClickListener(this);
		llMaintainanceExpenses.setOnClickListener(this);
		llMonthlyCreditPayments.setOnClickListener(this);
		llNextButtonLayout.setOnClickListener(this);
		llOtherExpensesContainer.setOnClickListener(this);

		etMortgagePayments = (WEditTextView) view.findViewById(R.id.etMortgagePayments);
		etRentalPayments = (WEditTextView) view.findViewById(R.id.etRentalPayments);
		etMaintainanceExpenses = (WEditTextView) view.findViewById(R.id.etMaintainanceExpenses);
		etMonthlyCreditPayments = (WEditTextView) view.findViewById(R.id.etMonthlyCreditPayments);
		etOtherExpenses = (WEditTextView) view.findViewById(R.id.etOtherExpenses);

		etMortgagePayments.addTextChangedListener(new CurrencyTextWatcher(etMortgagePayments));
		etRentalPayments.addTextChangedListener(new CurrencyTextWatcher(etRentalPayments));
		etMaintainanceExpenses.addTextChangedListener(new CurrencyTextWatcher(etMaintainanceExpenses));
		etMonthlyCreditPayments.addTextChangedListener(new CurrencyTextWatcher(etMonthlyCreditPayments));
		etOtherExpenses.addTextChangedListener(new CurrencyTextWatcher(etOtherExpenses));

		etMortgagePayments.addTextChangedListener(new GenericTextWatcher(etMortgagePayments));
		etRentalPayments.addTextChangedListener(new GenericTextWatcher(etRentalPayments));
		etMaintainanceExpenses.addTextChangedListener(new GenericTextWatcher(etMaintainanceExpenses));
		etMonthlyCreditPayments.addTextChangedListener(new GenericTextWatcher(etMonthlyCreditPayments));
		etOtherExpenses.addTextChangedListener(new GenericTextWatcher(etOtherExpenses));

		etOtherExpenses.setOnFocusChangeListener(this);

		tvMortgagePayments = (WTextView) view.findViewById(R.id.tvMortgagePayments);
		tvRentalPayments = (WTextView) view.findViewById(R.id.tvRentalPayments);
		tvMaintainanceExpenses = (WTextView) view.findViewById(R.id.tvMaintainanceExpenses);
		tvMonthlyCreditPayments = (WTextView) view.findViewById(R.id.tvMonthlyCreditPayments);
		tvOtherExpenses = (WTextView) view.findViewById(R.id.tvOtherExpenses);

		llNextButtonLayout = (LinearLayout) view.findViewById(R.id.llNextButtonLayout);
		llNextButtonLayout.setOnClickListener(this);

		WButton btnContinue = (WButton) view.findViewById(R.id.btnContinue);
		btnContinue.setOnClickListener(this);
		btnContinue.setText(getActivity().getResources().getString(R.string.next));
	}

	@Override
	public void onClick(View v) {
		MultiClickPreventer.preventMultiClick(v);
		switch (v.getId()) {
			case R.id.llMortgagePayment:
				mIncreaseLimitController.populateExpenseField(etMortgagePayments, tvMortgagePayments, getActivity());
				break;

			case R.id.llRentalPayment:
				mIncreaseLimitController.populateExpenseField(etRentalPayments, tvRentalPayments, getActivity());
				break;

			case R.id.llMaintainanceExpenses:
				mIncreaseLimitController.populateExpenseField(etMaintainanceExpenses, tvMaintainanceExpenses, getActivity());
				break;

			case R.id.llMonthlyCreditPayments:
				mIncreaseLimitController.populateExpenseField(etMonthlyCreditPayments, tvMonthlyCreditPayments, getActivity());
				break;

			case R.id.llOtherExpensesContainer:
				mIncreaseLimitController.populateExpenseField(etOtherExpenses, tvOtherExpenses, getActivity());
				break;

			case R.id.imInfo:
				String[] arrTitle = getResources().getStringArray(R.array.supply_info_expenses_title);
				String[] arrDescription = getResources().getStringArray(R.array.supply_info_expenses_desc);

				LinkedHashMap<String, String> incomeMap = new LinkedHashMap<>();
				for (int position = 0; position < arrTitle.length; position++) {
					incomeMap.put(arrTitle[position], arrDescription[position]);
				}
				mIncreaseLimitController.hideSoftKeyboard(getActivity());
				Utils.displayValidationMessage(getActivity(),
						CustomPopUpWindow.MODAL_LAYOUT.SUPPLY_DETAIL_INFO,
						new Gson().toJson(incomeMap, LinkedHashMap.class));
				break;

			case R.id.llNextButtonLayout:
			case R.id.btnContinue:
				IncreaseLimitController increaseLimitController = new IncreaseLimitController(getActivity());
				HashMap<String, String> hmExpenseMap = increaseLimitController.expenseHashMap(etMortgagePayments, etRentalPayments, etMaintainanceExpenses, etMonthlyCreditPayments, etOtherExpenses);
				Bundle bundle = new Bundle();
				bundle.putSerializable(IncreaseLimitController.INCOME_DETAILS, mHashIncomeDetail);
				bundle.putSerializable(IncreaseLimitController.EXPENSE_DETAILS, hmExpenseMap);
				bundle.putBoolean(IncreaseLimitController.FROM_EXPENSE_SCREEN, true);
				OfferCalculationFragment ocFragment = new OfferCalculationFragment();
				ocFragment.setStepIndicatorListener(mCliStepIndicatorListener);
				ocFragment.setArguments(bundle);
				FragmentUtils fragmentUtils = new FragmentUtils();
				fragmentUtils.nextFragment((AppCompatActivity) SupplyExpensesDetailFragment.this.getActivity()
						, getFragmentManager().beginTransaction(), ocFragment, R.id.cli_steps_container);
				break;
		}
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (hasFocus) {
			if (etOtherExpenses.hasFocus() && etOtherExpensesWasTouched) {
				nsSupplyExpense.post(new Runnable() {
					@Override
					public void run() {
						ObjectAnimator.ofInt(nsSupplyExpense, "scrollY", nsSupplyExpense.getBottom()).setDuration(300).start();
					}
				});
			}
		}
	}

	private class GenericTextWatcher implements TextWatcher {

		private View view;

		private GenericTextWatcher(View view) {
			this.view = view;
		}

		public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
		}

		public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
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

				case R.id.etOtherExpenses:
					etOtherExpensesWasEdited = IncreaseLimitController.editTextLength(currentAmount);
					enableNextButton();
					break;
				default:
					break;
			}
		}
	}

	private void nextFocusEditText() {
		etMortgagePayments.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView exampleView, int actionId, KeyEvent event) {

				if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_GO || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
					llRentalPayment.performClick();
				}
				return false;
			}
		});
		etRentalPayments.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView exampleView, int actionId, KeyEvent event) {

				if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_GO || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
					llMaintainanceExpenses.performClick();
				}
				return false;
			}
		});
		etMaintainanceExpenses.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView exampleView, int actionId, KeyEvent event) {

				if (actionId == EditorInfo.IME_ACTION_DONE || (actionId == EditorInfo.IME_ACTION_NEXT) || actionId == EditorInfo.IME_ACTION_GO || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
					llMonthlyCreditPayments.performClick();
				}
				return false;
			}
		});

		etMonthlyCreditPayments.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView exampleView, int actionId, KeyEvent event) {

				if (actionId == EditorInfo.IME_ACTION_DONE || (actionId == EditorInfo.IME_ACTION_NEXT) || actionId == EditorInfo.IME_ACTION_GO || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
					llOtherExpensesContainer.performClick();
				}
				return false;
			}
		});

		etOtherExpenses.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				etOtherExpensesWasTouched = true;
				return false;
			}
		});

		etOtherExpenses.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView exampleView, int actionId, KeyEvent event) {

				if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_GO || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
					llOtherExpensesContainer.performClick();
				}
				return false;
			}

		});
	}

	private void enableNextButton() {
		if (etMortgagePaymentsWasEdited
				&& etRentalPaymentsWasEdited
				&& etMaintainanceExpensesWasEdited
				&& etMonthlyCreditPaymentsWasEdited
				&& etOtherExpensesWasEdited) {
			llNextButtonLayout.setVisibility(View.VISIBLE);
		} else {
			llNextButtonLayout.setVisibility(View.GONE);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		if (llNextButtonLayout != null)
			llNextButtonLayout.requestFocus();
	}

	@Override
	public void onResume() {
		super.onResume();
		Utils.setScreenName(getActivity(), FirebaseManagerAnalyticsProperties.ScreenNames.CLI_EXPENSES);
		if (llNextButtonLayout != null)
			llNextButtonLayout.requestFocus();
		CLIPhase2Activity cliPhase2Activity = (CLIPhase2Activity) getActivity();
		cliPhase2Activity.actionBarBackIcon();
	}
}