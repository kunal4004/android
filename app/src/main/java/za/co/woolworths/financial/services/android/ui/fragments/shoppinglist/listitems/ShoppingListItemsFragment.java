package za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.listitems;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;
import com.awfs.coordination.databinding.ShoppingListItemsFragmentBinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.functions.Consumer;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.AddItemToCart;
import za.co.woolworths.financial.services.android.models.dto.AddItemToCartResponse;
import za.co.woolworths.financial.services.android.models.dto.AddToCartDaTum;
import za.co.woolworths.financial.services.android.models.dto.CartSummary;
import za.co.woolworths.financial.services.android.models.dto.CartSummaryResponse;
import za.co.woolworths.financial.services.android.models.dto.DeliveryLocationHistory;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.Province;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItem;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItemsResponse;
import za.co.woolworths.financial.services.android.models.dto.SkuInventory;
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse;
import za.co.woolworths.financial.services.android.models.dto.Suburb;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.models.rest.product.GetCartSummary;
import za.co.woolworths.financial.services.android.models.rest.product.GetInventorySkusForStore;
import za.co.woolworths.financial.services.android.models.rest.product.PostAddItemToCart;
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.DeleteShoppingList;
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.DeleteShoppingListItem;
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.GetShoppingListItems;
import za.co.woolworths.financial.services.android.models.service.event.CartState;
import za.co.woolworths.financial.services.android.models.service.event.ShopState;
import za.co.woolworths.financial.services.android.ui.activities.ConfirmColorSizeActivity;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.activities.DeliveryLocationSelectionActivity;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator;
import za.co.woolworths.financial.services.android.ui.activities.product.ProductSearchActivity;
import za.co.woolworths.financial.services.android.ui.adapters.ShoppingListItemsAdapter;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.search.SearchResultFragment;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.EmptyCartView;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.MultiMap;
import za.co.woolworths.financial.services.android.util.NetworkChangeListener;
import za.co.woolworths.financial.services.android.util.ScreenManager;
import za.co.woolworths.financial.services.android.util.SessionUtilities;
import za.co.woolworths.financial.services.android.util.ToastUtils;
import za.co.woolworths.financial.services.android.util.Utils;

public class ShoppingListItemsFragment extends BaseFragment<ShoppingListItemsFragmentBinding, ShoppingListItemsViewModel> implements ShoppingListItemsNavigator, View.OnClickListener, EmptyCartView.EmptyCartInterface, NetworkChangeListener, ToastUtils.ToastInterface {
	private ShoppingListItemsViewModel shoppingListItemsViewModel;
	private String listName;
	private String listId;
	private GetShoppingListItems getShoppingListItems;
	private List<ShoppingListItem> listItems;
	private DeleteShoppingList deleteShoppingList;
	private DeleteShoppingListItem deleteShoppingListItem;
	private ShoppingListItemsAdapter shoppingListItemsAdapter;
	private MenuItem mMenuActionSearch, mMenuActionSelectAll;
	private boolean isMenuItemReadyToShow = false;
	private WTextView tvMenuSelectAll;
	private WoolworthsApplication mWoolWorthsApplication;
	private int changeQuantityItem;
	private RelativeLayout rlNoConnectionLayout;
	private ErrorHandlerView mErrorHandlerView;
	private BroadcastReceiver mConnectionBroadcast;
	private ToastUtils mToastUtils;
	private String TAG = this.getClass().getSimpleName();
	private final int DELIVERY_LOCATION_REQUEST = 2;
	private final int SUBURB_SET_RESULT = 123401;
	private GetCartSummary mCartSummary;
	private PostAddItemToCart mPostAddToCart;
	private GetInventorySkusForStore mGetInventorySkusForStore;
	private Map<String, String> mMapStoreFulFillmentKeyValue;
	private boolean errorMessageWasPopUp;
	private int REQUEST_SUBURB_CHANGE = 12345;
	private ShoppingListItem mOpenShoppingListItem;

	@Override
	public ShoppingListItemsViewModel getViewModel() {
		return shoppingListItemsViewModel;
	}

	@Override
	public int getBindingVariable() {
		return BR.viewModel;
	}

