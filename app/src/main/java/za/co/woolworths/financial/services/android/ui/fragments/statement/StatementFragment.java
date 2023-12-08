package za.co.woolworths.financial.services.android.ui.fragments.statement;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.Nullable;
import kotlin.Pair;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.contracts.IResponseListener;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.Account;
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode;
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState;
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
import za.co.woolworths.financial.services.android.ui.activities.WPdfViewerActivity;
import za.co.woolworths.financial.services.android.ui.adapters.StatementAdapter;
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatBubbleVisibility;
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatFloatingActionButtonBubbleView;
import za.co.woolworths.financial.services.android.ui.views.NotificationBadge;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.actionsheet.AccountsErrorHandlerFragment;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager;
import za.co.woolworths.financial.services.android.util.KotlinUtils;
import za.co.woolworths.financial.services.android.util.NetworkChangeListener;
import za.co.woolworths.financial.services.android.util.NetworkManager;
import za.co.woolworths.financial.services.android.util.SessionUtilities;
import za.co.woolworths.financial.services.android.util.StatementUtils;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.wenum.VocTriggerEvent;

public class StatementFragment extends Fragment implements StatementAdapter.StatementListener, View.OnClickListener, NetworkChangeListener {

    public static final String TAG = "StatementFragment";
    private WButton mBtnEmailStatement;
    private StatementAdapter mStatementAdapter;
    private RelativeLayout relNextButton;
    private RecyclerView rclEStatement;
    private ConstraintLayout ctNoResultFound;
    private ConstraintLayout ccProgressLayout;
    private ErrorHandlerView mErrorHandlerView;
    private WButton mBtnRetry;
    private GetStatement mGetStatementFile;
    private int mViewIsLoadingPosition = -1;
    private Call<StatementResponse> cliGetStatements;
    private BroadcastReceiver mConnectionBroadcast;
    private LoadState loadState;
    private boolean viewWasCreated = false;
    private View view;
    private Call<ResponseBody> mGetPdfFile;
    private UserStatement mSelectedStatement;
    private View topMarginView;
    private FloatingActionButton chatWithAgentFloatingButton;
    private NotificationBadge notificationBadge;
    private ImageView onlineIndicatorImageView;
    private ApplyNowState applyNowState;
    public static boolean SHOW_VIEW_STATEMENT_SCREEN = false;
    public static boolean VIEW_STATEMENT_DETAIL = false;
    public static boolean SEND_STATEMENT_SCREEN = false;
    public static boolean SEND_STATEMENT_DETAIL = false;

