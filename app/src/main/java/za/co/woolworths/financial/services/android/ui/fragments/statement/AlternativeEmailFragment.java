package za.co.woolworths.financial.services.android.ui.fragments.statement;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.activities.StatementActivity;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WLoanEditTextView;
import za.co.woolworths.financial.services.android.util.FragmentUtils;
import za.co.woolworths.financial.services.android.util.StatementUtils;

public class AlternativeEmailFragment extends Fragment implements View.OnClickListener {

	private WLoanEditTextView etAlternativeEmailAddress;
	private WButton btnSendEmail;
	private RelativeLayout relEmailStatement;
	private StatementUtils statmentUtils;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.alternative_email_statement_fragment, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		statmentUtils = new StatementUtils(getActivity());
		initView(view);
		disableButton();
	}

	private void initView(View view) {
		etAlternativeEmailAddress = (WLoanEditTextView) view.findViewById(R.id.etAlternativeEmailAddress);
		relEmailStatement = (RelativeLayout) view.findViewById(R.id.relEmailStatement);
		btnSendEmail = (WButton) view.findViewById(R.id.btnSendEmail);

		showKeyboard();
		onTextChangeListener();
		listener();
	}

	private void listener() {
		btnSendEmail.setOnClickListener(this);
		WLoanEditTextView.OnKeyPreImeListener onKeyPreImeListener =
				new WLoanEditTextView.OnKeyPreImeListener() {
					@Override
					public void onBackPressed() {
						Activity activity = getActivity();
						if (activity instanceof StatementActivity) {
							activity.onBackPressed();
						}
					}
				};

		etAlternativeEmailAddress.setOnKeyPreImeListener(onKeyPreImeListener);
	}

	private void onTextChangeListener() {
		etAlternativeEmailAddress.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if (s.length() >= 5) {
					enableButton();
				} else {
					disableButton();
				}
			}
		});
	}

	private void enableButton() {
		statmentUtils.enableView(relEmailStatement);
		statmentUtils.enableView(btnSendEmail);
		statmentUtils.showView(relEmailStatement);
	}

	private void disableButton() {
		statmentUtils.disableView(relEmailStatement);
		statmentUtils.disableView(btnSendEmail);
		statmentUtils.invisibleView(relEmailStatement);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		hideKeyboard();
	}

	public void showKeyboard() {
		Activity activity = getActivity();
		if (activity != null) {
			etAlternativeEmailAddress.requestFocus();
			InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
			assert imm != null;
			imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
		}
	}

	public void hideKeyboard() {
		Activity activity = getActivity();
		if (activity != null) {
			InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
			assert imm != null;
			imm.hideSoftInputFromWindow(etAlternativeEmailAddress.getWindowToken(), 0);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnSendEmail:
				String alternativeEmail = etAlternativeEmailAddress.getText().toString();
				if (statmentUtils.validateEmail(alternativeEmail)) {
					hideKeyboard();
					FragmentUtils fragmentUtils = new FragmentUtils();
					EmailStatementFragment emailStatementFragment = new EmailStatementFragment();
					fragmentUtils.nextFragment((AppCompatActivity) AlternativeEmailFragment.this.getActivity(), getFragmentManager().beginTransaction(), emailStatementFragment, R.id.flEStatement);
				}
				break;

			default:
				break;
		}
	}
}
