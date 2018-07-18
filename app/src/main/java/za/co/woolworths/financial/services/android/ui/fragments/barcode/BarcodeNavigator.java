package za.co.woolworths.financial.services.android.ui.fragments.barcode;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.ProductDetailResponse;
import za.co.woolworths.financial.services.android.models.dto.ProductDetails;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.WProduct;
import za.co.woolworths.financial.services.android.ui.fragments.product.category.CategoryFragment;

public interface BarcodeNavigator {
	void failureResponseHandler(String e);

	void unhandledResponseCode(Response response);

	void onLoadProductSuccess(ProductDetailResponse productDetailResponse, String detailProduct);

	void onLoadStart();

	void noItemFound();
}
