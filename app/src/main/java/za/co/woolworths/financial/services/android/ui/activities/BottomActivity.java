package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.core.widget.NestedScrollView;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

import com.awfs.coordination.R;
import com.google.gson.Gson;

import java.util.ArrayList;

import za.co.woolworths.financial.services.android.models.dto.ProductDetails;
import za.co.woolworths.financial.services.android.models.service.event.ProductState;
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.ProductDetailFragment;
import za.co.woolworths.financial.services.android.ui.views.NestedScrollableViewHelper;
import za.co.woolworths.financial.services.android.ui.views.SlidingUpPanelLayout;
import za.co.woolworths.financial.services.android.util.PermissionResultCallback;
import za.co.woolworths.financial.services.android.util.PermissionUtils;
import za.co.woolworths.financial.services.android.util.ScreenManager;
import za.co.woolworths.financial.services.android.util.Utils;

import static za.co.woolworths.financial.services.android.models.service.event.ProductState.SHOW_ADDED_TO_SHOPPING_LIST_TOAST;
import static za.co.woolworths.financial.services.android.util.Utils.sendBus;

public abstract class BottomActivity extends AppCompatActivity implements PermissionResultCallback {

	public static final int SLIDE_UP_COLLAPSE_REQUEST_CODE = 13;
	public static final int SLIDE_UP_COLLAPSE_RESULT_CODE = 12345;

	private int currentSection;
	private int shoppingListItemCount;
	private boolean closeFromListEnabled;
	private boolean singleOrMultipleItemSelector;
	private PermissionUtils permissionUtils;
	private ArrayList<String> permissions;
	public static int NAVIGATE_TO_SHOPPING_LIST_FRAGMENT = 3333;

	protected abstract int getLayoutResourceId();

	protected abstract void initUI();

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getLayoutResourceId());
		renderUI();
		initUI();
	}

	public SlidingUpPanelLayout getSlidingLayout() {
		return findViewById(R.id.slideUpPanel);
	}

	public void closeSlideUpPanel() {
		getSlidingLayout().setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
	}

	public void renderUI() {
		slideUpPanelListener();
		setUpRuntimePermission();
	}

	public void statusBarColor(int color) {
		Utils.updateStatusBarBackground(this, color);
	}

	public void statusBarColor(int color, boolean enableDecor) {
		Utils.updateStatusBarBackground(this, color, enableDecor);
	}


	public void slideUpBottomView() {
		getSlidingLayout().setAnchorPoint(1.0f);
		getSlidingLayout().setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
	}

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
						// show toast on search result fragment after add to list
						// activates when user access pdp page from list section
						if (closeFromListEnabled()) {
							sendBus(new ProductState(getShoppingListItemCount(), SHOW_ADDED_TO_SHOPPING_LIST_TOAST));
							setCloseFromListEnabled(false);
						}

						// open single list or multiple list view on collapsed
						if (singleOrMultipleItemSelector()) {
							setResult(NAVIGATE_TO_SHOPPING_LIST_FRAGMENT);
							setSingleOrMultipleItemSelector(false);
							finish();
							overridePendingTransition(0, 0);
						}
						onActivityResult(SLIDE_UP_COLLAPSE_REQUEST_CODE, SLIDE_UP_COLLAPSE_RESULT_CODE, null);

						if (getBottomFragmentById() instanceof ProductDetailFragment) {
							try {
								getBottomFragmentById().onDetach();
							} catch (NullPointerException ex) {
							}
						}

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

	public void openProductDetailFragment(String productName, ProductDetails productList) {
		Gson gson = new Gson();
		String strProductList = gson.toJson(productList);
		Bundle bundle = new Bundle();
		bundle.putString("strProductList", strProductList);
		bundle.putString("strProductCategory", productName);
		ScreenManager.presentProductDetails(BottomActivity.this,bundle);
	}

	public void scrollableViewHelper(NestedScrollView nsv) {
		getSlidingLayout().setScrollableViewHelper(new NestedScrollableViewHelper(nsv));
	}


	public void showStatusBar() {
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	public void hideStatusBar() {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}


	public void setUpRuntimePermission() {
		permissionUtils = new PermissionUtils(this, this);
		permissions = new ArrayList<>();
	}

	public PermissionUtils getRuntimePermission() {
		return permissionUtils;
	}

	public ArrayList<String> getPermissionType(String type) {
		if (!permissions.isEmpty())
			permissions.clear();
		permissions.add(type);
		return permissions;
	}

	public void closeSlideUpPanelFromList(int count) {
		setCloseFromListEnabled(true);
		setShoppingListItemCount(count);
		closeSlideUpPanel();
	}

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
	}

	public void PartialPermissionGranted(int request_code, ArrayList<String> granted_permissions) {

	}

	public void PermissionDenied(int request_code) {

	}

	public void NeverAskAgain(int request_code) {

	}

	public void setCurrentSection(int currentSection) {
		this.currentSection = currentSection;
	}

	public int getCurrentSection() {
		return currentSection;
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

	public boolean singleOrMultipleItemSelector() {
		return singleOrMultipleItemSelector;
	}

	public void setSingleOrMultipleItemSelector(boolean singleOrMultipleItemSelector) {
		this.singleOrMultipleItemSelector = singleOrMultipleItemSelector;
	}

	public void setShoppingListItemCount(int shoppingListItemCount) {
		this.shoppingListItemCount = shoppingListItemCount;
	}

	public int getShoppingListItemCount() {
		return shoppingListItemCount;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		// redirects to utils
		permissionUtils.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	private Fragment getBottomFragmentById() {
		FragmentManager fm = getSupportFragmentManager();
		return fm.findFragmentById(R.id.fragment_bottom_container);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		//TODO: Explain where this is coming from.
	}

}
