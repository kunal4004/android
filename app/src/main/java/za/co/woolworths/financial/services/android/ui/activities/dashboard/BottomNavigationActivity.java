package za.co.woolworths.financial.services.android.ui.activities.dashboard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;
import com.awfs.coordination.databinding.ActivityBottomNavigationBinding;
import com.google.gson.Gson;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import io.reactivex.functions.Consumer;
import za.co.woolworths.financial.services.android.models.dto.CartSummary;
import za.co.woolworths.financial.services.android.models.dto.CartSummaryResponse;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.service.event.AuthenticationState;
import za.co.woolworths.financial.services.android.models.service.event.BadgeState;
import za.co.woolworths.financial.services.android.models.service.event.LoadState;
import za.co.woolworths.financial.services.android.models.service.event.ProductState;
import za.co.woolworths.financial.services.android.ui.activities.CartActivity;
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity;
import za.co.woolworths.financial.services.android.ui.activities.splash.WSplashScreenActivity;
import za.co.woolworths.financial.services.android.ui.base.BaseActivity;
import za.co.woolworths.financial.services.android.ui.base.SavedInstanceFragment;
import za.co.woolworths.financial.services.android.ui.fragments.account.MyAccountsFragment;
import za.co.woolworths.financial.services.android.ui.fragments.barcode.BarcodeFragment;
import za.co.woolworths.financial.services.android.ui.fragments.barcode.manual.ManualBarcodeFragment;
import za.co.woolworths.financial.services.android.ui.fragments.product.category.CategoryFragment;
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.ProductDetailFragment;
import za.co.woolworths.financial.services.android.ui.fragments.product.grid.GridFragment;
import za.co.woolworths.financial.services.android.ui.fragments.wreward.WRewardsVouchersFragment;
import za.co.woolworths.financial.services.android.ui.fragments.wreward.base.WRewardsFragment;
import za.co.woolworths.financial.services.android.ui.fragments.wtoday.WTodayFragment;
import za.co.woolworths.financial.services.android.ui.views.NestedScrollableViewHelper;
import za.co.woolworths.financial.services.android.ui.views.SlidingUpPanelLayout;
import za.co.woolworths.financial.services.android.ui.views.WBottomNavigationView;
import za.co.woolworths.financial.services.android.util.AuthenticateUtils;
import za.co.woolworths.financial.services.android.util.KeyboardUtil;
import za.co.woolworths.financial.services.android.util.MultiClickPreventer;
import za.co.woolworths.financial.services.android.util.NotificationUtils;
import za.co.woolworths.financial.services.android.util.PermissionResultCallback;
import za.co.woolworths.financial.services.android.util.PermissionUtils;
import za.co.woolworths.financial.services.android.util.ScreenManager;
import za.co.woolworths.financial.services.android.util.SessionUtilities;
import za.co.woolworths.financial.services.android.util.ToastUtils;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.nav.FragNavController;
import za.co.woolworths.financial.services.android.util.nav.FragNavSwitchController;
import za.co.woolworths.financial.services.android.util.nav.FragNavTransactionOptions;
import za.co.woolworths.financial.services.android.util.nav.tabhistory.FragNavTabHistoryController;

import static za.co.woolworths.financial.services.android.models.service.event.BadgeState.CART_COUNT;
import static za.co.woolworths.financial.services.android.models.service.event.BadgeState.CART_COUNT_TEMP;
import static za.co.woolworths.financial.services.android.models.service.event.BadgeState.MESSAGE_COUNT;
import static za.co.woolworths.financial.services.android.models.service.event.BadgeState.REWARD_COUNT;
import static za.co.woolworths.financial.services.android.models.service.event.ProductState.OPEN_GET_LIST_SCREEN;
import static za.co.woolworths.financial.services.android.models.service.event.ProductState.SHOW_ADDED_TO_SHOPPING_LIST_TOAST;
import static za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow.CART_DEFAULT_ERROR_TAPPED;
import static za.co.woolworths.financial.services.android.ui.fragments.product.detail.ProductDetailFragment.INDEX_ADD_TO_SHOPPING_LIST;

