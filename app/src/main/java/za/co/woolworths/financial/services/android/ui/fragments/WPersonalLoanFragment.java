package za.co.woolworths.financial.services.android.ui.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.contracts.RequestListener;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.Account;
import za.co.woolworths.financial.services.android.models.dto.AccountsResponse;
import za.co.woolworths.financial.services.android.models.dto.OfferActive;
import za.co.woolworths.financial.services.android.models.network.CompletionHandler;
import za.co.woolworths.financial.services.android.models.network.OneAppService;
import za.co.woolworths.financial.services.android.models.service.event.BusStation;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.activities.DebitOrderActivity;
import za.co.woolworths.financial.services.android.ui.activities.MyAccountCardsActivity;
import za.co.woolworths.financial.services.android.ui.activities.StatementActivity;
import za.co.woolworths.financial.services.android.ui.activities.WTransactionsActivity;
import za.co.woolworths.financial.services.android.ui.activities.bpi.BPIBalanceProtectionActivity;
import za.co.woolworths.financial.services.android.ui.views.WMaterialShowcaseView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.FontHyperTextParser;
import za.co.woolworths.financial.services.android.util.FragmentLifecycle;
import za.co.woolworths.financial.services.android.util.MultiClickPreventer;
import za.co.woolworths.financial.services.android.util.MyAccountHelper;
import za.co.woolworths.financial.services.android.util.NetworkChangeListener;
import za.co.woolworths.financial.services.android.util.NetworkManager;
import za.co.woolworths.financial.services.android.util.ScreenManager;
import za.co.woolworths.financial.services.android.util.SessionUtilities;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WFormatter;
import za.co.woolworths.financial.services.android.util.controller.IncreaseLimitController;

import static android.app.Activity.RESULT_OK;

public class WPersonalLoanFragment extends MyAccountCardsActivity.MyAccountCardsFragment implements View.OnClickListener, FragmentLifecycle, NetworkChangeListener, WMaterialShowcaseView.IWalkthroughActionListener {

    public WTextView tvIncreaseLimitDescription, availableBalance, creditLimit, dueDate, minAmountDue, currentBalance, tvViewTransaction, tvIncreaseLimit, tvProtectionInsurance;
    private String productOfferingId;
    private WoolworthsApplication woolworthsApplication;
    private ProgressBar mProgressCreditLimit;
    private WTextView tvApplyNowIncreaseLimit;
    private boolean personalWasAlreadyRunOnce = false;

    private ErrorHandlerView mErrorHandlerView;
    private BroadcastReceiver connectionBroadcast;
    private boolean boolBroadcastRegistered;
    private int minDrawnAmount;
    public RelativeLayout mRelDrawnDownAmount, mRelFindOutMore, mRelIncreaseMyLimit;
    private View view;
    private LinearLayout llCommonLayer, llIncreaseLimitContainer;
    private ImageView logoIncreaseLimit;
    private OfferActive offerActive;
    private IncreaseLimitController mIncreaseLimitController;
    private boolean viewWasCreated = false;
    private RelativeLayout relBalanceProtection, relViewTransactions;
    private WTextView tvBPIProtectInsurance;
    private Call<OfferActive> cliGetOfferActive;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private RelativeLayout rlViewStatement;
    private AccountsResponse accountsResponse;
    private LinearLayout accountInArrearsLayout;
    private WTextView tvHowToPayAccountStatus;
    private WTextView tvAmountOverdue;
    private ImageView iconAvailableFundsInfo, infoCreditLimit, infoCurrentBalance, infoNextPaymentDue, infoAmountOverdue, infoMinimumAmountDue;
    public static int RESULT_CODE_FUNDS_INFO = 60;
    private LinearLayout llActiveAccount;
    private RelativeLayout llChargedOffAccount;
    private boolean productOfferingGoodStanding;
    private Account account;
    private WTextView tvHowToPayArrears;
    private ImageView imViewStatementLogo;

    private RelativeLayout relDebitOrders;

