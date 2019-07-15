package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import retrofit2.Call;
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.contracts.RequestListener;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.TransactionHistoryResponse;
import za.co.woolworths.financial.services.android.models.network.CompletionHandler;
import za.co.woolworths.financial.services.android.models.network.OneAppService;
import za.co.woolworths.financial.services.android.ui.adapters.WTransactionsAdapter;
import za.co.woolworths.financial.services.android.ui.views.FloatingActionButtonExpandable;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.ui.views.actionsheet.SingleButtonDialogFragment;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.NetworkManager;
import za.co.woolworths.financial.services.android.util.SessionUtilities;
import za.co.woolworths.financial.services.android.util.Utils;

public class WTransactionsActivity extends AppCompatActivity implements View.OnClickListener {

	public Toolbar toolbar;
	public ExpandableListView transactionListview;
	public String productOfferingId;
	private ErrorHandlerView mErrorHandlerView;
	private ProgressBar pbTransaction;
	private Call<TransactionHistoryResponse> mExecuteTransactionRequest;
	private FloatingActionButtonExpandable chatIcon;
	private String accountNumber;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wtransactions_activity);
		Utils.updateStatusBarBackground(this);
		toolbar = findViewById(R.id.toolbar);
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
		transactionListview = findViewById(R.id.transactionListView);
		pbTransaction = findViewById(R.id.pbTransaction);
		chatIcon = findViewById(R.id.chatIcon);
		productOfferingId = getIntent().getStringExtra("productOfferingId");
		accountNumber = getIntent().getStringExtra("accountNumber");
		loadTransactionHistory(productOfferingId);
		findViewById(R.id.btnRetry).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (NetworkManager.getInstance().isConnectedToNetwork(WTransactionsActivity.this)) {
					loadTransactionHistory(productOfferingId);
				}
			}

		});
		chatIcon.expand(true);
		transactionListview.setOnScrollListener(new AbsListView.OnScrollListener(){
			private int lastPosition = -1;
			@Override
			public void onScrollStateChanged(AbsListView absListView, int i) {

			}

			@Override
			public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if(lastPosition == firstVisibleItem)
				{
					return;
				}

				if(firstVisibleItem > lastPosition)
				{
					if (transactionListview.getVisibility() == View.VISIBLE)
						chatIcon.collapse(true);
				}
				else
				{
					if (transactionListview.getVisibility() == View.VISIBLE)
						chatIcon.expand(true);
				}

				lastPosition = firstVisibleItem;
			}
		});
		chatIcon.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Utils.setScreenName(this, FirebaseManagerAnalyticsProperties.ScreenNames.TRANSACTIONS);
	}

	public void loadTransactionHistory(final String prOfferId) {
			pbTransaction.setVisibility(View.VISIBLE);
			mExecuteTransactionRequest = transactionAsyncAPI(prOfferId);
	}

	private Call<TransactionHistoryResponse> transactionAsyncAPI(final String productOfferingId) {

		Call<TransactionHistoryResponse> transactionHistoryRequestCall = OneAppService.INSTANCE.getAccountTransactionHistory(productOfferingId);
		transactionHistoryRequestCall.enqueue(new CompletionHandler<>(new RequestListener<TransactionHistoryResponse>() {
			@Override
			public void onSuccess(TransactionHistoryResponse transactionHistoryResponse) {
				if (getSupportFragmentManager() != null) {
					dismissProgress();
					switch (transactionHistoryResponse.httpCode) {
						case 200:
							if (transactionHistoryResponse.transactions.size() > 0) {
								mErrorHandlerView.hideEmpyState();
								transactionListview.setAdapter(new WTransactionsAdapter(WTransactionsActivity.this, Utils.getdata(transactionHistoryResponse.transactions)));
								transactionListview.setVisibility(View.VISIBLE);
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

			@Override
			public void onFailure(Throwable error) {
					if(error != null)
						networkFailureHandler(error.getMessage());
			}
		},TransactionHistoryResponse.class));
		return transactionHistoryRequestCall;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
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

		if (mExecuteTransactionRequest != null && !mExecuteTransactionRequest.isCanceled()) {
			mExecuteTransactionRequest.cancel();
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.chatIcon:
				Intent intent = new Intent(this, WChatActivity.class);
				intent.putExtra("productOfferingId", productOfferingId);
				intent.putExtra("accountNumber", accountNumber);
				startActivity(intent);
				break;
			default:
				break;
		}
	}
}
