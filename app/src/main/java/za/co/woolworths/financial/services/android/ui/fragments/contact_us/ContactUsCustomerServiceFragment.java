package za.co.woolworths.financial.services.android.ui.fragments.contact_us;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.activities.bottom_menu.BottomNavigator;

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
		generalEng = (RelativeLayout) view.findViewById(R.id.generalEnq);
		woolworthsOnline = (RelativeLayout) view.findViewById(R.id.woolworthsOnline);
		wRewards = (RelativeLayout) view.findViewById(R.id.wRewards);
		mySchoolEnq = (RelativeLayout) view.findViewById(R.id.mySchoolEnq);
		generalEng.setOnClickListener(this);
		woolworthsOnline.setOnClickListener(this);
		wRewards.setOnClickListener(this);
		mySchoolEnq.setOnClickListener(this);
		try {
			mBottomNavigator = (BottomNavigator) getActivity();
		} catch (ClassCastException ex) {
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
		FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
		fragmentManager.beginTransaction()
				.setCustomAnimations(R.anim.slide_from_right, R.anim.slide_to_left, R.anim.slide_from_left, R.anim.slide_to_right)
				.replace(R.id.frag_container, fragment).addToBackStack(null).commit();
	}
}
