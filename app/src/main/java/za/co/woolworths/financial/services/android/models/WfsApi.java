package za.co.woolworths.financial.services.android.models;

import android.content.Context;
import android.location.Location;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.jakewharton.retrofit.Ok3Client;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

import retrofit.Callback;
import okhttp3.OkHttpClient;
import retrofit.RestAdapter;
import za.co.wigroup.androidutils.Util;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.AccountResponse;
import za.co.woolworths.financial.services.android.models.dto.AccountsResponse;
import za.co.woolworths.financial.services.android.models.dto.AuthoriseLoanRequest;
import za.co.woolworths.financial.services.android.models.dto.AuthoriseLoanResponse;
import za.co.woolworths.financial.services.android.models.dto.BankAccountTypes;
import za.co.woolworths.financial.services.android.models.dto.CLIEmailResponse;
import za.co.woolworths.financial.services.android.models.dto.CardDetailsResponse;
import za.co.woolworths.financial.services.android.models.dto.ContactUsConfigResponse;
import za.co.woolworths.financial.services.android.models.dto.CLIOfferDecision;
import za.co.woolworths.financial.services.android.models.dto.CreateOfferRequest;
import za.co.woolworths.financial.services.android.models.dto.CreateUpdateDevice;
import za.co.woolworths.financial.services.android.models.dto.CreateUpdateDeviceResponse;
import za.co.woolworths.financial.services.android.models.dto.DeaBanks;
import za.co.woolworths.financial.services.android.models.dto.DeleteMessageResponse;
import za.co.woolworths.financial.services.android.models.dto.FAQ;
import za.co.woolworths.financial.services.android.models.dto.IssueLoanRequest;
import za.co.woolworths.financial.services.android.models.dto.IssueLoanResponse;
import za.co.woolworths.financial.services.android.models.dto.LocationResponse;
import za.co.woolworths.financial.services.android.models.dto.LoginRequest;
import za.co.woolworths.financial.services.android.models.dto.LoginResponse;
import za.co.woolworths.financial.services.android.models.dto.MessageReadRequest;
import za.co.woolworths.financial.services.android.models.dto.MessageResponse;
import za.co.woolworths.financial.services.android.models.dto.OfferActive;
import za.co.woolworths.financial.services.android.models.dto.ProductView;
import za.co.woolworths.financial.services.android.models.dto.PromotionsResponse;
import za.co.woolworths.financial.services.android.models.dto.ReadMessagesResponse;
import za.co.woolworths.financial.services.android.models.dto.RootCategories;
import za.co.woolworths.financial.services.android.models.dto.statement.Statement;
import za.co.woolworths.financial.services.android.models.dto.statement.StatementResponse;
import za.co.woolworths.financial.services.android.models.dto.SubCategories;
import za.co.woolworths.financial.services.android.models.dto.TransactionHistoryResponse;
import za.co.woolworths.financial.services.android.models.dto.UpdateBankDetail;
import za.co.woolworths.financial.services.android.models.dto.UpdateBankDetailResponse;
import za.co.woolworths.financial.services.android.models.dto.VoucherResponse;
import za.co.woolworths.financial.services.android.models.dto.WProduct;
import za.co.woolworths.financial.services.android.util.Utils;

public class WfsApi {

	private Context mContext;
	private ApiInterface mApiInterface;
	public static final String TAG = "WfsApi";
	private Location loc;

