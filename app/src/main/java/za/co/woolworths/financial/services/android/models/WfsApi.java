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
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity;
import za.co.woolworths.financial.services.android.util.DatabaseHelper;

public class WfsApi {

    private Context mContext;
    private ApiInterface mApiInterface;
    String responseString = "";
    public static final String TAG = "WfsApi";


    protected WfsApi(Context mContext) {
        this.mContext = mContext;
        OkHttpClient client = new OkHttpClient();
        client.setReadTimeout(60, TimeUnit.SECONDS);
        client.setConnectTimeout(60, TimeUnit.SECONDS);
        /*client.interceptors().add(new Interceptor() {
            @Override
            public com.squareup.okhttp.Response intercept(Chain chain) throws IOException {
                boolean isCached = false;
                int requestId = 0;
                com.squareup.okhttp.Request request = chain.request();
                com.squareup.okhttp.Response response = null;
                String endpoint = request.urlString().replace(WoolworthsApplication.getBaseURL(), "");
                int id = dbHelper.checkApirequest(endpoint, request.method(), request.headers().toString(), bodyToString(request.body()));
                if (id > 0) {
                    requestId = id;
                    isCached = true;
                }
                if (isCached) {
                    if (dbHelper.checkResponseHandler(requestId)) {
                        responseString = dbHelper.getApiResponse(requestId);
                        Log.d("APIRESPONSEBODY", responseString);
                        response = new Response.Builder()
                                .code(200)
                                .message("")
                                .request(chain.request())
                                .protocol(Protocol.HTTP_1_0)
                                .body(ResponseBody.create(MediaType.parse("application/json"), responseString.getBytes()))
                                .addHeader("content-type", "application/json")
                                .build();

                    } else {
                        response = chain.proceed(request);
                        int code = 0;
                        if (response.code() == 200)
                            code = 1;
                        String body = getResponseBodyString(response);
                        int requestID = dbHelper.addApIRequest(endpoint, request.method(), request.headers().toString(), bodyToString(request.body()));
                        dbHelper.addApIResponse(body, requestID, code);

                    }


                } else {
                    response = chain.proceed(request);
                    String body = getResponseBodyString(response);
                    int code = 0;
                    if (response.code() == 200)
                        code = 1;
                    int requestID = dbHelper.addApIRequest(endpoint, request.method(), request.headers().toString(), bodyToString(request.body()));
                    dbHelper.addApIResponse(body, requestID, code);
                }
                return response;


            }
        });*/
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

    public LocationResponse getLocations(String lat, String lon, String searchString, String radious) {
        return mApiInterface.getStoresLocation(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), lat, lon, searchString, radious);
    }

