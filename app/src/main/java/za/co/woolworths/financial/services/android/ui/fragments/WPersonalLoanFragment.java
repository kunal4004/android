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
import android.text.TextUtils;
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
import java.util.List;

import za.co.woolworths.financial.services.android.FragmentLifecycle;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.Account;
import za.co.woolworths.financial.services.android.models.dto.AccountsResponse;
import za.co.woolworths.financial.services.android.models.dto.OfferActive;
import za.co.woolworths.financial.services.android.models.rest.CLIGetOfferActive;
import za.co.woolworths.financial.services.android.ui.activities.BalanceProtectionActivity;
import za.co.woolworths.financial.services.android.ui.activities.CLIIncreaseLimitInfoActivity;
import za.co.woolworths.financial.services.android.ui.activities.CLIPhase2Activity;
import za.co.woolworths.financial.services.android.ui.activities.LoanWithdrawalActivity;
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
import za.co.woolworths.financial.services.android.util.SharePreferenceHelper;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WFormatter;

public class WPersonalLoanFragment extends MyAccountCardsActivity.MyAccountCardsFragment implements View.OnClickListener, FragmentLifecycle, NetworkChangeListener {

	public WTextView availableBalance, creditLimit, dueDate, minAmountDue, currentBalance, tvViewTransaction, tvIncreaseLimit, tvProtectionInsurance;
	String productOfferingId;
	private WoolworthsApplication woolworthsApplication;
	private ProgressBar mProgressCreditLimit;
	private boolean isOfferActive = true;
	private WTextView tvApplyNowIncreaseLimit;
	private SharePreferenceHelper mSharePreferenceHelper;
	private AsyncTask<String, String, OfferActive> asyncRequestPersonalLoan;
	private boolean personalWasAlreadyRunOnce = false;

	private ErrorHandlerView mErrorHandlerView;
	private BroadcastReceiver connectionBroadcast;
	private NetworkChangeListener networkChangeListener;
	private boolean boolBroadcastRegistered;
	private int minDrawnAmount;
	public RelativeLayout mRelDrawnDownAmount, mRelFindOutMore;
	private View view;
	private LinearLayout llCommonLayer;
	private ImageView logoIncreaseLimit, iconDrawnDownAmount;
	private OfferActive offerActive;

	public enum OFFER_STATUS {
		APPLY_NOW("apply_now"),
		OFFER_AVAILABLE("offer_available"),
		IN_PROGRESS("InProgress"),
		PLEASE_TRY_AGAIN("please_try_again"),
		POI_PROBLEM("poi_problem"),
		POI_REQUIRED("poi_required"),
		UNAVAILABLE("unavailable");

		private final String name;

		private OFFER_STATUS(String s) {
			name = s;
		}

		public boolean equalsName(String otherName) {
			// (otherName == null) check is not needed because name.equals(null) returns false
			return name.equals(otherName);
		}

		public String toString() {
			return this.name;
		}
	}

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
		mSharePreferenceHelper = SharePreferenceHelper.getInstance(getActivity());
		availableBalance = (WTextView) view.findViewById(R.id.available_funds);
		creditLimit = (WTextView) view.findViewById(R.id.creditLimit);
		dueDate = (WTextView) view.findViewById(R.id.dueDate);
		minAmountDue = (WTextView) view.findViewById(R.id.minAmountDue);
		currentBalance = (WTextView) view.findViewById(R.id.currentBalance);
		tvViewTransaction = (WTextView) view.findViewById(R.id.tvViewTransaction);
		tvProtectionInsurance = (WTextView) view.findViewById(R.id.tvProtectionInsurance);
		tvIncreaseLimit = (WTextView) view.findViewById(R.id.tvIncreaseLimit);
		mProgressCreditLimit = (ProgressBar) view.findViewById(R.id.progressCreditLimit);
		mRelDrawnDownAmount = (RelativeLayout) view.findViewById(R.id.relDrawnDownAmount);
		mRelFindOutMore = (RelativeLayout) view.findViewById(R.id.relFindOutMore);
		showView(mRelDrawnDownAmount);
		tvApplyNowIncreaseLimit = (WTextView) view.findViewById(R.id.tvApplyNowIncreaseLimit);
		llCommonLayer = (LinearLayout) view.findViewById(R.id.llCommonLayer);
		logoIncreaseLimit = (ImageView) view.findViewById(R.id.logoIncreaseLimit);
		iconDrawnDownAmount = (ImageView) view.findViewById(R.id.iconDrawnDownAmount);

		RelativeLayout relBalanceProtection = (RelativeLayout) view.findViewById(R.id.relBalanceProtection);
		RelativeLayout relViewTransactions = (RelativeLayout) view.findViewById(R.id.rlViewTransactions);

		tvApplyNowIncreaseLimit.setOnClickListener(this);
		tvViewTransaction.setOnClickListener(this);
		tvIncreaseLimit.setOnClickListener(this);
		relBalanceProtection.setOnClickListener(this);
		mRelDrawnDownAmount.setOnClickListener(this);
		relViewTransactions.setOnClickListener(this);
		mRelFindOutMore.setOnClickListener(this);