	WfsApi(Context mContext) {
		this.mContext = mContext;
		OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder();
		httpBuilder.addInterceptor(new WfsApiInterceptor(mContext));
		httpBuilder.readTimeout(45, TimeUnit.SECONDS);
		httpBuilder.connectTimeout(45, TimeUnit.SECONDS);
		mApiInterface = new RestAdapter.Builder()
				.setClient((new Ok3Client(httpBuilder.build())))
				.setEndpoint(WoolworthsApplication.getBaseURL())
				.setLogLevel(Util.isDebug(mContext) ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
				.build()
				.create(ApiInterface.class);
	}

	public LoginResponse login(LoginRequest loginRequest) {
		return mApiInterface.login(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", loginRequest);
	}

	public AccountResponse getAccount(String productOfferingId) {
		return mApiInterface.getAccount(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), productOfferingId);
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


	public ContactUsConfigResponse getContactUsConfig() {
		return mApiInterface.getContactUsConfig(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "");
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
			return mApiInterface.getStoresLocationItem(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", String.valueOf(loc.getLatitude()), String.valueOf(loc.getLongitude()), sku, startRadius, endRadius, true);
		} else {
			return mApiInterface.getStoresLocationItem(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", String.valueOf(loc.getLatitude()), String.valueOf(loc.getLongitude()), sku, startRadius, endRadius, true);
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

	public IssueLoanResponse issueLoan(IssueLoanRequest issueLoanRequest) {
		return mApiInterface.issueLoan(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), issueLoanRequest);
	}

	public PromotionsResponse getPromotions() {
		return mApiInterface.getPromotions(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "");
	}

	public RootCategories getRootCategory() {
		return mApiInterface.getRootCategories(getOsVersion(), getApiId(), getOS(), getSha1Password(), getDeviceModel(), getNetworkCarrier(), getOsVersion(), "Android");
	}

	public SubCategories getSubCategory(String category_id) {
		return mApiInterface.getSubCategory(getOsVersion(), getApiId(), getOS(), getSha1Password(), getDeviceModel(), getNetworkCarrier(), getOsVersion(), "Android", category_id);
	}

	public ProductView productViewRequest(boolean isBarcode, int pageSize, int pageNumber, String product_id) {
		getMyLocation();
		if (Utils.isLocationEnabled(mContext)) {
			return mApiInterface.getProduct(getOsVersion(), getDeviceModel(), getOsVersion(), getOS(), getNetworkCarrier(), getApiId(), "", "", getSha1Password(), loc.getLatitude(), loc.getLongitude(), pageSize, pageNumber, product_id);
		} else {
			return mApiInterface.getProduct(getOsVersion(), getDeviceModel(), getOsVersion(), getOS(), getNetworkCarrier(), getApiId(), "", "", getSha1Password(), pageSize, pageNumber, product_id);
		}
	}

	public ProductView getProductSearchList(String search_item, boolean isBarcode, int pageSize, int pageNumber) {
		getMyLocation();
		try {
			search_item = URLEncoder.encode(search_item, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		if (Utils.isLocationEnabled(mContext)) {// should we implement location update here ?
			return mApiInterface.getProductSearch(getOsVersion(), getDeviceModel(), getOsVersion(), getOS(), getNetworkCarrier(), getApiId(), "", "", getSha1Password(), loc.getLatitude(), loc.getLongitude(), isBarcode, search_item, pageSize, pageNumber);
		} else {
			return mApiInterface.getProductSearch(getOsVersion(), getDeviceModel(), getOsVersion(), getOS(), getNetworkCarrier(), getApiId(), "", "", getSha1Password(), isBarcode, search_item, pageSize, pageNumber);
		}
	}

	public FAQ getFAQ() {
		return mApiInterface.getFAQ(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "");
	}


	public WProduct getProductDetailView(String productId, String skuId) {
		return mApiInterface.getProductDetail(getOsVersion(), getDeviceModel(), getOsVersion(),
				getOS(), getNetworkCarrier(), getApiId(), "", "",
				getSha1Password(), productId, skuId);
	}

	public CardDetailsResponse getCardDetails() {
		return mApiInterface.getCardDetails(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken());
	}

	public StatementResponse getStatementResponse(Statement statement) {
		return mApiInterface.getStatements(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), statement.getProductOfferingId(), statement.getAccountNumber(), statement.getStartDate(), statement.getEndDate());
	}


	public String getOsVersion() {
		String osVersion = Util.getOsVersion();
		if (TextUtils.isEmpty(osVersion)) {
			String myVersion = android.os.Build.VERSION.RELEASE; // e.g. myVersion := "1.6"
			int sdkVersion = android.os.Build.VERSION.SDK_INT; // e.g. sdkVersion := 8;
			osVersion = String.valueOf(sdkVersion);
		}
		return osVersion;
	}

	public String getOS() {
		return "Android";
	}

	public String getNetworkCarrier() {
		String networkCarrier = Util.getNetworkCarrier(mContext);
		return networkCarrier.isEmpty() ? "Unavailable" : Utils.removeUnicodesFromString(networkCarrier);
	}

	public String getDeviceModel() {
		return Util.getDeviceModel();
	}

	public String getDeviceManufacturer() {
		return Util.getDeviceManufacturer();
	}

	public String getSha1Password() {
		return WoolworthsApplication.getSha1Password();
	}

	public String getApiId() {
		return WoolworthsApplication.getApiKey();
	}

	private String getDeviceID() {
		try {
			return Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
		} catch (Exception e) {
			return null;
		}
	}

	public String getSessionToken() {
		try {
			SessionDao sessionDao = new SessionDao(mContext, SessionDao.KEY.USER_TOKEN).get();
			if (sessionDao.value != null && !sessionDao.value.equals("")) {
				return sessionDao.value;
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
		return "";
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