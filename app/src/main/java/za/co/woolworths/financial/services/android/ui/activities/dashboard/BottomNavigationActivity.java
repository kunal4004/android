package za.co.woolworths.financial.services.android.ui.activities.dashboard;

import static za.co.woolworths.financial.services.android.cart.view.CartFragment.REQUEST_PAYMENT_STATUS;
import static za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow.DISMISS_POP_WINDOW_CLICKED;
import static za.co.woolworths.financial.services.android.ui.activities.SSOActivity.FORGOT_PASSWORD;
import static za.co.woolworths.financial.services.android.ui.activities.SSOActivity.FORGOT_PASSWORD_VALUE;
import static za.co.woolworths.financial.services.android.ui.activities.TipsAndTricksViewPagerActivity.OPEN_SHOPPING_LIST_TAB_FROM_TIPS_AND_TRICK_RESULT_CODE;
import static za.co.woolworths.financial.services.android.ui.activities.TipsAndTricksViewPagerActivity.RESULT_OK_ACCOUNTS;
import static za.co.woolworths.financial.services.android.ui.activities.TipsAndTricksViewPagerActivity.RESULT_OK_OPEN_CART_FROM_TIPS_AND_TRICKS;
import static za.co.woolworths.financial.services.android.ui.activities.account.MyAccountActivity.RESULT_CODE_MY_ACCOUNT_FRAGMENT;
import static za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsFragment.BRAND_NAVIGATION_DETAILS;
import static za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsFragment.IS_BROWSING;
import static za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsFragment.STR_BRAND_HEADER;
import static za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsFragment.STR_PRODUCT_CATEGORY;
import static za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsFragment.STR_PRODUCT_LIST;
import static za.co.woolworths.financial.services.android.ui.fragments.product.shop.CheckOutFragment.REQUEST_CHECKOUT_ON_CONTINUE_SHOPPING;
import static za.co.woolworths.financial.services.android.ui.fragments.product.shop.CheckOutFragment.RESULT_RELOAD_CART;
import static za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.listitems.ShoppingListDetailFragment.ADD_TO_CART_SUCCESS_RESULT;
import static za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.search.SearchResultFragment.PRODUCT_DETAILS_FROM_MY_LIST_SEARCH;
import static za.co.woolworths.financial.services.android.ui.fragments.wreward.WRewardsVouchersFragment.LOCK_REQUEST_CODE_WREWARDS;
import static za.co.woolworths.financial.services.android.util.AppConstant.DP_LINKING_MY_ACCOUNTS_ORDER_DETAILS;
import static za.co.woolworths.financial.services.android.util.AppConstant.REQUEST_CODE_BARCODE_ACTIVITY;
import static za.co.woolworths.financial.services.android.util.AppConstant.REQUEST_CODE_ORDER_DETAILS_PAGE;
import static za.co.woolworths.financial.services.android.util.FuseLocationAPISingleton.REQUEST_CHECK_SETTINGS;
import static za.co.woolworths.financial.services.android.util.ScreenManager.CART_LAUNCH_VALUE;
import static za.co.woolworths.financial.services.android.util.ScreenManager.SHOPPING_LIST_DETAIL_ACTIVITY_REQUEST_CODE;
import static za.co.woolworths.financial.services.android.util.nav.tabhistory.FragNavTabHistoryController.UNLIMITED_TAB_HISTORY;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;
import com.awfs.coordination.databinding.ActivityBottomNavigationBinding;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.functions.Consumer;
import za.co.woolworths.financial.services.android.cart.view.CartFragment;
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.contracts.IToastInterface;
import za.co.woolworths.financial.services.android.models.AppConfigSingleton;
import za.co.woolworths.financial.services.android.models.BrandNavigationDetails;
import za.co.woolworths.financial.services.android.models.dto.CartSummary;
import za.co.woolworths.financial.services.android.models.dto.CartSummaryResponse;
import za.co.woolworths.financial.services.android.models.dto.ProductDetails;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.ProductSearchTypeAndTerm;
import za.co.woolworths.financial.services.android.models.dto.ProductView;
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams;
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SessionStateType;
import za.co.woolworths.financial.services.android.models.dto.item_limits.ProductCountMap;
import za.co.woolworths.financial.services.android.models.network.Parameter;
import za.co.woolworths.financial.services.android.models.service.event.BadgeState;
import za.co.woolworths.financial.services.android.models.service.event.LoadState;
import za.co.woolworths.financial.services.android.onecartgetstream.OCChatActivity;
import za.co.woolworths.financial.services.android.shoppinglist.view.MyShoppingListFragment;
import za.co.woolworths.financial.services.android.ui.activities.ConfirmColorSizeActivity;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity;
import za.co.woolworths.financial.services.android.ui.activities.TipsAndTricksViewPagerActivity;
import za.co.woolworths.financial.services.android.ui.base.BaseActivity;
import za.co.woolworths.financial.services.android.ui.base.SavedInstanceFragment;
import za.co.woolworths.financial.services.android.ui.fragments.RefinementDrawerFragment;
import za.co.woolworths.financial.services.android.ui.fragments.account.AccountMasterCache;
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatAWSAmplify;
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.helper.AmplifyInit;
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsFragment;
import za.co.woolworths.financial.services.android.ui.fragments.product.grid.ProductListingFragment;
import za.co.woolworths.financial.services.android.ui.fragments.product.sub_category.SubCategoryFragment;
import za.co.woolworths.financial.services.android.ui.fragments.shop.OrderDetailsFragment;
import za.co.woolworths.financial.services.android.ui.fragments.shop.ShopFragment;
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.NavigateToShoppingList;
import za.co.woolworths.financial.services.android.ui.fragments.store.StoresNearbyFragment1;
import za.co.woolworths.financial.services.android.ui.fragments.wreward.WRewardsFragment;
import za.co.woolworths.financial.services.android.ui.fragments.wreward.WRewardsLoggedInAndNotLinkedFragment;
import za.co.woolworths.financial.services.android.ui.fragments.wreward.WRewardsLoggedOutFragment;
import za.co.woolworths.financial.services.android.ui.fragments.wreward.WRewardsVouchersFragment;
import za.co.woolworths.financial.services.android.ui.fragments.wreward.logged_in.WRewardsLoggedinAndLinkedFragment;
import za.co.woolworths.financial.services.android.ui.fragments.wtoday.WTodayFragment;
import za.co.woolworths.financial.services.android.ui.views.NestedScrollableViewHelper;
import za.co.woolworths.financial.services.android.ui.views.SlidingUpPanelLayout;
import za.co.woolworths.financial.services.android.ui.views.ToastFactory;
import za.co.woolworths.financial.services.android.ui.views.WBottomNavigationView;
import za.co.woolworths.financial.services.android.ui.views.WMaterialShowcaseView;
import za.co.woolworths.financial.services.android.ui.views.shop.dash.ChangeFulfillmentCollectionStoreFragment;
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature.fragment.UserAccountsLandingFragment;
import za.co.woolworths.financial.services.android.util.AppConstant;
import za.co.woolworths.financial.services.android.util.AuthenticateUtils;
import za.co.woolworths.financial.services.android.util.DeepLinkingUtils;
import za.co.woolworths.financial.services.android.util.KotlinUtils;
import za.co.woolworths.financial.services.android.util.MultiClickPreventer;
import za.co.woolworths.financial.services.android.util.PermissionResultCallback;
import za.co.woolworths.financial.services.android.util.PermissionUtils;
import za.co.woolworths.financial.services.android.util.QueryBadgeCounter;
import za.co.woolworths.financial.services.android.util.ScreenManager;
import za.co.woolworths.financial.services.android.util.SessionExpiredUtilities;
import za.co.woolworths.financial.services.android.util.SessionUtilities;
import za.co.woolworths.financial.services.android.util.ToastUtils;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager;
import za.co.woolworths.financial.services.android.util.nav.FragNavController;
import za.co.woolworths.financial.services.android.util.nav.FragNavTransactionOptions;