	@Override
	public int getLayoutId() {
		return R.layout.shopping_list_items_fragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		shoppingListItemsViewModel = ViewModelProviders.of(this).get(ShoppingListItemsViewModel.class);
		shoppingListItemsViewModel.setNavigator(this);
		mWoolWorthsApplication = ((WoolworthsApplication) getActivity().getApplication());
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		getBottomNavigator().hideBottomNavigationMenu();
		mToastUtils = new ToastUtils(this);
		mMapStoreFulFillmentKeyValue = new HashMap<>();
		listName = getArguments().getString("listName");
		listId = getArguments().getString("listId");
		view.findViewById(R.id.btnRetry).setOnClickListener(this);
		EmptyCartView emptyCartView = new EmptyCartView(view, this);
		emptyCartView.setView(getString(R.string.title_empty_shopping_list), getString(R.string.description_empty_shopping_list), getString(R.string.button_empty_shopping_list), R.drawable.emptyshoppinglist);
		showToolbar(listName);
		observableOn(new Consumer() {
			@Override
			public void accept(Object object) throws Exception {
				if (object != null) {
					if (object instanceof CartState) {
						CartState cartState = (CartState) object;
						int updatedQuantity = cartState.getQuantity();
						if (updatedQuantity > 0) {
							listItems.get(changeQuantityItem).userQuantity = updatedQuantity;
							listItems.get(changeQuantityItem).isSelected = true;
							shoppingListItemsAdapter.updateList(listItems);
						}
						return;
					}
					if (object instanceof ShopState) {
						ShopState shopState = (ShopState) object;
						if (!TextUtils.isEmpty(shopState.getState())) {
							SearchResultFragment searchResultFragment = new SearchResultFragment();
							Bundle bundle = new Bundle();
							bundle.putString("searchTEXT", shopState.getState());
							bundle.putString("listID", shopState.getListId());
							searchResultFragment.setArguments(bundle);
							pushFragment(searchResultFragment);
							return;
						}

						if (shopState.getUpdatedList() != null) {
							Activity activity = getActivity();
							if (activity != null) {
								BottomNavigator bottomNavigator = getBottomNavigator();
								setToast(shopState, bottomNavigator);
								closeSoftKeyboard();
								listItems = shopState.getUpdatedList();
								updateList(listItems);
								setUpView();
							}
						}
					}
				}
			}
		});

		rlNoConnectionLayout = getViewDataBinding().incConnectionLayout.noConnectionLayout;
		mErrorHandlerView = new ErrorHandlerView(getActivity(), rlNoConnectionLayout);
		mErrorHandlerView.setMargin(rlNoConnectionLayout, 0, 0, 0, 0);
		mConnectionBroadcast = Utils.connectionBroadCast(getActivity(), this);
		initList(getViewDataBinding().rcvShoppingListItems);
		initGetShoppingListItems();
		setScrollListener(getViewDataBinding().rcvShoppingListItems);
		getViewDataBinding().textProductSearch.setOnClickListener(this);
		setUpAddToCartButton();
	}

	private void setToast(ShopState shopState, BottomNavigator bottomNavigator) {
		mToastUtils.setActivity(getActivity());
		mToastUtils.setView(bottomNavigator.getBottomNavigationById());
		mToastUtils.setGravity(Gravity.BOTTOM);
		mToastUtils.setCurrentState(TAG);
		mToastUtils.setPixel(0);
		mToastUtils.setAllCapsUpperCase(true);
		mToastUtils.setView(bottomNavigator.getBottomNavigationById());
		mToastUtils.setMessage(shopState.getCount() == 1 ? shopState.getCount() + " " + getString(R.string.single_item_text) + " " + getString(R.string.added_to) : shopState.getCount() + " " + getString(R.string.multiple_item_text) + " " + getString(R.string.added_to));
		mToastUtils.setViewState(false);
		mToastUtils.setCartText(listName);
		mToastUtils.build();
	}

	private void updateList(List<ShoppingListItem> listItems) {
		setHeader();
		if (shoppingListItemsAdapter != null) {
			shoppingListItemsAdapter.updateList(listItems);
			setUpView();
		}
	}

