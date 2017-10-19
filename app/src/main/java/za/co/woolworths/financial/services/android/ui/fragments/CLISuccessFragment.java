package za.co.woolworths.financial.services.android.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.views.WButton;

public class CLISuccessFragment extends Fragment implements View.OnClickListener {

	private View view;
	private WButton mBtnBackToAccounts;

	public CLISuccessFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		if (view == null)
			view = inflater.inflate(R.layout.cli_success_fragment, container, false);

		return view;
	}

	private void initUI() {
		mBtnBackToAccounts = (WButton) view.findViewById(R.id.btnContinue);
	}

	private void setListener() {
		mBtnBackToAccounts.setOnClickListener(this);
	}

	private void setContent() {
		mBtnBackToAccounts.setText(getActivity().getResources().getString(R.string.cli_back_to_acc));
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.btnContinue:
				closeActivity();
				break;
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Do something that differs the Activity's menu here
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		closeActivity();
		return false;
	}

	public void closeActivity() {
		getActivity().finish();
		getActivity().overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
	}
}