@AndroidEntryPoint
public class BottomNavigationActivity extends BaseActivity<ActivityBottomNavigationBinding, BottomNavigationViewModel>
        implements BottomNavigator, FragNavController.TransactionListener, FragNavController.RootFragmentListener,
        PermissionResultCallback, ToastUtils.ToastInterface, IToastInterface, Observer {


    public static final int INDEX_PRODUCT = FragNavController.TAB1;
    public static final int INDEX_TODAY = FragNavController.TAB2;
    public static final int INDEX_CART = FragNavController.TAB3;
    public static final int INDEX_REWARD = FragNavController.TAB4;
    public static final int INDEX_ACCOUNT = FragNavController.TAB5;
    public static final int REMOVE_ALL_BADGE_COUNTER = FragNavController.TAB6;

    public static final int OPEN_CART_REQUEST = 12346;
    public static final int SLIDE_UP_COLLAPSE_REQUEST_CODE = 13;
    public static final int SLIDE_UP_COLLAPSE_RESULT_CODE = 12345;
    public static final int BOTTOM_FRAGMENT_REQUEST_CODE = 3401;
    public static final int TIPS_AND_TRICKS_CTA_REQUEST_CODE = 3627;
    public static final int DEEP_LINK_REQUEST_CODE = 123;
    public static final int SHARE_LINK_REQUEST_CODE = 321;
    public static final int RESULT_OK_OPEN_CART_FROM_SHOPPING_DETAILS = 3628;
    public static final int RESULT_OK_OPEN_CART = 3629;
    public static final String KEY_PRODUCT_LIST = "productList";
    public static final String KEY_PRODUCT_NAME = "productName";

    public final String TAG = this.getClass().getSimpleName();
    public AccountMasterCache mAccountMasterCache = AccountMasterCache.INSTANCE;
    private PermissionUtils permissionUtils;
    private ArrayList<String> permissions;
    private BottomNavigationViewModel bottomNavigationViewModel;
    public FragNavController mNavController;
    private Bundle mBundle;
    private int currentSection;
    private ToastUtils mToastUtils;
    public static final int LOCK_REQUEST_CODE_ACCOUNTS = 444;
    private QueryBadgeCounter mQueryBadgeCounter;
    public WMaterialShowcaseView walkThroughPromtView = null;
    public RefinementDrawerFragment drawerFragment;
    public JsonObject appLinkData;
    private BottomNavigationItemView accountNavigationView;
    private View notificationBadgeOne;
    private ImageView onlineIconImageView;
    private Boolean isDeeplinkAction = false;
    private Boolean isNewSession = false;
    private int currentTabIndex = INDEX_TODAY;
    private int previousTabIndex = INDEX_TODAY;

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
        try {
            super.onSaveInstanceState(outState);
            SavedInstanceFragment.getInstance(getFragmentManager()).pushData((Bundle) outState.clone());
            outState.clear();
        } catch (Exception ex) {
            FirebaseManager.Companion.logException(ex);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        try {
            if (savedInstanceState != null
                    && getFragmentManager() != null
                    && SavedInstanceFragment.getInstance(getFragmentManager()).popData() != null)
                super.onRestoreInstanceState(SavedInstanceFragment.getInstance(getFragmentManager()).popData());
        } catch (NullPointerException ex) {
            FirebaseManager.Companion.logException(ex);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(SavedInstanceFragment.getInstance(getFragmentManager()).popData());
        mBundle = getIntent().getExtras();
        parseDeepLinkData();
        new AmplifyInit();
        mNavController = FragNavController.newBuilder(savedInstanceState,
                        getSupportFragmentManager(),
                        R.id.frag_container)
                .fragmentHideStrategy(FragNavController.HIDE)
                .transactionListener(this)
                .switchController(UNLIMITED_TAB_HISTORY, (index, transactionOptions) -> getBottomNavigationById().setCurrentItem(index))
                .eager(true)
                .rootFragmentListener(this, 5)
                .build();
        // Adding default position for WToday Tab with index(INDEX_TODAY that is position 1) (When open app)
        // Tab order remain same in order to Shop,Today, My Cart,WRewards,My Account
        getBottomNavigationById().setCurrentItem(INDEX_TODAY);
        renderUI();

        initBadgeCounter();

        observableOn((Consumer<Object>) object -> {
            if (object instanceof LoadState) {
                String searchProduct = ((LoadState) object).getSearchProduct();
                if (!TextUtils.isEmpty((searchProduct))) {
                    pushFragment(ProductListingFragment.Companion.newInstance(
                            ProductsRequestParams.SearchType.SEARCH,
                            "",
                            searchProduct,
                            true,
                            ((LoadState) object).isSendDeliveryDetails(), true));
                }
            } else if (object instanceof CartSummaryResponse) {
                // product item successfully added to cart
                cartSummaryAPI();
                closeSlideUpPanel();
                setToast(getResources().getString(R.string.added_to), getResources().getString(R.string.cart), null, 0);
            } else if (object instanceof BadgeState) {
                // call observer to update independent count
                BadgeState badgeState = (BadgeState) object;
                switch (badgeState.getPosition()) {
                    case BadgeState.CART_COUNT_TEMP:
                        addBadge(INDEX_CART, badgeState.getCount());
                        break;
                    case BadgeState.CART_COUNT:
                        cartSummaryAPI();
                        break;
                    default:
                        break;
                }
            }
        });

        queryBadgeCountOnStart();
        addDrawerFragment();
    }

    private void parseDeepLinkData() {
        if (mBundle == null) {
            return;
        }
        String deepLinkData = mBundle.getString("parameters", "").replace("\\", "");
        if (deepLinkData == null) {
            return;
        }
        try {
            appLinkData = (JsonObject) Utils.strToJson(deepLinkData, JsonObject.class);
        } catch (Exception e) {
            mOnNavigationItemSelectedListener.onNavigationItemSelected(
                    getBottomNavigationById().getMenu().findItem(R.id.navigation_today));
        }

    }

    private void queryBadgeCountOnStart() {
        if (SessionUtilities.getInstance().isUserAuthenticated() && AppConfigSingleton.INSTANCE.isBadgesRequired()) {
            mQueryBadgeCounter.queryVoucherCount();
            mQueryBadgeCounter.updateCartSummaryCount();
            mQueryBadgeCounter.queryMessageCount();
            AppConfigSingleton.INSTANCE.setBadgesRequired(false);
        } else if (!AppConfigSingleton.INSTANCE.isBadgesRequired()) {
            AppConfigSingleton.INSTANCE.setBadgesRequired(true);
        }
    }

    /**
     * Update bottom navigation view counter
     */
    private void initBadgeCounter() {
        mQueryBadgeCounter = QueryBadgeCounter.getInstance();
        mQueryBadgeCounter.addObserver(this);
    }

    public void setToast(String message, String cartText, ProductCountMap productCountMap, int noOfItems) {
        if (productCountMap != null
                && (KotlinUtils.Companion.isDeliveryOptionClickAndCollect()
                || KotlinUtils.Companion.isDeliveryOptionDash())
                && productCountMap.getQuantityLimit() != null
                && productCountMap.getQuantityLimit().getFoodLayoutColour() != null) {
            ToastFactory.Companion.showItemsLimitToastOnAddToCart(getBottomNavigationById(), productCountMap, this, noOfItems, true);
            return;
        }
        mToastUtils = new ToastUtils(BottomNavigationActivity.this);
        mToastUtils.setActivity(BottomNavigationActivity.this);
        mToastUtils.setGravity(Gravity.BOTTOM);
        mToastUtils.setCurrentState(TAG);
        mToastUtils.setCartText(cartText);
        mToastUtils.setAllCapsUpperCase(false);
        mToastUtils.setPixel(getBottomNavigationById().getHeight() + Utils.dp2px(10));
        mToastUtils.setView(getBottomNavigationById());
        mToastUtils.setMessage(message);
        mToastUtils.setViewState(true);
        mToastUtils.build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBundle != null) {

            String mSessionExpiredAtTabSection = mBundle.getString("sessionExpiredAtTabSection");
            if (!TextUtils.isEmpty(mSessionExpiredAtTabSection)) {
                getBottomNavigationById().setCurrentItem(Integer.parseInt(mSessionExpiredAtTabSection));
                SessionExpiredUtilities.getInstance().showSessionExpireDialog(BottomNavigationActivity.this);
            }
            String changePassword = mBundle.getString(FORGOT_PASSWORD);
            if (null != changePassword && changePassword.equals(FORGOT_PASSWORD_VALUE)) {
                navigateMyAccountScreen();
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
        if (mBundle != null && mBundle.get("feature") != null && !TextUtils.isEmpty(mBundle.get("feature").toString())) {
            String deepLinkType = mBundle.get("feature").toString();

            switch (deepLinkType) {
                case AppConstant.DP_LINKING_PRODUCT_LISTING:
                    if (appLinkData == null) {
                        return;
                    }
                    if (appLinkData.get("url") == null) {
                        return;
                    }

                    Uri linkData = Uri.parse(appLinkData.get("url").getAsString());
                    ProductSearchTypeAndTerm productSearchTypeAndSearchTerm = DeepLinkingUtils.Companion.getProductSearchTypeAndSearchTerm(linkData.toString());
                    if (!productSearchTypeAndSearchTerm.getSearchTerm().isEmpty() && !productSearchTypeAndSearchTerm.getSearchTerm().equalsIgnoreCase(DeepLinkingUtils.WHITE_LISTED_DOMAIN)) {
                        Map<String, String> arguments = new HashMap<>();
                        arguments.put(FirebaseManagerAnalyticsProperties.PropertyNames.ENTRY_POINT, FirebaseManagerAnalyticsProperties.EntryPoint.DEEP_LINK.getValue());
                        arguments.put(FirebaseManagerAnalyticsProperties.PropertyNames.DEEP_LINK_URL, linkData.toString());
                        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYCARTDELIVERY, arguments, this);
                        pushFragment(ProductListingFragment.Companion.newInstance(productSearchTypeAndSearchTerm.getSearchType(), "", productSearchTypeAndSearchTerm.getSearchTerm(), true, false));
                    }
                    break;

                case AppConstant.DP_LINKING_STREAM_CHAT_CHANNEL_ID:
                    if (appLinkData.get(AppConstant.DP_LINKING_PARAM_STREAM_ORDER_ID) == null) {
                        return;
                    }

                    String orderId = appLinkData.get(AppConstant.DP_LINKING_PARAM_STREAM_ORDER_ID).getAsString();
                    String channelId = appLinkData.get(AppConstant.DP_LINKING_PARAM_STREAM_CHANNEL_ID).getAsString();
                    if (orderId != null && !orderId.isEmpty() && SessionUtilities.getInstance().isUserAuthenticated()) {
                        startActivity(OCChatActivity.Companion.newIntent(this, orderId, channelId));
                    }
                    break;

                /*Deep link to PDP disabled*/
                /*case AppConstant.DP_LINKING_PRODUCT_DETAIL:
                    Intent intent = new Intent(this, ProductDetailsDeepLinkActivity.class);
                    intent.putExtra("feature", AppConstant.DP_LINKING_PRODUCT_DETAIL);
                    intent.putExtra("parameters", appLinkData.toString());
                    intent.putExtra("deepLinkRequestCode", DEEP_LINK_REQUEST_CODE);
                    startActivityForResult(intent, DEEP_LINK_REQUEST_CODE);
                    break;*/
                case AppConstant.DP_LINKING_MY_ACCOUNTS_PRODUCT_STATEMENT:
                case AppConstant.DP_LINKING_MY_ACCOUNTS_PRODUCT_PAY_MY_ACCOUNT:
                case AppConstant.DP_LINKING_MY_ACCOUNTS_PRODUCT:
                case AppConstant.DP_LINKING_MY_ACCOUNTS:
                    isDeeplinkAction = true;
                    BottomNavigationItemView itemView = getBottomNavigationById().getBottomNavigationItemView(INDEX_ACCOUNT);
                    new Handler().postDelayed(itemView::performClick, AppConstant.DELAY_100_MS);
                    break;

                case DP_LINKING_MY_ACCOUNTS_ORDER_DETAILS:
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (appLinkData != null && !TextUtils.isEmpty(appLinkData.toString())) {
                            Parameter params = new Gson().fromJson(appLinkData.toString(), Parameter.class);
                            deepLinkToOrderDetails(params);
                        }
                    }, AppConstant.DELAY_100_MS);
                    break;
            }
        }

        BottomNavigationMenuView bottomNavigationMenu = getBottomNavigationById().getBottomNavigationMenuView();
        if (bottomNavigationMenu != null && bottomNavigationMenu.getChildAt(INDEX_ACCOUNT) instanceof BottomNavigationItemView) {
            accountNavigationView = (BottomNavigationItemView) bottomNavigationMenu.getChildAt(INDEX_ACCOUNT);
            notificationBadgeOne = LayoutInflater.from(this).inflate(R.layout.green_circle_icon, accountNavigationView, false);
            onlineIconImageView = notificationBadgeOne.findViewById(R.id.onlineIconImageView);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            params.addRule(RelativeLayout.ALIGN_END, RelativeLayout.TRUE);
            params.addRule(RelativeLayout.ALIGN_BOTTOM, RelativeLayout.TRUE);
            notificationBadgeOne.setLayoutParams(params);
        }
    }

    private void deepLinkToOrderDetails(Parameter params) {
        if (SessionUtilities.getInstance().isUserAuthenticated()) {
            pushFragment(OrderDetailsFragment.Companion.getInstance(params));
        }
    }

    @Override
    public void bottomNavConfig() {
        Typeface tfMyriadProT = Typeface.createFromAsset(getAssets(), "fonts/OpenSans-SemiBold.ttf");
        getBottomNavigationById().setTypeface(tfMyriadProT);
        getBottomNavigationById().setTextSize(10);
        // set icon size
        int iconSize = 24;
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
        runOnUiThread(() -> Utils.addBadgeAt(this, getBottomNavigationById(), position, number));
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

    public  void  setToolbarContentDescription(String description) {
        getToolbarTitle().setContentDescription(description);
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
        getSlidingLayout().setFadeOnClickListener(view -> getSlidingLayout().setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED));
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
        bundle.putString(STR_PRODUCT_LIST, strProductList);
        bundle.putString(STR_PRODUCT_CATEGORY, productName);
        bundle.putString(STR_BRAND_HEADER, productList.brandHeaderDescription);
        ProductDetailsFragment productDetailsFragmentNew = ProductDetailsFragment.Companion.newInstance();
        productDetailsFragmentNew.setArguments(bundle);
        Utils.updateStatusBarBackground(this);
        pushFragment(productDetailsFragmentNew);
    }

    public void openProductDetailFragment(String productName, ProductList productList, String bannerLabel, String bannerImage, Boolean isUserBrowsing) {
        Gson gson = new Gson();
        String strProductList = gson.toJson(productList);
        Bundle bundle = new Bundle();
        bundle.putString(STR_PRODUCT_LIST, strProductList);
        bundle.putString(STR_PRODUCT_CATEGORY, productName);
        bundle.putString(STR_BRAND_HEADER, productList.brandHeaderDescription);
        bundle.putBoolean(IS_BROWSING, isUserBrowsing);
        bundle.putSerializable(BRAND_NAVIGATION_DETAILS, new BrandNavigationDetails(
                productList.brandText,
                bannerLabel,
                bannerImage
        ));
        ProductDetailsFragment productDetailsFragmentNew = ProductDetailsFragment.Companion.newInstance();
        productDetailsFragmentNew.setArguments(bundle);
        Utils.updateStatusBarBackground(this);
        pushFragment(productDetailsFragmentNew);
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

            if (mNavController.getCurrentFrag() instanceof ProductDetailsFragment) {
                ProductDetails productDetails = ((ProductDetailsFragment) mNavController.getCurrentFrag()).getProductDetails();
                Bundle arguments = fragment.getArguments();
                ProductDetails newProductDetails = (ProductDetails) Utils.jsonStringToObject(arguments.getString(STR_PRODUCT_LIST), ProductDetails.class);

                if (productDetails != null && newProductDetails != null && !TextUtils.isEmpty(productDetails.productId)
                        && !TextUtils.isEmpty(newProductDetails.productId) && productDetails.productId.equals(newProductDetails.productId)) {
                    // when we open same PDP then instead of new PDP it will close existing PDP and opens up new same PDP.
                    mNavController.popFragment();
                    mNavController.pushFragment(fragment, ft);
                    return;
                }
            }
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

    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
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
                    replaceAccountIcon(item);
                    setCurrentSection(R.id.navigation_today);
                    setToolbarBackgroundColor(R.color.white);
                    switchTab(INDEX_TODAY);
                    hideToolbar();
                    Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.WTODAYMENU, BottomNavigationActivity.this);
                    return true;

                case R.id.navigate_to_shop:
                    onShopTabSelected(item);
                    return true;

                case R.id.navigate_to_cart:
                    replaceAccountIcon(item);
                    setCurrentSection(R.id.navigate_to_cart);
                    switchTab(INDEX_CART);
                    hideToolbar();
                    identifyTokenValidationAPI();
                    if (AppConfigSingleton.INSTANCE.isBadgesRequired())
                        queryBadgeCountOnStart();
                    Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYCARTMENU, BottomNavigationActivity.this);
                    return true;

                case R.id.navigate_to_wreward:
                    replaceAccountIcon(item);
                    currentSection = R.id.navigate_to_wreward;
                    setToolbarBackgroundColor(R.color.white);
                    switchTab(INDEX_REWARD);
                    if (AppConfigSingleton.INSTANCE.isBadgesRequired())
                        queryBadgeCountOnStart();
                    Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.WREWARDSMENU, BottomNavigationActivity.this);
                    return true;

                case R.id.navigate_to_account:
                    setCurrentSection(R.id.navigate_to_account);
                    replaceAccountIcon(item);
                    if (AppConfigSingleton.INSTANCE.isBadgesRequired() && !isDeeplinkAction)
                        queryBadgeCountOnStart();
                    isDeeplinkAction = false;
                    if (AuthenticateUtils.getInstance(BottomNavigationActivity.this).isBiometricAuthenticationRequired()) {
                        try {
                            AuthenticateUtils.getInstance(BottomNavigationActivity.this).startAuthenticateApp(LOCK_REQUEST_CODE_ACCOUNTS);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        setToolbarBackgroundColor(R.color.white);
                        switchTab(INDEX_ACCOUNT);
                        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTSMENU, BottomNavigationActivity.this);
                        return true;
                    }
            }
            return false;
        }
    };

    public void onShopTabSelected(MenuItem item) {
        replaceAccountIcon(item);
        setCurrentSection(R.id.navigate_to_shop);
        switchTab(INDEX_PRODUCT);
        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SHOPMENU, BottomNavigationActivity.this);

        Fragment fragment = mNavController.getCurrentFrag();
        if (isNewSession && fragment instanceof ShopFragment) {
            isNewSession = false;
            ((ShopFragment) fragment).setShopDefaultTab();
        }

       if(getCurrentFragment() instanceof ShopFragment) {
            ShopFragment fragment1 = (ShopFragment) getCurrentFragment();
            fragment1.showShopFeatureWalkThrough();
           // Check for location permission. if permission is rejected then never ask again.
           if(Utils.isLocationEnabled(this)) {
               fragment1.checkRunTimePermissionForLocation();
           }
        }
    }

    private void replaceAccountIcon(@NonNull MenuItem item) {
        if (accountNavigationView != null) {
            if (ChatAWSAmplify.INSTANCE.isLiveChatBackgroundServiceRunning()
                    && item.getItemId() != R.id.navigate_to_account) {
                accountNavigationView.removeView(notificationBadgeOne);
                SessionStateType sessionStateType = ChatAWSAmplify.INSTANCE.getSessionStateType();
                if (sessionStateType != null) {
                    if (sessionStateType == SessionStateType.DISCONNECT) {
                        onlineIconImageView.setImageResource(R.drawable.nb_borderless_disconnect_badge_bg);
                    } else {
                        onlineIconImageView.setImageResource(R.drawable.nb_borderless_badge_bg);
                    }
                }
                accountNavigationView.addView(notificationBadgeOne);
            } else {
                accountNavigationView.removeView(notificationBadgeOne);
            }
        } else {
            FirebaseManager.logException("accountNavigationView is null");
        }
    }

    private final BottomNavigationView.OnNavigationItemReselectedListener mOnNavigationItemReSelectedListener
            = new BottomNavigationView.OnNavigationItemReselectedListener() {
        @Override
        public void onNavigationItemReselected(@NonNull MenuItem item) {

            switch (item.getItemId()) {
                case R.id.navigation_today:
                    clearStack();
                    WTodayFragment wTodayFragment = (WTodayFragment) mNavController.getCurrentFrag();
                    if (wTodayFragment != null) {
                        wTodayFragment.scrollToTop();
                    }
                    Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.WTODAYMENU, BottomNavigationActivity.this);
                    break;

                case R.id.navigate_to_shop:
                    clearStack();
                    ShopFragment shopFragment = (ShopFragment) mNavController.getCurrentFrag();
                    if (shopFragment != null) {
                        shopFragment.scrollToTop();
                    }
                    Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SHOPMENU, BottomNavigationActivity.this);
                    break;

                case R.id.navigate_to_cart:
                    clearStack();
                    Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYCARTMENU, BottomNavigationActivity.this);
                    break;

                case R.id.navigate_to_wreward:
                    clearStack();
                    WRewardsFragment wRewardsFragment = (WRewardsFragment) mNavController.getCurrentFrag();
                    Fragment currentChildFragment = wRewardsFragment.getWRewardContentFrame();
                    if (currentChildFragment instanceof WRewardsLoggedinAndLinkedFragment) {
                        ((WRewardsLoggedinAndLinkedFragment) currentChildFragment).scrollToTop();
                    } else if (currentChildFragment instanceof WRewardsLoggedInAndNotLinkedFragment) {
                        ((WRewardsLoggedInAndNotLinkedFragment) currentChildFragment).scrollToTop();
                    } else {
                        if (currentChildFragment != null) {
                            ((WRewardsLoggedOutFragment) currentChildFragment).scrollToTop();
                        }
                    }
                    Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.WREWARDSMENU, BottomNavigationActivity.this);
                    break;

                case R.id.navigate_to_account:
                    clearStack();
                    if (mNavController.getCurrentFrag() instanceof UserAccountsLandingFragment) {
                        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTSMENU, BottomNavigationActivity.this);
                    }
                    break;
            }
        }
    };

    @Override
    public void onBackPressed() {

        if (walkThroughPromtView != null && !walkThroughPromtView.isDismissed()) {
            walkThroughPromtView.hide();
            return;
        }

        if (mNavController.getCurrentFrag() instanceof ProductListingFragment) {
            ((ProductListingFragment) mNavController.getCurrentFrag()).onBackPressed();
        }

        // Close slide up panel when expanded
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

        // Slide to previous fragment with custom left to right animation Close activity if fragment is at root level
        if (!mNavController.isRootFragment()) {
            mNavController.popFragment(new FragNavTransactionOptions.Builder().customAnimations(R.anim.slide_in_from_left, R.anim.slide_out_to_right).build());
        } else {
            if (!isFinishing())
                super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    public void onTabTransaction(Fragment fragment, int index) {
        // If we have a backstack, show the back button
        if (getSupportActionBar() != null && mNavController != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(!mNavController.isRootFragment());
        }
    }

    @Override
    public void onFragmentTransaction(Fragment fragment, @NonNull FragNavController.TransactionType transactionType) {
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
                return new CartFragment();
            case INDEX_REWARD:
                return new WRewardsFragment();
            case INDEX_ACCOUNT:
                UserAccountsLandingFragment userAccountsLandingFragment = new UserAccountsLandingFragment();
                userAccountsLandingFragment.setArguments(mBundle);
                return userAccountsLandingFragment;
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
        previousTabIndex = currentTabIndex;
        currentTabIndex = number;
        mNavController.switchTab(number);
        SessionUtilities.getInstance().setBottomNavigationPosition(String.valueOf(number));
    }

    @Override
    public void clearStack() {
        if (mNavController != null)
            mNavController.clearStack(new FragNavTransactionOptions.Builder().customAnimations(R.anim.slide_in_from_left, R.anim.slide_out_to_right).build());
    }

    @Override
    public void clearStackSignOut() {
        if (mNavController != null)
            mNavController.clearStackSignOut(new FragNavTransactionOptions.Builder().customAnimations(R.anim.slide_in_from_left, R.anim.slide_out_to_right).build(), previousTabIndex);
        isNewSession = true;
    }

    @Override
    public void cartSummaryAPI() {
    }

    @Override
    public void permissionGranted(int requestCode) {
        //TODO:: Parse result_code and use only onActivityResult line
        onActivityResult(requestCode, 200, null);
        switch (requestCode) {
            case 2:
                onActivityResult(requestCode, 200, null);
                break;

            default:
                break;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Fragment fragment = getCurrentFragment();
        if (fragment instanceof StoresNearbyFragment1) {
            fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        } else if (fragment instanceof ShopFragment) {
            fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        } else if (fragment instanceof ProductDetailsFragment) {
            fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        } else if (fragment instanceof ChangeFulfillmentCollectionStoreFragment) {
            fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }


        // redirects to utils
        permissionUtils.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // This flow is only when app is in foreground.
        if (intent.getExtras() == null || TextUtils.isEmpty(intent.getExtras().getString(AppConstant.Keys.EXTRA_NOTIFICATION_FEATURE))) {
            return;
        }

        String featureName = intent.getExtras().getString(AppConstant.Keys.EXTRA_NOTIFICATION_FEATURE);
        switch (featureName) {
            // Feature name is Order Details for Push Notification. And app is in foreground.
            case DP_LINKING_MY_ACCOUNTS_ORDER_DETAILS:
                String parameters = intent.getExtras().getString(AppConstant.Keys.EXTRA_NOTIFICATION_PARAMETERS);
                if (!TextUtils.isEmpty(parameters)) {
                    Parameter params = new Gson().fromJson(parameters, Parameter.class);
                    deepLinkToOrderDetails(params);
                }

                break;

            //  Old way to navigate Deeplinking flows.
            default:
                mBundle = intent.getExtras();
                parseDeepLinkData();
                renderUI();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //TODO: Explain where this is coming from.

        // Navigate from shopping list detail activity
        switch (requestCode) {
            case REQUEST_PAYMENT_STATUS:

                if (resultCode == RESULT_RELOAD_CART) {
                    getCurrentFragment().onActivityResult(requestCode, resultCode, data);
                }
                else {
                    navigateToTabIndex(BottomNavigationActivity.INDEX_PRODUCT, null);
                    QueryBadgeCounter.getInstance().queryCartSummaryCount();
                    break;
                }
                    break;
            case REQUEST_CODE_ORDER_DETAILS_PAGE:// Call back when Toast clicked after adding item to shopping list
            case SHOPPING_LIST_DETAIL_ACTIVITY_REQUEST_CODE:

                switch (resultCode) {
                    case RESULT_OK:
                        // Open Shopping List Detail Fragment From MyList and Add item to cart
                        String itemAddToCartMessage = data.getStringExtra("addedToCartMessage");
                        ProductCountMap productCountMap = (ProductCountMap) Utils.jsonStringToObject(data.getStringExtra("ProductCountMap"), ProductCountMap.class);
                        int itemsCount = data.getIntExtra("ItemsCount", 0);
                        if (itemAddToCartMessage != null) {
                            setToast(itemAddToCartMessage, "", productCountMap, itemsCount);
                        }
                        break;
                    case RESULT_OK_OPEN_CART_FROM_SHOPPING_DETAILS:
                        if (getBottomNavigationById() == null) {
                            return;
                        }
                        getBottomNavigationById().setCurrentItem(INDEX_CART);
                        break;
                }
                break;

            case REQUEST_CHECK_SETTINGS:
                getCurrentFragment().onActivityResult(requestCode, resultCode, data);
                break;

            case DEEP_LINK_REQUEST_CODE:
                finish();
                break;

            default:
                break;
        }

        if (resultCode == REQUEST_CHECKOUT_ON_CONTINUE_SHOPPING) {
            navigateToTabIndex(BottomNavigationActivity.INDEX_PRODUCT, null);
        }

        if (resultCode == PRODUCT_DETAILS_FROM_MY_LIST_SEARCH) {
            String productLIstStr = data.getStringExtra(KEY_PRODUCT_LIST);
            ProductList productList = (ProductList) Utils.jsonStringToObject(productLIstStr, ProductList.class);
            openProductDetailFragment(data.getStringExtra(KEY_PRODUCT_NAME), productList);
        }
        if ((requestCode == REQUEST_CODE_BARCODE_ACTIVITY || requestCode == TIPS_AND_TRICKS_CTA_REQUEST_CODE) && resultCode == RESULT_OK) {
            ProductsRequestParams.SearchType searchType = ProductsRequestParams.SearchType.valueOf(data.getStringExtra("searchType"));
            String searchTerm = data.getStringExtra("searchTerm");
            pushFragment(ProductListingFragment.Companion.newInstance(searchType, "", searchTerm, true, false));
            return;
        }

        //Open shopping from Tips and trick activity requestCode
        if (requestCode == TIPS_AND_TRICKS_CTA_REQUEST_CODE) {
            switch (resultCode) {
                case OPEN_SHOPPING_LIST_TAB_FROM_TIPS_AND_TRICK_RESULT_CODE:
                    if (getBottomNavigationById() == null) return;
                    getBottomNavigationById().setCurrentItem(INDEX_PRODUCT);
                    Fragment fragment = mNavController.getCurrentFrag();
                    if (fragment instanceof ShopFragment) {
                        ShopFragment shopFragment = (ShopFragment) fragment;
                        shopFragment.refreshViewPagerFragment();
                        shopFragment.navigateToMyListFragment();
                        return;
                    }
                    break;
                case RESULT_CODE_MY_ACCOUNT_FRAGMENT:
                    navigateToDepartmentFragment();
                    break;
                default:
                    break;
            }
        }

        if (requestCode == TIPS_AND_TRICKS_CTA_REQUEST_CODE && resultCode == RESULT_OK_ACCOUNTS) {
            getBottomNavigationById().setCurrentItem(INDEX_PRODUCT);
            clearStack();
            Fragment fragment = mNavController.getCurrentFrag();
            switch (resultCode) {
                case RESULT_OK_ACCOUNTS:
                    if (fragment instanceof ShopFragment) {
                        ShopFragment shopFragment = (ShopFragment) fragment;
                        shopFragment.navigateToMyListFragment();
                        shopFragment.refreshViewPagerFragment();
                        return;
                    }
                    break;
                default:
                    break;
            }
        }

        // navigate to product section
        if (requestCode == OPEN_CART_REQUEST) {
            //Handling error 500 from cart
            if (resultCode == CustomPopUpWindow.CART_DEFAULT_ERROR_TAPPED) {
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
                case RESULT_OK_OPEN_CART:
                    if (getBottomNavigationById() == null) {
                        return;
                    }
                    getBottomNavigationById().setCurrentItem(INDEX_CART);
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
                        clearStack();
                        identifyTokenValidationAPI();
                        return;
                    }
                    break;
                default:
                    Fragment fragmentById = getBottomFragmentById();
                    if (fragmentById == null) break;
                    if (fragmentById instanceof ProductDetailsFragment)
                        fragmentById.onActivityResult(requestCode, resultCode, data);
                    break;
            }
        }

        Fragment fragment = mNavController.getCurrentFrag();
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }

        //Call product detail onActivityResult
        if (resultCode == ConfirmColorSizeActivity.RESULT_TAP_FIND_INSTORE_BTN) {
            if (getBottomFragmentById() instanceof ProductDetailsFragment) {
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
                ProductCountMap productCountMap = (ProductCountMap) Utils.jsonStringToObject(data.getStringExtra("ProductCountMap"), ProductCountMap.class);
                int itemsCount = data.getIntExtra("ItemsCount", 0);
                if (itemAddToCartMessage != null) {
                    setToast(itemAddToCartMessage, "", productCountMap, itemsCount);
                }
            }
        }

        if (requestCode == BOTTOM_FRAGMENT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (getBottomFragmentById() instanceof ProductDetailsFragment) {
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
                case RESULT_OK_OPEN_CART_FROM_TIPS_AND_TRICKS:
                    if (SessionUtilities.getInstance().isUserAuthenticated()) {
                        getBottomNavigationById().setCurrentItem(INDEX_CART);
                    }
                    break;
            }
        }

    }

    private Fragment getBottomFragmentById() {
        FragmentManager fm = getSupportFragmentManager();
        return fm.findFragmentById(R.id.fragment_bottom_container);
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
            if (!(mNavController.getCurrentFrag() instanceof CartFragment)) {
                return;
            }
            CartFragment cartFragment = (CartFragment) mNavController.getCurrentFrag();
            cartFragment.reloadFragment(true);
        }
    }

    @Override
    public void cartSummaryInvalidToken() {
        runOnUiThread(() -> addBadge(INDEX_CART, 0));
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
                    if (getBottomNavigationById() == null) {
                        return;
                    }
                    getBottomNavigationById().setCurrentItem(INDEX_CART);
                }
            }
        }
    }

    @Override
    public void onToastButtonClicked(@Nullable JsonElement jsonElement) {
        if (mNavController != null && mNavController.getCurrentFrag() instanceof CartFragment) {
            NavigateToShoppingList.Companion navigateTo = NavigateToShoppingList.Companion;
            if (jsonElement != null) {
                //Navigate to Account tab, select My list tab and open shopping list
                getBottomNavigationById().setCurrentItem(INDEX_ACCOUNT);
                if (getCurrentFragment() instanceof ShopFragment) {
                    ShopFragment shopFragment = (ShopFragment) getCurrentFragment();
                    shopFragment.navigateToMyListFragment();
                }
                navigateTo.navigateToShoppingListOnToastClicked(this, jsonElement);
            }
            return;
        }
        switchToShoppingListTab(jsonElement);
    }

    public Fragment getCurrentFragment() {
        return mNavController.getCurrentFrag();
    }

    // SSO - After a successful login (and user has C2 ID):
    @Override
    public void badgeCount() {
        switch (getCurrentSection()) {
            case R.id.navigate_to_account:
            case R.id.navigation_today:
            case R.id.navigate_to_shop:
                mQueryBadgeCounter.queryCartSummaryCount();
                mQueryBadgeCounter.queryVoucherCount();
                break;
            case R.id.navigate_to_wreward:
                mQueryBadgeCounter.queryCartSummaryCount();
                break;
            case R.id.navigate_to_cart:
                mQueryBadgeCounter.queryVoucherCount();
                break;
            default:
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
                    queryBadgeCountOnStart();
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
                if (shoppingList != null) {
                    getBottomNavigationById().setCurrentItem(INDEX_ACCOUNT);
                    ScreenManager.presentShoppingListDetailActivity(this, listId, shoppingList.getAsJsonObject().get("name").getAsString());
                }
            } else {
                getBottomNavigationById().setCurrentItem(INDEX_ACCOUNT);
                MyShoppingListFragment fragment = new MyShoppingListFragment();
                pushFragment(fragment);
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

    public void navigateToDepartmentFragment() {
        getBottomNavigationById().setCurrentItem(INDEX_PRODUCT);
        clearStack();
        Fragment currentFragment = mNavController.getCurrentFrag();
        if (currentFragment instanceof ShopFragment) {
            ShopFragment shopFragment = (ShopFragment) currentFragment;
            shopFragment.onStartShopping();
        }
    }

    @Override
    public void addDrawerFragment() {
        int width = (int) (getResources().getDisplayMetrics().widthPixels * .80);
        DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) getViewDataBinding().drawerFragment.getLayoutParams();
        params.width = width;
        getViewDataBinding().drawerFragment.setLayoutParams(params);
        lockDrawerFragment();
        drawerFragment = new RefinementDrawerFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.drawerFragment, drawerFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void lockDrawerFragment() {
        getViewDataBinding().drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    @Override
    public void unLockDrawerFragment() {
        getViewDataBinding().drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    @Override
    public void setUpDrawerFragment(ProductView productsResponse, ProductsRequestParams productsRequestParams) {
        unLockDrawerFragment();
        if (drawerFragment != null && getViewDataBinding().drawerLayout != null)
            drawerFragment.setUpDrawer(getViewDataBinding().drawerLayout, productsResponse, productsRequestParams);
    }

    @Override
    public void openDrawerFragment() {
        getViewDataBinding().drawerLayout.openDrawer(Gravity.RIGHT);
    }

    @Override
    public void closeDrawerFragment() {
        getViewDataBinding().drawerLayout.closeDrawer(Gravity.RIGHT);
    }

    @Override
    public void onRefined(String navigationState, Boolean isMultiSelectCategoryRefined) {
        if (getCurrentFragment() instanceof ProductListingFragment) {
            ((ProductListingFragment) getCurrentFragment()).onRefined(navigationState, isMultiSelectCategoryRefined);
        }
    }

    @Override
    public void onResetFilter() {
        if (getCurrentFragment() instanceof ProductListingFragment) {
            ((ProductListingFragment) getCurrentFragment()).onResetFilter();
        }
    }

    @Override
    public void navigateToTabIndex(int tabIndex, @androidx.annotation.Nullable Bundle data) {
        getBottomNavigationById().setCurrentItem(tabIndex);
    }

    public void onSignedOut() {
        clearBadgeCount();
        clearStackSignOut();
        ScreenManager.presentSSOLogout(BottomNavigationActivity.this);
    }

    public void reloadDepartmentFragment() {
        Fragment currentFragment = mNavController.getCurrentFrag();
        if (currentFragment instanceof ShopFragment) {
            ShopFragment shopFragment = (ShopFragment) currentFragment;
            shopFragment.refreshCategories();
        }
    }

    public int getPreviousTabIndex() {
        return previousTabIndex;
    }

    private void navigateMyAccountScreen() {
        getBottomNavigationById().setCurrentItem(INDEX_ACCOUNT);
        switchTab(INDEX_ACCOUNT);
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            ScreenManager.presentSSOSignin(this);
        }, AppConstant.DELAY_500_MS);

    }
}