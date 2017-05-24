package za.co.woolworths.financial.services.android.ui.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.Utils;

public class WConnectionHandlerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.updateStatusBarBackground(this);
        setContentView(R.layout.no_connection_handler);
        init();
        setActionBar();
    }

    private void init() {
        WButton btnRetry = (WButton) findViewById(R.id.btnRetry);
        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (new ConnectionDetector().isOnline()) {
                    ((WoolworthsApplication) getApplication()).setTriggerErrorHandler(true);
                    finish();
                    overridePendingTransition(0, 0);
                }
            }
        });
    }

    private void setActionBar() {
        setSupportActionBar((Toolbar) findViewById(R.id.mToolbar));
        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(false);
            mActionBar.setDisplayShowTitleEnabled(false);
            mActionBar.setDisplayUseLogoEnabled(false);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
