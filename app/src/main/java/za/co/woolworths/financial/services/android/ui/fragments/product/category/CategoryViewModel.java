package za.co.woolworths.financial.services.android.ui.fragments.product.category;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.LinearLayout;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.RootCategories;
import za.co.woolworths.financial.services.android.models.dto.RootCategory;
import za.co.woolworths.financial.services.android.models.rest.product.ProductCategoryRequest;
import za.co.woolworths.financial.services.android.ui.base.BaseViewModel;
import za.co.woolworths.financial.services.android.ui.fragments.product.sub_category.SubCategoryFragment;
import za.co.woolworths.financial.services.android.ui.fragments.product.grid.GridFragment;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.rx.SchedulerProvider;

public class CategoryViewModel extends BaseViewModel<CategoryNavigator> {

	public CategoryViewModel() {
		super();
	}

	public CategoryViewModel(SchedulerProvider schedulerProvider) {
		super(schedulerProvider);
	}

	public ProductCategoryRequest categoryRequest() {
		setIsLoading(true);
		return new ProductCategoryRequest(new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				RootCategories rootCategories = (RootCategories) object;
				switch (rootCategories.httpCode) {
					case 200:
						List<RootCategory> rootCategoryList = rootCategories.rootCategories;
						if (rootCategoryList != null) {
							getNavigator().bindCategory(rootCategoryList);
						}
						break;

					default:
						if (rootCategories.response != null) {
							getNavigator().unhandledResponseCode(rootCategories.response);
						}
						break;
				}
				setIsLoading(false);
			}

			@Override
			public void onFailure(String e) {
				getNavigator().failureResponseHandler(e);
				setIsLoading(false);
			}
		});
	}

	public Fragment enterNextFragment(RootCategory rootCategory, boolean value) {
		SubCategoryFragment drillDownCategoryFragment = new SubCategoryFragment();
		if (rootCategory.hasChildren) {
			// drill down of categories
			Bundle bundle = new Bundle();
			bundle.putString("ROOT_CATEGORY", Utils.toJson(rootCategory));
			drillDownCategoryFragment.setArguments(bundle);
			return drillDownCategoryFragment;
		} else {
			// navigate to product grid
			GridFragment gridFragment = new GridFragment();
			Bundle bundle = new Bundle();
			bundle.putString("sub_category_id", rootCategory.dimValId);
			bundle.putString("sub_category_name", rootCategory.categoryName);
			gridFragment.setArguments(bundle);
			return gridFragment;
		}
	}
}
