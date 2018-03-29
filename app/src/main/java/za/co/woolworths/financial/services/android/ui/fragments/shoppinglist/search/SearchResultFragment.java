package za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.search;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;
import com.awfs.coordination.databinding.GridLayoutBinding;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.functions.Consumer;
import za.co.woolworths.financial.services.android.models.dto.AddToListRequest;
import za.co.woolworths.financial.services.android.models.dto.OtherSkus;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItem;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.models.dto.WProduct;
import za.co.woolworths.financial.services.android.models.dto.WProductDetail;
import za.co.woolworths.financial.services.android.models.rest.product.GetProductDetail;
import za.co.woolworths.financial.services.android.models.rest.product.ProductRequest;
import za.co.woolworths.financial.services.android.models.service.event.ProductState;
import za.co.woolworths.financial.services.android.models.service.event.ShopState;
import za.co.woolworths.financial.services.android.ui.activities.ConfirmColorSizeActivity;
import za.co.woolworths.financial.services.android.ui.adapters.SearchResultShopAdapter;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.Utils;

import static za.co.woolworths.financial.services.android.ui.fragments.product.detail.ProductDetailFragment.INDEX_SEARCH_FROM_LIST;

public class SearchResultFragment extends BaseFragment<GridLayoutBinding, SearchResultViewModel> implements SearchResultNavigator, View.OnClickListener {

