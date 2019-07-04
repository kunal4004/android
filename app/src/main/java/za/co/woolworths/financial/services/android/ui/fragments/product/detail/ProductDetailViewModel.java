package za.co.woolworths.financial.services.android.ui.fragments.product.detail;


import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import retrofit2.Call;
import za.co.woolworths.financial.services.android.contracts.RequestListener;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.AddItemToCart;
import za.co.woolworths.financial.services.android.models.dto.AddItemToCartResponse;
import za.co.woolworths.financial.services.android.models.dto.CartSummaryResponse;
import za.co.woolworths.financial.services.android.models.dto.LocationResponse;
import za.co.woolworths.financial.services.android.models.dto.OtherSkus;
import za.co.woolworths.financial.services.android.models.dto.ProductDetailResponse;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.ProductRequest;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.models.dto.WProduct;
import za.co.woolworths.financial.services.android.models.dto.WProductDetail;
import za.co.woolworths.financial.services.android.models.network.CompletionHandler;
import za.co.woolworths.financial.services.android.models.network.OneAppService;
import za.co.woolworths.financial.services.android.ui.base.BaseViewModel;
import za.co.woolworths.financial.services.android.util.GetCartSummary;
import za.co.woolworths.financial.services.android.util.PostItemToCart;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.rx.SchedulerProvider;

public class ProductDetailViewModel extends BaseViewModel<ProductDetailNavigator> {

	private String TAG = this.getClass().getSimpleName();
	private final String EMPTY = " ";
	private final String INGREDIENTS = "ingredients";
	private final String PRODUCT = "product";
	public static final String CLOTHING_PRODUCT = "clothingProducts";
	public static final String FOOD_PRODUCT = "foodProducts";

	private ProductList defaultProduct;
	private List<String> AuxiliaryImage;
	private WProductDetail newProductDetail;
	private String mProductJson;
	private boolean productLoadFail = false;
	private boolean findInStoreLoadFail = false;
	private boolean addedToCart = true;

	public ProductDetailViewModel() {
		super();
	}

	public ProductDetailViewModel(SchedulerProvider schedulerProvider) {
		super(schedulerProvider);
	}

	public void setAuxiliaryImage(List<String> auxiliaryImage) {
		AuxiliaryImage = auxiliaryImage;
	}

	public List<String> getAuxiliaryImage() {
		return AuxiliaryImage;
	}

	public void setDefaultProduct(String defaultProduct) {
		this.defaultProduct = new Gson().fromJson(defaultProduct, ProductList.class);
	}

	public ProductList getDefaultProduct() {
		return defaultProduct;
	}

	public void setAddedToCart(boolean addedToCart) {
		this.addedToCart = addedToCart;
	}

	public boolean getAddToCart() {
		return addedToCart;
	}


	public Call<ProductDetailResponse> productDetail(ProductRequest productRequest) {
		getNavigator().onLoadStart();
		setProductLoadFail(false);
		Call<ProductDetailResponse> productDetailRequest = OneAppService.INSTANCE.productDetail(productRequest.getProductId(), productRequest.getSkuId());
		productDetailRequest.enqueue(new CompletionHandler<>(new RequestListener<ProductDetailResponse>() {
			@Override
			public void onSuccess(ProductDetailResponse productDetailResponse) {
				ProductDetailResponse productDetail = productDetailResponse;
				String detailProduct = Utils.objectToJson(productDetail);
				switch (productDetail.httpCode) {
					case 200:
						WProduct product = (WProduct) Utils.strToJson(detailProduct, WProduct.class);
						setProduct(detailProduct);
						setProduct(product.product);
						getNavigator().onSuccessResponse(product);
						break;
					default:
						if (productDetail.response != null) {
							getNavigator().responseFailureHandler(productDetail.response);
						}
						break;
				}
				setProductLoadFail(false);
				getNavigator().onLoadComplete();
			}

			@Override
			public void onFailure(Throwable error) {
				if (error!=null) {
					setProductLoadFail(true);
					getNavigator().onFailureResponse(error.getMessage());
				}
			}
		},ProductDetailResponse.class));

		return productDetailRequest;
	}


