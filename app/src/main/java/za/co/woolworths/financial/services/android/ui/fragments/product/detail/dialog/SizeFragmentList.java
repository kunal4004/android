package za.co.woolworths.financial.services.android.ui.fragments.product.detail.dialog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.awfs.coordination.R;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.OtherSkus;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItem;
import za.co.woolworths.financial.services.android.models.dto.SkuInventory;
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse;
import za.co.woolworths.financial.services.android.models.rest.product.GetInventorySkusForStore;
import za.co.woolworths.financial.services.android.ui.activities.ConfirmColorSizeActivity;
import za.co.woolworths.financial.services.android.ui.activities.WStockFinderActivity;
import za.co.woolworths.financial.services.android.ui.adapters.CustomSizePickerAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.StockFinderSizeColorAdapter;
import za.co.woolworths.financial.services.android.util.CenterLayoutManager;
import za.co.woolworths.financial.services.android.util.ColorInterface;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.Utils;

import static za.co.woolworths.financial.services.android.ui.activities.ConfirmColorSizeActivity.RESULT_LOADING_INVENTORY_FAILURE;
import static za.co.woolworths.financial.services.android.ui.fragments.product.detail.ProductDetailFragment.INDEX_ADD_TO_CART;

public class SizeFragmentList extends Fragment implements StockFinderSizeColorAdapter.RecyclerViewClickListener, ColorInterface, CustomSizePickerAdapter.RecyclerViewClickListener {

