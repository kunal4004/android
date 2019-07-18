package za.co.woolworths.financial.services.android.ui.activities.dashboard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;
import com.awfs.coordination.databinding.ActivityBottomNavigationBinding;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import io.reactivex.functions.Consumer;
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.contracts.IToastInterface;
import za.co.woolworths.financial.services.android.models.dto.CartSummary;
import za.co.woolworths.financial.services.android.models.dto.CartSummaryResponse;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.service.event.AuthenticationState;
import za.co.woolworths.financial.services.android.models.service.event.BadgeState;
import za.co.woolworths.financial.services.android.models.service.event.LoadState;
import za.co.woolworths.financial.services.android.ui.activities.CartActivity;
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity;
import za.co.woolworths.financial.services.android.ui.activities.TipsAndTricksViewPagerActivity;
import za.co.woolworths.financial.services.android.ui.base.BaseActivity;
import za.co.woolworths.financial.services.android.ui.base.SavedInstanceFragment;
import za.co.woolworths.financial.services.android.ui.fragments.account.MyAccountsFragment;
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsFragmentNew;
import za.co.woolworths.financial.services.android.ui.fragments.product.grid.GridFragment;
import za.co.woolworths.financial.services.android.ui.fragments.product.sub_category.SubCategoryFragment;
import za.co.woolworths.financial.services.android.ui.fragments.shop.ShopFragment;
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.NavigateToShoppingList;
import za.co.woolworths.financial.services.android.ui.fragments.store.StoresNearbyFragment1;
import za.co.woolworths.financial.services.android.ui.fragments.wreward.WRewardsLoggedOutFragment;
import za.co.woolworths.financial.services.android.ui.fragments.wreward.WRewardsLoggedinAndNotLinkedFragment;
import za.co.woolworths.financial.services.android.ui.fragments.wreward.WRewardsVouchersFragment;
import za.co.woolworths.financial.services.android.ui.fragments.wreward.base.WRewardsFragment;
import za.co.woolworths.financial.services.android.ui.fragments.wreward.logged_in.WRewardsLoggedinAndLinkedFragment;
import za.co.woolworths.financial.services.android.ui.fragments.wtoday.WTodayFragment;
import za.co.woolworths.financial.services.android.ui.views.NestedScrollableViewHelper;
import za.co.woolworths.financial.services.android.ui.views.SlidingUpPanelLayout;
import za.co.woolworths.financial.services.android.ui.views.ToastFactory;
import za.co.woolworths.financial.services.android.ui.views.WBottomNavigationView;
import za.co.woolworths.financial.services.android.ui.views.WMaterialShowcaseView;
import za.co.woolworths.financial.services.android.util.AuthenticateUtils;
import za.co.woolworths.financial.services.android.util.KeyboardUtil;
import za.co.woolworths.financial.services.android.util.MultiClickPreventer;
import za.co.woolworths.financial.services.android.util.NotificationUtils;
import za.co.woolworths.financial.services.android.util.PermissionResultCallback;
import za.co.woolworths.financial.services.android.util.PermissionUtils;
import za.co.woolworths.financial.services.android.util.QueryBadgeCounter;
import za.co.woolworths.financial.services.android.util.ScreenManager;
import za.co.woolworths.financial.services.android.util.SessionExpiredUtilities;
import za.co.woolworths.financial.services.android.util.SessionUtilities;
import za.co.woolworths.financial.services.android.util.ToastUtils;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.nav.FragNavController;
import za.co.woolworths.financial.services.android.util.nav.FragNavSwitchController;
import za.co.woolworths.financial.services.android.util.nav.FragNavTransactionOptions;
import za.co.woolworths.financial.services.android.util.nav.tabhistory.FragNavTabHistoryController;

import static za.co.woolworths.financial.services.android.models.service.event.BadgeState.CART_COUNT;
import static za.co.woolworths.financial.services.android.models.service.event.BadgeState.CART_COUNT_TEMP;
import static za.co.woolworths.financial.services.android.ui.activities.AddToShoppingListActivity.ADD_TO_SHOPPING_LIST_REQUEST_CODE;
import static za.co.woolworths.financial.services.android.ui.activities.AddToShoppingListActivity.ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE;
import static za.co.woolworths.financial.services.android.ui.activities.ConfirmColorSizeActivity.RESULT_TAP_FIND_INSTORE_BTN;
import static za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow.CART_DEFAULT_ERROR_TAPPED;
import static za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow.DISMISS_POP_WINDOW_CLICKED;
import static za.co.woolworths.financial.services.android.ui.activities.DeliveryLocationSelectionActivity.DELIVERY_LOCATION_CLOSE_CLICKED;
import static za.co.woolworths.financial.services.android.ui.activities.TipsAndTricksViewPagerActivity.RESULT_OK_ACCOUNTS;
import static za.co.woolworths.financial.services.android.ui.activities.TipsAndTricksViewPagerActivity.RESULT_OK_BARCODE_SCAN;
import static za.co.woolworths.financial.services.android.ui.fragments.shop.list.AddToShoppingListFragment.POST_ADD_TO_SHOPPING_LIST;
import static za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.listitems.ShoppingListDetailFragment.ADD_TO_CART_SUCCESS_RESULT;
import static za.co.woolworths.financial.services.android.ui.fragments.wreward.WRewardsVouchersFragment.LOCK_REQUEST_CODE_WREWARDS;
import static za.co.woolworths.financial.services.android.util.FuseLocationAPISingleton.REQUEST_CHECK_SETTINGS;
import static za.co.woolworths.financial.services.android.util.ScreenManager.CART_LAUNCH_VALUE;
import static za.co.woolworths.financial.services.android.util.ScreenManager.SHOPPING_LIST_DETAIL_ACTIVITY_REQUEST_CODE;

