package za.co.woolworths.financial.services.android.ui.activities.bottom_menu;

import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;

import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.VoucherResponse;
import za.co.woolworths.financial.services.android.ui.views.SlidingUpPanelLayout;
import za.co.woolworths.financial.services.android.ui.views.WBottomNavigationView;

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

	void pushFragment(Fragment fragment);

	void hideBottomNavigationMenu();

	void showBottomNavigationMenu();

	void displayToolbar();

	void removeToolbar();

	void addRelativeLayoutAlignTopRule();

	void removeRelativeLayoutTopRule();
}
