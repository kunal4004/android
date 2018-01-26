package za.co.woolworths.financial.services.android.ui.fragments.statement;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.statement.GetStatement;
import za.co.woolworths.financial.services.android.models.dto.statement.SendUserStatementRequest;
import za.co.woolworths.financial.services.android.models.dto.statement.USDocument;
import za.co.woolworths.financial.services.android.models.dto.statement.USDocuments;
import za.co.woolworths.financial.services.android.models.dto.statement.UserStatement;
import za.co.woolworths.financial.services.android.models.dto.statement.StatementResponse;
import za.co.woolworths.financial.services.android.models.rest.GetStatements;
import za.co.woolworths.financial.services.android.models.service.event.LoadState;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.activities.StatementActivity;
import za.co.woolworths.financial.services.android.ui.adapters.StatementAdapter;
import za.co.woolworths.financial.services.android.ui.views.SlidingUpPanelLayout;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.FragmentUtils;
import za.co.woolworths.financial.services.android.util.NetworkChangeListener;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.SessionExpiredUtilities;
import za.co.woolworths.financial.services.android.util.StatementUtils;
import za.co.woolworths.financial.services.android.util.Utils;

public class StatementFragment extends Fragment implements StatementAdapter.StatementListener, View.OnClickListener, NetworkChangeListener {

	private WButton mBtnEmailStatement;
	private StatementAdapter mStatementAdapter;
	private RelativeLayout relNextButton;
	private RecyclerView rclEStatement;
	private ConstraintLayout ctNoResultFound;
	private ConstraintLayout ccProgressLayout;
	private ErrorHandlerView mErrorHandlerView;
	private WButton mBtnRetry;
	private GetStatement mGetStatementFile;
	private SlidingUpPanelLayout mSlideUpPanelLayout;
	private int mViewIsLoadingPosition = -1;
	private GetStatements cliGetStatements;
	private BroadcastReceiver mConnectionBroadcast;
	private LoadState loadState;
	private SlidingUpPanelLayout.PanelState panelIsCollapsed = SlidingUpPanelLayout.PanelState.COLLAPSED;
	private boolean viewWasCreated = false;

	View view;

	public StatementFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		if (view == null) {
			view = inflater.inflate(R.layout.statement_fragment, container, false);
		}

