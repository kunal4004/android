package za.co.woolworths.financial.services.android.ui.fragments;


import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.views.WTextView;

import static android.R.attr.duration;


/**
 * A simple {@link Fragment} subclass.
 */
public class CLIEligibilityAndPermissionFragment extends Fragment implements View.OnClickListener {

	private LinearLayout permissionView;
	private WTextView eligibilityYes;
	private WTextView eligibilityNo;
	private WTextView permissionYes;
	private WTextView permissionNo;
	private ScrollView scrollView;
	private boolean isEligible;
	private boolean isPermitted;


	public CLIEligibilityAndPermissionFragment() {
		// Required empty public constructor
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view=inflater.inflate(R.layout.cli_eligibility_and_permission_fragment, container, false);
		permissionView=(LinearLayout)view.findViewById(R.id.permissionView);
		eligibilityYes=(WTextView)view.findViewById(R.id.eligibilityYes);
		eligibilityNo=(WTextView)view.findViewById(R.id.eligibilityNo);
		permissionYes=(WTextView)view.findViewById(R.id.permissionYes);
		permissionNo=(WTextView)view.findViewById(R.id.permissionNo);
		scrollView=(ScrollView) view.findViewById(R.id.scrollView);
		eligibilityNo.setOnClickListener(this);
		eligibilityYes.setOnClickListener(this);
		permissionYes.setOnClickListener(this);
		permissionNo.setOnClickListener(this);

		return view;
	}

	@Override
	public void onClick(View view) {
		switch (view.getId())
		{
			case R.id.eligibilityYes:
				eligibilityNo.setBackgroundColor(ContextCompat.getColor(getActivity(),android.R.color.transparent));
				eligibilityNo.setTextColor(ContextCompat.getColor(getActivity(),R.color.cli_yes_no_button_color));
				eligibilityYes.setBackgroundColor(ContextCompat.getColor(getActivity(),R.color.black));
				eligibilityYes.setTextColor(ContextCompat.getColor(getActivity(),R.color.white));
				permissionView.setVisibility(View.GONE);
				break;
			case R.id.eligibilityNo:
				eligibilityYes.setBackgroundColor(ContextCompat.getColor(getActivity(),android.R.color.transparent));
				eligibilityYes.setTextColor(ContextCompat.getColor(getActivity(),R.color.cli_yes_no_button_color));
				eligibilityNo.setBackgroundColor(ContextCompat.getColor(getActivity(),R.color.black));
				eligibilityNo.setTextColor(ContextCompat.getColor(getActivity(),R.color.white));
				permissionView.setVisibility(View.VISIBLE);
				scrollView.post(new Runnable() {
					@Override
					public void run() {
						//scrollView.smoothScrollTo(0,permissionView.getTop());
						ObjectAnimator.ofInt(scrollView, "scrollY",  permissionView.getTop()).setDuration(300).start();
					}
				});
				break;
			case R.id.permissionYes:
				FragmentManager fragmentManager=getActivity().getSupportFragmentManager();
				fragmentManager.beginTransaction()
						.setCustomAnimations(R.anim.fade_in,R.anim.fade_out,R.anim.fade_in,R.anim.fade_out)
						.replace(R.id.cliMainFrame,new CLIAllStepsContainerFragment()).commit();
				break;
			case R.id.permissionNo:
				break;
			default:
				break;
		}
	}
}
