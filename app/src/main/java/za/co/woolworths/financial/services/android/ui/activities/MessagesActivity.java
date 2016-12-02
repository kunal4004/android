package za.co.woolworths.financial.services.android.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.awfs.coordination.R;
import com.daimajia.swipe.util.Attributes;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.MessageDetails;
import za.co.woolworths.financial.services.android.models.dto.MessageRead;
import za.co.woolworths.financial.services.android.models.dto.MessageReadRequest;
import za.co.woolworths.financial.services.android.models.dto.MessageResponse;
import za.co.woolworths.financial.services.android.models.dto.ReadMessagesResponse;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.ui.adapters.MesssagesListAdapter;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.NotificationUtils;
import za.co.woolworths.financial.services.android.util.Utils;

public class MessagesActivity extends AppCompatActivity {
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
    int previousTotal = 0;
    public List<MessageDetails> messageList;
    public ProgressBar mLoadingImageView;
    public int visibleThreshold = 5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messages_activity);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.white));
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mLayoutManager = new LinearLayoutManager(MessagesActivity.this);
        messsageListview = (RecyclerView) findViewById(R.id.messsageListView);
        mLoadingImageView = (ProgressBar) findViewById(R.id.loadingBar);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeToRefresh);
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


    }

    public void loadMessages() {
        new HttpAsyncTask<String, String, MessageResponse>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected MessageResponse httpDoInBackground(String... params) {
                mCurrentPage = 1;
                mIsLastPage = false;
                return ((WoolworthsApplication) getApplication()).getApi().getMessagesResponse(PAGE_SIZE, mCurrentPage);
            }

            @Override
            protected Class<MessageResponse> httpDoInBackgroundReturnType() {
                return MessageResponse.class;
            }

            @Override
            protected MessageResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
                MessageResponse messageResponse = new MessageResponse();
                messageResponse.response = new Response();
                hideRefreshView();
                return messageResponse;
            }

            @Override
            protected void onPostExecute(MessageResponse messageResponse) {
                super.onPostExecute(messageResponse);
                messageList = null;
                messageList = new ArrayList<>();
                if (messageResponse.messagesList != null && messageResponse.messagesList.size() != 0) {
                    messageList = messageResponse.messagesList;
                    bindDataWithUI(messageList);
                    setMeassagesAsRead(messageList);
                    mIsLastPage = false;
                    mCurrentPage = 1;
                    mIsLoading = false;
                }
                hideRefreshView();


            }
        }.execute();
    }

    public void bindDataWithUI(List<MessageDetails> messageDetailsList) {
        adapter = new MesssagesListAdapter(MessagesActivity.this, messageDetailsList);
        ((MesssagesListAdapter) adapter).setMode(Attributes.Mode.Single);
        messsageListview.setAdapter(adapter);
    }

    public void loadMoreMessages() {
        new HttpAsyncTask<String, String, MessageResponse>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mLoadingImageView.setVisibility(View.VISIBLE);
                mIsLoading = true;
                mCurrentPage += 1;

            }

            @Override
            protected MessageResponse httpDoInBackground(String... params) {
                return ((WoolworthsApplication) getApplication()).getApi().getMessagesResponse(PAGE_SIZE, mCurrentPage);
            }

            @Override
            protected Class<MessageResponse> httpDoInBackgroundReturnType() {
                return MessageResponse.class;
            }

            @Override
            protected MessageResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
                MessageResponse messageResponse = new MessageResponse();
                messageResponse.response = new Response();
                mLoadingImageView.setVisibility(View.GONE);
                mIsLoading = false;
                return messageResponse;
            }

            @Override
            protected void onPostExecute(MessageResponse messageResponse) {
                super.onPostExecute(messageResponse);
                mLoadingImageView.setVisibility(View.GONE);
                mIsLoading = false;
                List<MessageDetails> moreMessageList = null;
                moreMessageList = new ArrayList<MessageDetails>();
                moreMessageList = messageResponse.messagesList;
                if (moreMessageList != null && moreMessageList.size() != 0) {
                    if (moreMessageList.size() < PAGE_SIZE) {
                        mIsLastPage = true;

                    }
                    messageList.addAll(moreMessageList);
                    adapter.notifyDataSetChanged();
                    setMeassagesAsRead(moreMessageList);
                }


            }
        }.execute();
    }

    public void setMeassagesAsRead(final List<MessageDetails> readMessages) {
        new HttpAsyncTask<String, String, ReadMessagesResponse>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();

            }

            @Override
            protected ReadMessagesResponse httpDoInBackground(String... params) {
                return ((WoolworthsApplication) getApplication()).getApi().getReadMessagesResponse(getJsonString(readMessages));
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

    /*public JSONObject getJsonString(List<MessageDetails> readMessages) {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        JSONObject obj = null;
        try {
            for (int i = 0; i < readMessages.size(); i++) {
                obj = new JSONObject();
                obj.put("id", Integer.parseInt(readMessages.get(i).id));
                obj.put("isRead", true);
                jsonArray.put(obj);
            }
            jsonObject.put("messages", jsonArray);

        } catch (Exception e) {
        }
        return jsonObject;
    }*/
    public MessageReadRequest getJsonString(List<MessageDetails> readMessages) {
        MessageReadRequest msgRequest = new MessageReadRequest();
        List<MessageRead> msgList = new ArrayList<>();
        MessageRead msg = null;
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
            startActivity(new Intent(MessagesActivity.this, WOneAppBaseActivity.class));
            overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);
            finish();
        } else {
            super.onBackPressed();
            overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
