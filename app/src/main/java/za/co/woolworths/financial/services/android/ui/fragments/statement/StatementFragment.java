package za.co.woolworths.financial.services.android.ui.fragments.statement;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.contracts.RequestListener;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.statement.GetStatement;
import za.co.woolworths.financial.services.android.models.dto.statement.SendUserStatementRequest;
import za.co.woolworths.financial.services.android.models.dto.statement.StatementResponse;
import za.co.woolworths.financial.services.android.models.dto.statement.USDocument;
import za.co.woolworths.financial.services.android.models.dto.statement.USDocuments;
import za.co.woolworths.financial.services.android.models.dto.statement.UserStatement;
import za.co.woolworths.financial.services.android.models.network.CompletionHandler;
import za.co.woolworths.financial.services.android.models.network.OneAppService;
import za.co.woolworths.financial.services.android.models.service.event.LoadState;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.activities.StatementActivity;
import za.co.woolworths.financial.services.android.ui.adapters.StatementAdapter;
import za.co.woolworths.financial.services.android.ui.views.SlidingUpPanelLayout;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.FragmentUtils;
import za.co.woolworths.financial.services.android.util.NetworkChangeListener;
import za.co.woolworths.financial.services.android.util.NetworkManager;
import za.co.woolworths.financial.services.android.util.SessionUtilities;
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
    private Call<StatementResponse> cliGetStatements;
    private BroadcastReceiver mConnectionBroadcast;
    private LoadState loadState;
    private SlidingUpPanelLayout.PanelState panelIsCollapsed = SlidingUpPanelLayout.PanelState.COLLAPSED;
    private boolean viewWasCreated = false;
    private final String TAG = StatementFragment.this.getClass().getSimpleName();
    View view;
    private Call<retrofit2.Response<ResponseBody>> mGetPdfFile;

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
        mSlideUpPanelLayout = v.findViewById(R.id.sliding_layout);
    }

    private void showSlideUpPanel() {
        mSlideUpPanelLayout.setAnchorPoint(1.0f);
        mSlideUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
        mSlideUpPanelLayout.setTouchEnabled(false);
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
        ctNoResultFound = view.findViewById(R.id.ctNoResultFound);
        ccProgressLayout = view.findViewById(R.id.ccProgressLayout);
        rclEStatement = view.findViewById(R.id.rclEStatement);
        relNextButton = view.findViewById(R.id.relNextButton);
        mBtnEmailStatement = view.findViewById(R.id.btnEmailStatement);
        RelativeLayout relativeLayout = view.findViewById(R.id.no_connection_layout);
        mBtnRetry = view.findViewById(R.id.btnRetry);

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
                if (NetworkManager.getInstance().isConnectedToNetwork(getActivity()))
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
        UserStatement userStatement = new UserStatement(String.valueOf(WoolworthsApplication.getProductOfferingId()), Utils.getDate(6), Utils.getDate(0));
        cliGetStatements = OneAppService.INSTANCE.getStatementResponse(userStatement);
        cliGetStatements.enqueue(new CompletionHandler<>(new RequestListener<StatementResponse>() {
            @Override
            public void onSuccess(StatementResponse statementResponse) {
                if (statementResponse != null && getActivity() !=null) {
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

                            SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, statementResponse.response.stsParams, getActivity());
                            break;

                        default:
                            if (statementResponse.response != null) {
                                Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.STATEMENT_ERROR, statementResponse.response.desc);
                            }
                            break;
                    }
                    onLoadComplete();
                }
            }

            @Override
            public void onFailure(final Throwable error) {
                if (error == null)return;
                Activity activity = getActivity();
                if (activity !=null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onLoadComplete();
                            mErrorHandlerView.networkFailureHandler(error.getMessage());
                        }
                    });
                }
            }
        },StatementResponse.class));

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
        showViewProgress();
        final FragmentActivity activity = getActivity();
        if (activity == null) return;
        mGetPdfFile = OneAppService.INSTANCE.getPDFResponse(mGetStatementFile);
        mGetPdfFile.enqueue(new Callback<Response<ResponseBody>>() {
            @Override
            public void onResponse(Call<Response<ResponseBody>> call, Response<Response<ResponseBody>> response) {
                if (getActivity() != null) {
                    loadSuccess();
                    hideViewProgress();
                    if (response.code() == 200) {
                        try {
                            StatementUtils statementUtils = new StatementUtils(activity);
                            if (response.body() != null) {
                                if (response.body().body() != null) {
                                    statementUtils.savePDF(response.body().body().byteStream());
                                }
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    PreviewStatement previewStatement = new PreviewStatement();
                                    FragmentUtils fragmentUtils = new FragmentUtils(activity);
                                    FragmentManager fragmentManager = activity.getSupportFragmentManager();
                                    fragmentUtils.openFragment(fragmentManager, previewStatement, R.id.flAccountStatement);
                                    showSlideUpPanel();
                                } else {
                                    launchOpenPDFIntent();
                                }
                            }
                        } catch (Exception ex) {
                            Log.d(TAG, ex.getMessage());
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<Response<ResponseBody>> call, Throwable t) {
                Activity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mErrorHandlerView.showToast();
                            loadFailure();
                            hideViewProgress();
                        }
                    });
                }
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

            if ((mGetPdfFile != null) && !mGetPdfFile.isCanceled())
                mGetPdfFile.cancel();
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
                    if (NetworkManager.getInstance().isConnectedToNetwork(getActivity())) {
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
        Utils.setScreenName(getActivity(), FirebaseManagerAnalyticsProperties.ScreenNames.STATEMENTS_LIST);
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

    private void launchOpenPDFIntent() {
        File file = new File(getActivity().getExternalFilesDir("woolworth") + "/Files/" + "statement.pdf");
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            Utils.deleteDirectory(new File(getActivity().getExternalFilesDir("woolworth") + "/Files/" + "statement.pdf"));
        } catch (Exception ex) {
            Log.d("deleteDirectoryErr", ex.toString());
        }
    }
}



