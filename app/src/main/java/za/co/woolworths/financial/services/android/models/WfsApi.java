package za.co.woolworths.financial.services.android.models;

import android.content.Context;
import android.location.Location;
import android.text.TextUtils;

import com.jakewharton.retrofit.Ok3Client;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit.RestAdapter;
import retrofit.client.Response;
import za.co.wigroup.androidutils.Util;
import za.co.woolworths.financial.services.android.models.network.NetworkConfig;
import za.co.woolworths.financial.services.android.models.network.WfsApiInterceptor;
import za.co.woolworths.financial.services.android.models.dto.AccountsResponse;
import za.co.woolworths.financial.services.android.models.dto.AddItemToCart;
import za.co.woolworths.financial.services.android.models.dto.AddItemToCartResponse;
import za.co.woolworths.financial.services.android.models.dto.AddToListRequest;
import za.co.woolworths.financial.services.android.models.dto.AuthoriseLoanRequest;
import za.co.woolworths.financial.services.android.models.dto.AuthoriseLoanResponse;
import za.co.woolworths.financial.services.android.models.dto.BankAccountTypes;
import za.co.woolworths.financial.services.android.models.dto.CLIEmailResponse;
import za.co.woolworths.financial.services.android.models.dto.CLIOfferDecision;
import za.co.woolworths.financial.services.android.models.dto.CardDetailsResponse;
import za.co.woolworths.financial.services.android.models.dto.CartSummaryResponse;
import za.co.woolworths.financial.services.android.models.dto.ChangeQuantity;
import za.co.woolworths.financial.services.android.models.dto.CheckoutSuccess;
import za.co.woolworths.financial.services.android.models.dto.CreateList;
import za.co.woolworths.financial.services.android.models.dto.CreateOfferRequest;
import za.co.woolworths.financial.services.android.models.dto.CreateUpdateDevice;
import za.co.woolworths.financial.services.android.models.dto.CreateUpdateDeviceResponse;
import za.co.woolworths.financial.services.android.models.dto.CreditCardTokenResponse;
import za.co.woolworths.financial.services.android.models.dto.DeaBanks;
import za.co.woolworths.financial.services.android.models.dto.DeleteMessageResponse;
import za.co.woolworths.financial.services.android.models.dto.FAQ;
import za.co.woolworths.financial.services.android.models.dto.IssueLoan;
import za.co.woolworths.financial.services.android.models.dto.IssueLoanResponse;
import za.co.woolworths.financial.services.android.models.dto.LocationResponse;
import za.co.woolworths.financial.services.android.models.dto.LoginRequest;
import za.co.woolworths.financial.services.android.models.dto.LoginResponse;
import za.co.woolworths.financial.services.android.models.dto.MessageReadRequest;
import za.co.woolworths.financial.services.android.models.dto.MessageResponse;
import za.co.woolworths.financial.services.android.models.dto.OfferActive;
import za.co.woolworths.financial.services.android.models.dto.OrderDetailsResponse;
import za.co.woolworths.financial.services.android.models.dto.OrderTaxInvoiceResponse;
import za.co.woolworths.financial.services.android.models.dto.OrderToListReponse;
import za.co.woolworths.financial.services.android.models.dto.OrderToShoppingListRequestBody;
import za.co.woolworths.financial.services.android.models.dto.OrdersResponse;
import za.co.woolworths.financial.services.android.models.dto.ProductDetailResponse;
import za.co.woolworths.financial.services.android.models.dto.ProductView;
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams;
import za.co.woolworths.financial.services.android.models.dto.PromotionsResponse;
import za.co.woolworths.financial.services.android.models.dto.ProvincesResponse;
import za.co.woolworths.financial.services.android.models.dto.ReadMessagesResponse;
import za.co.woolworths.financial.services.android.models.dto.RootCategories;
import za.co.woolworths.financial.services.android.models.dto.SetDeliveryLocationSuburbRequest;
import za.co.woolworths.financial.services.android.models.dto.SetDeliveryLocationSuburbResponse;
import za.co.woolworths.financial.services.android.models.dto.ShoppingCartResponse;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItemsResponse;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse;
import za.co.woolworths.financial.services.android.models.dto.SkuInventoryResponse;
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse;
import za.co.woolworths.financial.services.android.models.dto.SubCategories;
import za.co.woolworths.financial.services.android.models.dto.SuburbsResponse;
import za.co.woolworths.financial.services.android.models.dto.TransactionHistoryResponse;
import za.co.woolworths.financial.services.android.models.dto.UpdateBankDetail;
import za.co.woolworths.financial.services.android.models.dto.UpdateBankDetailResponse;
import za.co.woolworths.financial.services.android.models.dto.VoucherResponse;
import za.co.woolworths.financial.services.android.models.dto.npc.BlockCardRequestBody;
import za.co.woolworths.financial.services.android.models.dto.npc.BlockMyCardResponse;
import za.co.woolworths.financial.services.android.models.dto.statement.GetStatement;
import za.co.woolworths.financial.services.android.models.dto.statement.SendUserStatementRequest;
import za.co.woolworths.financial.services.android.models.dto.statement.SendUserStatementResponse;
import za.co.woolworths.financial.services.android.models.dto.statement.StatementResponse;
import za.co.woolworths.financial.services.android.models.dto.statement.UserStatement;
import za.co.woolworths.financial.services.android.util.SessionUtilities;
import za.co.woolworths.financial.services.android.util.Utils;

