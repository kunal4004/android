package za.co.woolworths.financial.services.android.ui.activities.dashboard;

import androidx.fragment.app.Fragment;
import androidx.core.widget.NestedScrollView;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;

import za.co.woolworths.financial.services.android.models.dto.CartSummary;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.ProductView;
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams;
import za.co.woolworths.financial.services.android.ui.views.SlidingUpPanelLayout;
import za.co.woolworths.financial.services.android.ui.views.WBottomNavigationView;
import za.co.woolworths.financial.services.android.util.PermissionUtils;

public interface BottomNavigator {

	WBottomNavigationView getBottomNavigationById();

	SlidingUpPanelLayout getSlidingLayout();

	void closeSlideUpPanel();

	void renderUI();

	void bottomNavConfig();

	void addBadge(int position, int number);

	void statusBarColor(int color);

	void statusBarColor(int color, boolean enableDecor);

	void showBackNavigationIcon(boolean visibility);

	void setTitle(String title);

	void setTitle(String title, int color);

	void slideUpBottomView();

	void slideUpPanelListener();

	void openProductDetailFragment(String productName, ProductList productList);

	void scrollableViewHelper(NestedScrollView nestedScrollView);

	void showStatusBar();

	void hideStatusBar();

	void fadeOutToolbar(int color);

	void hideBottomNavigationMenu();

	void showBottomNavigationMenu();

	void displayToolbar();

	void removeToolbar();

	void setUpRuntimePermission();

	PermissionUtils getRuntimePermission();

	ArrayList<String> getPermissionType(String type);

	void pushFragment(Fragment fragment);

	void pushFragment(Fragment fragment, boolean state);

	void pushFragmentSlideUp(Fragment fragment);

	void pushFragmentSlideUp(Fragment fragment, boolean state);

	void pushFragmentNoAnim(Fragment fragment);

	void popFragment();

	void popFragmentNoAnim();

	void popFragmentSlideDown();

	void setSelectedIconPosition(int position);

	void switchTab(int number);

	void clearStack();

	void cartSummaryAPI();

	void updateCartSummaryCount(CartSummary cartSummary);

	void identifyTokenValidationAPI();

	void cartSummaryInvalidToken();

	int getCurrentStackIndex();

	void updateVoucherCount(int size);

	void updateMessageCount(int unreadCount);

	void badgeCount();

	Toolbar toolbar();

	void closeSlideUpPanelFromList(int count);

	void setHomeAsUpIndicator(int drawable);

	void lockDrawerFragment();

	void unLockDrawerFragment();

	void setUpDrawerFragment(ProductView productsResponse, ProductsRequestParams productsRequestParams);

	void closeDrawerFragment();

	void openDrawerFragment();

	void addDrawerFragment();

	void onRefined(String navigationState);

	void onResetFilter();
}
