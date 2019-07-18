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
 * Created by W7099877 on 01/12/2016.
 */

public class ContactUsFinancialServiceFragment extends Fragment implements View.OnClickListener {
	private static final int REQUEST_CALL = 1;
	Intent callIntent;

	private BottomNavigator mBottomNavigator;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.contact_us_financial_services, container, false);
		view.findViewById(R.id.localCaller).setOnClickListener(this);
		view.findViewById(R.id.internationalCaller).setOnClickListener(this);
		view.findViewById(R.id.blackCrediCardQuery).setOnClickListener(this);
		view.findViewById(R.id.complains).setOnClickListener(this);
		view.findViewById(R.id.creditCardQuery).setOnClickListener(this);
		view.findViewById(R.id.wRewardsQuery).setOnClickListener(this);
		view.findViewById(R.id.insuranceClaim).setOnClickListener(this);
		view.findViewById(R.id.paymentQuery).setOnClickListener(this);
		view.findViewById(R.id.storeCardPesonalLoanQuery).setOnClickListener(this);
		view.findViewById(R.id.proofOfIncome).setOnClickListener(this);
		view.findViewById(R.id.technical).setOnClickListener(this);

		try {
			mBottomNavigator = (BottomNavigator) getActivity();
			mBottomNavigator.setTitle(getActivity().getResources().getString(R.string.contact_us_financial_services));
		} catch (ClassCastException ex) {
			Log.e("Interface", ex.toString());
		}

		return view;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.localCaller:
				Utils.makeCall(getActivity(), getActivity().getResources().getString(R.string.fs_local_caller_number));
				break;
			case R.id.internationalCaller:
				Utils.makeCall(getActivity(), getActivity().getResources().getString(R.string.fs_inter_national_caller_number));
				break;
			case R.id.blackCrediCardQuery:
				sendEmail(getActivity().getResources().getString(R.string.email_black_credit_card_query), getActivity().getResources().getString(R.string.txt_black_credit_card_query));
				break;
			case R.id.complains:
				sendEmail(getActivity().getResources().getString(R.string.email_complaints), getActivity().getResources().getString(R.string.txt_complaint));
				break;
			case R.id.creditCardQuery:
				sendEmail(getActivity().getResources().getString(R.string.email_credicard_query), getActivity().getResources().getString(R.string.txt_cc_query));
				break;
			case R.id.wRewardsQuery:
				sendEmail(getActivity().getResources().getString(R.string.email_wrewards_query), getActivity().getResources().getString(R.string.txt_wrewards_query));
				break;
			case R.id.insuranceClaim:
				sendEmail(getActivity().getResources().getString(R.string.email_insurance_claim), getActivity().getResources().getString(R.string.txt_insurance_claim));
				break;
			case R.id.paymentQuery:
				sendEmail(getActivity().getResources().getString(R.string.email_payment_query), getActivity().getResources().getString(R.string.txt_payment_query));
				break;
			case R.id.storeCardPesonalLoanQuery:
				sendEmail(getActivity().getResources().getString(R.string.email_sc_and_pl_query), getActivity().getResources().getString(R.string.txt_sc_and_pl_query));
				break;
			case R.id.proofOfIncome:
				sendEmail(getActivity().getResources().getString(R.string.email_proof_of_income), getActivity().getResources().getString(R.string.txt_proof_of_income));
				break;
			case R.id.technical:
				sendEmail(getActivity().getResources().getString(R.string.email_technical), getActivity().getResources().getString(R.string.txt_technical_problem));
				break;
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode) {
			case REQUEST_CALL:
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					startActivity(callIntent);
				}
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
				mBottomNavigator.setTitle(getActivity().getResources().getString(R.string.contact_us_financial_services));
				mBottomNavigator.displayToolbar();
				mBottomNavigator.showBackNavigationIcon(true);
			}
		}
	}
}
