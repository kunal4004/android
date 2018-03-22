package za.co.woolworths.financial.services.android.ui.fragments.product.grid;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
	private GridLayoutManager mRecyclerViewLayoutManager;
	private int totalItemCount;
	private int lastVisibleItem;
	private boolean isLoading = true;
	private int visibleThreshold = 5;

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
		RelativeLayout relNoConnectionLayout = getViewDataBinding().incNoConnectionHandler.noConnectionLayout;
		assert getViewDataBinding().incNoConnectionHandler != null;
		mErrorHandlerView = new ErrorHandlerView(getActivity(), relNoConnectionLayout);
		mErrorHandlerView.setMargin(relNoConnectionLayout, 0, 0, 0, 0);
		setTitle();
		startProductRequest();
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
			if (!listContainHeader()) {
				ProductList headerProduct = new ProductList();
				headerProduct.viewTypeHeader = true;
				headerProduct.numberOfItems = getViewModel().getNumItemsInTotal();
				mProductList.add(0, headerProduct);
			}
			bindRecyclerViewWithUI(mProductList);
		} else if (productLists.size() == 1) {
			onGridItemSelected(productLists.get(0));
			popFragment();
		} else {
			if (listContainFooter()) {
				removeFooter();
			}
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
	public void bindRecyclerViewWithUI(final List<ProductList> productList) {
		this.mProductList = productList;
		if (!listContainHeader()) {
			ProductList headerProduct = new ProductList();
			headerProduct.viewTypeHeader = true;
			headerProduct.numberOfItems = getViewModel().getNumItemsInTotal();
			mProductList.add(0, headerProduct);
		}

		mProductAdapter = new ProductViewListAdapter(getActivity(), mProductList, this);
		mRecyclerViewLayoutManager = new GridLayoutManager(getActivity(), 2);
		// Set up a GridLayoutManager to change the SpanSize of the header and footer
		mRecyclerViewLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
			@Override
			public int getSpanSize(int position) {
				//header should have span size of 2, and regular item should have span size of 1
				return (mProductList.get(position).viewTypeHeader || mProductList.get(position).viewTypeFooter) ? 2 : 1;
			}
		});
		getViewDataBinding().productList.setLayoutManager(mRecyclerViewLayoutManager);
		getViewDataBinding().productList.setAdapter(mProductAdapter);
		getViewDataBinding().productList.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				totalItemCount = mRecyclerViewLayoutManager.getItemCount();
				lastVisibleItem = mRecyclerViewLayoutManager.findLastVisibleItemPosition();
				//loadData(dy);
			}
		});
	}

	private void loadData(int dy) {
		if (isLoading && !getViewModel().isLastPage()) {
			if (dy > 0) { //check for scroll down
				if (totalItemCount <= (lastVisibleItem + visibleThreshold)) {
					int Total = getViewModel().getNumItemsInTotal() + Utils.PAGE_SIZE;
					int start = mProductList.size();
					int end = start + Utils.PAGE_SIZE;
					isLoading = (Total > end);
					if (!isLoading) {
						return;
					}
					if (!listContainFooter()) {
						ProductList footerItem = new ProductList();
						footerItem.viewTypeFooter = true;
						mProductList.add(footerItem);
						mProductAdapter.notifyItemInserted(mProductList.size() - 1);
					}
					startProductRequest();
				}
			}
		}
	}

	private boolean listContainFooter() {
		try {
			for (ProductList pl : mProductList) {
				if (pl.viewTypeFooter) {
					return true;
				}
			}
		} catch (Exception ignored) {
		}
		return false;
	}

	private void removeFooter() {
		int index = 0;
		for (ProductList pl : mProductList) {
			if (pl.viewTypeFooter) {
				mProductList.remove(pl);
				mProductAdapter.notifyItemRemoved(index);
				return;
			}
			index++;
		}
	}

	private boolean listContainHeader() {
		for (ProductList pl : mProductList) {
			if (pl.viewTypeHeader) {
				return true;
			}
		}
		return false;
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
		try {
			if (listContainFooter()) {
				removeFooter();
			}
		} catch (Exception ex) {
			Log.e("containFooter", ex.getMessage());
		}
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
		if (!isLoadMore) {
			showView(mProgressLimitStart);
			mProgressLimitStart.bringToFront();
		}
	}

	@Override
	public void onLoadComplete(boolean isLoadMore) {
		getViewModel().setIsLoading(false);
		if (!isLoadMore) {
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
