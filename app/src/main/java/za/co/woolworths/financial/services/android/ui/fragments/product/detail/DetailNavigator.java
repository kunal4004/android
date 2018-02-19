package za.co.woolworths.financial.services.android.ui.fragments.product.detail;

import android.content.Context;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.AddItemToCartResponse;
import za.co.woolworths.financial.services.android.models.dto.OtherSkus;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.StoreDetails;
import za.co.woolworths.financial.services.android.models.dto.TokenValidationResponse;
import za.co.woolworths.financial.services.android.models.dto.WProduct;
import za.co.woolworths.financial.services.android.models.dto.WProductDetail;

public interface DetailNavigator {

	void renderView();

	void closeView(View view);

	void nestedScrollViewHelper();

	void setUpImageViewPager();

	void defaultProduct();

	void setProductName();

	void onLoadStart();

	void onLoadComplete();

	void addToShoppingList();

	String getImageByWidth(String imageUrl, Context context);

	List<String> getAuxiliaryImage();

	void onSuccessResponse(WProduct strProduct);

	void onFailureResponse(String s);

	void disableStoreFinder();

	void responseFailureHandler(Response response);

	void enableFindInStoreButton(WProductDetail productList);

	void setLayoutWeight(View v, float weight);

	void setIngredients(String string);

	void setProductCode(String productCode);

	void setProductDescription(String productDescription);

	void setSelectedSize(OtherSkus sku);

	void setPrice(OtherSkus otherSkus);

	void setAuxiliaryImages(ArrayList<String> auxiliaryImages);

	void setSelectedTextColor(OtherSkus otherSkus);

	void removeAllDots();

	void setupPagerIndicatorDots(int size);

	void colorSizeContainerVisibility(int size);

	void setColorList(List<OtherSkus> skuList);

	void setSizeList(List<OtherSkus> skuList);

	void onSizeItemClicked(OtherSkus otherSkus);

	void onColourItemClicked(OtherSkus otherSkus);

	// find in-store
	void startLocationUpdates();

	void stopLocationUpdate();

	void showFindInStoreProgress();

	void dismissFindInStoreProgress();

	void onLocationItemSuccess(List<StoreDetails> location);

	void noStockAvailable();

	void onPermissionGranted();

	// add item to cart
	void apiIdentifyTokenValidation();

	void onTokenFailure(String e);

	void onSessionTokenValid();

	void onTokenValidationFailure(TokenValidationResponse tokenValidationResponse);

	void onAddToCartLoad();

	void onAddToCartLoadComplete();

	void apiAddItemToCart();

	void addItemToCartResponse(AddItemToCartResponse addItemToCartResponse);

	void onSessionTokenInValid(TokenValidationResponse addItemToCartResponse);

	void onAddItemToCartFailure(AddItemToCartResponse addItemToCartResponse);

	void onAddItemToCartFailure(String error);
}
