package za.co.woolworths.financial.services.android.models;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.awfs.coordination.R;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;


import retrofit.RestAdapter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import okio.Buffer;
import okio.BufferedSource;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import za.co.wigroup.androidutils.Util;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.AccountResponse;
import za.co.woolworths.financial.services.android.models.dto.AccountsResponse;
import za.co.woolworths.financial.services.android.models.dto.AuthoriseLoanRequest;
import za.co.woolworths.financial.services.android.models.dto.AuthoriseLoanResponse;
import za.co.woolworths.financial.services.android.models.dto.BankAccountTypes;
import za.co.woolworths.financial.services.android.models.dto.CLIEmailResponse;
import za.co.woolworths.financial.services.android.models.dto.ContactUsConfigResponse;
import za.co.woolworths.financial.services.android.models.dto.CreateOfferRequest;
import za.co.woolworths.financial.services.android.models.dto.CreateOfferResponse;
import za.co.woolworths.financial.services.android.models.dto.CreateUpdateDevice;
import za.co.woolworths.financial.services.android.models.dto.CreateUpdateDeviceResponse;
import za.co.woolworths.financial.services.android.models.dto.DeaBanks;
import za.co.woolworths.financial.services.android.models.dto.DeleteMessageResponse;
import za.co.woolworths.financial.services.android.models.dto.IssueLoanRequest;
import za.co.woolworths.financial.services.android.models.dto.IssueLoanResponse;
import za.co.woolworths.financial.services.android.models.dto.LocationResponse;
import za.co.woolworths.financial.services.android.models.dto.LoginRequest;
import za.co.woolworths.financial.services.android.models.dto.LoginResponse;
import za.co.woolworths.financial.services.android.models.dto.MessageReadRequest;
import za.co.woolworths.financial.services.android.models.dto.MessageResponse;
import za.co.woolworths.financial.services.android.models.dto.Offer;
import za.co.woolworths.financial.services.android.models.dto.OfferActive;
import za.co.woolworths.financial.services.android.models.dto.ReadMessagesResponse;
import za.co.woolworths.financial.services.android.models.dto.TransactionHistoryResponse;
import za.co.woolworths.financial.services.android.models.dto.UpdateBankDetail;
import za.co.woolworths.financial.services.android.models.dto.UpdateBankDetailResponse;
import za.co.woolworths.financial.services.android.models.dto.VoucherResponse;

public class WfsApi {

    private Context mContext;
    private ApiInterface mApiInterface;
    public static final String TAG = "WfsApi";


    protected WfsApi(Context mContext) {
        this.mContext = mContext;
        OkHttpClient client = new OkHttpClient();
        client.setReadTimeout(60, TimeUnit.SECONDS);
        client.setConnectTimeout(60, TimeUnit.SECONDS);
        client.interceptors().add(new WfsApiInterceptor(mContext));
        mApiInterface = new RestAdapter.Builder()
                .setClient(new OkClient(client))
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

    public IssueLoanResponse issueLoan(IssueLoanRequest issueLoanRequest) {
        return mApiInterface.issueLoan(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), issueLoanRequest);
    }

    public ContactUsConfigResponse getContactUsConfig() {
        return mApiInterface.getContactUsConfig(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "");
    }

    public LocationResponse getLocations(String lat,String lon,String searchString ,String radius) {

        if (radius != null && radius.equals("")) {
            //This should never happen for now
            return mApiInterface.getStoresLocation(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSession(), lat, lon, searchString, radius);
        } else {

            return mApiInterface.getStoresLocation(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSession(), lat, lon, searchString);
        }
    }

    public MessageResponse getMessagesResponse(int pageSize, int pageNumber) {
        return mApiInterface.getMessages(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), pageSize, pageNumber);
    }

    public CreateOfferResponse createOfferRequest(CreateOfferRequest offerRequest) {
        return mApiInterface.createOfferRequest(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), offerRequest);
    }

    public DeaBanks getDeaBanks(){
        return  mApiInterface.getDeaBanks(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "",getSessionToken(),0,0);
    }

    public BankAccountTypes getBankAccountTypes(){
        return  mApiInterface.getBankAccountTypes(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "",getSessionToken(),0,0);
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

    public CLIEmailResponse cliEmailResponse(String email){
        return mApiInterface.cliSendEmailRquest(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken(), email);
    }


    public UpdateBankDetailResponse cliUpdateBankDetail(UpdateBankDetail updateBankDetail) {
        return mApiInterface.cliUpdateBankRequest(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), updateBankDetail);
    }

    public CreateUpdateDeviceResponse getResponseOnCreateUpdateDevice(CreateUpdateDevice device) {
        return mApiInterface.createUpdateDevice(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), device);
    }

    private String getOsVersion() {
        String osVersion = Util.getOsVersion();
        if (TextUtils.isEmpty(osVersion)){
            String myVersion = android.os.Build.VERSION.RELEASE; // e.g. myVersion := "1.6"
            int sdkVersion = android.os.Build.VERSION.SDK_INT; // e.g. sdkVersion := 8;
            osVersion= String.valueOf(sdkVersion);
        }
        return osVersion;
    }

    private String getOS() {
        return "Android";
    }

    private String getNetworkCarrier() {
        String networkCarrier = Util.getNetworkCarrier(mContext);
        return networkCarrier.isEmpty() ? "Unavailable" : networkCarrier;
    }

    private String getDeviceModel() {
        return Util.getDeviceModel();
    }

    private String getDeviceManufacturer() {
        return Util.getDeviceManufacturer();
    }

    private String getSha1Password() {
        return mContext.getString(R.string.sha1_password);
    }

    private String getApiId() {
        return mContext.getString(R.string.api_id);
    }

    private String getDeviceID() {
        try {
            return Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            return null;
        }
    }

    private String getSessionToken(){
        try{
            SessionDao sessionDao = new SessionDao(mContext, SessionDao.KEY.USER_TOKEN).get();
            if (sessionDao.value != null && !sessionDao.value.equals("")){
                return sessionDao.value;
            }
        }catch(Exception e){
            Log.e(TAG, e.getMessage());
        }
        return "";
    }
/*   public ConfigResponse getConfig(){
        ApiInterface mApiInterface = new RestAdapter.Builder()
                .setEndpoint(mContext.getString(R.string.config_endpoint))
                .setLogLevel(Util.isDebug(mContext) ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
                .build()
                .create(ApiInterface.class);
        return mApiInterface.getConfig("wfsAndroid",getDeviceID());
    }*/
}
