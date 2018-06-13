package za.co.woolworths.financial.services.android.ui.fragments.shoppinglist;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;
import com.awfs.coordination.databinding.ShoppinglistFragmentBinding;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation;
import za.co.woolworths.financial.services.android.models.dto.ShoppingList;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse;
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.DeleteShoppingList;
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.GetShoppingLists;
import za.co.woolworths.financial.services.android.ui.activities.DeliveryLocationSelectionActivity;
import za.co.woolworths.financial.services.android.ui.adapters.ShoppingListAdapter;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.list.NewListFragment;
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.listitems.ShoppingListItemsFragment;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.EmptyCartView;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.KeyboardUtil;
import za.co.woolworths.financial.services.android.util.NetworkChangeListener;
import za.co.woolworths.financial.services.android.util.Utils;

import static za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow.DISMISS_POP_WINDOW_CLICKED;
import static za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.OPEN_CART_REQUEST;

public class ShoppingListFragment extends BaseFragment<ShoppinglistFragmentBinding, ShoppingListViewModel> implements ShoppingListNavigator, EmptyCartView.EmptyCartInterface, NetworkChangeListener, View.OnClickListener {
	private ShoppingListViewModel shoppingListViewModel;
	private DeleteShoppingList deleteShoppingList;
	private GetShoppingLists mGetShoppingLists;
	private ErrorHandlerView mErrorHandlerView;
	private BroadcastReceiver mConnectionBroadcast;
	private MenuItem mMenuCreateList;
	ShoppingListAdapter shoppingListAdapter;
	private String mSuburbName, mProvinceName;
	private static final int REQUEST_SUBURB_CHANGE = 143;
	private RelativeLayout rlLocationSelectedLayout;
	private WTextView tvDeliveryLocation;
	private WTextView tvDeliveringToText;


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
		emptyCartView.setView(getString(R.string.title_no_shopping_lists), getString(R.string.description_no_shopping_lists), getString(R.string.button_no_shopping_lists), R.drawable.emptylists);
		view.findViewById(R.id.btnRetry).setOnClickListener(this);

		rlLocationSelectedLayout = getViewDataBinding().deliveryLocationLayout.locationSelectedLayout;
		tvDeliveryLocation = getViewDataBinding().deliveryLocationLayout.tvDeliveryLocation;
		tvDeliveringToText = getViewDataBinding().deliveryLocationLayout.tvDeliveringTo;

		setDeliveryLocation();

