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

import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.ui.activities.BalanceProtectionActivity;
import za.co.woolworths.financial.services.android.ui.adapters.RequiredFormAdapter;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.util.Utils;

public class BalanceInsuranceDetailFragment extends Fragment implements View.OnClickListener {

	String[] mRequiredForm, mRequiredFormSubmit;
	final String REQUIRED_FORM = "REQUIRED_FORM", REQUIRED_FORM_SUBMIT = "REQUIRED_FORM_SUBMIT", REQUIRED_TITLE = "REQUIRED_TITLE";
	private String mTitle;
	private String externalURL = "http://www.woolworths.co.za/store/fragments/corporate/corporate-index.jsp?content=corporate-content&contentId=cmp208540";

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRequiredForm = getArguments().getStringArray(REQUIRED_FORM);
		mRequiredFormSubmit = getArguments().getStringArray(REQUIRED_FORM_SUBMIT);
		mTitle = getArguments().getString(REQUIRED_TITLE);
		return inflater.inflate(R.layout.balance_insurance_detail_fragment, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		RecyclerView rlRequiredForm = (RecyclerView) view.findViewById(R.id.rlRequiredForm);
		RecyclerView rlAdditionalSubmission = (RecyclerView) view.findViewById(R.id.rlAdditionalSubmission);
		WButton btnGetDocument = (WButton)view.findViewById(R.id.btnGetDocument);

		((BalanceProtectionActivity) getActivity()).setTitle(mTitle);
		RequiredFormAdapter requiredFormAdapter = new RequiredFormAdapter(mRequiredForm, true);
		RequiredFormAdapter relAdditionalSubmission = new RequiredFormAdapter(mRequiredFormSubmit, false);


		LinearLayoutManager llmManager = new LinearLayoutManager(getActivity());
		llmManager.setOrientation(LinearLayoutManager.VERTICAL);

		LinearLayoutManager llmsInsurance = new LinearLayoutManager(getActivity());
		llmsInsurance.setOrientation(LinearLayoutManager.VERTICAL);

		btnGetDocument.setOnClickListener(this);

		rlRequiredForm.setLayoutManager(llmManager);
		rlAdditionalSubmission.setLayoutManager(llmsInsurance);

		rlRequiredForm.setAdapter(requiredFormAdapter);
		rlAdditionalSubmission.setAdapter(relAdditionalSubmission);

	}

	@Override
	public void onResume() {
		super.onResume();
		Utils.setScreenName(getActivity(), FirebaseManagerAnalyticsProperties.ScreenNames.BPI_DOCUMENTS_PROCESSING);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.btnGetDocument:
				Utils.openExternalLink(getActivity(),externalURL);
				break;
			default:
				break;
		}
	}
}