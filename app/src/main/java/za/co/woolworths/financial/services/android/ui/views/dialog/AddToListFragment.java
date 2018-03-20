package za.co.woolworths.financial.services.android.ui.views.dialog;

import android.app.Activity;
import android.os.Bundle;
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

import com.awfs.coordination.R;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.AddToListRequest;
import za.co.woolworths.financial.services.android.models.dto.ShoppingList;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.PostAddToList;
import za.co.woolworths.financial.services.android.ui.activities.ConfirmColorSizeActivity;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.adapters.AddToListAdapter;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.util.MultiClickPreventer;
import za.co.woolworths.financial.services.android.util.OnEventListener;

public class AddToListFragment extends Fragment implements View.OnClickListener, AddToListInterface {

	private String mShoppingResponse = "";
	private WButton mBtnCancel;
	private AddToListAdapter mShoppingListAdapter;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			if (getArguments().containsKey("LIST_PAYLOAD")) {
				mShoppingResponse = getArguments().getString("LIST_PAYLOAD");
			}
		}
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.add_to_list_content, container, false);
		return view;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initUI(view);
	}

	private ShoppingListsResponse getShoppingListsResponse() {
		return new Gson().fromJson((TextUtils.isEmpty(mShoppingResponse)) ? "" : mShoppingResponse, ShoppingListsResponse.class);
	}

	private void initUI(View view) {
		RecyclerView rcvShoppingLists = view.findViewById(R.id.rclAddToList);
		mBtnCancel = view.findViewById(R.id.btnCancel);
		ImageView imCreateList = view.findViewById(R.id.imCreateList);
		ShoppingListsResponse shoppingResponse = getShoppingListsResponse();
		recyclerViewHeight(rcvShoppingLists, shoppingResponse);
		setAdapter(rcvShoppingLists);
		imCreateList.setOnClickListener(this);
		mBtnCancel.setOnClickListener(this);
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
				EnterNewListFragment enterNewListFragment = new EnterNewListFragment();
				if (activity != null) {
					CustomPopUpWindow customPopUpWindow = (CustomPopUpWindow) activity;
					FragmentManager fragmentManager = customPopUpWindow.getSupportFragmentManager();
					ConfirmColorSizeActivity
					fragmentManager.beginTransaction()
							.replace(R.id.flShoppingListContainer, enterNewListFragment)
							.addToBackStack(null)
							.commitAllowingStateLoss();
				}
				break;

			case R.id.btnCancel:
				String label = mBtnCancel.getText().toString();
				if (label.toLowerCase().equalsIgnoreCase("ok")) {
					List<AddToListRequest> addToListRequests = new ArrayList<>();
					for (ShoppingList spl : mShoppingListAdapter.getList()) {
						if (spl.viewIsSelected) {
							WoolworthsApplication woolworthsApplication = WoolworthsApplication.getInstance();
							if (woolworthsApplication != null) {
								WGlobalState globalState = woolworthsApplication.getWGlobalState();
								AddToListRequest addToListRequest = new AddToListRequest();
								addToListRequest.setGiftListId(spl.listId);
								addToListRequest.setCatalogRefId(globalState.getSelectedSKUId());
								addToListRequest.setQuantity("1");
								addToListRequest.setSkuID(globalState.getSelectedSKUId());
								addToListRequests.add(addToListRequest);
							}
						}
					}

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

	public PostAddToList addToList(List<AddToListRequest> addToListRequest, String listId) {
		return new PostAddToList(new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
			}

			@Override
			public void onFailure(String e) {
			}
		}, addToListRequest, listId);
	}
}
