package za.co.woolworths.financial.services.android.ui.activities.bottom_menu;

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
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;
import com.awfs.coordination.databinding.ActivityBottomNavigationBinding;
import com.google.gson.Gson;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.service.event.BusStation;
import za.co.woolworths.financial.services.android.ui.activities.product.shop.ShoppingListActivity;
import za.co.woolworths.financial.services.android.ui.base.BaseActivity;
import za.co.woolworths.financial.services.android.ui.fragments.account.MyAccountsFragment;
import za.co.woolworths.financial.services.android.ui.fragments.product.category.CategoryFragment;
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DetailFragment;
import za.co.woolworths.financial.services.android.ui.fragments.product.grid.GridFragment;
import za.co.woolworths.financial.services.android.ui.fragments.shop.ShopFragment;
import za.co.woolworths.financial.services.android.ui.fragments.wreward.base.WRewardsFragment;
import za.co.woolworths.financial.services.android.ui.fragments.wtoday.WTodayFragment;
import za.co.woolworths.financial.services.android.ui.views.NestedScrollableViewHelper;
import za.co.woolworths.financial.services.android.ui.views.SlidingUpPanelLayout;
import za.co.woolworths.financial.services.android.ui.views.StatusBarUtils;
import za.co.woolworths.financial.services.android.ui.views.WBottomNavigationView;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.frag_nav.FragNavController;
import za.co.woolworths.financial.services.android.util.frag_nav.FragNavSwitchController;
import za.co.woolworths.financial.services.android.util.frag_nav.FragNavTransactionOptions;
import za.co.woolworths.financial.services.android.util.frag_nav.tab_history.FragNavTabHistoryController;

public class BottomNavigationActivity extends BaseActivity<ActivityBottomNavigationBinding, BottomNavigationViewModel> implements BottomNavigator, FragNavController.TransactionListener, FragNavController.RootFragmentListener {

	public static final int INDEX_TODAY = FragNavController.TAB1;
	public static final int INDEX_SHOP = FragNavController.TAB2;
	public static final int INDEX_CART = FragNavController.TAB3;
	public static final int INDEX_REWARD = FragNavController.TAB4;
	public static final int INDEX_ACCOUNT = FragNavController.TAB5;
	public static Toolbar mToolbar;

	private final CompositeDisposable mDisposables = new CompositeDisposable();

	private BottomNavigationViewModel bottomNavigationViewModel;
	private FragNavController mNavController;

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
		if (mNavController != null) {
			mNavController.onSaveInstanceState(outState);
		}
	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mNavController = FragNavController.newBuilder(savedInstanceState, getSupportFragmentManager(), R.id.frag_container)
				.transactionListener(this)
				.rootFragmentListener(this, 5)
				.popStrategy(FragNavTabHistoryController.UNIQUE_TAB_HISTORY)
				.switchController(new FragNavSwitchController() {
					@Override
					public void switchTab(int index, FragNavTransactionOptions transactionOptions) {
						if (index != 2) {
							getBottomNavigationById().setCurrentItem(index);
						}
					}
				})
				.build();

		renderUI();

		mDisposables.add(WoolworthsApplication.getInstance()
				.bus()
				.toObservable()
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Consumer<Object>() {
					@Override
					public void accept(Object object) throws Exception {
						String searchProduct = ((BusStation) object).getSearchProductBrand();
						if (!TextUtils.isEmpty((searchProduct))) {
							GridFragment gridFragment = new GridFragment();
							Bundle bundle = new Bundle();
							bundle.putString("sub_category_id", "categoryId");
							bundle.putString("sub_category_name", "categoryName");
							bundle.putString("str_search_product", searchProduct);
							gridFragment.setArguments(bundle);
							pushFragment(gridFragment);
						}
					}
				}));
	}

	@Override
	public void renderUI() {
		//Utils.updateStatusBarBackground(this);
		mToolbar = getToolbar();
		setActionBar();
		hideToolbar();
		bottomNavigationViewModel = ViewModelProviders.of(this).get(BottomNavigationViewModel.class);
		bottomNavigationViewModel.setNavigator(this);
		bottomNavConfig();
//		addBadge(2, 3);
//		addBadge(3, 17);
//		addBadge(3, 100);
		getBottomNavigationById().setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
		slideUpPanelListener();
	}

	@SuppressLint("RestrictedApi")
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
	public void pushFragment(Fragment fragment) {
		if (mNavController != null) {
			mNavController.pushFragment(fragment);
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
			switch (item.getItemId()) {
				case R.id.navigation_today:
					setToolbarBackgroundColor(R.color.white);
					mNavController.switchTab(INDEX_TODAY);
					return true;

				case R.id.navigation_shop:
					mNavController.switchTab(INDEX_SHOP);
					Utils.showOneTimePopup(BottomNavigationActivity.this);
					return true;

				case R.id.navigation_cart:
					Toast.makeText(BottomNavigationActivity.this, "add", Toast.LENGTH_SHORT).show();
					Intent openCardActivity = new Intent(BottomNavigationActivity.this, ShoppingListActivity.class);
					startActivity(openCardActivity);
					overridePendingTransition(0, 0);
					return false;

				case R.id.navigation_reward:
					setToolbarBackgroundColor(R.color.white);
					setToolbarTitle(getString(R.string.nav_item_wrewards));
					mNavController.switchTab(INDEX_REWARD);
					showToolbar();
					return true;

				case R.id.navigation_account:
					setToolbarBackgroundColor(R.color.white);
					setToolbarTitle(getString(R.string.nav_item_accounts));
					mNavController.switchTab(INDEX_ACCOUNT);
					showToolbar();
					return true;
			}
			return false;
		}
	};

	@Override
	public void onBackPressed() {
		if (!mNavController.popFragment()) {
			mNavController.popFragment();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				mNavController.popFragment();
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
	public void onFragmentTransaction(Fragment fragment, FragNavController.TransactionType transactionType) {
		// If we have a backstack, show the back button
		if (getSupportActionBar() != null && mNavController != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(!mNavController.isRootFragment());
		}
	}

	@Override
	public Fragment getRootFragment(int index) {
		switch (index) {
			case INDEX_TODAY:
				return new WTodayFragment();
			case INDEX_SHOP:
				return new CategoryFragment();
			case INDEX_CART:
				return new ShopFragment();
			case INDEX_REWARD:
				return new WRewardsFragment();
			case INDEX_ACCOUNT:
				return new MyAccountsFragment();
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
	}

	@Override
	public void showBottomNavigationMenu() {
		showView(getBottomNavigationById());
	}

	@Override
	public void addRelativeLayoutAlignTopRule() {
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getViewDataBinding().fragContainer.getLayoutParams();
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		getViewDataBinding().fragContainer.setLayoutParams(params);
	}

	@Override
	public void removeRelativeLayoutTopRule() {
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getViewDataBinding().fragContainer.getLayoutParams();
		params.removeRule(RelativeLayout.ALIGN_PARENT_TOP);
		getViewDataBinding().fragContainer.setLayoutParams(params);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (mNavController.getCurrentFrag() instanceof WRewardsFragment) {
			mNavController.getCurrentFrag().onActivityResult(requestCode, resultCode, data);
		}

		if (mNavController.getCurrentFrag() instanceof MyAccountsFragment) {
			mNavController.getCurrentFrag().onActivityResult(requestCode, resultCode, data);
		}
	}
}