public class BottomNavigationActivity extends BaseActivity<ActivityBottomNavigationBinding, BottomNavigationViewModel> implements BottomNavigator, FragNavController.TransactionListener, FragNavController.RootFragmentListener, PermissionResultCallback, ToastUtils.ToastInterface, IToastInterface, Observer {

    public static final int INDEX_TODAY = FragNavController.TAB1;
    public static final int INDEX_PRODUCT = FragNavController.TAB2;
    public static final int INDEX_CART = FragNavController.TAB3;
    public static final int INDEX_REWARD = FragNavController.TAB4;
    public static final int INDEX_ACCOUNT = FragNavController.TAB5;
    public static final int REMOVE_ALL_BADGE_COUNTER = FragNavController.TAB6;

    public static final int OPEN_CART_REQUEST = 12346;
    public static final int SLIDE_UP_COLLAPSE_REQUEST_CODE = 13;
    public static final int SLIDE_UP_COLLAPSE_RESULT_CODE = 12345;
    public static final int BOTTOM_FRAGMENT_REQUEST_CODE = 3401;
    public static final int TIPS_AND_TRICKS_CTA_REQUEST_CODE = 3627;

    public final String TAG = this.getClass().getSimpleName();
    private PermissionUtils permissionUtils;
    private ArrayList<String> permissions;
    private BottomNavigationViewModel bottomNavigationViewModel;
    public FragNavController mNavController;
    private Bundle mBundle;
    private int currentSection;
    private ToastUtils mToastUtils;
    public static final int LOCK_REQUEST_CODE_ACCOUNTS = 444;
    private QueryBadgeCounter mQueryBadgeCounter;
    public static final int PDP_REQUEST_CODE = 18;
    public WMaterialShowcaseView walkThroughPromtView = null;

    @Override
    public int getLayoutId() {
        return R.layout.activity_bottom_navigation;
    }

    @Override
    public int getBindingVariable() {
        return BR.viewModel;
    }

    @Override
    public BottomNavigationViewModel getViewModel() {
        return bottomNavigationViewModel;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        SavedInstanceFragment.getInstance(getFragmentManager()).pushData((Bundle) outState.clone());
        outState.clear();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        try {
            super.onRestoreInstanceState(SavedInstanceFragment.getInstance(getFragmentManager()).popData());
        } catch (NullPointerException ex) {
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(SavedInstanceFragment.getInstance(getFragmentManager()).popData());
        mBundle = getIntent().getExtras();
        mNavController = FragNavController.newBuilder(savedInstanceState,
                getSupportFragmentManager(),
                R.id.frag_container)
                .fragmentHideStrategy(FragNavController.HIDE)
                .transactionListener(this)
                .switchController(FragNavTabHistoryController.Companion.UNLIMITED_TAB_HISTORY, new FragNavSwitchController() {
                    @Override
                    public void switchTab(int index, @Nullable FragNavTransactionOptions transactionOptions) {
                        getBottomNavigationById().setCurrentItem(index);
                    }
                })
                .eager(true)
                .rootFragmentListener(this, 5)
                .build();
        renderUI();

        /***
         * Update bottom navigation view counter
         */
        initBadgeCounter();

        observableOn(new Consumer<Object>() {
            @Override
            public void accept(Object object) {
                if (object instanceof LoadState) {
                    String searchProduct = ((LoadState) object).getSearchProduct();
                    if (!TextUtils.isEmpty((searchProduct))) {
                        GridFragment gridFragment = new GridFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("sub_category_id", "categoryId");
                        bundle.putString("sub_category_name", "categoryName");
                        bundle.putString("str_search_product", searchProduct);
                        gridFragment.setArguments(bundle);
                        pushFragment(gridFragment);
                    }
                } else if (object instanceof AuthenticationState) {
                    AuthenticationState auth = ((AuthenticationState) object);
                    if (auth.getAuthStateTypeDef() == AuthenticationState.SIGN_OUT) {
                        clearBadgeCount();
                        ScreenManager.presentSSOLogout(BottomNavigationActivity.this);
                    }
                } else if (object instanceof CartSummaryResponse) {
                    // product item successfully added to cart
                    cartSummaryAPI();
                    closeSlideUpPanel();
                    setToast(getResources().getString(R.string.added_to), getResources().getString(R.string.cart));
                } else if (object instanceof BadgeState) {
                    // call observer to update independent count
                    BadgeState badgeState = (BadgeState) object;
                    switch (badgeState.getPosition()) {
                        case CART_COUNT_TEMP:
                            addBadge(INDEX_CART, badgeState.getCount());
                            break;
                        case CART_COUNT:
                            cartSummaryAPI();
                            break;
                        default:
                            break;
                    }
                }
            }
        });

        if (SessionUtilities.getInstance().isUserAuthenticated()) {
            badgeCount();
        }
    }

    private void initBadgeCounter() {
        mQueryBadgeCounter = QueryBadgeCounter.getInstance();
        mQueryBadgeCounter.addObserver(this);
    }

    private void setToast(String message, String cartText) {
        mToastUtils = new ToastUtils(BottomNavigationActivity.this);
        mToastUtils.setActivity(BottomNavigationActivity.this);
        mToastUtils.setView(getBottomNavigationById());
        mToastUtils.setGravity(Gravity.BOTTOM);
        mToastUtils.setCurrentState(TAG);
        mToastUtils.setCartText(cartText);
        mToastUtils.setAllCapsUpperCase(false);
        mToastUtils.setPixel(getBottomNavigationById().getHeight() + Utils.dp2px(BottomNavigationActivity.this, 10));
        mToastUtils.setView(getBottomNavigationById());
        mToastUtils.setMessage(message);
        mToastUtils.setViewState(true);
        mToastUtils.build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBundle != null) {
            if (!TextUtils.isEmpty(mBundle.getString(NotificationUtils.PUSH_NOTIFICATION_INTENT))) {
                getBottomNavigationById().setCurrentItem(INDEX_ACCOUNT);
            }

            String mSessionExpiredAtTabSection = mBundle.getString("sessionExpiredAtTabSection");
            if (!TextUtils.isEmpty(mSessionExpiredAtTabSection)) {
                getBottomNavigationById().setCurrentItem(Integer.valueOf(mSessionExpiredAtTabSection));
                SessionExpiredUtilities.getInstance().showSessionExpireDialog(BottomNavigationActivity.this);
            }
        }
        mBundle = null;
    }

