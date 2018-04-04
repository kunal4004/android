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
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;
import com.awfs.coordination.BR;
import com.awfs.coordination.databinding.ShoppingListItemsFragmentBinding;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.functions.Consumer;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.AddItemToCart;
import za.co.woolworths.financial.services.android.models.dto.AddItemToCartResponse;
import za.co.woolworths.financial.services.android.models.dto.AddToCartDaTum;
import za.co.woolworths.financial.services.android.models.dto.CartSummaryResponse;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItem;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItemsResponse;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.DeleteShoppingList;
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.DeleteShoppingListItem;
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.GetShoppingListItems;
import za.co.woolworths.financial.services.android.models.service.event.CartState;
import za.co.woolworths.financial.services.android.models.service.event.ShopState;
import za.co.woolworths.financial.services.android.ui.activities.ConfirmColorSizeActivity;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator;
import za.co.woolworths.financial.services.android.ui.adapters.ShoppingListItemsAdapter;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;

import za.co.woolworths.financial.services.android.ui.activities.product.ProductSearchActivity;
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.search.SearchResultFragment;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.EmptyCartView;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.NetworkChangeListener;
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
		mToastUtils.setView(bottomNavigator.getBottomNavigationById());
		mToastUtils.setMessage(shopState.getCount() == 1 ? shopState.getCount() + " " + getString(R.string.single_item_text) + " " + getString(R.string.added_to) : shopState.getCount() + " " + getString(R.string.multiple_item_text) + " " + getString(R.string.added_to));
		mToastUtils.setViewState(false);
		mToastUtils.setCartText(listName);
		mToastUtils.build();
	}

	private void updateList(List<ShoppingListItem> listItems) {
		setHeader();
		if (shoppingListItemsAdapter != null)
			shoppingListItemsAdapter.updateList(listItems);
	}

	public void loadShoppingListItems(ShoppingListItemsResponse shoppingListItemsResponse) {
		getViewDataBinding().loadingBar.setVisibility(View.GONE);
		listItems = shoppingListItemsResponse.listItems;
		updateList(listItems);
		setUpView();
	}

	private void setUpView() {
		RecyclerView rcvShoppingListItems = getViewDataBinding().rcvShoppingListItems;
		RelativeLayout rlSoppingList = getViewDataBinding().incEmptyLayout.relEmptyStateHandler;
		rlSoppingList.setVisibility(listItems == null || listItems.size() <= 1 ? View.VISIBLE : View.GONE); // 1 to exclude header
		rcvShoppingListItems.setVisibility(listItems == null || listItems.size() <= 1 ? View.GONE : View.VISIBLE);
		getViewDataBinding().rlShopSearch.setVisibility(listItems == null || listItems.size() <= 1 ? View.VISIBLE : View.GONE);
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
					initGetShoppingListItems();
				}
				break;
			case R.id.btnCheckOut:
				executeAddToCart(listItems.subList(1, listItems.size()));
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
		loadShoppingListItems(shoppingListItemsResponse);
	}

	@Override
	public void onItemSelectionChange(List<ShoppingListItem> items) {
		getViewDataBinding().incConfirmButtonLayout.rlCheckOut.setVisibility(getButtonStatus(items) ? View.VISIBLE : View.GONE);
		if (items.size() > 0)
			tvMenuSelectAll.setText(getString(getSelectAllMenuVisibility(items) ? R.string.deselect_all : R.string.select_all));
		else
			mMenuActionSelectAll.setVisible(false);
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
	public void onSessionTokenExpired(Response response) {

	}

	@Override
	public void otherHttpCode(Response response) {

	}

	@Override
	public void onAddItemToCartFailure(String errorMessage) {

	}

	@Override
	public void onQuantityChangeClick(int position) {
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
			activity.startActivity(editQuantityIntent);
			activity.overridePendingTransition(0, 0);
		}
	}

	@Override
	public void onGetListFailure(final String errorMessage) {
		Activity activity = getBaseActivity();
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

	public void initGetShoppingListItems() {
		mErrorHandlerView.hideErrorHandler();
		getViewDataBinding().loadingBar.setVisibility(View.VISIBLE);
		getShoppingListItems = getViewModel().getShoppingListItems(listId);
		getShoppingListItems.execute();
	}

	@Override
	public void onDetach() {
		super.onDetach();
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
				//Your action here
				Log.e("scroll", (dy > getViewDataBinding().rcvShoppingListItems.getHeight()) + " dy " + dy + " shopList " + getViewDataBinding().relEmptyStateHandler.getHeight());
				actionSearchVisibility(dy > getViewDataBinding().rcvShoppingListItems.getHeight());
			}
		});
	}

	private void setUpAddToCartButton() {
		getViewDataBinding().incConfirmButtonLayout.btnCheckOut.setOnClickListener(this);
		setText(getViewDataBinding().incConfirmButtonLayout.btnCheckOut, getString(R.string.add_to_cart));
	}

	private void executeAddToCart(List<ShoppingListItem> items) {
		List<AddItemToCart> selectedItems = new ArrayList<>();
		for (ShoppingListItem item : items) {
			if (item.isSelected)
				selectedItems.add(new AddItemToCart(item.productId, item.catalogRefId, item.quantityDesired));
		}

		getViewModel().postAddItemToCart(selectedItems).execute();
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
				item.isSelected = setSelection;
				int quantity = item.userQuantity > 1 ? item.userQuantity : 1; // Click -> Select all - when one item quantity is > 1
				item.userQuantity = setSelection ? quantity : 0;
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


}