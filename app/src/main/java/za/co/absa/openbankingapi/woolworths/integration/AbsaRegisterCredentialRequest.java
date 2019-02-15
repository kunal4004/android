package za.co.absa.openbankingapi.woolworths.integration;

import android.content.Context;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import java.net.HttpCookie;
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
import za.co.absa.openbankingapi.woolworths.integration.dto.Header;
import za.co.absa.openbankingapi.woolworths.integration.dto.RegisterCredentialRequest;
import za.co.absa.openbankingapi.woolworths.integration.dto.RegisterCredentialResponse;
import za.co.absa.openbankingapi.woolworths.integration.service.AbsaBankingOpenApiRequest;
import za.co.absa.openbankingapi.woolworths.integration.service.AbsaBankingOpenApiResponse;

public class AbsaRegisterCredentialRequest {

	private SessionKey sessionKey;
	private RequestQueue requestQueue;

	public AbsaRegisterCredentialRequest(final Context context){

		try {
			this.sessionKey = SessionKey.generate(context.getApplicationContext());
			this.requestQueue = Volley.newRequestQueue(context.getApplicationContext());
		} catch (KeyGenerationFailureException | AsymmetricCryptoHelper.AsymmetricEncryptionFailureException | AsymmetricCryptoHelper.AsymmetricKeyGenerationFailureException e) {
			e.printStackTrace();
		}
	}

	public void make(final String aliasId, final String deviceId, final String credential, final JSession jSession, final AbsaBankingOpenApiResponse.ResponseDelegate<RegisterCredentialResponse> responseDelegate){
		Map<String, String> headers = new HashMap<>();
		headers.put("Content-Type", "application/json");
		headers.put("Accept", "application/json");
		headers.put("action", "registerCredential");
		headers.put("JSESSIONID", jSession.getId());


		final byte[] symmetricKey = sessionKey.getKey();
		final String gatewaySymmetricKey = this.sessionKey.getEncryptedKeyBase64Encoded();
		String encryptedAlias = null;
		String encryptedCredential = null;
		RegisterCredentialRequest.CredentialVO[] credentialVOs = new RegisterCredentialRequest.CredentialVO[1];

		try{
			encryptedAlias = SymmetricCipher.Aes256EncryptAndBase64Encode(aliasId, symmetricKey);
			encryptedCredential = SymmetricCipher.Aes256EncryptAndBase64Encode(credential, symmetricKey);
			credentialVOs[0] = new RegisterCredentialRequest.CredentialVO(encryptedAlias, "MOBILEAPP_5DIGIT_PIN", encryptedCredential);
		} catch (DecryptionFailureException e) {
			throw new RuntimeException(e);
		}


		final String body = new RegisterCredentialRequest(encryptedAlias, deviceId, credentialVOs, gatewaySymmetricKey).getJson();

		final AbsaBankingOpenApiRequest request = new AbsaBankingOpenApiRequest<>(RegisterCredentialResponse.class, headers, body, new AbsaBankingOpenApiResponse.Listener<RegisterCredentialResponse>(){

			@Override
			public void onResponse(RegisterCredentialResponse response, List<HttpCookie> cookies) {
				Header.ResultMessage[] resultMessages = response.getHeader().getResultMessages();
				if (resultMessages == null || resultMessages.length == 0){
					responseDelegate.onSuccess(response, cookies);
				}

				else
					responseDelegate.onFailure(resultMessages[0].getResponseMessage());
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {

			}
		});

		List<String> cookies = new ArrayList<>();
		cookies.add(jSession.getCookie().toString());
		request.setCookies(cookies);

		requestQueue.add(request);
	}
}
