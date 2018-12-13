package za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.AddItemToCart;
import za.co.woolworths.financial.services.android.models.dto.AddItemToCartResponse;
import za.co.woolworths.financial.services.android.models.dto.AddToCartDaTum;
import za.co.woolworths.financial.services.android.models.dto.CartSummaryResponse;
import za.co.woolworths.financial.services.android.models.dto.FormException;
import za.co.woolworths.financial.services.android.models.dto.LocationResponse;
import za.co.woolworths.financial.services.android.models.dto.OtherSkus;
import za.co.woolworths.financial.services.android.models.dto.ProductDetailResponse;
import za.co.woolworths.financial.services.android.models.dto.ProductDetails;
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse;
import za.co.woolworths.financial.services.android.models.dto.StoreDetails;
import za.co.woolworths.financial.services.android.models.rest.product.GetCartSummary;
import za.co.woolworths.financial.services.android.models.rest.product.GetInventorySkusForStore;
import za.co.woolworths.financial.services.android.models.rest.product.GetProductDetail;
import za.co.woolworths.financial.services.android.models.rest.product.PostAddItemToCart;
import za.co.woolworths.financial.services.android.models.rest.product.ProductRequest;
import za.co.woolworths.financial.services.android.ui.base.BaseViewModel;
import za.co.woolworths.financial.services.android.util.LocationItemTask;
import za.co.woolworths.financial.services.android.util.OnEventListener;

/**
 * Created by W7099877 on 2018/07/14.
 */

public class ProductDetailsViewModelNew extends BaseViewModel<ProductDetailNavigatorNew> {

