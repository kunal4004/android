package za.co.woolworths.financial.services.android.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.FragmentLifecycle;
import za.co.woolworths.financial.services.android.ui.activities.StatementActivity;
import za.co.woolworths.financial.services.android.ui.views.WButton;

/**
 * Created by W7099877 on 30/11/2016.
 */

public class WPersonalLoanEmptyFragment extends Fragment implements FragmentLifecycle {

	public WButton applyNow;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.logged_out_state_personal_loan, container, false);
		return view;
	}

	@Override
	public void onPauseFragment() {

	}

	@Override
	public void onResumeFragment() {

	}
}
