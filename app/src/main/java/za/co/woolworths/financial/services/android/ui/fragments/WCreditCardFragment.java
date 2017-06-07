package za.co.woolworths.financial.services.android.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.awfs.coordination.R;
import com.google.gson.Gson;

import java.text.ParseException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import za.co.wigroup.logger.lib.WiGroupLogger;
import za.co.woolworths.financial.services.android.FragmentLifecycle;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.Account;
import za.co.woolworths.financial.services.android.models.dto.AccountsResponse;
import za.co.woolworths.financial.services.android.models.dto.OfferActive;
import za.co.woolworths.financial.services.android.ui.activities.CLIActivity;
import za.co.woolworths.financial.services.android.ui.activities.MyAccountCardsActivity;
import za.co.woolworths.financial.services.android.ui.activities.WTransactionsActivity;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.FontHyperTextParser;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.NetworkChangeListener;
import za.co.woolworths.financial.services.android.util.NetworkFailureInterface;
import za.co.woolworths.financial.services.android.util.PopWindowValidationMessage;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WFormatter;


public class WCreditCardFragment extends MyAccountCardsActivity.MyAccountCardsFragment implements View.OnClickListener, FragmentLifecycle,NetworkChangeListener {

	private NetworkFailureInterface mNetworkFailureInterface;
	public WTextView availableBalance;
	public WTextView creditLimit;
	public WTextView dueDate;
	public WTextView minAmountDue;
	public WTextView currentBalance;
	public WTextView transactions;
	public WTextView txtIncreseLimit;
	private String productOfferingId;
	private WoolworthsApplication woolworthsApplication;
	private ProgressBar mProgressCreditLimit;
	private boolean isOfferActive = false;
	private ImageView mImageArrow;
	private PopWindowValidationMessage mPopWindowValidationMessage;
	private AsyncTask<String, String, OfferActive> asyncRequestCredit;
	private boolean cardHasId = false;
	private ErrorHandlerView mErrorHandlerView;
	private BroadcastReceiver connectionBroadcast;
	private NetworkChangeListener networkChangeListener;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.cards_common_fragment, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		/*try {
			mNetworkFailureInterface = (NetworkFailureInterface) getActivity();
		} catch (ClassCastException ignored) {
		}*/
		woolworthsApplication = (WoolworthsApplication) getActivity().getApplication();
		availableBalance = (WTextView) view.findViewById(R.id.available_funds);
		creditLimit = (WTextView) view.findViewById(R.id.creditLimit);
		mPopWindowValidationMessage = new PopWindowValidationMessage(getActivity());
		dueDate = (WTextView) view.findViewById(R.id.dueDate);
		minAmountDue = (WTextView) view.findViewById(R.id.minAmountDue);
		currentBalance = (WTextView) view.findViewById(R.id.currentBalance);
		transactions = (WTextView) view.findViewById(R.id.txtTransactions);
		txtIncreseLimit = (WTextView) view.findViewById(R.id.txtIncreseLimit);
		mProgressCreditLimit = (ProgressBar) view.findViewById(R.id.progressCreditLimit);
		mProgressCreditLimit.getIndeterminateDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
		mImageArrow = (ImageView) view.findViewById(R.id.imgArrow);
		transactions.setOnClickListener(this);
		txtIncreseLimit.setOnClickListener(this);
		try {
			networkChangeListener = (NetworkChangeListener) this;
		} catch (ClassCastException ignored) {
		}
		connectionBroadcast= Utils.connectionBroadCast(getActivity(),networkChangeListener);
		AccountsResponse accountsResponse = new Gson().fromJson(getArguments().getString("accounts"), AccountsResponse.class);
		bindData(accountsResponse);
		disableIncreaseLimit();
		hideProgressBar();
		view.setBackgroundColor(Color.WHITE);
		mErrorHandlerView=new ErrorHandlerView(getActivity());
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
				if ("CC".equals(p.productGroupCode)) {
					productOfferingId = String.valueOf(p.productOfferingId);
					woolworthsApplication.setProductOfferingId(p.productOfferingId);
					availableBalance.setText(removeNegativeSymbol(FontHyperTextParser.getSpannable(WFormatter.formatAmount(p.availableFunds), 1, getActivity())));
					creditLimit.setText(removeNegativeSymbol(FontHyperTextParser.getSpannable(WFormatter.formatAmount(p.creditLimit), 1, getActivity())));
					minAmountDue.setText(removeNegativeSymbol(WFormatter.formatAmount(p.minimumAmountDue)));
					currentBalance.setText(removeNegativeSymbol(WFormatter.formatAmount(p.currentBalance)));
					WoolworthsApplication.setCreditCardType(p.accountNumberBin);
					try {
						dueDate.setText(WFormatter.formatDate(p.paymentDueDate));
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
			case R.id.txtTransactions:
				Intent intent = new Intent(getActivity(), WTransactionsActivity.class);
				intent.putExtra("productOfferingId", productOfferingId);
				startActivity(intent);
				getActivity().overridePendingTransition(R.anim.slide_up_anim, R.anim
						.stay);

				break;

			case R.id.txtIncreseLimit:
				Log.e("productOfferIdStore", String.valueOf(productOfferingId));
				if (!isOfferActive) {
					((WoolworthsApplication) getActivity().getApplication()).setProductOfferingId(Integer.valueOf(productOfferingId));
					Intent openCLIIncrease = new Intent(getActivity(), CLIActivity.class);
					startActivity(openCLIIncrease);
					getActivity().overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
				}
				break;
		}
	}

