package za.co.woolworths.financial.services.android.ui.activities;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.fragments.BalanceInsuranceFragment;
import za.co.woolworths.financial.services.android.ui.fragments.SubmitClaimFragment;


public class BalanceProtectionActivity extends AppCompatActivity {

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.balance_insurance_activity);

		BalanceInsuranceFragment balanceInsuranceFragment = new BalanceInsuranceFragment();
		currentFragment(balanceInsuranceFragment);
	}

	public void selectFrag(int position) {
		switch (position) {
			case 0:
				break;

			case 1:
				SubmitClaimFragment submitClaimFragment = new SubmitClaimFragment();
				currentFragment(submitClaimFragment);
				break;
		}
	}

	private void currentFragment(Fragment frag) {
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fm.beginTransaction();
		fragmentTransaction.replace(R.id.fragment_container, frag);
		fragmentTransaction.addToBackStack(frag.getClass().getName());
//		fragmentTransaction.setCustomAnimations(R.anim.slide_from_right,R.anim.slide_to_left);
		fragmentTransaction.show(frag);
		fragmentTransaction.commit();
	}
}
