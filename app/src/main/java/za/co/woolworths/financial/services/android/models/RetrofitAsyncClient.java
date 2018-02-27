package za.co.woolworths.financial.services.android.models;

import android.content.Context;
import android.location.Location;
import android.text.TextUtils;
import android.util.Log;

import com.jakewharton.retrofit.Ok3Client;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.client.Response;
import za.co.wigroup.androidutils.Util;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.statement.GetStatement;
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
		return WoolworthsApplication.getApiKey();
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

	public void getShoppingCart(Callback<String> callback) {
		mApiInterface.getShoppingCart(getApiId(), getSha1Password(), getOsVersion(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken(), callback);
	}

	public void getPDFResponse(GetStatement getStatement, Callback<Response> callback) {
		mApiInterface.getStatement(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), getStatement.getDocId(), getStatement.getProductOfferingId(), getStatement.getDocDesc(), callback);
	}

	public void removeCartItem(String commerceId,Callback<String> callback){
		mApiInterface.removeItemFromCart(getApiId(),getSha1Password(),getOsVersion(),getDeviceModel(),getNetworkCarrier(),getOS(),getOsVersion(),getSessionToken(),commerceId,callback);
	}

	public void removeAllCartItems(Callback<String> callback){
		mApiInterface.removeAllCartItems(getApiId(),getSha1Password(),getOsVersion(),getDeviceModel(),getNetworkCarrier(),getOS(),getOsVersion(),getSessionToken(),callback);
	}

	public String setInvalidToken() {
		return "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6ImEzck1VZ01Gdjl0UGNsTGE2eUYzekFrZnF1RSIsImtpZCI6ImEzck1VZ01Gdjl0UGNsTGE2eUYzekFrZnF1RSJ9.eyJpc3MiOiJodHRwczovL3N0c3FhLndvb2x3b3J0aHMuY28uemEvY3VzdG9tZXJpZCIsImF1ZCI6IldXT25lQXBwIiwiZXhwIjoxNTE3NjQ4OTU1LCJuYmYiOjE1MTcyMTY5NTUsIm5vbmNlIjoiMDJFMjZDRTctQzNBNC00ODFFLUE5NUItNUVDM0IxNjI5N0FFIiwiaWF0IjoxNTE3MjE2OTU1LCJzaWQiOiIzN2JlNzAxM2EwNTM0NGIzNGQ0MDA5MmYzNzc4YzgyMSIsInN1YiI6Ijg3MTgyMmJiLThjZTEtNDk4OS1iMWI4LTFiYmY1NGE5MTk1ZiIsImF1dGhfdGltZSI6MTUxNzIxNjk1NCwiaWRwIjoiaWRzcnYiLCJ1cGRhdGVkX2F0IjoiMTUxNzIwOTc1NCIsInRlbmFudCI6ImRlZmF1bHQiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiIzMTM1ZWU3MjQ3NWM0YWFlOTFhNzZkMDQzOGY4MjJjZiIsImVtYWlsIjoidGVzdDIzQGdtYWlsLmNvbSIsImVtYWlsX3ZlcmlmaWVkIjoiZmFsc2UiLCJuYW1lIjoiTUZJU0hBTkUgTUFSSUEiLCJmYW1pbHlfbmFtZSI6Ik1BSExBTkdVIiwiQXRnSWQiOiIyNjMwMDAwMzMiLCJBdGdTZXNzaW9uIjoie1wiSlNFU1NJT05JRFwiOlwiZGxWQkxWZFZhV2F5ZnNCWEdlRUtrTEFmZmZCRU53V1NzRGlqcWI0MkxUalZfV2hhYlFwcSEtMjA5NjcyOTI1NFwiLFwiX2R5blNlc3NDb25mXCI6XCItNDY4NTQ0NjU5OTcyMDE5MTg5NFwifSIsIkMySWQiOiIyMDg2NzE";
	}


}