	private WStockFinderActivity.RecyclerItemSelected mRecyclerItemSelected;
	private RecyclerView mSizeRecycleView;
	private SizeFragmentList mContext;
	private CustomSizePickerAdapter mStockFinderSizeColorAdapter;
	private ArrayList<OtherSkus> mOtherSKUList;
	private boolean mShouldShowPrice;
	private GetInventorySkusForStore mGetInventorySkusForStore;
	private ProgressBar pbLoadInventory;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.color_fragment, container, false);
		return v;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mContext = this;
		try {
			mRecyclerItemSelected = (WStockFinderActivity.RecyclerItemSelected) this.getActivity();
		} catch (ClassCastException ignored) {
		}

		mSizeRecycleView = view.findViewById(R.id.recyclerColorList);
		pbLoadInventory = view.findViewById(R.id.pbLoadInventory);
	}

	@Override
	public void recyclerViewListClicked(View v, int position) {
		mRecyclerItemSelected.onRecyclerItemClick(v, position, "size");
	}

	@Override
	public void onUpdate(final ArrayList<OtherSkus> otherSkuList, final String viewType, final boolean shouldShowPrice) {
		final Activity activity = getActivity();
		if (activity != null) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mOtherSKUList = otherSkuList;
					mShouldShowPrice = shouldShowPrice;
					WoolworthsApplication woolworthsApplication = WoolworthsApplication.getInstance();
					if (woolworthsApplication == null) return;
					if (activity == null) return;
					/**
					 * enable API call for cart item only
					 */
					if (!shouldShowPrice ||
							(woolworthsApplication.getWGlobalState().getSaveButtonClick() != INDEX_ADD_TO_CART)) {
						showInventoryProgressBar(false);
						setSizeAdapter(otherSkuList);
						return;
					}
					List<String> skuIds = new ArrayList<>();
					for (OtherSkus sku : mOtherSKUList) {
						skuIds.add(sku.sku);
					}
					String multiSKU = TextUtils.join("-", skuIds);
					ConfirmColorSizeActivity confirmColorSizeActivity = (ConfirmColorSizeActivity) activity;
					String storeId = confirmColorSizeActivity.getFulFillMentStoreId();
					// no store found, display size with find in-store button to the right.
					if (TextUtils.isEmpty(storeId)) {
						setSizeAdapter(otherSkuList);
						return;
					}

					//store id exist, perform inventory call
					executeGetInventoryForStore(storeId, multiSKU);
				}
			});
		}
	}

	@Override
	public void onUpdate(List<Integer> quantityList) {

	}

	@Override
	public void onUpdate(ShoppingListItem shoppingListItem) {

	}

	private void executeGetInventoryForStore(String storeId, String multiSku) {
		showInventoryProgressBar(true);
		mGetInventorySkusForStore = getInventoryStockForStore(storeId, multiSku);
		mGetInventorySkusForStore.execute();
	}

	private GetInventorySkusForStore getInventoryStockForStore(String storeId, String multiSku) {
		return new GetInventorySkusForStore(storeId, multiSku, new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				SkusInventoryForStoreResponse skusInventoryForStoreResponse = (SkusInventoryForStoreResponse) object;
				switch (skusInventoryForStoreResponse.httpCode) {

					case 200:
						for (SkuInventory skuInventory : skusInventoryForStoreResponse.skuInventory) {

							for (OtherSkus otherSkus : mOtherSKUList) {
								if (skuInventory.sku.equalsIgnoreCase(otherSkus.sku)) {
									otherSkus.quantity = skuInventory.quantity;
								}
							}
						}
						setSizeAdapter(mOtherSKUList);
						break;
					case 440:

						break;
					default:
						/***
						 * close the activity with close down animation and open result
						 * from pdp onActivity result
						 */
						Activity activity = getActivity();
						if (activity != null) {
							ConfirmColorSizeActivity confirmColorSizeActivity = (ConfirmColorSizeActivity) activity;
							if (skusInventoryForStoreResponse == null) return;
							if (skusInventoryForStoreResponse.response == null) return;
							Intent intent = new Intent();
							intent.putExtra("response", Utils.toJson(skusInventoryForStoreResponse.response));
							confirmColorSizeActivity.closeConfirmColorSizeActivity(RESULT_LOADING_INVENTORY_FAILURE, intent);
						}
						break;
				}
				showInventoryProgressBar(false);
			}

			@Override
			public void onFailure(String e) {
				showInventoryProgressBar(false);
			}
		});
	}

	@Override
	public void onDetach() {
		super.onDetach();
		cancelRequest(mGetInventorySkusForStore);
	}

	public void cancelRequest(HttpAsyncTask httpAsyncTask) {
		if (httpAsyncTask != null) {
			if (!httpAsyncTask.isCancelled()) {
				httpAsyncTask.cancel(true);
			}
		}
	}

	private void showInventoryProgressBar(boolean visible) {
		pbLoadInventory.setVisibility(visible ? View.VISIBLE : View.GONE);
		mSizeRecycleView.setVisibility(visible ? View.GONE : View.VISIBLE);
	}

	private void setSizeAdapter(ArrayList<OtherSkus> otherSkuList) {
		Activity activity = getActivity();
		if (activity != null) {
			mSizeRecycleView.setLayoutManager(new CenterLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
			RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(activity) {
				@Override
				protected int getVerticalSnapPreference() {
					return LinearSmoothScroller.SNAP_TO_START;
				}
			};
			mStockFinderSizeColorAdapter = new CustomSizePickerAdapter(otherSkuList, mContext, mShouldShowPrice);
			mSizeRecycleView.setAdapter(mStockFinderSizeColorAdapter);
			mSizeRecycleView.scrollToPosition(0);
			if (mStockFinderSizeColorAdapter != null)
				mStockFinderSizeColorAdapter.notifyDataSetChanged();
			/***
			 * Item out of stock
			 * auto-scroll to out of stock product position in recyclerview
			 */
			ConfirmColorSizeActivity confirmColorSizeActivity = (ConfirmColorSizeActivity) activity;
			mStockFinderSizeColorAdapter.getItemCount();
			String selectedSku = confirmColorSizeActivity.getSelectedSku();
			if (TextUtils.isEmpty(selectedSku)) return;
			final int position = mStockFinderSizeColorAdapter.getPositionBySkuId(selectedSku);
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					mSizeRecycleView.smoothScrollToPosition(position);
				}
			}, 200);
		}
	}

	public ArrayList<OtherSkus> getOtherSKUList() {
		return mOtherSKUList;
	}
}