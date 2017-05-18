package za.co.woolworths.financial.services.android.ui.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.SpannableString;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.awfs.coordination.R;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit.RetrofitError;
import za.co.wigroup.logger.lib.WiGroupLogger;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.Transaction;
import za.co.woolworths.financial.services.android.models.dto.TransactionHistoryResponse;
import za.co.woolworths.financial.services.android.util.FontHyperTextParser;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.WErrorDialog;
import za.co.woolworths.financial.services.android.util.WFormatter;

public class TransactionHistoryActivity extends Activity {

    public static final String ACCOUNT_TYPE = "account_type";
    public static final String PRODUCT_ID = "product_id";
    private static final String TAG = "TransactionHistoryActivity";
    private TransactionAdapter mAdapter = new TransactionAdapter();
    private ProgressDialog mProgressDialog;
    private List<Transaction> mTransactions = new ArrayList<Transaction>();
    private AlertDialog mLogin;

    private SwipeRefreshLayout swipeRefreshLayout;
    private int mProductId;
    private AlertDialog mError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transaction_history_activity);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        mLogin = WErrorDialog.getLoginErrorDialog(this);
        swipeRefreshLayout = ((SwipeRefreshLayout) findViewById(R.id.transaction_history_refresh));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getTransactions(false);
            }
        });
        mError = WErrorDialog.getSingleActionActivityErrorDialog(this, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finishActivity();
            }
        });
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(FontHyperTextParser.getSpannable(getString(R.string.history_loading), 1, this));
        mProgressDialog.setCancelable(false);
        Accounts account = (Accounts) getIntent().getSerializableExtra(ACCOUNT_TYPE);
        switch (account) {
            case CREDIT_CARD:
                getActionBar().setTitle(FontHyperTextParser.getSpannable(getString(R.string.credit_card), 0, this));
                break;
            case STORE_CARD:
                getActionBar().setTitle(FontHyperTextParser.getSpannable(getString(R.string.store_card), 0, this));
                break;
            case PERSONAL_LOAN:
                getActionBar().setTitle(FontHyperTextParser.getSpannable(getString(R.string.personal_loan), 0, this));
                break;
        }
        mProductId = getIntent().getIntExtra(PRODUCT_ID, 0);
        View button = findViewById(R.id.transaction_history_hide_action);
        ((ListView) findViewById(R.id.transaction_history_list)).setAdapter(mAdapter);
        switch (account) {
            case STORE_CARD:
                setTitle(getString(R.string.store_card));
                button.setBackgroundResource(R.drawable.store_card_selector);
                break;
            case CREDIT_CARD:
                setTitle(getString(R.string.credit_card));
                button.setBackgroundResource(R.drawable.credit_card_selector);
                break;
            case PERSONAL_LOAN:
                setTitle(getString(R.string.personal_loan));
                button.setBackgroundResource(R.drawable.personal_loan_selector);
                break;
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishActivity();
            }
        });
        getTransactions();
    }

    private void getTransactions() {
        getTransactions(true);
    }

    private void getTransactions(final boolean showProgress) {
        new HttpAsyncTask<String, String, TransactionHistoryResponse>() {

            @Override
            protected Class<TransactionHistoryResponse> httpDoInBackgroundReturnType() {
                return TransactionHistoryResponse.class;
            }

            @Override
            protected TransactionHistoryResponse httpDoInBackground(String... params) {
                return ((WoolworthsApplication) getApplication()).getApi().getAccountTransactionHistory(String.valueOf(mProductId));
            }

            @Override
            protected TransactionHistoryResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
                WiGroupLogger.e(TransactionHistoryActivity.this, TAG, errorMessage);
                TransactionHistoryResponse transactionHistoryResponse = new TransactionHistoryResponse();
                transactionHistoryResponse.httpCode = 408;
                transactionHistoryResponse.response = new Response();
                transactionHistoryResponse.response.desc = getString(R.string.err_002);
                return transactionHistoryResponse;
            }

            @Override
            protected void onPreExecute() {
                if (showProgress) {
                    mProgressDialog.show();
                }
            }

            @Override
            protected void onPostExecute(TransactionHistoryResponse transactionHistoryResponse) {
                mProgressDialog.dismiss();
                swipeRefreshLayout.setRefreshing(false);
                switch (transactionHistoryResponse.httpCode) {
                    case 200:
                        if ("0618".equals(transactionHistoryResponse.response.code) || "0618".equals(transactionHistoryResponse.response.code)) {
                            Intent intent = new Intent(TransactionHistoryActivity.this, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                            return;
                        }
                        if (transactionHistoryResponse == null || transactionHistoryResponse.transactions == null || transactionHistoryResponse.transactions.isEmpty()) {
                            ((ListView) findViewById(R.id.transaction_history_list)).setEmptyView(findViewById(R.id.transaction_empty_stub));
                        } else {
                            mTransactions = transactionHistoryResponse.transactions;
                        }
                        mAdapter.notifyDataSetChanged();
                        break;
                    case 400:
                        if ("0619".equals(transactionHistoryResponse.response.code) || "0618".equals(transactionHistoryResponse.response.code)) {
                            if (!isFinishing()) {
                                mLogin.show();
                            }
                            break;
                        }
                    default:
                        if (!isFinishing()) {
                            mError.setMessage(transactionHistoryResponse.response.desc);
                            mError.show();
                        }
                }

            }
        }.execute();
    }


    private class TransactionAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mTransactions.size();
        }

        @Override
        public Transaction getItem(int position) {
            return mTransactions.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.transaction_view, null);
            }
            Transaction item = getItem(position);
            ((TextView) convertView.findViewById(R.id.transaction_view_amount)).setText(WFormatter.formatAmount((int)item.amount));
            DateFormat m_ISO8601Local = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date date = m_ISO8601Local.parse(item.date);
                ((TextView) convertView.findViewById(R.id.transaction_view_date)).setText(new SimpleDateFormat("dd/MM/yy").format(date));
            } catch (ParseException e) {
                ((TextView) convertView.findViewById(R.id.transaction_view_date)).setText(item.date);
                WiGroupLogger.e(TransactionHistoryActivity.this, TAG, e.getMessage(), e);
            }
            ((TextView) convertView.findViewById(R.id.transaction_view_description)).setText(item.description);
            return convertView;
        }
    }

    public enum Accounts {
        STORE_CARD,
        CREDIT_CARD,
        PERSONAL_LOAN
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finishActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void finishActivity() {
        finish();
        overridePendingTransition(R.anim.slide_down_in, R.anim.slide_down_out);
    }

    @Override
    protected void onPause() {
        mProgressDialog.dismiss();
        mError.dismiss();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        finishActivity();
    }

}
