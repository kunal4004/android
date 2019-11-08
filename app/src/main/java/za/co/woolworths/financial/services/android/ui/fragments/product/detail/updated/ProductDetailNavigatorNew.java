package za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated;

import android.content.Context;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.AddItemToCartResponse;
import za.co.woolworths.financial.services.android.models.dto.CartSummaryResponse;
import za.co.woolworths.financial.services.android.models.dto.OtherSkus;
import za.co.woolworths.financial.services.android.models.dto.ProductDetails;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse;
import za.co.woolworths.financial.services.android.models.dto.StoreDetails;
import za.co.woolworths.financial.services.android.models.dto.WProduct;
import za.co.woolworths.financial.services.android.models.dto.WProductDetail;

/**
 * Created by W7099877 on 2018/07/14.
 */

public interface ProductDetailNavigatorNew {

	String getImageByWidth(String imageUrl, Context context);

	void onSuccessResponse(ProductDetails productDetails);

	void onFailureResponse(String s);

	void requestDeliveryLocation(String requestMessage);

	void responseFailureHandler(Response response);

	void setProductCode(String productCode);

	void setProductDescription(String productDescription);
	// find in-store
	void startLocationUpdates();

	void stopLocationUpdate();

	void dismissFindInStoreProgress();

	void onLocationItemSuccess(List<StoreDetails> location);

	void outOfStockDialog();

	void onTokenFailure(String e);

	void onCartSummarySuccess(CartSummaryResponse cartSummaryResponse);

	void addItemToCartResponse(AddItemToCartResponse addItemToCartResponse);

	void onAddItemToCartFailure(String error);

	void onSessionTokenExpired();

	void onUpdatedInventoryResponse(SkusInventoryForStoreResponse inventoryResponse);

	void onInventoryResponseForAllSKUs(SkusInventoryForStoreResponse inventoryResponse);

	void onProductDetailedFailed(Response response);

	void showOutOfStockInStores();
}
