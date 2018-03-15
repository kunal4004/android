package za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.listitems;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.awfs.coordination.R;
import com.awfs.coordination.BR;
import com.awfs.coordination.databinding.ShoppingListItemsFragmentBinding;

import java.util.List;

import io.reactivex.functions.Consumer;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItem;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItemsResponse;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse;
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.DeleteShoppingList;
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.GetShoppingListItems;
import za.co.woolworths.financial.services.android.models.service.event.ShopState;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.adapters.ShoppingListItemsAdapter;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;

import za.co.woolworths.financial.services.android.ui.activities.product.ProductSearchActivity;
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.search.SearchResultFragment;
import za.co.woolworths.financial.services.android.util.Utils;

import static android.app.Activity.RESULT_OK;

public class ShoppingListItemsFragment extends BaseFragment<ShoppingListItemsFragmentBinding, ShoppingListItemsViewModel> implements ShoppingListItemsNavigator, View.OnClickListener {
	private ShoppingListItemsViewModel shoppingListItemsViewModel;
	private String listName;
	private String listId;
	private GetShoppingListItems getShoppingListItems;
	private List<ShoppingListItem> listItems;
	private DeleteShoppingList deleteShoppingList;
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
		listName=getArguments().getString("listName");
		listId=getArguments().getString("listId");
		showToolbar(listName);
		getViewDataBinding().textProductSearch.setOnClickListener(this);
		getViewModel().consumeObservable(new Consumer() {
			@Override
			public void accept(Object object) throws Exception {
				if (object != null) {
					if (object instanceof ShopState) {
						ShopState shopState = (ShopState) object;
						if (shopState.getState().equalsIgnoreCase("DELETE_LIST"))
						{
							initDeleteShoppingList();
							return;
						}
						if (!TextUtils.isEmpty(shopState.getState())) {
							SearchResultFragment searchResultFragment = new SearchResultFragment();
							Bundle bundle = new Bundle();
							bundle.putString("search_text", shopState.getState());
							searchResultFragment.setArguments(bundle);
							pushFragment(searchResultFragment);
						}
					}
				}
			}
		});

		initGetShoppingListItems();
	}

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

	public void loadShoppingListItems(ShoppingListItemsResponse shoppingListItemsResponse) {
		getViewDataBinding().loadingBar.setVisibility(View.GONE);
		listItems=shoppingListItemsResponse.listItems;
		ShoppingListItemsAdapter shoppingListItemsAdapter = new ShoppingListItemsAdapter(listItems,this);
		LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
		mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		getViewDataBinding().rcvShoppingListItems.setLayoutManager(mLayoutManager);
		getViewDataBinding().rcvShoppingListItems.setAdapter(shoppingListItemsAdapter);
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {
			showToolbar(R.string.general_fashion);
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.textProductSearch:
				Log.e("getId", "rlShoppingListSearch");
				Intent openProductSearchActivity = new Intent(getActivity(), ProductSearchActivity.class);
				openProductSearchActivity.putExtra("SEARCH_TEXT_HINT", getString(R.string.shopping_search_hint));
				startActivity(openProductSearchActivity);
				getActivity().overridePendingTransition(R.anim.stay, R.anim.stay);
				break;
			default:
				break;
		}
	}

	@Override
	public void onShoppingListItemsResponse(ShoppingListItemsResponse shoppingListItemsResponse) {
		loadShoppingListItems(shoppingListItemsResponse);
	}

	@Override
	public void onItemSelectionChange(List<ShoppingListItem> items) {
		getViewDataBinding().btnAddToCart.setVisibility(getButtonStatus(items) ? View.VISIBLE : View.GONE);
	}

	@Override
	public void onDeleteShoppingList(ShoppingListsResponse shoppingListsResponse) {
		Intent intent = new Intent(getActivity(), ShoppingListItemsFragment.class);
		intent.putExtra("ShoppingList",Utils.objectToJson(shoppingListsResponse));
		getTargetFragment().onActivityResult(getTargetRequestCode(), RESULT_OK, intent);
		getActivity().onBackPressed();
	}

	public void initGetShoppingListItems(){
		getViewDataBinding().loadingBar.setVisibility(View.VISIBLE);
		getShoppingListItems=getViewModel().getShoppingListItems(listId);
		getShoppingListItems.execute();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		getBottomNavigator().showBottomNavigationMenu();
	}

	public boolean getButtonStatus(List<ShoppingListItem> items){
		for (ShoppingListItem shoppingListItem: listItems) {
			if(shoppingListItem.isSelected)
				return true;
		}
		return false;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.shopping_list_more, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_create_list:
				Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.EDIT_SHOPPING_LIST, "");
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public void initDeleteShoppingList(){
		deleteShoppingList = getViewModel().deleteShoppingList(listId);
		deleteShoppingList.execute();
	}
}