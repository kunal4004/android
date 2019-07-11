package za.co.woolworths.financial.services.android.ui.fragments.contact_us;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import java.util.List;

import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator;
import za.co.woolworths.financial.services.android.util.Utils;

/**
 * Created by W7099877 on 01/12/2016.
 */

public class ContactUsWRewardsFragment extends Fragment implements View.OnClickListener {
	public BottomNavigator mBottomNavigator;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.contact_us_wrewards, container, false);
		view.findViewById(R.id.localCaller).setOnClickListener(this);
		view.findViewById(R.id.internationalCaller).setOnClickListener(this);
		view.findViewById(R.id.complaints).setOnClickListener(this);
		view.findViewById(R.id.wrewardCard).setOnClickListener(this);
		view.findViewById(R.id.wRewardsLoyalVoucher).setOnClickListener(this);
		view.findViewById(R.id.littleWorld).setOnClickListener(this);
		view.findViewById(R.id.general).setOnClickListener(this);
		try {
			mBottomNavigator = (BottomNavigator) getActivity();
			mBottomNavigator.setTitle(getActivity().getResources().getString(R.string.wrewards));
		} catch (ClassCastException ex) {
			Log.e("Interface", ex.toString());
		}
		return view;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.localCaller:
				Utils.makeCall(getActivity(), getActivity().getResources().getString(R.string.wrewards_local_caller_number));
				break;
			case R.id.internationalCaller:
				Utils.makeCall(getActivity(), getActivity().getResources().getString(R.string.wrewards_inter_national_caller_number));
				break;
			case R.id.complaints:
				sendEmail(getActivity().getResources().getString(R.string.email_rewards), getActivity().getResources().getString(R.string.txt_complaint));
				break;
			case R.id.wrewardCard:
				sendEmail(getActivity().getResources().getString(R.string.email_rewards), getActivity().getResources().getString(R.string.txt_wrewards_card));
				break;
			case R.id.wRewardsLoyalVoucher:
				sendEmail(getActivity().getResources().getString(R.string.email_rewards), getActivity().getResources().getString(R.string.txt_wrewards_loyalty_vouchers));
				break;
			case R.id.littleWorld:
				sendEmail(getActivity().getResources().getString(R.string.email_rewards), getActivity().getResources().getString(R.string.txt_littleworld));
				break;
			case R.id.general:
				sendEmail(getActivity().getResources().getString(R.string.email_rewards), getActivity().getResources().getString(R.string.txt_general));
				break;
		}
	}

	public void sendEmail(String emailId, String subject) {
		Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
		emailIntent.setData(Uri.parse("mailto:" + emailId +
				"?subject=" + Uri.encode(subject) +
				"&body=" + Uri.encode("")));

		PackageManager pm = getActivity().getPackageManager();
		List<ResolveInfo> listOfEmail = pm.queryIntentActivities(emailIntent, 0);
		if (listOfEmail.size() > 0) {
			startActivity(emailIntent);
		} else {
			Utils.displayValidationMessage(getActivity(),
					CustomPopUpWindow.MODAL_LAYOUT.INFO,
					getActivity().getResources().getString(R.string.contact_us_no_email_error)
							.replace("email_address", emailId).replace("subject_line", subject));
		}
	}


	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {
			if (mBottomNavigator != null) {
				mBottomNavigator.setTitle(getActivity().getResources().getString(R.string.wrewards));
				mBottomNavigator.displayToolbar();
				mBottomNavigator.showBackNavigationIcon(true);
			}
		}
	}
}

