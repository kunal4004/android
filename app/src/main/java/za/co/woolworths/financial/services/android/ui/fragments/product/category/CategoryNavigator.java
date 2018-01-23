package za.co.woolworths.financial.services.android.ui.fragments.product.category;

import android.view.View;
import android.widget.LinearLayout;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.RootCategory;

public interface CategoryNavigator {
	void renderUI();

	void navigateToBarcode(View view);

	void navigateToProductSearch(View view);

	void checkCameraPermission();

	void toolbarState(boolean visibility);

	void onRetryConnectionClicked();

	void bindCategory(List<RootCategory> object);

	void bindViewWithUI(List<RootCategory> rootCategories, LinearLayout llAddView);

	void unhandledResponseCode(Response response);

	void onCategoryItemClicked(RootCategory rootCategory);

	int searchContainerHeight();

	void failureResponseHandler(String e);
}
