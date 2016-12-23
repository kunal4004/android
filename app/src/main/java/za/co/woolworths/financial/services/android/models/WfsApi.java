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
import za.co.woolworths.financial.services.android.models.dto.BankAccountType;
import za.co.woolworths.financial.services.android.models.dto.BankAccountTypes;
import za.co.woolworths.financial.services.android.models.dto.ContactUsConfigResponse;
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
import za.co.woolworths.financial.services.android.models.dto.ReadMessagesResponse;
import za.co.woolworths.financial.services.android.models.dto.TransactionHistoryResponse;
import za.co.woolworths.financial.services.android.models.dto.VoucherResponse;

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
        return mApiInterface.getAccounts(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", "9178d4a6-24a6-412e-822d-b426acd2df1d");
    }

    public AuthoriseLoanResponse authoriseLoan(AuthoriseLoanRequest authoriseLoanRequest) {
        return mApiInterface.authoriseLoan(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSession(), authoriseLoanRequest);
    }
    // Session token hardcoded for test purpose
    public TransactionHistoryResponse getAccountTransactionHistory(String productOfferingId) {
        return mApiInterface.getAccountTransactionHistory(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", "9178d4a6-24a6-412e-822d-b426acd2df1d", productOfferingId);
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

        // Session token hardcoded for test purpose
        return  mApiInterface.getMessages(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "","eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6ImEzck1VZ01Gdjl0UGNsTGE2eUYzekFrZnF1RSIsImtpZCI6ImEzck1VZ01Gdjl0UGNsTGE2eUYzekFrZnF1RSJ9.eyJpc3MiOiJodHRwczovL3N0cy53b29sd29ydGhzLmNvLnphL2N1c3RvbWVyaWQiLCJhdWQiOiJXV09uZUFwcCIsImV4cCI6MTQ3NTc2NjEyNywibmJmIjoxNDc1NzY1ODI3LCJub25jZSI6ImFjM2UyIiwiaWF0IjoxNDc1NzY1ODI3LCJzaWQiOiI2NjFlNmM1NTdjZWM4MDNiMWU5YmE2YjA1MGFiMTVlZCIsInN1YiI6ImM2YjcyZTYwLTY1ZjgtNGY3ZS1hNWRmLTJiYjgyMWQ5NDZhNyIsImF1dGhfdGltZSI6MTQ3NTc2NTgyNSwiaWRwIjoiV1ctT05MSU5FIiwiZ2l2ZW5fbmFtZSI6IlRFU1QiLCJmYW1pbHlfbmFtZSI6IlBBR0UiLCJBdGdJZCI6IjE3OTEwMDQ1IiwiQXRnU2Vzc2lvbiI6IlZlMmFmMWVUa3pDX2dEak1RcmxmbTFTQVYxNldvWlVTdEwtdzU4emZTNU05ZnFNZVJTQ1IhMTY5MDY0ODc0NCIsImVtYWlsIjoiemFoaXJzaWVyc0B3b29sd29ydGhzLmNvLnphIiwiQzJJZCI6IjQzMzc4MzkiLCJhbXIiOlsiZXh0ZXJuYWwiXX0.bNMe1vvnzjaxO6iLbWQIC-Xi5x7Q6h4b7OZpfDeaMlj8noqnaVvkvmrvLggoypvv33_y0XhiJ6DtBC5MVQiuyU9K1U9w5uzNc2oTBzr5f65l4JbemEKoK4A0FpzM4rTroBDX8p3x_ubtYPa0Toluu9nzpIFeEnsQXNzU-2OGkaZ0nxWvYkJ0wsGeDXZlY0Lsix_OxdO6sgReDf9nL5f9hm6ekBE7oEMKlNAjZkB8TIvhCZXxumad_3SSVEhYDCfZzW-hoIOViUXFkqelVtdQmiRZei2-x65PZMu4EwkF-EYpNI9TNC6N35bYc3HrfK39H7hW979rX8CN-Aj9jOoyXg",pageSize,pageNumber);
    }
    public DeleteMessageResponse getDeleteMessagesResponse(String id){

        // Session token hardcoded for test purpose
        return  mApiInterface.getDeleteresponse(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "","eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6ImEzck1VZ01Gdjl0UGNsTGE2eUYzekFrZnF1RSIsImtpZCI6ImEzck1VZ01Gdjl0UGNsTGE2eUYzekFrZnF1RSJ9.eyJpc3MiOiJodHRwczovL3N0cy53b29sd29ydGhzLmNvLnphL2N1c3RvbWVyaWQiLCJhdWQiOiJXV09uZUFwcCIsImV4cCI6MTQ3NTc2NjEyNywibmJmIjoxNDc1NzY1ODI3LCJub25jZSI6ImFjM2UyIiwiaWF0IjoxNDc1NzY1ODI3LCJzaWQiOiI2NjFlNmM1NTdjZWM4MDNiMWU5YmE2YjA1MGFiMTVlZCIsInN1YiI6ImM2YjcyZTYwLTY1ZjgtNGY3ZS1hNWRmLTJiYjgyMWQ5NDZhNyIsImF1dGhfdGltZSI6MTQ3NTc2NTgyNSwiaWRwIjoiV1ctT05MSU5FIiwiZ2l2ZW5fbmFtZSI6IlRFU1QiLCJmYW1pbHlfbmFtZSI6IlBBR0UiLCJBdGdJZCI6IjE3OTEwMDQ1IiwiQXRnU2Vzc2lvbiI6IlZlMmFmMWVUa3pDX2dEak1RcmxmbTFTQVYxNldvWlVTdEwtdzU4emZTNU05ZnFNZVJTQ1IhMTY5MDY0ODc0NCIsImVtYWlsIjoiemFoaXJzaWVyc0B3b29sd29ydGhzLmNvLnphIiwiQzJJZCI6IjQzMzc4MzkiLCJhbXIiOlsiZXh0ZXJuYWwiXX0.bNMe1vvnzjaxO6iLbWQIC-Xi5x7Q6h4b7OZpfDeaMlj8noqnaVvkvmrvLggoypvv33_y0XhiJ6DtBC5MVQiuyU9K1U9w5uzNc2oTBzr5f65l4JbemEKoK4A0FpzM4rTroBDX8p3x_ubtYPa0Toluu9nzpIFeEnsQXNzU-2OGkaZ0nxWvYkJ0wsGeDXZlY0Lsix_OxdO6sgReDf9nL5f9hm6ekBE7oEMKlNAjZkB8TIvhCZXxumad_3SSVEhYDCfZzW-hoIOViUXFkqelVtdQmiRZei2-x65PZMu4EwkF-EYpNI9TNC6N35bYc3HrfK39H7hW979rX8CN-Aj9jOoyXg",id);
    }

    public ReadMessagesResponse getReadMessagesResponse(MessageReadRequest readMessages){

        // Session token hardcoded for test purpose
        return  mApiInterface.setReadMessages(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "","eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6ImEzck1VZ01Gdjl0UGNsTGE2eUYzekFrZnF1RSIsImtpZCI6ImEzck1VZ01Gdjl0UGNsTGE2eUYzekFrZnF1RSJ9.eyJpc3MiOiJodHRwczovL3N0cy53b29sd29ydGhzLmNvLnphL2N1c3RvbWVyaWQiLCJhdWQiOiJXV09uZUFwcCIsImV4cCI6MTQ3NTc2NjEyNywibmJmIjoxNDc1NzY1ODI3LCJub25jZSI6ImFjM2UyIiwiaWF0IjoxNDc1NzY1ODI3LCJzaWQiOiI2NjFlNmM1NTdjZWM4MDNiMWU5YmE2YjA1MGFiMTVlZCIsInN1YiI6ImM2YjcyZTYwLTY1ZjgtNGY3ZS1hNWRmLTJiYjgyMWQ5NDZhNyIsImF1dGhfdGltZSI6MTQ3NTc2NTgyNSwiaWRwIjoiV1ctT05MSU5FIiwiZ2l2ZW5fbmFtZSI6IlRFU1QiLCJmYW1pbHlfbmFtZSI6IlBBR0UiLCJBdGdJZCI6IjE3OTEwMDQ1IiwiQXRnU2Vzc2lvbiI6IlZlMmFmMWVUa3pDX2dEak1RcmxmbTFTQVYxNldvWlVTdEwtdzU4emZTNU05ZnFNZVJTQ1IhMTY5MDY0ODc0NCIsImVtYWlsIjoiemFoaXJzaWVyc0B3b29sd29ydGhzLmNvLnphIiwiQzJJZCI6IjQzMzc4MzkiLCJhbXIiOlsiZXh0ZXJuYWwiXX0.bNMe1vvnzjaxO6iLbWQIC-Xi5x7Q6h4b7OZpfDeaMlj8noqnaVvkvmrvLggoypvv33_y0XhiJ6DtBC5MVQiuyU9K1U9w5uzNc2oTBzr5f65l4JbemEKoK4A0FpzM4rTroBDX8p3x_ubtYPa0Toluu9nzpIFeEnsQXNzU-2OGkaZ0nxWvYkJ0wsGeDXZlY0Lsix_OxdO6sgReDf9nL5f9hm6ekBE7oEMKlNAjZkB8TIvhCZXxumad_3SSVEhYDCfZzW-hoIOViUXFkqelVtdQmiRZei2-x65PZMu4EwkF-EYpNI9TNC6N35bYc3HrfK39H7hW979rX8CN-Aj9jOoyXg",readMessages);
    }
    public DeaBanks getDeaBanks(){
        // Session token hardcoded for test purpose
        return  mApiInterface.getDeaBanks(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "","eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6ImEzck1VZ01Gdjl0UGNsTGE2eUYzekFrZnF1RSIsImtpZCI6ImEzck1VZ01Gdjl0UGNsTGE2eUYzekFrZnF1RSJ9.eyJpc3MiOiJodHRwczovL3N0cy53b29sd29ydGhzLmNvLnphL2N1c3RvbWVyaWQiLCJhdWQiOiJXV09uZUFwcCIsImV4cCI6MTQ3NTc2NjEyNywibmJmIjoxNDc1NzY1ODI3LCJub25jZSI6ImFjM2UyIiwiaWF0IjoxNDc1NzY1ODI3LCJzaWQiOiI2NjFlNmM1NTdjZWM4MDNiMWU5YmE2YjA1MGFiMTVlZCIsInN1YiI6ImM2YjcyZTYwLTY1ZjgtNGY3ZS1hNWRmLTJiYjgyMWQ5NDZhNyIsImF1dGhfdGltZSI6MTQ3NTc2NTgyNSwiaWRwIjoiV1ctT05MSU5FIiwiZ2l2ZW5fbmFtZSI6IlRFU1QiLCJmYW1pbHlfbmFtZSI6IlBBR0UiLCJBdGdJZCI6IjE3OTEwMDQ1IiwiQXRnU2Vzc2lvbiI6IlZlMmFmMWVUa3pDX2dEak1RcmxmbTFTQVYxNldvWlVTdEwtdzU4emZTNU05ZnFNZVJTQ1IhMTY5MDY0ODc0NCIsImVtYWlsIjoiemFoaXJzaWVyc0B3b29sd29ydGhzLmNvLnphIiwiQzJJZCI6IjQzMzc4MzkiLCJhbXIiOlsiZXh0ZXJuYWwiXX0.bNMe1vvnzjaxO6iLbWQIC-Xi5x7Q6h4b7OZpfDeaMlj8noqnaVvkvmrvLggoypvv33_y0XhiJ6DtBC5MVQiuyU9K1U9w5uzNc2oTBzr5f65l4JbemEKoK4A0FpzM4rTroBDX8p3x_ubtYPa0Toluu9nzpIFeEnsQXNzU-2OGkaZ0nxWvYkJ0wsGeDXZlY0Lsix_OxdO6sgReDf9nL5f9hm6ekBE7oEMKlNAjZkB8TIvhCZXxumad_3SSVEhYDCfZzW-hoIOViUXFkqelVtdQmiRZei2-x65PZMu4EwkF-EYpNI9TNC6N35bYc3HrfK39H7hW979rX8CN-Aj9jOoyXg",0,0);
    }

    public BankAccountTypes getBankAccountTypes(){
        // Session token hardcoded for test purpose
        return  mApiInterface.getBankAccountTypes(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "","eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6ImEzck1VZ01Gdjl0UGNsTGE2eUYzekFrZnF1RSIsImtpZCI6ImEzck1VZ01Gdjl0UGNsTGE2eUYzekFrZnF1RSJ9.eyJpc3MiOiJodHRwczovL3N0cy53b29sd29ydGhzLmNvLnphL2N1c3RvbWVyaWQiLCJhdWQiOiJXV09uZUFwcCIsImV4cCI6MTQ3NTc2NjEyNywibmJmIjoxNDc1NzY1ODI3LCJub25jZSI6ImFjM2UyIiwiaWF0IjoxNDc1NzY1ODI3LCJzaWQiOiI2NjFlNmM1NTdjZWM4MDNiMWU5YmE2YjA1MGFiMTVlZCIsInN1YiI6ImM2YjcyZTYwLTY1ZjgtNGY3ZS1hNWRmLTJiYjgyMWQ5NDZhNyIsImF1dGhfdGltZSI6MTQ3NTc2NTgyNSwiaWRwIjoiV1ctT05MSU5FIiwiZ2l2ZW5fbmFtZSI6IlRFU1QiLCJmYW1pbHlfbmFtZSI6IlBBR0UiLCJBdGdJZCI6IjE3OTEwMDQ1IiwiQXRnU2Vzc2lvbiI6IlZlMmFmMWVUa3pDX2dEak1RcmxmbTFTQVYxNldvWlVTdEwtdzU4emZTNU05ZnFNZVJTQ1IhMTY5MDY0ODc0NCIsImVtYWlsIjoiemFoaXJzaWVyc0B3b29sd29ydGhzLmNvLnphIiwiQzJJZCI6IjQzMzc4MzkiLCJhbXIiOlsiZXh0ZXJuYWwiXX0.bNMe1vvnzjaxO6iLbWQIC-Xi5x7Q6h4b7OZpfDeaMlj8noqnaVvkvmrvLggoypvv33_y0XhiJ6DtBC5MVQiuyU9K1U9w5uzNc2oTBzr5f65l4JbemEKoK4A0FpzM4rTroBDX8p3x_ubtYPa0Toluu9nzpIFeEnsQXNzU-2OGkaZ0nxWvYkJ0wsGeDXZlY0Lsix_OxdO6sgReDf9nL5f9hm6ekBE7oEMKlNAjZkB8TIvhCZXxumad_3SSVEhYDCfZzW-hoIOViUXFkqelVtdQmiRZei2-x65PZMu4EwkF-EYpNI9TNC6N35bYc3HrfK39H7hW979rX8CN-Aj9jOoyXg",0,0);
    }

    public CreateUpdateDeviceResponse getResponseOnCreateUpdateDevice(CreateUpdateDevice device){

        // Session token hardcoded for test purpose
        return  mApiInterface.createUpdateDevice(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "","eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6ImEzck1VZ01Gdjl0UGNsTGE2eUYzekFrZnF1RSIsImtpZCI6ImEzck1VZ01Gdjl0UGNsTGE2eUYzekFrZnF1RSJ9.eyJpc3MiOiJodHRwczovL3N0cy53b29sd29ydGhzLmNvLnphL2N1c3RvbWVyaWQiLCJhdWQiOiJXV09uZUFwcCIsImV4cCI6MTQ3NTc2NjEyNywibmJmIjoxNDc1NzY1ODI3LCJub25jZSI6ImFjM2UyIiwiaWF0IjoxNDc1NzY1ODI3LCJzaWQiOiI2NjFlNmM1NTdjZWM4MDNiMWU5YmE2YjA1MGFiMTVlZCIsInN1YiI6ImM2YjcyZTYwLTY1ZjgtNGY3ZS1hNWRmLTJiYjgyMWQ5NDZhNyIsImF1dGhfdGltZSI6MTQ3NTc2NTgyNSwiaWRwIjoiV1ctT05MSU5FIiwiZ2l2ZW5fbmFtZSI6IlRFU1QiLCJmYW1pbHlfbmFtZSI6IlBBR0UiLCJBdGdJZCI6IjE3OTEwMDQ1IiwiQXRnU2Vzc2lvbiI6IlZlMmFmMWVUa3pDX2dEak1RcmxmbTFTQVYxNldvWlVTdEwtdzU4emZTNU05ZnFNZVJTQ1IhMTY5MDY0ODc0NCIsImVtYWlsIjoiemFoaXJzaWVyc0B3b29sd29ydGhzLmNvLnphIiwiQzJJZCI6IjQzMzc4MzkiLCJhbXIiOlsiZXh0ZXJuYWwiXX0.bNMe1vvnzjaxO6iLbWQIC-Xi5x7Q6h4b7OZpfDeaMlj8noqnaVvkvmrvLggoypvv33_y0XhiJ6DtBC5MVQiuyU9K1U9w5uzNc2oTBzr5f65l4JbemEKoK4A0FpzM4rTroBDX8p3x_ubtYPa0Toluu9nzpIFeEnsQXNzU-2OGkaZ0nxWvYkJ0wsGeDXZlY0Lsix_OxdO6sgReDf9nL5f9hm6ekBE7oEMKlNAjZkB8TIvhCZXxumad_3SSVEhYDCfZzW-hoIOViUXFkqelVtdQmiRZei2-x65PZMu4EwkF-EYpNI9TNC6N35bYc3HrfK39H7hW979rX8CN-Aj9jOoyXg",device);
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
/*   public ConfigResponse getConfig(){
        ApiInterface mApiInterface = new RestAdapter.Builder()
                .setEndpoint(mContext.getString(R.string.config_endpoint))
                .setLogLevel(Util.isDebug(mContext) ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
                .build()
                .create(ApiInterface.class);
        return mApiInterface.getConfig("wfsAndroid",getDeviceID());
    }*/
}
