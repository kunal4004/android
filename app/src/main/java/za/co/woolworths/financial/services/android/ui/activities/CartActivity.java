package za.co.woolworths.financial.services.android.ui.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.fragments.product.shop.CartFragment;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.Utils;

public class CartActivity extends AppCompatActivity implements View.OnClickListener {

	private WTextView btnEditCart;
	private WTextView btnClearCart;
	private ImageView btnCloseCart;
	private CartFragment cartFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cart);
		Utils.updateStatusBarBackground(this);

		Toolbar toolbar = findViewById(R.id.toolbar);
		btnCloseCart = findViewById(R.id.btnCloseCart);
		btnCloseCart.setOnClickListener(this);

		btnEditCart = findViewById(R.id.btnEditCart);
		btnEditCart.setOnClickListener(this);

		btnClearCart = findViewById(R.id.btnClearCart);
		btnClearCart.setOnClickListener(this);

		btnEditCart = findViewById(R.id.btnEditCart);

		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		getSupportActionBar().setTitle(null);
		cartFragment = new CartFragment();
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction()
				.replace(R.id.content_frame, cartFragment).commit();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnEditCart:
				toggleCartMode();
				break;
			case R.id.btnCloseCart:
				finishActivity();
				break;
			case R.id.btnClearCart:
				cartFragment.clearAllCartItems();
				break;
		}
	}

	private void toggleCartMode() {
		boolean isEditMode = cartFragment.toggleEditMode();
		btnEditCart.setText(isEditMode ? R.string.done : R.string.edit);
		btnCloseCart.setVisibility(isEditMode ? View.GONE : View.VISIBLE);
		btnClearCart.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
	}

	@Override
	public void onBackPressed() {
		if (getFragmentManager().getBackStackEntryCount() > 0) {
			getFragmentManager().popBackStack();
		} else {
			super.onBackPressed();
		}
		overridePendingTransition(R.anim.stay, R.anim.slide_down_anim);
	}

	public void finishActivity() {
		finish();
		overridePendingTransition(R.anim.stay, R.anim.slide_down_anim);
	}

	public void showEditCart() {
		btnEditCart.setVisibility(View.VISIBLE);
	}

	public void hideEditCart() {
		btnEditCart.setVisibility(View.GONE);
	}

	public void resetToolBarIcons() {
		btnEditCart.setVisibility(View.GONE);
		btnCloseCart.setVisibility(View.VISIBLE);
		btnClearCart.setVisibility(View.GONE);
	}
}
