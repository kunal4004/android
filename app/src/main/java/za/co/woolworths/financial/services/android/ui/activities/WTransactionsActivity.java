package za.co.woolworths.financial.services.android.ui.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ExpandableListView;

import com.awfs.coordination.R;

import java.util.ArrayList;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.MessageResponse;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.TransactionHistoryResponse;
import za.co.woolworths.financial.services.android.ui.adapters.WTransactionsAdapter;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.Utils;

public class WTransactionsActivity extends AppCompatActivity {

    public Toolbar toolbar;
    public ExpandableListView transactionListview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wtransactions_activity);
        Utils.updateStatusBarBackground(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);
        transactionListview=(ExpandableListView)findViewById(R.id.transactionListView);

        loadTransactionHistory();
    }

    public void loadTransactionHistory() {
        new HttpAsyncTask<String, String, TransactionHistoryResponse>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected TransactionHistoryResponse httpDoInBackground(String... params) {

                return ((WoolworthsApplication) getApplication()).getApi().getAccountTransactionHistory("20");
            }

            @Override
            protected Class<TransactionHistoryResponse> httpDoInBackgroundReturnType() {
                return TransactionHistoryResponse.class;
            }

            @Override
            protected TransactionHistoryResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
                TransactionHistoryResponse transactionHistoryResponse = new TransactionHistoryResponse();
                transactionHistoryResponse.response = new Response();
                return transactionHistoryResponse;
            }

            @Override
            protected void onPostExecute(TransactionHistoryResponse transactionHistoryResponse) {
                super.onPostExecute(transactionHistoryResponse);
                if(transactionHistoryResponse.transactions!=null)
                  transactionListview.setAdapter(new WTransactionsAdapter(WTransactionsActivity.this,Utils.getdata(transactionHistoryResponse.transactions)));
            }
        }.execute();
    }

}
