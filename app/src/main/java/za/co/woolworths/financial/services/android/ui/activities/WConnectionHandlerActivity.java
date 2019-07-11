package za.co.woolworths.financial.services.android.ui.activities;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.util.NetworkManager;
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
                if (NetworkManager.getInstance().isConnectedToNetwork(WConnectionHandlerActivity.this)) {
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
