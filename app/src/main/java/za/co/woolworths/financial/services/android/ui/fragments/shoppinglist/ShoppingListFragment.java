package za.co.woolworths.financial.services.android.ui.fragments.shoppinglist;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;
import com.awfs.coordination.databinding.ShoppinglistFragmentBinding;
import com.google.gson.Gson;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.ShoppingList;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse;
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.DeleteShoppingList;
import za.co.woolworths.financial.services.android.ui.adapters.ShoppingListAdapter;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.list.NewListFragment;
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.listitems.ShoppingListItemsFragment;
import za.co.woolworths.financial.services.android.util.EmptyCartView;

import static android.app.Activity.RESULT_OK;

public class ShoppingListFragment extends BaseFragment<ShoppinglistFragmentBinding, ShoppingListViewModel> implements ShoppingListNavigator, EmptyCartView.EmptyCartInterface {
	private ShoppingListViewModel shoppingListViewModel;
	private ShoppingListsResponse shoppingListsResponse;
	public static final int DELETE_REQUEST_CODE = 111;
	private DeleteShoppingList deleteShoppingList;

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

		EmptyCartView emptyCartView = new EmptyCartView(view, this);
		emptyCartView.setView("title text", "description text", "button text", R.drawable.woolworth_logo_icon);

		if (getArguments().containsKey("ShoppingList")) {
			shoppingListsResponse = new Gson().fromJson(getArguments().getString("ShoppingList"), ShoppingListsResponse.class);
			loadShoppingList(shoppingListsResponse.lists);
		}
	}

	public void loadShoppingList(List<ShoppingList> lists) {
		RecyclerView rclShoppingList = getViewDataBinding().rcvShoppingLists;
		RelativeLayout rlSoppingList = getViewDataBinding().incEmptyLayout.relEmptyStateHandler;

		rlSoppingList.setVisibility(lists == null || lists.size() == 0 ? View.VISIBLE : View.GONE);
		rclShoppingList.setVisibility(lists == null || lists.size() == 0 ? View.GONE : View.VISIBLE);

		ShoppingListAdapter shoppingListAdapter = new ShoppingListAdapter(this, lists);
		LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
		mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		rclShoppingList.setLayoutManager(mLayoutManager);
		rclShoppingList.setAdapter(shoppingListAdapter);
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
		pushFragment(shoppingListItemsFragment);
	}

	@Override
	public void onClickItemDelete(String listID) {
		deleteShoppingList = getViewModel().deleteShoppingList(listID);
		deleteShoppingList.execute();
	}

	@Override
	public void onDeleteShoppingList(ShoppingListsResponse shoppingListsResponse) {
		loadShoppingList(shoppingListsResponse.lists);
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {
			showToolbar(R.string.title_my_list);
		}
	}

	/*@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == DELETE_REQUEST_CODE) {
				shoppingListsResponse = new Gson().fromJson(data.getStringExtra("ShoppingList"), ShoppingListsResponse.class);
				loadShoppingList(shoppingListsResponse.lists);
			}
		}
	}*/

	@Override
	public void onEmptyCartRetry() {
		Log.e("onEmptyCartClicked", "emptyCartClicked");
	}
}
