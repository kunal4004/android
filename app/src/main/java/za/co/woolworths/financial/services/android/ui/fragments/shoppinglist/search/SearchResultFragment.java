package za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.search;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;
import com.awfs.coordination.databinding.GridLayoutBinding;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.ui.adapters.ShoppingListSearchResultAdapter;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.Utils;

public class SearchResultFragment extends BaseFragment<GridLayoutBinding, SearchResultViewModel> implements SearchResultNavigator, View.OnClickListener {


	private SearchResultViewModel mGridViewModel;
	private ErrorHandlerView mErrorHandlerView;
	private ShoppingListSearchResultAdapter mProductAdapter;
	private List<ProductList> mProductList;
	private ProgressBar mProgressLimitStart;
	private LinearLayoutManager mRecyclerViewLayoutManager;
	private String mSearchText;
	private int totalItemCount;
	private int lastVisibleItem;
	private int visibleThreshold = 5;
	private boolean isLoading;

	@Override
	public SearchResultViewModel getViewModel() {
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
		mGridViewModel = ViewModelProviders.of(this).get(SearchResultViewModel.class);
		mGridViewModel.setNavigator(this);

		Bundle bundle = this.getArguments();
		if (bundle != null) {
			mSearchText = bundle.getString("search_text");
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
		onBottomReached();
		getViewDataBinding().incNoConnectionHandler.btnRetry.setOnClickListener(this);
	}

	private void setTitle() {
		if (mSearchText != null)
			setTitle(mSearchText);
	}

	@Override
	public void onLoadProductSuccess(List<ProductList> productLists, boolean loadMoreData) {
		if (productLists != null) {
			if (productLists.size() == 1) {
				onGridItemSelected(productLists.get(0));
				popFragment();
			} else {
				if (!loadMoreData) {
					bindRecyclerViewWithUI(productLists);
				} else {
					loadMoreData(productLists);
				}
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
			mGridViewModel.cancelRequest(mGridViewModel.getSearchProductRequest());
		}
	}

	@Override
	public void bindRecyclerViewWithUI(List<ProductList> productList) {
		this.mProductList = productList;

		if (!listContainHeader()) {
			ProductList headerProduct = new ProductList();
			headerProduct.viewTypeHeader = true;
			headerProduct.numberOfItems = getViewModel().getNumItemsInTotal();
			productList.add(0, headerProduct);
		}

		mProductAdapter = new ShoppingListSearchResultAdapter(mProductList, this);
		mRecyclerViewLayoutManager = new LinearLayoutManager(getActivity());
		getViewDataBinding().productList.setLayoutManager(mRecyclerViewLayoutManager);
		getViewDataBinding().productList.setNestedScrollingEnabled(false);
		getViewDataBinding().productList.setAdapter(mProductAdapter);
		getViewDataBinding().productList.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				totalItemCount = mRecyclerViewLayoutManager.getItemCount();
				lastVisibleItem = mRecyclerViewLayoutManager.findLastVisibleItemPosition();
				loadData();
			}
		});
	}

	private void loadData() {
		if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
			int Total = getViewModel().getNumItemsInTotal() + Utils.PAGE_SIZE;
			int start = mProductList.size();
			int end = start + Utils.PAGE_SIZE;
			isLoading = (Total < end);
			if (isLoading) {
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

	private boolean listContainFooter() {
		for (ProductList pl : mProductList) {
			if (pl.viewTypeFooter) {
				return true;
			}
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
		getBottomNavigator().openProductDetailFragment(mSearchText, productList);
	}

	@Override
	public void onBottomReached() {
	}

	@Override
	public void startProductRequest() {
		getViewModel().executeSearchProduct(getActivity(), getViewModel().getProductRequestBody());
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
		getViewModel().setProductRequestBody(mSearchText, false);
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
		if (listContainFooter()) {
			removeFooter();
		}
		getViewModel().setIsLoading(false);
		if (!isLoadMore) {
			hideView(mProgressLimitStart);
		}
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
