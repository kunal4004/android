package za.co.woolworths.financial.services.android.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import za.co.woolworths.financial.services.android.ui.activities.BalanceProtectionActivity;
import za.co.woolworths.financial.services.android.ui.activities.CLIActivity;
import za.co.woolworths.financial.services.android.ui.activities.LoanWithdrawalActivity;
import za.co.woolworths.financial.services.android.ui.activities.MyAccountCardsActivity;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpDialogManager;
import za.co.woolworths.financial.services.android.ui.activities.WTransactionsActivity;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.FontHyperTextParser;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.NetworkChangeListener;
import za.co.woolworths.financial.services.android.util.PersonalLoanAmount;
import za.co.woolworths.financial.services.android.util.SessionExpiredUtilities;
import za.co.woolworths.financial.services.android.util.SharePreferenceHelper;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WFormatter;

public class WPersonalLoanFragment extends MyAccountCardsActivity.MyAccountCardsFragment implements View.OnClickListener, FragmentLifecycle, NetworkChangeListener {

	private PersonalLoanAmount personalLoanInfo;

	public WTextView availableBalance, creditLimit, dueDate, minAmountDue, currentBalance, tvViewTransaction, tvIncreaseLimit, tvProtectionInsurance;
	String productOfferingId;
	private WoolworthsApplication woolworthsApplication;
	private ProgressBar mProgressCreditLimit;
	private boolean isOfferActive = true;
	private ImageView iconIncreaseLimit;
	private SharePreferenceHelper mSharePreferenceHelper;
	private HttpAsyncTask<String, String, OfferActive> asyncRequestPersonalLoan;
	private boolean cardHasId = false;

	private AccountsResponse temp = null;
	private ErrorHandlerView mErrorHandlerView;
	private BroadcastReceiver connectionBroadcast;
	private NetworkChangeListener networkChangeListener;
	private boolean bolBroacastRegistred;
	private ImageView iconBalanceProdtectionInsurance;
	private int minDrawnAmount;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.card_common_fragment, container, false);
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
		iconBalanceProdtectionInsurance = (ImageView) view.findViewById(R.id.iconBalanceProdtectionInsurance);
		iconBalanceProdtectionInsurance.setImageResource(R.drawable.ic_caret_black);
		iconIncreaseLimit = (ImageView) view.findViewById(R.id.iconIncreaseLimit);
		ImageView logoProtectionInsurance = (ImageView) view.findViewById(R.id.logoProtectionInsurance);
		logoProtectionInsurance.setImageResource(R.drawable.money);
		RelativeLayout rlIncreaseLimit = (RelativeLayout) view.findViewById(R.id.rlIncreaseLimit);
		RelativeLayout relBalanceProtection = (RelativeLayout) view.findViewById(R.id.relBalanceProtection);
		RelativeLayout rlViewTransactions = (RelativeLayout) view.findViewById(R.id.rlViewTransactions);

		rlIncreaseLimit.setOnClickListener(this);
		tvViewTransaction.setOnClickListener(this);
		tvIncreaseLimit.setOnClickListener(this);
		relBalanceProtection.setOnClickListener(this);
		rlViewTransactions.setOnClickListener(this);

		tvProtectionInsurance.setText(getString(R.string.instore_withdraw_cash_now));
		try {
			networkChangeListener = this;
		} catch (ClassCastException ignored) {
		}
		connectionBroadcast = Utils.connectionBroadCast(getActivity(), networkChangeListener);
		bolBroacastRegistred = true;
		getActivity().registerReceiver(connectionBroadcast, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
		temp = new Gson().fromJson(getArguments().getString("accounts"), AccountsResponse.class);
		disableIncreaseLimit();
		hideProgressBar();
		setTextSize();
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
						dueDate.setText(WFormatter.formatDate(p.paymentDueDate));
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
			case R.id.rlIncreaseLimit:
			case R.id.tvIncreaseLimit:
				if (!isOfferActive) {
					((WoolworthsApplication) getActivity().getApplication()).setProductOfferingId(Integer.valueOf(productOfferingId));
					Intent openCLIIncrease = new Intent(getActivity(), CLIActivity.class);
					startActivity(openCLIIncrease);
					getActivity().overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
				}
				break;

			case R.id.relBalanceProtection:
				mSharePreferenceHelper.save("", "lw_amount_drawn_cent");
				Intent openWithdrawCashNow = new Intent(getActivity(), LoanWithdrawalActivity.class);
				openWithdrawCashNow.putExtra("minDrawnDownAmount", minDrawnAmount);
				startActivity(openWithdrawCashNow);
				getActivity().overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
				break;
		}
	}

	private void getActiveOffer() {
		asyncRequestPersonalLoan = activeOfferAsyncAPI();
		asyncRequestPersonalLoan.execute();
	}

	private HttpAsyncTask<String, String, OfferActive> activeOfferAsyncAPI() {
		return new HttpAsyncTask<String, String, OfferActive>() {
			@Override
			protected OfferActive httpDoInBackground(String... params) {
				return (woolworthsApplication.getApi().getActiveOfferRequest(productOfferingId));
			}

			@Override
			protected OfferActive httpError(String errorMessage, HttpErrorCode httpErrorCode) {
				networkFailureHandler();
				return new OfferActive();
			}

			@Override
			protected void onPreExecute() {
				mProgressCreditLimit.getIndeterminateDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
				mProgressCreditLimit.setVisibility(View.VISIBLE);
				iconIncreaseLimit.setVisibility(View.GONE);
				tvIncreaseLimit.setAlpha((float) 0.5);

				super.onPreExecute();
			}

			@Override
			protected void onPostExecute(OfferActive offerActive) {
				super.onPostExecute(offerActive);
				hideProgressBar();
				try {
					int httpCode = offerActive.httpCode;
					String httpDesc = offerActive.response.desc;
					if (httpCode == 200) {
						cardHasId = true;
						isOfferActive = offerActive.offerActive;
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
			}

			@Override
			protected Class<OfferActive> httpDoInBackgroundReturnType() {
				return OfferActive.class;
			}
		};
	}

	public void hideProgressBar() {
		mProgressCreditLimit.getIndeterminateDrawable().setColorFilter(null);
		mProgressCreditLimit.setVisibility(View.GONE);
		iconIncreaseLimit.setVisibility(View.VISIBLE);
		tvIncreaseLimit.setVisibility(View.VISIBLE);
	}

	public void enableIncreaseLimit() {
		tvIncreaseLimit.setEnabled(true);
		tvIncreaseLimit.setTextColor(Color.BLACK);
		iconIncreaseLimit.setImageAlpha(255);
	}

	public void disableIncreaseLimit() {
		tvIncreaseLimit.setEnabled(false);
		tvIncreaseLimit.setTextColor(Color.GRAY);
		iconIncreaseLimit.setImageAlpha(75);
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

		if (temp != null)
			bindData(temp);
		setTextSize();
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		personalLoanInfo = (PersonalLoanAmount) context;
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
				if (!cardHasId) {
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
				isOfferActive = false;
				hideProgressBar();
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
		if (!cardHasId) {
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
		if (!cardHasId) {
			if (new ConnectionDetector().isOnline(getActivity()))
				getActiveOffer();
			else {
				mErrorHandlerView.showToast();
				disableIncreaseLimit();
			}
		}
	}
}

