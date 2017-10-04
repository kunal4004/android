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

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.Bank;
import za.co.woolworths.financial.services.android.ui.adapters.DocumentAdapter;
import za.co.woolworths.financial.services.android.util.StepIndicatorCallback;

public class DocumentFragment extends Fragment implements DocumentAdapter.OnItemClick {

	private StepIndicatorCallback mStepIndicatorCallback;
	private RecyclerView rclSelectYourBank;
	private List<Bank> deaBankList;

	public DocumentFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.document_fragment, container, false);
		deaBankList = new ArrayList<>();
		deaBankList.add(new Bank("ABSA Bank"));
		deaBankList.add(new Bank("Standard Bank"));
		deaBankList.add(new Bank("Nedbank"));
		deaBankList.add(new Bank("FNB"));
		deaBankList.add(new Bank("Other"));
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
		DocumentAdapter documentAdapter = new DocumentAdapter(deaBankList, this);
		mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		rclSelectYourBank.setLayoutManager(mLayoutManager);
		rclSelectYourBank.setAdapter(documentAdapter);
	}

	public void setStepIndicatorCallback(StepIndicatorCallback mStepIndicatorCallback) {
		this.mStepIndicatorCallback = mStepIndicatorCallback;
	}

	@Override
	public void onItemClick(View view, int position) {
		Bank selectedBank = deaBankList.get(position);

	}
}
