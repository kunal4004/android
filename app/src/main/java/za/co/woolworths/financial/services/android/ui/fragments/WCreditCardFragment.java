package za.co.woolworths.financial.services.android.ui.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import za.co.wigroup.logger.lib.WiGroupLogger;
import za.co.woolworths.financial.services.android.contracts.AsyncAPIResponse;
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.Account;
import za.co.woolworths.financial.services.android.models.dto.AccountsResponse;
import za.co.woolworths.financial.services.android.models.dto.Card;
import za.co.woolworths.financial.services.android.models.dto.CreditCardTokenResponse;
import za.co.woolworths.financial.services.android.models.dto.OfferActive;
import za.co.woolworths.financial.services.android.models.rest.cli.CLIGetOfferActive;
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.GetCreditCardToken;
import za.co.woolworths.financial.services.android.models.service.event.BusStation;
import za.co.woolworths.financial.services.android.ui.activities.ABSAOnlineBankingRegistrationActivity;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.activities.MyAccountCardsActivity;
import za.co.woolworths.financial.services.android.ui.activities.WTransactionsActivity;
import za.co.woolworths.financial.services.android.ui.activities.bpi.BPIBalanceProtectionActivity;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.FontHyperTextParser;
import za.co.woolworths.financial.services.android.util.FragmentLifecycle;
import za.co.woolworths.financial.services.android.util.MultiClickPreventer;
import za.co.woolworths.financial.services.android.util.MyAccountHelper;
import za.co.woolworths.financial.services.android.util.NetworkChangeListener;
import za.co.woolworths.financial.services.android.util.NetworkManager;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.ScreenManager;
import za.co.woolworths.financial.services.android.util.SessionUtilities;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WFormatter;
import za.co.woolworths.financial.services.android.util.controller.IncreaseLimitController;

import static android.app.Activity.RESULT_OK;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static za.co.woolworths.financial.services.android.ui.activities.ABSAOnlineBankingRegistrationActivity.SHOULD_DISPLAY_LOGIN_SCREEN;

public class WCreditCardFragment extends MyAccountCardsActivity.MyAccountCardsFragment implements View.OnClickListener, FragmentLifecycle, NetworkChangeListener {

    public WTextView availableBalance, creditLimit, dueDate, minAmountDue, currentBalance, tvViewTransaction, tvIncreaseLimit, tvIncreaseLimitDescription;
    private boolean bolBroacastRegistred;
    private boolean creditWasAlreadyRunOnce = false;
    private String productOfferingId;
    private WoolworthsApplication woolworthsApplication;
    private ProgressBar mProgressCreditLimit;
    private WTextView tvApplyNowIncreaseLimit;
    private ErrorHandlerView mErrorHandlerView;
    private BroadcastReceiver connectionBroadcast;
    private View view;
    private RelativeLayout mRelIncreaseMyLimit;
    private LinearLayout llCommonLayer, llIncreaseLimitContainer;
    private ImageView logoIncreaseLimit;
    private IncreaseLimitController mIncreaseLimitController;
    private OfferActive offerActive;
    private boolean viewWasCreated = false;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private CLIGetOfferActive cliGetOfferActive;
    private AccountsResponse accountsResponse;
    private LinearLayout accountInArrearsLayout;
    private WTextView tvHowToPayAccountStatus;
    private WTextView tvAmountOverdue;
    private ImageView iconAvailableFundsInfo, infoCreditLimit, infoCurrentBalance, infoNextPaymentDue, infoAmountOverdue, infoMinimumAmountDue;
    public static int RESULT_CODE_FUNDS_INFO = 70;
    private LinearLayout llActiveAccount;
    private RelativeLayout llChargedOffAccount;
    private boolean productOfferingGoodStanding;
    private Account account;
    private WTextView tvHowToPayArrears;

    private RelativeLayout relDebitOrders;

