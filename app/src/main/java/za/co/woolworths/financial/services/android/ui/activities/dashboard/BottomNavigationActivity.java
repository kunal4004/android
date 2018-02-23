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
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;
import com.awfs.coordination.databinding.ActivityBottomNavigationBinding;
import com.google.gson.Gson;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import za.co.woolworths.financial.services.android.models.dto.CartSummary;
import za.co.woolworths.financial.services.android.models.dto.CartSummaryResponse;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.service.event.AuthenticationState;
import za.co.woolworths.financial.services.android.models.service.event.LoadState;
import za.co.woolworths.financial.services.android.ui.activities.CartActivity;
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity;
import za.co.woolworths.financial.services.android.ui.base.BaseActivity;
import za.co.woolworths.financial.services.android.ui.base.SavedInstanceFragment;
import za.co.woolworths.financial.services.android.ui.fragments.account.MyAccountsFragment;
import za.co.woolworths.financial.services.android.ui.fragments.product.category.CategoryFragment;
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DetailFragment;
import za.co.woolworths.financial.services.android.ui.fragments.product.grid.GridFragment;
import za.co.woolworths.financial.services.android.ui.fragments.wreward.base.WRewardsFragment;
import za.co.woolworths.financial.services.android.ui.fragments.wtoday.WTodayFragment;
import za.co.woolworths.financial.services.android.ui.views.NestedScrollableViewHelper;
import za.co.woolworths.financial.services.android.ui.views.SlidingUpPanelLayout;
import za.co.woolworths.financial.services.android.ui.views.WBottomNavigationView;
import za.co.woolworths.financial.services.android.util.MultiClickPreventer;
import za.co.woolworths.financial.services.android.util.NotificationUtils;
import za.co.woolworths.financial.services.android.util.PermissionResultCallback;
import za.co.woolworths.financial.services.android.util.PermissionUtils;
import za.co.woolworths.financial.services.android.util.ScreenManager;
import za.co.woolworths.financial.services.android.util.SessionManager;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.nav.FragNavController;
import za.co.woolworths.financial.services.android.util.nav.FragNavSwitchController;
import za.co.woolworths.financial.services.android.util.nav.FragNavTransactionOptions;
import za.co.woolworths.financial.services.android.util.nav.tabhistory.FragNavTabHistoryController;

import static za.co.woolworths.financial.services.android.util.SessionManager.RELOAD_REWARD;

public class BottomNavigationActivity extends BaseActivity<ActivityBottomNavigationBinding, BottomNavigationViewModel> implements BottomNavigator, FragNavController.TransactionListener, FragNavController.RootFragmentListener, PermissionResultCallback {

	public static final int INDEX_TODAY = FragNavController.TAB1;
	public static final int INDEX_PRODUCT = FragNavController.TAB2;
	public static final int INDEX_CART = FragNavController.TAB3;
	public static final int INDEX_REWARD = FragNavController.TAB4;
	public static final int INDEX_ACCOUNT = FragNavController.TAB5;
	public static final int OPEN_CART_REQUEST = 12346;
	public static Toolbar mToolbar;

