package za.co.woolworths.financial.services.android.ui.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ExpandableListView;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.adapters.WTransactionsAdapter;
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
        transactionListview.setAdapter(new WTransactionsAdapter(this));
    }
}
