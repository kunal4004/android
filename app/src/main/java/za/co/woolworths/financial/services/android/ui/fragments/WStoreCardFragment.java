package za.co.woolworths.financial.services.android.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.util.TypedValue;
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
import java.util.HashMap;
import java.util.List;

import za.co.wigroup.logger.lib.WiGroupLogger;
import za.co.woolworths.financial.services.android.FragmentLifecycle;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.Account;
import za.co.woolworths.financial.services.android.models.dto.AccountsResponse;
import za.co.woolworths.financial.services.android.models.dto.Cli;
import za.co.woolworths.financial.services.android.models.dto.OfferActive;
import za.co.woolworths.financial.services.android.models.rest.CLIGetOfferActive;
import za.co.woolworths.financial.services.android.ui.activities.BalanceProtectionActivity;
import za.co.woolworths.financial.services.android.ui.activities.CLIPhase2Activity;
import za.co.woolworths.financial.services.android.ui.activities.MyAccountCardsActivity;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpDialogManager;
import za.co.woolworths.financial.services.android.ui.activities.WTransactionsActivity;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.FontHyperTextParser;
import za.co.woolworths.financial.services.android.util.NetworkChangeListener;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.SessionExpiredUtilities;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WFormatter;
import za.co.woolworths.financial.services.android.util.controller.IncreaseLimitController;
import za.co.woolworths.financial.services.android.util.controller.OfferStatus;


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

	private boolean isOfferActive = true;
	private WTextView tvApplyNowIncreaseLimit;
	private AsyncTask<String, String, OfferActive> asyncTaskStore;
	private boolean storeWasAlreadyRunOnce = false;
	private ErrorHandlerView mErrorHandlerView;
	private BroadcastReceiver connectionBroadcast;
	private NetworkChangeListener networkChangeListener;
	private boolean bolBroacastRegistred;
	private View view;
	private RelativeLayout mRelFindOutMore, mRelIncreaseMyLimit;
	private LinearLayout llCommonLayer;
	private ImageView logoIncreaseLimit, iconDrawnDownAmount;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		if (view == null) {
			view = inflater.inflate(R.layout.card_common_fragment, container, false);
		}
		return view;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
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
		RelativeLayout relBalanceProtection = (RelativeLayout) view.findViewById(R.id.relBalanceProtection);
		RelativeLayout rlViewTransactions = (RelativeLayout) view.findViewById(R.id.rlViewTransactions);

		mRelFindOutMore = (RelativeLayout) view.findViewById(R.id.relFindOutMore);
		mRelIncreaseMyLimit = (RelativeLayout) view.findViewById(R.id.relIncreaseMyLimit);
		tvApplyNowIncreaseLimit = (WTextView) view.findViewById(R.id.tvApplyNowIncreaseLimit);
		llCommonLayer = (LinearLayout) view.findViewById(R.id.llCommonLayer);
		logoIncreaseLimit = (ImageView) view.findViewById(R.id.logoIncreaseLimit);
		iconDrawnDownAmount = (ImageView) view.findViewById(R.id.iconDrawnDownAmount);

		IncreaseLimitController increaseLimitController = new IncreaseLimitController(getActivity());
		increaseLimitController.offerActiveUIState(llCommonLayer, tvIncreaseLimit, tvApplyNowIncreaseLimit, logoIncreaseLimit, OfferStatus.PLEASE_TRY_AGAIN);
		relBalanceProtection.setOnClickListener(this);
		tvIncreaseLimit.setOnClickListener(this);
		tvViewTransaction.setOnClickListener(this);
		rlViewTransactions.setOnClickListener(this);

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
	}

	//To remove negative signs from negative balance and add "CR" after the negative balance
	public String removeNegativeSymbol(SpannableString amount) {
		String currentAmount = amount.toString();
		if (currentAmount.contains("-")) {
			currentAmount = currentAmount.replace("-", "") + " CR";
		}
		return currentAmount;
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
						dueDate.setText(WFormatter.newDateFormat(p.paymentDueDate));
					} catch (ParseException e) {
						dueDate.setText(p.paymentDueDate);
						WiGroupLogger.e(getActivity(), "TAG", e.getMessage(), e);
					}
				}
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.rlViewTransactions:
			case R.id.tvViewTransaction:
				Intent intent = new Intent(getActivity(), WTransactionsActivity.class);
				intent.putExtra("productOfferingId", productOfferingId);
				startActivityForResult(intent, 0);
				getActivity().overridePendingTransition(R.anim.slide_up_anim, R.anim
						.stay);
				break;

			case R.id.tvApplyNowIncreaseLimit:
				if (!isOfferActive) {
					((WoolworthsApplication) getActivity().getApplication()).setProductOfferingId(Integer.valueOf(productOfferingId));
					Intent openCLIIncrease = new Intent(getActivity(), CLIPhase2Activity.class);
					startActivity(openCLIIncrease);
					getActivity().overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
				}
				break;

			case R.id.relBalanceProtection:
				Intent intBalanceProtection = new Intent(getActivity(), BalanceProtectionActivity.class);
				startActivity(intBalanceProtection);
				getActivity().overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
				break;

		}
	}


	private void getActiveOffer() {
		onLoad();
		CLIGetOfferActive cliGetOfferActive = new CLIGetOfferActive(getActivity(), productOfferingId, new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				OfferActive offerActive = ((OfferActive) object);
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
		asyncTaskStore = cliGetOfferActive.execute();
	}

	private void bindUI(OfferActive offerActive) {
		try {
			int httpCode = offerActive.httpCode;
			String httpDesc = offerActive.response.desc;
			if (httpCode == 200) {
				isOfferActive = offerActive.offerActive;
				storeWasAlreadyRunOnce = true;
				Cli cli = offerActive.cli;
				String nextStep = cli.nextStep;
				String messageSummary = cli.messageSummary;
				String messageDetail = cli.messageDetail;

				HashMap<String, String> hMIncreaseCreditLimit = new HashMap<>();
				hMIncreaseCreditLimit.put("NEXT_STEP", nextStep);
				hMIncreaseCreditLimit.put("MESSAGE_SUMMARY", messageSummary);
				hMIncreaseCreditLimit.put("MESSAGE_DETAIL", messageDetail);

				Utils.setOvalTagDrawable(getActivity(), tvApplyNowIncreaseLimit, tvIncreaseLimitDescription, hMIncreaseCreditLimit);

			} else if (httpCode == 440) {
				SessionExpiredUtilities.INSTANCE.setAccountSessionExpired(getActivity(), offerActive.response
						.stsParams);
			} else {
				disableIncreaseLimit();
				Utils.displayValidationMessage(getActivity(),
						CustomPopUpDialogManager.VALIDATION_MESSAGE_LIST.ERROR,
						httpDesc);
			}
		} catch (NullPointerException ignored) {
		}
		onLoadComplete();
	}

	private void onLoad() {
		mProgressCreditLimit.setVisibility(View.VISIBLE);
		mProgressCreditLimit.getIndeterminateDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
		tvApplyNowIncreaseLimit.setVisibility(View.GONE);
		tvIncreaseLimit.setVisibility(View.VISIBLE);
		disableIncreaseLimit();
	}

	public void onLoadComplete() {
		mProgressCreditLimit.setVisibility(View.GONE);
		tvApplyNowIncreaseLimit.setVisibility(View.VISIBLE);
		tvIncreaseLimit.setVisibility(View.VISIBLE);
	}

	public void enableIncreaseLimit() {
		tvIncreaseLimit.setEnabled(true);
		tvApplyNowIncreaseLimit.setEnabled(true);
		tvIncreaseLimit.setTextColor(Color.BLACK);
		tvApplyNowIncreaseLimit.setAlpha(1);
	}

	public void disableIncreaseLimit() {
		tvIncreaseLimit.setEnabled(false);
		tvApplyNowIncreaseLimit.setEnabled(false);
		tvIncreaseLimit.setTextColor(Color.GRAY);
		tvApplyNowIncreaseLimit.setAlpha(0.5f);
	}

	private void setTextSize() {
		dueDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
		minAmountDue.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
		currentBalance.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);

		Typeface mMyriaProFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/MyriadPro-Regular.otf");
		dueDate.setTypeface(mMyriaProFont);
		minAmountDue.setTypeface(mMyriaProFont);
		currentBalance.setTypeface(mMyriaProFont);
	}

	@Override
	public void onResume() {
		super.onResume();
		setTextSize();
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
		if (asyncTaskStore != null) {
			asyncTaskStore.isCancelled();
		}
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
						disableIncreaseLimit();
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
				disableIncreaseLimit();
			}
		}
	}

}