		RelativeLayout rlNoConnectionLayout = getViewDataBinding().incConnectionLayout.noConnectionLayout;
		mErrorHandlerView = new ErrorHandlerView(getActivity(), rlNoConnectionLayout);
		mErrorHandlerView.setMargin(rlNoConnectionLayout, 0, 0, 0, 0);
		mConnectionBroadcast = Utils.connectionBroadCast(getActivity(), this);
		rlLocationSelectedLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				locationSelectionClicked();
			}
		});
	}

	public void setDeliveryLocation(){
		ShoppingDeliveryLocation lastDeliveryLocation = Utils.getPreferredDeliveryLocation();
		if (lastDeliveryLocation != null) {
			mSuburbName = lastDeliveryLocation.suburb.name;
			mProvinceName = lastDeliveryLocation.province.name;
			manageDeliveryLocationUI(mSuburbName + ", " + mProvinceName);
		}
	}

	public void loadShoppingList(List<ShoppingList> lists) {
		mMenuCreateList.setVisible(true);
		RecyclerView rclShoppingList = getViewDataBinding().rcvShoppingLists;
		RelativeLayout rlSoppingList = getViewDataBinding().incEmptyLayout.relEmptyStateHandler;

		rlSoppingList.setVisibility(lists == null || lists.size() == 0 ? View.VISIBLE : View.GONE);
		rclShoppingList.setVisibility(lists == null || lists.size() == 0 ? View.GONE : View.VISIBLE);

		shoppingListAdapter = new ShoppingListAdapter(this, lists);
		LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
		mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		rclShoppingList.setLayoutManager(mLayoutManager);
		rclShoppingList.setAdapter(shoppingListAdapter);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.shopping_list_menu, menu);
		mMenuCreateList = menu.findItem(R.id.action_create_list);
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
				pushFragmentSlideUp(new NewListFragment());
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
	public void onShoppingListsResponse(ShoppingListsResponse shoppingListsResponse) {
		getViewDataBinding().loadingBar.setVisibility(View.GONE);
		loadShoppingList(shoppingListsResponse.lists);
		Utils.deliveryLocationEnabled(getActivity(), true, rlLocationSelectedLayout);
	}

	@Override
	public void onGetShoppingListFailed(final String errorMessage) {
		Activity activity = getBaseActivity();
		if (activity != null) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Utils.deliveryLocationEnabled(getActivity(), true, rlLocationSelectedLayout);
					getViewDataBinding().loadingBar.setVisibility(View.GONE);
					mMenuCreateList.setVisible(false);
					mErrorHandlerView.showErrorHandler();
					mErrorHandlerView.networkFailureHandler(errorMessage);
				}
			});
		}

	}

	@Override
	public void onDeleteFailed() {
		Activity activity = getBaseActivity();
		if (activity != null) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mErrorHandlerView.showToast();
					if (shoppingListAdapter != null)
						shoppingListAdapter.update();

				}
			});
		}
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {
			showToolbar(R.string.title_my_list);
			initGetShoppingList();
		}
	}


	@Override
	public void onEmptyCartRetry() {
		pushFragmentSlideUp(new NewListFragment());
	}

	public void initGetShoppingList() {
		mErrorHandlerView.hideErrorHandler();
		Utils.deliveryLocationEnabled(getActivity(), false, rlLocationSelectedLayout);
		getViewDataBinding().rcvShoppingLists.setVisibility(View.GONE);
		getViewDataBinding().loadingBar.setVisibility(View.VISIBLE);
		mGetShoppingLists = getViewModel().getShoppingListsResponse();
		mGetShoppingLists.execute();
	}

	@Override
	public void onConnectionChanged() {

	}

	@Override
	public void onResume() {
		super.onResume();
		initGetShoppingList();
		setDeliveryLocation();
		KeyboardUtil.hideSoftKeyboard(getActivity());
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
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.btnRetry:
				if (new ConnectionDetector().isOnline(getActivity())) {
					initGetShoppingList();
				}
				break;
			default:
				break;
		}
	}


	@Override
	public void onDetach() {
		super.onDetach();
		cancelRequest(mGetShoppingLists);
		cancelRequest(deleteShoppingList);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_SUBURB_CHANGE) {
			showToolbar(R.string.title_my_list);
			ShoppingDeliveryLocation lastDeliveryLocation = Utils.getPreferredDeliveryLocation();
			if (lastDeliveryLocation != null) {
				mSuburbName = lastDeliveryLocation.suburb.name;
				mProvinceName = lastDeliveryLocation.province.name;
				manageDeliveryLocationUI(mSuburbName + ", " + mProvinceName);
			}
			initGetShoppingList();
			return;
		}

		if (requestCode == OPEN_CART_REQUEST) {
			if (resultCode == DISMISS_POP_WINDOW_CLICKED) {
				showToolbar(R.string.title_my_list);
			}
		}
	}

	private void locationSelectionClicked() {
		Activity activity = getActivity();
		if (activity != null) {
			Intent openDeliveryLocationSelectionActivity = new Intent(this.getContext(), DeliveryLocationSelectionActivity.class);
			openDeliveryLocationSelectionActivity.putExtra("suburbName", mSuburbName);
			openDeliveryLocationSelectionActivity.putExtra("provinceName", mProvinceName);
			startActivityForResult(openDeliveryLocationSelectionActivity, REQUEST_SUBURB_CHANGE);
			activity.overridePendingTransition(R.anim.slide_up_fast_anim, R.anim.stay);
		}
	}

	public void manageDeliveryLocationUI(String deliveryLocation) {
		tvDeliveringToText.setText(getContext().getString(R.string.delivering_to));
		tvDeliveryLocation.setVisibility(View.VISIBLE);
		tvDeliveryLocation.setText(deliveryLocation);
	}
}


