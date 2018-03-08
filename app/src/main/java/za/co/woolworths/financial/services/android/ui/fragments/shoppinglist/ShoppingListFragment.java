package za.co.woolworths.financial.services.android.ui.fragments.shoppinglist;


import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;
import com.awfs.coordination.databinding.ShoppinglistFragmentBinding;

import za.co.woolworths.financial.services.android.ui.adapters.ShoppingListAdapter;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;
import za.co.woolworths.financial.services.android.ui.base.BaseViewModel;
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.listitems.ShoppingListItemsFragment;

/**
 * Created by W7099877 on 2018/03/07.
 */

public class ShoppingListFragment extends BaseFragment<ShoppinglistFragmentBinding,ShoppingListViewModel> implements ShoppingListNavigator {
	private ShoppingListViewModel shoppingListViewModel;
	private ShoppingListAdapter shoppingListAdapter;

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
		showBackNavigationIcon(true);
		setToolbarBackgroundDrawable(R.drawable.appbar_background);
		setTitle(getString(R.string.title_my_list));
		showToolbar();
		loadShoppingList();
	}

	public void loadShoppingList() {
		shoppingListAdapter=new ShoppingListAdapter(this);
		LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
		mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		getViewDataBinding().rcvShoppingLists.setLayoutManager(mLayoutManager);
		getViewDataBinding().rcvShoppingLists.setAdapter(shoppingListAdapter);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.shopping_list_menu,menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onListItemSelected() {
		getBottomNavigator().pushFragment(new ShoppingListItemsFragment());
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {
			showBackNavigationIcon(true);
			setToolbarBackgroundDrawable(R.drawable.appbar_background);
			setTitle(getString(R.string.title_my_list));
			showToolbar();

		}
	}
}
