package za.co.woolworths.financial.services.android.ui.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.views.WTextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class CLIAllStepsContainerFragment extends Fragment {

	private FrameLayout indicator1;
	private FrameLayout indicator2;
	private FrameLayout indicator3;
	private FrameLayout indicator4;
	private WTextView indicatorNumber1;
	private WTextView indicatorNumber2;
	private WTextView indicatorNumber3;
	private WTextView indicatorNumber4;

	public CLIAllStepsContainerFragment() {
		// Required empty public constructor
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.cli_all_steps_container_fragment, container, false);
	}

	public void updateStepIndicator(int position)
	{

	}

}
