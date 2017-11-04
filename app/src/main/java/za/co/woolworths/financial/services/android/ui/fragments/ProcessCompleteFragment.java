package za.co.woolworths.financial.services.android.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.util.controller.CLIFragment;

public class ProcessCompleteFragment extends CLIFragment implements View.OnClickListener {

	private View view;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		if (view == null) {
			view = inflater.inflate(R.layout.cli_process_complete, container, false);
		}
		return view;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initUI(view);
	}


	private void initUI(View view) {
		WButton btnProcessComplete = (WButton) view.findViewById(R.id.btnProcessComplete);
		btnProcessComplete.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnProcessComplete:

				break;
		}
	}
}