	public GetProductDetail productDetail(ProductRequest productRequest) {
		return new GetProductDetail(productRequest, new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				ProductDetailResponse productDetailResponse = (ProductDetailResponse) object;
				switch (productDetailResponse.httpCode) {
					case 200:
						getNavigator().onSuccessResponse(productDetailResponse.product);
						break;
					default:
						if (productDetailResponse.response != null) {
							getNavigator().onProductDetailedFailed(productDetailResponse.response);
						}
						break;
				}
			}

			@Override
			public void onFailure(String e) {
				getNavigator().onFailureResponse(e.toString());
			}
		});
	}

	public LocationItemTask locationItemTask(final Context context, OtherSkus otherSkus) {
		return new LocationItemTask(new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				if (object != null) {
					List<StoreDetails> location = ((LocationResponse) object).Locations;
					if (location != null && location.size() > 0) {
						getNavigator().onLocationItemSuccess(location);
					} else {
						getNavigator().showOutOfStockInStores();
					}
				}
				getNavigator().dismissFindInStoreProgress();
			}

			@Override
			public void onFailure(final String e) {
				if (context != null) {
					Activity activity = (Activity) context;
					if (activity != null) {
						getNavigator().dismissFindInStoreProgress();
					}
				}
			}
		}, otherSkus);
	}

	public String getProductDescription(Context context, ProductDetails productDetails) {

		if (context == null)
			return "";

		String head = "<head>" +
				"<meta charset=\"UTF-8\">" +
				"<style>" +
				"@font-face {font-family: 'myriad-pro-regular';src: url('file://"
				+ context.getFilesDir().getAbsolutePath() + "/fonts/myriadpro_regular.otf');}" +
				"body {" +
				"line-height: 110%;" +
				"font-size: 92% !important;" +
				"text-align: justify;" +
				"color:grey;" +
				"font-family:'myriad-pro-regular';}" +
				"</style>" +
				"</head>";

		String descriptionWithoutExtraTag = "";
		if (productDetails != null) {
			if (!TextUtils.isEmpty(productDetails.longDescription)) {
				descriptionWithoutExtraTag = productDetails.longDescription
						.replaceAll("</ul>\n\n<ul>\n", " ")
						.replaceAll("<p>&nbsp;</p>", "")
						.replaceAll("<ul><p>&nbsp;</p></ul>", " ");
			}
		}
		String htmlData = "<!DOCTYPE html><html>"
				+ head
				+ "<body>"
				+ isEmpty(descriptionWithoutExtraTag)
				+ "</body></html>";
		return htmlData;
	}


	public String maxWasPrice(List<OtherSkus> otherSku) {
		ArrayList<Double> priceList = new ArrayList<>();
		for (OtherSkus os : otherSku) {
			if (!TextUtils.isEmpty(os.wasPrice)) {
				priceList.add(Double.valueOf(os.wasPrice));
			}
		}
		String wasPrice = "";
		if (priceList.size() > 0) {
			wasPrice = String.valueOf(Collections.max(priceList));
		}
		return wasPrice;
	}

	protected GetCartSummary getCartSummary() {
		return new GetCartSummary(new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				if (object != null) {
					CartSummaryResponse cartSummaryResponse = (CartSummaryResponse) object;
					if (cartSummaryResponse != null) {
						switch (cartSummaryResponse.httpCode) {
							case 200:
								getNavigator().onCartSummarySuccess(cartSummaryResponse);
								break;

							case 440:
								if (cartSummaryResponse.response != null)
									getNavigator().onSessionTokenExpired();
								break;

							default:
								getNavigator().responseFailureHandler(cartSummaryResponse.response);
								break;
						}
					}
				}
			}

			@Override
			public void onFailure(String e) {
				getNavigator().onTokenFailure(e);
			}
		});
	}

	protected PostAddItemToCart postAddItemToCart(List<AddItemToCart> addItemToCart) {
		return new PostAddItemToCart(addItemToCart, new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				if (object != null) {
					AddItemToCartResponse addItemToCartResponse = (AddItemToCartResponse) object;
					if (addItemToCartResponse != null) {
						switch (addItemToCartResponse.httpCode) {
							case 200:
								List<AddToCartDaTum> addToCartList = addItemToCartResponse.data;
								if (addToCartList != null && addToCartList.size() > 0 && addToCartList.get(0).formexceptions != null) {
									FormException formException = addToCartList.get(0).formexceptions.get(0);
									if (formException != null) {
										if (formException.message.toLowerCase().contains("some of the products chosen are out of stock")) {
											addItemToCartResponse.response.desc = "Unfortunately this item is currently out of stock.";
										} else {
											addItemToCartResponse.response.desc = formException.message;
										}
										getNavigator().responseFailureHandler(addItemToCartResponse.response);
										return;
									}
								}
								if (addToCartList != null) {
									getNavigator().addItemToCartResponse(addItemToCartResponse);
								}
								break;

							case 440:
								if (addItemToCartResponse.response != null)
									getNavigator().onSessionTokenExpired();
								break;

							default:
								if (addItemToCartResponse.response != null)
									getNavigator().responseFailureHandler(addItemToCartResponse.response);
								break;
						}
					}
				}
			}

			@Override
			public void onFailure(String e) {
				getNavigator().onAddItemToCartFailure(e);
			}
		});
	}

	public GetInventorySkusForStore queryInventoryForSKUs(String storeId, String multiSku, final boolean isMultiSKUs) {
		return new GetInventorySkusForStore(storeId, multiSku, new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				SkusInventoryForStoreResponse skusInventoryForStoreResponse = (SkusInventoryForStoreResponse) object;
				switch (skusInventoryForStoreResponse.httpCode) {
					case 200:
						if (isMultiSKUs)
							getNavigator().onInventoryResponseForAllSKUs(skusInventoryForStoreResponse);
						else
							getNavigator().onInventoryResponseForSelectedSKU(skusInventoryForStoreResponse);
						break;
					default:
						if (skusInventoryForStoreResponse.response != null)
							getNavigator().responseFailureHandler(skusInventoryForStoreResponse.response);
						break;
				}
			}

			@Override
			public void onFailure(String e) {

			}
		});
	}

	public String getMultiSKUsStringForInventory(ArrayList<OtherSkus> otherSKUsList) {
		List<String> skuIds = new ArrayList<>();
		for (OtherSkus otherSkus : otherSKUsList) {
			skuIds.add(otherSkus.sku);
		}
		String multiSKUS = TextUtils.join("-", skuIds);
		return multiSKUS;
	}
}