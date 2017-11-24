package za.co.woolworths.financial.services.android.ui.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.dto.Statement;
import za.co.woolworths.financial.services.android.ui.adapters.StatementAdapter;

public class EStatementFragment extends Fragment implements StatementAdapter.StatementListener {

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
	}

	private void init(View view) {
		RecyclerView rclEStatement = (RecyclerView) view.findViewById(R.id.rclEStatement);
		LinearLayout llNextButtonLayout = (LinearLayout) view.findViewById(R.id.llNextButtonLayout);
		showView(llNextButtonLayout);
		setRecyclerView(rclEStatement);
	}

	private void showView(View v) {
//		v.setVisibility(View.VISIBLE);
//		v.setBackgroundColor(Color.WHITE);
	}

	private void setRecyclerView(RecyclerView rclEStatement) {
		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
		rclEStatement.setLayoutManager(layoutManager);
		StatementAdapter statementAdapter = new StatementAdapter(this);
		statementAdapter.add(new Statement("HEADER"));
		statementAdapter.add(new Statement("JANUARY 2016"));
		statementAdapter.add(new Statement("FEBRUARY 2016"));
		statementAdapter.add(new Statement("MARCH 2016"));
		statementAdapter.add(new Statement("APRIL 2016"));
		statementAdapter.add(new Statement("MAY 2016"));
		statementAdapter.add(new Statement("JUNE 2016"));
		statementAdapter.add(new Statement("JULY 2016"));
		statementAdapter.add(new Statement("AUGUST 2016"));
		statementAdapter.add(new Statement("SEPTEMBER 2016"));
		statementAdapter.add(new Statement("OCTOBER 2016"));
		statementAdapter.add(new Statement("NOVEMBER 2016"));
		rclEStatement.setAdapter(statementAdapter);
	}

	@Override
	public void onItemClicked(View v, int position) {

	}

	@Override
	public void onViewClicked(View v, int position) {

	}
}
