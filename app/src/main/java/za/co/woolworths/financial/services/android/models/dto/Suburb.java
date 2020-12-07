package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by W7099877 on 31/10/2016.
 */

public class Suburb {

	public String id;
	public String name;
	public boolean suburbDeliverable;

	public boolean hasHeader = false;
	/***
	 *  "fullfilmentStores": [
	 {
	 "suburbId": "1505",
	 "suburbName": "Aanhou Wen",
	 "fulFillmentStoreId": "126",
	 "fulFillmentStoreName": "Somerset Mall Food",
	 "fulFillmentTypeId": "1"
	 }
	 */
	@SerializedName("fulfillmentStores")
	@Expose
	public JsonElement fulfillmentStores;

	public boolean storePickup;

	public StoreAddress storeAddress;

	public boolean storeDeliverable;
}