	private SearchResultViewModel mGridViewModel;
	private ErrorHandlerView mErrorHandlerView;
	private SearchResultShopAdapter mProductAdapter;
	private List<ProductList> mProductList;
	private ProgressBar mProgressLimitStart;
	private LinearLayoutManager mRecyclerViewLayoutManager;
	private String mSearchText;
	private int totalItemCount;
	private int lastVisibleItem;
	private boolean isLoading;
	private String mListId;
	private GetProductDetail getProductDetail;
	private ProductList mSelectedProduct;
	private int mAddToListSize = 0;

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
			mSearchText = bundle.getString("searchTEXT");
			mListId = bundle.getString("listID");
		}
		setProductBody();
		observableOn(new Consumer() {
			@Override
			public void accept(Object object) throws Exception {
				if (object != null) {
					if (object instanceof ProductState) {
						ProductState productState = (ProductState) object;
						switch (productState.getState()) {
							case ProductState.INDEX_SEARCH_FROM_LIST:
								if (getProductAdapter() != null) {
									getProductAdapter().setSelectedSku(getSelectedProduct(), getGlobalState().getSelectedSKUId());
								}
								toggleAddToListBtn(true);
								break;

							default:
								break;
						}
					}
				}
			}
		});
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		ViewCompat.setTranslationZ(getView(), 100.f);
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
		setUpAddToListButton();
	}

	private void setUpAddToListButton() {
		getViewDataBinding().incConfirmButtonLayout.btnCheckOut.setOnClickListener(this);
		setText(getViewDataBinding().incConfirmButtonLayout.btnCheckOut, getString(R.string.add_to_list));
		toggleAddToListBtn(false);
	}

	private void setTitle() {
		if (mSearchText != null)
			setTitle(mSearchText);
	}

	@Override
	public void onLoadProductSuccess(List<ProductList> productLists, boolean loadMoreData) {
		if (productLists != null) {
			if (productLists.size() == 1) {
				onClothingTypeSelect(productLists.get(0));
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

	private void disableSelect() {
		OtherSkus otherSkus = new OtherSkus();
		otherSkus.sku = getSelectedProduct().sku;
		if (getProductAdapter() != null)
			getProductAdapter().onDeselectSKU(getSelectedProduct(), otherSkus);
	}

	private SearchResultShopAdapter getProductAdapter() {
		return mProductAdapter;
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

		mProductAdapter = new SearchResultShopAdapter(mProductList, this);
		mRecyclerViewLayoutManager = new LinearLayoutManager(getActivity());
		getViewDataBinding().productList.setLayoutManager(mRecyclerViewLayoutManager);
		getViewDataBinding().productList.setNestedScrollingEnabled(false);
		getViewDataBinding().productList.setAdapter(getProductAdapter());
		getViewDataBinding().productList.setItemAnimator(null);
		getViewDataBinding().productList.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				totalItemCount = mRecyclerViewLayoutManager.getItemCount();
				lastVisibleItem = mRecyclerViewLayoutManager.findLastVisibleItemPosition();
				//loadData();
			}
		});
	}

	private void loadData() {
		int visibleThreshold = 5;
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
				getProductAdapter().notifyItemInserted(mProductList.size() - 1);
			}
			startProductRequest();
		}
	}

	private boolean listContainFooter() {
		if (mProductList == null) return false;
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
				getProductAdapter().notifyItemRemoved(index);
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
		getProductAdapter().notifyItemChanged(actualSize, sizeOfList);
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

			case R.id.btnCheckOut:
				List<AddToListRequest> addToListRequests = new ArrayList<>();
				for (ProductList list : mProductList) {
					if (list.itemWasChecked) {
						AddToListRequest addToList = new AddToListRequest();
						addToList.setSkuID(list.sku);
						addToList.setCatalogRefId(list.sku);
						addToList.setQuantity("1");
						addToList.setGiftListId(list.productId);
						addToListRequests.add(addToList);
					}
				}
				mAddToListSize = addToListRequests.size();
				getViewModel().addToList(addToListRequests, mListId).execute();
				break;

			default:
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

	@Override
	public void onFoodTypeSelect(ProductList productList) {
		toggleAddToListBtn(true);
		getBottomNavigator().openProductDetailFragment(mSearchText, productList);
	}

	@Override
	public void onClothingTypeSelect(ProductList productList) {
		toggleAddToListBtn(true);
		getBottomNavigator().openProductDetailFragment(mSearchText, productList);
	}

	@Override
	public void minOneItemSelected(List<ProductList> prodList) {
		boolean productWasChecked = false;
		for (ProductList productList : prodList) {
			if (productList.itemWasChecked) {
				productWasChecked = true;
			}
		}
		// hide checkbox when no item selected
		if (!productWasChecked) {
			toggleAddToListBtn(false);
		}
	}

	@Override
	public void onAddToListFailure(String e) {
		Log.e("onAddToListFailure", e);
	}

	@Override
	public void onAddToListLoad() {
		;
		ProgressBar progressBar = getViewDataBinding().incConfirmButtonLayout.pbLoadingIndicator;
		WButton btnCheck0ut = getViewDataBinding().incConfirmButtonLayout.btnCheckOut;
		progressBar.setVisibility(View.VISIBLE);
		btnCheck0ut.setVisibility(View.GONE);
	}

	@Override
	public void onAddToListLoadComplete(List<ShoppingListItem> listItems) {
		ProgressBar progressBar = getViewDataBinding().incConfirmButtonLayout.pbLoadingIndicator;
		WButton btnCheck0ut = getViewDataBinding().incConfirmButtonLayout.btnCheckOut;
		progressBar.setVisibility(View.GONE);
		btnCheck0ut.setVisibility(View.VISIBLE);
		sendBus(new ShopState(listItems, mAddToListSize));
		popFragmentSlideDown();
	}

	@Override
	public void onCheckedItem(ProductList selectedProduct, boolean viewIsLoading) {
		setSelectedProduct(selectedProduct);
		if (viewIsLoading) {
			ProductRequest productRequest = new ProductRequest(selectedProduct.productId, selectedProduct.sku);
			productDetailRequest(productRequest);
		} else {
			if (getProductAdapter() != null) {
				OtherSkus otherSkus = new OtherSkus();
				otherSkus.sku = selectedProduct.sku;
				getProductAdapter().onDeselectSKU(getSelectedProduct(), otherSkus);
			}
		}
	}

	@Override
	public void onLoadStart() {
	}

	@Override
	public void responseFailureHandler(Response response) {
	}

	@Override
	public void onSuccessResponse(WProduct product) {
		getGlobalState().saveButtonClicked(INDEX_SEARCH_FROM_LIST);
		getProductAdapter().setCheckedProgressBar(getSelectedProduct());
		if (isNetworkConnected()) {
			ArrayList<OtherSkus> otherSkuList = getViewModel().getOtherSkus();
			ArrayList<OtherSkus> colorList = getViewModel().getColorList();
			ArrayList<OtherSkus> sizeList = getViewModel().getSizeList();

			WProductDetail objProduct = product.product;

			int colorSize = colorList.size();
			boolean productContainColor = colorSize > 1;
			boolean onlyOneColor = colorSize == 1;

			int sizeSize = sizeList.size();
			boolean productContainSize = sizeSize > 1;
			boolean onlyOneSize = sizeSize == 1;

			if (productContainColor) { // contains one or more color
				//show picker dialog
				twoOrMoreColorIntent(otherSkuList, colorList, objProduct);
			} else {
				if (onlyOneColor) { // contains one color only
					String color = colorList.get(0) != null ? colorList.get(0).colour : "";
					// contains more than one size
					intentSizeList(color, colorList.get(0), otherSkuList, colorList, objProduct);  // open size intent with color as filter
				} else {  // no color found
					if (productContainSize) {
						if (onlyOneSize) {
							noSizeColorIntent(TextUtils.isEmpty(sizeList.get(0).sku) ? objProduct.sku : sizeList.get(0).sku);
						} else {
							twoOrMoreSizeIntent(otherSkuList, colorList, objProduct);
						}
					} else {
						// no size found
						noSizeColorIntent(objProduct.sku);
					}
				}
			}
		}
	}

	public void twoOrMoreSizeIntent(String colour, ArrayList<OtherSkus> otherSkuList, ArrayList<OtherSkus> colorList, WProductDetail objProduct) {
		getGlobalState().setColourSKUArrayList(colorList);
		Intent mIntent = new Intent(getBaseActivity(), ConfirmColorSizeActivity.class);
		mIntent.putExtra("SELECTED_COLOUR", colour);
		mIntent.putExtra("OTHERSKU", Utils.toJson(otherSkuList));
		mIntent.putExtra("PRODUCT_HAS_COLOR", false);
		mIntent.putExtra("PRODUCT_HAS_SIZE", true);
		mIntent.putExtra(ConfirmColorSizeActivity.SELECT_PAGE, "");
		mIntent.putExtra("PRODUCT_NAME", objProduct.productName);
		startActivityForResult(mIntent, WGlobalState.SYNC_FIND_IN_STORE);
		getBaseActivity().overridePendingTransition(0, 0);
	}

	private void intentSizeList(String color, OtherSkus otherSku, ArrayList<OtherSkus> otherSkuList, ArrayList<OtherSkus> colorList, WProductDetail objProduct) {
		ArrayList<OtherSkus> sizeList = getViewModel().commonSizeList(otherSku);
		int sizeListSize = sizeList.size();
		if (sizeListSize > 0) {
			if (sizeListSize == 1) {
				// one size only
				noSizeColorIntent(sizeList.get(0).sku);
			} else {
				// size > 1
				twoOrMoreSizeIntent(color, otherSkuList, sizeList, objProduct);
			}
		} else {
			// no size
			noSizeColorIntent(otherSku.sku);
		}
	}

	public void noSizeColorIntent(String mSkuId) {
		OtherSkus otherSkus = new OtherSkus();
		otherSkus.sku = mSkuId;
		getGlobalState().setSelectedSKUId(otherSkus);
		Activity activity = getActivity();
		if (activity != null) {
			switch (getGlobalState().getSaveButtonClick()) {
				default:
					Log.e("openAddIemToList", "openAddItemToList");
					break;
			}
		}
	}

	private void twoOrMoreColorIntent(ArrayList<OtherSkus> otherSkuList, ArrayList<OtherSkus> colorList, WProductDetail objProduct) {
		getGlobalState().setColourSKUArrayList(colorList);
		Intent mIntent = new Intent(getBaseActivity(), ConfirmColorSizeActivity.class);
		mIntent.putExtra("COLOR_LIST", Utils.toJson(colorList));
		mIntent.putExtra("OTHERSKU", Utils.toJson(otherSkuList));
		mIntent.putExtra("PRODUCT_HAS_COLOR", true);
		mIntent.putExtra("PRODUCT_HAS_SIZE", true);
		mIntent.putExtra(ConfirmColorSizeActivity.SELECT_PAGE, "");
		mIntent.putExtra("PRODUCT_NAME", objProduct.productName);
		startActivityForResult(mIntent, WGlobalState.SYNC_FIND_IN_STORE);
		getBaseActivity().overridePendingTransition(0, 0);
	}

	public void twoOrMoreSizeIntent(ArrayList<OtherSkus> otherSkuList, ArrayList<OtherSkus> colorList, WProductDetail objProduct) {
		getGlobalState().setColourSKUArrayList(colorList);
		Intent mIntent = new Intent(getBaseActivity(), ConfirmColorSizeActivity.class);
		mIntent.putExtra("COLOR_LIST", Utils.toJson(colorList));
		mIntent.putExtra("OTHERSKU", Utils.toJson(otherSkuList));
		mIntent.putExtra("PRODUCT_HAS_COLOR", false);
		mIntent.putExtra("PRODUCT_HAS_SIZE", true);
		mIntent.putExtra(ConfirmColorSizeActivity.SELECT_PAGE, "");
		mIntent.putExtra("PRODUCT_NAME", objProduct.productName);
		startActivityForResult(mIntent, WGlobalState.SYNC_FIND_IN_STORE);
		getBaseActivity().overridePendingTransition(0, 0);
	}

	@Override
	public void onLoadComplete() {
	}

	@Override
	public void onLoadDetailFailure(String e) {
		Activity activity = getActivity();
		if (activity != null) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					disableSelect();
				}
			});
		}
	}

	@Override
	public void onFoodTypeChecked(ProductList selectedProduct) {
		toggleAddToListBtn(true);
	}

	private void productDetailRequest(ProductRequest productRequest) {
		getProductDetail = getViewModel().getProductDetail(productRequest);
		getProductDetail.execute();
	}

	public void toggleAddToListBtn(boolean enable) {
		RelativeLayout rlAddToList = getViewDataBinding().incConfirmButtonLayout.rlCheckOut;
		WButton btnAddToList = getViewDataBinding().incConfirmButtonLayout.btnCheckOut;
		rlAddToList.setVisibility(enable ? View.VISIBLE : View.GONE);
		btnAddToList.setEnabled(enable);
	}

	public void setSelectedProduct(ProductList mSelectedProduct) {
		this.mSelectedProduct = mSelectedProduct;
	}

	public ProductList getSelectedProduct() {
		return mSelectedProduct;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		cancelRequest(getProductDetail);
	}
}
