package za.co.absa.openbankingapi.woolworths.integration;

import android.content.Context;
import android.util.Base64;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import java.net.HttpCookie;
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
import za.co.absa.openbankingapi.woolworths.integration.dao.JSession;
import za.co.absa.openbankingapi.woolworths.integration.dto.CreateAliasRequest;
import za.co.absa.openbankingapi.woolworths.integration.dto.CreateAliasResponse;
import za.co.absa.openbankingapi.woolworths.integration.dto.Header;
import za.co.absa.openbankingapi.woolworths.integration.service.AbsaBankingOpenApiRequest;
import za.co.absa.openbankingapi.woolworths.integration.service.AbsaBankingOpenApiResponse;

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

	public void make(final String deviceId, final JSession jSession, final AbsaBankingOpenApiResponse.ResponseDelegate<CreateAliasResponse> responseDelegate){
		Map<String, String> headers = new HashMap<>();
		headers.put("Accept", "application/json");
		headers.put("action", "createAlias");
		headers.put("JSESSIONID", jSession.getId());

		final String body = new CreateAliasRequest(deviceId, sessionKey.getEncryptedKeyBase64Encoded(), sessionKey.getEncryptedIVBase64Encoded()).getJson();
		final AbsaBankingOpenApiRequest request = new AbsaBankingOpenApiRequest<>(CreateAliasResponse.class, headers, body, new AbsaBankingOpenApiResponse.Listener<CreateAliasResponse>(){

			@Override
			public void onResponse(CreateAliasResponse response, List<HttpCookie> cookies) {
				Header.ResultMessage[] resultMessages = response.getHeader().getResultMessages();
				if (resultMessages == null || resultMessages.length == 0){
					try{
						byte[] encryptedAliasBytes = response.getAliasId().getBytes(StandardCharsets.UTF_8);
						byte[] encryptedAliasBase64DecodedBytes = Base64.decode(encryptedAliasBytes, Base64.NO_WRAP);

						byte[] aliasBytes = SymmetricCipher.Aes256Decrypt(sessionKey.getKey(), encryptedAliasBase64DecodedBytes, sessionKey.getIV());
						byte[] aliasBase64DecodedBytes = Base64.decode(Base64.encodeToString(aliasBytes, Base64.NO_WRAP), Base64.DEFAULT);
						String decryptedAlias = new String(aliasBase64DecodedBytes, StandardCharsets.UTF_8);

						response.setAliasId(decryptedAlias);
					}catch (DecryptionFailureException e){
						//TODO: Handle decryption issue
						throw new RuntimeException(e);
					}

					responseDelegate.onSuccess(response, cookies);
				}

				else
					responseDelegate.onFailure(resultMessages[0].getResponseMessage());
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				responseDelegate.onFatalError(error);
			}
		});

		List<String> cookies = new ArrayList<>();
		cookies.add(jSession.getCookie().toString());
		request.setCookies(cookies);

		requestQueue.add(request);
	}
}
