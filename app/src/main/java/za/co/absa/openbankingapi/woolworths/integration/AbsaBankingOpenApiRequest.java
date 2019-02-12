package za.co.absa.openbankingapi.woolworths.integration;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public class AbsaBankingOpenApiRequest<T> extends Request<T> {

	private final Gson gson = new GsonBuilder().serializeNulls().create();
	private final Class<T> clazz;
	private Map<String, String> headers;
	private final Response.Listener<T> listener;
	
	private AbsaBankingOpenApiRequest(int method, String url, Class<T> clazz, Map<String, String> headers, Response.Listener<T> listener, Response.ErrorListener errorListener) {
		super(method, url, errorListener);
		this.clazz = clazz;
		this.headers = headers;
		this.listener = listener;
	}

	AbsaBankingOpenApiRequest(Class<T> clazz, Map<String, String> headers, Response.Listener<T> listener, Response.ErrorListener errorListener) {
		this(Method.POST, "https://eu.absa.co.za/wcob/wfsMobileRegistration", clazz, headers, listener, errorListener);
	}

	@Override
	public Map<String, String> getHeaders() throws AuthFailureError {

		if (headers == null){
			return super.getHeaders();
		}

		headers.put("Content-Type", "application/json");
		return headers;
	}

	@Override
	protected void deliverResponse(T response) {
		listener.onResponse(response);
	}

	@Override
	protected Response<T> parseNetworkResponse(NetworkResponse response) {
		try {
			String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
			return Response.success(gson.fromJson(json, clazz), HttpHeaderParser.parseCacheHeaders(response));
		} catch (UnsupportedEncodingException e) {
			return Response.error(new ParseError(e));
		} catch (JsonSyntaxException e) {
			return Response.error(new ParseError(e));
		}
	}
}
