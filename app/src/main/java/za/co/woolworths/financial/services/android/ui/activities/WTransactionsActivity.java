package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import org.jetbrains.annotations.NotNull;

import retrofit2.Call;
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.contracts.IResponseListener;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.TransactionHistoryResponse;
import za.co.woolworths.financial.services.android.models.network.CompletionHandler;
import za.co.woolworths.financial.services.android.models.network.OneAppService;
import za.co.woolworths.financial.services.android.ui.adapters.AccountTransactionAdapter;
import za.co.woolworths.financial.services.android.ui.views.FloatingActionButtonExpandable;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.ui.views.actionsheet.SingleButtonDialogFragment;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.KotlinUtils;
import za.co.woolworths.financial.services.android.util.NetworkManager;
import za.co.woolworths.financial.services.android.util.SessionUtilities;
import za.co.woolworths.financial.services.android.util.Utils;

import static androidx.lifecycle.Lifecycle.State.STARTED;

public class WTransactionsActivity extends AppCompatActivity implements View.OnClickListener{

	public RecyclerView transactionListView;
	public String productOfferingId;
	private ErrorHandlerView mErrorHandlerView;
	private ProgressBar pbTransaction;
	private Call<TransactionHistoryResponse> mExecuteTransactionRequest;
	private FloatingActionButtonExpandable chatIcon;
	private String accountNumber;
	private int lastPosition = -1;
	private String cardType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wtransactions_activity);
		Utils.updateStatusBarBackground(this);
		WoolworthsApplication woolworthsApplication = (WoolworthsApplication) WTransactionsActivity.this.getApplication();
		mErrorHandlerView = new ErrorHandlerView(this, woolworthsApplication,
				(RelativeLayout) findViewById(R.id.relEmptyStateHandler),
				(ImageView) findViewById(R.id.imgEmpyStateIcon),
				(WTextView) findViewById(R.id.txtEmptyStateTitle),
				(WTextView) findViewById(R.id.txtEmptyStateDesc),
				(RelativeLayout) findViewById(R.id.no_connection_layout));

		ImageButton mCloseTransactionImageButton = (ImageButton) findViewById(R.id.closeTransactionImageButton);
		mCloseTransactionImageButton.setOnClickListener(this);

		transactionListView = (RecyclerView) findViewById(R.id.transactionListView);
		LinearLayoutManager linearLayoutManager = new  LinearLayoutManager(this);
		linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		transactionListView.setLayoutManager(linearLayoutManager);

		pbTransaction = findViewById(R.id.pbTransaction);
		chatIcon = findViewById(R.id.chatIcon);
		productOfferingId = getIntent().getStringExtra("productOfferingId");
		accountNumber = getIntent().getStringExtra("accountNumber");
		cardType = getIntent().getStringExtra("cardType");
		loadTransactionHistory(productOfferingId);
		findViewById(R.id.btnRetry).setOnClickListener(v -> {
			if (NetworkManager.getInstance().isConnectedToNetwork(WTransactionsActivity.this)) {
				loadTransactionHistory(productOfferingId);
			}
		});
		initInAppChat();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Utils.setScreenName(this, FirebaseManagerAnalyticsProperties.ScreenNames.TRANSACTIONS);
	}

	public void loadTransactionHistory(final String prOfferId) {
			pbTransaction.setVisibility(View.VISIBLE);
			transactionAsyncAPI(prOfferId);
	}

	private void transactionAsyncAPI(final String productOfferingId) {
		mExecuteTransactionRequest = OneAppService.INSTANCE.getAccountTransactionHistory(productOfferingId);
		mExecuteTransactionRequest.enqueue(new CompletionHandler<>(new IResponseListener<TransactionHistoryResponse>() {
			@Override
			public void onSuccess(TransactionHistoryResponse transactionHistoryResponse) {
				dismissProgress();
				if (WTransactionsActivity.this.getLifecycle().getCurrentState().isAtLeast(STARTED)) {
					switch (transactionHistoryResponse.httpCode) {
						case 200:
							if (transactionHistoryResponse.transactions.size() > 0) {
								mErrorHandlerView.hideEmpyState();
								AccountTransactionAdapter transactionsAdapter = new AccountTransactionAdapter(KotlinUtils.Companion.getListOfTransaction(transactionHistoryResponse.transactions));
								transactionListView.setAdapter(transactionsAdapter);
								transactionListView.setVisibility(View.VISIBLE);
							} else {
								transactionListView.setVisibility(View.GONE);
								mErrorHandlerView.showEmptyState(3);
							}
							showChatBubble();
							break;
						case 440:
							if (!(WTransactionsActivity.this.isFinishing())) {
								SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, transactionHistoryResponse
										.response.stsParams, WTransactionsActivity.this);
							}
							break;
						default:
							if (transactionHistoryResponse.response != null && transactionHistoryResponse.response.desc != null) {
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
				if (WTransactionsActivity.this.getLifecycle().getCurrentState().isAtLeast(STARTED)) {
					if (error != null)
						networkFailureHandler(error.getMessage());
				}
			}
		},TransactionHistoryResponse.class));
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
		runOnUiThread(() -> {
			mErrorHandlerView.networkFailureHandler(errorMessage);
			dismissProgress();
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
			case R.id.closeTransactionImageButton:
				onBackPressed();
				break;
			case R.id.chatIcon:
				Utils.triggerFireBaseEvents(Utils.isOperatingHoursForInAppChat() ? FirebaseManagerAnalyticsProperties.MY_ACCOUNTS_CHAT_ONLINE : FirebaseManagerAnalyticsProperties.MY_ACCOUNTS_CHAT_OFFLINE);
				Intent intent = new Intent(this, WChatActivity.class);
				intent.putExtra("productOfferingId", productOfferingId);
				intent.putExtra("accountNumber", accountNumber);
				startActivity(intent);
				break;
			default:
				break;
		}
	}

    private void initInAppChat() {
		cardType = "cc";
        if (cardType.equalsIgnoreCase("CC") && chatIsEnabled()) {
            chatIcon.expand(true);
            chatIcon.setStatusIndicatorIcon(Utils.isOperatingHoursForInAppChat() ? R.drawable.indicator_online : R.drawable.indicator_offline);
			transactionListView.addOnScrollListener(new RecyclerView.OnScrollListener() {

				@Override
				public void onScrollStateChanged(@NotNull RecyclerView recyclerView, int newState) {
					super.onScrollStateChanged(recyclerView, newState);

				}

				@Override
				public void onScrolled(@NotNull RecyclerView recyclerView, int dx, int dy) {
					super.onScrolled(recyclerView, dx, dy);
					LinearLayoutManager layoutManager = ((LinearLayoutManager) transactionListView.getLayoutManager());
					int firstVisibleItem = 0;
					if (layoutManager != null) {
						firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
					}

					if (lastPosition == firstVisibleItem) {
						return;
					}

					if (firstVisibleItem > lastPosition) {
						if (transactionListView.getVisibility() == View.VISIBLE)
							chatIcon.collapse(true);
					} else {
						if (transactionListView.getVisibility() == View.VISIBLE)
							chatIcon.expand(true);
					}

					lastPosition = firstVisibleItem;
				}
			});
            chatIcon.setOnClickListener(this);
        }
    }

    private void showChatBubble() {
        if (cardType.equalsIgnoreCase("CC") && chatIsEnabled())
            chatIcon.setVisibility(View.VISIBLE);
    }

	private boolean chatIsEnabled() {
		boolean chatIsEnabled;
		try {
			chatIsEnabled = WoolworthsApplication.getPresenceInAppChat().isEnabled();
		} catch (NullPointerException npe) {
			chatIsEnabled = false;
		}
		return chatIsEnabled;
	}
}
