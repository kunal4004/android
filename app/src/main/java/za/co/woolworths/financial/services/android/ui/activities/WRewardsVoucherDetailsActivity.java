package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

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
import za.co.woolworths.financial.services.android.ui.views.card_swipe.CardStackView;
import za.co.woolworths.financial.services.android.util.Utils;


public class WRewardsVoucherDetailsActivity extends AppCompatActivity implements View.OnClickListener {

	private CardStackView mSwipeStack;
	WRewardsVouchersAdapter mAdapter;
	VoucherCollection voucherCollection;
	public TextView termsAndConditions;
	int position;
	List<Voucher> vouchers;
	public static final String TAG = "VoucherDetailsActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.updateStatusBarBackground(this, R.color.reward_status_bar_color);
		setContentView(R.layout.wrewards_voucher_details);
		termsAndConditions = findViewById(R.id.termsCondition);
		mSwipeStack = findViewById(R.id.swipeStack);
		ImageButton closeVoucherImageButton = findViewById(R.id.closeVoucherImageButton);
		closeVoucherImageButton.setOnClickListener(this);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			voucherCollection = new Gson().fromJson(extras.getString("VOUCHERS"), VoucherCollection
					.class);
			position = extras.getInt("POSITION");
			vouchers = voucherCollection.vouchers;
			Collections.rotate(vouchers, -position);
			mAdapter = new WRewardsVouchersAdapter(WRewardsVoucherDetailsActivity.this, vouchers);
			mSwipeStack.setAdapter(mAdapter);
		}

		mSwipeStack.setOnClickListener(this);
		this.tagVoucherDescription(mSwipeStack.getTopIndex());
		termsAndConditions.setOnClickListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		Utils.setScreenName(this, FirebaseManagerAnalyticsProperties.ScreenNames.WREWARDS_VOUCHERS_BARCODE);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		//overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);

	}


	public void tagVoucherDescription(int position){
		Map<String, String> arguments = new HashMap<>();
		arguments.put(FirebaseManagerAnalyticsProperties.PropertyNames.VOUCHERDESCRIPTION, Utils.ellipsizeVoucherDescription(vouchers.get(position).description));
		Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.WREWARDSDESCRIPTION_VOUCHERDESCRIPTION, arguments);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.closeVoucherImageButton:
				onBackPressed();
				break;
			case  R.id.termsCondition:
				String terms = vouchers.get(mSwipeStack.getTopIndex()).termsAndConditions;
				if (TextUtils.isEmpty(terms)) {
					Utils.openLinkInInternalWebView( WoolworthsApplication.getWrewardsTCLink());
				} else {
					startActivity(new Intent(WRewardsVoucherDetailsActivity.this, WRewardsVoucherTermAndConditions.class).putExtra("TERMS", terms));
					overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
				}
				break;
			default:
				break;
		}
	}
}
