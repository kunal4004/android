package za.co.woolworths.financial.services.android.ui.fragments;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.service.event.BusStation;
import za.co.woolworths.financial.services.android.ui.activities.CLIPhase2Activity;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.util.controller.CLIFragment;

public class CliEmailSentFragment extends CLIFragment implements View.OnClickListener {

	public CliEmailSentFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		return inflater.inflate(R.layout.cli_email_sent_fragment, container, false);
	}


	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		//mCliStepIndicatorListener.onStepSelected(5);
		initUI(view);

	}

	private void initUI(View view) {
		WButton btnProcessComplete = (WButton) view.findViewById(R.id.btnProcessComplete);
		//WTextView tvClearDocumentPhoto = (WTextView) view.findViewById(R.id.tvClearDocumentPhoto);
		btnProcessComplete.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnProcessComplete:
				Activity activity = getActivity();
				if (activity != null && activity instanceof CLIPhase2Activity) {
					((WoolworthsApplication) activity.getApplication())
							.bus()
							.send(new BusStation(true));
					activity.finish();
					activity.overridePendingTransition(R.anim.stay, R.anim.slide_down_anim);
				}
				break;
		}
	}
}
