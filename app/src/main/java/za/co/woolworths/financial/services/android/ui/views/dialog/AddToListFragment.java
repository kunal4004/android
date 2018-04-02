package za.co.woolworths.financial.services.android.ui.views.dialog;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
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
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.awfs.coordination.R;
import com.google.gson.Gson;

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
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.PostAddToList;
import za.co.woolworths.financial.services.android.models.service.event.ProductState;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.adapters.AddToListAdapter;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.MultiClickPreventer;
import za.co.woolworths.financial.services.android.util.NetworkChangeListener;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.Utils;

import static za.co.woolworths.financial.services.android.models.service.event.ProductState.CLOSE_PDP_FROM_ADD_TO_LIST;

public class AddToListFragment extends Fragment implements View.OnClickListener, AddToListInterface, NetworkChangeListener {

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
		}
		return view;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	private ShoppingListsResponse getShoppingListsResponse() {
		return new Gson().fromJson((TextUtils.isEmpty(mShoppingResponse)) ? "" : mShoppingResponse, ShoppingListsResponse.class);
	}

	private void initUI(View view) {
		RecyclerView rcvShoppingLists = view.findViewById(R.id.rclAddToList);
		mBtnCancel = view.findViewById(R.id.btnCancel);
		imCreateList = view.findViewById(R.id.imCreateList);
		mProgressBar = view.findViewById(R.id.pbAddToList);
		ShoppingListsResponse shoppingResponse = getShoppingListsResponse();
		recyclerViewHeight(rcvShoppingLists, shoppingResponse);
		setAdapter(rcvShoppingLists);
		imCreateList.setOnClickListener(this);
		mBtnCancel.setOnClickListener(this);
		Activity activity = getActivity();
		if (activity != null) {
			mErrorHandlerView = new ErrorHandlerView(activity);
			mConnectionBroadcast = Utils.connectionBroadCast(activity, this);
		}
	}

	private void setAdapter(RecyclerView rcvShoppingLists) {
		Activity activity = getActivity();
		if (activity != null) {
			mShoppingListAdapter = new AddToListAdapter(getShoppingListsResponse().lists, this);
			LinearLayoutManager mLayoutManager = new LinearLayoutManager(activity);
			mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
			rcvShoppingLists.setLayoutManager(mLayoutManager);
			rcvShoppingLists.setAdapter(mShoppingListAdapter);
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
			case R.id.imCreateList:
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
							.addToBackStack(null)
							.commitAllowingStateLoss();
				}
				break;

			case R.id.btnCancel:
				String label = mBtnCancel.getText().toString();
				if (label.toLowerCase().equalsIgnoreCase("ok")) {
					postAddToList(getAddToListRequests());
					return;
				}
				Activity act = getActivity();
				if (act != null) {
					((CustomPopUpWindow) act).startExitAnimation();
				}
				break;

			default:
				break;
		}
	}

	@NonNull
	private List<AddToListRequest> getAddToListRequests() {
		List<AddToListRequest> addToListRequests = new ArrayList<>();
		for (ShoppingList spl : mShoppingListAdapter.getList()) {
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
								Utils.sendBus(new ProductState(CLOSE_PDP_FROM_ADD_TO_LIST));
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
		imCreateList.setEnabled(!isLoading);
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
}