		try {
			networkChangeListener = this;
		} catch (ClassCastException ignored) {
		}
		connectionBroadcast = Utils.connectionBroadCast(getActivity(), networkChangeListener);
		boolBroadcastRegistered = true;
		getActivity().registerReceiver(connectionBroadcast, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
		AccountsResponse temp = new Gson().fromJson(getArguments().getString("accounts"), AccountsResponse.class);
		onLoadComplete();
		setTextSize();
		mErrorHandlerView = new ErrorHandlerView(getActivity());
		if (temp != null)
			bindData(temp);

		offerActiveUIState(tvIncreaseLimit, logoIncreaseLimit, OFFER_STATUS.APPLY_NOW);
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
						dueDate.setText(WFormatter.newDateFormat(p.paymentDueDate));
					} catch (ParseException ex) {
						dueDate.setText(p.paymentDueDate);
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
				startActivity(intent);
				getActivity().overridePendingTransition(R.anim.slide_up_anim, R.anim
						.stay);
				break;
			case R.id.tvApplyNowIncreaseLimit:
				if (!isOfferActive) {
					((WoolworthsApplication) getActivity().getApplication()).setProductOfferingId(Integer.valueOf(productOfferingId));
					Intent openCLIIncrease = new Intent(getActivity(), CLIPhase2Activity.class);
					openCLIIncrease.putExtra("jsonOfferActive", Utils.objectToJson(offerActive));
					startActivity(openCLIIncrease);
					getActivity().overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
				}
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

			case R.id.relFindOutMore:
				Intent openFindOutMore = new Intent(getActivity(), CLIIncreaseLimitInfoActivity.class);
				getActivity().startActivity(openFindOutMore);
				getActivity().overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
				break;
		}
	}

	private void getActiveOffer() {
		onLoad();
		CLIGetOfferActive cliGetOfferActive = new CLIGetOfferActive(getActivity(), productOfferingId, new OnEventListener() {
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
		asyncRequestPersonalLoan = cliGetOfferActive.execute();
	}

	private void bindUI(OfferActive offerActive) {
		try {
			int httpCode = offerActive.httpCode;
			String httpDesc = offerActive.response.desc;
			if (httpCode == 200) {
				isOfferActive = offerActive.offerActive;
				personalWasAlreadyRunOnce = true;
				isOfferActive = false;
				if (isOfferActive) {
					disableIncreaseLimit();
				} else {
					enableIncreaseLimit();
				}
			} else if (httpCode == 440) {
				SessionExpiredUtilities.INSTANCE.setAccountSessionExpired(getActivity(), offerActive.response.stsParams);
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
		mProgressCreditLimit.getIndeterminateDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
		mProgressCreditLimit.setVisibility(View.VISIBLE);
		tvApplyNowIncreaseLimit.setVisibility(View.GONE);
		disableIncreaseLimit();
	}

	public void onLoadComplete() {
		mProgressCreditLimit.getIndeterminateDrawable().setColorFilter(null);
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
		mSharePreferenceHelper.removeValue("lw_installment_amount");
		mSharePreferenceHelper.removeValue("lwf_drawDownAmount");
		mSharePreferenceHelper.removeValue("lw_months");
		mSharePreferenceHelper.removeValue("lw_product_offering_id");
		mSharePreferenceHelper.removeValue("lw_amount_drawn_cent");

		setTextSize();
	}

	@Override
	public void onPauseFragment() {
		if (asyncRequestPersonalLoan != null) {
			asyncRequestPersonalLoan.isCancelled();
		}
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
				disableIncreaseLimit();
			}
		}
	}

	private void offerActiveUIState(WTextView tvIncreaseLimit, ImageView logoIncreaseLimit, OFFER_STATUS status) {
		switch (status) {
			case APPLY_NOW:
				tvIncreaseLimit.setText(getString(R.string.cli_credit_limit_increase));
				hideView(logoIncreaseLimit);
				showView(llCommonLayer);
				setStatusText(R.string.apply_now);
				setStatusBackground(R.drawable.cli_round_apply_now_tag);
				break;

			case OFFER_AVAILABLE:
				showView(logoIncreaseLimit);
				hideView(llCommonLayer);
				setStatusText(R.string.offer_available);
				setStatusBackground(R.drawable.cli_round_offer_available);
				break;

			case IN_PROGRESS:
				showView(logoIncreaseLimit);
				hideView(llCommonLayer);
				setStatusText(R.string.status_in_progress);
				setStatusBackground(R.drawable.cli_round_inprogress_tag);
				break;

			case PLEASE_TRY_AGAIN:
				showView(logoIncreaseLimit);
				hideView(llCommonLayer);
				setStatusText(R.string.status_please_try_again);
				setStatusBackground(R.drawable.cli_round_inprogress_tag);
				break;

			case POI_REQUIRED:
				showView(logoIncreaseLimit);
				hideView(llCommonLayer);
				setStatusText(R.string.status_poi_required);
				setStatusBackground(R.drawable.cli_round_inprogress_tag);
				break;

			case POI_PROBLEM:
				showView(logoIncreaseLimit);
				hideView(llCommonLayer);
				setStatusText(R.string.status_poi_problem);
				setStatusBackground(R.drawable.cli_round_offer_poi_problem);
				break;

			case UNAVAILABLE:
				showView(logoIncreaseLimit);
				hideView(llCommonLayer);
				setStatusText(R.string.status_unavailable);
				setStatusBackground(R.drawable.cli_round_offer_unavailable);
				break;
			default:
				break;
		}
	}

	private void hideView(View view) {
		view.setVisibility(View.GONE);
	}

	private void showView(View view) {
		view.setVisibility(View.VISIBLE);
	}

	private void setStatusText(int id) {
		tvApplyNowIncreaseLimit.setText(getString(id));
	}

	private void setStatusBackground(int drawable) {
		tvApplyNowIncreaseLimit.setBackgroundResource(drawable);
	}
}

