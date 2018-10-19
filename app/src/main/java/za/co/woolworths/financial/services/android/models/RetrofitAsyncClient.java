package za.co.woolworths.financial.services.android.models;

import android.content.Context;
import android.location.Location;
import android.text.TextUtils;

import com.jakewharton.retrofit.Ok3Client;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.client.Response;
import za.co.wigroup.androidutils.Util;
import za.co.woolworths.financial.services.android.models.dto.statement.GetStatement;
import za.co.woolworths.financial.services.android.util.SessionUtilities;
import za.co.woolworths.financial.services.android.util.StringConverter;
import za.co.woolworths.financial.services.android.util.Utils;

import static za.co.wigroup.androidutils.Util.getDeviceManufacturer;

public class RetrofitAsyncClient {

	private ApiInterface mApiInterface;
	private Context mContext;
	private Location loc;
	public static final String TAG = "RetrofitAsyncClient";

	public RetrofitAsyncClient(Context mContext) {
		this.mContext = mContext;

		OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder();
		httpBuilder.addInterceptor(new WfsApiInterceptor(mContext));
		httpBuilder.readTimeout(45, TimeUnit.SECONDS);
		httpBuilder.connectTimeout(45, TimeUnit.SECONDS);

		mApiInterface = new RestAdapter.Builder()
				.setClient(new Ok3Client(httpBuilder.build()))
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
		return networkCarrier.isEmpty() ? "Unavailable" : Utils.removeUnicodesFromString(networkCarrier);
	}

	private String getDeviceModel() {
		return Util.getDeviceModel();
	}

	private String getSha1Password() {
		return WoolworthsApplication.getSha1Password();
	}

	private String getApiId() {
		return WoolworthsApplication.getApiId();
	}

	public String getSessionToken() {
		String sessionToken = SessionUtilities.getInstance().getSessionToken();
		if (sessionToken.isEmpty())
			return "";
		else
			return sessionToken;
	}

	public void getProductDetail(String productId, String skuId, Callback<String> callback) {
		getMyLocation();
		if (Utils.isLocationEnabled(mContext)) {
			mApiInterface.getProductDetail(getOsVersion(), getDeviceModel(), getOsVersion(),
					getOS(), getNetworkCarrier(), getApiId(), "", "",
					getSha1Password(), loc.getLongitude(), loc.getLatitude(), productId, skuId, callback);
		} else {
			mApiInterface.getProductDetail(getOsVersion(), getDeviceModel(), getOsVersion(),
					getOS(), getNetworkCarrier(), getApiId(), "", "",
					getSha1Password(), productId, skuId, callback);
		}
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


	public void getPDFResponse(GetStatement getStatement, Callback<Response> callback) {
		mApiInterface.getStatement(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), getStatement.getDocId(), getStatement.getProductOfferingId(), getStatement.getDocDesc(), callback);
	}



}