public class WfsApi {

	private Context mContext;
	private ApiInterface mApiInterface;
	public static final String TAG = "WfsApi";
	private Location loc;

	WfsApi(Context mContext) {
		this.mContext = mContext;
		OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder();
		httpBuilder.addInterceptor(new WfsApiInterceptor());
		httpBuilder.readTimeout(45, TimeUnit.SECONDS);
		httpBuilder.connectTimeout(45, TimeUnit.SECONDS);
		mApiInterface = new RestAdapter.Builder()
				.setClient((new Ok3Client(httpBuilder.build())))
				.setEndpoint(com.awfs.coordination.BuildConfig.HOST)
				.setLogLevel(Util.isDebug(mContext) ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
				.build()
				.create(ApiInterface.class);
	}

	public LoginResponse login(LoginRequest loginRequest) {
		return mApiInterface.login(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), loginRequest);
	}

	public AccountsResponse getAccounts() {
		return mApiInterface.getAccounts(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken());
	}

	public AuthoriseLoanResponse authoriseLoan(AuthoriseLoanRequest authoriseLoanRequest) {
		return mApiInterface.authoriseLoan(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), authoriseLoanRequest);
	}

	public TransactionHistoryResponse getAccountTransactionHistory(String productOfferingId) {
		return mApiInterface.getAccountTransactionHistory(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), productOfferingId);
	}

	public VoucherResponse getVouchers() {
		return mApiInterface.getVouchers(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken());
	}

	public LocationResponse getLocations(String lat, String lon, String searchString, String radius) {
		if (radius != null && radius.equals("")) {
			//This should never happen for now
			return mApiInterface.getStoresLocation(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), lat, lon, searchString, radius);
		} else {

			return mApiInterface.getStoresLocation(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), lat, lon, searchString);
		}
	}

	public LocationResponse getLocationsItem(String sku, String startRadius, String endRadius) {
		getMyLocation();
		if (startRadius != null && startRadius.equals("")) {
			//This should never happen for now
			return mApiInterface.getStoresLocationItem(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", String.valueOf(loc.getLatitude()), String.valueOf(loc.getLongitude()),getSessionToken(), sku, startRadius, endRadius, true);
		} else {
			return mApiInterface.getStoresLocationItem(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", String.valueOf(loc.getLatitude()), String.valueOf(loc.getLongitude()),getSessionToken(), sku, startRadius, endRadius, true);
		}
	}

	public MessageResponse getMessagesResponse(int pageSize, int pageNumber) {
		return mApiInterface.getMessages(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), pageSize, pageNumber);
	}

	public OfferActive cliCreateApplication(CreateOfferRequest offerRequest) {
		return mApiInterface.cliCreateApplication(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), offerRequest);
	}

	public OfferActive cliUpdateApplication(CreateOfferRequest offerRequest, String cliId) {
		return mApiInterface.cliUpdateApplication(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), cliId, offerRequest);
	}

	public OfferActive createOfferDecision(CLIOfferDecision createOfferDecision, String cliId) {
		return mApiInterface.createOfferDecision(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), cliId, createOfferDecision);
	}

	public DeaBanks getDeaBanks() {
		return mApiInterface.getDeaBanks(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), 0, 0);
	}

	public BankAccountTypes getBankAccountTypes() {
		return mApiInterface.getBankAccountTypes(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), 0, 0);
	}

	public OfferActive getActiveOfferRequest(String productOfferingId) {
		return mApiInterface.getActiveOfferRequest(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), productOfferingId);
	}

	public DeleteMessageResponse getDeleteMessagesResponse(String id) {
		return mApiInterface.getDeleteresponse(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), id);
	}

	public ReadMessagesResponse getReadMessagesResponse(MessageReadRequest readMessages) {
		return mApiInterface.setReadMessages(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), readMessages);
	}

	public CLIEmailResponse cliEmailResponse() {
		return mApiInterface.cliSendEmailRquest(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken());
	}

	public UpdateBankDetailResponse cliUpdateBankDetail(UpdateBankDetail updateBankDetail) {
		return mApiInterface.cliUpdateBankRequest(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), updateBankDetail);
	}

	public CreateUpdateDeviceResponse getResponseOnCreateUpdateDevice(CreateUpdateDevice device) {
		return mApiInterface.createUpdateDevice(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), device);
	}

	public IssueLoanResponse issueLoan(IssueLoan issueLoan) {
		return mApiInterface.issueLoan(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), issueLoan);
	}

	public ShoppingListItemsResponse addToList(List<AddToListRequest> addToListRequest, String listId) {
		return mApiInterface.addToList(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken(), listId, addToListRequest);
	}

	public PromotionsResponse getPromotions() {
		return mApiInterface.getPromotions(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "",getSessionToken());
	}

	public RootCategories getRootCategory() {
		return mApiInterface.getRootCategories(getOsVersion(), getApiId(), getOS(), getSha1Password(), getDeviceModel(), getNetworkCarrier(), getDeviceManufacturer(), "Android",getSessionToken());
	}

	public SubCategories getSubCategory(String category_id) {
		return mApiInterface.getSubCategory(getOsVersion(), getApiId(), getOS(), getSha1Password(), getDeviceModel(), getNetworkCarrier(), getDeviceManufacturer(), "Android", getSessionToken(),category_id);
	}

	public ProvincesResponse getProvinces() {
		return mApiInterface.getProvinces(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken());
	}

	public CartSummaryResponse getCartSummary() {
		return mApiInterface.getCartSummary(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), null, null, getSessionToken());
	}

	public SuburbsResponse getSuburbs(String locationId) {
		return mApiInterface.getSuburbs(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), locationId);
	}

	public SetDeliveryLocationSuburbResponse setSuburb(String suburbId) {
		SetDeliveryLocationSuburbRequest request = new SetDeliveryLocationSuburbRequest(suburbId);
		return mApiInterface.setDeliveryLocationSuburb(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken(), request);
	}

    public ProductView getProducts(ProductsRequestParams requestParams) {
        getMyLocation();
        if (Utils.isLocationEnabled(mContext)) {
            return mApiInterface.getProducts(getOsVersion(), getDeviceModel(), getDeviceManufacturer(), getOS(), getNetworkCarrier(), getApiId(), "", "", getSha1Password(), loc.getLatitude(), loc.getLongitude(),getSessionToken(), requestParams.getSearchTerm(), requestParams.getSearchType().getValue(), requestParams.getResponseType().getValue(), requestParams.getPageOffset(), Utils.PAGE_SIZE, requestParams.getSortOption(), requestParams.getRefinement());
        } else {
            return mApiInterface.getProductsWithoutLocation(getOsVersion(), getDeviceModel(), getDeviceManufacturer(), getOS(), getNetworkCarrier(), getApiId(), "", "", getSha1Password(),getSessionToken(), requestParams.getSearchTerm(), requestParams.getSearchType().getValue(), requestParams.getResponseType().getValue(), requestParams.getPageOffset(), Utils.PAGE_SIZE, requestParams.getSortOption(), requestParams.getRefinement());
        }
    }

	public FAQ getFAQ() {
		return mApiInterface.getFAQ(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken());
	}

	public CardDetailsResponse getCardDetails() {
		return mApiInterface.getCardDetails(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken());
	}

	public StatementResponse getStatementResponse(UserStatement statement) {
		return mApiInterface.getUserStatement(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), statement.getProductOfferingId(), statement.getAccountNumber(), statement.getStartDate(), statement.getEndDate());
	}

	public SendUserStatementResponse sendStatementRequest(SendUserStatementRequest statement) {
		return mApiInterface.sendUserStatement(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), statement);
	}

	public AddItemToCartResponse addItemToCart(List<AddItemToCart> addToCart) {
		return mApiInterface.addItemToCart(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), addToCart);
	}

	public ShoppingCartResponse getShoppingCart() {
		return mApiInterface.getShoppingCart(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken());
	}

	public ShoppingCartResponse getChangeQuantity(ChangeQuantity changeQuantity) {
		return mApiInterface.changeQuantityRequest(getApiId(), getSha1Password(), getDeviceManufacturer(),
				getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "",
				"", getSessionToken(), changeQuantity.getCommerceId(), changeQuantity);
	}

	public ShoppingCartResponse removeCartItem(String commerceId) {
		return mApiInterface.removeItemFromCart(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken(), commerceId);
	}

	public ShoppingCartResponse removeAllCartItems() {
		return mApiInterface.removeAllCartItems(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken());
	}

	public ProductDetailResponse productDetail(String productId, String skuId) {
		getMyLocation();
		if (Utils.isLocationEnabled(mContext)) {
			return mApiInterface.productDetail(getOsVersion(), getDeviceModel(), getDeviceManufacturer(),
					getOS(), getNetworkCarrier(), getApiId(), "", "",
					getSha1Password(), loc.getLongitude(), loc.getLatitude(),getSessionToken(), productId, skuId);
		} else {
			return mApiInterface.productDetail(getOsVersion(), getDeviceModel(), getDeviceManufacturer(),
					getOS(), getNetworkCarrier(), getApiId(), "", "",
					getSha1Password(),getSessionToken(), productId, skuId);
		}
	}

	public ShoppingListsResponse getShoppingLists() {
		return mApiInterface.getShoppingLists(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken());
	}

	public ShoppingListsResponse createList(CreateList listName) {
		return mApiInterface.createList(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken(), listName);
	}


	public ShoppingListItemsResponse getShoppingListItems(String listId) {
		return mApiInterface.getShoppingListItems(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken(), listId);
	}

	public ShoppingListsResponse deleteShoppingList(String listId) {
		return mApiInterface.deleteShoppingList(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken(), listId);
	}

	public ShoppingListItemsResponse deleteShoppingListItem(String listId, String id, String productId, String catalogRefId) {
		return mApiInterface.deleteShoppingListItem(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken(), listId, id, productId, catalogRefId);
	}

	public SkuInventoryResponse getInventorySku(String multipleSku) {
		return mApiInterface.getInventorySKU(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken(), multipleSku);
	}

	public SkusInventoryForStoreResponse getInventorySkuForStore(String store_id, String multipleSku) {
		return mApiInterface.getInventorySKUForStore(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken(), store_id, multipleSku);

	}

	public Response getPDFResponse(GetStatement getStatement) {
		return mApiInterface.getStatement(getApiId(), com.awfs.coordination.BuildConfig.SHA1, getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), getStatement.getDocId(), getStatement.getProductOfferingId(), getStatement.getDocDesc());
	}
	
	public Void postCheckoutSuccess(CheckoutSuccess checkoutSuccess) {
		return mApiInterface.postCheckoutSuccess(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken(), checkoutSuccess);
	}

	public OrdersResponse getOrders() {
		return mApiInterface.getOrders(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken());
	}

	public OrderDetailsResponse getOrderDetails(String orderId) {
		return mApiInterface.getOrderDetails(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken(), orderId);
	}

	public OrderToListReponse addOrderToList(String orderId, OrderToShoppingListRequestBody orderToShoppingListRequestBody) {
		return mApiInterface.addOrderToList(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken(), orderId, orderToShoppingListRequestBody);
	}

	public OrderTaxInvoiceResponse getOrderTaxInvoice(String taxNoteNumber) {
		return mApiInterface.getTaxInvoice(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken(), taxNoteNumber);
	}

	public CreditCardTokenResponse getCreditCardToken() {
		return mApiInterface.getCreditCardToken(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken());
	}

	public BlockMyCardResponse postBlockMyCard(BlockCardRequestBody blockCardRequestBody, String productOfferingId) {
		return mApiInterface.blockStoreCard(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(),getOsVersion(), getOsVersion(),"",getSessionToken(),productOfferingId , blockCardRequestBody);
	}

	private String getOsVersion() {
		String osVersion = Util.getOsVersion();
		if (TextUtils.isEmpty(osVersion)) {
			int sdkVersion = android.os.Build.VERSION.SDK_INT; // e.g. sdkVersion := 8;
			osVersion = String.valueOf(sdkVersion);
		}
		return osVersion;
	}

	public String getOS() {
		return "Android";
	}

	private String getNetworkCarrier() {
		String networkCarrier = Util.getNetworkCarrier(mContext);
		return networkCarrier.isEmpty() ? "Unavailable" : Utils.removeUnicodesFromString(networkCarrier);
	}

	private String getDeviceModel() {
		return Util.getDeviceModel();
	}

	private String getDeviceManufacturer() {
		return Util.getDeviceManufacturer();
	}

	private String getSha1Password() {
		return com.awfs.coordination.BuildConfig.SHA1;
	}

	private String getApiId() {
		return WoolworthsApplication.getApiId();
	}

	private String getSessionToken() {
		String sessionToken = SessionUtilities.getInstance().getSessionToken();
		if (sessionToken.isEmpty())
			return ".";
		else
			return sessionToken;
	}

	private void getMyLocation() {
		loc = Utils.getLastSavedLocation(mContext);
		if (loc == null) {
			loc = new Location("myLocation");
		}
		if (Utils.isLocationEnabled(mContext)) {
			double latitude = loc.getLatitude();
			double longitude = loc.getLongitude();
			if (TextUtils.isEmpty(String.valueOf(latitude)))
				loc.setLatitude(0);
			if (TextUtils.isEmpty(String.valueOf(longitude)))
				loc.setLongitude(0);

		}
	}

}