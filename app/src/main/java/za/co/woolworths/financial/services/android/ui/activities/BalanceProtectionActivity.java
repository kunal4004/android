package za.co.woolworths.financial.services.android.ui.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.fragments.BalanceInsuranceFragment;


public class BalanceProtectionActivity extends AppCompatActivity {

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.balance_insurance_activity);

		BalanceInsuranceFragment balanceInsuranceFragment = new BalanceInsuranceFragment();
		selectFrag(balanceInsuranceFragment);
	}

	public void selectFrag(Fragment frag) {
		FragmentManager fm = getFragmentManager();
		FragmentTransaction fragmentTransaction = fm.beginTransaction();
		fragmentTransaction.replace(R.id.fragment_container, frag);
		fragmentTransaction.commit();

	}
}
