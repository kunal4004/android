package za.co.woolworths.financial.services.android.ui.fragments.contact_us;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator;

/**
 * Created by W7099877 on 01/12/2016.
 */

public class ContactUsCustomerServiceFragment extends Fragment implements View.OnClickListener {

	public RelativeLayout generalEng;
	public RelativeLayout woolworthsOnline;
	public RelativeLayout wRewards;
	public RelativeLayout mySchoolEnq;
	private BottomNavigator mBottomNavigator;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.contact_us_customer_service, container, false);
		generalEng = view.findViewById(R.id.generalEnq);
		woolworthsOnline = view.findViewById(R.id.woolworthsOnline);
		wRewards = view.findViewById(R.id.wRewards);
		mySchoolEnq = view.findViewById(R.id.mySchoolEnq);
		generalEng.setOnClickListener(this);
		woolworthsOnline.setOnClickListener(this);
		wRewards.setOnClickListener(this);
		mySchoolEnq.setOnClickListener(this);
		try {
			mBottomNavigator = (BottomNavigator) getActivity();
		} catch (ClassCastException ignored) {
		}

		mBottomNavigator.setTitle(getActivity().getResources().getString(R.string.contact_us_customer_service));
		return view;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.generalEnq:
				openFragment(new ContactUsGeneralEnquiriesFragment());
				break;
			case R.id.woolworthsOnline:
				openFragment(new ContactUsOnlineFragment());
				break;
			case R.id.wRewards:
				openFragment(new ContactUsWRewardsFragment());
				break;
			case R.id.mySchoolEnq:
				openFragment(new ContactUsMySchoolFragment());
				break;
		}
	}

	public void openFragment(Fragment fragment) {
		if (mBottomNavigator != null) {
			mBottomNavigator.pushFragment(fragment);
		}
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {
			if (mBottomNavigator != null) {
				mBottomNavigator.setTitle(getActivity().getResources().getString(R.string.contact_us_customer_service));
				mBottomNavigator.displayToolbar();
				mBottomNavigator.showBackNavigationIcon(true);
			}
		}
	}
}
