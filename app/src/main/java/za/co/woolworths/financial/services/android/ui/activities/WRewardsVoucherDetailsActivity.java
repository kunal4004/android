package za.co.woolworths.financial.services.android.ui.activities;

import android.graphics.Color;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.awfs.coordination.R;
import com.google.gson.Gson;

import java.util.Collections;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.Voucher;
import za.co.woolworths.financial.services.android.models.dto.VoucherCollection;
import za.co.woolworths.financial.services.android.models.dto.VoucherResponse;
import za.co.woolworths.financial.services.android.ui.adapters.WRewardsVouchersAdapter;
import za.co.woolworths.financial.services.android.ui.views.SwipeStack;

import static za.co.woolworths.financial.services.android.ui.activities.StoreLocatorActivity.toolbar;

public class WRewardsVoucherDetailsActivity extends AppCompatActivity implements SwipeStack.SwipeStackListener{

    public Toolbar toolbar;
    private SwipeStack mSwipeStack;
    WRewardsVouchersAdapter mAdapter;
    VoucherCollection voucherCollection;
    int postion;
    List<Voucher> vouchers;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Color.parseColor("#cc000000"));
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        setContentView(R.layout.wrewards_voucher_details);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mSwipeStack = (SwipeStack) findViewById(R.id.swipeStack);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);
        voucherCollection=new Gson().fromJson(getIntent().getStringExtra("VOUCHERS"),VoucherCollection.class);
        postion=getIntent().getIntExtra("POSITION",0);
        vouchers=voucherCollection.vouchers;
        Collections.rotate(vouchers, -postion);
        mAdapter = new WRewardsVouchersAdapter(WRewardsVoucherDetailsActivity.this,vouchers);
        mSwipeStack.setAdapter(mAdapter);
        mSwipeStack.setListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return  true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);

    }

    @Override
    public void onViewSwipedToLeft(int position) {
    }

    @Override
    public void onViewSwipedToRight(int position) {
    }

    @Override
    public void onViewSwipedToTop(int position) {
    }

    @Override
    public void onViewSwipedToBottom(int position) {
    }

    @Override
    public void onStackEmpty() {
    }
}
