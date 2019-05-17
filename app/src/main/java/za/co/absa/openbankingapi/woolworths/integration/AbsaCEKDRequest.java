package za.co.absa.openbankingapi.woolworths.integration;

import android.content.Context;
import android.util.Base64;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import za.co.absa.openbankingapi.AsymmetricCryptoHelper;
import za.co.absa.openbankingapi.Cryptography;
import za.co.absa.openbankingapi.KeyGenerationFailureException;
import za.co.absa.openbankingapi.SessionKey;
import za.co.absa.openbankingapi.woolworths.integration.dao.JSession;
import za.co.absa.openbankingapi.woolworths.integration.dto.CEKDRequest;
import za.co.absa.openbankingapi.woolworths.integration.dto.CEKDResponse;
import za.co.absa.openbankingapi.woolworths.integration.service.AbsaBankingOpenApiRequest;
import za.co.absa.openbankingapi.woolworths.integration.service.AbsaBankingOpenApiResponse;
import za.co.absa.openbankingapi.woolworths.integration.service.VolleySingleton;
import za.co.woolworths.financial.services.android.util.Utils;

public class AbsaCEKDRequest {

    private VolleySingleton requestQueue;
    private byte[] contentEncryptionSeed;
    public static byte[] derivedSeed;
    private String deviceId;
    public static JSession jSession = new JSession();
    public static String keyId;


    public AbsaCEKDRequest(final Context context) {

        try {
            this.deviceId = Utils.getAbsaUniqueDeviceID();
            byte[] seed = SessionKey.generateKey(SessionKey.OUTPUT_KEY_LENGTH).getEncoded();
            this.contentEncryptionSeed = new AsymmetricCryptoHelper().encryptSymmetricKey(context, seed, SessionKey.CONTENT_ENCRYPTION_KEY_FILE);
            derivedSeed = Cryptography.PasswordBasedKeyDerivationFunction2(deviceId, seed, 1000, 256);
            //this.requestQueue = VolleySingleton.getInstance();
        } catch (UnsupportedEncodingException | KeyGenerationFailureException | AsymmetricCryptoHelper.AsymmetricEncryptionFailureException | AsymmetricCryptoHelper.AsymmetricKeyGenerationFailureException e) {
            e.printStackTrace();
        }
    }

    public void make(final AbsaBankingOpenApiResponse.ResponseDelegate<CEKDResponse> responseDelegate) {

        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");

        String body = null;
        try {
            body = new CEKDRequest(deviceId, Base64.encodeToString(contentEncryptionSeed, Base64.NO_WRAP)).getJson();
        } catch (Exception e) {
            e.printStackTrace();
        }

        final AbsaBankingOpenApiRequest request = new AbsaBankingOpenApiRequest<>("https://eu.absa.co.za/wcob/cekd", CEKDResponse.class, headers, body, false, new AbsaBankingOpenApiResponse.Listener<CEKDResponse>() {

            @Override
            public void onResponse(CEKDResponse response, List<HttpCookie> cookies) {
                for (HttpCookie cookie : cookies) {
                    if (cookie.getName().equalsIgnoreCase("jsessionid")) {
                        jSession.setCookie(cookie);
                    }
                }
                keyId = response.keyId;
                responseDelegate.onSuccess(response, cookies);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                responseDelegate.onFatalError(error);

            }
        });

        //requestQueue.addToRequestQueue(request, AbsaCEKDRequest.class);

    }
}
