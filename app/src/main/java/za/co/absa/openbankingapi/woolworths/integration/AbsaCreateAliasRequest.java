package za.co.absa.openbankingapi.woolworths.integration;

import android.content.Context;
import android.util.Base64;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import za.co.absa.openbankingapi.AsymmetricCryptoHelper;
import za.co.absa.openbankingapi.DecryptionFailureException;
import za.co.absa.openbankingapi.KeyGenerationFailureException;
import za.co.absa.openbankingapi.SessionKey;
import za.co.absa.openbankingapi.SymmetricCipher;
import za.co.absa.openbankingapi.woolworths.integration.dto.CreateAliasRequest;
import za.co.absa.openbankingapi.woolworths.integration.dto.CreateAliasResponse;
import za.co.absa.openbankingapi.woolworths.integration.dto.Header;
import za.co.absa.openbankingapi.woolworths.integration.service.AbsaBankingOpenApiRequest;
import za.co.absa.openbankingapi.woolworths.integration.service.AbsaBankingOpenApiResponse;
import za.co.absa.openbankingapi.woolworths.integration.service.IAbsaBankingOpenApiResponseListener;

public class AbsaCreateAliasRequest {

	private SessionKey sessionKey;
	private RequestQueue requestQueue;

	public AbsaCreateAliasRequest(final Context context){

		try {
			this.sessionKey = SessionKey.generate(context.getApplicationContext());
			this.requestQueue = Volley.newRequestQueue(context.getApplicationContext());
		} catch (KeyGenerationFailureException | AsymmetricCryptoHelper.AsymmetricEncryptionFailureException | AsymmetricCryptoHelper.AsymmetricKeyGenerationFailureException e) {
			e.printStackTrace();
		}
	}

	public void make(final String deviceId, final String jsessionId, final IAbsaBankingOpenApiResponseListener<CreateAliasResponse> responseListener){
		Map<String, String> headers = new HashMap<>();
		headers.put("Content-Type", "application/json");
		headers.put("Accept", "application/json");
		headers.put("action", "createAlias");
		headers.put("JSESSIONID", jsessionId);

		final String body = new CreateAliasRequest(deviceId, sessionKey.getEncryptedKeyBase64Encoded()).getJson();
		final AbsaBankingOpenApiRequest request = new AbsaBankingOpenApiRequest<>(CreateAliasResponse.class, headers, body, new AbsaBankingOpenApiResponse.Listener<CreateAliasResponse>(){

			@Override
			public void onResponse(CreateAliasResponse response, String cookies) {
				Header.ResultMessage[] resultMessages = response.getHeader().getResultMessages();
				if (resultMessages == null || resultMessages.length == 0){
					final String encryptedAlias = Base64.encodeToString(response.getAliasId().getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
					byte[] decryptedAlias;

					try {
						decryptedAlias = SymmetricCipher.Aes256Decrypt(sessionKey.getKey(), encryptedAlias.getBytes(StandardCharsets.UTF_8));
					} catch (DecryptionFailureException e) {
						e.printStackTrace();
					}



					responseListener.onSuccess(response, cookies);
				}

				else
					responseListener.onFailure(resultMessages[0].getResponseMessage());
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				responseListener.onFailure(error.getMessage());
			}
		});

		List<String> cookies = new ArrayList<>();
		cookies.add("JSESSIONID=0000" + jsessionId + ":19maojp8e");
		request.setCookies(cookies);

		requestQueue.add(request);
	}
}
