package za.co.woolworths.financial.services.android.ui.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

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


import java.text.ParseException;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import za.co.wigroup.logger.lib.WiGroupLogger;
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.contracts.RequestListener;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.Account;
import za.co.woolworths.financial.services.android.models.dto.AccountsResponse;
import za.co.woolworths.financial.services.android.models.dto.InstantCardReplacement;
import za.co.woolworths.financial.services.android.models.dto.OfferActive;
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsData;
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsRequestBody;
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse;
import za.co.woolworths.financial.services.android.models.network.CompletionHandler;
import za.co.woolworths.financial.services.android.models.network.OneAppService;
import za.co.woolworths.financial.services.android.models.service.event.BusStation;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.activities.DebitOrderActivity;
import za.co.woolworths.financial.services.android.ui.activities.MyAccountCardsActivity;
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity;
import za.co.woolworths.financial.services.android.ui.activities.StatementActivity;
import za.co.woolworths.financial.services.android.ui.activities.WTransactionsActivity;
import za.co.woolworths.financial.services.android.ui.activities.bpi.BPIBalanceProtectionActivity;
import za.co.woolworths.financial.services.android.ui.activities.temporary_store_card.GetTemporaryStoreCardPopupActivity;
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
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity.STORE_CARD_DETAIL;

public class WStoreCardFragment extends MyAccountCardsActivity.MyAccountCardsFragment implements View.OnClickListener, FragmentLifecycle, NetworkChangeListener {

    public static final int RESULT_CODE_FUNDS_INFO = 50;
    public static final int REQUEST_CODE_BLOCK_MY_STORE_CARD = 3021;

    public WTextView availableBalance;
    public WTextView creditLimit;
    public WTextView dueDate;
    public WTextView minAmountDue;
    public WTextView currentBalance;
    public WTextView tvViewTransaction;
    public WTextView tvIncreaseLimit;
    public WTextView tvIncreaseLimitDescription;

    private ImageView iconAvailableFundsInfo, infoCreditLimit, infoCurrentBalance, infoNextPaymentDue, infoAmountOverdue, infoMinimumAmountDue;

    String productOfferingId;
    WoolworthsApplication woolworthsApplication;
    private ProgressBar mProgressCreditLimit;

    private WTextView tvApplyNowIncreaseLimit;
    private boolean storeWasAlreadyRunOnce = false;
    private ErrorHandlerView mErrorHandlerView;
    private BroadcastReceiver connectionBroadcast;
    private NetworkChangeListener networkChangeListener;
    private boolean bolBroacastRegistred;
    private View view;
    private LinearLayout llCommonLayer, llIncreaseLimitContainer;
    private OfferActive offerActive;
    private IncreaseLimitController mIncreaseLimitController;
    private ImageView logoIncreaseLimit;
    private RelativeLayout mRelIncreaseMyLimit;
    private boolean viewWasCreated = false;
    private RelativeLayout rlViewTransactions, relBalanceProtection, mRelFindOutMore;
    private WTextView tvBPIProtectInsurance;
    private Call<OfferActive> cliGetOfferActive;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private RelativeLayout rlViewStatement;
    private AccountsResponse accountsResponse;
    private LinearLayout accountInArrearsLayout;
    private WTextView tvHowToPayAccountStatus;
    private WTextView tvAmountOverdue;
    private LinearLayout llActiveAccount;
    private RelativeLayout llChargedOffAccount;
    private boolean productOfferingGoodStanding;
    private Account account;
    private WTextView tvHowToPayArrears;

    private RelativeLayout relDebitOrders;

