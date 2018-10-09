package za.co.woolworths.financial.services.android.ui.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import za.co.wigroup.logger.lib.WiGroupLogger;
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.Account;
import za.co.woolworths.financial.services.android.models.dto.AccountsResponse;
import za.co.woolworths.financial.services.android.models.dto.OfferActive;
import za.co.woolworths.financial.services.android.models.rest.cli.CLIGetOfferActive;
import za.co.woolworths.financial.services.android.models.service.event.BusStation;
import za.co.woolworths.financial.services.android.ui.activities.BalanceProtectionActivity;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.activities.DebitOrderActivity;
import za.co.woolworths.financial.services.android.ui.activities.MyAccountCardsActivity;
import za.co.woolworths.financial.services.android.ui.activities.StatementActivity;
import za.co.woolworths.financial.services.android.ui.activities.WTransactionsActivity;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.FontHyperTextParser;
import za.co.woolworths.financial.services.android.util.FragmentLifecycle;
import za.co.woolworths.financial.services.android.util.MultiClickPreventer;
import za.co.woolworths.financial.services.android.util.NetworkChangeListener;
import za.co.woolworths.financial.services.android.util.NetworkManager;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.ScreenManager;
import za.co.woolworths.financial.services.android.util.SessionUtilities;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WFormatter;
import za.co.woolworths.financial.services.android.util.controller.IncreaseLimitController;

import static android.app.Activity.RESULT_OK;

public class WStoreCardFragment extends MyAccountCardsActivity.MyAccountCardsFragment implements View.OnClickListener, FragmentLifecycle, NetworkChangeListener {

	public static int RESULT_CODE_FUNDS_INFO = 50;

	public WTextView availableBalance;
	public WTextView creditLimit;
	public WTextView dueDate;
	public WTextView minAmountDue;
	public WTextView currentBalance;
	public WTextView tvViewTransaction;
	public WTextView tvIncreaseLimit;
	public WTextView tvIncreaseLimitDescription;

	private ImageView iconAvailableFundsInfo;

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
	private CLIGetOfferActive cliGetOfferActive;

	private final CompositeDisposable disposables = new CompositeDisposable();
	private RelativeLayout rlViewStatement;
	private AccountsResponse accountsResponse;
	private LinearLayout accountInArrearsLayout;
	private WTextView tvHowToPayAccountStatus;
	private WTextView tvAmountOverdue;
	private WTextView tvTotalAmountDue;
	private LinearLayout llActiveAccount;
	private RelativeLayout llChargedOffAccount;
	private boolean productOfferingGoodStanding;
	private Account account;
	private WTextView tvHowToPayArrears;

	private RelativeLayout relDebitOrders;