    @Override
    public void renderUI() {
        getToolbar();
        setActionBar();
        bottomNavigationViewModel = ViewModelProviders.of(this).get(BottomNavigationViewModel.class);
        bottomNavigationViewModel.setNavigator(this);
        bottomNavConfig();
        slideUpPanelListener();
        setUpRuntimePermission();
        getBottomNavigationById().setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        getBottomNavigationById().setOnNavigationItemReselectedListener(mOnNavigationItemReSelectedListener);
        removeToolbar();
    }

    @Override
    public void bottomNavConfig() {
        Typeface tfMyriadProT = Typeface.createFromAsset(getAssets(), "fonts/MyriadPro-Regular.otf");
        getBottomNavigationById().setTypeface(tfMyriadProT);
        getBottomNavigationById().setTextSize(12);
        // set icon size
        int iconSize = 28;
        getBottomNavigationById().setIconSize(iconSize, iconSize);
        getBottomNavigationById().enableAnimation(false);
        getBottomNavigationById().enableShiftingMode(false);
        getBottomNavigationById().enableItemShiftingMode(false);
    }

    @Override
    public WBottomNavigationView getBottomNavigationById() {
        return getViewDataBinding().wBottomNavigation;
    }

    @Override
    public void addBadge(int position, int number) {
        Utils.addBadgeAt(this, getBottomNavigationById(), position, number);
    }

    @Override
    public void statusBarColor(int color) {
        Utils.updateStatusBarBackground(this, color);
    }

    @Override
    public void statusBarColor(int color, boolean enableDecor) {
        Utils.updateStatusBarBackground(this, color, enableDecor);
    }

    @Override
    public void showBackNavigationIcon(boolean visibility) {
        setBackNavigationIcon(visibility);
    }

    @Override
    public void setTitle(String title) {
        setToolbarTitle(title);
    }

    @Override
    public void setTitle(String title, int color) {
        setToolbarTitle(title, color);
        getToolbarTitle().setTextColor(ContextCompat.getColor(BottomNavigationActivity.this, R.color.white));
        Utils.updateStatusBarBackground(BottomNavigationActivity.this, color, true);
        setHomeAsUpIndicator(R.drawable.close_white);
    }

    @Override
    public void slideUpBottomView() {
        getSlidingLayout().setAnchorPoint(1.0f);
        getSlidingLayout().setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
    }

