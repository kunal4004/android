package za.co.woolworths.financial.services.android.ui.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.SpannableString;
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
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.Account;
import za.co.woolworths.financial.services.android.models.dto.AccountsResponse;
import za.co.woolworths.financial.services.android.models.dto.OfferActive;
import za.co.woolworths.financial.services.android.models.rest.cli.CLIGetOfferActive;
import za.co.woolworths.financial.services.android.models.service.event.BusStation;
import za.co.woolworths.financial.services.android.ui.activities.BalanceProtectionActivity;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.activities.LoanWithdrawalActivity;
import za.co.woolworths.financial.services.android.ui.activities.MyAccountCardsActivity;
import za.co.woolworths.financial.services.android.ui.activities.StatementActivity;
import za.co.woolworths.financial.services.android.ui.activities.WTransactionsActivity;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.FontHyperTextParser;
import za.co.woolworths.financial.services.android.util.FragmentLifecycle;
import za.co.woolworths.financial.services.android.util.MultiClickPreventer;
import za.co.woolworths.financial.services.android.util.NetworkChangeListener;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.SessionUtilities;
import za.co.woolworths.financial.services.android.util.SharePreferenceHelper;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WFormatter;
import za.co.woolworths.financial.services.android.util.controller.IncreaseLimitController;

public class WPersonalLoanFragment extends MyAccountCardsActivity.MyAccountCardsFragment implements View.OnClickListener, FragmentLifecycle, NetworkChangeListener {

	public WTextView tvIncreaseLimitDescription, availableBalance, creditLimit, dueDate, minAmountDue, currentBalance, tvViewTransaction, tvIncreaseLimit, tvProtectionInsurance;
	private String productOfferingId;
	private WoolworthsApplication woolworthsApplication;
	private ProgressBar mProgressCreditLimit;
	private WTextView tvApplyNowIncreaseLimit;
	private SharePreferenceHelper mSharePreferenceHelper;
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
	private CLIGetOfferActive cliGetOfferActive;
	private final CompositeDisposable disposables = new CompositeDisposable();
	private RelativeLayout rlViewStatement;
	private AccountsResponse accountsResponse;
	private LinearLayout accountInArrearsLayout;
	private WTextView tvHowToPayAccountStatus;
	private WTextView tvAmountOverdue;
	private WTextView tvTotalAmountDue;
	private ImageView iconAvailableFundsInfo;
	public static int RESULT_CODE_FUNDS_INFO = 60;
    private LinearLayout llActiveAccount;
    private RelativeLayout llChargedOffAccount;
	private boolean productOfferingGoodStanding;

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

	private void init(View view) {
		woolworthsApplication = (WoolworthsApplication) getActivity().getApplication();
		mSharePreferenceHelper = SharePreferenceHelper.getInstance(getActivity());
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
		relViewTransactions = (RelativeLayout) view.findViewById(R.id.rlViewTransactions);
		accountInArrearsLayout = view.findViewById(R.id.llAccountInArrearsParentContainer);
		tvHowToPayAccountStatus = view.findViewById(R.id.howToPayAccountStatus);
		tvAmountOverdue = view.findViewById(R.id.amountOverdue);
		tvTotalAmountDue = view.findViewById(R.id.totalAmountDue);
		iconAvailableFundsInfo = view.findViewById(R.id.iconAvailableFundsInfo);
        llActiveAccount = view.findViewById(R.id.llActiveAccount);
        llChargedOffAccount = view.findViewById(R.id.llChargedOffAccount);
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
		connectionBroadcast = Utils.connectionBroadCast(getActivity(), this);
	}

