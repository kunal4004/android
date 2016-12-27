package za.co.woolworths.financial.services.android.models;

import android.content.Context;
import android.provider.Settings;

import com.awfs.coordination.R;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit.RestAdapter;


import za.co.wigroup.androidutils.Util;
import za.co.woolworths.financial.services.android.models.dto.AccountResponse;
import za.co.woolworths.financial.services.android.models.dto.AccountsResponse;
import za.co.woolworths.financial.services.android.models.dto.AuthoriseLoanRequest;
import za.co.woolworths.financial.services.android.models.dto.AuthoriseLoanResponse;
import za.co.woolworths.financial.services.android.models.dto.ContactUsConfigResponse;
import za.co.woolworths.financial.services.android.models.dto.CreateUpdateDevice;
import za.co.woolworths.financial.services.android.models.dto.CreateUpdateDeviceResponse;
import za.co.woolworths.financial.services.android.models.dto.DeleteMessageResponse;
import za.co.woolworths.financial.services.android.models.dto.IssueLoanRequest;
import za.co.woolworths.financial.services.android.models.dto.IssueLoanResponse;
import za.co.woolworths.financial.services.android.models.dto.LocationResponse;
import za.co.woolworths.financial.services.android.models.dto.LoginRequest;
import za.co.woolworths.financial.services.android.models.dto.LoginResponse;
import za.co.woolworths.financial.services.android.models.dto.MessageReadRequest;
import za.co.woolworths.financial.services.android.models.dto.MessageResponse;
import za.co.woolworths.financial.services.android.models.dto.ReadMessagesResponse;
import za.co.woolworths.financial.services.android.models.dto.TransactionHistoryResponse;
import za.co.woolworths.financial.services.android.models.dto.VoucherResponse;
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity;

public class WfsApi {

    private Context mContext;
    private ApiInterface mApiInterface;

    protected WfsApi(Context mContext) {
        this.mContext = mContext;
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();
        mApiInterface = new RestAdapter.Builder()
                .setEndpoint(WoolworthsApplication.getBaseURL())
                .setLogLevel(Util.isDebug(mContext) ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
                .build()
                .create(ApiInterface.class);
    }

    public LoginResponse login(LoginRequest loginRequest) {
        return mApiInterface.login(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", loginRequest);
    }

    public AccountResponse getAccount(String productOfferingId) {
        return mApiInterface.getAccount(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSession(), productOfferingId);
    }
    // Session token hardcoded for test purpose
    public AccountsResponse getAccounts() {
        return mApiInterface.getAccounts(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken());
    }

    public AuthoriseLoanResponse authoriseLoan(AuthoriseLoanRequest authoriseLoanRequest) {
        return mApiInterface.authoriseLoan(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSession(), authoriseLoanRequest);
    }
    // Session token hardcoded for test purpose
    public TransactionHistoryResponse getAccountTransactionHistory(String productOfferingId) {
        return mApiInterface.getAccountTransactionHistory(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), productOfferingId);
    }

    public VoucherResponse getVouchers() {
        return mApiInterface.getVouchers(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSession());
    }

    public IssueLoanResponse issueLoan(IssueLoanRequest issueLoanRequest) {
        return mApiInterface.issueLoan(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSession(), issueLoanRequest);
    }

    public ContactUsConfigResponse getContactUsConfig() {
        return mApiInterface.getContactUsConfig(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "");
    }
    public LocationResponse getLocations(String lat,String lon,String searchString,String radious){
        return  mApiInterface.getStoresLocation(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "",getSession(),lat,lon,searchString,radious);
    }
    public MessageResponse getMessagesResponse(int pageSize, int pageNumber){

        return  mApiInterface.getMessages(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "",getSessionToken() ,pageSize,pageNumber);
    }
    public DeleteMessageResponse getDeleteMessagesResponse(String id){

        return  mApiInterface.getDeleteresponse(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken() ,id);
    }

    public ReadMessagesResponse getReadMessagesResponse(MessageReadRequest readMessages){

        return  mApiInterface.setReadMessages(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(),readMessages);
    }
    public CreateUpdateDeviceResponse getResponseOnCreateUpdateDevice(CreateUpdateDevice device){

        return  mApiInterface.createUpdateDevice(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken() ,device);
    }

    private String getSession() {
        return ((WoolworthsApplication)mContext).getUserManager().getSession();
    }

    private String getOsVersion() {
        return Util.getOsVersion();
    }

    private String getOS() {
        return "Android";
    }

    private String getNetworkCarrier() {
        String networkCarrier = Util.getNetworkCarrier(mContext);
        return networkCarrier.isEmpty()?"Unavailable":networkCarrier ;
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

    private String getDeviceID(){
        try{

            return  Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        }catch (Exception e){
            return null;
        }
    }

    private String getSessionToken(){
        return mContext.getSharedPreferences("User", Context.MODE_PRIVATE).getString(SSOActivity.TAG_JWT, "");
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
