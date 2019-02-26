package za.co.woolworths.financial.services.android.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.awfs.coordination.R;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.Nullable;

import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.contracts.IToastInterface;
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.ProductDetailFragment;
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.CartFragment;
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.CheckOutFragment;
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.NavigateToShoppingList;
import za.co.woolworths.financial.services.android.ui.views.SlidingUpPanelLayout;
import za.co.woolworths.financial.services.android.ui.views.ToastFactory;
import za.co.woolworths.financial.services.android.ui.views.WMaterialShowcaseView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ScreenManager;
import za.co.woolworths.financial.services.android.util.ToastUtils;
import za.co.woolworths.financial.services.android.util.Utils;

import static za.co.woolworths.financial.services.android.ui.activities.AddToShoppingListActivity.ADD_TO_SHOPPING_LIST_REQUEST_CODE;
import static za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow.DISMISS_POP_WINDOW_CLICKED;
import static za.co.woolworths.financial.services.android.ui.fragments.product.detail.ProductDetailFragment.RESULT_FROM_ADD_TO_CART_PRODUCT_DETAIL;
import static za.co.woolworths.financial.services.android.ui.fragments.shop.list.AddToDepartmentFragment.POST_ADD_TO_SHOPPING_LIST;

public class CartActivity extends BottomActivity implements View.OnClickListener, CartFragment.ToggleRemoveItem, ToastUtils.ToastInterface, IToastInterface {

    private WTextView btnEditCart;
    private WTextView btnClearCart;
    private ImageView btnCloseCart;
    private CartFragment cartFragment;
    private ProgressBar pbRemoveAllItem;
    public static WMaterialShowcaseView walkThroughPromtView = null;
    public static final int CHECKOUT_SUCCESS = 13134;
    private FrameLayout flContentFrame;

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

        flContentFrame = findViewById(R.id.content_frame);

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

        //One time biometricsWalkthrough
        ScreenManager.presentBiometricWalkthrough(CartActivity.this);
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
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYCARTREMOVEALL);
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
        // close expanded ProductDetails detail page
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
        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYCARTEXIT);
        finish();
        overridePendingTransition(R.anim.stay, R.anim.slide_down_anim);
    }

    public void showEditCart() {
        btnEditCart.setAlpha(1.0f);
        btnEditCart.setVisibility(View.VISIBLE);
        btnEditCart.setEnabled(true);
    }

    public void hideEditCart() {
        btnEditCart.setAlpha(0.0f);
        btnEditCart.setVisibility(View.GONE);
        btnEditCart.setEnabled(false);
    }

    public void resetToolBarIcons() {
        hideEditCart();
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
        if (requestCode == ADD_TO_SHOPPING_LIST_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                FragmentManager fm = getSupportFragmentManager();
                ToastFactory.Companion.buildShoppingListToast(flContentFrame, true, data, this);
                return;
            }
        }
        if (requestCode == CheckOutFragment.REQUEST_CART_REFRESH_ON_DESTROY && resultCode == RESULT_OK) {
            finishActivityOnCheckoutSuccess();
            return;
        }
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.content_frame);
        Fragment bottomFragment = fm.findFragmentById(R.id.fragment_bottom_container);
        if (bottomFragment != null) {
            if (bottomFragment != null && bottomFragment instanceof ProductDetailFragment) {
                bottomFragment.onActivityResult(requestCode, resultCode, data);
            }
        }
        /***
         * Result from success add to cart
         */

        if (requestCode == RESULT_FROM_ADD_TO_CART_PRODUCT_DETAIL) {
            if (resultCode == RESULT_FROM_ADD_TO_CART_PRODUCT_DETAIL) {

                if (fragment instanceof CartFragment) {
                    fragment.onActivityResult(requestCode, resultCode, null);
                }

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

    public void finishActivityOnCheckoutSuccess() {
        setResult(CHECKOUT_SUCCESS);
        finish();
        overridePendingTransition(R.anim.stay, R.anim.slide_down_anim);
    }

    @Override
    public void onToastButtonClicked(@Nullable JsonElement jsonElement) {
        NavigateToShoppingList.Companion navigateTo = NavigateToShoppingList.Companion;
        if (jsonElement instanceof JsonObject) {
            navigateTo.requestToastOnNavigateBack(this, POST_ADD_TO_SHOPPING_LIST, jsonElement.getAsJsonObject());
        } else {
            if (jsonElement != null) {
                navigateTo.requestToastOnNavigateBack(this, POST_ADD_TO_SHOPPING_LIST, jsonElement.getAsJsonArray());
            }
        }
    }
}