	public void loadShoppingListItems(ShoppingListItemsResponse shoppingListItemsResponse) {
		getViewDataBinding().loadingBar.setVisibility(View.GONE);
		listItems = shoppingListItemsResponse.listItems;
		Activity activity = getActivity();
		if (activity == null) return;

		List<DeliveryLocationHistory> deliveryLocationHistories = Utils.getDeliveryLocationHistory(activity);
		if (deliveryLocationHistories.size() == 0) {
			if (listItems == null)
				listItems = new ArrayList<>();
			cancelQuantityLoad();
			updateList(listItems);
			return;
		}

		if (shoppingListInventory()) return;

		if (listItems == null)
			listItems = new ArrayList<>();
		updateList(listItems);

		/**
		 * Activated when a user click on quantity selector but no suburb was set
		 * OpenQuantitySelector automatically
		 */
		if (mOpenShoppingListItem != null) {
			for (ShoppingListItem shoppingListItem : listItems) {
				if (shoppingListItem.catalogRefId == null) continue;
				if (shoppingListItem.catalogRefId.equalsIgnoreCase(mOpenShoppingListItem.catalogRefId)) {
					mOpenShoppingListItem.quantityInStock = shoppingListItem.quantityInStock;
				}
			}

			if (mOpenShoppingListItem.quantityInStock == 0) {
				mToastUtils.setActivity(getActivity());
				mToastUtils.setView(getBottomNavigator().getBottomNavigationById());
				mToastUtils.setGravity(Gravity.BOTTOM);
				mToastUtils.setCurrentState(TAG);
				mToastUtils.setPixel(0);
				mToastUtils.setView(getBottomNavigator().getBottomNavigationById());
				mToastUtils.setMessage(activity.getResources().getString(R.string.product_unavailable_desc));
				mToastUtils.setViewState(false);
				mToastUtils.setAllCapsUpperCase(false);
				mToastUtils.setCartText("");
				mToastUtils.build();
				return;
			}
			Intent editQuantityIntent = new Intent(activity, ConfirmColorSizeActivity.class);
			editQuantityIntent.putExtra(ConfirmColorSizeActivity.SELECT_PAGE, ConfirmColorSizeActivity.QUANTITY);
			editQuantityIntent.putExtra("CART_QUANTITY_In_STOCK", mOpenShoppingListItem.quantityInStock);
			activity.startActivity(editQuantityIntent);
			activity.overridePendingTransition(0, 0);

		}
	}

	private boolean shoppingListInventory() {
		if (listItems == null) {
			setUpView();
			return true;
		}
		MultiMap<String, ShoppingListItem> multiListItem = MultiMap.create();
		for (ShoppingListItem shoppingListItem : listItems) {
			multiListItem.put(shoppingListItem.fulfillmentType, shoppingListItem);
		}

		Map<String, String> collectOtherSkuId = new HashMap<>();
		Map<String, Collection<ShoppingListItem>> collections = multiListItem.getEntries();
		for (Map.Entry<String, Collection<ShoppingListItem>> collectionEntry : collections.entrySet()) {
			Collection<ShoppingListItem> collectionEntryValue = collectionEntry.getValue();
			String fulFillmentTypeIdCollection = collectionEntry.getKey();
			List<String> skuIds = new ArrayList<>();
			for (ShoppingListItem shoppingListItem : collectionEntryValue) {
				skuIds.add(shoppingListItem.catalogRefId);
			}
			String multiSKUS = TextUtils.join("-", skuIds);
			collectOtherSkuId.put(fulFillmentTypeIdCollection, multiSKUS);
			String fulFillmentStoreId = Utils.retrieveStoreId(fulFillmentTypeIdCollection, getActivity());
			if (!TextUtils.isEmpty(fulFillmentStoreId)) {
				fulFillmentStoreId = fulFillmentStoreId.replaceAll("\"", "");
				mMapStoreFulFillmentKeyValue.put(fulFillmentTypeIdCollection, fulFillmentStoreId);
				executeGetInventoryForStore(fulFillmentStoreId, multiSKUS);
			}
		}
		return false;
	}

	private void setUpView() {
		RecyclerView rcvShoppingListItems = getViewDataBinding().rcvShoppingListItems;
		RelativeLayout rlSoppingList = getViewDataBinding().incEmptyLayout.relEmptyStateHandler;
		rlSoppingList.setVisibility(listItems == null || listItems.size() <= 1 ? View.VISIBLE : View.GONE); // 1 to exclude header
		rcvShoppingListItems.setVisibility(listItems == null || listItems.size() <= 1 ? View.GONE : View.VISIBLE);
		manageSelectAllMenuVisibility(listItems.size());
	}

	private void setHeader() {
		listItems.add(0, new ShoppingListItem());
	}