public class BottomNavigationActivity extends BaseActivity<ActivityBottomNavigationBinding, BottomNavigationViewModel> implements BottomNavigator, FragNavController.TransactionListener, FragNavController.RootFragmentListener, PermissionResultCallback, ToastUtils.ToastInterface {

	public static final int INDEX_TODAY = FragNavController.TAB1;
	public static final int INDEX_PRODUCT = FragNavController.TAB2;
	public static final int INDEX_CART = FragNavController.TAB3;
	public static final int INDEX_REWARD = FragNavController.TAB4;
	public static final int INDEX_ACCOUNT = FragNavController.TAB5;
	public static final int OPEN_CART_REQUEST = 12346;
	public static final int SLIDE_UP_COLLAPSE_REQUEST_CODE = 13;
	public static final int SLIDE_UP_COLLAPSE_RESULT_CODE = 12345;

	public final String TAG = this.getClass().getSimpleName();
	private PermissionUtils permissionUtils;
	private ArrayList<String> permissions;
	private BottomNavigationViewModel bottomNavigationViewModel;
	private FragNavController mNavController;
	private WRewardsFragment wRewardsFragment;
	private MyAccountsFragment myAccountsFragment;
	private Bundle mBundle;
	private int currentSection;
	private ToastUtils mToastUtils;
	private boolean closeFromListEnabled;
	private int shoppingListItemCount;
	private boolean singleOrMultipleItemSelector;
	public static final int LOCK_REQUEST_CODE_ACCOUNTS = 444;

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
						onSignOut();
						ScreenManager.presentSSOLogout(BottomNavigationActivity.this);
					}
				} else if (object instanceof CartSummaryResponse) {
					// product item successfully added to cart
					cartSummaryAPI();
					closeSlideUpPanel();
					setToast();
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

						case REWARD_COUNT:
							getViewModel().getVoucherCount().execute();
							break;

						case MESSAGE_COUNT:
							getViewModel().getMessageResponse().execute();
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

	private void setToast() {
		mToastUtils = new ToastUtils(BottomNavigationActivity.this);
		mToastUtils.setActivity(BottomNavigationActivity.this);
		mToastUtils.setView(getBottomNavigationById());
		mToastUtils.setGravity(Gravity.BOTTOM);
		mToastUtils.setCurrentState(TAG);
		mToastUtils.setCartText(R.string.cart);
		mToastUtils.setPixel(getBottomNavigationById().getHeight() + Utils.dp2px(BottomNavigationActivity.this, 45));
		mToastUtils.setView(getBottomNavigationById());
		mToastUtils.setMessage(R.string.added_to);
		mToastUtils.setViewState(true);
		mToastUtils.build();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mBundle != null) {
			if (!TextUtils.isEmpty(mBundle.getString(NotificationUtils.PUSH_NOTIFICATION_INTENT))) {
				getBottomNavigationById().setCurrentItem(INDEX_ACCOUNT);
				mBundle = null;
			}
		}
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
						try {
							//detach detail fragment
							if (getBottomFragmentById() instanceof ProductDetailFragment) {
								ProductDetailFragment productDetailFragment = (ProductDetailFragment) getBottomFragmentById();
								productDetailFragment.onDetach();
							}
						} catch (ClassCastException e) {
							// not that fragment
						}

						// show toast on search result fragment after add to list
						// activates when user access pdp page from list section
						if (closeFromListEnabled()) {
							Utils.sendBus(new ProductState(getShoppingListItemCount(), SHOW_ADDED_TO_SHOPPING_LIST_TOAST));
							setCloseFromListEnabled(false);
						}

						// open single list or multiple list view on collapsed
						if (singleOrMultipleItemSelector()) {
							Utils.sendBus(new ProductState(OPEN_GET_LIST_SCREEN));
							setSingleOrMultipleItemSelector(false);
						}
						onActivityResult(SLIDE_UP_COLLAPSE_REQUEST_CODE, SLIDE_UP_COLLAPSE_RESULT_CODE, null);
						break;

					case EXPANDED:
						setCloseFromListEnabled(false);
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
		ProductDetailFragment productDetailFragment = new ProductDetailFragment();
		Gson gson = new Gson();
		String strProductList = gson.toJson(productList);
		Bundle bundle = new Bundle();
		bundle.putString("strProductList", strProductList);
		bundle.putString("strProductCategory", productName);
		productDetailFragment.setArguments(bundle);
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.fragment_bottom_container, productDetailFragment).commitAllowingStateLoss();
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
						if (getGlobalState().toolbarIsShown()) {
							statusBarColor(R.color.white);
						} else {
							statusBarColor(R.color.recent_search_bg);
						}
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

			statusBarColor(R.color.white);
			MultiClickPreventer.preventMultiClick(getViewDataBinding().wBottomNavigation);
			switch (item.getItemId()) {
				case R.id.navigation_today:
					setCurrentSection(R.id.navigation_today);
					setToolbarBackgroundColor(R.color.white);
					switchTab(INDEX_TODAY);
					hideToolbar();
					return true;

				case R.id.navigation_product:
					setCurrentSection(R.id.navigation_product);
					switchTab(INDEX_PRODUCT);
					Utils.showOneTimePopup(BottomNavigationActivity.this);
					return true;

				case R.id.navigation_cart:
					setCurrentSection(R.id.navigation_cart);
					identifyTokenValidationAPI();
					return false;

				case R.id.navigation_reward:
					currentSection = R.id.navigation_reward;
					setToolbarBackgroundColor(R.color.white);
					switchTab(INDEX_REWARD);
					return true;

				case R.id.navigation_account:
					if(AuthenticateUtils.getInstance(BottomNavigationActivity.this).isBiometricAuthenticationRequired()){
						try {
							AuthenticateUtils.getInstance(BottomNavigationActivity.this).startAuthenticateApp(LOCK_REQUEST_CODE_ACCOUNTS);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}else {
						setCurrentSection(R.id.navigation_account);
						setToolbarBackgroundColor(R.color.white);
						switchTab(INDEX_ACCOUNT);
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
					break;

				case R.id.navigation_product:
					clearStack();
					break;

				case R.id.navigation_cart:
					clearStack();
					break;

				case R.id.navigation_reward:
					clearStack();
					break;

				case R.id.navigation_account:
					clearStack();
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
		/**
		 *  Close slide up panel when expanded
		 */
		if (getSlidingLayout() != null) {
			if (getSlidingLayout().getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
				closeSlideUpPanel();
				return;
			}
		}

		/**
		 *  Close barcode fragment with slide down animation
		 */
		if (mNavController.getCurrentFrag() instanceof BarcodeFragment) {
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
				return new CategoryFragment();
			case INDEX_CART:
				return new CategoryFragment();
			case INDEX_REWARD:
				wRewardsFragment = new WRewardsFragment();
				return wRewardsFragment;
			case INDEX_ACCOUNT:
				myAccountsFragment = new MyAccountsFragment();
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
			mNavController.popFragment(new FragNavTransactionOptions.Builder().customAnimations(R.anim.stay, R.anim.stay).build());
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
	}

	@Override
	public void clearStack() {
		if (mNavController != null)
			mNavController.clearStack(new FragNavTransactionOptions.Builder().customAnimations(R.anim.slide_in_from_left, R.anim.slide_out_to_right).build());
	}

	@Override
	public void cartSummaryAPI() {
		getViewModel().getCartSummary().execute();
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
				sendBus(new ProductDetailFragment());
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
		// redirects to utils
		permissionUtils.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		//TODO: Explain where this is coming from.

		// navigate to product section
		if (requestCode == OPEN_CART_REQUEST) {
			//Handling error 500 from cart
			if (resultCode == CART_DEFAULT_ERROR_TAPPED) {
				return;
			}
			switch (resultCode) {
				case RESULT_OK:
					getBottomNavigationById().setCurrentItem(INDEX_PRODUCT);
					break;
				case 0:
					//load count on login success
					badgeCount();
					switch (getCurrentSection()) {
						case R.id.navigation_cart:
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
			badgeCount();
			switch (getCurrentSection()) {
				case R.id.navigation_cart:
					Intent openCartActivity = new Intent(this, CartActivity.class);
					startActivityForResult(openCartActivity, OPEN_CART_REQUEST);
					overridePendingTransition(0, 0);
					break;
				default:
					break;
			}

			switch (getBottomNavigationById().getCurrentItem()) {
				case 1:
					switch (getGlobalState().getSaveButtonClick()) {
						case INDEX_ADD_TO_SHOPPING_LIST:
							try {
								Fragment fragmentById = getBottomFragmentById();
								if (fragmentById instanceof ProductDetailFragment) {
									ProductDetailFragment productDetailFragment = (ProductDetailFragment) fragmentById;
									productDetailFragment.reloadGetListAPI();
								}
							} catch (ClassCastException e) {
								// not that fragment
							}
					}
					break;
				default:
					break;
			}
		}

		Fragment fragment = mNavController.getCurrentFrag();
		//trigger reward and account call
		switch (getBottomNavigationById().getCurrentItem()) {
			case 0:
				break;
			case 1:
				if (fragment instanceof CategoryFragment) // camera runtime permission successfully granted
					if (fragment != null) {
						fragment.onActivityResult(requestCode, resultCode, data);
					}
				if (fragment instanceof BarcodeFragment)
					if (fragment != null) {
						fragment.onActivityResult(requestCode, resultCode, data);
					}
				if (fragment instanceof ManualBarcodeFragment)
					if (fragment != null) {
						fragment.onActivityResult(requestCode, resultCode, data);
					}
				break;
			case 2:
				break;
			default:
				if (wRewardsFragment != null) {
					wRewardsFragment.onActivityResult(requestCode, resultCode, data);
				}
				if (myAccountsFragment != null) {
					myAccountsFragment.onActivityResult(requestCode, resultCode, data);
				}
				/**
				 * Trigger onActivityResult() from current visible fragment
				 */
				if (fragment != null) {
					fragment.onActivityResult(requestCode, resultCode, data);
				}
				break;
		}

		if (requestCode == WRewardsVouchersFragment.LOCK_REQUEST_CODE_WREWARDS && resultCode == RESULT_OK) {
			Utils.sendBus(new WRewardsVouchersFragment());
		}

		if (requestCode == LOCK_REQUEST_CODE_ACCOUNTS && resultCode == RESULT_OK) {
			AuthenticateUtils.getInstance(BottomNavigationActivity.this).enableBiometricForCurrentSession(false);
			getBottomNavigationById().setCurrentItem(INDEX_ACCOUNT);
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
	public void badgeCount() {
		getViewModel().getCartSummary().execute();
		getViewModel().getVoucherCount().execute();
		getViewModel().getMessageResponse().execute();
	}

	@Override
	public void identifyTokenValidationAPI() {
		if (!SessionUtilities.getInstance().isUserAuthenticated()) {
			getGlobalState().setDetermineLocationPopUpEnabled(true);
			ScreenManager.presentSSOSignin(BottomNavigationActivity.this);
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

	public void onSignOut() {
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
		setCloseFromListEnabled(true);
		setShoppingListItemCount(count);
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
					startActivity(openCartActivity);
					overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
				}
			}
		}
	}

	// show toast after slideUpPanel closed
	public void setCloseFromListEnabled(boolean closeFromListEnabled) {
		this.closeFromListEnabled = closeFromListEnabled;
	}

	public boolean closeFromListEnabled() {
		return closeFromListEnabled;
	}

	public void navigateToList(int listItemCount) {
		setSingleOrMultipleItemSelector(true);
		closeSlideUpPanel();
	}

	public void setShoppingListItemCount(int shoppingListItemCount) {
		this.shoppingListItemCount = shoppingListItemCount;
	}

	public int getShoppingListItemCount() {
		return shoppingListItemCount;
	}

	public void setSingleOrMultipleItemSelector(boolean singleOrMultipleItemSelector) {
		this.singleOrMultipleItemSelector = singleOrMultipleItemSelector;
	}

	public boolean singleOrMultipleItemSelector() {
		return singleOrMultipleItemSelector;
	}

	public void updateStatusBarColor(String color) {// Color must be in hexadecimal fromat
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(Color.parseColor(color));
		}
	}

	public Fragment getCurrentFragment() {
		return mNavController.getCurrentFrag();
	}

}