	private View fakeView;

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
									hideCLIView();
									storeWasAlreadyRunOnce = false;
									retryConnect();
								}
							}
						}
					}));
		}
	}

	private void initUI(View view) {
		woolworthsApplication = (WoolworthsApplication) getActivity().getApplication();
		availableBalance = (WTextView) view.findViewById(R.id.available_funds);
		creditLimit = (WTextView) view.findViewById(R.id.creditLimit);
		rlViewStatement = (RelativeLayout) view.findViewById(R.id.rlViewStatement);
		dueDate = (WTextView) view.findViewById(R.id.dueDate);
		minAmountDue = (WTextView) view.findViewById(R.id.minAmountDue);
		currentBalance = (WTextView) view.findViewById(R.id.currentBalance);
		tvViewTransaction = (WTextView) view.findViewById(R.id.tvViewTransaction);
		tvIncreaseLimit = (WTextView) view.findViewById(R.id.tvIncreaseLimit);
		mProgressCreditLimit = (ProgressBar) view.findViewById(R.id.progressCreditLimit);
		tvApplyNowIncreaseLimit = (WTextView) view.findViewById(R.id.tvApplyNowIncreaseLimit);
		tvIncreaseLimitDescription = (WTextView) view.findViewById(R.id.tvIncreaseLimitDescription);
		relBalanceProtection = (RelativeLayout) view.findViewById(R.id.relBalanceProtection);
		rlViewTransactions = (RelativeLayout) view.findViewById(R.id.rlViewTransactions);

		iconAvailableFundsInfo = view.findViewById(R.id.iconAvailableFundsInfo);
		iconAvailableFundsInfo.setOnClickListener(this);

		mRelFindOutMore = (RelativeLayout) view.findViewById(R.id.relFindOutMore);
		mRelIncreaseMyLimit = (RelativeLayout) view.findViewById(R.id.relIncreaseMyLimit);
		tvApplyNowIncreaseLimit = (WTextView) view.findViewById(R.id.tvApplyNowIncreaseLimit);
		llCommonLayer = (LinearLayout) view.findViewById(R.id.llCommonLayer);
		llIncreaseLimitContainer = (LinearLayout) view.findViewById(R.id.llIncreaseLimitContainer);
		logoIncreaseLimit = (ImageView) view.findViewById(R.id.logoIncreaseLimit);
		accountInArrearsLayout = view.findViewById(R.id.llAccountInArrearsParentContainer);
		tvHowToPayAccountStatus = view.findViewById(R.id.howToPayAccountStatus);
		tvHowToPayArrears = view.findViewById(R.id.howToPayArrears);
		tvAmountOverdue = view.findViewById(R.id.amountOverdue);
		tvTotalAmountDue = view.findViewById(R.id.totalAmountDue);
		llActiveAccount = view.findViewById(R.id.llActiveAccount);
		llChargedOffAccount = view.findViewById(R.id.llChargedOffAccount);

		relDebitOrders = view.findViewById(R.id.relDebitOrders);
		relDebitOrders.setOnClickListener(this);

		fakeView = view.findViewById(R.id.fakeView);
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
					if(!p.productOfferingGoodStanding && p.productOfferingStatus.equalsIgnoreCase(Utils.ACCOUNT_CHARGED_OFF))
					{
						llActiveAccount.setVisibility(View.GONE);
						llChargedOffAccount.setVisibility(View.VISIBLE);
						Utils.setViewHeightToRemainingBottomSpace(getActivity(), fakeView);
						return;
					}else {
						llActiveAccount.setVisibility(View.VISIBLE);
						llChargedOffAccount.setVisibility(View.GONE);
					}
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
					iconAvailableFundsInfo.setVisibility(p.productOfferingGoodStanding ? View.GONE : View.VISIBLE);
					availableBalance.setTextColor(getResources().getColor(p.productOfferingGoodStanding ? R.color.black : R.color.bg_overlay));
					accountInArrearsLayout.setVisibility(p.productOfferingGoodStanding ? View.GONE : View.VISIBLE);
					llIncreaseLimitContainer.setVisibility(p.productOfferingGoodStanding ? View.VISIBLE : View.GONE);
					tvHowToPayAccountStatus.setVisibility(p.productOfferingGoodStanding ? View.VISIBLE : View.INVISIBLE);
					if(!p.productOfferingGoodStanding){
						tvAmountOverdue.setText(WFormatter.newAmountFormat(p.amountOverdue));
						tvTotalAmountDue.setText(WFormatter.newAmountFormat(p.totalAmountDue));
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
		accountsResponse = new Gson().fromJson(getArguments().getString("accounts"), AccountsResponse.class);
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
		if (accountsResponse != null) {
			productOfferingId = Utils.getProductOfferingId(accountsResponse, "SC");
		}
		switch (v.getId()) {
			case R.id.rlViewTransactions:
			case R.id.tvViewTransaction:
				Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTSSTORECARDTRANSACTIONS);
				Intent intent = new Intent(getActivity(), WTransactionsActivity.class);
				intent.putExtra("productOfferingId", productOfferingId);
				startActivityForResult(intent, 0);
				getActivity().overridePendingTransition(R.anim.slide_up_anim, R.anim
						.stay);
				break;

			case R.id.relBalanceProtection:
				Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTSSTORECARDBPI);
				Intent intBalanceProtection = new Intent(getActivity(), BalanceProtectionActivity.class);
				startActivity(intBalanceProtection);
				getActivity().overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
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
				Activity activity = getActivity();
				if (activity != null) {
					Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTSSTORECARDSTATEMENTS);
					((WoolworthsApplication) WStoreCardFragment.this.getActivity().getApplication())
							.getUserManager
									().getAccounts();
					Intent openStatement = new Intent(getActivity(), StatementActivity.class);
					//UserStatement statement = new UserStatement(productOfferingId);
					startActivity(openStatement);
					activity.overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
				}
				break;
			case R.id.iconAvailableFundsInfo:
				Utils.displayValidationMessageForResult(
						this,
						getActivity(),
						CustomPopUpWindow.MODAL_LAYOUT.ERROR_TITLE_DESC,
						getActivity().getResources().getString(R.string.account_in_arrears_info_title),
						getActivity().getResources().getString(R.string.account_in_arrears_info_description)
								.replace("minimum_payment", Utils.removeNegativeSymbol(WFormatter.newAmountFormat(account.amountOverdue)))
								.replace("card_name", "Store Card"),
						getActivity().getResources().getString(R.string.how_to_pay),
						RESULT_CODE_FUNDS_INFO);
				break;
			case R.id.howToPayAccountStatus:
			case R.id.howToPayArrears:
				ScreenManager.presentHowToPayActivity(getActivity(),account);
				break;
			case R.id.relDebitOrders:
				if (account.debitOrder.debitOrderActive) {
					Intent debitOrderIntent = new Intent(getActivity(), DebitOrderActivity.class);
					debitOrderIntent.putExtra("DebitOrder", account.debitOrder);
					startActivity(debitOrderIntent);
					getActivity().overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
				}
				break;
			default:
				break;
		}
	}

	private void getActiveOffer() {

		if(!productOfferingGoodStanding)
			return;

		onLoad();
		cliGetOfferActive = new CLIGetOfferActive(getActivity(), productOfferingId, new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				offerActive = ((OfferActive) object);
				bindUI(offerActive);
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
		WStoreCardFragment.this.getActivity().runOnUiThread(new Runnable() {
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
		if(requestCode == RESULT_CODE_FUNDS_INFO && resultCode == RESULT_OK) {
			ScreenManager.presentHowToPayActivity(getActivity(),account);
		}
		retryConnect();
	}

	private void retryConnect() {
		if (!storeWasAlreadyRunOnce) {
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
		if (cliGetOfferActive != null) {
			if (!cliGetOfferActive.isCancelled()) {
				cliGetOfferActive.cancel(true);
			}
		}
	}

	private void cliOfferStatus(OfferActive offerActive) {
		mIncreaseLimitController.accountCLIStatus(llCommonLayer, tvIncreaseLimit, tvApplyNowIncreaseLimit, tvIncreaseLimitDescription, logoIncreaseLimit, offerActive);
	}

	private void hideCLIView() {
		mIncreaseLimitController.cliDefaultView(llCommonLayer, tvIncreaseLimitDescription);
	}
}