	private void initList(RecyclerView rcvShoppingListItems) {
		listItems = new ArrayList<>();
		listItems.add(new ShoppingListItem());
		shoppingListItemsAdapter = new ShoppingListItemsAdapter(listItems, this);
		LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
		mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		rcvShoppingListItems.setLayoutManager(mLayoutManager);
		rcvShoppingListItems.setAdapter(shoppingListItemsAdapter);
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {
			showToolbar(listName);
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.textProductSearch:
				openProductSearchActivity();
				break;
			case R.id.btnRetry:
				if (new ConnectionDetector().isOnline(getActivity())) {
					errorMessageWasPopUp = false;
					initGetShoppingListItems();
				}
				break;
			case R.id.btnCheckOut:
				loadCartSummary();
				break;
			default:
				break;
		}
	}

	private void openProductSearchActivity() {
		Activity activity = getActivity();
		if (activity != null) {
			Intent openProductSearchActivity = new Intent(activity, ProductSearchActivity.class);
			Bundle bundle = new Bundle();
			bundle.putString("listName", listName);
			openProductSearchActivity.putExtra("SEARCH_TEXT_HINT", getString(R.string.shopping_search_hint));
			openProductSearchActivity.putExtra("listID", listId);
			startActivity(openProductSearchActivity);
			getActivity().overridePendingTransition(R.anim.stay, R.anim.stay);
		}
	}

	@Override
	public void onShoppingListItemsResponse(ShoppingListItemsResponse shoppingListItemsResponse) {
		switch (shoppingListItemsResponse.httpCode) {
			case 200:
				loadShoppingListItems(shoppingListItemsResponse);
				break;
			case 440:
				break;
			default:
				getViewDataBinding().loadingBar.setVisibility(View.GONE);
				Activity activity = getActivity();
				if (activity == null) return;
				if (shoppingListItemsResponse.response == null) return;
				if (TextUtils.isEmpty(shoppingListItemsResponse.response.desc)) return;
				Utils.displayValidationMessage(activity, CustomPopUpWindow.MODAL_LAYOUT.ERROR, shoppingListItemsResponse.response.desc);
				break;
		}
	}

	@Override
	public void onItemSelectionChange(List<ShoppingListItem> items) {
		getViewDataBinding().incConfirmButtonLayout.rlCheckOut.setVisibility(getButtonStatus(items) ? View.VISIBLE : View.GONE);
		if (isAdded()) {
			if (items.size() > 0)
				tvMenuSelectAll.setText(getString(getSelectAllMenuVisibility(items) ? R.string.deselect_all : R.string.select_all));
			else
				mMenuActionSelectAll.setVisible(false);
		}
	}

	@Override
	public void onShoppingListItemDelete(ShoppingListItemsResponse shoppingListItemsResponse) {
		listItems = shoppingListItemsResponse.listItems;
		updateList(listItems);
	}

	@Override
	public void onItemDeleteClick(String id, String productId, String catalogRefId) {
		deleteShoppingListItem = getViewModel().deleteShoppingListItem(listId, id, productId, catalogRefId);
		deleteShoppingListItem.execute();
	}

	@Override
	public void onShoppingSearchClick() {
		openProductSearchActivity();
	}

	@Override
	public void onAddToCartPreExecute() {
		getViewDataBinding().incConfirmButtonLayout.pbLoadingIndicator.setVisibility(View.VISIBLE);
		getViewDataBinding().incConfirmButtonLayout.btnCheckOut.setVisibility(View.GONE);
	}

	@Override
	public void onAddToCartSuccess(AddItemToCartResponse addItemToCartResponse) {
		if (addItemToCartResponse.data != null) {
			List<AddToCartDaTum> addToCartDaTumList = addItemToCartResponse.data;
			AddToCartDaTum addToCartDaTum = addToCartDaTumList.get(0);
			if (addToCartDaTum != null) {
				if (addToCartDaTum.totalCommerceIteItemCount != null) {
					sendBus(new CartSummaryResponse(addItemToCartResponse));
				}
			}
		}
		popFragmentSlideDown();
	}

