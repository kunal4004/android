package za.co.woolworths.financial.services.android.ui.fragments.contact_us;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
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
 * Created by W7099877 on 18/02/2017.
 */

public class ContactUsGeneralEnquiriesFragment extends Fragment implements View.OnClickListener {
	private BottomNavigator mBottomNavigator;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.contact_us_general_enquiries, container, false);
		view.findViewById(R.id.localCaller).setOnClickListener(this);
		view.findViewById(R.id.internationalCaller).setOnClickListener(this);
		view.findViewById(R.id.productQuery).setOnClickListener(this);
		view.findViewById(R.id.storeQuery).setOnClickListener(this);
		view.findViewById(R.id.complaints).setOnClickListener(this);
		view.findViewById(R.id.technicalProblem).setOnClickListener(this);
		try {
			mBottomNavigator = (BottomNavigator) getActivity();
			mBottomNavigator.setTitle(getActivity().getResources().getString(R.string.txt_general_enquiry));
		} catch (ClassCastException ex) {
			Log.e("Interface", ex.toString());
		}
		return view;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.localCaller:
				Utils.makeCall(getActivity(), getActivity().getResources().getString(R.string.customer_service_local_caller_number));
				break;
			case R.id.internationalCaller:
				Utils.makeCall(getActivity(), getActivity().getResources().getString(R.string.customer_service_international_call));
				break;
			case R.id.productQuery:
				sendEmail(getActivity().getResources().getString(R.string.email_custserv), getActivity().getResources().getString(R.string.txt_product_query));
				break;
			case R.id.storeQuery:
				sendEmail(getActivity().getResources().getString(R.string.email_custserv), getActivity().getResources().getString(R.string.txt_store_query));
				break;
			case R.id.complaints:
				sendEmail(getActivity().getResources().getString(R.string.email_custserv), getActivity().getResources().getString(R.string.txt_complaint));
				break;
			case R.id.technicalProblem:
				sendEmail(getActivity().getResources().getString(R.string.email_custserv), getActivity().getResources().getString(R.string.txt_general_technical_problem));
				break;
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

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
				mBottomNavigator.setTitle(getActivity().getResources().getString(R.string.txt_general_enquiry));
				mBottomNavigator.displayToolbar();
				mBottomNavigator.showBackNavigationIcon(true);
			}
		}
	}
}