	private void getActiveOffer() {
		asyncRequestCredit = activeOfferAsyncAPI();
		activeOfferAsyncAPI().execute();
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
				mProgressCreditLimit.setVisibility(View.VISIBLE);
				mImageArrow.setVisibility(View.GONE);
				txtIncreseLimit.setVisibility(View.GONE);
				super.onPreExecute();
			}

			@Override
			protected void onPostExecute(OfferActive offerActive) {
				super.onPostExecute(offerActive);
				try {
					int httpCode = offerActive.httpCode;
					String httpDesc = offerActive.response.desc;
					if (httpCode == 200) {
						isOfferActive = offerActive.offerActive;
						cardHasId = true;
						if (isOfferActive) {
							disableIncreaseLimit();
						} else {
							enableIncreaseLimit();
						}
					} else {
						disableIncreaseLimit();
						mPopWindowValidationMessage.displayValidationMessage(httpDesc,
								PopWindowValidationMessage.OVERLAY_TYPE.ERROR);
					}
				} catch (NullPointerException ignored) {
				}
				hideProgressBar();
			}

			@Override
			protected Class<OfferActive> httpDoInBackgroundReturnType() {
				return OfferActive.class;
			}
		};
	}

	public void hideProgressBar() {
		mProgressCreditLimit.setVisibility(View.GONE);
		mImageArrow.setVisibility(View.VISIBLE);
		txtIncreseLimit.setVisibility(View.VISIBLE);
	}

	public void enableIncreaseLimit() {
		txtIncreseLimit.setEnabled(true);
		txtIncreseLimit.setTextColor(Color.BLACK);
		mImageArrow.setImageAlpha(255);
	}

	public void disableIncreaseLimit() {
		txtIncreseLimit.setEnabled(false);
		txtIncreseLimit.setTextColor(Color.GRAY);
		mImageArrow.setImageAlpha(75);
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
		getActivity().registerReceiver(connectionBroadcast,new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
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
		if (asyncRequestCredit != null) {
			asyncRequestCredit.isCancelled();
		}
	}

	@Override
	public void onResumeFragment() {
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (!cardHasId) {
					if (new ConnectionDetector().isOnline())
					getActiveOffer();
					else {
						mErrorHandlerView.showToast();
						disableIncreaseLimit();
					}
				}
			}
		}, 100);
	}

	public void networkFailureHandler() {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				isOfferActive = false;
				hideProgressBar();
				//mNetworkFailureInterface.onNetworkFailure();
			}
		});
	}


	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(connectionBroadcast);
	}
	@Override
	public void onConnectionChanged() {
		//connection changed
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (!cardHasId) {
					if (new ConnectionDetector().isOnline())
						getActiveOffer();

				}
			}
		}, 100);
	}
}
