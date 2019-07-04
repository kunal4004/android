package za.co.absa.openbankingapi.woolworths.integration;

import android.content.Context;
import android.util.Base64;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.crashlytics.android.Crashlytics;

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
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.util.Utils;

public class AbsaContentEncryptionRequest {

    private byte[] contentEncryptionSeed;
    public static byte[] derivedSeed;
    private String deviceId;
    public static JSession jSession;
    public static String keyId;


    public AbsaContentEncryptionRequest() {

        try {
            this.deviceId = Utils.getAbsaUniqueDeviceID();
            byte[] seed = SessionKey.generateKey(SessionKey.OUTPUT_KEY_LENGTH).getEncoded();
            this.contentEncryptionSeed = new AsymmetricCryptoHelper().encryptSymmetricKey(seed, WoolworthsApplication.getAbsaBankingOpenApiServices().getContentEncryptionPublicKey());
            derivedSeed = Cryptography.PasswordBasedKeyDerivationFunction2(deviceId, seed, 1000, 256);
        } catch (UnsupportedEncodingException | KeyGenerationFailureException | AsymmetricCryptoHelper.AsymmetricEncryptionFailureException | AsymmetricCryptoHelper.AsymmetricKeyGenerationFailureException e) {
            Crashlytics.logException(e);
        }
    }

    public void make(final AbsaBankingOpenApiResponse.ResponseDelegate<CEKDResponse> responseDelegate) {

        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");

        String body = null;
        try {
            body = new CEKDRequest(deviceId, Base64.encodeToString(contentEncryptionSeed, Base64.NO_WRAP)).getJson();
        } catch (Exception e) {
            Crashlytics.logException(e);
        }

        new AbsaBankingOpenApiRequest<>(WoolworthsApplication.getAbsaBankingOpenApiServices().getBaseURL() + "/wcob/cekd", CEKDResponse.class, headers, body, false, new AbsaBankingOpenApiResponse.Listener<CEKDResponse>() {

            @Override
            public void onResponse(CEKDResponse response, List<HttpCookie> cookies) {
                for (HttpCookie cookie : cookies) {
                    if (cookie.getName().equalsIgnoreCase("jsessionid")) {
                        jSession = new JSession(cookie.getName(),cookie);
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

    }

    public static void clearContentEncryptionData(){
        derivedSeed = null;
        jSession = null;
        keyId = null;
    }
}
