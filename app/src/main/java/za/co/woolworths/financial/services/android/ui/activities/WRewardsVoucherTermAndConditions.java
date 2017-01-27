package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;

public class WRewardsVoucherTermAndConditions extends AppCompatActivity {
    public Toolbar toolbar;
    public WTextView termsAndCondition;
    public WButton viewGeneralTermsAndCondtions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wrewards_voucher_term_and_conditions);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        termsAndCondition=(WTextView)findViewById(R.id.termsAndCondtions);
        viewGeneralTermsAndCondtions=(WButton)findViewById(R.id.generalTermsAndConditions);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);
        if(getIntent().hasExtra("TERMS"))
             termsAndCondition.setText(getIntent().getStringExtra("TERMS"));
        viewGeneralTermsAndCondtions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(WoolworthsApplication.getWrewardsTCLink())));
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }
}
