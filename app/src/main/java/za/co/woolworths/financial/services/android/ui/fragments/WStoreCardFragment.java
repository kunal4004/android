package za.co.woolworths.financial.services.android.ui.fragments;

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
import za.co.wigroup.logger.lib.WiGroupLogger;
import za.co.woolworths.financial.services.android.FragmentLifecycle;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.Account;
import za.co.woolworths.financial.services.android.models.dto.AccountsResponse;
import za.co.woolworths.financial.services.android.models.dto.OfferActive;
import za.co.woolworths.financial.services.android.models.rest.CLIGetOfferActive;
import za.co.woolworths.financial.services.android.models.service.event.BusStation;
import za.co.woolworths.financial.services.android.ui.activities.BalanceProtectionActivity;
import za.co.woolworths.financial.services.android.ui.activities.MyAccountCardsActivity;
import za.co.woolworths.financial.services.android.ui.activities.WTransactionsActivity;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.FontHyperTextParser;
import za.co.woolworths.financial.services.android.util.MultiClickPreventer;
import za.co.woolworths.financial.services.android.util.NetworkChangeListener;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.SessionExpiredUtilities;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WFormatter;
import za.co.woolworths.financial.services.android.util.controller.IncreaseLimitController;

public class WStoreCardFragment extends MyAccountCardsActivity.MyAccountCardsFragment implements View.OnClickListener, FragmentLifecycle, NetworkChangeListener {

	public WTextView availableBalance;
	public WTextView creditLimit;
	public WTextView dueDate;
	public WTextView minAmountDue;
	public WTextView currentBalance;
	public WTextView tvViewTransaction;
	public WTextView tvIncreaseLimit;
	public WTextView tvIncreaseLimitDescription;

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
								hideCLIView();
								if (offerActive != null) {
									cliOfferStatus(offerActive);
								} else if (busStation.makeApiCall()) {
									storeWasAlreadyRunOnce = false;
									retryConnect();
								} else {
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

		mRelFindOutMore = (RelativeLayout) view.findViewById(R.id.relFindOutMore);
		mRelIncreaseMyLimit = (RelativeLayout) view.findViewById(R.id.relIncreaseMyLimit);
		tvApplyNowIncreaseLimit = (WTextView) view.findViewById(R.id.tvApplyNowIncreaseLimit);
		llCommonLayer = (LinearLayout) view.findViewById(R.id.llCommonLayer);
		llIncreaseLimitContainer = (LinearLayout) view.findViewById(R.id.llIncreaseLimitContainer);
		logoIncreaseLimit = (ImageView) view.findViewById(R.id.logoIncreaseLimit);
	}

	//To remove negative signs from negative balance and add "CR" after the negative balance
	public String removeNegativeSymbol(SpannableString amount) {
		String currentAmount = amount.toString();
		if (currentAmount.contains("-")) {
			currentAmount = currentAmount.replace("-", "") + " CR";
		}
		return currentAmount;
	}

	private void addListener() {
		relBalanceProtection.setOnClickListener(this);
		tvViewTransaction.setOnClickListener(this);
		rlViewTransactions.setOnClickListener(this);
		llIncreaseLimitContainer.setOnClickListener(this);
		mRelIncreaseMyLimit.setOnClickListener(this);
		mRelFindOutMore.setOnClickListener(this);
	}

	public void bindData(AccountsResponse response) {
		List<Account> accountList = response.accountList;
		if (accountList != null) {
			for (Account p : accountList) {
				if ("SC".equals(p.productGroupCode)) {
					productOfferingId = String.valueOf(p.productOfferingId);
					woolworthsApplication.setProductOfferingId(p.productOfferingId);
					availableBalance.setText(removeNegativeSymbol(FontHyperTextParser.getSpannable(WFormatter.newAmountFormat(p.availableFunds), 1, getActivity())));
					creditLimit.setText(removeNegativeSymbol(FontHyperTextParser.getSpannable(WFormatter.newAmountFormat(p.creditLimit), 1, getActivity())));
					minAmountDue.setText(removeNegativeSymbol(WFormatter.newAmountFormat(p.minimumAmountDue)));
					currentBalance.setText(removeNegativeSymbol(WFormatter.newAmountFormat(p.currentBalance)));
					try {
						dueDate.setText(WFormatter.addSpaceToDate(WFormatter.newDateFormat(p.paymentDueDate)));
					} catch (ParseException e) {
						dueDate.setText(p.paymentDueDate);
						WiGroupLogger.e(getActivity(), "TAG", e.getMessage(), e);
					}
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
		AccountsResponse accountsResponse = new Gson().fromJson(getArguments().getString("accounts"), AccountsResponse.class);
		bindData(accountsResponse);
		onLoadComplete();
		mErrorHandlerView = new ErrorHandlerView(getActivity());
		viewWasCreated = true;
		if (!new ConnectionDetector().isOnline(getActivity()))
				mErrorHandlerView.showToast();
	}

	@Override
	public void onClick(View v) {
		MultiClickPreventer.preventMultiClick(v);
		switch (v.getId()) {
			case R.id.rlViewTransactions:
			case R.id.tvViewTransaction:
				Intent intent = new Intent(getActivity(), WTransactionsActivity.class);
				intent.putExtra("productOfferingId", productOfferingId);
				startActivityForResult(intent, 0);
				getActivity().overridePendingTransition(R.anim.slide_up_anim, R.anim
						.stay);
				break;

			case R.id.relBalanceProtection:
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
			default:
				break;
		}
	}

	private void getActiveOffer() {
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
				SessionExpiredUtilities.INSTANCE.setAccountSessionExpired(getActivity(), offerActive
						.response.stsParams);
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

	//To remove negative signs from negative balance and add "CR" after the negative balance
	public String removeNegativeSymbol(String amount) {
		String currentAmount = amount;
		if (currentAmount.contains("-")) {
			currentAmount = currentAmount.replace("-", "") + " CR";
		}
		return currentAmount;
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
		if (!storeWasAlreadyRunOnce) {
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
