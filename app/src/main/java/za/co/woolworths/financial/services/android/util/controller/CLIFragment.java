package za.co.woolworths.financial.services.android.util.controller;

import android.support.v4.app.Fragment;

public class CLIFragment extends Fragment {

	public CLIStepIndicatorListener cliStepIndicatorListener;

	public void setStepIndicatorListener(CLIStepIndicatorListener cliStepIndicatorListener) {
		this.cliStepIndicatorListener = cliStepIndicatorListener;
	}
}
