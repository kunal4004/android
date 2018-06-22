package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.fragments.product.shop.CartFragment;
import za.co.woolworths.financial.services.android.ui.views.SlidingUpPanelLayout;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ToastUtils;
import za.co.woolworths.financial.services.android.util.Utils;

import static za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow.DISMISS_POP_WINDOW_CLICKED;
import static za.co.woolworths.financial.services.android.ui.fragments.product.detail.ProductDetailFragment.RESULT_FROM_ADD_TO_CART_PRODUCT_DETAIL;

public class CartActivity extends BottomActivity implements View.OnClickListener, CartFragment.ToggleRemoveItem, ToastUtils.ToastInterface {

	private WTextView btnEditCart;
	private WTextView btnClearCart;
	private ImageView btnCloseCart;
	private CartFragment cartFragment;
	private ProgressBar pbRemoveAllItem;

	@Override
	protected int getLayoutResourceId() {
		return R.layout.activity_cart;
	}

	@Override
	protected void initUI() {
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
		cartFragment.deliveryLocationEnabled(!isEditMode);
	}

	@Override
	public void onBackPressed() {
		// close expanded Product detail page
		if (getSlidingLayout().getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
			closeSlideUpPanel();
			return;
		}
		if (getFragmentManager().getBackStackEntryCount() > 0) {
			getFragmentManager().popBackStack();
		} else {
			finishActivity();
		}
	}

	public void finishActivity() {
		setResult(DISMISS_POP_WINDOW_CLICKED);
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

	public void enableEditCart(boolean enable) {
		Utils.fadeInFadeOutAnimation(btnEditCart, enable);
		btnEditCart.setEnabled(enable ? false : true);
	}

	@Override
	public void onRemoveSuccess() {
		pbRemoveAllItem.setVisibility(View.GONE);
		btnCloseCart.setVisibility(View.VISIBLE);
		btnClearCart.setVisibility(View.GONE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		FragmentManager fm = getSupportFragmentManager();
		Fragment fragment = fm.findFragmentById(R.id.content_frame);

		Fragment bottomFragment = fm.findFragmentById(R.id.fragment_bottom_container);
		bottomFragment.onActivityResult(requestCode, resultCode, data);

		/***
		 * Result from success add to cart
		 */

		if (requestCode == RESULT_FROM_ADD_TO_CART_PRODUCT_DETAIL) {
			if (resultCode == RESULT_FROM_ADD_TO_CART_PRODUCT_DETAIL) {
				ToastUtils mToastUtils = new ToastUtils(this);
				mToastUtils.setActivity(this);
				mToastUtils.setGravity(Gravity.BOTTOM);
				mToastUtils.setCartText(R.string.cart);
				mToastUtils.setView((SlidingUpPanelLayout) findViewById(R.id.slideUpPanel));
				mToastUtils.setPixel(Utils.dp2px(this, 105));
				mToastUtils.setMessage(R.string.added_to);
				mToastUtils.setViewState(false);
				mToastUtils.build();
			}
		}

		//DISMISS_POP_WINDOW_CLICKED
		//Cancel button click from session expired pop-up dialog
		//will close CartActivity
		if (fragment instanceof CartFragment) {
			if (resultCode == DISMISS_POP_WINDOW_CLICKED) {
				finishActivity();
				return;
			}
			fragment.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public void onToastButtonClicked(String currentState) {

	}
}
