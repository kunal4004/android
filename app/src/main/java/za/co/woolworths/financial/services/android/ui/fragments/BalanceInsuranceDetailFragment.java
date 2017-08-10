package za.co.woolworths.financial.services.android.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.activities.BalanceProtectionActivity;
import za.co.woolworths.financial.services.android.ui.adapters.BalanceInsuranceAdapter;

public class BalanceInsuranceDetailFragment extends Fragment implements BalanceInsuranceAdapter.OnItemClick {

	private final String TAG = this.getClass().getSimpleName();

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.balance_insurancet_detail_fragment, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

	}

	@Override
	public void onItemClick(View view, int position) {
		BalanceProtectionActivity balanceProtectionActivity = (BalanceProtectionActivity) BalanceInsuranceDetailFragment.this.getActivity();
		switch (position) {
			case 0:
				break;

			case 1:
				balanceProtectionActivity.selectFrag(1);
				break;

			default:
				break;
		}
	}
}
