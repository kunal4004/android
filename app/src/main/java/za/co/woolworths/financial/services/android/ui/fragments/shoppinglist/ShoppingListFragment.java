package za.co.woolworths.financial.services.android.ui.fragments.shoppinglist;


import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;
import com.awfs.coordination.databinding.ShoppinglistFragmentBinding;

import za.co.woolworths.financial.services.android.ui.base.BaseFragment;
import za.co.woolworths.financial.services.android.ui.base.BaseViewModel;

/**
 * Created by W7099877 on 2018/03/07.
 */

public class ShoppingListFragment extends BaseFragment<ShoppinglistFragmentBinding,ShoppingListViewModel> implements ShoppingListNavigator {
	private ShoppingListViewModel shoppingListViewModel;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
}
