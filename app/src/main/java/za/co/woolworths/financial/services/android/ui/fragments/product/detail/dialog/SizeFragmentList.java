package za.co.woolworths.financial.services.android.ui.fragments.product.detail.dialog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.awfs.coordination.R;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import za.co.woolworths.financial.services.android.contracts.IResponseListener;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.OtherSkus;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItem;
import za.co.woolworths.financial.services.android.models.dto.SkuInventory;
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse;
import za.co.woolworths.financial.services.android.models.network.CompletionHandler;
import za.co.woolworths.financial.services.android.models.network.OneAppService;
import za.co.woolworths.financial.services.android.ui.activities.ConfirmColorSizeActivity;
import za.co.woolworths.financial.services.android.ui.activities.WStockFinderActivity;
import za.co.woolworths.financial.services.android.ui.adapters.CustomSizePickerAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.StockFinderSizeColorAdapter;
import za.co.woolworths.financial.services.android.util.CenterLayoutManager;
import za.co.woolworths.financial.services.android.util.ColorInterface;
import za.co.woolworths.financial.services.android.util.SessionUtilities;
import za.co.woolworths.financial.services.android.util.Utils;

import static za.co.woolworths.financial.services.android.ui.activities.ConfirmColorSizeActivity.RESULT_LOADING_INVENTORY_FAILURE;
import static za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsFragment.INDEX_ADD_TO_CART;

public class SizeFragmentList extends Fragment implements StockFinderSizeColorAdapter.RecyclerViewClickListener, ColorInterface, CustomSizePickerAdapter.RecyclerViewClickListener {

	private WStockFinderActivity.RecyclerItemSelected mRecyclerItemSelected;
	private RecyclerView mSizeRecycleView;
	private SizeFragmentList mContext;
	private CustomSizePickerAdapter mStockFinderSizeColorAdapter;
	private ArrayList<OtherSkus> mOtherSKUList;
	private boolean mShouldShowPrice;
	private Call<SkusInventoryForStoreResponse> mGetInventorySkusForStore;
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
	public void onOutOfStockItemClicked(OtherSkus otherSkus) {
		Activity activity = getActivity();
		if (activity == null) return;
		if (activity instanceof ConfirmColorSizeActivity) {
			ConfirmColorSizeActivity confirmColorSizeActivity = (ConfirmColorSizeActivity) activity;
			confirmColorSizeActivity.tapOnFindInStoreButton(otherSkus);
		}
	}

	@Override
	public void onUpdate(final ArrayList<OtherSkus> otherSkuList, final String viewType,
						 final boolean shouldShowPrice) {
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
	}

	private Call<SkusInventoryForStoreResponse> getInventoryStockForStore(String storeId, String multiSku) {
		Call<SkusInventoryForStoreResponse> skusInventoryForStoreRequestCall = OneAppService.INSTANCE.getInventorySkuForStore(storeId, multiSku);
		skusInventoryForStoreRequestCall.enqueue(new CompletionHandler<>(new IResponseListener<SkusInventoryForStoreResponse>() {
			@Override
			public void onSuccess(SkusInventoryForStoreResponse skusInventoryForStoreResponse) {
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
						SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, skusInventoryForStoreResponse.response.stsParams, getActivity());
						break;
					default:
						/***
						 * close the activity with close down animation and open result
						 * from pdp onActivity result
						 */
						Activity activity = getActivity();
						if (activity != null) {
							ConfirmColorSizeActivity confirmColorSizeActivity = (ConfirmColorSizeActivity) activity;
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
			public void onFailure(Throwable error) {
				showInventoryProgressBar(false);
			}
		},SkusInventoryForStoreResponse.class));

		return skusInventoryForStoreRequestCall;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		if (mGetInventorySkusForStore != null && !mGetInventorySkusForStore.isCanceled()) {
			mGetInventorySkusForStore.cancel();
		}
	}

	private void showInventoryProgressBar(boolean visible) {
		pbLoadInventory.setVisibility(visible ? View.VISIBLE : View.GONE);
		mSizeRecycleView.setVisibility(visible ? View.GONE : View.VISIBLE);
	}

	private void setSizeAdapter(ArrayList<OtherSkus> otherSkuList) {
		Activity activity = getActivity();
		if (activity != null) {
			mSizeRecycleView.setLayoutManager(new CenterLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false, 1500));
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