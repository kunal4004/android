package za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.listitems;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;
import com.awfs.coordination.BR;
import com.awfs.coordination.databinding.ShoppingListItemsFragmentBinding;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.functions.Consumer;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItem;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItemsResponse;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse;
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.DeleteShoppingList;
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.DeleteShoppingListItem;
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.GetShoppingListItems;
import za.co.woolworths.financial.services.android.models.service.event.ShopState;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.adapters.ShoppingListItemsAdapter;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;

import za.co.woolworths.financial.services.android.ui.activities.product.ProductSearchActivity;
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.search.SearchResultFragment;
import za.co.woolworths.financial.services.android.util.EmptyCartView;
import za.co.woolworths.financial.services.android.util.Utils;

import static android.app.Activity.RESULT_OK;

public class ShoppingListItemsFragment extends BaseFragment<ShoppingListItemsFragmentBinding, ShoppingListItemsViewModel> implements ShoppingListItemsNavigator, View.OnClickListener, EmptyCartView.EmptyCartInterface {
	private ShoppingListItemsViewModel shoppingListItemsViewModel;
	private String listName;
	private String listId;
	private GetShoppingListItems getShoppingListItems;
	private List<ShoppingListItem> listItems;
	private DeleteShoppingList deleteShoppingList;
	private DeleteShoppingListItem deleteShoppingListItem;
	private ShoppingListItemsAdapter shoppingListItemsAdapter;
	private MenuItem mMenuActionSearch;

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
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		getBottomNavigator().hideBottomNavigationMenu();
		listName = getArguments().getString("listName");
		listId = getArguments().getString("listId");
		EmptyCartView emptyCartView = new EmptyCartView(view, this);
		emptyCartView.setView("title text", "description text", R.drawable.vector_icon_empty_list);
		showToolbar(listName);
		observableOn(new Consumer() {
			@Override
			public void accept(Object object) throws Exception {
				if (object != null) {
					if (object instanceof ShopState) {
						ShopState shopState = (ShopState) object;
						if (shopState.getState().equalsIgnoreCase("DELETE_LIST")) {
							initDeleteShoppingList();
							return;
						}
						if (!TextUtils.isEmpty(shopState.getState())) {
							SearchResultFragment searchResultFragment = new SearchResultFragment();
							Bundle bundle = new Bundle();
							bundle.putString("searchTEXT", shopState.getState());
							bundle.putString("listID", shopState.getListId());
							searchResultFragment.setArguments(bundle);
							pushFragment(searchResultFragment);
						}
					}
				}
			}
		});

		initList(getViewDataBinding().rcvShoppingListItems);
		initGetShoppingListItems();
		setScrollListener(getViewDataBinding().rcvShoppingListItems);
		getViewDataBinding().textProductSearch.setOnClickListener(this);
		setUpAddToCartButton();
	}

	public void loadShoppingListItems(ShoppingListItemsResponse shoppingListItemsResponse) {
		getViewDataBinding().loadingBar.setVisibility(View.GONE);
		listItems = shoppingListItemsResponse.listItems;
		listItems.add(0, new ShoppingListItem());
		shoppingListItemsAdapter.updateList(listItems);
		RecyclerView rcvShoppingListItems = getViewDataBinding().rcvShoppingListItems;
		RelativeLayout rlSoppingList = getViewDataBinding().incEmptyLayout.relEmptyStateHandler;
		getViewDataBinding().incConfirmButtonLayout.rlCheckOut.setVisibility(listItems == null || listItems.size() <= 1 ? View.GONE : View.VISIBLE);
		rlSoppingList.setVisibility(listItems == null || listItems.size() <= 1 ? View.VISIBLE : View.GONE); // 1 to exclude header
		rcvShoppingListItems.setVisibility(listItems == null || listItems.size() <= 1 ? View.GONE : View.VISIBLE);
		getViewDataBinding().rlShopSearch.setVisibility(listItems == null || listItems.size() <= 1 ? View.VISIBLE : View.GONE);
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
			case R.id.btnCheckOut:
				onClickAddToCart(listItems.subList(1,listItems.size()));
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
	}

	@Override
	public void onDeleteShoppingList(ShoppingListsResponse shoppingListsResponse) {
		Intent intent = new Intent(getActivity(), ShoppingListItemsFragment.class);
		intent.putExtra("ShoppingList", Utils.objectToJson(shoppingListsResponse));
		getTargetFragment().onActivityResult(getTargetRequestCode(), RESULT_OK, intent);
		getActivity().onBackPressed();
	}

	@Override
	public void onShoppingListItemDelete(ShoppingListItemsResponse shoppingListItemsResponse) {
		listItems = shoppingListItemsResponse.listItems;
		shoppingListItemsAdapter.updateList(listItems);

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

	}

	@Override
	public void onAddToCartPostExecute() {

	}

	public void initGetShoppingListItems() {
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

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.shopping_list_more, menu);
		mMenuActionSearch = menu.findItem(R.id.action_search);
		actionSearchVisibility(false);
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
			case R.id.action_create_list:
				Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.EDIT_SHOPPING_LIST, "");
				return super.onOptionsItemSelected(item);
			case R.id.action_search:
				return super.onOptionsItemSelected(item);
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public void initDeleteShoppingList() {
		deleteShoppingList = getViewModel().deleteShoppingList(listId);
		deleteShoppingList.execute();
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

	private void onClickAddToCart(List<ShoppingListItem> items){
			
	}
}