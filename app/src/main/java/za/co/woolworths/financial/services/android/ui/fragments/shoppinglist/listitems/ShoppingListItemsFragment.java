package za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.listitems;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.awfs.coordination.R;
import com.awfs.coordination.BR;
import com.awfs.coordination.databinding.ShoppingListItemsFragmentBinding;

import io.reactivex.functions.Consumer;
import za.co.woolworths.financial.services.android.models.service.event.ShopState;
import za.co.woolworths.financial.services.android.ui.adapters.ShoppingListItemsAdapter;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;

import za.co.woolworths.financial.services.android.ui.activities.product.ProductSearchActivity;
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.search.SearchResultFragment;

public class ShoppingListItemsFragment extends BaseFragment<ShoppingListItemsFragmentBinding, ShoppingListItemsViewModel> implements ShoppingListItemsNavigator, View.OnClickListener {
	private ShoppingListItemsViewModel shoppingListItemsViewModel;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		shoppingListItemsViewModel = ViewModelProviders.of(this).get(ShoppingListItemsViewModel.class);
		shoppingListItemsViewModel.setNavigator(this);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		showToolbar(R.string.general_fashion);
		loadShoppingListItems();
		getViewDataBinding().textProductSearch.setOnClickListener(this);
		getViewModel().consumeObservable(new Consumer() {
			@Override
			public void accept(Object object) throws Exception {
				if (object != null) {
					if (object instanceof ShopState) {
						ShopState shopState = (ShopState) object;
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
		ShoppingListItemsAdapter shoppingListItemsAdapter = new ShoppingListItemsAdapter();
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
}