    @Override
    public void slideUpPanelListener() {
        getSlidingLayout().setFadeOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSlidingLayout().setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });
        getSlidingLayout().addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                if (slideOffset == 0.0) {
                    getSlidingLayout().setAnchorPoint(1.0f);
                }
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState,
                                            SlidingUpPanelLayout.PanelState newState) {
                switch (newState) {
                    case COLLAPSED:
                        showStatusBar();
                        onActivityResult(SLIDE_UP_COLLAPSE_REQUEST_CODE, SLIDE_UP_COLLAPSE_RESULT_CODE, null);

                        break;

                    case EXPANDED:
                        hideStatusBar();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    @Override
    public void openProductDetailFragment(String productName, ProductList productList) {
        Gson gson = new Gson();
        String strProductList = gson.toJson(productList);
        Bundle bundle = new Bundle();
        bundle.putString("strProductList", strProductList);
        bundle.putString("strProductCategory", productName);
        ScreenManager.presentProductDetails(BottomNavigationActivity.this, bundle);
    }

    @Override
    public void scrollableViewHelper(NestedScrollView nsv) {
        getSlidingLayout().setScrollableViewHelper(new NestedScrollableViewHelper(nsv));
    }

    @Override
    public void showStatusBar() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void hideStatusBar() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void fadeOutToolbar(final int color) {
        final Toolbar mToolbar = getToolbar();
        mToolbar.animate()
                .alpha(0.0f)
                .setDuration(300)
                .translationYBy(0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        statusBarColor(R.color.white);
                        hideView(mToolbar);
                    }
                });
    }

    @Override
    public void pushFragment(Fragment fragment) {
        if (mNavController != null) {
            FragNavTransactionOptions ft = new FragNavTransactionOptions.Builder()
                    .customAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
                    .allowStateLoss(true)
                    .build();

            mNavController.pushFragment(fragment, ft);
        }
    }

    @Override
    public void pushFragment(Fragment fragment, boolean state) {
        if (mNavController != null) {
            FragNavTransactionOptions ft = new FragNavTransactionOptions.Builder()
                    .customAnimations(R.anim.stay, R.anim.stay)
                    .allowStateLoss(true)
                    .build();

            mNavController.pushFragment(fragment, ft);
        }
    }

    @Override
    public void pushFragmentSlideUp(Fragment fragment) {
        if (mNavController != null) {
            FragNavTransactionOptions ft = new FragNavTransactionOptions.Builder()
                    .customAnimations(R.anim.slide_up_anim, R.anim.stay)
                    .allowStateLoss(true)
                    .build();

            mNavController.pushFragment(fragment, ft);
        }
    }

    @Override
    public void pushFragmentSlideUp(Fragment fragment, boolean state) {
        if (mNavController != null) {
            FragNavTransactionOptions ft = new FragNavTransactionOptions.Builder()
                    .customAnimations(R.anim.slide_up_anim, R.anim.stay)
                    .allowStateLoss(true)
                    .build();

            mNavController.pushFragment(fragment, ft);
        }
    }

    @Override
    public void pushFragmentNoAnim(Fragment fragment) {
        if (mNavController != null) {
            FragNavTransactionOptions ft = new FragNavTransactionOptions.Builder()
                    .customAnimations(0, 0)
                    .allowStateLoss(true)
                    .build();

            mNavController.pushFragment(fragment, ft);
        }
    }

    @Override
    public SlidingUpPanelLayout getSlidingLayout() {
        return getViewDataBinding().slideUpPanel;
    }

    @Override
    public void closeSlideUpPanel() {
        getSlidingLayout().setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            // To avoid clicks while feature tutorial popup showing
            if (!Utils.isFeatureTutorialsDismissed(walkThroughPromtView))
                return false;

            statusBarColor(R.color.white);
            MultiClickPreventer.preventMultiClick(getViewDataBinding().wBottomNavigation);
            switch (item.getItemId()) {
                case R.id.navigation_today:
                    setCurrentSection(R.id.navigation_today);
                    setToolbarBackgroundColor(R.color.white);
                    switchTab(INDEX_TODAY);
                    hideToolbar();
                    Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.WTODAYMENU);
                    return true;

                case R.id.navigate_to_shop:
                    setCurrentSection(R.id.navigate_to_shop);
                    switchTab(INDEX_PRODUCT);
                    Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SHOPMENU);
                    return true;

                case R.id.navigate_to_cart:
                    setCurrentSection(R.id.navigate_to_cart);
                    identifyTokenValidationAPI();
                    Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYCARTMENU);
                    return false;

                case R.id.navigate_to_wreward:
                    currentSection = R.id.navigate_to_wreward;
                    setToolbarBackgroundColor(R.color.white);
                    switchTab(INDEX_REWARD);
                    Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.WREWARDSMENU);
                    return true;

                case R.id.navigate_to_account:
                    setCurrentSection(R.id.navigate_to_account);
                    if (AuthenticateUtils.getInstance(BottomNavigationActivity.this).isBiometricAuthenticationRequired()) {
                        try {
                            AuthenticateUtils.getInstance(BottomNavigationActivity.this).startAuthenticateApp(LOCK_REQUEST_CODE_ACCOUNTS);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        setToolbarBackgroundColor(R.color.white);
                        switchTab(INDEX_ACCOUNT);
                        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTSMENU);
                        return true;
                    }
            }
            return false;
        }
    };

    private BottomNavigationView.OnNavigationItemReselectedListener mOnNavigationItemReSelectedListener
            = new BottomNavigationView.OnNavigationItemReselectedListener() {
        @Override
        public void onNavigationItemReselected(@NonNull MenuItem item) {

            switch (item.getItemId()) {
                case R.id.navigation_today:
                    clearStack();
                    WTodayFragment wTodayFragment = (WTodayFragment) mNavController.getCurrentFrag();
                    wTodayFragment.scrollToTop();
                    Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.WTODAYMENU);
                    break;

                case R.id.navigate_to_shop:
                    clearStack();
                    ShopFragment shopFragment = (ShopFragment) mNavController.getCurrentFrag();
                    shopFragment.scrollToTop();
                    Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SHOPMENU);
                    break;

                case R.id.navigate_to_cart:
                    clearStack();
                    break;

                case R.id.navigate_to_wreward:
                    clearStack();
                    WRewardsFragment wRewardsFragment = (WRewardsFragment) mNavController.getCurrentFrag();
                    Fragment currentChildFragment = wRewardsFragment.getWRewardContentFrame();
                    if (currentChildFragment instanceof WRewardsLoggedinAndLinkedFragment) {
                        ((WRewardsLoggedinAndLinkedFragment) currentChildFragment).scrollToTop();
                    } else if (currentChildFragment instanceof WRewardsLoggedinAndNotLinkedFragment) {
                        ((WRewardsLoggedinAndNotLinkedFragment) currentChildFragment).scrollToTop();
                    } else {
                        ((WRewardsLoggedOutFragment) currentChildFragment).scrollToTop();
                    }
                    Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.WREWARDSMENU);
                    break;

                case R.id.navigate_to_account:
                    clearStack();
                    if (mNavController.getCurrentFrag() instanceof MyAccountsFragment) {
                        MyAccountsFragment currentAccountFragment = (MyAccountsFragment) mNavController.getCurrentFrag();
                        currentAccountFragment.scrollToTop();
                        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTSMENU);
                    }
                    break;
            }
        }
    };

    private void openCartActivity() {
        Intent openCartActivity = new Intent(this, CartActivity.class);
        startActivityForResult(openCartActivity, OPEN_CART_REQUEST);
        overridePendingTransition(R.anim.anim_accelerate_in, R.anim.stay);
    }

    @Override
    public void onBackPressed() {

        if (walkThroughPromtView != null && !walkThroughPromtView.isDismissed()) {
            walkThroughPromtView.hide();
            return;
        }

        /**
         *  Close slide up panel when expanded
         */
        if (getSlidingLayout() != null) {
            // Send result to store locator fragment onActivityResult
            // if current visible fragment points to store locator
            // and store locator detail is anchored

            if (mNavController.getCurrentFrag() instanceof StoresNearbyFragment1) {
                Fragment currentFrag = mNavController.getCurrentFrag();
                StoresNearbyFragment1 storesNearbyFragment = (StoresNearbyFragment1) currentFrag;
                if (storesNearbyFragment.layoutIsAnchored()) {
                    currentFrag.onActivityResult(StoresNearbyFragment1.LAYOUT_ANCHORED_RESULT_CODE, StoresNearbyFragment1.LAYOUT_ANCHORED_RESULT_CODE, null);
                    return;
                }
            }

            if (getSlidingLayout().getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
                closeSlideUpPanel();
                return;
            }
        }
        if (mNavController.getCurrentFrag() instanceof SubCategoryFragment) {
            popFragmentSlideDown();
            return;
        }

        /**
         *  Slide to previous fragment with custom left to right animation
         *  Close activity if fragment is at root level
         */
        if (!mNavController.isRootFragment()) {
            mNavController.popFragment(new FragNavTransactionOptions.Builder().customAnimations(R.anim.slide_in_from_left, R.anim.slide_out_to_right).build());
        } else {
            super.onBackPressed();
        }
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
    public void onTabTransaction(Fragment fragment, int index) {
        if (index == 2) {
            return;
        }
        // If we have a backstack, show the back button
        if (getSupportActionBar() != null && mNavController != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(!mNavController.isRootFragment());
        }
    }

    @Override
    public void onFragmentTransaction(Fragment fragment, @NonNull FragNavController.TransactionType transactionType) {
        // If we have a backstack, show the back button
        if (getSupportActionBar() != null && mNavController != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(!mNavController.isRootFragment());
            KeyboardUtil.hideSoftKeyboard(BottomNavigationActivity.this);
        }
    }

    @NonNull
    @Override
    public Fragment getRootFragment(int index) {
        switch (index) {
            case INDEX_TODAY:
                return new WTodayFragment();
            case INDEX_PRODUCT:
                return new ShopFragment();
            case INDEX_CART:
                return new ShopFragment();
            case INDEX_REWARD:
                WRewardsFragment wRewardsFragment = new WRewardsFragment();
                return wRewardsFragment;
            case INDEX_ACCOUNT:
                MyAccountsFragment myAccountsFragment = new MyAccountsFragment();
                return myAccountsFragment;
        }
        throw new IllegalStateException("Need to send an index that we know");
    }

    @Override
    public void hideBottomNavigationMenu() {
        hideView(getBottomNavigationById());
        hideView(getViewDataBinding().bottomLine);
    }

    @Override
    public void showBottomNavigationMenu() {
        showView(getBottomNavigationById());
        showView(getViewDataBinding().bottomLine);
    }

    @Override
    public void displayToolbar() {
        showToolbar();
    }

    @Override
    public void removeToolbar() {
        hideToolbar();
    }

    @Override
    public void setUpRuntimePermission() {
        permissionUtils = new PermissionUtils(this, this);
        permissions = new ArrayList<>();
    }

    @Override
    public PermissionUtils getRuntimePermission() {
        return permissionUtils;
    }

    @Override
    public ArrayList<String> getPermissionType(String type) {
        if (!permissions.isEmpty())
            permissions.clear();
        permissions.add(type);
        return permissions;
    }

    @Override
    public void popFragment() {
        onBackPressed();
    }

    @Override
    public void popFragmentNoAnim() {
        if (!mNavController.isRootFragment()) {
            mNavController.popFragment(new FragNavTransactionOptions.Builder().customAnimations(R.anim.stay_short_anim, R.anim.stay_short_anim).build());
        }
    }

    @Override
    public void popFragmentSlideDown() {
        if (!mNavController.isRootFragment()) {
            mNavController.popFragment(new FragNavTransactionOptions.Builder().customAnimations(R.anim.stay, R.anim.slide_down_anim).build());
        }
    }

    @Override
    public void setSelectedIconPosition(int position) {

    }

    @Override
    public void switchTab(int number) {
        mNavController.switchTab(number);
        SessionUtilities.getInstance().setBottomNavigationPosition(String.valueOf(number));
    }

    @Override
    public void clearStack() {
        if (mNavController != null)
            mNavController.clearStack(new FragNavTransactionOptions.Builder().customAnimations(R.anim.slide_in_from_left, R.anim.slide_out_to_right).build());
    }

    @Override
    public void cartSummaryAPI() {
    }

    @Override
    public void PermissionGranted(int request_code) {
        //TODO:: Parse result_code and use only onActivityResult line
        onActivityResult(request_code, 200, null);
        switch (request_code) {
            case 2:
                onActivityResult(request_code, 200, null);
                break;

            default:
                break;
        }
        ;
    }

    @Override
    public void PartialPermissionGranted(int request_code, ArrayList<String> granted_permissions) {

    }

    @Override
    public void PermissionDenied(int request_code) {

    }

    @Override
    public void NeverAskAgain(int request_code) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        Fragment fragment = getCurrentFragment();
        if (fragment instanceof StoresNearbyFragment1) {
            fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }else if(fragment instanceof ShopFragment){
            fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

        // redirects to utils
        permissionUtils.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //TODO: Explain where this is coming from.

        // Navigate from shopping list detail activity
        switch (requestCode) {
            case ADD_TO_SHOPPING_LIST_REQUEST_CODE:  // Call back when Toast clicked after adding item to shopping list
            case SHOPPING_LIST_DETAIL_ACTIVITY_REQUEST_CODE:
                navigateToMyList(requestCode, resultCode, data);

                switch (resultCode){
                    case ADD_TO_SHOPPING_LIST_REQUEST_CODE:
                        // refresh my list view
                        getBottomNavigationById().setCurrentItem(INDEX_PRODUCT);
                        clearStack();
                        Fragment fragment = mNavController.getCurrentFrag();
                        if (fragment instanceof ShopFragment) {
                            ShopFragment shopFragment = (ShopFragment) fragment;
                            shopFragment.navigateToMyListFragment();
                            shopFragment.refreshViewPagerFragment(true);
                        }
                        break;

                    case RESULT_OK:
                        // Open Shopping List Detail Fragment From MyList and Add item to cart
                        String itemAddToCartMessage = data.getStringExtra("addedToCartMessage");
                        if (itemAddToCartMessage != null) {
                            setToast(itemAddToCartMessage, "");
                        }
                        break;
                }
                break;

            case REQUEST_CHECK_SETTINGS:
                getCurrentFragment().onActivityResult(requestCode, resultCode, data);
                break;

            default:
                break;
        }

        //Open shopping from Tips and trick activity requestcode
        if (requestCode == TIPS_AND_TRICKS_CTA_REQUEST_CODE && (resultCode == RESULT_OK_ACCOUNTS || resultCode == RESULT_OK_BARCODE_SCAN)) {
            getBottomNavigationById().setCurrentItem(INDEX_PRODUCT);
            clearStack();
            Fragment fragment = mNavController.getCurrentFrag();
            switch (resultCode) {
                case RESULT_OK_ACCOUNTS:
                    if (fragment instanceof ShopFragment) {
                        ShopFragment shopFragment = (ShopFragment) fragment;
                        shopFragment.navigateToMyListFragment();
                        shopFragment.refreshViewPagerFragment(false);
                        return;
                    }
                    break;

                case RESULT_OK_BARCODE_SCAN:
                    if (fragment instanceof ShopFragment) {
                        ShopFragment shopFragment = (ShopFragment) fragment;
                        shopFragment.openBarcodeScanner();
                        return;
                    }
                    break;
                default:
                    break;
            }
        }

        if (requestCode == PDP_REQUEST_CODE) {
            navigateToMyList(requestCode, resultCode, data);
            if (resultCode == RESULT_OK) {
                String itemAddToCartMessage = data.getStringExtra("addedToCartMessage");
                if (itemAddToCartMessage != null) {
                    setToast(itemAddToCartMessage, "");
                }
                return;
            }
        }

        // navigate to product section
        if (requestCode == OPEN_CART_REQUEST) {
            navigateToMyList(requestCode, resultCode, data);
            //Handling error 500 from cart
            if (resultCode == CART_DEFAULT_ERROR_TAPPED) {
                Fragment fragmentById = getCurrentFragment();
                if (fragmentById != null)
                    fragmentById.onActivityResult(requestCode, resultCode, null);
                return;
            }
            switch (resultCode) {
                case RESULT_OK:
                    navigateToDepartmentFragment();
                    break;
                case DISMISS_POP_WINDOW_CLICKED:
                    //ensure counter is refresh when user cart activity is closed
                    QueryBadgeCounter.getInstance().queryCartSummaryCount();
                    break;
                case 0:
                    switch (getCurrentSection()) {
                        case R.id.navigate_to_cart:
                            Intent openCartActivity = new Intent(this, CartActivity.class);
                            startActivityForResult(openCartActivity, OPEN_CART_REQUEST);
                            overridePendingTransition(0, 0);
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
        // prevent firing reward and account api on every activity resume
        if (resultCode == SSOActivity.SSOActivityResult.SUCCESS.rawValue()) {
            //load count on login success
            switch (getCurrentSection()) {
                case R.id.navigate_to_cart:
                    //open cart activity after login from cart only
                    if (requestCode == CART_LAUNCH_VALUE) {
                        Intent openCartActivity = new Intent(this, CartActivity.class);
                        startActivityForResult(openCartActivity, OPEN_CART_REQUEST);
                        overridePendingTransition(0, 0);
                        return;
                    }
                    break;
                default:
                    Fragment fragmentById = getBottomFragmentById();
                    if (fragmentById == null) break;
                    if (fragmentById instanceof ProductDetailsFragmentNew)
                        fragmentById.onActivityResult(requestCode, resultCode, data);
                    break;
            }
        }

        Fragment fragment = mNavController.getCurrentFrag();
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }

        //Call product detail onActivityResult
        if (resultCode == RESULT_TAP_FIND_INSTORE_BTN) {
            if (getBottomFragmentById() instanceof ProductDetailsFragmentNew) {
                getBottomFragmentById().onActivityResult(requestCode, resultCode, null);
            }

        }
        // Biometric Authentication check
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case LOCK_REQUEST_CODE_ACCOUNTS:
                    AuthenticateUtils.getInstance(BottomNavigationActivity.this).enableBiometricForCurrentSession(false);
                    getBottomNavigationById().setCurrentItem(INDEX_ACCOUNT);
                    break;
                case LOCK_REQUEST_CODE_WREWARDS:
                    Utils.sendBus(new WRewardsVouchersFragment());
                    break;
                default:
                    break;
            }
        }

        if (requestCode == ADD_TO_CART_SUCCESS_RESULT) {
            if (resultCode == ADD_TO_CART_SUCCESS_RESULT) {
                String itemAddToCartMessage = data.getStringExtra("addedToCartMessage");
                if (itemAddToCartMessage != null) {
                    setToast(itemAddToCartMessage, "");
                }
            }
        }

        if (requestCode == BOTTOM_FRAGMENT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (getBottomFragmentById() instanceof ProductDetailsFragmentNew) {
                    getBottomFragmentById().onActivityResult(requestCode, resultCode, data);
                }
            }
        }

        if (requestCode == TIPS_AND_TRICKS_CTA_REQUEST_CODE) {
            switch (resultCode) {
                case TipsAndTricksViewPagerActivity.RESULT_OK_PRODUCTS:
                    navigateToDepartmentFragment();
                    break;
                case RESULT_OK_ACCOUNTS:
                    getBottomNavigationById().setCurrentItem(INDEX_ACCOUNT);
                    break;
                case TipsAndTricksViewPagerActivity.RESULT_OK_REWARDS:
                    getBottomNavigationById().setCurrentItem(INDEX_REWARD);
                    break;
            }
        }

    }

    private void navigateToMyList(int requestCode, int resultCode, Intent data) {
        if (resultCode == ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE) {
            clearStack();
            String obj = data.getStringExtra(POST_ADD_TO_SHOPPING_LIST);
            JsonElement element = new JsonParser().parse(obj);
            Fragment fragmentById = getCurrentFragment();
            if (fragmentById instanceof ShopFragment)
                fragmentById.onActivityResult(requestCode, resultCode, null);
            switchToShoppingListTab(element);
        } else if (resultCode == NavigateToShoppingList.DISPLAY_TOAST_RESULT_CODE) {
            clearStack();
            ToastFactory toastFactory = new ToastFactory();
            toastFactory.Companion.buildShoppingListToast(this, getBottomNavigationById(), true, data, this);
            Fragment fragmentById = getCurrentFragment();
            if (fragmentById != null)
                fragmentById.onActivityResult(requestCode, resultCode, null);
        }
    }

    private Fragment getBottomFragmentById() {
        FragmentManager fm = getSupportFragmentManager();
        return fm.findFragmentById(R.id.fragment_bottom_container);
    }

    private void removeBottomFragment() {
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .remove(fm.findFragmentById(R.id.fragment_bottom_container))
                .commitAllowingStateLoss();
    }

    public void setCurrentSection(int currentSection) {
        this.currentSection = currentSection;
    }

    public int getCurrentSection() {
        return currentSection;
    }

    @Override
    public void identifyTokenValidationAPI() {
        if (!SessionUtilities.getInstance().isUserAuthenticated()) {
            getGlobalState().setDetermineLocationPopUpEnabled(true);
            ScreenManager.presentCartSSOSignin(BottomNavigationActivity.this);
        } else {
            openCartActivity();
        }
    }

    @Override
    public void cartSummaryInvalidToken() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                addBadge(INDEX_CART, 0);
            }
        });
    }

    @Override
    public int getCurrentStackIndex() {
        return mNavController.getCurrentStackIndex();
    }

    @Override
    public void updateCartSummaryCount(CartSummary cartSummary) {
        if (cartSummary == null) return;
        addBadge(INDEX_CART, cartSummary.totalItemsCount);
    }

    @Override
    public void updateVoucherCount(int count) {
        addBadge(INDEX_REWARD, count);
    }

    @Override
    public void updateMessageCount(int unreadCount) {
        addBadge(INDEX_ACCOUNT, unreadCount);
    }

    public void clearBadgeCount() {
        addBadge(INDEX_CART, 0);
        addBadge(INDEX_ACCOUNT, 0);
        addBadge(INDEX_REWARD, 0);
    }

    @Override
    public Toolbar toolbar() {
        return getToolbar();
    }

    @Override
    public void closeSlideUpPanelFromList(int count) {
        closeSlideUpPanel();
    }

    @Override
    public void setHomeAsUpIndicator(int drawable) {
        getSupportActionBar().setHomeAsUpIndicator(drawable);
    }

    @Override
    public void onToastButtonClicked(String currentState) {
        if (mToastUtils != null) {
            String state = mToastUtils.getCurrentState();
            if (currentState.equalsIgnoreCase(state)) {
                // do anything when popupWindow was clicked
                if (!SessionUtilities.getInstance().isUserAuthenticated()) {
                    ScreenManager.presentSSOSignin(BottomNavigationActivity.this);
                } else {
                    Intent openCartActivity = new Intent(BottomNavigationActivity.this, CartActivity.class);
                    startActivityForResult(openCartActivity, OPEN_CART_REQUEST);
                    overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
                }
            }
        }
    }

    @Override
    public void onToastButtonClicked(@Nullable JsonElement jsonElement) {
        switchToShoppingListTab(jsonElement);
    }

    public Fragment getCurrentFragment() {
        return mNavController.getCurrentFrag();
    }

    @Override
    public void badgeCount() {
        switch (getCurrentSection()) {
            case R.id.navigate_to_account:
                mQueryBadgeCounter.queryCartSummaryCount();
                mQueryBadgeCounter.queryVoucherCount();
                break;

            case R.id.navigate_to_shop:
                /***
                 * Trigger cart count when delivery location address was set
                 * if delivery location is empty or null, cart summary call will occur
                 * in ProductDetailActivity.
                 * It ensure only one cart count call is made on sign in
                 */
                if (Utils.getPreferredDeliveryLocation() != null)
                    mQueryBadgeCounter.queryCartSummaryCount();
                mQueryBadgeCounter.queryMessageCount();
                mQueryBadgeCounter.queryVoucherCount();
                break;
            case R.id.navigate_to_wreward:
                mQueryBadgeCounter.queryCartSummaryCount();
                mQueryBadgeCounter.queryMessageCount();
                break;
            case R.id.navigate_to_cart:
                mQueryBadgeCounter.queryMessageCount();
                mQueryBadgeCounter.queryVoucherCount();
                break;
            default:
                mQueryBadgeCounter.queryAllBadgeCounters();
                break;
        }
    }

    @Override
    public void update(Observable observable, Object o) {
        if (observable instanceof QueryBadgeCounter) {
            QueryBadgeCounter queryBadgeCounter = (QueryBadgeCounter) observable;
            int bottomNavigationIndex = queryBadgeCounter.getUpdateAtPosition();
            switch (mQueryBadgeCounter.getUpdateAtPosition()) {

                case INDEX_ACCOUNT:
                    addBadge(bottomNavigationIndex, queryBadgeCounter.getMessageCount());
                    break;

                case INDEX_CART:
                    addBadge(bottomNavigationIndex, queryBadgeCounter.getCartCount());
                    break;

                case INDEX_REWARD:
                    addBadge(bottomNavigationIndex, queryBadgeCounter.getVoucherCount());
                    break;

                case R.id.navigate_to_shop:
                    setCurrentSection(R.id.navigate_to_shop);
                    badgeCount();
                    break;

                case REMOVE_ALL_BADGE_COUNTER:
                    clearBadgeCount();
                    break;

                default:
                    badgeCount();
                    break;
            }
        }
    }


    private void switchToShoppingListTab(JsonElement element) {
        if (element instanceof JsonObject) {
            JsonObject list = (JsonObject) element;
            if (list.size() == 1) {
                Set<Map.Entry<String, JsonElement>> entries = list.entrySet();//will return members of your object
                String listId = null;
                JsonElement shoppingList = null;
                for (Map.Entry<String, JsonElement> entry : entries) {
                    listId = entry.getKey();
                    shoppingList = entry.getValue();
                }
                if (shoppingList != null)
                    ScreenManager.presentShoppingListDetailActivity(this, listId, shoppingList.getAsJsonObject().get("name").getAsString());
            } else {
                getBottomNavigationById().setCurrentItem(INDEX_PRODUCT);
                if (mNavController.getCurrentFrag() instanceof ShopFragment) {
                    ShopFragment shopFragment = (ShopFragment) mNavController.getCurrentFrag();
                    shopFragment.navigateToMyListFragment();
                    shopFragment.refreshViewPagerFragment(true);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mQueryBadgeCounter == null) return;
        mQueryBadgeCounter.deleteObserver(this);
        mQueryBadgeCounter.cancelCounterRequest();
    }

    private void navigateToDepartmentFragment(){
        getBottomNavigationById().setCurrentItem(INDEX_PRODUCT);
        clearStack();
        Fragment currentFragment = mNavController.getCurrentFrag();
        if (currentFragment instanceof ShopFragment) {
            ShopFragment shopFragment = (ShopFragment) currentFragment;
            shopFragment.onStartShopping();
            return;
        }
    }
}