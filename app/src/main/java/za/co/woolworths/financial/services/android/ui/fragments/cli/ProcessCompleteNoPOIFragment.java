package za.co.woolworths.financial.services.android.ui.fragments.cli;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.service.event.BusStation;
import za.co.woolworths.financial.services.android.ui.activities.cli.CLIPhase2Activity;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.WFormatter;
import za.co.woolworths.financial.services.android.util.controller.CLIFragment;

public class ProcessCompleteNoPOIFragment extends CLIFragment implements View.OnClickListener {

	private View view;
	private WTextView tvProcessCompleteDesc;
	private Integer approvedIncreaseValue;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		if (view == null) {
			((CLIPhase2Activity) this.getActivity()).hideCloseIcon();
			view = inflater.inflate(R.layout.cli_process_complete_no_poi, container, false);
		}
		return view;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initUI(view);
		mCliStepIndicatorListener.onStepSelected(5);
		tvProcessCompleteDesc.setText(getResources().getText(R.string.process_complete_no_poi_desc).toString().replace(":approvedValue", WFormatter.escapeDecimalFormat(approvedIncreaseValue)));
	}

	private void initUI(View view) {
		WButton btnProcessComplete = (WButton) view.findViewById(R.id.btnProcessComplete);
		btnProcessComplete.setOnClickListener(this);
		tvProcessCompleteDesc = view.findViewById(R.id.tvProcessCompleteDesc);
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

	public Integer getApprovedIncreaseValue() {
		return approvedIncreaseValue;
	}

	public void setApprovedIncreaseValue(Integer approvedIncreaseValue) {
		this.approvedIncreaseValue = approvedIncreaseValue;
	}
}
