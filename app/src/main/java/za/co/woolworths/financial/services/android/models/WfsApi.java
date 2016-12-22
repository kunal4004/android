package za.co.woolworths.financial.services.android.models;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.awfs.coordination.R;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.util.ArrayList;


import retrofit.RestAdapter;


import retrofit.client.Client;
import retrofit.client.Header;
import retrofit.client.OkClient;
import retrofit.client.Request;
import retrofit.mime.TypedByteArray;
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
import za.co.woolworths.financial.services.android.util.DatabaseHelper;

import static android.R.attr.value;
import static com.google.android.gms.plus.PlusOneDummyView.TAG;

public class WfsApi {

    private Context mContext;
    private ApiInterface mApiInterface;
    String responseString="{}";
    DatabaseHelper dbHelper;


    protected WfsApi(Context mContext) {
        this.mContext = mContext;
        dbHelper=new DatabaseHelper(mContext,mContext.getFilesDir().getAbsolutePath());
        try {
            dbHelper.prepareDatabase();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
       // HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        //interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient();
        client.interceptors().add(new Interceptor() {
            @Override
            public com.squareup.okhttp.Response intercept(Chain chain) throws IOException {
                com.squareup.okhttp.Request request=chain.request();
              // com.squareup.okhttp.Response response = chain.proceed(request);
                //responseString="{\\\"accountList\\\": [ ],\\\"response\\\": {\\\"code\\\": \\\"-1\\\",\\\"desc\\\": \\\"Success\\\" }, \\\"httpCode\\\": 200}";
               responseString=dbHelper.getApiResponse(3);
                com.squareup.okhttp.Response res=null;
                res=new com.squareup.okhttp.Response.Builder()
                        .code(200)
                        .message(responseString)
                        .request(chain.request())
                        .protocol(Protocol.HTTP_1_0)
                        .body(ResponseBody.create(MediaType.parse("application/json"), responseString.getBytes()))
                        .addHeader("content-type", "application/json")
                        .build();
             return res;
            }
        });

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
