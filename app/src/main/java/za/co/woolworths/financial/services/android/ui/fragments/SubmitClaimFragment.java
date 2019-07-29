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

import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.models.dto.BalanceInsurance;
import za.co.woolworths.financial.services.android.ui.activities.BalanceProtectionActivity;
import za.co.woolworths.financial.services.android.ui.adapters.BalanceInsuranceAdapter;
import za.co.woolworths.financial.services.android.util.Utils;

public class SubmitClaimFragment extends Fragment implements BalanceInsuranceAdapter.OnItemClick {

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.balance_insurance_fragment, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		RecyclerView rvBalanceInsurance = (RecyclerView) view.findViewById(R.id.rvBalanceInsurance);
		BalanceInsuranceAdapter balanceInsuranceAdapter = new BalanceInsuranceAdapter(getBalanceInsurance(), this);
		LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
		mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		rvBalanceInsurance.setLayoutManager(mLayoutManager);
		rvBalanceInsurance.setAdapter(balanceInsuranceAdapter);
	}

	private ArrayList<BalanceInsurance> getBalanceInsurance() {
		ArrayList<BalanceInsurance> balanceInsurance = new ArrayList<>();
		balanceInsurance.add(new BalanceInsurance(getString(R.string.death_cover), getString(R.string.death_cover_desc)));
		balanceInsurance.add(new BalanceInsurance(getString(R.string.retrenchment), getString(R.string.retrenchment_desc)));
		balanceInsurance.add(new BalanceInsurance(getString(R.string.temporary_disability), getString(R.string.temporary_disability_desc)));
		balanceInsurance.add(new BalanceInsurance(getString(R.string.permanent_disability), getString(R.string.permanent_disability_desc)));
		balanceInsurance.add(new BalanceInsurance(getString(R.string.critical_illness), getString(R.string.critical_illness_desc)));
		return balanceInsurance;
	}

	@Override
	public void onItemClick(View view, int position) {
		final String REQUIRED_FORM = "REQUIRED_FORM";
		final String REQUIRED_FORM_SUBMIT = "REQUIRED_FORM_SUBMIT";
		final String REQUIRED_TITLE = "REQUIRED_TITLE";

		BalanceProtectionActivity balanceProtectionActivity = (BalanceProtectionActivity) SubmitClaimFragment.this.getActivity();
		Bundle bundle = new Bundle();
		switch (position) {
			case 0:
				bundle.putStringArray(REQUIRED_FORM, getStringArray(R.array.death_cover_required_forms));
				bundle.putStringArray(REQUIRED_FORM_SUBMIT, getStringArray(R.array.death_cover_required_forms_submit));
				bundle.putString(REQUIRED_TITLE,getString(R.string.death_cover));
				break;

			case 1:
				bundle.putStringArray(REQUIRED_FORM, getStringArray(R.array.retrenchment_form));
				bundle.putStringArray(REQUIRED_FORM_SUBMIT, getStringArray(R.array.retrenchment_form_submit));
				bundle.putString(REQUIRED_TITLE,getString(R.string.retrenchment));
				break;

			case 2:
				bundle.putStringArray(REQUIRED_FORM, getStringArray(R.array.temporary_disability));
				bundle.putStringArray(REQUIRED_FORM_SUBMIT, getStringArray(R.array.temporary_disability_submit));
				bundle.putString(REQUIRED_TITLE,getString(R.string.temporary_disability));

				break;

			case 3:
				bundle.putStringArray(REQUIRED_FORM, getStringArray(R.array.permanent_disability));
				bundle.putStringArray(REQUIRED_FORM_SUBMIT, getStringArray(R.array.permanent_disability_submit));
				bundle.putString(REQUIRED_TITLE,getString(R.string.permanent_disability));
				break;

			case 4:
				bundle.putStringArray(REQUIRED_FORM, getStringArray(R.array.critical_illness));
				bundle.putStringArray(REQUIRED_FORM_SUBMIT, getStringArray(R.array.critical_illness_submit));
				bundle.putString(REQUIRED_TITLE,getString(R.string.critical_illness));
				break;

			default:
				bundle.putStringArray(REQUIRED_FORM, getStringArray(R.array.death_cover_required_forms));
				bundle.putStringArray(REQUIRED_FORM_SUBMIT, getStringArray(R.array.death_cover_required_forms_submit));
				bundle.putString(REQUIRED_TITLE,getString(R.string.death_cover));
				break;
		}

		BalanceInsuranceDetailFragment balanceInsuranceDetailFragment = new BalanceInsuranceDetailFragment();
		balanceInsuranceDetailFragment.setArguments(bundle);
		balanceProtectionActivity.currentFragment(balanceInsuranceDetailFragment);
	}

	private String[] getStringArray(int array) {
		return getResources().getStringArray(array);
	}

	@Override
	public void onResume() {
		super.onResume();
		Utils.setScreenName(getActivity(), FirebaseManagerAnalyticsProperties.ScreenNames.BPI_CLAIM_REASONS);
		((BalanceProtectionActivity) getActivity()).setTitle(getActivity().getString(R.string.select_claim_reason));
	}
}
