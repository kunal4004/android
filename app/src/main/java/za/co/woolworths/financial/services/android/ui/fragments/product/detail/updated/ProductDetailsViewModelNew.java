package za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import za.co.woolworths.financial.services.android.contracts.RequestListener;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.AddItemToCart;
import za.co.woolworths.financial.services.android.models.dto.AddItemToCartResponse;
import za.co.woolworths.financial.services.android.models.dto.AddToCartDaTum;
import za.co.woolworths.financial.services.android.models.dto.CartSummaryResponse;
import za.co.woolworths.financial.services.android.models.dto.FormException;
import za.co.woolworths.financial.services.android.models.dto.LocationResponse;
import za.co.woolworths.financial.services.android.models.dto.OtherSkus;
import za.co.woolworths.financial.services.android.models.dto.ProductDetailResponse;
import za.co.woolworths.financial.services.android.models.dto.ProductDetails;
import za.co.woolworths.financial.services.android.models.dto.ProductRequest;
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse;
import za.co.woolworths.financial.services.android.models.dto.StoreDetails;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.models.network.CompletionHandler;
import za.co.woolworths.financial.services.android.models.network.OneAppService;
import za.co.woolworths.financial.services.android.ui.base.BaseViewModel;
import za.co.woolworths.financial.services.android.util.GetCartSummary;
import za.co.woolworths.financial.services.android.util.PostItemToCart;

/**
 * Created by W7099877 on 2018/07/14.
 */

public class ProductDetailsViewModelNew extends BaseViewModel<ProductDetailNavigatorNew> {

    public Call<ProductDetailResponse> productDetail(ProductRequest productRequest) {
        Call<ProductDetailResponse> productDetailRequest = OneAppService.INSTANCE.productDetail(productRequest.getProductId(), productRequest.getSkuId());
        productDetailRequest.enqueue(new CompletionHandler<>(new RequestListener<ProductDetailResponse>() {
            @Override
            public void onSuccess(ProductDetailResponse productDetailResponse) {
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
            public void onFailure(Throwable error) {
                getNavigator().onFailureResponse(error.toString());
            }
        },ProductDetailResponse.class));

        return productDetailRequest;
    }

    public Call<LocationResponse> locationItemTask(final Activity activity, OtherSkus otherSkus) {
        Call<LocationResponse> locationResponseCall;
        WGlobalState mWGlobalState = WoolworthsApplication.getInstance().getWGlobalState();
        if (otherSkus != null) {
            locationResponseCall = OneAppService.INSTANCE.getLocationsItem(otherSkus.sku, String.valueOf(mWGlobalState.getStartRadius()), String.valueOf(mWGlobalState.getEndRadius()));
        } else {
            locationResponseCall = OneAppService.INSTANCE.getLocationsItem(mWGlobalState.getSelectedSKUId().sku, String.valueOf(mWGlobalState.getStartRadius()), String.valueOf(mWGlobalState.getEndRadius()));
        }

        locationResponseCall.enqueue(new CompletionHandler<>(new RequestListener<LocationResponse>() {
            @Override
            public void onSuccess(LocationResponse response) {
                List<StoreDetails> location = response.Locations;
                if (location != null && location.size() > 0) {
                    getNavigator().onLocationItemSuccess(location);
                } else {
                    getNavigator().showOutOfStockInStores();
                }
                getNavigator().dismissFindInStoreProgress();
            }

            @Override
            public void onFailure(Throwable error) {
                if (activity != null) {
                    getNavigator().dismissFindInStoreProgress();
                }
            }
        },LocationResponse.class));

        return locationResponseCall;
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

    protected Call<CartSummaryResponse> getCartSummary() {
        GetCartSummary cartSummary = new GetCartSummary();
        return cartSummary.getCartSummary(new RequestListener<CartSummaryResponse>() {
            @Override
            public void onSuccess(CartSummaryResponse cartSummaryResponse) {
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

            @Override
            public void onFailure(Throwable error) {
                getNavigator().onTokenFailure(error.toString());
            }
        });
    }

    protected void postAddItemToCart(List<AddItemToCart> addItemToCart) {
        PostItemToCart postItemToCart = new PostItemToCart();
        postItemToCart.make(addItemToCart, new RequestListener<AddItemToCartResponse>() {
            @Override
            public void onSuccess(AddItemToCartResponse addItemToCartResponse) {
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

                        case 417:
                            // Preferred Delivery Location has been reset on server
                            // As such, we give the user the ability to set their location again
                            if (addItemToCartResponse.response != null)
                                getNavigator().requestDeliveryLocation(addItemToCartResponse.response.desc);
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

            @Override
            public void onFailure(Throwable error) {
                getNavigator().onAddItemToCartFailure(error.toString());
            }
        });
    }

    public Call<SkusInventoryForStoreResponse> queryInventoryForSKUs(String storeId, String multiSku, final boolean isMultiSKUs) {
        Call<SkusInventoryForStoreResponse> skusInventoryForStoreResponseCall = OneAppService.INSTANCE.getInventorySkuForStore(storeId, multiSku);
        skusInventoryForStoreResponseCall.enqueue(new CompletionHandler<>(new RequestListener<SkusInventoryForStoreResponse>() {
            @Override
            public void onSuccess(SkusInventoryForStoreResponse skusInventoryForStoreResponse) {
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
            public void onFailure(Throwable error) {

            }
        },SkusInventoryForStoreResponse.class));
        return skusInventoryForStoreResponseCall;
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