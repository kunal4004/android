package za.co.woolworths.financial.services.android.ui.fragments.product.grid;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
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

import java.util.ArrayList;
import java.util.List;

import io.reactivex.functions.Consumer;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.ShoppingList;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse;
import za.co.woolworths.financial.services.android.models.service.event.ProductState;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity;
import za.co.woolworths.financial.services.android.ui.activities.product.ProductSearchActivity;
import za.co.woolworths.financial.services.android.ui.adapters.ProductViewListAdapter;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.ShoppingListFragment;
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.listitems.ShoppingListItemsFragment;
import za.co.woolworths.financial.services.android.ui.views.actionsheet.SingleButtonDialogFragment;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.Utils;

import static za.co.woolworths.financial.services.android.models.service.event.ProductState.OPEN_GET_LIST_SCREEN;

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
	private int lastVisibleItem;
	int totalItemCount;
	private boolean isLoading;

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

		Activity activity = getActivity();
		if (activity != null) {
			BottomNavigationActivity bottomNavigationActivity = (BottomNavigationActivity) activity;
			bottomNavigationActivity.getToolbar().setNavigationOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					popFragment();
				}
			});
		}

		mProgressLimitStart = getViewDataBinding().incCenteredProgress.progressCreditLimit;
		RelativeLayout relNoConnectionLayout = getViewDataBinding().incNoConnectionHandler.noConnectionLayout;
		assert getViewDataBinding().incNoConnectionHandler != null;
		mErrorHandlerView = new ErrorHandlerView(getActivity(), relNoConnectionLayout);
		mErrorHandlerView.setMargin(relNoConnectionLayout, 0, 0, 0, 0);
		setTitle();
		startProductRequest();
		getViewDataBinding().incNoConnectionHandler.btnRetry.setOnClickListener(this);

		observableOn(new Consumer() {
			@Override
			public void accept(Object object) throws Exception {
				if (object instanceof ProductState) {
					ProductState productState = (ProductState) object;
					switch (productState.getState()) {
						case OPEN_GET_LIST_SCREEN:
							List<ShoppingList> newList = new ArrayList<>();
							List<ShoppingList> shoppingList = getGlobalState().getShoppingListRequest();
							if (shoppingList != null) {
								for (ShoppingList shopList : shoppingList) {
									if (shopList.viewIsSelected) {
										newList.add(shopList);
									}
								}
							}
							int shoppingListSize = newList.size();
							if (shoppingListSize == 1) {
								getBottomNavigator().hideBottomNavigationMenu();
								ShoppingList shop = newList.get(0);
								Bundle bundle = new Bundle();
								bundle.putString("listId", shop.listId);
								bundle.putString("listName", shop.listName);
								ShoppingListItemsFragment shoppingListItemsFragment = new ShoppingListItemsFragment();
								shoppingListItemsFragment.setArguments(bundle);
								pushFragmentSlideUp(shoppingListItemsFragment);
							} else if (shoppingListSize > 1) {
								Bundle bundle = new Bundle();
								ShoppingListsResponse shoppingListsResponse = new ShoppingListsResponse();
								bundle.putString("ShoppingList", Utils.objectToJson(shoppingListsResponse));
								ShoppingListFragment shoppingListFragment = new ShoppingListFragment();
								shoppingListFragment.setArguments(bundle);
								pushFragmentSlideUp(shoppingListFragment);
							}

							break;

						default:
							break;
					}
				}
			}
		});
	}

	private void setTitle() {
		if (isEmpty(mSearchProduct)) {
			setTitle(mSubCategoryName);
		} else {
			setTitle(mSearchProduct);
		}
	}

	@Override
	public void onLoadProductSuccess(final List<ProductList> productLists, boolean loadMoreData) {
		if (mProductList == null) {
			mProductList = new ArrayList<>();
		}
		if (productLists.isEmpty()) {
			if (!listContainHeader()) {
				ProductList headerProduct = new ProductList();
				headerProduct.viewTypeHeader = true;
				headerProduct.numberOfItems = getViewModel().getNumItemsInTotal();
				mProductList.add(0, headerProduct);
			}
			bindRecyclerViewWithUI(mProductList);
		} else if (productLists.size() == 1) {
			getBottomNavigator().popFragmentNoAnim();
			getBottomNavigator().openProductDetailFragment(mSubCategoryName, productLists.get(0));

		} else {
			hideFooterView();
			if (!loadMoreData) {
				bindRecyclerViewWithUI(productLists);
			} else {
				loadMoreData(productLists);
			}
		}

	}

	@Override
	public void unhandledResponseCode(Response response) {
		Activity activity = getActivity();
		if (activity == null) return;
		if (response.desc == null) return;
		hideFooterView();
		FragmentManager fm = ((AppCompatActivity) activity).getSupportFragmentManager();
		// check if dialog is being displayed
		if (hasOpenedDialogs((AppCompatActivity) activity)) return;

		// show dialog
		SingleButtonDialogFragment singleButtonDialogFragment = SingleButtonDialogFragment.newInstance(response.desc);
		singleButtonDialogFragment.show(fm, SingleButtonDialogFragment.class.getSimpleName());

	}

	private boolean hasOpenedDialogs(AppCompatActivity activity) {
		List<Fragment> fragments = activity.getSupportFragmentManager().getFragments();
		if (fragments != null) {
			for (Fragment fragment : fragments) {
				if (fragment instanceof DialogFragment) {
					return true;
				}
			}
		}

		return false;
	}

	private void hideFooterView() {
		if (listContainFooter())
			removeFooter();
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
		final RecyclerView rcvProductList = getViewDataBinding().productList;
		rcvProductList.setLayoutManager(mRecyclerViewLayoutManager);
		rcvProductList.setAdapter(mProductAdapter);
		rcvProductList.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				totalItemCount = mRecyclerViewLayoutManager.getItemCount();
				lastVisibleItem = mRecyclerViewLayoutManager.findLastVisibleItemPosition();

				// Detect scrolling up
				if (dy > 0)
					loadData();
			}
		});
	}

	private void loadData() {
		int visibleThreshold = 5;
		if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
			if (getViewModel().productIsLoading())
				return;
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
		if (mProductList != null) {
			for (ProductList pl : mProductList) {
				if (pl.viewTypeHeader) {
					return true;
				}
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
	public void onGridItemSelected(final ProductList productList) {
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
			hideFooterView();
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
