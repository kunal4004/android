package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import com.awfs.coordination.R;
import com.google.gson.Gson;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.Voucher;
import za.co.woolworths.financial.services.android.models.dto.VoucherCollection;
import za.co.woolworths.financial.services.android.ui.adapters.WRewardsVouchersAdapter;
import za.co.woolworths.financial.services.android.ui.views.SwipeStack;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.Utils;


public class WRewardsVoucherDetailsActivity extends AppCompatActivity implements SwipeStack.SwipeStackListener {

	public Toolbar toolbar;
	private SwipeStack mSwipeStack;
	WRewardsVouchersAdapter mAdapter;
	VoucherCollection voucherCollection;
	public WTextView termsAndCondtions;
	int postion;
	List<Voucher> vouchers;
	public static final String TAG = "VoucherDetailsActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.updateStatusBarBackground(this, R.color.reward_status_bar_color);
		setContentView(R.layout.wrewards_voucher_details);
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		termsAndCondtions = (WTextView) findViewById(R.id.termsCondition);
		mSwipeStack = (SwipeStack) findViewById(R.id.swipeStack);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(null);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			voucherCollection = new Gson().fromJson(extras.getString("VOUCHERS"), VoucherCollection
					.class);
			postion = extras.getInt("POSITION");
			vouchers = voucherCollection.vouchers;
			Collections.rotate(vouchers, -postion);
			mAdapter = new WRewardsVouchersAdapter(WRewardsVoucherDetailsActivity.this, vouchers);
			mSwipeStack.setAdapter(mAdapter);
		}

		mSwipeStack.setListener(this);
		this.tagVoucherDescription(mSwipeStack.getCurrentPosition());
		termsAndCondtions.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String terms = vouchers.get(mSwipeStack.getCurrentPosition()).termsAndConditions;
				if (TextUtils.isEmpty(terms)) {
					Utils.openExternalLink(WRewardsVoucherDetailsActivity.this, WoolworthsApplication
							.getWrewardsTCLink());
				} else {
					startActivity(new Intent(WRewardsVoucherDetailsActivity.this, WRewardsVoucherTermAndConditions.class).putExtra("TERMS", terms));
					overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
				}
			}
		});

	}

	@Override
	public void onResume() {
		super.onResume();
		Utils.setScreenName(this, FirebaseManagerAnalyticsProperties.ScreenNames.WREWARDS_VOUCHERS_BARCODE);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				return true;
		}
		return false;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		//overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);

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
		this.tagVoucherDescription(position + 1);
	}

	@Override
	public void onStackEmpty() {
	}

	public void tagVoucherDescription(int position){
		Map<String, String> arguments = new HashMap<>();
		arguments.put(FirebaseManagerAnalyticsProperties.PropertyNames.VOUCHERDESCRIPTION, Utils.ellipsizeVoucherDescription(vouchers.get(position).description));
		Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.WREWARDSDESCRIPTION_VOUCHERDESCRIPTION, arguments);
	}
}
