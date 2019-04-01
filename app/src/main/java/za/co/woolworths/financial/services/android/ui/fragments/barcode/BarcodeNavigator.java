package za.co.woolworths.financial.services.android.ui.fragments.barcode;

import za.co.woolworths.financial.services.android.models.dto.ProductDetailResponse;
import za.co.woolworths.financial.services.android.models.dto.Response;

public interface BarcodeNavigator {
	void failureResponseHandler(String e);

	void unhandledResponseCode(Response response);

	void onLoadProductSuccess(ProductDetailResponse productDetailResponse, String detailProduct);

	void onLoadStart();

	void noItemFound();
}