	private void setAccountDetails() {
		boolBroadcastRegistered = true;
		getActivity().registerReceiver(connectionBroadcast, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
		accountsResponse = new Gson().fromJson(getArguments().getString("accounts"), AccountsResponse.class);
		onLoadComplete();
		mErrorHandlerView = new ErrorHandlerView(getActivity());
		if (!new ConnectionDetector().isOnline(getActivity()))
			mErrorHandlerView.showToast();
		if (accountsResponse != null)
			bindData(accountsResponse);

		{
			if (controllerNotNull())
				mIncreaseLimitController.showView(mRelDrawnDownAmount);
			mIncreaseLimitController.defaultIncreaseLimitView(logoIncreaseLimit, llCommonLayer, tvIncreaseLimit);
		}
	}

	//To remove negative signs from negative balance and add "CR" after the negative balance
	public String removeNegativeSymbol(SpannableString amount) {
		String currentAmount = amount.toString();
		if (currentAmount.contains("-")) {
			currentAmount = currentAmount.replace("-", "") + " CR";
		}
		return currentAmount;
	}

	//To remove negative signs from negative balance and add "CR" after the negative balance
	public String removeNegativeSymbol(String amount) {
		String currentAmount = amount;
		if (currentAmount.contains("-")) {
			currentAmount = currentAmount.replace("-", "") + " CR";
		}
		return currentAmount;
	}

	public void bindData(AccountsResponse response) {
		List<Account> accountList = response.accountList;
		if (accountList != null) {
			for (Account p : accountList) {
				if ("PL".equals(p.productGroupCode)) {

                    if(!p.productOfferingGoodStanding && p.productOfferingStatus.equalsIgnoreCase(Utils.ACCOUNT_CHARGED_OFF))
                    {
                        llActiveAccount.setVisibility(View.GONE);
                        llChargedOffAccount.setVisibility(View.VISIBLE);
                        return;
                    }else {
                        llActiveAccount.setVisibility(View.VISIBLE);
                        llChargedOffAccount.setVisibility(View.GONE);
                    }
					productOfferingGoodStanding = p.productOfferingGoodStanding;
					productOfferingId = String.valueOf(p.productOfferingId);
					woolworthsApplication.setProductOfferingId(p.productOfferingId);
					mSharePreferenceHelper.save(String.valueOf(p.productOfferingId), "lw_product_offering_id");
					minDrawnAmount = p.minDrawDownAmount;
					if (TextUtils.isEmpty(String.valueOf(minDrawnAmount))) {
						minDrawnAmount = 0;
					}
					availableBalance.setText(removeNegativeSymbol(FontHyperTextParser.getSpannable(WFormatter.newAmountFormat(p.availableFunds), 1, getActivity())));
					mSharePreferenceHelper.save(availableBalance.getText().toString(), "lw_available_fund");
					creditLimit.setText(removeNegativeSymbol(FontHyperTextParser.getSpannable(WFormatter.newAmountFormat(p.creditLimit), 1, getActivity())));
					mSharePreferenceHelper.save(creditLimit.getText().toString(), "lw_credit_limit");

					minAmountDue.setText(removeNegativeSymbol(WFormatter.newAmountFormat(p.minimumAmountDue)));
					currentBalance.setText(removeNegativeSymbol(WFormatter.newAmountFormat(p.currentBalance)));
					try {
						dueDate.setText(WFormatter.addSpaceToDate(WFormatter.newDateFormat(p.paymentDueDate)));
					} catch (ParseException ex) {
						dueDate.setText(p.paymentDueDate);
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
				}
			}
		}
	}

	@Override
	public void onClick(View v) {
		MultiClickPreventer.preventMultiClick(v);
		if (accountsResponse != null) {
			productOfferingId = Utils.getProductOfferingId(accountsResponse, "PL");
		}
		switch (v.getId()) {
			case R.id.rlViewTransactions:
			case R.id.tvViewTransaction:
				Intent intent = new Intent(getActivity(), WTransactionsActivity.class);
				intent.putExtra("productOfferingId", productOfferingId);
				startActivity(intent);
				getActivity().overridePendingTransition(R.anim.slide_up_anim, R.anim
						.stay);
				break;

			case R.id.relBalanceProtection:
				Intent intBalanceProtection = new Intent(getActivity(), BalanceProtectionActivity.class);
				startActivity(intBalanceProtection);
				getActivity().overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
				break;

			case R.id.relDrawnDownAmount:
				mSharePreferenceHelper.save("", "lw_amount_drawn_cent");
				mSharePreferenceHelper.save(String.valueOf(productOfferingId), "lw_product_offering_id");
				Intent openWithdrawCashNow = new Intent(getActivity(), LoanWithdrawalActivity.class);
				openWithdrawCashNow.putExtra("minDrawnDownAmount", minDrawnAmount);
				startActivity(openWithdrawCashNow);
				getActivity().overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
				break;

			case R.id.rlViewStatement:
				Activity activity = getActivity();
				if (activity != null) {
					Intent openStatement = new Intent(getActivity(), StatementActivity.class);
					startActivity(openStatement);
					activity.overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
				}
				break;
			case R.id.relFindOutMore:
				if (controllerNotNull())
					mIncreaseLimitController.intentFindOutMore(getActivity(), offerActive);
				break;
			case R.id.relIncreaseMyLimit:
			case R.id.llIncreaseLimitContainer:
				if (controllerNotNull())
					mIncreaseLimitController.nextStep(offerActive, productOfferingId);
				break;
			case R.id.iconAvailableFundsInfo:
				Utils.displayValidationMessageForResult(
						getActivity(),
						CustomPopUpWindow.MODAL_LAYOUT.ERROR_TITLE_DESC,
						"YOUR ACCOUNT IS IN ARREARS",
						"Your Woolies Store Card is in arrears, please make an immediate minimum payment of Rxxx.xx in order to continue shopping.\n\nFor assistance, call us on 0861 50 20 20",
						RESULT_CODE_FUNDS_INFO);
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
				personalWasAlreadyRunOnce = true;
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

	private void bindUI(OfferActive offerActive) {
		int httpCode = offerActive.httpCode;
		switch (httpCode) {
			case 502:
			case 200:
				offerActiveResult(offerActive);
				break;

			case 440:
				SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, offerActive.response.stsParams,getActivity());
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
		} catch (IllegalStateException ignored) {
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		try {
			mSharePreferenceHelper.removeValue("lw_installment_amount");
			mSharePreferenceHelper.removeValue("lwf_drawDownAmount");
			mSharePreferenceHelper.removeValue("lw_months");
			mSharePreferenceHelper.removeValue("lw_product_offering_id");
			mSharePreferenceHelper.removeValue("lw_amount_drawn_cent");
		} catch (Exception ex) {
		}
	}

	@Override
	public void onPauseFragment() {
	}

	@Override
	public void onResumeFragment() {
		WPersonalLoanFragment.this.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (!personalWasAlreadyRunOnce) {
					if (new ConnectionDetector().isOnline(getActivity()))
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
				personalWasAlreadyRunOnce = false;
				onLoadComplete();
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
			if (new ConnectionDetector().isOnline(getActivity()))
				getActiveOffer();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		retryConnect();
	}

	private void retryConnect() {
		if (!personalWasAlreadyRunOnce) {
			if (new ConnectionDetector().isOnline(getActivity()))
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