	public Call<LocationResponse> locationItemTask(final Context context) {
		WoolworthsApplication woolworthsApplication = WoolworthsApplication.getInstance();
		WGlobalState mWGlobalState = woolworthsApplication.getWGlobalState();
		setFindInStoreLoadFail(false);
		getNavigator().showFindInStoreProgress();
		Call<LocationResponse> locationResponseCall = OneAppService.INSTANCE.getLocationsItem(mWGlobalState.getSelectedSKUId().sku, String.valueOf(mWGlobalState.getStartRadius()), String.valueOf(mWGlobalState.getEndRadius()));
		locationResponseCall.enqueue(new CompletionHandler<>(new RequestListener<LocationResponse>() {
			@Override
			public void onSuccess(LocationResponse locationResponse) {
				if (locationResponse != null && locationResponse.Locations.size() > 0) {
					getNavigator().onLocationItemSuccess(locationResponse.Locations);
				} else {
					getNavigator().outOfStockDialog();
				}

				getNavigator().dismissFindInStoreProgress();
				setFindInStoreLoadFail(false);
			}

			@Override
			public void onFailure(Throwable error) {
				if (context != null && error != null) {
					Activity activity = (Activity) context;
					if (activity != null) {
						setFindInStoreLoadFail(true);
						getNavigator().dismissFindInStoreProgress();
						getNavigator().onFailureResponse(error.getMessage());
					}
				}
			}
		},LocationResponse.class));

		return locationResponseCall;
	}

	public String getProductType() {
		return getProduct().productType;
	}

	public List<OtherSkus> otherSkuList() {
		if (getProduct() != null) {
			if (!getProduct().otherSkus.isEmpty()) {
				return getProduct().otherSkus;
			}
		}
		return new ArrayList<>();
	}

	//set new product list
	public void setProduct(WProductDetail prod) {
		this.newProductDetail = prod;
	}

	public void setProduct(String strProduct) {
		this.mProductJson = strProduct;
	}

	public JSONObject getProductJSON() throws JSONException {
		JSONObject jsonObject = new JSONObject(mProductJson);
		if (jsonObject != null) {
			if (jsonObject.has(PRODUCT)) {
				return jsonObject.getJSONObject(PRODUCT);
			}
		}
		return null;
	}

	public String getProductId() {
		if (getProduct() != null) {
			return getProduct().productId;
		}
		return null;
	}

	// return new product list
	public WProductDetail getProduct() {
		if (newProductDetail != null)
			return newProductDetail;
		else
			return null;
	}

	//return check out link
	public String getCheckOutLink() {
		return getProduct().checkOutLink;
	}

	//return ingredient info
	public void displayIngredient() {
		try {
			if (getProductJSON() != null) {
				JSONObject jsProductList = getProductJSON();
				if (jsProductList.has(INGREDIENTS)) {
					getNavigator().setIngredients(jsProductList.getString(INGREDIENTS));
				} else {
					getNavigator().setIngredients("");
				}
			}
		} catch (JSONException e) {
			Log.d(TAG, e.toString());
		}
	}

