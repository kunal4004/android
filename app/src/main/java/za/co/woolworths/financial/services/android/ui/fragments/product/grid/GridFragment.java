package za.co.woolworths.financial.services.android.ui.fragments.product.grid;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.GridLayoutManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;
import com.awfs.coordination.databinding.GridLayoutBinding;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.ui.activities.product.ProductSearchActivity;
import za.co.woolworths.financial.services.android.ui.adapters.ProductViewListAdapter;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.Utils;

public class GridFragment extends BaseFragment<GridLayoutBinding, GridViewModel> implements GridNavigator, View.OnClickListener {

	private GridViewModel mGridViewModel;
	private ErrorHandlerView mErrorHandlerView;
	private String mSubCategoryId;
	private String mSubCategoryName;
	private String mSearchProduct;
	private ProductViewListAdapter mProductAdapter;
	private List<ProductList> mProductList;
	private ProgressBar mProgressLimitStart;
	private RelativeLayout mRelLoadMoreProduct;
	private GridLayoutManager mRecyclerViewLayoutManager;

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
			mSearchProduct = bundle.getString("str_search_product");
		}
		setProductBody();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		showToolbar();
		showBackNavigationIcon(true);
		setToolbarBackgroundDrawable(R.drawable.appbar_background);
		mProgressLimitStart = getViewDataBinding().incCenteredProgress.progressCreditLimit;
		mRelLoadMoreProduct = getViewDataBinding().relLoadMoreProduct;
		RelativeLayout relNoConnectionLayout = getViewDataBinding().incNoConnectionHandler.noConnectionLayout;
		assert getViewDataBinding().incNoConnectionHandler != null;
		mErrorHandlerView = new ErrorHandlerView(getActivity(), relNoConnectionLayout);
		mErrorHandlerView.setMargin(relNoConnectionLayout, 0, 0, 0, 0);
		setTitle();
		startProductRequest();
		onBottomReached();
		getViewDataBinding().incNoConnectionHandler.btnRetry.setOnClickListener(this);
	}

	private void setTitle() {
		if (isEmpty(mSearchProduct)) {
			setTitle(mSubCategoryName);
		} else {
			setTitle(mSearchProduct);
		}
	}

	@Override
	public void onLoadProductSuccess(List<ProductList> productLists, boolean loadMoreData) {
		if (productLists.isEmpty()) {

		} else if (productLists.size() == 1) {
			onGridItemSelected(productLists.get(0));
			popFragment();
		} else {
			setTotalNumberOfItem();
			if (!loadMoreData) {
				bindRecyclerViewWithUI(productLists);
			} else {
				loadMoreData(productLists);
			}
		}
	}

	@Override
	public void unhandledResponseCode(Response response) {
	}

	@Override
	public void failureResponseHandler(final String e) {
		Activity activity = getBaseActivity();
		if (activity != null) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mErrorHandlerView.networkFailureHandler(e);
				}
			});
		}
	}

	@Override
	public void cancelAPIRequest() {
		if (mGridViewModel != null) {
			mGridViewModel.cancelRequest(mGridViewModel.getLoadProductRequest());
			mGridViewModel.cancelRequest(mGridViewModel.getSearchProductRequest());
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
		mRecyclerViewLayoutManager = new GridLayoutManager(getActivity(), 2);
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
		if (!isEmpty(mSearchProduct)) {
			mSubCategoryName = mSearchProduct;
		}
		getBottomNavigator().openProductDetailFragment(mSubCategoryName, productList);
	}

	@Override
	public void onBottomReached() {
		final NestedScrollView scroll = getViewDataBinding().scrollProduct;

		scroll.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
			@Override
			public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
				if (v.getChildAt(v.getChildCount() - 1) != null) {
					if ((scrollY >= (v.getChildAt(v.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight())) &&
							scrollY > oldScrollY) {
						//code to fetch more data for endless scrolling
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								try {
									int visibleItemCount = mRecyclerViewLayoutManager.getChildCount();
									int totalItemCount = mRecyclerViewLayoutManager.getItemCount();
									int firstVisibleItemPosition = mRecyclerViewLayoutManager.findFirstVisibleItemPosition();
									if (!getViewModel().isLoading() && !getViewModel().isLastPage()) {
										if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
												&& firstVisibleItemPosition >= 0
												&& totalItemCount >= Utils.PAGE_SIZE) {
											if (mProductList.size() < getViewModel().getNumItemsInTotal()) {
												startProductRequest();
											}
										}
									} else {
										onLoadComplete(true);
									}
								} catch (NullPointerException ignored) {
								}
							}
						});
					}
				}
			}
		});
	}

	@Override
	public void startProductRequest() {
		if (isEmpty(mSearchProduct)) {
			getViewModel().executeLoadProduct(getActivity(), getViewModel().getProductRequestBody());
		} else {
			getViewModel().executeSearchProduct(getActivity(), getViewModel().getProductRequestBody());
		}
	}

	@Override
	public void loadMoreData(List<ProductList> productLists) {
		int actualSize = mProductList.size() + 1;
		mProductList.addAll(productLists);
		int sizeOfList = mProductList.size();
		mProductAdapter.notifyItemChanged(actualSize, sizeOfList);
		getViewModel().canLoadMore(getViewModel().getNumItemsInTotal(), sizeOfList);
	}

	@Override
	public void setProductBody() {
		if (isEmpty(mSearchProduct)) {
			getViewModel().setProductRequestBody(false, mSubCategoryId);
		} else {
			getViewModel().setProductRequestBody(mSearchProduct, false);
		}
	}

	@Override
	public void onLoadStart(boolean isLoadMore) {
		getViewModel().setIsLoading(true);
		if (isLoadMore) {
			showView(mRelLoadMoreProduct);
		} else {
			showView(mProgressLimitStart);
			mProgressLimitStart.bringToFront();
		}
	}

	@Override
	public void onLoadComplete(boolean isLoadMore) {
		getViewModel().setIsLoading(false);
		if (isLoadMore) {
			hideView(mRelLoadMoreProduct);
		} else {
			hideView(mProgressLimitStart);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		menu.clear();
		inflater.inflate(R.menu.drill_down_category_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_drill_search:
				Intent openSearchActivity = new Intent(getBaseActivity(), ProductSearchActivity.class);
				startActivity(openSearchActivity);
				getBaseActivity().overridePendingTransition(0, 0);
				return true;
			default:
				break;
		}
		return false;
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.btnRetry:
				if (isNetworkConnected()) {
					mErrorHandlerView.hideErrorHandler();
					startProductRequest();
				}
				break;
		}
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {
			showToolbar();
			showBackNavigationIcon(true);
			setToolbarBackgroundDrawable(R.drawable.appbar_background);
			setTitle();
		}
	}
}
