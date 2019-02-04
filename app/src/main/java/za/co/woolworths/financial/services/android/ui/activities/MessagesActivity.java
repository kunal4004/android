package za.co.woolworths.financial.services.android.ui.activities;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;
import com.daimajia.swipe.util.Attributes;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.DeleteMessageResponse;
import za.co.woolworths.financial.services.android.models.dto.MessageDetails;
import za.co.woolworths.financial.services.android.models.dto.MessageRead;
import za.co.woolworths.financial.services.android.models.dto.MessageReadRequest;
import za.co.woolworths.financial.services.android.models.dto.MessageResponse;
import za.co.woolworths.financial.services.android.models.dto.ReadMessagesResponse;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity;
import za.co.woolworths.financial.services.android.ui.adapters.MesssagesListAdapter;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.NetworkManager;
import za.co.woolworths.financial.services.android.util.NotificationUtils;
import za.co.woolworths.financial.services.android.util.SessionUtilities;
import za.co.woolworths.financial.services.android.util.Utils;

public class MessagesActivity extends AppCompatActivity implements MesssagesListAdapter.MessageClickListener {
	public RecyclerView messsageListview;
	public MesssagesListAdapter adapter = null;
	public LinearLayoutManager mLayoutManager;
	public Toolbar toolbar;
	SwipeRefreshLayout swipeRefreshLayout;
	private BroadcastReceiver mRegistrationBroadcastReceiver;

