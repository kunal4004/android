package za.co.woolworths.financial.services.android.ui.activities;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import java.util.ArrayList;

import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.TransactionHistoryResponse;
import za.co.woolworths.financial.services.android.ui.adapters.WTransactionsAdapter;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.ui.views.actionsheet.SingleButtonDialogFragment;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.NetworkManager;
import za.co.woolworths.financial.services.android.util.SessionUtilities;
import za.co.woolworths.financial.services.android.util.Utils;

public class WTransactionsActivity extends AppCompatActivity {

	public Toolbar toolbar;
	public ExpandableListView transactionListview;
	public String productOfferingId;
	private ErrorHandlerView mErrorHandlerView;
	private ProgressBar pbTransaction;
	private AsyncTask<String, String, TransactionHistoryResponse> mExecuteTransactionRequest;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wtransactions_activity);
		Utils.updateStatusBarBackground(this);
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		WoolworthsApplication woolworthsApplication = (WoolworthsApplication) WTransactionsActivity.this.getApplication();
		mErrorHandlerView = new ErrorHandlerView(this, woolworthsApplication,
				(RelativeLayout) findViewById(R.id.relEmptyStateHandler),
				(ImageView) findViewById(R.id.imgEmpyStateIcon),
				(WTextView) findViewById(R.id.txtEmptyStateTitle),
				(WTextView) findViewById(R.id.txtEmptyStateDesc),
				(RelativeLayout) findViewById(R.id.no_connection_layout));

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(null);
		getSupportActionBar().setElevation(0);
		transactionListview = (ExpandableListView) findViewById(R.id.transactionListView);
		pbTransaction = findViewById(R.id.pbTransaction);
		productOfferingId = getIntent().getStringExtra("productOfferingId");
		loadTransactionHistory(productOfferingId);
		findViewById(R.id.btnRetry).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (NetworkManager.getInstance().isConnectedToNetwork(WTransactionsActivity.this)) {
					loadTransactionHistory(productOfferingId);
				}
			}

		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		Utils.setScreenName(this, FirebaseManagerAnalyticsProperties.ScreenNames.TRANSACTIONS);
	}

	public void loadTransactionHistory(final String prOfferId) {
			pbTransaction.setVisibility(View.VISIBLE);
			mExecuteTransactionRequest = transactionAsyncAPI(prOfferId).execute();
	}

	@SuppressLint("StaticFieldLeak")
	private HttpAsyncTask<String, String, TransactionHistoryResponse> transactionAsyncAPI(final String prOfferId) {
		return new HttpAsyncTask<String, String, TransactionHistoryResponse>() {
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				mErrorHandlerView.hideErrorHandlerLayout();
			}

			@Override
			protected TransactionHistoryResponse httpDoInBackground(String... params) {
				return ((WoolworthsApplication) WTransactionsActivity.this.getApplication())
						.getApi()
						.getAccountTransactionHistory(prOfferId);
			}

			@Override
			protected Class<TransactionHistoryResponse> httpDoInBackgroundReturnType() {
				return TransactionHistoryResponse.class;
			}

			@Override
			protected TransactionHistoryResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
				networkFailureHandler(errorMessage);
				return new TransactionHistoryResponse();
			}

			@Override
			protected void onPostExecute(TransactionHistoryResponse transactionHistoryResponse) {
				super.onPostExecute(transactionHistoryResponse);
				if (WTransactionsActivity.this != null && getSupportFragmentManager() != null) {
					dismissProgress();
					int httpCode = transactionHistoryResponse.httpCode;
					httpCode = 200;
					transactionHistoryResponse.transactions = new ArrayList<>();
					switch (httpCode) {
						case 200:
							if (transactionHistoryResponse.transactions.size() > 0) {
								transactionListview.setVisibility(View.VISIBLE);
								mErrorHandlerView.hideEmpyState();
								transactionListview.setAdapter(new WTransactionsAdapter(WTransactionsActivity.this, Utils.getdata(transactionHistoryResponse.transactions)));
							} else {
								transactionListview.setVisibility(View.GONE);
								mErrorHandlerView.showEmptyState(3);
							}
							break;
						case 440:
							if (!(WTransactionsActivity.this.isFinishing())) {
								SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, transactionHistoryResponse
										.response.stsParams, WTransactionsActivity.this);
							}
							break;
						default:
							if (transactionHistoryResponse.response != null) {
								if (!TextUtils.isEmpty(transactionHistoryResponse.response.desc)) {
									SingleButtonDialogFragment singleButtonDialogFragment = SingleButtonDialogFragment.newInstance(transactionHistoryResponse.response.desc);
									singleButtonDialogFragment.show(getSupportFragmentManager(), SingleButtonDialogFragment.class.getSimpleName());
								}
							}
							break;
					}
				}
			}
		};
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				return true;
		}
		return false;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.stay, R.anim.slide_down_anim);
	}

	private void dismissProgress() {
		if (pbTransaction != null)
			pbTransaction.setVisibility(View.GONE);

	}

	public void networkFailureHandler(final String errorMessage) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mErrorHandlerView.networkFailureHandler(errorMessage);
				dismissProgress();
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mExecuteTransactionRequest != null && !mExecuteTransactionRequest.isCancelled()) {
			mExecuteTransactionRequest.cancel(true);
		}
	}
}
