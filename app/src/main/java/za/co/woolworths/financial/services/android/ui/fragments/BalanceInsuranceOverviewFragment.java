package za.co.woolworths.financial.services.android.ui.fragments;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.activities.BalanceProtectionActivity;

public class BalanceInsuranceOverviewFragment extends Fragment {

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.balance_insurance_overview, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

	}


	@Override
	public void onResume() {
		super.onResume();
		((BalanceProtectionActivity) getActivity()).setTitle(getActivity().getString(R.string.balance_insurance_overview));
	}
}
