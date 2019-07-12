package za.co.woolworths.financial.services.android.ui.fragments;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import java.util.ArrayList;

import za.co.woolworths.financial.services.android.models.dto.BalanceInsurance;
import za.co.woolworths.financial.services.android.ui.activities.BalanceProtectionActivity;
import za.co.woolworths.financial.services.android.ui.adapters.BalanceInsuranceAdapter;

public class BalanceInsuranceFragment extends Fragment implements BalanceInsuranceAdapter.OnItemClick {

	private BalanceInsuranceAdapter balanceInsuranceAdapter;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.balance_insurance_fragment, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		RecyclerView rvBalanceInsurance = (RecyclerView) view.findViewById(R.id.rvBalanceInsurance);
		balanceInsuranceAdapter = new BalanceInsuranceAdapter(getBalanceInsurance(), this);
		LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
		mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		rvBalanceInsurance.setLayoutManager(mLayoutManager);
		rvBalanceInsurance.setAdapter(balanceInsuranceAdapter);
	}

	private ArrayList<BalanceInsurance> getBalanceInsurance() {
		ArrayList<BalanceInsurance> balanceInsurance = new ArrayList<>();
		balanceInsurance.add(new BalanceInsurance(getString(R.string.balance_protection_insurance_overview), getString(R.string.balance_protection_insurance_overview_desc)));
		balanceInsurance.add(new BalanceInsurance(getString(R.string.submit_a_claim), getString(R.string.submit_a_claim_desc)));
		return balanceInsurance;
	}

	@Override
	public void onItemClick(View view, int position) {
		BalanceProtectionActivity balanceProtectionActivity = (BalanceProtectionActivity) BalanceInsuranceFragment.this.getActivity();
		switch (position) {
			case 0:
				balanceProtectionActivity.currentFragment(new BalanceInsuranceOverviewFragment());
				break;

			case 1:
				balanceProtectionActivity.currentFragment(new SubmitClaimFragment());
				break;

			default:
				break;
		}

		if (balanceInsuranceAdapter != null)
			balanceInsuranceAdapter.notifyDataSetChanged();
	}

	@Override
	public void onResume() {
		super.onResume();
		((BalanceProtectionActivity) getActivity()).setTitle(getActivity().getString(R.string.balance_protection_title));
	}
}
