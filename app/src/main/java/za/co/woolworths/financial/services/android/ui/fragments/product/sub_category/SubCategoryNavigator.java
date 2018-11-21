package za.co.woolworths.financial.services.android.ui.fragments.product.sub_category;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.SubCategory;
import za.co.woolworths.financial.services.android.util.expand.ParentSubCategoryViewHolder;

public interface SubCategoryNavigator {
	void bindSubCategoryResult(List<SubCategory> subCategoryList);

	void unhandledResponseHandler(Response response);

	void onFailureResponse(String e);

	void onLoad();

	void onLoadComplete();

	void onChildItemClicked(SubCategory subCategory);

	void noConnectionDetected();

	void retrieveChildItem(ParentSubCategoryViewHolder parentSubCategoryViewHolder, SubCategory subCategory, int adapterPosition);

	void onCloseIconPressed();
}