    private View fakeView;
    private boolean mStoreCardFragmentIsVisible = false;
    private RelativeLayout rlMyStoreCard;
    private MyAccountHelper myAccountHelper;
    private String mStoreCardAccountDetail;
    private ProgressBar progressBarMyCard;
    private ImageView myCArdRightArrow;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        accountsResponse = new Gson().fromJson(getArguments().getString("accounts"), AccountsResponse.class);
        myAccountHelper = new MyAccountHelper();
        mStoreCardAccountDetail = myAccountHelper.getAccountInfo(accountsResponse, "SC");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.card_common_fragment, container, false);
            view.setContentDescription(getString(R.string.linked_store_card_layout));
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mIncreaseLimitController = new IncreaseLimitController(getActivity());
        if (savedInstanceState == null & !viewWasCreated) {
            initUI(view);
            addListener();
            setAccountDetails();
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
                                    if (!mStoreCardFragmentIsVisible) return;
                                    hideCLIView();
                                    storeWasAlreadyRunOnce = false;
                                    retryConnect();
                                }
                            }
                        }
                    }));
        }

        InstantCardReplacement instantCardReplacement = WoolworthsApplication.getInstantCardReplacement();
        boolean storeCardIsVisible = false;
        if (instantCardReplacement != null)
            storeCardIsVisible = instantCardReplacement.isEnabled();
        rlMyStoreCard.setVisibility(storeCardIsVisible ? VISIBLE : GONE);
    }

    private void initUI(View view) {
        woolworthsApplication = (WoolworthsApplication) getActivity().getApplication();
        availableBalance = view.findViewById(R.id.available_funds);
        creditLimit = view.findViewById(R.id.creditLimit);
        rlViewStatement = view.findViewById(R.id.rlViewStatement);
        dueDate = view.findViewById(R.id.dueDate);
        minAmountDue = view.findViewById(R.id.minAmountDue);
        currentBalance = view.findViewById(R.id.currentBalance);
        tvViewTransaction = view.findViewById(R.id.tvViewTransaction);
        tvIncreaseLimit = view.findViewById(R.id.tvIncreaseLimit);
        mProgressCreditLimit = view.findViewById(R.id.progressCreditLimit);
        tvApplyNowIncreaseLimit = view.findViewById(R.id.tvApplyNowIncreaseLimit);
        tvIncreaseLimitDescription = view.findViewById(R.id.tvIncreaseLimitDescription);
        relBalanceProtection = view.findViewById(R.id.relBalanceProtection);
        tvBPIProtectInsurance = view.findViewById(R.id.tvBPIProtectInsurance);
        rlViewTransactions = view.findViewById(R.id.rlViewTransactions);
        rlMyStoreCard = view.findViewById(R.id.rlMyStoreCard);
        rlMyStoreCard.setOnClickListener(this);

        iconAvailableFundsInfo = view.findViewById(R.id.iconAvailableFundsInfo);
        iconAvailableFundsInfo.setOnClickListener(this);

        mRelFindOutMore = view.findViewById(R.id.relFindOutMore);
        mRelIncreaseMyLimit = view.findViewById(R.id.relIncreaseMyLimit);
        tvApplyNowIncreaseLimit = view.findViewById(R.id.tvApplyNowIncreaseLimit);
        llCommonLayer = view.findViewById(R.id.llCommonLayer);
        llIncreaseLimitContainer = view.findViewById(R.id.llIncreaseLimitContainer);
        logoIncreaseLimit = view.findViewById(R.id.logoIncreaseLimit);
        accountInArrearsLayout = view.findViewById(R.id.llAccountInArrearsParentContainer);
        tvHowToPayAccountStatus = view.findViewById(R.id.howToPayAccountStatus);
        tvHowToPayArrears = view.findViewById(R.id.howToPayArrears);
        tvAmountOverdue = view.findViewById(R.id.amountOverdue);
        llActiveAccount = view.findViewById(R.id.llActiveAccount);
        llChargedOffAccount = view.findViewById(R.id.llChargedOffAccount);

        relDebitOrders = view.findViewById(R.id.relDebitOrders);
        relDebitOrders.setOnClickListener(this);

        fakeView = view.findViewById(R.id.fakeView);
        infoMinimumAmountDue = view.findViewById(R.id.infoMinimumAmountDue);
        infoAmountOverdue = view.findViewById(R.id.infoAmountOverdue);
        infoNextPaymentDue = view.findViewById(R.id.infoNextPaymentDue);
        infoCurrentBalance = view.findViewById(R.id.infoCurrentBalance);
        infoCreditLimit = view.findViewById(R.id.infoCreditLimit);

        progressBarMyCard = view.findViewById(R.id.pbGetCardToken);
        myCArdRightArrow = view.findViewById(R.id.imMyCardNextArrow);

        infoMinimumAmountDue.setOnClickListener(this);
        infoAmountOverdue.setOnClickListener(this);
        infoNextPaymentDue.setOnClickListener(this);
        infoCurrentBalance.setOnClickListener(this);
        infoCreditLimit.setOnClickListener(this);
    }

    private void addListener() {
        relBalanceProtection.setOnClickListener(this);
        tvViewTransaction.setOnClickListener(this);
        rlViewTransactions.setOnClickListener(this);
        llIncreaseLimitContainer.setOnClickListener(this);
        mRelIncreaseMyLimit.setOnClickListener(this);
        mRelFindOutMore.setOnClickListener(this);
        rlViewStatement.setOnClickListener(this);
        tvHowToPayArrears.setOnClickListener(this);
        tvHowToPayAccountStatus.setOnClickListener(this);
    }

    public void bindData(AccountsResponse response) {


        List<Account> accountList = response.accountList;
        if (accountList != null) {
            for (Account p : accountList) {
                if ("SC".equals(p.productGroupCode)) {
                    this.account = p;
                    /**
                     * Check if there is a primaryCard[]
                     * if primarycard[] exists, add a new cell 'My Card" to the store card display
                     * Else, do not add cell and end journey
                     */
                    rlMyStoreCard.setVisibility((account.primaryCard == null) ? View.GONE : View.VISIBLE);
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
                    availableBalance.setText(Utils.removeNegativeSymbol(FontHyperTextParser.getSpannable(WFormatter.newAmountFormat(p.availableFunds), 1, getActivity())));
                    creditLimit.setText(Utils.removeNegativeSymbol(FontHyperTextParser.getSpannable(WFormatter.newAmountFormat(p.creditLimit), 1, getActivity())));
                    minAmountDue.setText(Utils.removeNegativeSymbol(WFormatter.newAmountFormat(p.minimumAmountDue)));
                    currentBalance.setText(Utils.removeNegativeSymbol(WFormatter.newAmountFormat(p.currentBalance)));
                    try {
                        dueDate.setText(WFormatter.addSpaceToDate(WFormatter.newDateFormat(p.paymentDueDate)));
                    } catch (ParseException e) {
                        dueDate.setText(p.paymentDueDate);
                        WiGroupLogger.e(getActivity(), "TAG", e.getMessage(), e);
                    }
                    availableBalance.setTextColor(getResources().getColor(p.productOfferingGoodStanding ? R.color.black : R.color.bg_overlay));
                    accountInArrearsLayout.setVisibility(p.productOfferingGoodStanding ? View.GONE : View.VISIBLE);
                    llIncreaseLimitContainer.setVisibility(p.productOfferingGoodStanding ? View.VISIBLE : View.GONE);
                    tvHowToPayAccountStatus.setVisibility(p.productOfferingGoodStanding ? View.VISIBLE : View.INVISIBLE);
                    if (!p.productOfferingGoodStanding) {
                        tvAmountOverdue.setText(WFormatter.newAmountFormat(p.amountOverdue));
                    }

                    relDebitOrders.setVisibility(p.debitOrder.debitOrderActive ? View.VISIBLE : View.GONE);
                }
            }
        }
    }

    private void setAccountDetails() {
        if (controllerNotNull())
            mIncreaseLimitController.defaultIncreaseLimitView(logoIncreaseLimit, llCommonLayer, tvIncreaseLimit);
        addListener();

        try {
            networkChangeListener = this;
        } catch (ClassCastException ignored) {
        }
        bolBroacastRegistred = true;
        connectionBroadcast = Utils.connectionBroadCast(getActivity(), networkChangeListener);
        getActivity().registerReceiver(connectionBroadcast, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        bindData(accountsResponse);
        onLoadComplete();
        mErrorHandlerView = new ErrorHandlerView(getActivity());
        viewWasCreated = true;
        if (!NetworkManager.getInstance().isConnectedToNetwork(getActivity()))
            mErrorHandlerView.showToast();
    }

    @Override
    public void onClick(View v) {
        MultiClickPreventer.preventMultiClick(v);
        Activity activity = getActivity();
        if (activity == null) return;
        if (accountsResponse != null) {
            productOfferingId = Utils.getProductOfferingId(accountsResponse, "SC");
        }
        switch (v.getId()) {
            case R.id.rlViewTransactions:
            case R.id.tvViewTransaction:
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTSSTORECARDTRANSACTIONS);
                Intent intent = new Intent(getActivity(), WTransactionsActivity.class);
                intent.putExtra("productOfferingId", productOfferingId);
                intent.putExtra("cardType","SC");
                startActivityForResult(intent, 0);
                activity.overridePendingTransition(R.anim.slide_up_anim, R.anim
                        .stay);
                break;
            case R.id.relBalanceProtection:
                Intent intBalanceProtection = new Intent(getActivity(), BPIBalanceProtectionActivity.class);
                intBalanceProtection.putExtra("account_info", mStoreCardAccountDetail);
                startActivity(intBalanceProtection);
                activity.overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                break;

            case R.id.tvIncreaseLimit:
            case R.id.relIncreaseMyLimit:
            case R.id.llIncreaseLimitContainer:
                if (controllerNotNull())
                    mIncreaseLimitController.nextStep(offerActive, productOfferingId);
                break;

            case R.id.relFindOutMore:
                if (controllerNotNull())
                    mIncreaseLimitController.intentFindOutMore(getActivity(), offerActive);
                break;

            case R.id.rlViewStatement:
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTSSTORECARDSTATEMENTS);
                ((WoolworthsApplication) WStoreCardFragment.this.getActivity().getApplication())
                        .getUserManager
                                ().getAccounts();
                Intent openStatement = new Intent(getActivity(), StatementActivity.class);
                startActivity(openStatement);
                activity.overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
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
                                    .replace("card_name", "Store Card"),
                            getActivity().getResources().getString(R.string.how_to_pay),
                            RESULT_CODE_FUNDS_INFO);
                }
                break;
            case R.id.howToPayAccountStatus:
            case R.id.howToPayArrears:
                ScreenManager.presentHowToPayActivity(getActivity(), account);
                break;
            case R.id.relDebitOrders:
                if (account.debitOrder.debitOrderActive) {
                    Intent debitOrderIntent = new Intent(getActivity(), DebitOrderActivity.class);
                    debitOrderIntent.putExtra("DebitOrder", account.debitOrder);
                    startActivity(debitOrderIntent);
                    activity.overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                }
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
            case R.id.rlMyStoreCard:
                getStoreCards(this.account);
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
               offerActive  = response;
               if (getActivity() != null && mStoreCardFragmentIsVisible) {
                   bindUI(offerActive);
               }
           }

           @Override
           public void onFailure(Throwable error) {
               Activity activity = getActivity();
               if (activity != null && mStoreCardFragmentIsVisible) {
                   activity.runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           networkFailureHandler();
                       }
                   });
               }
           }
       },OfferActive.class));
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

        storeWasAlreadyRunOnce = true;
        onLoadComplete();
    }

    private void onLoad() {
        llIncreaseLimitContainer.setEnabled(false);
        mRelIncreaseMyLimit.setEnabled(false);
        mProgressCreditLimit.setVisibility(View.VISIBLE);
        mProgressCreditLimit.getIndeterminateDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
        tvApplyNowIncreaseLimit.setVisibility(View.GONE);
        tvIncreaseLimit.setVisibility(View.VISIBLE);
    }

    public void onLoadComplete() {
        llIncreaseLimitContainer.setEnabled(true);
        mRelIncreaseMyLimit.setEnabled(true);
        mProgressCreditLimit.setVisibility(View.GONE);
        tvIncreaseLimit.setVisibility(View.VISIBLE);
    }

    private void offerActiveResult(OfferActive offerActive) {
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
    }

    @Override
    public void onPauseFragment() {

    }

    @Override
    public void onResumeFragment() {
        Activity activity = getActivity();
        if (activity == null) return;

        Utils.setScreenName(activity, FirebaseManagerAnalyticsProperties.ScreenNames.FINANCIAL_SERVICES_STORE_CARD);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!storeWasAlreadyRunOnce) {
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
                storeWasAlreadyRunOnce = false;
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
        if (!storeWasAlreadyRunOnce) {
            if (NetworkManager.getInstance().isConnectedToNetwork(getActivity()))
                getActiveOffer();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RESULT_CODE_FUNDS_INFO:
                if (resultCode == RESULT_OK) {
                    ScreenManager.presentHowToPayActivity(getActivity(), account);
                }
                break;
            case REQUEST_CODE_BLOCK_MY_STORE_CARD:
                if (data != null)
                    mStoreCardAccountDetail = data.getStringExtra(STORE_CARD_DETAIL);
                break;
            default:
                break;
        }
        retryConnect();
    }

    private void retryConnect() {
        if (!storeWasAlreadyRunOnce) {
            storeWasAlreadyRunOnce = true;
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
        if (!disposables.isDisposed()) {
            disposables.clear();
        }
        if (cliGetOfferActive != null && !cliGetOfferActive.isCanceled()) {
            cliGetOfferActive.cancel();
        }
    }

    private void cliOfferStatus(OfferActive offerActive) {
        mIncreaseLimitController.accountCLIStatus(llCommonLayer, tvIncreaseLimit, tvApplyNowIncreaseLimit, tvIncreaseLimitDescription, logoIncreaseLimit, offerActive);
    }

    private void hideCLIView() {
        mIncreaseLimitController.cliDefaultView(llCommonLayer, tvIncreaseLimitDescription);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        mStoreCardFragmentIsVisible = isVisibleToUser;
    }

    public Call<StoreCardsResponse> getStoreCards(Account account) {
        showGetCreditCardTokenProgressBar(VISIBLE);
        Call<StoreCardsResponse> getStoreCardsRequest = OneAppService.INSTANCE.getStoreCards(new StoreCardsRequestBody(account.accountNumber, account.productOfferingId));
        getStoreCardsRequest.enqueue(new CompletionHandler<>(new RequestListener<StoreCardsResponse>() {
            @Override
            public void onSuccess(StoreCardsResponse storeCardsResponse) {
                showGetCreditCardTokenProgressBar(GONE);
                switch (storeCardsResponse.getHttpCode()) {
                    case 200:
                        handleStoreCardResponse(storeCardsResponse);
                        break;
                    case 440:
                        SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, offerActive.response.stsParams, getActivity());
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onFailure(Throwable error) {
                if (error == null)return;
                final Activity activity = getActivity();
                if (activity!=null && mStoreCardFragmentIsVisible) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showGetCreditCardTokenProgressBar(GONE);
                        }
                    });
                }
            }
        }, StoreCardsResponse.class));
        return getStoreCardsRequest;
    }

    private void handleStoreCardResponse(StoreCardsResponse storeCardsResponse){
        StoreCardsData storeCardsData = storeCardsResponse.getStoreCardsData();
        Activity activity = getActivity();
        if (activity == null || storeCardsData == null) return;

        storeCardsResponse.getStoreCardsData().setProductOfferingId(productOfferingId);
        storeCardsResponse.getStoreCardsData().setVisionAccountNumber(account.accountNumber);
        if(storeCardsData.getGenerateVirtualCard() ) {
            Intent intent = new Intent(activity, GetTemporaryStoreCardPopupActivity.class);
            intent.putExtra(STORE_CARD_DETAIL, Utils.objectToJson(storeCardsResponse));
            activity.startActivity(intent);
            activity.overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
        }else {
            Intent displayStoreCardDetail = new Intent(activity, MyCardDetailActivity.class);
            displayStoreCardDetail.putExtra(STORE_CARD_DETAIL, Utils.objectToJson(storeCardsResponse));
            activity.startActivityForResult(displayStoreCardDetail, REQUEST_CODE_BLOCK_MY_STORE_CARD);
            activity.overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
        }
    }

    private void showGetCreditCardTokenProgressBar(int state) {
        Activity activity = getActivity();
        if (activity != null) {
            progressBarMyCard.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(activity, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);
            progressBarMyCard.setVisibility(state);
            myCArdRightArrow.setVisibility((state == VISIBLE) ? GONE : VISIBLE);
        }
    }
}