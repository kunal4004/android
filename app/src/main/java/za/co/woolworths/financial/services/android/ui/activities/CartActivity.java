package za.co.woolworths.financial.services.android.ui.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.awfs.coordination.R;

import java.util.ArrayList;

import za.co.woolworths.financial.services.android.models.dto.CartSummary;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator;
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DetailFragment;
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.CartFragment;
import za.co.woolworths.financial.services.android.ui.views.NestedScrollableViewHelper;
import za.co.woolworths.financial.services.android.ui.views.SlidingUpPanelLayout;
import za.co.woolworths.financial.services.android.ui.views.WBottomNavigationView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.PermissionUtils;
import za.co.woolworths.financial.services.android.util.Utils;

public class CartActivity extends AppCompatActivity implements BottomNavigator, View.OnClickListener {

	private WTextView btnEditCart;
	private WTextView btnClearCart;
	private ImageView btnCloseCart;
	private CartFragment cartFragment;
	private SlidingUpPanelLayout mSlideUpPanel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cart);
		Utils.updateStatusBarBackground(this);

		mSlideUpPanel = findViewById(R.id.slideUpPanel);

		Toolbar toolbar = findViewById(R.id.toolbar);
		btnCloseCart = findViewById(R.id.btnCloseCart);
		btnCloseCart.setOnClickListener(this);

		btnEditCart = findViewById(R.id.btnEditCart);
		btnEditCart.setOnClickListener(this);

		btnClearCart = findViewById(R.id.btnClearCart);
		btnClearCart.setOnClickListener(this);

		btnEditCart = findViewById(R.id.btnEditCart);

		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		getSupportActionBar().setTitle(null);
		cartFragment = new CartFragment();
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction()
				.replace(R.id.content_frame, cartFragment).commit();


		slideUpPanelListener();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnEditCart:
				toggleCartMode();
				break;
			case R.id.btnCloseCart:
				finishActivity();
				break;
			case R.id.btnClearCart:
				cartFragment.clearAllCartItems();
				break;
		}
	}

	private void toggleCartMode() {
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
			super.onBackPressed();
		}
		overridePendingTransition(R.anim.stay, R.anim.slide_down_anim);
	}

	public void finishActivity() {
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

	public void slideUpPanelListener() {
		mSlideUpPanel.setFadeOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mSlideUpPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
			}
		});
		mSlideUpPanel.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
			@Override
			public void onPanelSlide(View panel, float slideOffset) {
				if (slideOffset == 0.0) {
					mSlideUpPanel.setAnchorPoint(1.0f);
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
							Fragment fragmentById = fm.findFragmentById(R.id.bottom_Fragment);
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
	public void fadeOutToolbar(int color) {

	}

	@Override
	public void pushFragment(Fragment fragment) {

	}

	@Override
	public void hideBottomNavigationMenu() {

	}

	@Override
	public void showBottomNavigationMenu() {

	}

	@Override
	public void displayToolbar() {

	}

	@Override
	public void removeToolbar() {

	}

	@Override
	public void setUpRuntimePermission() {

	}

	@Override
	public PermissionUtils getRuntimePermission() {
		return null;
	}

	@Override
	public ArrayList<String> getPermissionType(String type) {
		return null;
	}

	@Override
	public void popFragment() {

	}

	@Override
	public void setSelectedIconPosition(int position) {

	}

	@Override
	public void switchTab(int number) {

	}

	@Override
	public void clearStack() {

	}

	@Override
	public void cartSummaryAPI() {

	}

	@Override
	public void updateCartSummaryCount(CartSummary cartSummary) {

	}

	@Override
	public void identifyTokenValidationAPI() {

	}

	@Override
	public void cartSummaryInvalidToken() {

	}

	@Override
	public int getCurrentStackIndex() {
		return 0;
	}

	@Override
	public void updateVoucherCount(int size) {

	}

	@Override
	public void updateMessageCount(int unreadCount) {

	}

	public void scrollableViewHelper(NestedScrollView nsv) {
		mSlideUpPanel.setScrollableViewHelper(new NestedScrollableViewHelper(nsv));
	}

	@Override
	public WBottomNavigationView getBottomNavigationById() {
		return null;
	}

	@Override
	public SlidingUpPanelLayout getSlidingLayout() {
		return mSlideUpPanel;
	}

	public void closeSlideUpPanel() {
		mSlideUpPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
	}

	@Override
	public void renderUI() {

	}

	@Override
	public void bottomNavConfig() {

	}

	@Override
	public void addBadge(int position, int number) {

	}

	@Override
	public void statusBarColor(int color) {

	}

	@Override
	public void showBackNavigationIcon(boolean visibility) {

	}

	@Override
	public void setTitle(String title) {

	}

	@Override
	public void slideUpBottomView() {
		mSlideUpPanel.setAnchorPoint(1.0f);
		mSlideUpPanel.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
	}
}
