package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.widget.Toolbar;

import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.awfs.coordination.R;
import com.google.gson.JsonElement;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.contracts.IToastInterface;
import za.co.woolworths.financial.services.android.models.dto.CartItemGroup;
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsFragment;
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.CartFragment;
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.CheckOutFragment;
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.NavigateToShoppingList;
import za.co.woolworths.financial.services.android.ui.views.SlidingUpPanelLayout;
import za.co.woolworths.financial.services.android.ui.views.ToastFactory;
import za.co.woolworths.financial.services.android.ui.views.WMaterialShowcaseView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.QueryBadgeCounter;
import za.co.woolworths.financial.services.android.util.ScreenManager;
import za.co.woolworths.financial.services.android.util.ToastUtils;
import za.co.woolworths.financial.services.android.util.Utils;

import static za.co.woolworths.financial.services.android.ui.activities.AddToShoppingListActivity.ADD_TO_SHOPPING_LIST_REQUEST_CODE;
import static za.co.woolworths.financial.services.android.ui.activities.AddToShoppingListActivity.ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE;
import static za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow.DISMISS_POP_WINDOW_CLICKED;
import static za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.PDP_REQUEST_CODE;

public class CartActivity extends BottomActivity implements View.OnClickListener, CartFragment.ToggleRemoveItem, ToastUtils.ToastInterface, IToastInterface {

    private WTextView btnEditCart;
    private WTextView btnClearCart;
    private ImageView btnCloseCart;
    private CartFragment cartFragment;
    private ProgressBar pbRemoveAllItem;
    public static WMaterialShowcaseView walkThroughPromtView = null;
    public static final int CHECKOUT_SUCCESS = 13134;
    private FrameLayout flContentFrame;
    private boolean toastButtonWasClicked = false;
    private int localCartCount = 0;
    public static final int RESULT_PREVENT_CART_SUMMARY_CALL = 121;
    public static final String TAG = "CartActivity";

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
                .replace(R.id.content_frame, cartFragment, TAG).commit();

        localCartCount = QueryBadgeCounter.getInstance().getCartItemCount();

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
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYCARTREMOVEALL, this);
                cartFragment.removeAllCartItem(null);
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
        int currentCartCount = 0 ;
        if (cartFragment.productCountMap != null && cartFragment.productCountMap.getTotalProductCount() != null)
            currentCartCount = cartFragment.productCountMap.getTotalProductCount();
        // Check to prevent DISMISS_POP_WINDOW_CLICKED override setResult for toast clicked event
        if (!toastButtonWasClicked && localCartCount !=currentCartCount) {
            this.setResult(DISMISS_POP_WINDOW_CLICKED);
        }

        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        // Overrides activityResult to prevent cart summary api when shopping list is empty
        if (currentFragment instanceof CartFragment) {
            ArrayList<CartItemGroup> cartItem = ((CartFragment) currentFragment).getCartItems();
            if (cartItem == null || cartItem.isEmpty()) {
                // No product, hide badge counter
                QueryBadgeCounter.getInstance().setCartCount(0);
                this.setResult(RESULT_PREVENT_CART_SUMMARY_CALL);
            }
        }

        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYCARTEXIT, this);
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

    public void enableEditCart() {
        Utils.fadeInFadeOutAnimation(btnEditCart, false);
        btnEditCart.setEnabled(true);
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

        if (requestCode == PDP_REQUEST_CODE && resultCode == ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE) {
            setResult(ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE, data);
            finish();
            overridePendingTransition(0, 0);
            return;
        }

        if (requestCode == ADD_TO_SHOPPING_LIST_REQUEST_CODE && resultCode == ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE) {
            ToastFactory.Companion.buildShoppingListToast(this, flContentFrame, true, data, this);
            return;
        }

        if (requestCode == CheckOutFragment.REQUEST_CART_REFRESH_ON_DESTROY && resultCode == RESULT_OK) {
            finishActivityOnCheckoutSuccess();
            return;
        }
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.content_frame);
        Fragment bottomFragment = fm.findFragmentById(R.id.fragment_bottom_container);
        if (bottomFragment != null) {
            if (bottomFragment != null && bottomFragment instanceof ProductDetailsFragment) {
                bottomFragment.onActivityResult(requestCode, resultCode, data);
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
        toastButtonWasClicked = true;
        NavigateToShoppingList.Companion navigateTo = NavigateToShoppingList.Companion;
        if (jsonElement != null)
            navigateTo.navigateToShoppingListOnToastClicked(this, jsonElement);
    }
}
