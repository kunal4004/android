package za.co.woolworths.financial.services.android.ui.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.dto.ProductDetail;
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.ProductDetailFragment;
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.CartFragment;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.Utils;

public class CartActivity extends AppCompatActivity implements View.OnClickListener, CartFragment.ToggleRemoveItem {

	private WTextView btnEditCart;
	private WTextView btnClearCart;
	private ImageView btnCloseCart;
	private CartFragment cartFragment;
	private ProgressBar pbRemoveAllItem;

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

		pbRemoveAllItem = findViewById(R.id.pbRemoveAllItem);

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
				// prevent remove all item progressbar visible
				dismissProgress();
				break;
			case R.id.btnCloseCart:
				finishActivity();
				break;
			case R.id.btnClearCart:
				cartFragment.removeAllCartItem(null).execute();
				break;
		}
	}

	private void dismissProgress() {
		pbRemoveAllItem.setVisibility(View.GONE);
	}

	public void toggleCartMode() {
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
			finishActivity();
		}
	}

	public void finishActivity() {
		setResult(DEFAULT_KEYS_SEARCH_GLOBAL);
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

	@Override
	public void onRemoveItem(boolean visibility) {
		pbRemoveAllItem.setVisibility(visibility ? View.VISIBLE : View.GONE);
		btnClearCart.setVisibility(visibility ? View.GONE : View.VISIBLE);
		btnCloseCart.setVisibility(visibility ? View.GONE : View.GONE);
		btnEditCart.setEnabled(visibility ? false : true);
	}

	@Override
	public void onRemoveSuccess() {
		pbRemoveAllItem.setVisibility(View.GONE);
		btnCloseCart.setVisibility(View.VISIBLE);
		btnClearCart.setVisibility(View.GONE);
	}
}
