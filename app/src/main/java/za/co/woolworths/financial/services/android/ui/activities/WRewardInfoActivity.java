package za.co.woolworths.financial.services.android.ui.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.util.FontHyperTextParser;

public class WRewardInfoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.w_reward_info);
        setTitle(FontHyperTextParser.getSpannable(getString(R.string.information), 1, this));
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
