package za.co.woolworths.financial.services.android.ui.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.util.StepIndicatorCallback;

/**
 * A simple {@link Fragment} subclass.
 */
public class DocumentFragment extends Fragment {

	private StepIndicatorCallback mStepIndicatorCallback;
	private RecyclerView rclSelectYourBank;

	public DocumentFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.document_fragment, container, false);
		mStepIndicatorCallback.onCurrentStep(4);
		return view;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		init(view);
		selectBankLayoutManager();
	}

	private void init(View view) {
		rclSelectYourBank = (RecyclerView) view.findViewById(R.id.rclSelectYourBank);
	}

	private void selectBankLayoutManager() {
		LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
		mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		rclSelectYourBank.setLayoutManager(mLayoutManager);
		//rclSelectYourBank.setAdapter(balanceInsuranceAdapter);
	}

	public void setmStepIndicatorCallback(StepIndicatorCallback mStepIndicatorCallback) {
		this.mStepIndicatorCallback = mStepIndicatorCallback;
	}
}
