package za.co.woolworths.financial.services.android.ui.fragments.cli;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.ui.activities.cli.CLIPhase2Activity;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.util.MultiClickPreventer;
import za.co.woolworths.financial.services.android.util.Utils;

public class CLIPOIProblemFragment extends Fragment implements View.OnClickListener {

	View view;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		CLIPhase2Activity cliPhase2 = (CLIPhase2Activity) CLIPOIProblemFragment.this.getActivity();
		cliPhase2.hideBurgerButton();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		if (view == null)
			view = inflater.inflate(R.layout.cli_poi_problem, container, false);
		return view;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		init(view);

	}

	@Override
	public void onResume() {
		super.onResume();
		Utils.setScreenName(getActivity(), FirebaseManagerAnalyticsProperties.ScreenNames.CLI_POI_ERROR);
	}

	private void init(View view) {
		WButton btnContinue = (WButton) view.findViewById(R.id.btnContinue);
		LinearLayout llNextButtonLayout = (LinearLayout) view.findViewById(R.id.llNextButtonLayout);
		btnContinue.setText(getString(R.string.call_us));
		btnContinue.setOnClickListener(this);
		showView(llNextButtonLayout);
	}

	private void showView(View view) {
		view.setVisibility(View.VISIBLE);
	}

	@Override
	public void onClick(View v) {
		MultiClickPreventer.preventMultiClick(v);
		switch (v.getId()) {
			case R.id.btnContinue:
				Utils.makeCall(getActivity(), getActivity().getResources().getString(R.string.poi_problem_phone_no));
				break;
			default:
				break;
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.search_item, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_search:
				goBack();
				break;
		}
		return false;
	}

	private void goBack() {
		CLIPOIProblemFragment.this.getActivity().finish();
		CLIPOIProblemFragment.this.getActivity().overridePendingTransition(R.anim.stay, R.anim.slide_down_anim);
	}
}
