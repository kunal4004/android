package za.co.absa.openbankingapi.woolworths.integration.service;

import android.text.TextUtils;
import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import za.co.absa.openbankingapi.DecryptionFailureException;
import za.co.absa.openbankingapi.KeyGenerationFailureException;
import za.co.absa.openbankingapi.SessionKey;
import za.co.absa.openbankingapi.SymmetricCipher;
import za.co.absa.openbankingapi.woolworths.integration.AbsaContentEncryptionRequest;
import za.co.absa.openbankingapi.woolworths.integration.dto.CEKDResponse;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;

public class AbsaBankingOpenApiRequest<T> extends Request<T> {

    private final Gson gson = new GsonBuilder().serializeNulls().create();
    private final Class<T> clazz;
    private final Map<String, String> headers;
    private String body;
    private final AbsaBankingOpenApiResponse.Listener<T> listener;
    private List<HttpCookie> mCookies;
    private byte[] iv;
    private boolean isBodyEncryptionRequired;

    private AbsaBankingOpenApiRequest(final int method, final String url, final Class<T> clazz, final Map<String, String> headers, String body, final boolean isBodyEncryptionRequired, final AbsaBankingOpenApiResponse.Listener<T> listener, final Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.clazz = clazz;
        this.headers = headers;
        this.listener = listener;
        this.isBodyEncryptionRequired = isBodyEncryptionRequired;

        try {
            this.iv = SessionKey.generateKey(SessionKey.OUTPUT_KEY_LENGTH_IV).getEncoded();
        } catch (KeyGenerationFailureException e) {
            e.printStackTrace();
        }

        if (this.isBodyEncryptionRequired) {

            if (TextUtils.isEmpty(AbsaContentEncryptionRequest.keyId) || AbsaContentEncryptionRequest.derivedSeed.length == 0) {

                final String finalBody = body;
                new AbsaContentEncryptionRequest(WoolworthsApplication.getAppContext()).make(new AbsaBankingOpenApiResponse.ResponseDelegate<CEKDResponse>() {

                    @Override
                    public void onSuccess(CEKDResponse response, List<HttpCookie> cookies) {
                        new AbsaBankingOpenApiRequest<>(method, url, clazz, headers, finalBody, isBodyEncryptionRequired, listener, errorListener);
                    }

                    @Override
                    public void onFailure(String errorMessage) {

                    }

                    @Override
                    public void onFatalError(VolleyError error) {

                    }

                });

                return;
            }

            List<String> cookies = new ArrayList<>();
            cookies.add(AbsaContentEncryptionRequest.jSession.getCookie().toString());
            this.setCookies(cookies);

            this.headers.put("x-encrypted", body.length() + "|" + AbsaContentEncryptionRequest.keyId);
            body = getEncryptedBody(body);
        }

        this.body = body;


        this.setRetryPolicy(new DefaultRetryPolicy(
                18 * 1000,
                1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleySingleton.getInstance().addToRequestQueue(this,clazz);

    }

    public AbsaBankingOpenApiRequest(Class<T> clazz, Map<String, String> headers, String body, boolean isBodyEncryptionRequired, AbsaBankingOpenApiResponse.Listener<T> listener, Response.ErrorListener errorListener) {
        this(Method.POST, WoolworthsApplication.getAbsaBankingOpenApiServices().getBaseURL() + "/wcob/wfsMobileRegistration", clazz, headers, body, isBodyEncryptionRequired, listener, errorListener);
    }

    public AbsaBankingOpenApiRequest(String url, Class<T> clazz, Map<String, String> headers, String body, boolean isBodyEncryptionRequired, AbsaBankingOpenApiResponse.Listener<T> listener, Response.ErrorListener errorListener) {
        this(Method.POST, url, clazz, headers, body, isBodyEncryptionRequired, listener, errorListener);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {

        if (headers == null)
            return super.getHeaders();

        return headers;
    }


    public void setCookies(List<String> cookies) {
        StringBuilder sb = new StringBuilder();

        
        //if (!TextUtils.isEmpty(headers.get("Cookie")))
           // sb.append("; ").append(headers.get("Cookie")).append("; ");

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
        if (this.headers.containsKey("Content-Type")) {
            return this.headers.get("Content-Type");
        }

        return "application/json";
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));

            final String cookies = response.headers.get("Set-Cookie");
            if (cookies != null && !cookies.isEmpty()) {
                mCookies = HttpCookie.parse(cookies);
            }

            if (isBodyEncryptionRequired)
                json = getDecryptedResponse(json);

            return Response.success(gson.fromJson(json, clazz), HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        }
    }

    private String getEncryptedBody(String body) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(this.iv);
            outputStream.write(body.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String encryptionResult = null;

        try {
            encryptionResult = Base64.encodeToString(SymmetricCipher.Aes256Encrypt(AbsaContentEncryptionRequest.derivedSeed, outputStream.toByteArray(), this.iv), Base64.NO_WRAP);
        } catch (DecryptionFailureException e) {
            e.printStackTrace();
        }

        return encryptionResult;
    }

    private String getDecryptedResponse(String json){

        byte[] response = Base64.decode(json, Base64.DEFAULT);
        byte[] ivForDecrypt = Arrays.copyOfRange(response, 0, 16);
        byte[] encryptedResponse = Arrays.copyOfRange(response, 16, response.length);

        String decryptedResponse = null;

        try {
            decryptedResponse = new String(SymmetricCipher.Aes256Decrypt(AbsaContentEncryptionRequest.derivedSeed, encryptedResponse, ivForDecrypt), StandardCharsets.UTF_8);
        } catch (DecryptionFailureException e) {
            e.printStackTrace();
        }

        return decryptedResponse;
    }
}
