package za.co.woolworths.financial.services.android.models;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.awfs.coordination.R;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

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

    public DeleteMessageResponse getDeleteMessagesResponse(String id) {
        return mApiInterface.getDeleteresponse(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), id);
    }

    public ReadMessagesResponse getReadMessagesResponse(MessageReadRequest readMessages) {
        return mApiInterface.setReadMessages(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), readMessages);
    }
    
    public CreateUpdateDeviceResponse getResponseOnCreateUpdateDevice(CreateUpdateDevice device) {
        return mApiInterface.createUpdateDevice(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), device);
    }

    private String getOsVersion() {
        return Util.getOsVersion();
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
