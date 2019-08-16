package za.co.woolworths.financial.services.android.ui.fragments.cli;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.FragmentUtils;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.controller.IncreaseLimitController;

/**
 * A simple {@link Fragment} subclass.
 */
public class CLIEligibilityAndPermissionFragment extends Fragment implements View.OnClickListener {

	private static final int SLIDE_UP_ANIM_DURATION = 300;
	private LinearLayout permissionView;
	private WTextView eligibilityYes;
	private WTextView eligibilityNo;
	private WTextView permissionYes;
	private WTextView permissionNo;
	private ScrollView scrollView;
	private int paddingDp = 0;
	private LinearLayout llEligibilityView;

	public CLIEligibilityAndPermissionFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.cli_eligibility_and_permission_fragment, container, false);
		permissionView = (LinearLayout) view.findViewById(R.id.permissionView);
		llEligibilityView = (LinearLayout) view.findViewById(R.id.llEligibilityView);
		eligibilityYes = (WTextView) view.findViewById(R.id.eligibilityYes);
		eligibilityNo = (WTextView) view.findViewById(R.id.eligibilityNo);
		permissionYes = (WTextView) view.findViewById(R.id.permissionYes);
		permissionNo = (WTextView) view.findViewById(R.id.permissionNo);
		scrollView = (ScrollView) view.findViewById(R.id.scrollView);
		eligibilityNo.setOnClickListener(this);
		eligibilityYes.setOnClickListener(this);
		permissionYes.setOnClickListener(this);
		permissionNo.setOnClickListener(this);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		Utils.setScreenName(getActivity(), FirebaseManagerAnalyticsProperties.ScreenNames.CLI_INSOLVENCY_CHECK);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.eligibilityYes:
				eligibilityNo.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.transparent));
				eligibilityNo.setTextColor(ContextCompat.getColor(getActivity(), R.color.cli_yes_no_button_color));
				eligibilityYes.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.black));
				eligibilityYes.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
				permissionView.setVisibility(View.GONE);
				Utils.displayValidationMessage(CLIEligibilityAndPermissionFragment.this.getActivity(), CustomPopUpWindow.MODAL_LAYOUT.INSOLVENCY, "");
				break;
			case R.id.eligibilityNo:
				eligibilityYes.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.transparent));
				eligibilityYes.setTextColor(ContextCompat.getColor(getActivity(), R.color.cli_yes_no_button_color));
				eligibilityNo.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.black));
				eligibilityNo.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
				permissionView.setVisibility(View.VISIBLE);
				IncreaseLimitController ilc = new IncreaseLimitController(getActivity());
				int paddingPixel = 16;
				float density = getActivity().getResources().getDisplayMetrics().density;
				paddingDp = (int) (paddingPixel * density);
				permissionView.setPadding(0, paddingDp, 0, ilc.getScreenHeight(getActivity()));
				scrollView.post(new Runnable() {
					@Override
					public void run() {
						ObjectAnimator.ofInt(scrollView, "scrollY", permissionView.getTop()).setDuration(SLIDE_UP_ANIM_DURATION).start();
					}
				});
				Utils.setScreenName(getActivity(), FirebaseManagerAnalyticsProperties.ScreenNames.CLI_CONSENT);

				break;
			case R.id.permissionYes:
				permissionNo.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.transparent));
				permissionNo.setTextColor(ContextCompat.getColor(getActivity(), R.color.cli_yes_no_button_color));
				permissionYes.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.black));
				permissionYes.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
				llEligibilityView.setVisibility(View.GONE);
				permissionView.setPadding(0, paddingDp, 0, 0);
				FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
				FragmentUtils fragmentUtils = new FragmentUtils();
				fragmentUtils.nextFragment(fragmentManager,
						new CLIAllStepsContainerFragment(), R.id.cliMainFrame);
				break;
			case R.id.permissionNo:
				permissionNo.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.black));
				permissionNo.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
				Utils.displayValidationMessage(CLIEligibilityAndPermissionFragment.this.getActivity(), CustomPopUpWindow.MODAL_LAYOUT.CONFIDENTIAL, "");
				break;
			default:
				break;
		}
	}
}