    public MessageResponse getMessagesResponse(int pageSize, int pageNumber) {
        return mApiInterface.getMessages(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), pageSize, pageNumber);
    }

    public CreateOfferResponse createOfferRequest(CreateOfferRequest offerRequest) {
        return mApiInterface.createOfferRequest(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), offerRequest);
    }

    public DeaBanks getDeaBanks(){
        // Session token hardcoded for test purpose
        return  mApiInterface.getDeaBanks(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "",getSession(),0,0);
    }

    public BankAccountTypes getBankAccountTypes(){
        // Session token hardcoded for test purpose
        return  mApiInterface.getBankAccountTypes(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "","eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6ImEzck1VZ01Gdjl0UGNsTGE2eUYzekFrZnF1RSIsImtpZCI6ImEzck1VZ01Gdjl0UGNsTGE2eUYzekFrZnF1RSJ9.eyJpc3MiOiJodHRwczovL3N0cy53b29sd29ydGhzLmNvLnphL2N1c3RvbWVyaWQiLCJhdWQiOiJXV09uZUFwcCIsImV4cCI6MTQ3NTc2NjEyNywibmJmIjoxNDc1NzY1ODI3LCJub25jZSI6ImFjM2UyIiwiaWF0IjoxNDc1NzY1ODI3LCJzaWQiOiI2NjFlNmM1NTdjZWM4MDNiMWU5YmE2YjA1MGFiMTVlZCIsInN1YiI6ImM2YjcyZTYwLTY1ZjgtNGY3ZS1hNWRmLTJiYjgyMWQ5NDZhNyIsImF1dGhfdGltZSI6MTQ3NTc2NTgyNSwiaWRwIjoiV1ctT05MSU5FIiwiZ2l2ZW5fbmFtZSI6IlRFU1QiLCJmYW1pbHlfbmFtZSI6IlBBR0UiLCJBdGdJZCI6IjE3OTEwMDQ1IiwiQXRnU2Vzc2lvbiI6IlZlMmFmMWVUa3pDX2dEak1RcmxmbTFTQVYxNldvWlVTdEwtdzU4emZTNU05ZnFNZVJTQ1IhMTY5MDY0ODc0NCIsImVtYWlsIjoiemFoaXJzaWVyc0B3b29sd29ydGhzLmNvLnphIiwiQzJJZCI6IjQzMzc4MzkiLCJhbXIiOlsiZXh0ZXJuYWwiXX0.bNMe1vvnzjaxO6iLbWQIC-Xi5x7Q6h4b7OZpfDeaMlj8noqnaVvkvmrvLggoypvv33_y0XhiJ6DtBC5MVQiuyU9K1U9w5uzNc2oTBzr5f65l4JbemEKoK4A0FpzM4rTroBDX8p3x_ubtYPa0Toluu9nzpIFeEnsQXNzU-2OGkaZ0nxWvYkJ0wsGeDXZlY0Lsix_OxdO6sgReDf9nL5f9hm6ekBE7oEMKlNAjZkB8TIvhCZXxumad_3SSVEhYDCfZzW-hoIOViUXFkqelVtdQmiRZei2-x65PZMu4EwkF-EYpNI9TNC6N35bYc3HrfK39H7hW979rX8CN-Aj9jOoyXg",0,0);
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
        return mApiInterface.cliSendEmailRquest(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), getSession(), email);
    }


    public UpdateBankDetailResponse cliUpdateBankDetail(UpdateBankDetail updateBankDetail) {
        return mApiInterface.cliUpdateBankRequest(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), updateBankDetail);
    }


    private String getSession() {
//        String sessionId = ((WoolworthsApplication) mContext).getUserManager().getSession();
//      if (sessionId!=null)
//          return sessionId;
//        else
        return "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6ImEzck1VZ01Gdjl0UGNsTGE2eUYzekFrZnF1RSIsImtpZCI6ImEzck1VZ01Gdjl0UGNsTGE2eUYzekFrZnF1RSJ9.eyJpc3MiOiJodHRwczovL3N0cy53b29sd29ydGhzLmNvLnphL2N1c3RvbWVyaWQiLCJhdWQiOiJXV09uZUFwcCIsImV4cCI6MTQ3NTc2NjEyNywibmJmIjoxNDc1NzY1ODI3LCJub25jZSI6ImFjM2UyIiwiaWF0IjoxNDc1NzY1ODI3LCJzaWQiOiI2NjFlNmM1NTdjZWM4MDNiMWU5YmE2YjA1MGFiMTVlZCIsInN1YiI6ImM2YjcyZTYwLTY1ZjgtNGY3ZS1hNWRmLTJiYjgyMWQ5NDZhNyIsImF1dGhfdGltZSI6MTQ3NTc2NTgyNSwiaWRwIjoiV1ctT05MSU5FIiwiZ2l2ZW5fbmFtZSI6IlRFU1QiLCJmYW1pbHlfbmFtZSI6IlBBR0UiLCJBdGdJZCI6IjE3OTEwMDQ1IiwiQXRnU2Vzc2lvbiI6IlZlMmFmMWVUa3pDX2dEak1RcmxmbTFTQVYxNldvWlVTdEwtdzU4emZTNU05ZnFNZVJTQ1IhMTY5MDY0ODc0NCIsImVtYWlsIjoiemFoaXJzaWVyc0B3b29sd29ydGhzLmNvLnphIiwiQzJJZCI6IjQzMzc4MzkiLCJhbXIiOlsiZXh0ZXJuYWwiXX0.bNMe1vvnzjaxO6iLbWQIC-Xi5x7Q6h4b7OZpfDeaMlj8noqnaVvkvmrvLggoypvv33_y0XhiJ6DtBC5MVQiuyU9K1U9w5uzNc2oTBzr5f65l4JbemEKoK4A0FpzM4rTroBDX8p3x_ubtYPa0Toluu9nzpIFeEnsQXNzU-2OGkaZ0nxWvYkJ0wsGeDXZlY0Lsix_OxdO6sgReDf9nL5f9hm6ekBE7oEMKlNAjZkB8TIvhCZXxumad_3SSVEhYDCfZzW-hoIOViUXFkqelVtdQmiRZei2-x65PZMu4EwkF-EYpNI9TNC6N35bYc3HrfK39H7hW979rX8CN-Aj9jOoyXg";
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

    private String bodyToString(final RequestBody request) {
        try {
            final RequestBody copy = request;
            final Buffer buffer = new Buffer();
            if (copy != null)
                copy.writeTo(buffer);
            else
                return "";
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "did not work";
        }
    }

    public String getResponseBodyString(Response response) throws IOException {
        ResponseBody responseBody = response.body();
        BufferedSource source = responseBody.source();
        source.request(Long.MAX_VALUE); // Buffer the entire body.
        Buffer buffer = source.buffer();
        String responseBodyString = buffer.clone().readString(Charset.forName("UTF-8"));
        return responseBodyString;
    }
}
