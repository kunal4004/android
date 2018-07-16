package za.co.woolworths.financial.services.android.ui.fragments.product.detail;

import android.text.TextUtils;

import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse;
import za.co.woolworths.financial.services.android.models.rest.product.GetInventorySkusForStore;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.Utils;

public abstract class InventoryForStore {

	protected abstract void onInventoryForStoreSuccess(SkusInventoryForStoreResponse skusInventoryForStoreResponse);

	protected abstract void onInventoryForStoreFailure(String message);

	protected abstract void onNoMatchFoundForStoreId();

	private GetInventorySkusForStore getInventorySkusForStore;

	public InventoryForStore(String fulFillmentTypeId, String multiSku) {
		String storeId = Utils.retrieveStoreId(fulFillmentTypeId);
		if (TextUtils.isEmpty(storeId)) { // no storeId match found, cannot perform Inventory check
			onNoMatchFoundForStoreId();
			return;
		}
		executeGextInventoryTask(storeId, multiSku);
	}

	private void executeGextInventoryTask(String storeId, String multiSku) {
		getInventorySkusForStore = getInventoryStockForStore(storeId, multiSku);
		getInventorySkusForStore.execute();
	}

	private GetInventorySkusForStore getInventoryStockForStore(String storeId, String multiSku) {
		return new GetInventorySkusForStore(storeId, multiSku, new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				onInventoryForStoreSuccess((SkusInventoryForStoreResponse) object);
			}

			@Override
			public void onFailure(String errorMessage) {
				onInventoryForStoreFailure(errorMessage);
			}
		});
	}

	public void cancelInventoryForStoreCall() {
		if (getInventorySkusForStore == null) return;
		if (!getInventorySkusForStore.isCancelled())
			getInventorySkusForStore.cancel(true);
	}
}
