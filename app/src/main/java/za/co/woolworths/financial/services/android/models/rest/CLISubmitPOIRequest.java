package za.co.woolworths.financial.services.android.models.rest;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.AuthFailureError;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.jar.JarException;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.POIDocumentUploadResponse;
import za.co.woolworths.financial.services.android.util.OnEventListener;

import static com.facebook.FacebookSdk.getApplicationContext;
import static com.google.android.gms.internal.zzagz.runOnUiThread;

/**
 * Created by W7099877 on 2017/11/10.
 */

public class CLISubmitPOIRequest {

	public interface UploadEventListener{
		void onSuccess(POIDocumentUploadResponse response);
		void onFailure(String e);
		void onProgress(int percentage);
	}

	private UploadEventListener mCallBack;
	private Context mContext;
	private String filePath;
	private int fileNumber;
	private int fileTotal;
	private String saID;
	private String cliID;
	private WoolworthsApplication mWoolworthsApp;
	//private Map<String,String> headers;

	public CLISubmitPOIRequest(Context context,String path, String clid,int file, int total, String said,UploadEventListener callback)
	{
		this.mCallBack=callback;
		this.filePath=path;
		this.mContext=context;
		this.fileNumber=file;
		this.fileTotal=total;
		this.saID=said;
		this.cliID=clid;
		this.mWoolworthsApp = ((WoolworthsApplication) ((AppCompatActivity) mContext).getApplication());
	}
	public void execute()
	{
		Map<String,String> headers;
		headers=new ArrayMap<>();
		headers.put("Accept", "application/json");
		headers.put("apiId",mWoolworthsApp.getApi().getApiId());
		headers.put("sha1Password", mWoolworthsApp.getApi().getSha1Password());
		headers.put("deviceVersion", mWoolworthsApp.getApi().getDeviceManufacturer());
		headers.put("deviceModel", mWoolworthsApp.getApi().getDeviceModel());
		headers.put("network", mWoolworthsApp.getApi().getNetworkCarrier());
		headers.put("os", mWoolworthsApp.getApi().getOS());
		headers.put("osVersion", mWoolworthsApp.getApi().getOsVersion());
		headers.put("sessionToken", mWoolworthsApp.getApi().getSessionToken());
		String BASE_URL =  WoolworthsApplication.getBaseURL()+"/user/cli/offer/"+cliID+"/POI?fileTotal="+fileTotal+"&saId="+saID+"&fileNumber="+fileNumber;

		SimpleMultiPartRequest smr = new SimpleMultiPartRequest(Request.Method.POST, BASE_URL,
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						Log.d("Response", response);
						try {
							final POIDocumentUploadResponse poiDocumentUploadResponse=new Gson().fromJson(response,POIDocumentUploadResponse.class);
							//mCallBack.onSuccess(poiDocumentUploadResponse);
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									mCallBack.onSuccess(poiDocumentUploadResponse);
								}
							});

						} catch (final Exception e) {
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									mCallBack.onFailure(e.getMessage());
								}
							});
						}
					}

				}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(final VolleyError error) {

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mCallBack.onFailure(error.getMessage());
					}
				});
			}


		});


		smr.addFile("files", filePath);
		smr.setHeaders(headers);
		smr.setOnProgressListener(new Response.ProgressListener() {
			@Override
			public void onProgress(long transferredBytes, long totalSize) {
				final int percentage = (int) ((transferredBytes /  ((float)totalSize)) * 100);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if(percentage>0)
						     mCallBack.onProgress(percentage);
					}
				});
			}
		});

		WoolworthsApplication.getInstance().addToRequestQueue(smr);

	}
}
