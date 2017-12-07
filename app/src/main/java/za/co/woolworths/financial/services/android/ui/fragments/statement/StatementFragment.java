package za.co.woolworths.financial.services.android.ui.fragments.statement;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.statement.Statement;
import za.co.woolworths.financial.services.android.models.dto.statement.StatementResponse;
import za.co.woolworths.financial.services.android.models.rest.CLIGetStatements;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.adapters.StatementAdapter;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.SessionExpiredUtilities;
import za.co.woolworths.financial.services.android.util.Utils;


public class StatementFragment extends Fragment implements StatementAdapter.StatementListener, View.OnClickListener {

	private WButton mBtnEmailStatement;
	private StatementAdapter mStatementAdapter;
	private RelativeLayout relNextButton;
	private RecyclerView rclEStatement;
	private ConstraintLayout ctNoResultFound;
	private ConstraintLayout ccProgressLayout;
	private ProgressBar pbCircular;

	public StatementFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		return inflater.inflate(R.layout.statement_fragment, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		init(view);
		listener();
		setAdapter();
		resetAdapter();
		disableButton();
	}

	private void setAdapter() {
		mStatementAdapter = new StatementAdapter(this);
		Statement datum = new Statement();
		datum.setHeader(true);
		mStatementAdapter.add(datum);
	}

	private void listener() {
		mBtnEmailStatement.setOnClickListener(this);
	}

	private void init(View view) {

		ctNoResultFound = (ConstraintLayout) view.findViewById(R.id.ctNoResultFound);
		ccProgressLayout = (ConstraintLayout) view.findViewById(R.id.ccProgressLayout);
		rclEStatement = (RecyclerView) view.findViewById(R.id.rclEStatement);
		relNextButton = (RelativeLayout) view.findViewById(R.id.relNextButton);
		mBtnEmailStatement = (WButton) view.findViewById(R.id.btnEmailStatement);
		pbCircular = (ProgressBar)view.findViewById(R.id.pbCircular);
		setRecyclerView(rclEStatement);
	}

	private void setRecyclerView(RecyclerView rclEStatement) {
		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
		rclEStatement.setLayoutManager(layoutManager);
		getStatement();
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
		mStatementAdapter.updateStatementViewState(arrayContainTrue);

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
				Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.STATEMENT_SENT_TO);
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

	private void resetAdapter() {
		rclEStatement.setAdapter(mStatementAdapter);
	}

	public void getStatement() {
		onLoad();
//		Statement statement = new Statement(String.valueOf(WoolworthsApplication.getProductOfferingId()), "6007850115578203", Utils.getDate(6), Utils.getDate(0));
		Statement statement = new Statement(String.valueOf(WoolworthsApplication.getProductOfferingId()), "6007850115578203", "2017-01-01", "2017-11-27");

		CLIGetStatements cliGetStatements = new CLIGetStatements(getActivity(), statement, new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				StatementResponse statementResponse = (StatementResponse) object;
				if (statementResponse != null) {
					Response response = statementResponse.response;
					if (statementResponse != null) {
						switch (statementResponse.httpCode) {
							case 200:
								List<Statement> statement = statementResponse.data;
								if (statement.size() == 0) {
									showView(ctNoResultFound);
								} else {
									hideView(ctNoResultFound);
									int index = 0;
									for (Statement d : statement) {
										mStatementAdapter.add(d);
										mStatementAdapter.refreshBlockOverlay(index);
										index++;
									}
								}
								break;

							case 440:
								SessionExpiredUtilities.INSTANCE.setAccountSessionExpired(getActivity(), response.stsParams);
								break;

							default:
								if (response != null) {
									Utils.displayValidationMessage(getActivity(),
											CustomPopUpWindow.MODAL_LAYOUT.ERROR,
											response.desc);
								}
								break;
						}
					}
				}
				onLoadComplete();
			}

			@Override
			public void onFailure(String e) {
				onLoadComplete();
			}
		});

		cliGetStatements.execute();
	}

	public void hideView(View view) {
		view.setVisibility(View.GONE);
	}

	public void showView(View view) {
		view.setVisibility(View.VISIBLE);
	}

	public void onLoad() {
		showView(ccProgressLayout);
	}

	public void onLoadComplete() {
		hideView(ccProgressLayout);
	}
}
