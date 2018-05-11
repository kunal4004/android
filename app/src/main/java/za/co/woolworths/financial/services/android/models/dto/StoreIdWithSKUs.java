package za.co.woolworths.financial.services.android.models.dto;

import java.util.ArrayList;

/**
 * Created by W7099877 on 2018/05/11.
 */

public class StoreIdWithSKUs {
	public String fulFillmentStoreId;
	public ArrayList<String> skus;

	public String getFulFillmentStoreId() {
		return fulFillmentStoreId;
	}

	public void setFulFillmentStoreId(String fulFillmentStoreId) {
		this.fulFillmentStoreId = fulFillmentStoreId;
	}

	public ArrayList<String> getSkus() {
		return skus;
	}

	public void setSkus(ArrayList<String> skus) {
		this.skus = skus;
	}
}
