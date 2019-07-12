package za.co.woolworths.financial.services.android.util.controller;

import android.app.Activity;
import android.content.Context;
import androidx.fragment.app.Fragment;
import android.view.inputmethod.InputMethodManager;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class CLIFragment extends Fragment {

	public CLIStepIndicatorListener mCliStepIndicatorListener;

	public void setStepIndicatorListener(CLIStepIndicatorListener cliStepIndicatorListener) {
		this.mCliStepIndicatorListener = cliStepIndicatorListener;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		//show keyboard when any fragment of this class has been attached
		hideSoftKeyboard();
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		hideSoftKeyboard();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		//hide keyboard when any fragment of this class has been detached
		hideSoftKeyboard();
	}

	/**
	 * Hides the soft keyboard
	 */
	private void hideSoftKeyboard() {
		final Activity activity = getActivity();
		if (activity != null) {
			if (activity.getCurrentFocus() != null) {
				InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
				inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
			}
		}
	}
}
