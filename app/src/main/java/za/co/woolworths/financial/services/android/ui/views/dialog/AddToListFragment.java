package za.co.woolworths.financial.services.android.ui.views.dialog;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.AddToListRequest;
import za.co.woolworths.financial.services.android.models.dto.OtherSkus;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.ShoppingList;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItemsResponse;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.GetShoppingLists;
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.PostAddToList;
import za.co.woolworths.financial.services.android.models.service.event.ProductState;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.adapters.AddToListAdapter;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.EmptyCartView;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.MultiClickPreventer;
import za.co.woolworths.financial.services.android.util.NetworkChangeListener;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.Utils;

import static za.co.woolworths.financial.services.android.models.service.event.ProductState.CLOSE_PDP_FROM_ADD_TO_LIST;

public class AddToListFragment extends Fragment implements View.OnClickListener, AddToListInterface, NetworkChangeListener, EmptyCartView.EmptyCartInterface {

	private String mShoppingResponse = "";
	private WButton mBtnCancel;
	private AddToListAdapter mShoppingListAdapter;
	private int apiCount = 0;
	private ProgressBar mProgressBar;
	private BroadcastReceiver mConnectionBroadcast;
	private boolean addToListHasFail = false;
	private ErrorHandlerView mErrorHandlerView;
	private PostAddToList mPostAddToList;
	private ImageView imCreateList;
	private RecyclerView rcvShoppingLists;
	private RelativeLayout relProgressBar;
	private ProgressBar pbLoadShoppingList;
	private RelativeLayout relEmptyStateHandler;
	private FrameLayout flCancelButton;
	private WButton btnGoToProduct;
	private RelativeLayout rlNoConnectionLayout;
	private WButton btnRetry;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			if (getArguments().containsKey("LIST_PAYLOAD")) {
				mShoppingResponse = getArguments().getString("LIST_PAYLOAD");
			}
		}
	}

	View view;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		if (view == null) {
			view = inflater.inflate(R.layout.add_to_list_content, container, false);
			initUI(view);
			setEmptyList();
			loadShoppingList();
		}
		return view;
	}

	private AsyncTask<String, String, ShoppingListsResponse> loadShoppingList() {
		return getShoppingLists().execute();
	}

	private void setEmptyList() {
		EmptyCartView emptyCartView = new EmptyCartView(view, this);
		emptyCartView.setView(getString(R.string.title_no_shopping_lists), getString(R.string.description_no_shopping_lists), getString(R.string.button_no_shopping_lists), R.drawable.emptylists);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	private void initUI(View view) {
		rcvShoppingLists = view.findViewById(R.id.rclAddToList);
		rlNoConnectionLayout = view.findViewById(R.id.no_connection_layout);
		mBtnCancel = view.findViewById(R.id.btnCancel);
		imCreateList = view.findViewById(R.id.imCreateList);
		mProgressBar = view.findViewById(R.id.pbAddToList);
		relProgressBar = view.findViewById(R.id.relProgressBar);
		pbLoadShoppingList = view.findViewById(R.id.pbLoadShoppingList);
		relEmptyStateHandler = view.findViewById(R.id.relEmptyView);
		flCancelButton = view.findViewById(R.id.flCancelButton);
		btnGoToProduct = view.findViewById(R.id.btnGoToProduct);
		btnRetry = view.findViewById(R.id.btnRetry);
		imCreateList.setOnClickListener(this);
		imCreateList.setTag(R.drawable.add_black);
		mBtnCancel.setOnClickListener(this);
		btnGoToProduct.setOnClickListener(this);
		btnRetry.setOnClickListener(this);
		Activity activity = getActivity();
		if (activity != null) {
			mErrorHandlerView = new ErrorHandlerView(activity);
			mErrorHandlerView.setMargin(rlNoConnectionLayout, 0, 0, 0, 0);
			mConnectionBroadcast = Utils.connectionBroadCast(activity, this);
		}

		EmptyCartView emptyCartView = new EmptyCartView(view, this);
		emptyCartView.setView(getString(R.string.title_no_shopping_lists), getString(R.string.description_no_shopping_lists), getString(R.string.button_no_shopping_lists), R.drawable.emptylists);
		emptyCartView.buttonVisibility(getString(R.string.app_label));
	}

	private void setAdapter(RecyclerView rcvShoppingLists, ShoppingListsResponse response) {
		Activity activity = getActivity();
		if (activity != null) {
			if (response != null) {
				List<ShoppingList> shoppingLists = response.lists;
				if (shoppingLists != null && shoppingLists.size() == 0) {
					showEmptyListView();
					recyclerViewVisibility(false);
					return;
				}

				recyclerViewVisibility(true);
				mShoppingListAdapter = new AddToListAdapter(response.lists, this);
				LinearLayoutManager mLayoutManager = new LinearLayoutManager(activity);
				mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
				rcvShoppingLists.setLayoutManager(mLayoutManager);
				rcvShoppingLists.setAdapter(mShoppingListAdapter);
			}
		}
	}

	private void recyclerViewHeight(RecyclerView rcvShoppingLists, ShoppingListsResponse shoppingListsResponse) {
		if (shoppingListsResponse == null)
			return;

		ViewGroup.LayoutParams paramsRecyclerView = rcvShoppingLists.getLayoutParams();
		List<ShoppingList> list = shoppingListsResponse.lists;
		if (list.size() <= 4) {
			paramsRecyclerView.height = RecyclerView.LayoutParams.WRAP_CONTENT;
		} else {
			DisplayMetrics displayMetrics = new DisplayMetrics();
			Activity activity = getActivity();
			if (activity != null) {
				activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
				paramsRecyclerView.height = (2 * displayMetrics.heightPixels) / 5;
			}
		}
		rcvShoppingLists.setLayoutParams(paramsRecyclerView);
	}

	@Override
	public void onItemClick(ShoppingList shoppingList, boolean activate) {
		mBtnCancel.setText(activate ? getString(R.string.ok) : getString(R.string.cancel));
	}

	@Override
	public void onClick(View view) {
		MultiClickPreventer.preventMultiClick(view);
		switch (view.getId()) {
			case R.id.btnGoToProduct:
				navigateToCreateNewListFragment();
				break;
			case R.id.imCreateList:
				if (imCreateList.getAlpha() == 1.0) {  //enable create list only if alpha is 1.0
					if (imCreateList.getTag() != null) {
						int resourceID = (int) imCreateList.getTag();
						switch (resourceID) {
							case R.drawable.close_24:
								onOkButtonClicked();
								break;
							default:
								navigateToCreateNewListFragment();
								break;
						}
					}
				}
				break;
			case R.id.btnCancel:
				onOkButtonClicked();
				break;

			case R.id.btnRetry:
				if (new ConnectionDetector().isOnline(getActivity())) {
					onConnectionFailure(false);
					loadShoppingList();
				}
				break;

			default:
				break;
		}
	}

	private void onOkButtonClicked() {
		String label = mBtnCancel.getText().toString();
		Activity act = getActivity();
		if (act != null) {
			if (label.toLowerCase().equalsIgnoreCase("ok")) {
				List<AddToListRequest> addToLists = getAddToListRequests();
				WoolworthsApplication woolworthsApplication = WoolworthsApplication.getInstance();
				if (woolworthsApplication != null) {
					WGlobalState globalState = woolworthsApplication.getWGlobalState();
					if (mShoppingListAdapter != null)
						globalState.setShoppingListRequest(mShoppingListAdapter.getList());
				}
				postAddToList(addToLists);
				return;
			}
			((CustomPopUpWindow) act).startExitAnimation();
		}
	}

	private void navigateToCreateNewListFragment() {
		Activity activity = getActivity();
		Bundle bundle = new Bundle();
		CreateListFragment createListFragment = new CreateListFragment();
		List<AddToListRequest> addToList = getAddToListRequests();
		bundle.putString("ADD_TO_LIST_ITEMS", Utils.objectToJson(addToList));
		createListFragment.setArguments(bundle);
		if (activity != null) {
			CustomPopUpWindow customPopUpWindow = (CustomPopUpWindow) activity;
			FragmentManager fragmentManager = customPopUpWindow.getSupportFragmentManager();
			fragmentManager.beginTransaction()
					.replace(R.id.flShoppingListContainer, createListFragment)
					.setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
					.addToBackStack(null)
					.commitAllowingStateLoss();
		}
	}

	@NonNull
	private List<AddToListRequest> getAddToListRequests() {
		List<AddToListRequest> addToListRequests = new ArrayList<>();
		if (mShoppingListAdapter != null) {
			List<ShoppingList> shoppingLists = mShoppingListAdapter.getList();
			if (shoppingLists != null || shoppingLists.size() != 0)
				for (ShoppingList spl : shoppingLists) {
					if (spl.viewIsSelected) {
						if (!TextUtils.isEmpty(getSelectedSKU().sku)) {
							AddToListRequest addToListRequest = new AddToListRequest();
							addToListRequest.setGiftListId(spl.listId);
							addToListRequest.setCatalogRefId(getSelectedSKU().sku);
							addToListRequest.setQuantity("1");
							addToListRequest.setSkuID(getSelectedSKU().sku);
							addToListRequests.add(addToListRequest);
						}
					}
				}
			return addToListRequests;
		}
		return addToListRequests;
	}

	private void postAddToList(List<AddToListRequest> addToListRequests) {
		if (addToListRequests.size() > 0) {
			mPostAddToList = addToList(addToListRequests, addToListRequests.get(0).getGiftListId());
			mPostAddToList.execute();
		}
	}

	public PostAddToList addToList(final List<AddToListRequest> addToListRequest, String listId) {
		final int sizeOfList = addToListRequest.size();
		onLoad(true);
		return new PostAddToList(new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				ShoppingListItemsResponse addToListResponse = (ShoppingListItemsResponse) object;
				Activity activity = getActivity();
				if (activity != null) {
					switch (addToListResponse.httpCode) {
						case 200:
							if (apiCount < sizeOfList) {
								PostAddToList postAddToList = addToList(addToListRequest, addToListRequest.get(apiCount).getGiftListId());
								postAddToList.execute();
							} else {
								((CustomPopUpWindow) activity).startExitAnimation();
								Utils.sendBus(new ProductState(sizeOfList, CLOSE_PDP_FROM_ADD_TO_LIST));
								onLoad(false);
							}
							break;
						default:
							Response response = addToListResponse.response;
							if (response.desc != null) {
								Utils.displayValidationMessage(activity, CustomPopUpWindow.MODAL_LAYOUT.ERROR, response.desc, true);
							}
							onLoad(false);
							break;
					}
					apiCount = apiCount + 1;
					setAddToListHasFail(false);
				}
			}

			@Override
			public void onFailure(String e) {
				Activity activity = getActivity();
				if (activity != null) {
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							mErrorHandlerView.showToast();
							onLoad(false);
							setAddToListHasFail(true);
						}
					});
				}
			}
		}, addToListRequest, listId);
	}

	private void setAddToListHasFail(boolean value) {
		addToListHasFail = value;
	}

	private void onLoad(boolean isLoading) {
		imCreateList.setImageAlpha(isLoading ? 120 : 255);
		mProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
		mBtnCancel.setVisibility(isLoading ? View.GONE : View.VISIBLE);
	}

	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver();
	}

	private void unregisterReceiver() {
		Activity activity = getActivity();
		if (activity != null) {
			activity.unregisterReceiver(mConnectionBroadcast);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		registerReceiver();
	}

	private void registerReceiver() {
		Activity activity = getActivity();
		if (activity != null) {
			activity.registerReceiver(mConnectionBroadcast,
					new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
		}
	}

	public OtherSkus getSelectedSKU() {
		WoolworthsApplication woolworthsApplication = WoolworthsApplication.getInstance();
		if (woolworthsApplication != null) {
			WGlobalState globalState = woolworthsApplication.getWGlobalState();
			return globalState.getSelectedSKUId();
		}
		return null;
	}

	@Override
	public void onConnectionChanged() {
		if (addToListHasFail) {
			apiCount = 0;
			mBtnCancel.performClick();
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		if (mPostAddToList != null) {
			if (!mPostAddToList.isCancelled())
				mPostAddToList.cancel(true);
		}
	}

	protected GetShoppingLists getShoppingLists() {
		showShoppingListLoader(true);
		recyclerViewVisibility(true);
		return new GetShoppingLists(new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				ShoppingListsResponse shoppingListsResponse = (ShoppingListsResponse) object;
				switch (shoppingListsResponse.httpCode) {
					case 200:
						recyclerViewHeight(rcvShoppingLists, shoppingListsResponse);
						setAdapter(rcvShoppingLists, shoppingListsResponse);
						break;
					default:
						recyclerViewVisibility(false);
						break;
				}
				showShoppingListLoader(false);
			}

			@Override
			public void onFailure(String message) {
				showShoppingListLoader(false);
				recyclerViewVisibility(false);
				onConnectionFailure(true);
			}
		});
	}

	private void showShoppingListLoader(boolean isLoading) {
		imCreateList.setImageAlpha(isLoading ? 120 : 255);
		imCreateList.setEnabled(!isLoading);
		pbLoadShoppingList.setVisibility(isLoading ? View.VISIBLE : View.GONE);
		relProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
	}

	private void recyclerViewVisibility(boolean visible) {
		rcvShoppingLists.setVisibility(visible ? View.VISIBLE : View.GONE);
	}

	private void showEmptyListView() {
		rcvShoppingLists.setVisibility(View.GONE);
		flCancelButton.setVisibility(View.GONE);
		rlNoConnectionLayout.setVisibility(View.GONE);
		relEmptyStateHandler.setVisibility(View.VISIBLE);
		imCreateList.setImageResource(R.drawable.close_24);
		imCreateList.setTag(R.drawable.close_24);
	}

	public void onConnectionFailure(final boolean enable) {
		Activity activity = getActivity();
		if (activity != null) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					imCreateList.setImageResource(enable ? R.drawable.close_24 : R.drawable.add_black);
					rlNoConnectionLayout.setVisibility(enable ? View.VISIBLE : View.GONE);
					rcvShoppingLists.setVisibility(enable ? View.GONE : View.GONE);
					flCancelButton.setVisibility(enable ? View.GONE : View.VISIBLE);
				}
			});
		}
	}

	@Override
	public void onEmptyCartRetry() {
		Activity activity = getActivity();
		Bundle bundle = new Bundle();
		CreateListFragment createListFragment = new CreateListFragment();
		List<AddToListRequest> addToList = getAddToListRequests();
		bundle.putString("OPEN_FROM_POPUP", Utils.objectToJson(addToList));

		createListFragment.setArguments(bundle);
		if (activity != null) {
			CustomPopUpWindow customPopUpWindow = (CustomPopUpWindow) activity;
			FragmentManager fragmentManager = customPopUpWindow.getSupportFragmentManager();
			fragmentManager.beginTransaction()
					.replace(R.id.flShoppingListContainer, createListFragment)
					.setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
					.commitAllowingStateLoss();
		}
	}
}
