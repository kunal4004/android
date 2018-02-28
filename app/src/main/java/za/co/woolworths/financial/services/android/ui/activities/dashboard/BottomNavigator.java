package za.co.woolworths.financial.services.android.ui.activities.dashboard;

import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;

import java.util.ArrayList;

import za.co.woolworths.financial.services.android.models.dto.CartSummary;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
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

	void showBackNavigationIcon(boolean visibility);

	void setTitle(String title);

	void slideUpBottomView();

	void slideUpPanelListener();

	void openProductDetailFragment(String productName, ProductList productList);

	void scrollableViewHelper(NestedScrollView nestedScrollView);

	void showStatusBar();

	void hideStatusBar();

	void fadeOutToolbar(int color);

	void pushFragment(Fragment fragment);

	void hideBottomNavigationMenu();

	void showBottomNavigationMenu();

	void displayToolbar();

	void removeToolbar();

	void setUpRuntimePermission();

	PermissionUtils getRuntimePermission();

	ArrayList<String> getPermissionType(String type);

	void popFragment();

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
}