	public String getProductDescription(Context context) {

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
		if (getProduct() != null) {
			if (!TextUtils.isEmpty(getProduct().longDescription)) {
				descriptionWithoutExtraTag = getProduct().longDescription
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

	public OtherSkus highestSKUPrice(Float fromPrice) {
		String fp = String.valueOf(fromPrice);
		if (otherSkuList().size() > 0) {
			for (OtherSkus os : otherSkuList()) {
				if (fp.equalsIgnoreCase(os.price)) {
					return os;
				}
			}
		}
		return null;
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

	//setup auxiliaryImages
	public ArrayList<String> getAuxiliaryImageList(OtherSkus sku) {
		ArrayList<String> auxiliarySku = new ArrayList<>();
		// add default image if it match sku object
		OtherSkus gridSku = getDefaultSKUModel();
		ProductList product = getDefaultProduct();
		try {
			if (gridSku.colour.equalsIgnoreCase(sku.colour)) {
				auxiliarySku.add(getImageByWidth(gridSku.externalImageRef));
			}

			JSONObject jsProduct = getProductJSON();
			if (jsProduct.has("auxiliaryImages")) {
				// get auxiliaryImages list
				JSONObject jsAuxiliaryImages = jsProduct.getJSONObject("auxiliaryImages");
				Iterator<String> keyStr = jsAuxiliaryImages.keys();
				while (keyStr.hasNext()) {
					String key = keyStr.next();
					Object objAuxiliaryImage = jsAuxiliaryImages.get(key);
					String defaultColour = sku.colour.toLowerCase();
					int lastCharacterIsSpace = defaultColour.lastIndexOf(" ");
					int colourLength = defaultColour.length() - 1;
					if (!TextUtils.isEmpty(key)) {
						String auxiliaryColour = key.toLowerCase();
						// Concatenate first character with other colours if colour has space
						if (lastCharacterIsSpace == colourLength) {
							defaultColour = defaultColour.substring(0, colourLength - 1);
						}
						if (defaultColour.contains(EMPTY)) {
							String[] splitColour = defaultColour.split(EMPTY);
							defaultColour = "";
							int position = 0;
							for (String currentColour : splitColour) {
								if (position == 0) {
									defaultColour = currentColour.substring(0, 1);
								} else {
									defaultColour = defaultColour.concat(currentColour);
								}
								position++;
							}
						}
						// remove all spaces in colour
						String defaultSkuColour = sku.colour.toLowerCase();
						if (!TextUtils.isEmpty(defaultSkuColour)) {
							defaultSkuColour = defaultSkuColour.replaceAll(EMPTY, "");
						}
						// check if auxiliary colour code contains defaultColour
						if (auxiliaryColour.contains(defaultColour) || auxiliaryColour.contains(defaultSkuColour)) {
							String image = ((JSONObject) objAuxiliaryImage).getString("externalImageRef");
							auxiliarySku.add(getImageByWidth(image));
						}
					}
				}

				// show default colour if auxiliary image is empty
				if (auxiliarySku.isEmpty())
					auxiliarySku.add(getImageByWidth(product.externalImageRef));

				return auxiliarySku;
			}
		} catch (JSONException e) {
			Log.d(TAG, e.toString());
		} catch (NullPointerException e) {
			Log.d(TAG, e.toString());
		}
		// show default colour if auxiliary image is empty
		if (auxiliarySku.isEmpty())
			auxiliarySku.add(getImageByWidth(product.externalImageRef));
		return auxiliarySku;
	}

	private OtherSkus getDefaultSKUModel() {
		if (otherSkuList() != null) {
			if (otherSkuList().size() > 0) {
				for (OtherSkus option : otherSkuList()) {
					if (option.sku.equalsIgnoreCase(getProduct().sku)) {
						return option;
					}
				}
			}
		}
		return new OtherSkus();
	}

	private String getImageByWidth(String imageUrl) {
		Display display = ((WindowManager) WoolworthsApplication.getInstance()
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		imageUrl = (imageUrl.contains("jpg")) ? Utils.getExternalImageRef() + imageUrl : imageUrl;
		return imageUrl + "" + ((imageUrl.contains("jpg")) ? "" : "?w=" + width + "&q=" + 85);
	}

	public ArrayList<OtherSkus> commonColorList(OtherSkus otherSku) throws NullPointerException {
		ArrayList<OtherSkus> commonSizeList = new ArrayList<>();

		// filter by colour
		ArrayList<OtherSkus> sizeList = new ArrayList<>();
		for (OtherSkus sku : otherSkuList()) {
			if (sku.size.equalsIgnoreCase(otherSku.size)) {
				sizeList.add(sku);
			}
		}

		//remove duplicates
		for (OtherSkus os : sizeList) {
			if (!sizeValueExist(commonSizeList, os.colour)) {
				commonSizeList.add(os);
			}
		}

		return commonSizeList;
	}

	public ArrayList<OtherSkus> commonSizeList(OtherSkus otherSku) throws NullPointerException {
		ArrayList<OtherSkus> commonSizeList = new ArrayList<>();

		// filter by colour
		ArrayList<OtherSkus> sizeList = new ArrayList<>();
		for (OtherSkus sku : otherSkuList()) {
			if (sku.colour != null) {
				if (sku.colour.equalsIgnoreCase(otherSku.colour)) {
					sizeList.add(sku);
				}
			}
		}

		//remove duplicates
		for (OtherSkus os : sizeList) {
			if (!sizeValueExist(commonSizeList, os.size)) {
				commonSizeList.add(os);
			}
		}

		return commonSizeList;
	}

	private boolean sizeValueExist(ArrayList<OtherSkus> list, String name) {
		for (OtherSkus item : list) {
			if (item.size.equals(name)) {
				return true;
			}
		}
		return false;
	}

	public ArrayList<OtherSkus> commonSizeList(boolean productHasColour, String colour) {
		ArrayList<OtherSkus> commonSizeList = new ArrayList<>();

		if (productHasColour) { //product has color
			// filter by colour
			ArrayList<OtherSkus> sizeList = new ArrayList<>();
			for (OtherSkus sku : otherSkuList()) {
				if (sku.colour.equalsIgnoreCase(colour)) {
					sizeList.add(sku);
				}
			}

			//remove duplicates
			for (OtherSkus os : sizeList) {
				if (!sizeValueExist(commonSizeList, os.colour)) {
					commonSizeList.add(os);
				}
			}
		} else { // no color found
			ArrayList<OtherSkus> sizeList = new ArrayList<>();
			for (OtherSkus sku : otherSkuList()) {
				if (sku.colour.contains(colour)) {
					sizeList.add(sku);
				}
			}
			//remove duplicates
			for (OtherSkus os : sizeList) {
				if (!sizeValueExist(commonSizeList, os.size)) {
					commonSizeList.add(os);
				}
			}
		}
		return commonSizeList;
	}

	public void setFindInStoreLoadFail(boolean findInStoreLoadFail) {
		this.findInStoreLoadFail = findInStoreLoadFail;
	}

	public boolean findInStoreLoadFail() {
		return findInStoreLoadFail;
	}

	public void setProductLoadFail(boolean productLoadFail) {
		this.productLoadFail = productLoadFail;
	}

	public boolean productLoadFail() {
		return productLoadFail;
	}

	protected Call<CartSummaryResponse> getCartSummary() {
		setAddedToCart(true);
		getNavigator().onAddToCartLoad();
		GetCartSummary getCartSummary = new GetCartSummary();
		return getCartSummary.getCartSummary(new RequestListener<CartSummaryResponse>() {
			@Override
			public void onSuccess(CartSummaryResponse cartSummaryResponse) {
				if (cartSummaryResponse != null) {
					switch (cartSummaryResponse.httpCode) {
						case 200:
							getNavigator().onCartSummarySuccess(cartSummaryResponse);
							break;

						case 440:
							if (cartSummaryResponse.response != null)
								getNavigator().onSessionTokenExpired(cartSummaryResponse.response);
							break;

						default:
							getNavigator().otherHttpCode(cartSummaryResponse.response);
							break;
					}
				}
				setAddedToCart(true);
			}

			@Override
			public void onFailure(Throwable error) {
				if (error != null) {
					setAddedToCart(false);
					getNavigator().onTokenFailure(error.getMessage());
				}
			}
		});
	}



	protected Call<AddItemToCartResponse> postAddItemToCart(List<AddItemToCart> addItemToCart) {
		setAddedToCart(true);
		getNavigator().onAddToCartLoad();
		PostItemToCart postItemToCart = new PostItemToCart();
		return postItemToCart.make(addItemToCart, new RequestListener<AddItemToCartResponse>() {
			@Override
			public void onSuccess(AddItemToCartResponse addItemToCartResponse) {
				if (addItemToCartResponse != null) {
					switch (addItemToCartResponse.httpCode) {
						case 200:
							getNavigator().addItemToCartResponse(addItemToCartResponse);
							break;

						case 440:
							if (addItemToCartResponse.response != null)
								getNavigator().onSessionTokenExpired(addItemToCartResponse.response);
							break;

						default:
							if (addItemToCartResponse.response != null)
								getNavigator().otherHttpCode(addItemToCartResponse.response);
							break;
					}
				}
				setAddedToCart(true);
			}

			@Override
			public void onFailure(Throwable error) {
				if (error != null) {
					setAddedToCart(false);
					getNavigator().onAddItemToCartFailure(error.getMessage());
				}
			}
		});

	}

}