	//Pagination-----------------------------------------//
	public static final int PAGE_SIZE = 5;
	private boolean mIsLoading = false;
	private boolean mIsLastPage = false;
	private int mCurrentPage = 1;
	public List<MessageDetails> messageList;
	private final ThreadLocal<FragmentManager> fm = new ThreadLocal<>();
	private ErrorHandlerView mErrorHandlerView;
	private boolean paginationIsEnabled = false;
	private int unreadMessageCount = 0;
	private HttpAsyncTask<String, String, DeleteMessageResponse> mDeleteMessageRequest;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.messages_activity);
		Utils.updateStatusBarBackground(this);
		WoolworthsApplication woolWorthsApplication = (WoolworthsApplication) MessagesActivity.this.getApplication();
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(null);
		mLayoutManager = new LinearLayoutManager(MessagesActivity.this);
		messsageListview = findViewById(R.id.messsageListView);
		swipeRefreshLayout = findViewById(R.id.swipeToRefresh);
		mErrorHandlerView = new ErrorHandlerView(this, woolWorthsApplication,
				(RelativeLayout) findViewById(R.id.relEmptyStateHandler),
				(ImageView) findViewById(R.id.imgEmpyStateIcon),
				(WTextView) findViewById(R.id.txtEmptyStateTitle),
				(WTextView) findViewById(R.id.txtEmptyStateDesc),
				(RelativeLayout) findViewById(R.id.no_connection_layout));
		messsageListview.setHasFixedSize(true);
		messsageListview.setLayoutManager(mLayoutManager);
		swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				swipeRefreshLayout.setRefreshing(true);
				loadMessages();
			}
		});
		messsageListview.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
				super.onScrollStateChanged(recyclerView, newState);
			}

			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				int visibleItemCount = mLayoutManager.getChildCount();
				int totalItemCount = mLayoutManager.getItemCount();
				int firstVisibleItemPosition = mLayoutManager.findFirstVisibleItemPosition();
				if (!mIsLoading && !mIsLastPage) {
					if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
							&& firstVisibleItemPosition >= 0
							&& totalItemCount >= PAGE_SIZE) {
						loadMoreMessages();

					}
				}
			}
		});
		mRegistrationBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals(Utils.PUSH_NOTIFICATION)) {
					loadMessages();
				}
			}
		};

		loadMessages();
		findViewById(R.id.btnRetry).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (NetworkManager.getInstance().isConnectedToNetwork(MessagesActivity.this)) {
					loadMessages();
				}
			}

		});
	}

	public void loadMessages() {
		messageAsyncRequest().execute();
	}

	public HttpAsyncTask<String, String, MessageResponse> messageAsyncRequest() {
		fm.set(getSupportFragmentManager());
		return new HttpAsyncTask<String, String, MessageResponse>() {
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				mErrorHandlerView.hideErrorHandlerLayout();
			}

			@Override
			protected MessageResponse httpDoInBackground(String... params) {
				mCurrentPage = 1;
				mIsLastPage = false;
				return ((WoolworthsApplication) MessagesActivity.this.getApplication()).getApi()
						.getMessagesResponse
								(PAGE_SIZE, mCurrentPage);
			}

			@Override
			protected Class<MessageResponse> httpDoInBackgroundReturnType() {
				return MessageResponse.class;
			}

			@Override
			protected MessageResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
				networkFailureHandler(errorMessage, 0);
				return new MessageResponse();
			}


			@Override
			protected void onPostExecute(MessageResponse messageResponse) {
				super.onPostExecute(messageResponse);
				messsageListview.setVisibility(View.GONE);
				handleLoadMessagesResponse(messageResponse);
			}
		};
	}

	public void bindDataWithUI(List<MessageDetails> messageDetailsList) {
		mErrorHandlerView.hideEmpyState();
		messsageListview.setVisibility(View.VISIBLE);
		adapter = new MesssagesListAdapter(MessagesActivity.this, messageDetailsList);
		adapter.setMode(Attributes.Mode.Single);
		messsageListview.setAdapter(adapter);

	}

	private void loadMoreMessages() {
		moreMessageAsyncRequest().execute();
	}

	public HttpAsyncTask<String, String, MessageResponse> moreMessageAsyncRequest() {
		return new HttpAsyncTask<String, String, MessageResponse>() {
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				mErrorHandlerView.hideErrorHandlerLayout();
				mIsLoading = true;
			}

			@Override
			protected MessageResponse httpDoInBackground(String... params) {
				return ((WoolworthsApplication) MessagesActivity.this.getApplication()).getApi()
						.getMessagesResponse
								(PAGE_SIZE, mCurrentPage);
			}

			@Override
			protected Class<MessageResponse> httpDoInBackgroundReturnType() {
				return MessageResponse.class;
			}

			@Override
			protected MessageResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
				networkFailureHandler(errorMessage, 1);
				return new MessageResponse();
			}

			@Override
			protected void onPostExecute(MessageResponse messageResponse) {
				super.onPostExecute(messageResponse);
				int httpCode = messageResponse.httpCode;
				switch (httpCode) {
					case 200:
						mCurrentPage += 1;
						mIsLoading = false;
						List<MessageDetails> moreMessageList;
						moreMessageList = messageResponse.messagesList;
						if (moreMessageList != null && moreMessageList.size() != 0) {
							if (moreMessageList.size() < PAGE_SIZE) {
								mIsLastPage = true;
							}
							messageList.addAll(moreMessageList);
							adapter.notifyDataSetChanged();
							setMeassagesAsRead(moreMessageList);
						}
						break;
					case 440:
						SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, messageResponse.response.stsParams, MessagesActivity.this);
						break;

					default:
						if (messageResponse.response != null) {
							Utils.alertErrorMessage(MessagesActivity.this, messageResponse.response.desc);
						}
						break;
				}


			}
		};
	}

	public void setMeassagesAsRead(final List<MessageDetails> readMessages) {
		new HttpAsyncTask<String, String, ReadMessagesResponse>() {
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
			}

			@Override
			protected ReadMessagesResponse httpDoInBackground(String... params) {
				return ((WoolworthsApplication) MessagesActivity.this.getApplication()).getApi()
						.getReadMessagesResponse(getJsonString(readMessages));
			}

			@Override
			protected Class<ReadMessagesResponse> httpDoInBackgroundReturnType() {
				return ReadMessagesResponse.class;
			}

			@Override
			protected ReadMessagesResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
				ReadMessagesResponse readmessageResponse = new ReadMessagesResponse();
				readmessageResponse.response = new Response();
				return readmessageResponse;
			}

			@Override
			protected void onPostExecute(ReadMessagesResponse readmessageResponse) {
				super.onPostExecute(readmessageResponse);

			}
		}.execute();
	}

	public MessageReadRequest getJsonString(List<MessageDetails> readMessages) {
		MessageReadRequest msgRequest = new MessageReadRequest();
		List<MessageRead> msgList = new ArrayList<>();
		MessageRead msg;
		for (int i = 0; i < readMessages.size(); i++) {
			msg = new MessageRead();
			msg.id = Integer.parseInt(readMessages.get(i).id);
			msg.isRead = true;
			msgList.add(msg);
		}
		msgRequest.messages = msgList;
		return msgRequest;
	}

	public void hideRefreshView() {
		if (swipeRefreshLayout.isRefreshing()) {
			swipeRefreshLayout.setRefreshing(false);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Utils.setScreenName(this, FirebaseManagerAnalyticsProperties.ScreenNames.MESSAGES);
		// register new push message receiver
		// by doing this, the activity will be notified each time a new message arrives
		LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
				new IntentFilter(Utils.PUSH_NOTIFICATION));
		// clear the notification area when the app is opened
		NotificationUtils.clearNotifications(getApplicationContext());
	}

	@Override
	public void onBackPressed() {
		boolean fromNotification = false;
		if (getIntent().hasExtra("fromNotification"))
			fromNotification = getIntent().getExtras().getBoolean("fromNotification");
		if (fromNotification) {
			startActivityForResult(new Intent(MessagesActivity.this, BottomNavigationActivity.class), 0);
			finish();
			overridePendingTransition(R.anim.stay, R.anim.slide_down_anim);
		} else {
			super.onBackPressed();
			overridePendingTransition(R.anim.stay, R.anim.slide_down_anim);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				overridePendingTransition(R.anim.stay, R.anim.slide_down_anim);
				return true;
		}
		return false;
	}

	public void handleLoadMessagesResponse(MessageResponse messageResponse) {
		hideRefreshView();
		try {
			int httpCode = messageResponse.httpCode;
			switch (httpCode) {
				case 200:
					messageList = null;
					messageList = new ArrayList<>();
					if (messageResponse.messagesList != null && messageResponse.messagesList.size() != 0) {
						messageList = messageResponse.messagesList;
						unreadMessageCount = messageResponse.unreadCount;
						bindDataWithUI(messageList);
						String unreadCountValue = Utils.getSessionDaoValue(MessagesActivity.this,
								SessionDao.KEY.UNREAD_MESSAGE_COUNT);
						if (TextUtils.isEmpty(unreadCountValue)) {
							Utils.setBadgeCounter(0);
						} else {
							int unreadCount = Integer.valueOf(unreadCountValue) - messageList.size();
							Utils.setBadgeCounter(unreadCount);
						}
						setMeassagesAsRead(messageList);
						mIsLastPage = false;
						mCurrentPage = 1;
						mIsLoading = false;
					} else {
						if (messageResponse.messagesList != null) {
							if (messageResponse.messagesList.size() == 0) {
								emptyList();
							}
						}
					}
					break;
				case 440:
					SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, messageResponse.response.stsParams, MessagesActivity.this);
					break;
				default:
					mErrorHandlerView.networkFailureHandler("");
					Utils.displayValidationMessage(MessagesActivity.this, CustomPopUpWindow.MODAL_LAYOUT.ERROR, messageResponse.response.desc);
					break;
			}
		} catch (Exception ignored) {
		}
	}

	private void emptyList() {
		messsageListview.setVisibility(View.GONE);
		mErrorHandlerView.setEmptyState(5);
		mErrorHandlerView.showErrorView();
	}

	public void networkFailureHandler(final String errorMessage, final int type) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (type == 0) {
					hideRefreshView();
				} else {
					mIsLoading = false;
				}
				mErrorHandlerView.networkFailureHandler(errorMessage);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (paginationIsEnabled) {
			loadMoreMessages();
		} else {
			loadMessages();
		}
	}

	@Override
	public void onDeleteItemClicked(String id) {
		mDeleteMessageRequest = deleteMessage(id);
		mDeleteMessageRequest.execute();
	}

	@Override
	public void messageInboxIsEmpty(int sizeOfList) {
		if (sizeOfList == 0) {
			emptyList();
		}
	}

	@SuppressLint("StaticFieldLeak")
	public HttpAsyncTask<String, String, DeleteMessageResponse> deleteMessage(final String id) {
		return new HttpAsyncTask<String, String, DeleteMessageResponse>() {
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
			}

			@Override
			protected DeleteMessageResponse httpDoInBackground(String... params) {
				return (WoolworthsApplication.getInstance()).getApi().getDeleteMessagesResponse(id);
			}

			@Override
			protected Class<DeleteMessageResponse> httpDoInBackgroundReturnType() {
				return DeleteMessageResponse.class;
			}

			@Override
			protected DeleteMessageResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
				DeleteMessageResponse deleteMessageResponse = new DeleteMessageResponse();
				deleteMessageResponse.response = new Response();
				return deleteMessageResponse;
			}
		};
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mDeleteMessageRequest != null && !mDeleteMessageRequest.isCancelled()) {
			mDeleteMessageRequest.cancel(true);
		}
	}
}
