package za.co.woolworths.financial.services.android.ui.fragments.cli;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.service.event.BusStation;
import za.co.woolworths.financial.services.android.ui.activities.cli.CLIPhase2Activity;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.controller.CLIFragment;

public class ProcessCompleteFragment extends CLIFragment implements View.OnClickListener {

	private View view;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		if (view == null) {
			((CLIPhase2Activity) this.getActivity()).hideCloseIcon();
			view = inflater.inflate(R.layout.cli_process_complete, container, false);
		}
		return view;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initUI(view);
		mCliStepIndicatorListener.onStepSelected(5);
	}

	@Override
	public void onResume() {
		super.onResume();
		Utils.setScreenName(getActivity(), FirebaseManagerAnalyticsProperties.ScreenNames.CLI_PROCESS_COMPLETE);
	}

	private void initUI(View view) {
		WButton btnProcessComplete = (WButton) view.findViewById(R.id.btnProcessComplete);
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