    private View fakeView;
    private boolean mPersonalLoanFragmentIsVisible = false;
private static AsyncTask<Void, Void, Void> async;
    private AsyncTask<Void, Void, Void> mWalThroughStatementEducationFeature;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.card_common_fragment, container, false);
            mIncreaseLimitController = new IncreaseLimitController(getActivity());
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState == null & !viewWasCreated) {
            init(view);
            addListener();
            setAccountDetails();
            viewWasCreated = true;
            disposables.add(woolworthsApplication
                    .bus()
                    .toObservable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Object>() {
                        @Override
                        public void accept(Object object) throws Exception {
                            if (object instanceof BusStation) {
                                BusStation busStation = (BusStation) object;
                                OfferActive offerActive = busStation.getOfferActive();
                                if (offerActive != null) {
                                    hideCLIView();
                                    cliOfferStatus(offerActive);
                                } else if (busStation.makeApiCall()) {
                                    hideCLIView();
                                    personalWasAlreadyRunOnce = false;
                                    retryConnect();
                                } else {
                                }
                            }
                        }
                    }));
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(MyAccountCardsActivity.walkThroughPromtView != null){
            MyAccountCardsActivity.walkThroughPromtView.removeFromWindow();
        }
    }

    private void init(View view) {
        woolworthsApplication = (WoolworthsApplication) getActivity().getApplication();
        availableBalance = (WTextView) view.findViewById(R.id.available_funds);
        creditLimit = (WTextView) view.findViewById(R.id.creditLimit);
        rlViewStatement = (RelativeLayout) view.findViewById(R.id.rlViewStatement);
        dueDate = (WTextView) view.findViewById(R.id.dueDate);
        minAmountDue = (WTextView) view.findViewById(R.id.minAmountDue);
        currentBalance = (WTextView) view.findViewById(R.id.currentBalance);
        tvViewTransaction = (WTextView) view.findViewById(R.id.tvViewTransaction);
        tvProtectionInsurance = (WTextView) view.findViewById(R.id.tvProtectionInsurance);
        tvIncreaseLimitDescription = (WTextView) view.findViewById(R.id.tvIncreaseLimitDescription);
        tvIncreaseLimit = (WTextView) view.findViewById(R.id.tvIncreaseLimit);
        mProgressCreditLimit = (ProgressBar) view.findViewById(R.id.progressCreditLimit);
        mRelDrawnDownAmount = (RelativeLayout) view.findViewById(R.id.relDrawnDownAmount);
        mRelFindOutMore = (RelativeLayout) view.findViewById(R.id.relFindOutMore);
        mRelIncreaseMyLimit = (RelativeLayout) view.findViewById(R.id.relIncreaseMyLimit);
        tvApplyNowIncreaseLimit = (WTextView) view.findViewById(R.id.tvApplyNowIncreaseLimit);
        llCommonLayer = (LinearLayout) view.findViewById(R.id.llCommonLayer);
        logoIncreaseLimit = (ImageView) view.findViewById(R.id.logoIncreaseLimit);
        llIncreaseLimitContainer = (LinearLayout) view.findViewById(R.id.llIncreaseLimitContainer);
        relBalanceProtection = (RelativeLayout) view.findViewById(R.id.relBalanceProtection);
        tvBPIProtectInsurance = view.findViewById(R.id.tvBPIProtectInsurance);

        relViewTransactions = (RelativeLayout) view.findViewById(R.id.rlViewTransactions);
        accountInArrearsLayout = view.findViewById(R.id.llAccountInArrearsParentContainer);
        tvHowToPayAccountStatus = view.findViewById(R.id.howToPayAccountStatus);
        tvAmountOverdue = view.findViewById(R.id.amountOverdue);
        iconAvailableFundsInfo = view.findViewById(R.id.iconAvailableFundsInfo);
        llActiveAccount = view.findViewById(R.id.llActiveAccount);
        llChargedOffAccount = view.findViewById(R.id.llChargedOffAccount);
        tvHowToPayArrears = view.findViewById(R.id.howToPayArrears);

        relDebitOrders = view.findViewById(R.id.relDebitOrders);
        imViewStatementLogo =  view.findViewById(R.id.imViewStatementLogo);
        relDebitOrders.setOnClickListener(this);

        fakeView = view.findViewById(R.id.fakeView);
        infoMinimumAmountDue = view.findViewById(R.id.infoMinimumAmountDue);
        infoAmountOverdue = view.findViewById(R.id.infoAmountOverdue);
        infoNextPaymentDue = view.findViewById(R.id.infoNextPaymentDue);
        infoCurrentBalance = view.findViewById(R.id.infoCurrentBalance);
        infoCreditLimit = view.findViewById(R.id.infoCreditLimit);

        infoMinimumAmountDue.setOnClickListener(this);
        infoAmountOverdue.setOnClickListener(this);
        infoNextPaymentDue.setOnClickListener(this);
        infoCurrentBalance.setOnClickListener(this);
        infoCreditLimit.setOnClickListener(this);
    }

    private void addListener() {
        tvApplyNowIncreaseLimit.setOnClickListener(this);
        tvViewTransaction.setOnClickListener(this);
        relBalanceProtection.setOnClickListener(this);
        mRelDrawnDownAmount.setOnClickListener(this);
        relViewTransactions.setOnClickListener(this);
        mRelFindOutMore.setOnClickListener(this);
        mRelIncreaseMyLimit.setOnClickListener(this);
        llIncreaseLimitContainer.setOnClickListener(this);
        rlViewStatement.setOnClickListener(this);
        iconAvailableFundsInfo.setOnClickListener(this);
        tvHowToPayArrears.setOnClickListener(this);
        tvHowToPayAccountStatus.setOnClickListener(this);
        connectionBroadcast = Utils.connectionBroadCast(getActivity(), this);
    }

    private void setAccountDetails() {
            boolBroadcastRegistered = true;
            getActivity().registerReceiver(connectionBroadcast, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
            accountsResponse = new Gson().fromJson(getArguments().getString("accounts"), AccountsResponse.class);
            onLoadComplete();
            mErrorHandlerView = new ErrorHandlerView(getActivity());
            if (!NetworkManager.getInstance().isConnectedToNetwork(getActivity()))
                mErrorHandlerView.showToast();
            if (accountsResponse != null)
                bindData(accountsResponse);

            mIncreaseLimitController.defaultIncreaseLimitView(logoIncreaseLimit, llCommonLayer, tvIncreaseLimit);
    }

    public void bindData(AccountsResponse response) {
        List<Account> accountList = response.accountList;
        if (accountList != null) {
            for (Account p : accountList) {
                if ("PL".equals(p.productGroupCode)) {
                    this.account = p;
                    if (!p.productOfferingGoodStanding && p.productOfferingStatus.equalsIgnoreCase(Utils.ACCOUNT_CHARGED_OFF)) {
                        llActiveAccount.setVisibility(View.GONE);
                        llChargedOffAccount.setVisibility(View.VISIBLE);
                        Utils.setViewHeightToRemainingBottomSpace(getActivity(), fakeView);
                        return;
                    } else {
                        llActiveAccount.setVisibility(View.VISIBLE);
                        llChargedOffAccount.setVisibility(View.GONE);
                    }
                    tvBPIProtectInsurance.setText(p.insuranceCovered ? getString(R.string.bpi_covered) : getString(R.string.bpi_not_covered));
                    productOfferingGoodStanding = p.productOfferingGoodStanding;
                    productOfferingId = String.valueOf(p.productOfferingId);
                    woolworthsApplication.setProductOfferingId(p.productOfferingId);
                    minDrawnAmount = p.minDrawDownAmount;
                    if (TextUtils.isEmpty(String.valueOf(minDrawnAmount))) {
                        minDrawnAmount = 0;
                    }
                    availableBalance.setText(Utils.removeNegativeSymbol(FontHyperTextParser.getSpannable(WFormatter.newAmountFormat(p.availableFunds), 1, getActivity())));
                    creditLimit.setText(Utils.removeNegativeSymbol(FontHyperTextParser.getSpannable(WFormatter.newAmountFormat(p.creditLimit), 1, getActivity())));

                    minAmountDue.setText(Utils.removeNegativeSymbol(WFormatter.newAmountFormat(p.minimumAmountDue)));
                    currentBalance.setText(Utils.removeNegativeSymbol(WFormatter.newAmountFormat(p.currentBalance)));
                    try {
                        dueDate.setText(WFormatter.addSpaceToDate(WFormatter.newDateFormat(p.paymentDueDate)));
                    } catch (ParseException ex) {
                        dueDate.setText(p.paymentDueDate);
                    }
                    availableBalance.setTextColor(getResources().getColor(p.productOfferingGoodStanding ? R.color.black : R.color.bg_overlay));
                    accountInArrearsLayout.setVisibility(p.productOfferingGoodStanding ? View.GONE : View.VISIBLE);
                    llIncreaseLimitContainer.setVisibility(p.productOfferingGoodStanding ? View.VISIBLE : View.GONE);
                    tvHowToPayAccountStatus.setVisibility(p.productOfferingGoodStanding ? View.VISIBLE : View.INVISIBLE);
                    if (!p.productOfferingGoodStanding) {
                        tvAmountOverdue.setText(WFormatter.newAmountFormat(p.amountOverdue));
                    }

                    relDebitOrders.setVisibility(p.debitOrder.debitOrderActive ? View.VISIBLE : View.GONE);
                    mRelDrawnDownAmount.setVisibility(p.productOfferingGoodStanding ? View.VISIBLE : View.GONE);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        MultiClickPreventer.preventMultiClick(v);
        Activity activity = getActivity();
        if (activity == null) return;
        MyAccountHelper myAccountHelper = new MyAccountHelper();
        String accountInfo = myAccountHelper.getAccountInfo(accountsResponse, "PL");
        if (accountsResponse != null) {
            productOfferingId = Utils.getProductOfferingId(accountsResponse, "PL");
        }
        switch (v.getId()) {
            case R.id.rlViewTransactions:
            case R.id.tvViewTransaction:
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTSPERSONALLOANTRANSACTIONS);
                Intent intent = new Intent(getActivity(), WTransactionsActivity.class);
                intent.putExtra("productOfferingId", productOfferingId);
                startActivity(intent);
                activity.overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
                break;

            case R.id.relBalanceProtection:
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTSPERSONALLOANBPI);
                Intent intBalanceProtection = new Intent(activity, BPIBalanceProtectionActivity.class);
                intBalanceProtection.putExtra("account_info", accountInfo);
                startActivity(intBalanceProtection);
                activity.overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                break;

            case R.id.relDrawnDownAmount:
                Intent intentWithdrawalActivity = new Intent(getActivity(), za.co.woolworths.financial.services.android.ui.activities.loan.LoanWithdrawalActivity.class);
                intentWithdrawalActivity.putExtra("account_info", accountInfo);
                startActivityForResult(intentWithdrawalActivity, 0);
                activity.overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                break;

            case R.id.rlViewStatement:
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTSPERSONALLOANSTATEMENTS);
                Intent openStatement = new Intent(getActivity(), StatementActivity.class);
                startActivity(openStatement);
                activity.overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
                break;
            case R.id.relFindOutMore:
                if (controllerNotNull()) {
                    Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTSPERSONALLOANMORE);
                    mIncreaseLimitController.intentFindOutMore(getActivity(), offerActive);
                }
                break;
            case R.id.relIncreaseMyLimit:
            case R.id.llIncreaseLimitContainer:
                if (controllerNotNull()) {
                    Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTSPERSONALLOANINCREASE);
                    mIncreaseLimitController.nextStep(offerActive, productOfferingId);
                }
                break;
            case R.id.iconAvailableFundsInfo:
                if (this.account.productOfferingGoodStanding) {
                    Utils.displayValidationMessageForResult(
                            this,
                            activity,
                            CustomPopUpWindow.MODAL_LAYOUT.ERROR_TITLE_DESC,
                            activity.getResources().getString(R.string.info_available_funds_title),
                            getActivity().getResources().getString(R.string.info_available_funds_desc),
                            getActivity().getResources().getString(R.string.cli_got_it));
                } else {
                    Utils.displayValidationMessageForResult(
                            this,
                            activity,
                            CustomPopUpWindow.MODAL_LAYOUT.ERROR_TITLE_DESC,
                            getActivity().getResources().getString(R.string.account_in_arrears_info_title),
                            getActivity().getResources().getString(R.string.account_in_arrears_info_description)
                                    .replace("minimum_payment", Utils.removeNegativeSymbol(WFormatter.newAmountFormat(account.totalAmountDue)))
                                    .replace("card_name", "Credit Card"),
                            getActivity().getResources().getString(R.string.how_to_pay),
                            RESULT_CODE_FUNDS_INFO);
                }
                break;
            case R.id.howToPayAccountStatus:
            case R.id.howToPayArrears:
                ScreenManager.presentHowToPayActivity(getActivity(), account);
                break;
            case R.id.relDebitOrders:
                Intent debitOrderIntent = new Intent(getActivity(), DebitOrderActivity.class);
                debitOrderIntent.putExtra("DebitOrder", account.debitOrder);
                startActivity(debitOrderIntent);
                activity.overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                break;
            case R.id.infoMinimumAmountDue:
                Utils.displayValidationMessageForResult(
                        this,
                        activity,
                        CustomPopUpWindow.MODAL_LAYOUT.ERROR_TITLE_DESC,
                        activity.getResources().getString(R.string.info_total_amount_due_title),
                        getActivity().getResources().getString(R.string.info_total_amount_due_desc),
                        getActivity().getResources().getString(R.string.cli_got_it));
                break;
            case R.id.infoAmountOverdue:
                Utils.displayValidationMessageForResult(
                        this,
                        activity,
                        CustomPopUpWindow.MODAL_LAYOUT.ERROR_TITLE_DESC,
                        activity.getResources().getString(R.string.info_amount_overdue_title),
                        getActivity().getResources().getString(R.string.info_amount_overdue_desc),
                        getActivity().getResources().getString(R.string.cli_got_it));
                break;
            case R.id.infoNextPaymentDue:
                Utils.displayValidationMessageForResult(
                        this,
                        activity,
                        CustomPopUpWindow.MODAL_LAYOUT.ERROR_TITLE_DESC,
                        activity.getResources().getString(R.string.info_next_payment_due_title),
                        getActivity().getResources().getString(R.string.info_next_payment_due_desc),
                        getActivity().getResources().getString(R.string.cli_got_it));
                break;
            case R.id.infoCurrentBalance:
                Utils.displayValidationMessageForResult(
                        this,
                        activity,
                        CustomPopUpWindow.MODAL_LAYOUT.ERROR_TITLE_DESC,
                        activity.getResources().getString(R.string.info_current_balance_title),
                        getActivity().getResources().getString(R.string.info_current_balance_desc),
                        getActivity().getResources().getString(R.string.cli_got_it));
                break;
            case R.id.infoCreditLimit:
                Utils.displayValidationMessageForResult(
                        this,
                        activity,
                        CustomPopUpWindow.MODAL_LAYOUT.ERROR_TITLE_DESC,
                        activity.getResources().getString(R.string.info_credit_limit_title),
                        getActivity().getResources().getString(R.string.info_credit_limit_desc),
                        getActivity().getResources().getString(R.string.cli_got_it));
                break;
            default:
                break;
        }
    }

    private void getActiveOffer() {

        if (!productOfferingGoodStanding)
            return;

        onLoad();

        cliGetOfferActive = OneAppService.INSTANCE.getActiveOfferRequest(productOfferingId);
        cliGetOfferActive.enqueue(new CompletionHandler<>(new RequestListener<OfferActive>() {
            @Override
            public void onSuccess(OfferActive response) {
                if (getActivity()==null && !mPersonalLoanFragmentIsVisible) return;
                offerActive = response;
                bindUI(offerActive);
                personalWasAlreadyRunOnce = true;
                onLoadComplete();
            }

            @Override
            public void onFailure(Throwable error) {
                Activity activity = getActivity();
                if (activity!=null && mPersonalLoanFragmentIsVisible) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            personalWasAlreadyRunOnce = false;
                            onLoadComplete();
                        }
                    });
                }
            }
        },OfferActive.class));
    }

    private void bindUI(OfferActive offerActive) {
        int httpCode = offerActive.httpCode;
        switch (httpCode) {
            case 502:
            case 200:
                offerActiveResult(offerActive);
                break;

            case 440:
                SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, offerActive.response.stsParams, getActivity());
                break;

            default:
                break;
        }
        onLoadComplete();
    }


    private void onLoad() {
        llIncreaseLimitContainer.setEnabled(false);
        mRelIncreaseMyLimit.setEnabled(false);
        mProgressCreditLimit.getIndeterminateDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
        mProgressCreditLimit.setVisibility(View.VISIBLE);
        tvApplyNowIncreaseLimit.setVisibility(View.GONE);
    }

    public void onLoadComplete() {
        llIncreaseLimitContainer.setEnabled(true);
        mRelIncreaseMyLimit.setEnabled(true);
        mProgressCreditLimit.getIndeterminateDrawable().setColorFilter(null);
        mProgressCreditLimit.setVisibility(View.GONE);
        tvIncreaseLimit.setVisibility(View.VISIBLE);
    }

    private void offerActiveResult(OfferActive offerActive) {
        try {
            String messageSummary = TextUtils.isEmpty(offerActive.messageSummary) ? "" : offerActive.messageSummary;
            if (controllerNotNull()) {
                if (messageSummary.equalsIgnoreCase(getString(R.string.status_consents))) {
                    mIncreaseLimitController.disableView(mRelIncreaseMyLimit);
                    mIncreaseLimitController.disableView(llIncreaseLimitContainer);
                    mIncreaseLimitController.disableView(tvIncreaseLimit);
                } else {
                    mIncreaseLimitController.enableView(mRelIncreaseMyLimit);
                    mIncreaseLimitController.enableView(llIncreaseLimitContainer);
                    mIncreaseLimitController.enableView(tvIncreaseLimit);
                }
                cliOfferStatus(offerActive);
            }
            showFeatureWalkthroughStatements();
        } catch (IllegalStateException ignored) {
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.setScreenName(getActivity(), FirebaseManagerAnalyticsProperties.ScreenNames.FINANCIAL_SERVICES_PERSONAL_LOAN);
    }

    @Override
    public void onPauseFragment() {
    }

    @Override
    public void onResumeFragment() {
        Activity activity = getActivity();
        if (activity == null) return;
       activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!personalWasAlreadyRunOnce) {
                    if (NetworkManager.getInstance().isConnectedToNetwork(getActivity()))
                        getActiveOffer();
                    else {
                        mErrorHandlerView.showToast();
                        onLoadComplete();
                    }
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (boolBroadcastRegistered) {
            getActivity().unregisterReceiver(connectionBroadcast);
            boolBroadcastRegistered = false;
        }
    }

    @Override
    public void onConnectionChanged() {
        //connection changed
        if (!personalWasAlreadyRunOnce) {
            if (NetworkManager.getInstance().isConnectedToNetwork(getActivity()))
                getActiveOffer();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_CODE_FUNDS_INFO && resultCode == RESULT_OK) {
            ScreenManager.presentHowToPayActivity(getActivity(), account);
        }
        retryConnect();
    }

    private void retryConnect() {
        if (!personalWasAlreadyRunOnce) {
            if (NetworkManager.getInstance().isConnectedToNetwork(getActivity()))
                getActiveOffer();
            else {
                mErrorHandlerView.showToast();
                onLoadComplete();
            }
        }
    }

    private boolean controllerNotNull() {
        return mIncreaseLimitController != null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!disposables.isDisposed())
            disposables.clear();

        if (cliGetOfferActive != null && !cliGetOfferActive.isCanceled()) {
                cliGetOfferActive.cancel();
        }

        // Cancelling walkThrough statement asyncTask
        if (mWalThroughStatementEducationFeature != null && !mWalThroughStatementEducationFeature.isCancelled()) {
            mWalThroughStatementEducationFeature.cancel(true);
        }

    }

    private void cliOfferStatus(OfferActive offerActive) {
        mIncreaseLimitController.accountCLIStatus(llCommonLayer, tvIncreaseLimit, tvApplyNowIncreaseLimit, tvIncreaseLimitDescription, logoIncreaseLimit, offerActive);
    }

    private void hideCLIView() {
        mIncreaseLimitController.cliDefaultView(llCommonLayer, tvIncreaseLimitDescription);
    }

    private void showFeatureWalkthroughStatements() {
        if (!AppInstanceObject.get().featureWalkThrough.showTutorials || AppInstanceObject.get().featureWalkThrough.statements)
            return;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                final Rect rect = new Rect(0, 0, rlViewStatement.getWidth(), rlViewStatement.getHeight());
                rlViewStatement.requestRectangleOnScreen(rect, false);
            }
        }, 500);
        final WMaterialShowcaseView.IWalkthroughActionListener listener = this;

       mWalThroughStatementEducationFeature =  new WalkThroughStatementEducationFeature(getActivity(),imViewStatementLogo,listener).execute();
    }

    private static class WalkThroughStatementEducationFeature extends AsyncTask<Void, Void, Void> {

        private final WMaterialShowcaseView.IWalkthroughActionListener mWalkThroughActionListener;
        private WeakReference<Activity> activityReference;
        private WeakReference<ImageView> viewStatementLogo;

        // only retain a weak reference to the activity
        WalkThroughStatementEducationFeature(Activity context, ImageView imViewStatementLogo, WMaterialShowcaseView.IWalkthroughActionListener walkThroughActionListener) {
            activityReference = new WeakReference<>(context);
            viewStatementLogo = new WeakReference<>(imViewStatementLogo);
            mWalkThroughActionListener = walkThroughActionListener;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Activity activity = activityReference.get();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (viewStatementLogo != null)
                            viewStatementLogo.get().invalidate();
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Activity activity = activityReference.get();
            if (activity == null) return;
            Resources resources = activity.getResources();
            if (resources == null) return;

            Crashlytics.setString(resources.getString(R.string.crashlytics_materialshowcase_key), this.getClass().getCanonicalName());
            WMaterialShowcaseView walkThroughPromptView = new WMaterialShowcaseView.Builder(activity, WMaterialShowcaseView.Feature.STATEMENTS)
                    .setTarget(activity.getWindow().getDecorView().findViewById(R.id.imViewStatementLogo))
                    .setTitle(R.string.walkthrough_statement_title)
                    .setDescription(R.string.walkthrough_statement_desc)
                    .setActionText(R.string.walkthrough_statement_action)
                    .setImage(R.drawable.tips_tricks_statements)
                    .setAction(mWalkThroughActionListener)
                    .setShapePadding(48)
                    .setArrowPosition(WMaterialShowcaseView.Arrow.BOTTOM_LEFT)
                    .setMaskColour(ContextCompat.getColor(activity, R.color.semi_transparent_black)).build();
            walkThroughPromptView.show(activity);
        }
    }

    @Override
    public void onWalkthroughActionButtonClick() {
        onClick(rlViewStatement);
    }

    @Override
    public void onPromptDismiss() {

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        mPersonalLoanFragmentIsVisible = isVisibleToUser;
    }
}

