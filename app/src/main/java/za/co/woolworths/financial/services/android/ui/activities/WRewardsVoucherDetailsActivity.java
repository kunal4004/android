package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.Utils;

import static za.co.woolworths.financial.services.android.ui.activities.StoreLocatorActivity.toolbar;

public class WRewardsVoucherDetailsActivity extends AppCompatActivity implements SwipeStack.SwipeStackListener{

    public Toolbar toolbar;
    private SwipeStack mSwipeStack;
    WRewardsVouchersAdapter mAdapter;
    VoucherCollection voucherCollection;
    public WTextView termsAndCondtions;
    int postion;
    List<Voucher> vouchers;
    public static final String TAG ="VoucherDetailsActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.updateStatusBarBackground(this,R.color.reward_status_bar_color);
        setContentView(R.layout.wrewards_voucher_details);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        termsAndCondtions=(WTextView)findViewById(R.id.termsCondition);
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
        termsAndCondtions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String terms=vouchers.get(mSwipeStack.getCurrentPosition()).termsAndConditions;
                startActivity(new Intent(WRewardsVoucherDetailsActivity.this,WRewardsVoucherTermAndConditions.class).putExtra("TERMS",terms));
            }
        });

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
        vouchers.add(vouchers.get(position));
        mAdapter.notifyDataSetChanged();

    }

    @Override
    public void onStackEmpty() {
    }
}
