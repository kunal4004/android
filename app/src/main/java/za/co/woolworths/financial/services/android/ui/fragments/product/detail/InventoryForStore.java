package za.co.woolworths.financial.services.android.ui.fragments.product.detail;

import android.text.TextUtils;

import retrofit2.Call;
import za.co.woolworths.financial.services.android.contracts.IResponseListener;
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse;
import za.co.woolworths.financial.services.android.models.network.CompletionHandler;
import za.co.woolworths.financial.services.android.models.network.OneAppService;
import za.co.woolworths.financial.services.android.util.Utils;

public abstract class InventoryForStore {

	protected abstract void onInventoryForStoreSuccess(SkusInventoryForStoreResponse skusInventoryForStoreResponse);

	protected abstract void onInventoryForStoreFailure(String message);

	protected abstract void onNoMatchFoundForStoreId();

	private Call<SkusInventoryForStoreResponse> getInventorySkusForStore;

	private boolean onConnectivityFailure;

	InventoryForStore(String fulFillmentTypeId, String multiSku) {
		String storeId = Utils.retrieveStoreId(fulFillmentTypeId);
		if (TextUtils.isEmpty(storeId)) { // no storeId match found, cannot perform Inventory check
			onNoMatchFoundForStoreId();
			return;
		}
		executeGetInventoryTask(storeId, multiSku);
	}

	private void executeGetInventoryTask(String storeId, String multiSku) {
		getInventorySkusForStore = getInventoryStockForStore(storeId, multiSku);
	}

	private Call<SkusInventoryForStoreResponse> getInventoryStockForStore(String storeId, String multiSku) {
		setOnConnectFailure(false);

	Call<SkusInventoryForStoreResponse> skusInventoryForStoreRequestCall =  OneAppService.INSTANCE.getInventorySkuForStore(storeId, multiSku);
		skusInventoryForStoreRequestCall.enqueue(new CompletionHandler<>(new IResponseListener<SkusInventoryForStoreResponse>() {
			@Override
			public void onSuccess(SkusInventoryForStoreResponse skusInventoryForStoreResponse) {
				onInventoryForStoreSuccess(skusInventoryForStoreResponse);
			}

			@Override
			public void onFailure(Throwable error) {
				if (error !=null) {
					onInventoryForStoreFailure(error.getMessage());
					setOnConnectFailure(true);
				}
			}
		},SkusInventoryForStoreResponse.class));

		return skusInventoryForStoreRequestCall;
	}

	public void cancelInventoryForStoreCall() {
		if (getInventorySkusForStore != null && !getInventorySkusForStore.isCanceled())
			getInventorySkusForStore.cancel();
	}

	private void setOnConnectFailure(boolean isConnected) {
		this.onConnectivityFailure = isConnected;
	}

	public boolean getOnConnectFailure() {
		return onConnectivityFailure;
	}
}
