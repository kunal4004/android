package za.co.woolworths.financial.services.android.ui.activities;

import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.fragments.product.shop.CheckOutFragment;
import za.co.woolworths.financial.services.android.util.Utils;

public class CartCheckoutActivity extends AppCompatActivity implements View.OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cart_checkout);
		Utils.updateStatusBarBackground(this);
		CheckOutFragment checkOutFragment = new CheckOutFragment();
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction()
				.replace(R.id.content_frame, checkOutFragment).commit();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.btnCloseCart:
				onBackPressed();
				break;
			default:
				break;
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_down_anim, R.anim.stay);
	}
}