		return view;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (savedInstanceState == null & !viewWasCreated) {
			slideUpPanel(view);
			init(view);
			listener();
			setAdapter();
			setRecyclerView(rclEStatement);
			disableButton();
			slideUpPanelListener();
			loadState = new LoadState();
			loadSuccess();
			mConnectionBroadcast = Utils.connectionBroadCast(getActivity(), this);
			viewWasCreated = true;
		}
	}

	private void slideUpPanel(View v) {
		mSlideUpPanelLayout = (SlidingUpPanelLayout) v.findViewById(R.id.sliding_layout);
	}

	private void showSlideUpPanel() {
		mSlideUpPanelLayout.setAnchorPoint(1.0f);
		mSlideUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
		//mSlideUpPanelLayout.setScrollableViewHelper(new NestedScrollableViewHelper(mScrollProductDetail));
	}

	private void setAdapter() {
		mStatementAdapter = new StatementAdapter(this);
		UserStatement datum = new UserStatement();
		datum.setHeader(true);
		mStatementAdapter.add(datum);
		rclEStatement.setAdapter(mStatementAdapter);
	}

	private void listener() {
		mBtnEmailStatement.setOnClickListener(this);
		mBtnRetry.setOnClickListener(this);
	}

	private void init(View view) {
		ctNoResultFound = (ConstraintLayout) view.findViewById(R.id.ctNoResultFound);
		ccProgressLayout = (ConstraintLayout) view.findViewById(R.id.ccProgressLayout);
		rclEStatement = (RecyclerView) view.findViewById(R.id.rclEStatement);
		relNextButton = (RelativeLayout) view.findViewById(R.id.relNextButton);
		mBtnEmailStatement = (WButton) view.findViewById(R.id.btnEmailStatement);
		RelativeLayout relativeLayout = (RelativeLayout) view.findViewById(R.id.no_connection_layout);
		mBtnRetry = (WButton) view.findViewById(R.id.btnRetry);

		mErrorHandlerView = new ErrorHandlerView(getActivity()
				, relativeLayout);
		mErrorHandlerView.setMargin(relativeLayout, 0, 0, 0, 0);
	}

	private void setRecyclerView(RecyclerView rclEStatement) {
		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
		rclEStatement.setLayoutManager(layoutManager);
		getStatement();
	}

	@Override
	public void onItemClicked(View v, int position) {
		this.mViewIsLoadingPosition = position;
		ArrayList<UserStatement> arrStatement = mStatementAdapter.getStatementList();
		UserStatement statement = arrStatement.get(position);
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
		for (UserStatement s : arrStatement) {
			if (s.selectedByUser()) {
				arrayContainTrue = true;
			}
		}

		hideViewProgress();
		mStatementAdapter.updateStatementViewState(arrayContainTrue);

		if (arrayContainTrue) {
			enableButton();
		} else {
			disableButton();
		}

		mStatementAdapter.refreshBlockOverlay(position);
	}

	@Override
	public void onViewClicked(View v, int position, UserStatement statement) {
		this.mViewIsLoadingPosition = position;
		Activity activity = getActivity();
		mGetStatementFile = new GetStatement(statement.docId, String.valueOf(WoolworthsApplication.getProductOfferingId()), statement.docDesc);
		if (activity instanceof StatementActivity) {
			StatementActivity statementActivity = (StatementActivity) activity;
			statementActivity.checkPermission();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnEmailStatement:
				Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.STATEMENT_SENT_TO, createUserStatementRequest());
				break;

			case R.id.btnRetry:
				if (new ConnectionDetector().isOnline(getActivity()))
					getStatement();
				break;

			default:
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

	public void getStatement() {
		onLoad();
		UserStatement statement1 = new UserStatement(String.valueOf(WoolworthsApplication.getProductOfferingId()), Utils.getDate(6), Utils.getDate(0));
		cliGetStatements = new GetStatements(getActivity(), statement1, new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				StatementResponse statementResponse = (StatementResponse) object;
				if (statementResponse != null) {
					Response response = statementResponse.response;
					switch (statementResponse.httpCode) {
						case 200:
							List<UserStatement> statement = statementResponse.data;
							if (statement.size() == 0) {
								showView(ctNoResultFound);
							} else {
								hideView(ctNoResultFound);
								int index = 0;
								for (UserStatement d : statement) {
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
								Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.STATEMENT_ERROR, response.desc);
							}
							break;
					}
				}
				onLoadComplete();
			}

			@Override
			public void onFailure(String e) {
				onLoadComplete();
				mErrorHandlerView.networkFailureHandler(e);
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
		mErrorHandlerView.hideErrorHandler();
		showView(ccProgressLayout);
	}


	public void onLoadComplete() {
		hideView(ccProgressLayout);
	}

	public void getPDFFile() {
		WoolworthsApplication mWoolWorthsApplication = ((WoolworthsApplication) StatementFragment.this.getActivity().getApplication());
		showViewProgress();

		mWoolWorthsApplication.getAsyncApi().getPDFResponse(mGetStatementFile, new Callback<retrofit.client.Response>() {

			@Override
			public void success(retrofit.client.Response response, retrofit.client.Response response2) {
				switch (response.getStatus()) {
					case 200:
						try {
							StatementUtils statementUtils = new StatementUtils(getActivity());
							statementUtils.savePDF(response2.getBody().in());
							PreviewStatement previewStatement = new PreviewStatement();
							FragmentUtils fragmentUtils = new FragmentUtils(getActivity());
							FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
							fragmentUtils.openFragment(fragmentManager,
									previewStatement, R.id.flAccountStatement);
							showSlideUpPanel();
						} catch (Exception ignored) {
						}
						break;
					default:
						break;
				}
				loadSuccess();
				hideViewProgress();
			}

			@Override
			public void failure(RetrofitError error) {
				if (error.getKind().name().equalsIgnoreCase("NETWORK")) {
					mErrorHandlerView.showToast();
					loadFailure();
				}
				hideViewProgress();
			}
		});
	}

	private void slideUpPanelListener() {
		mSlideUpPanelLayout.setFadeOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mSlideUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
			}
		});
		mSlideUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
			@Override
			public void onPanelSlide(View panel, float slideOffset) {
				if (slideOffset == 0.0) {
					mSlideUpPanelLayout.setAnchorPoint(1.0f);
				}
			}

			@Override
			public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState,
											SlidingUpPanelLayout.PanelState newState) {
				Activity activity = getActivity();
				if (activity != null) {
					StatementActivity statementActivity = (StatementActivity) activity;
					switch (newState) {
						case COLLAPSED:
							panelIsCollapsed = SlidingUpPanelLayout.PanelState.COLLAPSED;
							statementActivity.showHomeButton();
							break;

						case DRAGGING:
							panelIsCollapsed = SlidingUpPanelLayout.PanelState.DRAGGING;
							break;

						case EXPANDED:
							panelIsCollapsed = SlidingUpPanelLayout.PanelState.EXPANDED;
							statementActivity.showAccountStatementButton();
							break;
						default:
							break;
					}
				}
			}
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Activity activity = getActivity();
		if (activity != null) {
			StatementUtils statementUtils = new StatementUtils(activity);
			statementUtils.cancelRequest(cliGetStatements);
		}
	}

	@Override
	public void onConnectionChanged() {
		retryConnect();
	}

	private void retryConnect() {
		Activity activity = getActivity();
		if (activity != null) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (new ConnectionDetector().isOnline(getActivity())) {
						if (!loadState.onLoanCompleted()) {
							getPDFFile();
						}
					}
				}
			});
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		Activity activity = getActivity();
		if (activity instanceof StatementActivity) {
			activity.registerReceiver(mConnectionBroadcast, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
			((StatementActivity) activity).setTitle(getString(R.string.statement));
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		Activity activity = getActivity();
		if (activity != null) {
			activity.unregisterReceiver(mConnectionBroadcast);
		}
	}

	public SlidingUpPanelLayout.PanelState isSlideUpPanelEnabled() {
		return panelIsCollapsed;
	}

	public void hideSlideUpPanel() {
		switch (panelIsCollapsed) {
			case EXPANDED:
				mSlideUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
				break;

			case DRAGGING:
				break;
			default:
				break;
		}
	}

	private void loadSuccess() {
		loadState.setLoadComplete(true);
	}

	private void loadFailure() {
		loadState.setLoadComplete(false);
	}

	public void showViewProgress() {
		mStatementAdapter.onViewClicked(mViewIsLoadingPosition, true);
	}

	public void hideViewProgress() {
		mStatementAdapter.onViewClicked(mViewIsLoadingPosition, false);
	}

	public SendUserStatementRequest createUserStatementRequest() {
		SendUserStatementRequest sendUserStatementRequest = new SendUserStatementRequest();
		List<USDocument> documents = new ArrayList<>();
		for (UserStatement us : mStatementAdapter.getStatementList()) {
			if (us.selectedByUser()) {
				documents.add(new USDocument(us.docType, us.docId, us.docDesc));
			}
		}
		USDocuments usDocuments = new USDocuments();
		usDocuments.document = documents;
		sendUserStatementRequest.documents = usDocuments;
		sendUserStatementRequest.productOfferingId = String.valueOf(WoolworthsApplication.getProductOfferingId());
		return sendUserStatementRequest;
	}
}



