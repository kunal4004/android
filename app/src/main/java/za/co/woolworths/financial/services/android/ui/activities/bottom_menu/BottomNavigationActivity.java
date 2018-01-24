package za.co.woolworths.financial.services.android.ui.activities.bottom_menu;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.NestedScrollView;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

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
import za.co.woolworths.financial.services.android.ui.base.BaseActivity;
import za.co.woolworths.financial.services.android.ui.fragments.MyAccountsFragment;
import za.co.woolworths.financial.services.android.ui.fragments.product.category.CategoryFragment;
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DetailFragment;
import za.co.woolworths.financial.services.android.ui.fragments.shop.CartFragment;
import za.co.woolworths.financial.services.android.ui.fragments.wreward.WRewardsFragment;
import za.co.woolworths.financial.services.android.ui.fragments.wtoday.WTodayFragment;
import za.co.woolworths.financial.services.android.ui.views.NestedScrollableViewHelper;
import za.co.woolworths.financial.services.android.ui.views.SlidingUpPanelLayout;
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

	private BottomNavigationViewModel bottomNavigationViewModel;
	private FragNavController mNavController;
	private final CompositeDisposable mDisposables = new CompositeDisposable();

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
						getBottomNavigationById().setCurrentItem(index);
					}
				})
				.build();

		renderUI();
	}

	@Override
	public void renderUI() {
		Utils.updateStatusBarBackground(this);
		setActionBar();
		hideToolbar();
		bottomNavigationViewModel = ViewModelProviders.of(this).get(BottomNavigationViewModel.class);
		bottomNavigationViewModel.setNavigator(this);
		bottomNavConfig();
		addBadge(2, 3);
		addBadge(3, 17);
		addBadge(3, 100);
		getBottomNavigationById().setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
		slideUpPanelListener();

		mDisposables.add(WoolworthsApplication.getInstance()
				.bus()
				.toObservable()
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Consumer<Object>() {
					@Override
					public void accept(Object object) throws Exception {
						if (object instanceof BusStation) {

						}
					}
				}));
	}

	@Override
	public void bottomNavConfig() {
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
		DetailFragment gridFragment = new DetailFragment();
		Gson gson = new Gson();
		String strProductList = gson.toJson(productList);
		Bundle bundle = new Bundle();
		bundle.putString("strProductList", strProductList);
		bundle.putString("strProductCategory", productName);
		gridFragment.setArguments(bundle);
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.fragment_bottom_container, gridFragment).commit();
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
					//hideToolbar();
					mNavController.switchTab(INDEX_TODAY);
					return true;

				case R.id.navigation_shop:
					//slideUpBottomView();
					//hideToolbar();
					statusBarColor(R.color.recent_search_bg);
					mNavController.switchTab(INDEX_SHOP);
					Utils.showOneTimePopup(BottomNavigationActivity.this);
					return true;

				case R.id.navigation_cart:
					setToolbarTitle(getString(R.string.bottom_title_cart));
					mNavController.switchTab(INDEX_CART);
					showToolbar();
					return true;

				case R.id.navigation_reward:
					setToolbarTitle(getString(R.string.nav_item_wrewards));
					mNavController.switchTab(INDEX_REWARD);
					showToolbar();
					return true;

				case R.id.navigation_account:
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
				return new CartFragment();
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
}
