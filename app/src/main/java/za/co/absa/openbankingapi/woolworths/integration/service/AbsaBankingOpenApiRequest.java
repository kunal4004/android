package za.co.absa.openbankingapi.woolworths.integration.service;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class AbsaBankingOpenApiRequest<T> extends Request<T> {

	private final Gson gson = new GsonBuilder().serializeNulls().create();
	private final Class<T> clazz;
	private final Map<String, String> headers;
	private final String body;
	private final AbsaBankingOpenApiResponse.Listener<T> listener;
	private List<HttpCookie> mCookies;

	private AbsaBankingOpenApiRequest(int method, String url, Class<T> clazz, Map<String, String> headers, String body, AbsaBankingOpenApiResponse.Listener<T> listener, Response.ErrorListener errorListener) {
		super(method, url, errorListener);
		this.clazz = clazz;
		this.headers = headers;
		this.listener = listener;
		this.body = body;

        this.setRetryPolicy(new DefaultRetryPolicy(
                18 * 1000,
                1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
	}

	public AbsaBankingOpenApiRequest(Class<T> clazz, Map<String, String> headers, String body, AbsaBankingOpenApiResponse.Listener<T> listener, Response.ErrorListener errorListener) {
		this(Method.POST, "https://eu.absa.co.za/wcob/wfsMobileRegistration", clazz, headers, body, listener, errorListener);
	}

	public AbsaBankingOpenApiRequest(String url, Class<T> clazz, Map<String, String> headers, String body, AbsaBankingOpenApiResponse.Listener<T> listener, Response.ErrorListener errorListener) {
		this(Method.POST, url, clazz, headers, body, listener, errorListener);
	}

	@Override
	public Map<String, String> getHeaders() throws AuthFailureError {

		if (headers == null)
			return super.getHeaders();

		return headers;
	}


	public void setCookies(List<String> cookies) {
		StringBuilder sb = new StringBuilder();
		for (String cookie : cookies) {
			sb.append(cookie).append("; ");
		}
		headers.put("Cookie", sb.toString());
	}

	@Override
	public byte[] getBody() throws AuthFailureError {
		if (this.body == null)
			return super.getBody();

		return this.body.getBytes(StandardCharsets.UTF_8);
	}

	@Override
	protected void deliverResponse(T response) {
		listener.onResponse(response, mCookies);
	}

	@Override
	public String getBodyContentType() {
		if (this.headers.containsKey("Content-Type")){
			return this.headers.get("Content-Type");
		}

		return "application/json";
	}

	@Override
	protected Response<T> parseNetworkResponse(NetworkResponse response) {
		try {
			String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));

			final String cookies = response.headers.get("Set-Cookie");
			if (cookies != null && !cookies.isEmpty()){
				mCookies = HttpCookie.parse(cookies);
			}

			return Response.success(gson.fromJson(json, clazz), HttpHeaderParser.parseCacheHeaders(response));
		} catch (UnsupportedEncodingException e) {
			return Response.error(new ParseError(e));
		} catch (JsonSyntaxException e) {
			return Response.error(new ParseError(e));
		}
	}
}
