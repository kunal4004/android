package za.co.woolworths.financial.services.android.util.barcode.scanner;

import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.BaseActivity;

public class BaseScannerActivity extends BaseActivity {

    private WTextView mTextToolbar;

    public void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mTextToolbar = (WTextView) findViewById(R.id.toolbarText);
        mTextToolbar.setText(getString(R.string.scan_product));
        mTextToolbar.setGravity(Gravity.LEFT);
        mTextToolbar.setTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        if(ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowTitleEnabled(false);
            ab.setDisplayUseLogoEnabled(false);
            ab.setDisplayShowTitleEnabled(false);
            ab.setDefaultDisplayHomeAsUpEnabled(false);
            ab.setHomeAsUpIndicator(R.drawable.close_white);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
