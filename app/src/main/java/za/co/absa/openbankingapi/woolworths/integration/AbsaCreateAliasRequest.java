package za.co.absa.openbankingapi.woolworths.integration;

import android.util.Base64;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.net.HttpCookie;
import java.nio.charset.StandardCharsets;
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
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager;

public class AbsaCreateAliasRequest {

	private SessionKey sessionKey;

	public AbsaCreateAliasRequest(){

		try {
			this.sessionKey = SessionKey.generate();
		} catch (KeyGenerationFailureException | AsymmetricCryptoHelper.AsymmetricEncryptionFailureException | AsymmetricCryptoHelper.AsymmetricKeyGenerationFailureException e) {
			FirebaseManager.Companion.logException(e);
		}
	}

	public void make(final String deviceId, final AbsaBankingOpenApiResponse.ResponseDelegate<CreateAliasResponse> responseDelegate){
		Map<String, String> headers = new HashMap<>();
		headers.put("Accept", "application/json");
		headers.put("action", "createAlias");
		//headers.put("JSESSIONID", jSession.getId());

		final String body = new CreateAliasRequest(deviceId, sessionKey.getEncryptedKeyBase64Encoded(), sessionKey.getEncryptedIVBase64Encoded()).getJson();
		new AbsaBankingOpenApiRequest<>(CreateAliasResponse.class, headers, body, true, new AbsaBankingOpenApiResponse.Listener<CreateAliasResponse>(){

			@Override
			public void onResponse(CreateAliasResponse response, List<HttpCookie> cookies) {
				Header.ResultMessage[] resultMessages = response.getHeader().getResultMessages();
				String statusCode = "0";
				try {
					statusCode = response.getHeader().getStatusCode();
				} catch (Exception e) {
					FirebaseManager.Companion.logException(e);
				}

				if (resultMessages == null || resultMessages.length == 0 && statusCode.equalsIgnoreCase("0")){
					try{
						byte[] encryptedAliasBytes = response.getAliasId().getBytes(StandardCharsets.UTF_8);
						byte[] encryptedAliasBase64DecodedBytes = Base64.decode(encryptedAliasBytes, Base64.NO_WRAP);

						byte[] aliasBytes = SymmetricCipher.Aes256Decrypt(sessionKey.getKey(), encryptedAliasBase64DecodedBytes, sessionKey.getIV());
						byte[] aliasBase64DecodedBytes = Base64.decode(Base64.encodeToString(aliasBytes, Base64.NO_WRAP), Base64.DEFAULT);
						String decryptedAlias = new String(aliasBase64DecodedBytes, StandardCharsets.UTF_8);

						response.setAliasId(decryptedAlias);
					}catch (DecryptionFailureException e){
						//TODO: Handle decryption issue
						FirebaseManager.Companion.logException(e);
						throw new RuntimeException(e);
					}

					responseDelegate.onSuccess(response, cookies);
				}

				else {
						responseDelegate.onFailure(resultMessages[0].getResponseMessage());
				}

				//Clearing up sensitive info
				sessionKey = null;
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				responseDelegate.onFatalError(error);
				//Clearing up sensitive info
				sessionKey = null;
			}
		});

		}
}
