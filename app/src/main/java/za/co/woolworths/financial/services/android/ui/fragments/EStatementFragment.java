package za.co.woolworths.financial.services.android.ui.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import java.util.ArrayList;

import za.co.woolworths.financial.services.android.models.dto.Statement;
import za.co.woolworths.financial.services.android.ui.adapters.StatementAdapter;
import za.co.woolworths.financial.services.android.ui.views.WButton;

public class EStatementFragment extends Fragment implements StatementAdapter.StatementListener, View.OnClickListener {

	private WButton mBtnEmailStatement;
	private StatementAdapter mStatementAdapter;
	private RelativeLayout relNextButton;

	public EStatementFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		return inflater.inflate(R.layout.account_e_statement_fragment, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		init(view);
		listener();
		disableButton();
	}

	private void listener() {
		mBtnEmailStatement.setOnClickListener(this);
	}

	private void init(View view) {
		RecyclerView rclEStatement = (RecyclerView) view.findViewById(R.id.rclEStatement);
		relNextButton = (RelativeLayout) view.findViewById(R.id.relNextButton);
		mBtnEmailStatement = (WButton) view.findViewById(R.id.btnEmailStatement);
		setRecyclerView(rclEStatement);
	}

	private void setRecyclerView(RecyclerView rclEStatement) {
		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
		rclEStatement.setLayoutManager(layoutManager);
		mStatementAdapter = new StatementAdapter(this);
		mStatementAdapter.add(new Statement("HEADER", false));
		mStatementAdapter.add(new Statement("JANUARY 2016", false));
		mStatementAdapter.add(new Statement("FEBRUARY 2016", false));
		mStatementAdapter.add(new Statement("MARCH 2016", false));
		mStatementAdapter.add(new Statement("APRIL 2016", false));
		mStatementAdapter.add(new Statement("MAY 2016", false));
		mStatementAdapter.add(new Statement("JUNE 2016", false));
		mStatementAdapter.add(new Statement("JULY 2016", false));
		mStatementAdapter.add(new Statement("AUGUST 2016", false));
		mStatementAdapter.add(new Statement("SEPTEMBER 2016", false));
		mStatementAdapter.add(new Statement("OCTOBER 2016", false));
		mStatementAdapter.add(new Statement("NOVEMBER 2016", false));
		mStatementAdapter.add(new Statement("HEADER", false));
		mStatementAdapter.add(new Statement("JANUARY 2016", false));
		mStatementAdapter.add(new Statement("FEBRUARY 2016", false));
		mStatementAdapter.add(new Statement("MARCH 2016", false));
		mStatementAdapter.add(new Statement("APRIL 2016", false));
		mStatementAdapter.add(new Statement("MAY 2016", false));
		mStatementAdapter.add(new Statement("JUNE 2016", false));
		mStatementAdapter.add(new Statement("JULY 2016", false));
		mStatementAdapter.add(new Statement("AUGUST 2016", false));
		mStatementAdapter.add(new Statement("SEPTEMBER 2016", false));
		mStatementAdapter.add(new Statement("OCTOBER 2016", false));
		mStatementAdapter.add(new Statement("NOVEMBER 2016", false));
		mStatementAdapter.add(new Statement("FEBRUARY 2016", false));
		mStatementAdapter.add(new Statement("MARCH 2016", false));
		mStatementAdapter.add(new Statement("APRIL 2016", false));
		mStatementAdapter.add(new Statement("MAY 2016", false));
		mStatementAdapter.add(new Statement("JUNE 2016", false));
		mStatementAdapter.add(new Statement("JULY 2016", false));
		mStatementAdapter.add(new Statement("AUGUST 2016", false));
		mStatementAdapter.add(new Statement("SEPTEMBER 2016", false));
		mStatementAdapter.add(new Statement("OCTOBER 2016", false));
		mStatementAdapter.add(new Statement("NOVEMBER 2016", false));
		mStatementAdapter.add(new Statement("HEADER", false));
		mStatementAdapter.add(new Statement("JANUARY 2016", false));
		mStatementAdapter.add(new Statement("FEBRUARY 2016", false));
		mStatementAdapter.add(new Statement("MARCH 2016", false));
		mStatementAdapter.add(new Statement("APRIL 2016", false));
		mStatementAdapter.add(new Statement("MAY 2016", false));
		mStatementAdapter.add(new Statement("JUNE 2016", false));
		mStatementAdapter.add(new Statement("JULY 2016", false));
		mStatementAdapter.add(new Statement("AUGUST 2016", false));
		mStatementAdapter.add(new Statement("SEPTEMBER 2016", false));
		mStatementAdapter.add(new Statement("OCTOBER 2016", false));
		mStatementAdapter.add(new Statement("NOVEMBER 2016", false));
		rclEStatement.setAdapter(mStatementAdapter);
	}

	@Override
	public void onItemClicked(View v, int position) {
		ArrayList<Statement> arrStatement = mStatementAdapter.getStatementList();
		Statement statement = arrStatement.get(position);
		boolean selectedByUser = statement.selectedByUser();
		/*
		  true - previously selected by user
		  set to false
		 */
		if (selectedByUser) {
			statement.setSelectedByUser(false);
		} else {
			statement.setSelectedByUser(true);
		}

		boolean arrayContainTrue = false;
		for (Statement s : arrStatement) {
			if (s.selectedByUser()) {
				arrayContainTrue = true;
			}
		}

		if (arrayContainTrue) {
			enableButton();
		} else {
			disableButton();
		}

		mStatementAdapter.refreshBlockOverlay(position);
	}

	@Override
	public void onViewClicked(View v, int position) {

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnEmailStatement:

				break;
		}
	}

	private void disableButton() {
		mBtnEmailStatement.setAlpha(1.0f);
		relNextButton.setAlpha(0.35f);
		mBtnEmailStatement.setEnabled(false);
	}

	private void enableButton() {
		mBtnEmailStatement.setAlpha(1.0f);
		relNextButton.setAlpha(1.0f);
		mBtnEmailStatement.setEnabled(true);
	}
}
