package za.co.woolworths.financial.services.android.ui.fragments.product.sub_category;

import android.content.Context;
import android.databinding.ObservableBoolean;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.SubCategories;
import za.co.woolworths.financial.services.android.models.dto.SubCategory;
import za.co.woolworths.financial.services.android.models.rest.product.ProductSubCategoryRequest;
import za.co.woolworths.financial.services.android.ui.base.BaseViewModel;
import za.co.woolworths.financial.services.android.ui.fragments.product.grid.GridFragment;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.frag_nav.FragNavTransactionOptions;
import za.co.woolworths.financial.services.android.util.rx.SchedulerProvider;

public class SubCategoryViewModel extends BaseViewModel<SubCategoryNavigator> {

	private ProductSubCategoryRequest mProductSubCategoryRequest;

	public SubCategoryViewModel() {
		super();
	}

	public SubCategoryViewModel(SchedulerProvider schedulerProvider) {
		super(schedulerProvider);
	}

	public void executeSubCategory(Context context, String category_id) {
		mProductSubCategoryRequest = subCategoryRequest(context, category_id);
		mProductSubCategoryRequest.execute();
	}

	private ProductSubCategoryRequest subCategoryRequest(Context context, String category_id) {
		getNavigator().onLoad();
		return new ProductSubCategoryRequest(context, category_id, new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				SubCategories subCategories = (SubCategories) object;
				switch (subCategories.httpCode) {
					case 200:
						getNavigator().bindSubCategoryResult(subCategories.subCategories);
						break;

					default:
						Response response = subCategories.response;
						if (response != null) {
							getNavigator().unhandledResponseHandler(response);
						}
						break;
				}
				getNavigator().onLoadComplete();
			}

			@Override
			public void onFailure(String e) {
				getNavigator().onLoadComplete();
				getNavigator().onFailureResponse(e);
			}
		});
	}

	public ObservableBoolean getLoading() {
		return getIsLoading();
	}

	public void cancelRequest() {
		cancelRequest(mProductSubCategoryRequest);
	}

	public Fragment enterNextFragment(SubCategory subCategory) {
		if (subCategory.hasChildren) {
			// drill down of categories
			SubCategoryFragment subCategoryFragment = new SubCategoryFragment();
			Bundle bundle = new Bundle();
			bundle.putString("root_category_id", subCategory.categoryId);
			bundle.putString("root_category_name", subCategory.categoryName);
			bundle.putInt("catStep", 0);
			subCategoryFragment.setArguments(bundle);
			return subCategoryFragment;
		} else {
			GridFragment gridFragment = new GridFragment();
			Bundle bundle = new Bundle();
			bundle.putString("sub_category_id", subCategory.categoryId);
			bundle.putString("sub_category_name", subCategory.categoryName);
			gridFragment.setArguments(bundle);
			return gridFragment;
		}
	}
}