    private View fakeView;
    private RelativeLayout relBalanceProtection;
    private WTextView tvBPIProtectInsurance;
    private RelativeLayout rlABSALinkOnlineBankingToDevice;
    private WTextView tvABSALinkOnlineBanking;
    private AsyncTask<String, String, CreditCardTokenResponse> mGetCreditCardToken;
    private ProgressBar mPbGetCreditCardToken;
    private ImageView mImABSAViewOnlineBanking;

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
            woolworthsApplication = (WoolworthsApplication) getActivity().getApplication();
            init(view);
            addListener();
            setAccountDetail();
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
                                    creditWasAlreadyRunOnce = false;
                                    retryConnect();
                                } else {
                                }
                            }
                        }
                    }));
        }
    }

    private void init(View view) {
        availableBalance = (WTextView) view.findViewById(R.id.available_funds);
        RelativeLayout rlViewStatement = (RelativeLayout) view.findViewById(R.id.rlViewStatement);
        rlViewStatement.setVisibility(GONE);
        creditLimit = (WTextView) view.findViewById(R.id.creditLimit);
        dueDate = (WTextView) view.findViewById(R.id.dueDate);
        minAmountDue = (WTextView) view.findViewById(R.id.minAmountDue);
        mPbGetCreditCardToken = (ProgressBar)view.findViewById(R.id.pbGetCreditCardToken);
        mImABSAViewOnlineBanking = (ImageView)view.findViewById(R.id.imABSAViewOnlineBanking);
        rlABSALinkOnlineBankingToDevice = view.findViewById(R.id.rlABSALinkOnlineBankingToDevice);
        currentBalance = (WTextView) view.findViewById(R.id.currentBalance);
        tvViewTransaction = (WTextView) view.findViewById(R.id.tvViewTransaction);
        tvIncreaseLimit = (WTextView) view.findViewById(R.id.tvIncreaseLimit);
        tvIncreaseLimitDescription = (WTextView) view.findViewById(R.id.tvIncreaseLimitDescription);
        mProgressCreditLimit = (ProgressBar) view.findViewById(R.id.progressCreditLimit);
        mProgressCreditLimit.getIndeterminateDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
        RelativeLayout mRelFindOutMore = (RelativeLayout) view.findViewById(R.id.relFindOutMore);
        mRelIncreaseMyLimit = (RelativeLayout) view.findViewById(R.id.relIncreaseMyLimit);
        tvApplyNowIncreaseLimit = (WTextView) view.findViewById(R.id.tvApplyNowIncreaseLimit);
        llCommonLayer = (LinearLayout) view.findViewById(R.id.llCommonLayer);
        llIncreaseLimitContainer = (LinearLayout) view.findViewById(R.id.llIncreaseLimitContainer);
        logoIncreaseLimit = (ImageView) view.findViewById(R.id.logoIncreaseLimit);
        accountInArrearsLayout = view.findViewById(R.id.llAccountInArrearsParentContainer);
        tvHowToPayAccountStatus = view.findViewById(R.id.howToPayAccountStatus);
        tvAmountOverdue = view.findViewById(R.id.amountOverdue);
        iconAvailableFundsInfo = view.findViewById(R.id.iconAvailableFundsInfo);
        llActiveAccount = view.findViewById(R.id.llActiveAccount);
        llChargedOffAccount = view.findViewById(R.id.llChargedOffAccount);
        tvHowToPayArrears = view.findViewById(R.id.howToPayArrears);
        tvABSALinkOnlineBanking = (WTextView) view.findViewById(R.id.tvABSALinkOnlineBanking);

        relDebitOrders = view.findViewById(R.id.relDebitOrders);
        relDebitOrders.setVisibility(GONE);

        // show absa cell
        rlABSALinkOnlineBankingToDevice.setVisibility(VISIBLE);
        relBalanceProtection = (RelativeLayout) view.findViewById(R.id.relBalanceProtection);
        tvBPIProtectInsurance = view.findViewById(R.id.tvBPIProtectInsurance);

        RelativeLayout rlViewTransactions = (RelativeLayout) view.findViewById(R.id.rlViewTransactions);

        if (controllerNotNull()) {
            mIncreaseLimitController.defaultIncreaseLimitView(logoIncreaseLimit, llCommonLayer, tvIncreaseLimit);
        }

        relBalanceProtection.setOnClickListener(this);
        rlABSALinkOnlineBankingToDevice.setOnClickListener(this);
        rlViewTransactions.setOnClickListener(this);
        llIncreaseLimitContainer.setOnClickListener(this);
        mRelIncreaseMyLimit.setOnClickListener(this);
        mRelFindOutMore.setOnClickListener(this);
        iconAvailableFundsInfo.setOnClickListener(this);
        tvHowToPayArrears.setOnClickListener(this);
        tvHowToPayAccountStatus.setOnClickListener(this);

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

        updateABSATitle();
    }

    private void addListener() {
        tvViewTransaction.setOnClickListener(this);
        connectionBroadcast = Utils.connectionBroadCast(getActivity(), this);
        getActivity().registerReceiver(connectionBroadcast, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
    }

    private void setAccountDetail() {
        bolBroacastRegistred = true;
        accountsResponse = new Gson().fromJson(getArguments().getString("accounts"), AccountsResponse.class);
        bindData(accountsResponse);
        onLoadComplete();
        mErrorHandlerView = new ErrorHandlerView(getActivity());
        if (!NetworkManager.getInstance().isConnectedToNetwork(getActivity()))
            mErrorHandlerView.showToast();
    }

    public void bindData(AccountsResponse response) {
        List<Account> accountList = response.accountList;
        if (accountList != null) {
            for (Account p : accountList) {
                if ("CC".equals(p.productGroupCode)) {
                    this.account = p;
                    if (!p.productOfferingGoodStanding && p.productOfferingStatus.equalsIgnoreCase(Utils.ACCOUNT_CHARGED_OFF)) {
                        llActiveAccount.setVisibility(GONE);
                        llChargedOffAccount.setVisibility(VISIBLE);
                        Utils.setViewHeightToRemainingBottomSpace(getActivity(), fakeView);
                        return;
                    } else {
                        llActiveAccount.setVisibility(VISIBLE);
                        llChargedOffAccount.setVisibility(GONE);
                    }

                    productOfferingGoodStanding = p.productOfferingGoodStanding;
                    productOfferingId = String.valueOf(p.productOfferingId);
                    woolworthsApplication.setProductOfferingId(p.productOfferingId);
                    tvBPIProtectInsurance.setText(p.insuranceCovered ? getString(R.string.bpi_covered) : getString(R.string.bpi_not_covered));
                    availableBalance.setText(Utils.removeNegativeSymbol(FontHyperTextParser.getSpannable(WFormatter.newAmountFormat(p.availableFunds), 1, getActivity())));
                    creditLimit.setText(Utils.removeNegativeSymbol(FontHyperTextParser.getSpannable(WFormatter.newAmountFormat(p.creditLimit), 1, getActivity())));
                    minAmountDue.setText(Utils.removeNegativeSymbol(WFormatter.newAmountFormat(p.minimumAmountDue)));
                    currentBalance.setText(Utils.removeNegativeSymbol(WFormatter.newAmountFormat(p.currentBalance)));
                    WoolworthsApplication.setCreditCardType(p.accountNumberBin);
                    try {
                        dueDate.setText(WFormatter.addSpaceToDate(WFormatter.newDateFormat(p.paymentDueDate)));
                    } catch (ParseException e) {
                        dueDate.setText(p.paymentDueDate);
                        WiGroupLogger.e(getActivity(), "TAG", e.getMessage(), e);
                    }
                    availableBalance.setTextColor(getResources().getColor(p.productOfferingGoodStanding ? R.color.black : R.color.bg_overlay));
                    accountInArrearsLayout.setVisibility(p.productOfferingGoodStanding ? GONE : VISIBLE);
                    llIncreaseLimitContainer.setVisibility(p.productOfferingGoodStanding ? VISIBLE : GONE);
                    tvHowToPayAccountStatus.setVisibility(p.productOfferingGoodStanding ? VISIBLE : View.INVISIBLE);
                    if (!p.productOfferingGoodStanding) {
                        tvAmountOverdue.setText(WFormatter.newAmountFormat(p.amountOverdue));
                    }
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        Activity activity = getActivity();
        if (activity == null) return;
        MultiClickPreventer.preventMultiClick(v);
        if (accountsResponse != null) {
            productOfferingId = Utils.getProductOfferingId(accountsResponse, "CC");
        }
        MyAccountHelper myAccountHelper = new MyAccountHelper();
        String accountInfo = myAccountHelper.getAccountInfo(accountsResponse, "CC");

        switch (v.getId()) {
            case R.id.rlABSALinkOnlineBankingToDevice:
                SessionDao aliasID = SessionDao.getByKey(SessionDao.KEY.ABSA_ALIASID);
                SessionDao deviceID = SessionDao.getByKey(SessionDao.KEY.ABSA_DEVICEID);
                if ((TextUtils.isEmpty(aliasID.value) && TextUtils.isEmpty(deviceID.value))) {
                    mGetCreditCardToken = getCreditCardToken(activity);
                } else {
                    openAbsaOnLineBankingActivity(activity);
                }
                break;
            case R.id.rlViewTransactions:
            case R.id.tvViewTransaction:
                Intent intent = new Intent(getActivity(), WTransactionsActivity.class);
                intent.putExtra("productOfferingId", productOfferingId);
                startActivity(intent);
                activity.overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
                break;

            case R.id.relBalanceProtection:
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTSCREDITCARDBPI);
                Intent intBalanceProtection = new Intent(getActivity(), BPIBalanceProtectionActivity.class);
                intBalanceProtection.putExtra("account_info", accountInfo);
                startActivity(intBalanceProtection);
                activity.overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                break;

            case R.id.relIncreaseMyLimit:
            case R.id.llIncreaseLimitContainer:
                if (controllerNotNull()) {
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
                            activity.getResources().getString(R.string.account_in_arrears_info_title),
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
            case R.id.relFindOutMore:
                if (controllerNotNull())
                    mIncreaseLimitController.intentFindOutMore(getActivity(), offerActive);
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
        }
    }

    private void getActiveOffer() {

        if (!productOfferingGoodStanding)
            return;

        onLoad();
        cliGetOfferActive = new CLIGetOfferActive(getActivity(), productOfferingId, new OnEventListener() {
            @Override
            public void onSuccess(Object object) {
                offerActive = ((OfferActive) object);
                bindUI(offerActive);
                creditWasAlreadyRunOnce = true;
                onLoadComplete();
            }

            @Override
            public void onFailure(String e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        networkFailureHandler();
                    }
                });
            }
        });
        cliGetOfferActive.execute();
    }

    private void onLoad() {
        llIncreaseLimitContainer.setEnabled(false);
        mRelIncreaseMyLimit.setEnabled(false);
        mProgressCreditLimit.setVisibility(VISIBLE);
        tvApplyNowIncreaseLimit.setVisibility(GONE);
        tvIncreaseLimit.setVisibility(VISIBLE);
    }

    private void bindUI(OfferActive offerActive) {
        switch (offerActive.httpCode) {
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


    public void onLoadComplete() {
        llIncreaseLimitContainer.setEnabled(true);
        mRelIncreaseMyLimit.setEnabled(true);
        mProgressCreditLimit.setVisibility(GONE);
        tvIncreaseLimit.setVisibility(VISIBLE);
    }

    @Override
    public void onPauseFragment() {
    }

    @Override
    public void onResumeFragment() {
        Activity activity = getActivity();
        if (activity==null) return;

        Utils.setScreenName(activity, FirebaseManagerAnalyticsProperties.ScreenNames.FINANCIAL_SERVICES_CREDIT_CARD);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!creditWasAlreadyRunOnce) {
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

    public void networkFailureHandler() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                creditWasAlreadyRunOnce = false;
                onLoadComplete();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (bolBroacastRegistred) {
            getActivity().unregisterReceiver(connectionBroadcast);
            bolBroacastRegistred = false;
        }
    }

    @Override
    public void onConnectionChanged() {
        //connection changed
        if (!creditWasAlreadyRunOnce) {
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
        if (!creditWasAlreadyRunOnce) {
            if (NetworkManager.getInstance().isConnectedToNetwork(getActivity()))
                getActiveOffer();
            else {
                mErrorHandlerView.showToast();
                onLoadComplete();
            }
        }
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
        } catch (IllegalStateException ignored) {
        }
    }

    private boolean controllerNotNull() {
        return mIncreaseLimitController != null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!disposables.isDisposed())
            disposables.clear(); // do not send event after activity has been destroyed
        if (cliGetOfferActive != null) {
            if (!cliGetOfferActive.isCancelled()) {
                cliGetOfferActive.cancel(true);
            }
        }
        //  Cancel creditCardToken api if running
        if (mGetCreditCardToken != null && !mGetCreditCardToken.isCancelled()) {
            mGetCreditCardToken.cancel(true);
        }
    }

    private void cliOfferStatus(OfferActive offerActive) {
        mIncreaseLimitController.accountCLIStatus(llCommonLayer, tvIncreaseLimit, tvApplyNowIncreaseLimit, tvIncreaseLimitDescription, logoIncreaseLimit, offerActive);
    }

    private void hideCLIView() {
        mIncreaseLimitController.cliDefaultView(llCommonLayer, tvIncreaseLimitDescription);
    }

    public void updateABSATitle() {
        if (tvABSALinkOnlineBanking != null
                && !TextUtils.isEmpty(SessionDao.getByKey(SessionDao.KEY.ABSA_ALIASID).value)
                && !TextUtils.isEmpty(SessionDao.getByKey(SessionDao.KEY.ABSA_DEVICEID).value))
            tvABSALinkOnlineBanking.setText(getString(R.string.online_banking));
    }

    public AsyncTask<String, String, CreditCardTokenResponse> getCreditCardToken(final Activity activity) {
        showGetCreditCardTokenProgressBar(VISIBLE);
        return new GetCreditCardToken(new AsyncAPIResponse.ResponseDelegate<CreditCardTokenResponse>() {
            @Override
            public void onSuccess(CreditCardTokenResponse response) {
                showGetCreditCardTokenProgressBar(GONE);
                switch (response.httpCode) {
                    case 200:
                        ArrayList<Card> cards = response.cards;
                        switch (cards.size()) {
                            case 0:
                                Utils.displayValidationMessage(activity, CustomPopUpWindow.MODAL_LAYOUT.ERROR, getString(R.string.card_number_not_found));
                                break;
                            default:
                                String creditCardNumber = "";
                                for (Card card : cards) {
                                    if (card.cardStatus.equalsIgnoreCase("AAA")) {
                                        creditCardNumber = card.absaCardToken;
                                    }
                                }

                                if (TextUtils.isEmpty(creditCardNumber)) {
                                    Utils.displayValidationMessage(activity, CustomPopUpWindow.MODAL_LAYOUT.ERROR, getString(R.string.card_number_not_found));
                                    return;
                                }
                                openAbsaOnLineBankingActivity(creditCardNumber, activity);
                                break;
                        }
                        break;

                    case 440:
                        SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, offerActive.response.stsParams, getActivity());
                        break;

                    default:
                        break;
                }
            }

            @Override
            public void onFailure(@NotNull final String errorMessage) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showGetCreditCardTokenProgressBar(GONE);
                        if (errorMessage.contains("ConnectException")
                                || errorMessage.contains("SocketTimeoutException")) {
                            Utils.displayValidationMessage(activity, CustomPopUpWindow.MODAL_LAYOUT.ERROR, getString(R.string.check_connection_status));
                        }
                    }
                });
            }
        }).execute();
    }

    private void openAbsaOnLineBankingActivity(String creditCardNumber, Activity activity) {
        Intent openABSAOnlineBanking = new Intent(getActivity(), ABSAOnlineBankingRegistrationActivity.class);
        openABSAOnlineBanking.putExtra(SHOULD_DISPLAY_LOGIN_SCREEN, false);
        openABSAOnlineBanking.putExtra("creditCardToken", creditCardNumber);
        activity.startActivityForResult(openABSAOnlineBanking, MyAccountCardsActivity.ABSA_ONLINE_BANKING_REGISTRATION_REQUEST_CODE);
        activity.overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
    }

    private void openAbsaOnLineBankingActivity(Activity activity) {
        Intent openABSAOnlineBanking = new Intent(getActivity(), ABSAOnlineBankingRegistrationActivity.class);
        openABSAOnlineBanking.putExtra(SHOULD_DISPLAY_LOGIN_SCREEN, true);
        activity.startActivityForResult(openABSAOnlineBanking, MyAccountCardsActivity.ABSA_ONLINE_BANKING_REGISTRATION_REQUEST_CODE);
        activity.overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
    }

    public void showGetCreditCardTokenProgressBar(int state) {
        Activity activity = getActivity();
        if (activity != null) {
            mPbGetCreditCardToken.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(activity, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);
            mPbGetCreditCardToken.setVisibility(state);
            mImABSAViewOnlineBanking.setVisibility((state == VISIBLE) ? GONE : VISIBLE);
        }
    }
}
