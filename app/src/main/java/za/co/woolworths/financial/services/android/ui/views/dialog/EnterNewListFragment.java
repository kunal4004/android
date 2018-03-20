package za.co.woolworths.financial.services.android.ui.views.dialog;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WLoanEditTextView;
import za.co.woolworths.financial.services.android.util.KeyboardUtil;

public class EnterNewListFragment extends Fragment implements View.OnClickListener {

	private WButton mBtnCancel;
	private ImageView mImBack;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.create_new_list_layout, container, false);
		return view;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Activity activity = getActivity();
		if (activity != null) {
			initUI(view);
			displayKeyboard(view, activity);
		}
	}

	private void initUI(View view) {
		mBtnCancel = view.findViewById(R.id.btnCancel);
		mImBack = view.findViewById(R.id.imBack);
		WLoanEditTextView etNewList = view.findViewById(R.id.etNewList);
		setUpEditText(etNewList);
		mBtnCancel.setOnClickListener(this);
		mImBack.setOnClickListener(this);
		enableCreateList(false);
	}


	private void displayKeyboard(View view, Activity activity) {
		if (activity != null) {
			activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
			new KeyboardUtil(activity, view.findViewById(R.id.rlRootList), 0);
		}
	}

	private void setUpEditText(WLoanEditTextView etNewList) {
		etNewList.setOnKeyPreImeListener(onKeyPreImeListener);
		etNewList.setOnEditorActionListener(onEditorActionListener);
		etNewList.requestFocus();
		showSoftKeyboard(etNewList);
		addTextChangedListener(etNewList);
	}


	private WLoanEditTextView.OnKeyPreImeListener onKeyPreImeListener = new WLoanEditTextView.OnKeyPreImeListener() {
		@Override
		public void onBackPressed() {
			EnterNewListFragment.this.onBackPressed();
		}
	};

	public void showSoftKeyboard(WLoanEditTextView editTextView) {
		Activity activity = getActivity();
		if (activity != null) {
			InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(editTextView, InputMethodManager.SHOW_IMPLICIT);
		}
	}

	private void onBackPressed() {
		Activity activity = getActivity();
		if (activity != null) {
			CustomPopUpWindow customPopUpWindow = (CustomPopUpWindow) activity;
			customPopUpWindow.onBackPressed();
		}
	}

	private void addTextChangedListener(WLoanEditTextView etNewList) {
		etNewList.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				if (charSequence.length() > 0) {
					enableCreateList(true);
				} else {
					enableCreateList(false);
				}
			}

			@Override
			public void afterTextChanged(Editable editable) {

			}
		});
	}

	private void enableCreateList(boolean enable) {
		mBtnCancel.setAlpha(enable ? (float) 1.0 : (float) 0.4);
		mBtnCancel.setEnabled(enable ? true : false);
	}


	private WLoanEditTextView.OnEditorActionListener onEditorActionListener = new WLoanEditTextView.OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			// Identifier of the action. This will be either the identifier you supplied,
			// or EditorInfo.IME_NULL if being called due to the enter key being pressed.
			if (actionId == EditorInfo.IME_ACTION_SEARCH
					|| actionId == EditorInfo.IME_ACTION_DONE
					|| event.getAction() == KeyEvent.ACTION_DOWN
					&& event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
				onBackPressed();
				return true;
			}
			// Return true if you have consumed the action, else false.
			return false;
		}
	};

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.btnCancel:
				break;
			case R.id.imBack:
				Activity activity = getActivity();
				if (activity != null) {
					CustomPopUpWindow customPopUpWindow = (CustomPopUpWindow) activity;
					customPopUpWindow.onBackPressed();
				}
				break;
			default:
				break;
		}
	}
}
