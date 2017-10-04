package za.co.woolworths.financial.services.android.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.util.StepIndicatorCallback;

/**
 * Created by W7099877 on 2017/10/04.
 */

public class CLISupplyDocumentsFragment extends Fragment {
	private StepIndicatorCallback mStepIndicatorCallback;


	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.cli_supply_documents_fragment, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mStepIndicatorCallback.onCurrentStep(4);
		FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
		fragmentManager.beginTransaction()
				.replace(R.id.cliDocumentsFragmentContainer, new CLIRequsetAccountNumberFragment()).commit();

	}

	public void setStepIndicatorCallback(StepIndicatorCallback mStepIndicatorCallback) {
		this.mStepIndicatorCallback = mStepIndicatorCallback;
	}
}
