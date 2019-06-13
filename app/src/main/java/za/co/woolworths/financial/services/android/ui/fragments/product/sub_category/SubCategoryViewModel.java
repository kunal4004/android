package za.co.woolworths.financial.services.android.ui.fragments.product.sub_category;

import android.databinding.ObservableBoolean;

import retrofit2.Call;
import za.co.woolworths.financial.services.android.contracts.RequestListener;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.SubCategories;
import za.co.woolworths.financial.services.android.models.network.CompletionHandler;
import za.co.woolworths.financial.services.android.models.network.OneAppService;
import za.co.woolworths.financial.services.android.ui.base.BaseViewModel;
import za.co.woolworths.financial.services.android.util.rx.SchedulerProvider;

public class SubCategoryViewModel extends BaseViewModel<SubCategoryNavigator> {

	private boolean childItem;

	private Call<SubCategories> mProductSubCategoryRequest;

	public SubCategoryViewModel() {
		super();
	}

	public SubCategoryViewModel(SchedulerProvider schedulerProvider) {
		super(schedulerProvider);
	}

	public void executeSubCategory(String category_id) {
		mProductSubCategoryRequest = OneAppService.INSTANCE.getSubCategory(category_id);
		mProductSubCategoryRequest.enqueue(new CompletionHandler<>(new RequestListener<SubCategories>() {
			@Override
			public void onSuccess(SubCategories subCategories) {
				switch (subCategories.httpCode) {
					case 200:
						getNavigator().bindSubCategoryResult(subCategories.subCategories);
						getNavigator().onLoadComplete();
						break;
					default:
						Response response = subCategories.response;
						if (response != null) {
							getNavigator().unhandledResponseHandler(response);
						}
						break;
				}
			}

			@Override
			public void onFailure(Throwable error) {
				getNavigator().onLoadComplete();
				getNavigator().onFailureResponse(error.toString());
			}
		},SubCategories.class));
	}

	public ObservableBoolean getLoading() {
		return getIsLoading();
	}

	public void cancelRequest() {
		if (mProductSubCategoryRequest!=null && mProductSubCategoryRequest.isCanceled()){
			mProductSubCategoryRequest.cancel();
		}
	}

	public void setChildItem(boolean childItem) {
		this.childItem = childItem;
	}

	public boolean childItem() {
		return childItem;
	}
}
