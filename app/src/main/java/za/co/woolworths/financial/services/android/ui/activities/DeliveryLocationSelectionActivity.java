package za.co.woolworths.financial.services.android.ui.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.DeliveryLocationSelectionFragment;
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.ProvinceSelectionFragment;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.binder.DeliveryLocationSelectionFragmentChange;

public class DeliveryLocationSelectionActivity extends AppCompatActivity implements View.OnClickListener, DeliveryLocationSelectionFragmentChange {

	public static final String LOAD_PROVINCE = "LOAD_PROVINCE";

	private Toolbar toolbar;
	private WTextView toolbarText;
	private View btnBack, btnClose;
	public static  final int DELIVERY_LOCATION_CLOSE_CLICKED = 1203;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_delivery_location_selection);
		Utils.updateStatusBarBackground(this);
		toolbar = findViewById(R.id.toolbar);
		toolbarText = findViewById(R.id.toolbarText);

		btnClose = findViewById(R.id.btnClose);
		btnClose.setOnClickListener(this);

		btnBack = findViewById(R.id.btnBack);
		btnBack.setOnClickListener(this);

		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		getSupportActionBar().setTitle(null);

		if (Utils.getShoppingDeliveryLocationHistory().size() > 0) {
			DeliveryLocationSelectionFragment deliveryLocationSelectionFragment = new DeliveryLocationSelectionFragment();
			FragmentManager fragmentManager = getSupportFragmentManager();
			fragmentManager.beginTransaction()
					.replace(R.id.content_frame, deliveryLocationSelectionFragment).commitAllowingStateLoss();
		} else {
			FragmentManager fragmentManager = getSupportFragmentManager();
			fragmentManager.beginTransaction()
					.replace(R.id.content_frame, new ProvinceSelectionFragment()).commitAllowingStateLoss();
		}

		Utils.showOneTimePopup(DeliveryLocationSelectionActivity.this, SessionDao.KEY.DELIVERY_LOCATION_ONE_TIME_POPUP, CustomPopUpWindow.MODAL_LAYOUT.INFO,getResources().getString(R.string.delivering_location_popup_message));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnClose:
			case R.id.btnBack:
				onBackPressed();
				break;
		}
	}

	@Override
	public void onBackPressed() {
		if (getFragmentManager().getBackStackEntryCount() > 0) {
			getFragmentManager().popBackStack();
		} else {
			setResult(DELIVERY_LOCATION_CLOSE_CLICKED);
			super.onBackPressed();
		}
		overridePendingTransition(R.anim.stay, R.anim.slide_down_anim);
	}

	@Override
	public void onFragmentChanged(String title, boolean showBackButton) {
		toolbarText.setText(title);
		btnBack.setVisibility(showBackButton ? View.VISIBLE : View.GONE);
		btnClose.setVisibility(showBackButton ? View.GONE : View.VISIBLE);
	}
}
