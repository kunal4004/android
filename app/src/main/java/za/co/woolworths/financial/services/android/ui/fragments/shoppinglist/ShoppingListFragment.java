package za.co.woolworths.financial.services.android.ui.fragments.shoppinglist;


import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;
import com.awfs.coordination.databinding.ShoppinglistFragmentBinding;
import com.google.gson.Gson;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.ShoppingList;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse;
import za.co.woolworths.financial.services.android.ui.adapters.ShoppingListAdapter;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.list.NewListFragment;
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.listitems.ShoppingListItemsFragment;

import static android.app.Activity.RESULT_OK;

/**
 * Created by W7099877 on 2018/03/07.
 */

public class ShoppingListFragment extends BaseFragment<ShoppinglistFragmentBinding, ShoppingListViewModel> implements ShoppingListNavigator {
	private ShoppingListViewModel shoppingListViewModel;
	private ShoppingListAdapter shoppingListAdapter;
	private ShoppingListsResponse shoppingListsResponse;
	public static final int DELETE_REQUEST_CODE = 111;
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		shoppingListViewModel = ViewModelProviders.of(this).get(ShoppingListViewModel.class);
		shoppingListViewModel.setNavigator(this);
	}

	@Override
	public ShoppingListViewModel getViewModel() {
		return shoppingListViewModel;
	}

	@Override
	public int getBindingVariable() {
		return BR.viewModel;
	}

	@Override
	public int getLayoutId() {
		return R.layout.shoppinglist_fragment;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		showToolbar(R.string.title_my_list);
		if (getArguments().containsKey("ShoppingList")) {
			shoppingListsResponse = new Gson().fromJson(getArguments().getString("ShoppingList"), ShoppingListsResponse.class);
			loadShoppingList(shoppingListsResponse.lists);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	public void loadShoppingList(List<ShoppingList> lists) {
		shoppingListAdapter = new ShoppingListAdapter(this, lists);
		LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
		mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		getViewDataBinding().rcvShoppingLists.setLayoutManager(mLayoutManager);
		getViewDataBinding().rcvShoppingLists.setAdapter(shoppingListAdapter);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.shopping_list_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.action_create_list:
				pushFragment(new NewListFragment());
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onListItemSelected(String listName, String listID) {
		Bundle bundle = new Bundle();
		bundle.putString("listName", listName);
		bundle.putString("listId", listID);
		ShoppingListItemsFragment shoppingListItemsFragment = new ShoppingListItemsFragment();
		shoppingListItemsFragment.setArguments(bundle);
		shoppingListItemsFragment.setTargetFragment(this,111);
		pushFragment(shoppingListItemsFragment);
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {
			showToolbar(R.string.title_my_list);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode==DELETE_REQUEST_CODE){
				shoppingListsResponse=new Gson().fromJson(data.getStringExtra("ShoppingList"),ShoppingListsResponse.class);
				loadShoppingList(shoppingListsResponse.lists);
			}
		}
	}
}