    public StatementFragment() {
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.statement_fragment, container, false);
        }

        return view;
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState == null & !viewWasCreated) {
            init(view);
            listener();
            setRecyclerView(rclEStatement);
            disableButton();
            loadState = new LoadState();
            loadSuccess();
            mConnectionBroadcast = Utils.connectionBroadCast(getActivity(), this);
            viewWasCreated = true;
            showChatBubble();
        }
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
        chatWithAgentFloatingButton = view.findViewById(R.id.chatBubbleFloatingButton);
        notificationBadge = view.findViewById(R.id.badge);
        onlineIndicatorImageView = view.findViewById(R.id.onlineIndicatorImageView);

        topMarginView = view.findViewById(R.id.topMarginView);
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

        boolean isAtleastOneStatementSelected = false;
        for (UserStatement s : arrStatement) {
            if (s.selectedByUser()) {
                isAtleastOneStatementSelected = true;
                break;
            }
        }

        hideViewProgress();
        mStatementAdapter.updateStatementViewState(isAtleastOneStatementSelected);

        if (isAtleastOneStatementSelected) {
            enableButton();
        } else {
            disableButton();
        }

        mStatementAdapter.refreshBlockOverlay(position);
    }

    @Override
    public void onViewClicked(View v, int position, UserStatement statement) {
        this.mViewIsLoadingPosition = position;
        mSelectedStatement = statement;
        Activity activity = getActivity();
        mGetStatementFile = new GetStatement(statement.docId, String.valueOf(WoolworthsApplication.getProductOfferingId()), statement.docDesc);
        if (activity instanceof StatementActivity) {
            StatementActivity statementActivity = (StatementActivity) activity;
            getPdfFile(statementActivity.getApplyNowStateForStatement());
        }
    }

    private void viewPdfStatement() {
        showViewProgress();
        final FragmentActivity activity = getActivity();
        if (activity == null || !isAdded()) return;
        mGetPdfFile = new OneAppService().getPDFResponse(mGetStatementFile);
        mGetPdfFile.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                if (getActivity() != null) {
                    if (response.isSuccessful()) {
                        if (response.code() == 200) {
                            try {
                                if (response.body() != null) {
                                    String fileName = "statement_" + mSelectedStatement.docDesc;
                                    Intent openPdfIntent = new Intent(activity, WPdfViewerActivity.class);
                                    openPdfIntent.putExtra(WPdfViewerActivity.FILE_NAME, fileName.replaceAll(" ", "_").toLowerCase());
                                    openPdfIntent.putExtra(WPdfViewerActivity.FILE_VALUE, response.body().bytes());
                                    openPdfIntent.putExtra(WPdfViewerActivity.PAGE_TITLE, mSelectedStatement.docDesc);
                                    activity.startActivity(openPdfIntent);
                                }
                            } catch (Exception ex) {
                                FirebaseManager.Companion.logException(ex);
                            }
                        }

                    } else {
                        if(response.errorBody() != null){
                            FirebaseManager.Companion.setCrashlyticsString ("viewPdfStatement - 500 ResponseCode with errorBody ",response.errorBody().source().toString());
                            FirebaseManager.Companion.logException(response.errorBody());
                        }
                        Utils.displayValidationMessage(activity, CustomPopUpWindow.MODAL_LAYOUT.ERROR, activity.getString(R.string.account_statement_error));
                    }
                }
                loadSuccess();
                hideViewProgress();
            }


            @Override
            public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
                Activity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        mErrorHandlerView.showToast();
                        loadFailure();
                        hideViewProgress();
                    });
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        Activity activity = getActivity();
        if (activity == null) return;
        switch (v.getId()) {
            case R.id.btnEmailStatement:
                if (applyNowState == null && activity instanceof StatementActivity){
                        applyNowState = ((StatementActivity) activity).getAccountWithApplyNowState().getFirst();
                }
                displayValidationMessage();
            break;

            case R.id.btnRetry:
                if (NetworkManager.getInstance().isConnectedToNetwork(activity))
                    getStatement();
                break;

            default:
                break;
        }
    }

    public void displayValidationMessage() {
            KotlinUtils.Companion.linkDeviceIfNecessary(
                    getActivity(),
                    applyNowState,
                    () -> { SEND_STATEMENT_DETAIL = true;
                        return null;
                    },
                    () -> {
                        Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.STATEMENT_SENT_TO, createUserStatementRequest());
                        return null;
                    }
            );

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

    private void getStatement() {
        onLoad();
        UserStatement userStatement = new UserStatement(String.valueOf(WoolworthsApplication.getProductOfferingId()), Utils.getDate(6), Utils.getDate(0));
        cliGetStatements = new OneAppService().getStatementResponse(userStatement);
        cliGetStatements.enqueue(new CompletionHandler<>(new IResponseListener<StatementResponse>() {
            @Override
            public void onSuccess(StatementResponse statementResponse) {
                if (statementResponse != null && getActivity() != null) {
                    switch (statementResponse.httpCode) {
                        case 200:
                            setAdapter();
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
                            onLoadComplete();
                            break;

                        case 440:
                            SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, statementResponse.response.stsParams, getActivity());
                            break;
                        default:
                            if (statementResponse.response != null && statementResponse.response.desc != null) {
                                try {
                                    AccountsErrorHandlerFragment accountsErrorHandlerFragment = AccountsErrorHandlerFragment.Companion.newInstance(statementResponse.response.desc);
                                    accountsErrorHandlerFragment.show(getActivity().getSupportFragmentManager(), AccountsErrorHandlerFragment.class.getSimpleName());
                                } catch (IllegalStateException ex) {
                                    FirebaseManager.Companion.logException(ex);
                                }
                            }
                            break;
                    }
                    hideView(ccProgressLayout);
                }
            }

            @Override
            public void onFailure(final Throwable error) {
                if (error == null) return;
                Activity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        onLoadComplete();
                        mErrorHandlerView.networkFailureHandler(error.getMessage());
                    });
                }
            }
        }, StatementResponse.class));

    }

    public void hideView(View view) {
        view.setVisibility(View.GONE);
    }

    public void showView(View view) {
        view.setVisibility(View.VISIBLE);
    }

    private void onLoad() {
        mErrorHandlerView.hideErrorHandler();
        showView(ccProgressLayout);
        hideView(topMarginView);
        hideView(relNextButton);
    }


    public void onLoadComplete() {
        hideView(ccProgressLayout);
        showView(topMarginView);
        showView(relNextButton);
    }

    public void getPdfFile(ApplyNowState state) {
        applyNowState = state;
        if (applyNowState != null) {
            KotlinUtils.Companion.linkDeviceIfNecessary(
                    getActivity(),
                    applyNowState,
                    () -> {
                        VIEW_STATEMENT_DETAIL = true;
                        return null;
                    },
                    () -> {
                        viewPdfStatement();
                        return null;
                    }
            );
        } else {
            viewPdfStatement();
        }
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
            activity.runOnUiThread(() -> {
                if (NetworkManager.getInstance().isConnectedToNetwork(getActivity())) {
                    if (!loadState.onLoanCompleted()) {
                        getPdfFile(applyNowState);
                    }
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Activity activity = getActivity();
        if (activity != null) {
            Utils.setScreenName(activity, FirebaseManagerAnalyticsProperties.ScreenNames.STATEMENTS_LIST);
            if (activity instanceof StatementActivity) {
                activity.registerReceiver(mConnectionBroadcast, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
                ((StatementActivity) activity).setTitle(getString(R.string.statement));
            }
        }
        if (SHOW_VIEW_STATEMENT_SCREEN) {
            SHOW_VIEW_STATEMENT_SCREEN = false;
            viewPdfStatement();
        } else if (SEND_STATEMENT_SCREEN) {
            SEND_STATEMENT_SCREEN = false;
            Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.STATEMENT_SENT_TO, createUserStatementRequest());
        }
    }

    @Override
    public void onPause() {
        Activity activity = getActivity();
        if (activity instanceof StatementActivity) {
            activity.unregisterReceiver(mConnectionBroadcast);
        }
        super.onPause();
    }

    private void loadSuccess() {
        loadState.setLoadComplete(true);
    }

    private void loadFailure() {
        loadState.setLoadComplete(false);
    }

    private void showViewProgress() {
        mStatementAdapter.onViewClicked(mViewIsLoadingPosition, true);
    }

    private void hideViewProgress() {
        mStatementAdapter.onViewClicked(mViewIsLoadingPosition, false);
    }

    private SendUserStatementRequest createUserStatementRequest() {
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

    private void showChatBubble() {
        Activity activity = getActivity();
        if (activity == null) return;
        Pair<ApplyNowState, Account> account = ((StatementActivity) activity).getAccountWithApplyNowState();
        ArrayList<Account> accountList = new ArrayList<>();
        accountList.add(account.getSecond());

        VocTriggerEvent vocTriggerEvent;
        if (account.getSecond() != null && account.getSecond().productGroupCode != null) {
            if (account.getSecond().productGroupCode.equalsIgnoreCase(AccountsProductGroupCode.STORE_CARD.getGroupCode())) {
                vocTriggerEvent = VocTriggerEvent.CHAT_SC_STATEMENT;
            } else if (account.getSecond().productGroupCode.equalsIgnoreCase(AccountsProductGroupCode.PERSONAL_LOAN.getGroupCode())) {
                vocTriggerEvent = VocTriggerEvent.CHAT_PL_STATEMENT;
            } else {
                vocTriggerEvent = VocTriggerEvent.CHAT_CC_STATEMENT;
            }
        }

        ChatFloatingActionButtonBubbleView inAppChatTipAcknowledgement = new ChatFloatingActionButtonBubbleView((StatementActivity) activity, new ChatBubbleVisibility(accountList, activity), chatWithAgentFloatingButton, account.getFirst(), rclEStatement, notificationBadge, onlineIndicatorImageView, vocTriggerEvent);
        inAppChatTipAcknowledgement.build();
    }
}



