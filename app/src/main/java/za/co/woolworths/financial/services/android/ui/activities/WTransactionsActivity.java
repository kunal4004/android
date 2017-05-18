package za.co.woolworths.financial.services.android.ui.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.TransactionHistoryResponse;
import za.co.woolworths.financial.services.android.ui.adapters.WTransactionsAdapter;
import za.co.woolworths.financial.services.android.ui.views.ErrorHandlerView;
import za.co.woolworths.financial.services.android.ui.views.ProgressDialogFragment;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WErrorDialog;

public class WTransactionsActivity extends AppCompatActivity {

    public Toolbar toolbar;
    public ExpandableListView transactionListview;
    public String productOfferingId;
    private ProgressDialogFragment mGetTransactionProgressDialog;
    private ErrorHandlerView mErrorHandlerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wtransactions_activity);
        Utils.updateStatusBarBackground(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setElevation(0);
        transactionListview = (ExpandableListView) findViewById(R.id.transactionListView);
        productOfferingId = getIntent().getStringExtra("productOfferingId");

        RelativeLayout mRelErrorHandler = (RelativeLayout) findViewById(R.id.relErrorHandler);
        WTextView mTitleError = (WTextView) findViewById(R.id.errorTitle);
        mErrorHandlerView = new ErrorHandlerView(this, mRelErrorHandler, mTitleError);
        retryApiCall();
        loadTransactionHistory(productOfferingId);
    }

    public void loadTransactionHistory(final String prOfferId) {
        final FragmentManager fm = getSupportFragmentManager();
        mGetTransactionProgressDialog = ProgressDialogFragment.newInstance();
        try {
            if (!mGetTransactionProgressDialog.isAdded()) {
                mGetTransactionProgressDialog.show(fm, "v");
            } else {
                mGetTransactionProgressDialog.dismiss();
                mGetTransactionProgressDialog = ProgressDialogFragment.newInstance();
                mGetTransactionProgressDialog.show(fm, "v");
            }

            transactionAsyncAPI(prOfferId).execute();

        } catch (NullPointerException ignored) {
        }
    }

    private HttpAsyncTask<String, String, TransactionHistoryResponse> transactionAsyncAPI(final String prOfferId) {
        return new HttpAsyncTask<String, String, TransactionHistoryResponse>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mErrorHandlerView.hideErrorHandlerLayout();
            }

            @Override
            protected TransactionHistoryResponse httpDoInBackground(String... params) {
                return ((WoolworthsApplication) getApplication()).getApi().getAccountTransactionHistory(prOfferId);
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
                switch (transactionHistoryResponse.httpCode) {
                    case 200:
                        transactionListview.setAdapter(new WTransactionsAdapter(WTransactionsActivity.this, Utils.getdata(transactionHistoryResponse.transactions)));
                        break;
                    case 440:
                        AlertDialog mError = WErrorDialog.getSimplyErrorDialog(WTransactionsActivity.this);
                        mError.setTitle("Authentication Error");
                        mError.setMessage("Your session expired. You've been signed out.");
                        mError.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                setResult(SSOActivity.SSOActivityResult.EXPIRED.rawValue());
                                finish();
                            }
                        });
                        mError.show();

                        try {
                            new SessionDao(WTransactionsActivity.this, SessionDao.KEY.USER_TOKEN).delete();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        break;
                    default:
                        try {
                            Utils.alertErrorMessage(WTransactionsActivity.this,
                                    transactionHistoryResponse.response.desc);
                        } catch (NullPointerException ex) {
                        }
                        break;
                }
                dismissProgress();
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
    }

    private void dismissProgress() {
        if (mGetTransactionProgressDialog != null && mGetTransactionProgressDialog.isVisible()) {
            mGetTransactionProgressDialog.dismiss();
        }
    }

    public void networkFailureHandler(final String errorMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                mErrorHandlerView.diplayErrorMessage(errorMessage);
            }
        });
    }

    private void retryApiCall() {
        findViewById(R.id.btnRetry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadTransactionHistory(productOfferingId);
            }
        });
    }
}
