package za.co.woolworths.financial.services.android.ui.fragments.product.grid;


import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.GridLayoutManager;
import android.view.View;
import android.view.ViewTreeObserver;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;
import com.awfs.coordination.databinding.GridLayoutBinding;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.ui.activities.bottom_menu.BottomNavigationActivity;
import za.co.woolworths.financial.services.android.ui.adapters.ProductViewListAdapter;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;

public class GridFragment extends BaseFragment<GridLayoutBinding, GridViewModel> implements GridNavigator {

	private GridViewModel mGridViewModel;
	private ErrorHandlerView mErrorHandlerView;
	private String mSubCategoryId;
	private String mSubCategoryName;
	private ProductViewListAdapter mProductAdapter;
	private List<ProductList> mProductList;

	@Override
	public GridViewModel getViewModel() {
		return mGridViewModel;
	}

	@Override
	public int getBindingVariable() {
		return BR.viewModel;
	}

	@Override
	public int getLayoutId() {
		return R.layout.grid_layout;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		mGridViewModel = ViewModelProviders.of(this).get(GridViewModel.class);
		mGridViewModel.setNavigator(this);

		Bundle bundle = this.getArguments();
		if (bundle != null) {
			mSubCategoryId = bundle.getString("sub_category_id");
			mSubCategoryName = bundle.getString("sub_category_name");
		}

		getViewModel().setProductRequestBody(false, mSubCategoryId);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mErrorHandlerView = new ErrorHandlerView(getActivity()
				, getViewDataBinding().incNoConnectionHandler.noConnectionLayout);
		setTitle(mSubCategoryName);
		startProductRequest();
		onBottomReached();
	}

	@Override
	public void onLoadProductSuccess(List<ProductList> productLists, boolean loadMoreData) {
		setTotalNumberOfItem();
		if (!loadMoreData) {
			bindRecyclerViewWithUI(productLists);
		} else {
			loadMoreData(productLists);
		}
	}

	@Override
	public void unhandledResponseCode(Response response) {

	}

	@Override
	public void failureResponseHandler(String e) {
		mErrorHandlerView.networkFailureHandler(e);
	}

	@Override
	public void onLoadStart() {
		mErrorHandlerView.hideErrorHandler();
	}

	@Override
	public void cancelAPIRequest() {
		if (mGridViewModel != null) {
			mGridViewModel.cancelRequest(mGridViewModel.getLoadProductRequest());
		}
	}

	@Override
	public void setTotalNumberOfItem() {
		getViewDataBinding().numberOfItem.setText(String.valueOf(getViewModel().getNumItemsInTotal()));
	}

	@Override
	public void bindRecyclerViewWithUI(List<ProductList> productList) {
		this.mProductList = productList;
		mProductAdapter = new ProductViewListAdapter(getActivity(), mProductList, this);
		GridLayoutManager mRecyclerViewLayoutManager = new GridLayoutManager(getActivity(), 2);
		getViewDataBinding().productList.setLayoutManager(mRecyclerViewLayoutManager);
		getViewDataBinding().productList.setNestedScrollingEnabled(false);
		getViewDataBinding().productList.setAdapter(mProductAdapter);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		cancelAPIRequest();
	}

	@Override
	public void onGridItemSelected(ProductList productList) {
		((BottomNavigationActivity) getActivity()).getViewModel().getNavigator().openProductDetailFragment(mSubCategoryName, productList);
	}

	@Override
	public void onBottomReached() {
		final NestedScrollView scroll = getViewDataBinding().scrollProduct;
		scroll.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
			@Override
			public void onScrollChanged() {
				View view = scroll.getChildAt(scroll.getChildCount() - 1);

				int reachedBottom = (view.getBottom() - (scroll.getHeight() + scroll
						.getScrollY()));

				if (reachedBottom == 0) {
					if (getViewModel().getLoadMoreData()) {
						startProductRequest();
					}
				}
			}
		});
	}

	@Override
	public void startProductRequest() {
		getViewModel().executeLoadProduct(getActivity(), getViewModel().getProductRequestBody());
	}

	@Override
	public void loadMoreData(List<ProductList> productLists) {
		int actualSize = mProductList.size() + 1;
		mProductList.addAll(productLists);
		int sizeOfList = mProductList.size();
		mProductAdapter.notifyItemRangeChanged(actualSize, sizeOfList);
		getViewModel().canLoadMore(getViewModel().getNumItemsInTotal(), sizeOfList);
	}

	@Override
	public void showProgressBarCentered() {
		showView(getViewDataBinding().rlCenteredProgressBar);
	}

	@Override
	public void dismissProgressBarCentered() {
		hideView(getViewDataBinding().rlCenteredProgressBar);
	}

	@Override
	public void showProgressBarAtBottom() {
		showView(getViewDataBinding().pbLoadMore);
	}

	@Override
	public void dismissProgressBarAtBottom() {
		hideView(getViewDataBinding().pbLoadMore);
	}
}
