package za.co.woolworths.financial.services.android.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.FontHyperTextParser;

public class VoucherTermsAndConditionsActivity extends Activity {
    public static final String TNC = "TNC";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.voucher_terms_and_conditions_activity);
        findViewById(R.id.voucher_terms).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i =new Intent(VoucherTermsAndConditionsActivity.this, WebViewActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("title","WREWARDS T&Cs");
                bundle.putString("link", WoolworthsApplication.getWrewardsTCLink());
                i.putExtra("Bundle",bundle);
                startActivity(i);

            }
        });
        ((WTextView) findViewById(R.id.voucher_terms_text)).setText(getIntent().getStringExtra(TNC));
        setTitle(FontHyperTextParser.getSpannable("VOUCHER TERMS & CONDITIONS", 1, this));
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
