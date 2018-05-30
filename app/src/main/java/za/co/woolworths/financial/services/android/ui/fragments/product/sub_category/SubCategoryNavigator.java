package za.co.woolworths.financial.services.android.ui.fragments.product.sub_category;

import android.view.View;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.SubCategory;

public interface SubCategoryNavigator {
	void bindSubCategoryResult(List<SubCategory> subCategoryList);

	void unhandledResponseHandler(Response response);

	void onFailureResponse(String e);

	void onLoad();

	void onLoadComplete();

	void onItemClick(SubCategory subCategory);
}
