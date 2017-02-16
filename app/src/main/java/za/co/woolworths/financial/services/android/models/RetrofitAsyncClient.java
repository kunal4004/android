package za.co.woolworths.financial.services.android.models;

import android.content.Context;
import android.text.TextUtils;

import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.TimeUnit;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.client.OkClient;

import za.co.wigroup.androidutils.Util;
import za.co.woolworths.financial.services.android.util.StringConverter;

public class RetrofitAsyncClient {

    private ApiInterface mApiInterface;
    private Context mContext;

   public RetrofitAsyncClient(Context mContext) {
        this.mContext = mContext;

        OkHttpClient client = new OkHttpClient();
        client.setReadTimeout(60, TimeUnit.SECONDS);
        client.setConnectTimeout(60, TimeUnit.SECONDS);
        client.interceptors().add(new WfsApiInterceptor(mContext));

        mApiInterface = new RestAdapter.Builder()
                .setClient(new OkClient(client))
                .setEndpoint(WoolworthsApplication.getBaseURL())
                .setLogLevel(Util.isDebug(mContext) ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
                .setConverter(new StringConverter())
                .build()
                .create(ApiInterface.class);
    }

    private String getOsVersion() {
        String osVersion = Util.getOsVersion();
        if (TextUtils.isEmpty(osVersion)) {
            String myVersion = android.os.Build.VERSION.RELEASE; // e.g. myVersion := "1.6"
            int sdkVersion = android.os.Build.VERSION.SDK_INT; // e.g. sdkVersion := 8;
            osVersion = String.valueOf(sdkVersion);
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

    private String getSha1Password() {
        return WoolworthsApplication.getSha1Password();
    }

    private String getApiId() {
        return WoolworthsApplication.getApiKey();
    }

    public void getProductDetail(String productId, String skuId, Callback<String> callback) {
        mApiInterface.getProductDetail(getOsVersion(), getDeviceModel(), getOsVersion(),
                getOS(), getNetworkCarrier(), getApiId(), "", "",
                getSha1Password(), productId, skuId,callback);
    }
}

