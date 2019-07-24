package za.co.woolworths.financial.services.android.ui.fragments.cli;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.activities.cli.CLIPhase2Activity;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.controller.CLIStepIndicatorListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class CLIAllStepsContainerFragment extends Fragment implements CLIStepIndicatorListener {

	private FrameLayout indicator1;
	private FrameLayout indicator2;
	private FrameLayout indicator3;
	private FrameLayout indicator4;
	private WTextView indicatorNumber1;
	private WTextView indicatorNumber2;
	private WTextView indicatorNumber3;
	private WTextView indicatorNumber4;
	private WTextView income;
	private WTextView expense;
	private WTextView offer;
	private WTextView documents;
	private Typeface mStepDefault;
	private Typeface mStepCurrent;
	private Typeface mStepFinished;

	private FrameLayout[] indicators = {indicator1, indicator2, indicator3, indicator4};
	private WTextView[] indicatorNumbers = {indicatorNumber1, indicatorNumber2, indicatorNumber3, indicatorNumber4};
	private WTextView[] stepNames = {income, expense, offer, documents};

	public CLIAllStepsContainerFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.cli_all_steps_container_fragment, container, false);
		initStepIndicatorViews(view);
		initUI();
		return view;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

	}

	private void initUI() {
		CLIPhase2Activity cliPhase2Activity = (CLIPhase2Activity) getActivity();
		cliPhase2Activity.initFragment(this);
	}

	public void initStepIndicatorViews(View view) {
		indicators[0] = (FrameLayout) view.findViewById(R.id.indicator1);
		indicators[1] = (FrameLayout) view.findViewById(R.id.indicator2);
		indicators[2] = (FrameLayout) view.findViewById(R.id.indicator3);
		indicators[3] = (FrameLayout) view.findViewById(R.id.indicator4);
		indicatorNumbers[0] = (WTextView) view.findViewById(R.id.indicatorText1);
		indicatorNumbers[1] = (WTextView) view.findViewById(R.id.indicatorText2);
		indicatorNumbers[2] = (WTextView) view.findViewById(R.id.indicatorText3);
		indicatorNumbers[3] = (WTextView) view.findViewById(R.id.indicatorText4);
		stepNames[0] = (WTextView) view.findViewById(R.id.incomeText);
		stepNames[1] = (WTextView) view.findViewById(R.id.expenseText);
		stepNames[2] = (WTextView) view.findViewById(R.id.offerText);
		stepNames[3] = (WTextView) view.findViewById(R.id.documentText);
		mStepDefault = Typeface.createFromAsset(getActivity().getAssets(), "fonts/WFutura-Medium.ttf");
		mStepCurrent = Typeface.createFromAsset(getActivity().getAssets(), "fonts/WFutura-SemiBold.ttf");
		mStepFinished = Typeface.createFromAsset(getActivity().getAssets(), "fonts/WFutura-Medium.ttf");
	}

	public void updateStepIndicator(int position) {
		int stepNumber = position - 1;
		Activity activity = getActivity();
		if (activity == null) return;
		for (int i = 0; i <= 3; i++) {
			if (i < stepNumber) {
				indicators[i].setBackgroundResource(R.drawable.cli_step_indicator_active);
				indicatorNumbers[i].setVisibility(View.INVISIBLE);
				stepNames[i].setTypeface(mStepFinished);
				stepNames[i].setTextColor(ContextCompat.getColor(activity, R.color.cli_step_indicator_done_text_color));
			} else if (i == stepNumber) {
				indicators[i].setBackgroundResource(R.drawable.cli_step_indicator_background_current_screen);
				indicatorNumbers[i].setVisibility(View.VISIBLE);
				indicatorNumbers[i].setTextColor(ContextCompat.getColor(activity, R.color.white));
				stepNames[i].setTypeface(mStepCurrent);
				stepNames[i].setTextColor(ContextCompat.getColor(activity, R.color.black));

			} else {
				indicators[i].setBackgroundResource(R.drawable.cli_step_indicator_background_next_screen);
				indicatorNumbers[i].setVisibility(View.VISIBLE);
				indicatorNumbers[i].setTextColor(ContextCompat.getColor(activity, R.color.mask_opacity));
				stepNames[i].setTypeface(mStepDefault);
				stepNames[i].setTextColor(ContextCompat.getColor(activity, R.color.mask_opacity));
			}
		}
	}

	@Override
	public void onStepSelected(int step) {
		updateStepIndicator(step);
	}
}