	private final CompositeDisposable mDisposables = new CompositeDisposable();
	private PermissionUtils permissionUtils;
	private ArrayList<String> permissions;
	private BottomNavigationViewModel bottomNavigationViewModel;
	private FragNavController mNavController;
	private WRewardsFragment wRewardsFragment;
	private MyAccountsFragment myAccountsFragment;
	private String TAG = this.getClass().getSimpleName();
	private Bundle mBundle;
	private int currentSection;

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
		super.onRestoreInstanceState(SavedInstanceFragment.getInstance(getFragmentManager()).popData());
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
		mDisposables.add(woolworthsApplication()
				.bus()
				.toObservable()
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Consumer<Object>() {
					@Override
					public void accept(Object object) throws Exception {
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
								addBadge(INDEX_REWARD, 0);
								addBadge(INDEX_ACCOUNT, 0);
								ScreenManager.presentSSOLogout(BottomNavigationActivity.this);
							}
						} else if (object instanceof CartSummaryResponse) {
							CartSummaryResponse cartSummaryResponse = (CartSummaryResponse) object;
							if (cartSummaryResponse != null) {
								// product item successfully added to cart
								cartSummaryAPI();
								closeSlideUpPanel();
								Utils.customToastMessage(BottomNavigationActivity.this);
							}
						}
					}
				}));

		SessionManager sessionManager = new SessionManager(BottomNavigationActivity.this);
		sessionManager.authenticationState();
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
		mToolbar = getToolbar();
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
	public void showBackNavigationIcon(boolean visibility) {
		setBackNavigationIcon(visibility);
	}

	@Override
	public void setTitle(String title) {
		setToolbarTitle(title);
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
							FragmentManager fm = getSupportFragmentManager();
							Fragment fragmentById = fm.findFragmentById(R.id.fragment_bottom_container);
							//detach detail fragment
							if (fragmentById instanceof DetailFragment) {
								DetailFragment detailFragment = (DetailFragment) fragmentById;
								detailFragment.onDetach();
							}
						} catch (ClassCastException e) {
							// not that fragment
						}

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
		DetailFragment detailFragment = new DetailFragment();
		Gson gson = new Gson();
		String strProductList = gson.toJson(productList);
		Bundle bundle = new Bundle();
		bundle.putString("strProductList", strProductList);
		bundle.putString("strProductCategory", productName);
		detailFragment.setArguments(bundle);
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.fragment_bottom_container, detailFragment).commit();
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
					.allowStateLoss(true)
					.customAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
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
					currentSection = R.id.navigation_today;
					setToolbarBackgroundColor(R.color.white);
					switchTab(INDEX_TODAY);
					hideToolbar();
					return true;

				case R.id.navigation_shop:
					currentSection = R.id.navigation_shop;
					switchTab(INDEX_PRODUCT);
					Utils.showOneTimePopup(BottomNavigationActivity.this);
					return true;

				case R.id.navigation_cart:
					currentSection = R.id.navigation_cart;
					identifyTokenValidationAPI();
					return false;

				case R.id.navigation_reward:
					currentSection = R.id.navigation_reward;
					Utils.sendBus(new SessionManager(RELOAD_REWARD));
					setToolbarBackgroundColor(R.color.white);
					switchTab(INDEX_REWARD);
					return true;

				case R.id.navigation_account:
					currentSection = R.id.navigation_account;
					setToolbarBackgroundColor(R.color.white);
					switchTab(INDEX_ACCOUNT);
					return true;
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

				case R.id.navigation_shop:
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
		if (!mNavController.isRootFragment()) {
			mNavController.popFragment(new FragNavTransactionOptions.Builder().customAnimations(R.anim.slide_in_from_left, R.anim.slide_out_to_right).build());
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
	public void onDestroy() {
		super.onDestroy();
		if (!mDisposables.isDisposed()) {
			mDisposables.clear();
		}
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
	public void updateCartSummaryBadgeCount(CartSummary cartSummary) {
		addBadge(INDEX_CART, cartSummary.totalItemsCount);
	}

	@Override
	public void PermissionGranted(int request_code) {
		woolworthsApplication()
				.bus()
				.send(new DetailFragment());
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
		if (requestCode == OPEN_CART_REQUEST) {
			if (resultCode == RESULT_OK) {
				getBottomNavigationById().setCurrentItem(INDEX_PRODUCT);
				return;
			}
		}

		// prevent firing reward and account api on every activity resume
		if (resultCode == SSOActivity.SSOActivityResult.SUCCESS.rawValue()) {
			switch (currentSection) {
				case R.id.navigation_cart:
					openCartActivity();
					break;
			}
		}

		switch (getBottomNavigationById().getCurrentItem()) {
			case 1:
			case 0:
				break;
			case 2:
				break;
			default:
				if (wRewardsFragment != null)
					wRewardsFragment.onActivityResult(requestCode, resultCode, data);
				if (myAccountsFragment != null)
					myAccountsFragment.onActivityResult(requestCode, resultCode, data);
				break;
		}
	}

	@Override
	public void identifyTokenValidationAPI() {
		if (isEmpty(Utils.getSessionToken(BottomNavigationActivity.this))) {
			getGlobalState().setDetermineLocationPopUpEnabled(true);
			ScreenManager.presentSSOSignin(BottomNavigationActivity.this);
		} else {
			openCartActivity();
		}
	}

	@Override
	public void onSessionTokenValid() {
		openCartActivity();
	}

	@Override
	public void onSessionTokenExpired(Response response) {
		ScreenManager.presentSSOSignin(BottomNavigationActivity.this);
	}

	@Override
	public void otherHttpCode(Response response) {

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
}