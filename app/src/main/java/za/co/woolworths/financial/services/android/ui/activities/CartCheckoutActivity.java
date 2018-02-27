package za.co.woolworths.financial.services.android.ui.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.fragments.product.shop.CheckOutFragment;
import za.co.woolworths.financial.services.android.util.Utils;

public class CartCheckoutActivity extends AppCompatActivity implements View.OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cart_checkout);
		Utils.updateStatusBarBackground(this);

		Toolbar toolbar = findViewById(R.id.toolbar);
		ImageView btnCloseCart = findViewById(R.id.btnCloseCart);
		btnCloseCart.setOnClickListener(this);

		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		getSupportActionBar().setTitle(null);
		CheckOutFragment checkOutFragment = new CheckOutFragment();
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction()
				.replace(R.id.content_frame, checkOutFragment).commit();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.btnCheckOut:
				onBackPressed();
				break;
			default:
				break;
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
}