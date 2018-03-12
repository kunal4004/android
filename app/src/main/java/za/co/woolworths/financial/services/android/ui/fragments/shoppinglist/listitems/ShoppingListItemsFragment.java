package za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.listitems;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.awfs.coordination.R;
import com.awfs.coordination.BR;
import com.awfs.coordination.databinding.ShoppingListItemsFragmentBinding;

import za.co.woolworths.financial.services.android.ui.adapters.ShoppingListAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.ShoppingListItemsAdapter;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;

/**
 * Created by W7099877 on 2018/03/08.
 */

public class ShoppingListItemsFragment extends BaseFragment<ShoppingListItemsFragmentBinding,ShoppingListItemsViewModel> implements ShoppingListItemsNavigator {
	private ShoppingListItemsViewModel shoppingListItemsViewModel;
	private ShoppingListItemsAdapter shoppingListItemsAdapter;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		shoppingListItemsViewModel= ViewModelProviders.of(this).get(ShoppingListItemsViewModel.class);
		shoppingListItemsViewModel.setNavigator(this);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setToolbarBackgroundDrawable(R.drawable.appbar_background);
		showBackNavigationIcon(true);
		setTitle("general Fashion");
		showToolbar();
		loadShoppingListItems();
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

	public void loadShoppingListItems() {
		shoppingListItemsAdapter=new ShoppingListItemsAdapter();
		LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
		mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		getViewDataBinding().rcvShoppingListItems.setLayoutManager(mLayoutManager);
		getViewDataBinding().rcvShoppingListItems.setAdapter(shoppingListItemsAdapter);
	}

}