	@Override
	public void onSessionTokenExpired(final Response response) {
		SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE);
		final Activity activity = getBaseActivity();
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (activity != null) {
					if (response != null) {
						if (response.message != null) {
							getGlobalState().setDetermineLocationPopUpEnabled(true);
							ScreenManager.presentSSOSignin(activity);
						}
					}
				}
			}
		});
	}

	@Override
	public void otherHttpCode(Response response) {

	}

	@Override
	public void onAddItemToCartFailure(String errorMessage) {

	}

	@Override
	public void onQuantityChangeClick(int position, ShoppingListItem shoppingListItem) {
		this.changeQuantityItem = position;
		if (mWoolWorthsApplication != null) {
			WGlobalState wGlobalState = mWoolWorthsApplication.getWGlobalState();
			if (wGlobalState != null) {
				wGlobalState.navigateFromQuantity(1);
			}
		}
		Activity activity = getActivity();
		if (activity != null) {
			Intent editQuantityIntent = new Intent(activity, ConfirmColorSizeActivity.class);
			editQuantityIntent.putExtra(ConfirmColorSizeActivity.SELECT_PAGE, ConfirmColorSizeActivity.QUANTITY);
			editQuantityIntent.putExtra("QUANTITY_IN_STOCK", Utils.toJson(shoppingListItem));
			activity.startActivity(editQuantityIntent);
			activity.overridePendingTransition(0, 0);
		}
	}

	@Override
	public void onGetListFailure(final String errorMessage) {
		Activity activity = getActivity();
		if (activity != null) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					getViewDataBinding().loadingBar.setVisibility(View.GONE);
					mErrorHandlerView.showErrorHandler();
					mErrorHandlerView.networkFailureHandler(errorMessage);
				}
			});
		}
	}

	@Override
	public void onDeleteItemFailed() {
		final Activity activity = getActivity();
		if (activity != null) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mErrorHandlerView.showToast();
					shoppingListItemsAdapter.updateList(listItems);
				}
			});
		}
	}

	@Override
	public void openProductDetailFragment(String productName, ProductList productList) {
		getBottomNavigator().openProductDetailFragment(productName, productList);
	}

	@Override
	public void onAddToCartLoad() {

	}

	public void initGetShoppingListItems() {
		mErrorHandlerView.hideErrorHandler();
		getViewDataBinding().loadingBar.setVisibility(View.VISIBLE);
		getShoppingListItems = getViewModel().getShoppingListItems(listId);
		getShoppingListItems.execute();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		cancelRequest(mCartSummary);
		cancelRequest(mPostAddToCart);
		cancelRequest(mGetInventorySkusForStore);
		getBottomNavigator().showBottomNavigationMenu();
	}

	public boolean getButtonStatus(List<ShoppingListItem> items) {
		for (ShoppingListItem shoppingListItem : items) {
			if (shoppingListItem.isSelected)
				return true;
		}
		return false;
	}

	public boolean getSelectAllMenuVisibility(List<ShoppingListItem> items) {
		for (ShoppingListItem shoppingListItem : items) {
			if (!shoppingListItem.isSelected)
				return false;
		}
		return true;
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.shopping_list_more, menu);
		mMenuActionSearch = menu.findItem(R.id.action_search);
		mMenuActionSelectAll = menu.findItem(R.id.selectAll);
		actionSearchVisibility(false);
		if (isMenuItemReadyToShow)
			mMenuActionSelectAll.setVisible(true);
		else
			mMenuActionSelectAll.setVisible(false);


		super.onCreateOptionsMenu(menu, inflater);
	}

	private void actionSearchVisibility(boolean visible) {
		if (mMenuActionSearch != null) {
			mMenuActionSearch.setVisible(visible);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.selectAll:
				if (tvMenuSelectAll.getText().toString().equalsIgnoreCase("SELECT ALL")) {
					selectAllListItems(true);
					tvMenuSelectAll.setText(getString(R.string.deselect_all));
				} else {
					selectAllListItems(false);
					tvMenuSelectAll.setText(getString(R.string.select_all));
				}
				return super.onOptionsItemSelected(item);
			case R.id.action_search:
				return super.onOptionsItemSelected(item);
			default:
				return super.onOptionsItemSelected(item);
		}

	}


	@Override
	public void onEmptyCartRetry() {
		openProductSearchActivity();
	}

	public void setScrollListener(RecyclerView recyclerView) {
		recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				actionSearchVisibility(dy > getViewDataBinding().rcvShoppingListItems.getHeight());
			}
		});
	}

	private void setUpAddToCartButton() {
		getViewDataBinding().incConfirmButtonLayout.btnCheckOut.setOnClickListener(this);
		setText(getViewDataBinding().incConfirmButtonLayout.btnCheckOut, getString(R.string.add_to_cart));
	}

	private void executeAddToCart(List<ShoppingListItem> items) {
		onAddToCartPreExecute();
		List<AddItemToCart> selectedItems = new ArrayList<>();
		for (ShoppingListItem item : items) {
			if (item.isSelected)
				selectedItems.add(new AddItemToCart(item.productId, item.catalogRefId, item.userQuantity));
		}

		mPostAddToCart = getViewModel().postAddItemToCart(selectedItems);
		mPostAddToCart.execute();
	}

	private void resetAddToCartButton() {
		getViewDataBinding().incConfirmButtonLayout.pbLoadingIndicator.setVisibility(View.GONE);
		getViewDataBinding().incConfirmButtonLayout.btnCheckOut.setVisibility(View.VISIBLE);
	}

	public void manageSelectAllMenuVisibility(int listSize) {
		isMenuItemReadyToShow = listSize > 1;
		Activity activity = getActivity();
		if (activity != null)
			activity.invalidateOptionsMenu();
	}

	public void selectAllListItems(boolean setSelection) {
		if (shoppingListItemsAdapter != null && listItems != null && listItems.size() > 1) {
			for (ShoppingListItem item : listItems) {
				if (item.quantityInStock != 0) {
					item.isSelected = setSelection;
					int quantity = item.userQuantity > 1 ? item.userQuantity : 1; // Click -> Select all - when one item quantity is > 1
					item.userQuantity = setSelection ? quantity : 0;
				}
			}
			shoppingListItemsAdapter.updateList(listItems);
		}

	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		final MenuItem menuItem = menu.findItem(R.id.selectAll);
		LinearLayout rootView = (LinearLayout) menuItem.getActionView();
		tvMenuSelectAll = rootView.findViewById(R.id.title);
		tvMenuSelectAll.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onOptionsItemSelected(menuItem);
			}
		});

	}

	@Override
	public void onConnectionChanged() {
		if (getViewModel().internetConnectionWasLost()) {
			shoppingListInventory();
			getViewModel().setInternetConnectionWasLost(false);
		}

		if (getViewModel().addedToCart()) {
			loadCartSummary();
			getViewModel().addedToCartFail(false);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		Activity activity = getActivity();
		if (activity != null) {
			activity.registerReceiver(mConnectionBroadcast, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		Activity activity = getActivity();
		if (activity != null) {
			activity.unregisterReceiver(mConnectionBroadcast);
		}
	}

	@Override
	public void onToastButtonClicked(String currentState) {

	}

	@Override
	public void onCartSummarySuccess(CartSummaryResponse cartSummaryResponse) {
		Activity activity = getActivity();
		if (activity != null) {
			CartSummary cartSummary = cartSummaryResponse.data.get(0);
			if (!TextUtils.isEmpty(cartSummary.provinceName)) {
				String suburbId = String.valueOf(cartSummary.suburbId);
				Province province = new Province();
				province.name = cartSummary.provinceName;
				province.id = suburbId;
				Suburb suburb = new Suburb();
				suburb.name = cartSummary.suburbName;
				suburb.id = suburbId;
				Utils.saveRecentDeliveryLocation(new DeliveryLocationHistory(province, suburb), activity);
				executeAddToCart(listItems.subList(1, listItems.size()));
			} else {
				deliverySelectionIntent(getActivity());
				resetAddToCartButton();
			}
		}
	}

	@Override
	public void onCartSummaryExpiredSession(Response response) {
		resetAddToCartButton();
	}

	@Override
	public void onCartSummaryOtherHttpCode(Response response) {
		resetAddToCartButton();
	}

	@Override
	public void onTokenFailure(String e) {
		Activity activity = getActivity();
		if (activity != null) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					resetAddToCartButton();
					mErrorHandlerView.showToast();
				}
			});
		}
	}

	public void executeGetInventoryForStore(String storeId, String multiSku) {
		onAddToCartLoad();
		mGetInventorySkusForStore = getViewModel().getInventoryStockForStore(storeId, multiSku);
		mGetInventorySkusForStore.execute();
	}

	@Override
	public void getInventoryForStoreSuccess(SkusInventoryForStoreResponse skusInventoryForStoreResponse) {
		switch (skusInventoryForStoreResponse.httpCode) {
			case 200:
				String fulFillmentType = null;
				String storeId = skusInventoryForStoreResponse.storeId;
				for (Map.Entry<String, String> mapFulfillmentStore : mMapStoreFulFillmentKeyValue.entrySet()) {
					if (storeId.equalsIgnoreCase(mapFulfillmentStore.getValue())) {
						fulFillmentType = mapFulfillmentStore.getKey();
					}
				}
				// skuInventory is empty or null
				if (skusInventoryForStoreResponse.skuInventory.isEmpty()) {
					for (ShoppingListItem inventoryItems : listItems) {
						if (TextUtils.isEmpty(inventoryItems.fulfillmentType)) continue;
						inventoryItems.inventoryCallCompleted = true;

						inventoryItems.userShouldSetSuburb = false;
					}
				}

				for (SkuInventory skuInventory : skusInventoryForStoreResponse.skuInventory) {
					String sku = skuInventory.sku;
					int quantity = skuInventory.quantity;
					for (ShoppingListItem inventoryItems : listItems) {
						if (TextUtils.isEmpty(inventoryItems.fulfillmentType)) continue;
						if (inventoryItems.fulfillmentType.equalsIgnoreCase(fulFillmentType)) {
							if (sku.equalsIgnoreCase(inventoryItems.catalogRefId)) {
								inventoryItems.quantityInStock = quantity;
							}
						}
						inventoryItems.inventoryCallCompleted = true;
					}
				}
				/**
				 * @method: getLastValueInMap() returns last storeId position
				 * @method: updateList()
				 */
				if (getLastValueInMap() == null) {
					updateList();
					return;
				}
				if (getLastValueInMap().equalsIgnoreCase(storeId)) {
					updateList();
				}
				break;
			default:
				updateList();
				if (!errorMessageWasPopUp) {
					Activity activity = getActivity();
					if (activity == null) return;
					if (skusInventoryForStoreResponse == null) return;
					if (skusInventoryForStoreResponse.response == null) return;
					if (TextUtils.isEmpty(skusInventoryForStoreResponse.response.desc)) return;
					Utils.displayValidationMessage(activity, CustomPopUpWindow.MODAL_LAYOUT.ERROR, skusInventoryForStoreResponse.response.desc);
					errorMessageWasPopUp = true;
				}
				break;
		}
	}

	private void updateList() {
		if (shoppingListItemsAdapter != null)
			shoppingListItemsAdapter.updateList(listItems);
	}

	@Override
	public void geInventoryForStoreFailure(final String errorMessage) {
		Activity activity = getActivity();
		if (activity != null) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mErrorHandlerView.showToast();
					updateList();
				}
			});
		}
	}

	@Override
	public void openSetSuburbProcess(ShoppingListItem shoppingListItem) {
		this.mOpenShoppingListItem = shoppingListItem;
		locationSelectionClicked();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		showToolbar(listName);
		if (requestCode == DELIVERY_LOCATION_REQUEST) {
			if (resultCode == SUBURB_SET_RESULT) { // on suburb selection successful
				executeAddToCart(listItems.subList(1, listItems.size()));
			}
		}

		if (requestCode == REQUEST_SUBURB_CHANGE) {
			initGetShoppingListItems();
		}
	}

	private void deliverySelectionIntent(Activity activity) {
		Intent deliveryLocationSelectionActivity = new Intent(activity, DeliveryLocationSelectionActivity.class);
		deliveryLocationSelectionActivity.putExtra(DeliveryLocationSelectionActivity.LOAD_PROVINCE, "LOAD_PROVINCE");
		activity.startActivityForResult(deliveryLocationSelectionActivity, DELIVERY_LOCATION_REQUEST);
		activity.overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
	}

	private void loadCartSummary() {
		onAddToCartPreExecute();
		mCartSummary = getViewModel().getCartSummary(getActivity());
		mCartSummary.execute();
	}

	private String getLastValueInMap() {
		for (Map.Entry<String, String> entry : mMapStoreFulFillmentKeyValue.entrySet()) {
			return entry.getValue();
		}
		return null;
	}

	private void cancelQuantityLoad() {
		for (ShoppingListItem inventoryItems : listItems) {
			inventoryItems.userShouldSetSuburb = true;
		}
	}

	private void locationSelectionClicked() {
		Activity activity = getActivity();
		if (activity == null) return;
		Intent openDeliveryLocationSelectionActivity = new Intent(this.getContext(), DeliveryLocationSelectionActivity.class);
		startActivityForResult(openDeliveryLocationSelectionActivity, REQUEST_SUBURB_CHANGE);
		activity.overridePendingTransition(R.anim.slide_up_fast_anim, R.anim.stay);
	}
}
