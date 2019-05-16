package za.co.absa.openbankingapi.woolworths.integration;

import android.content.Context;
import android.util.Base64;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import za.co.absa.openbankingapi.AsymmetricCryptoHelper;
import za.co.absa.openbankingapi.Cryptography;
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
import za.co.absa.openbankingapi.woolworths.integration.service.VolleySingleton;
import za.co.woolworths.financial.services.android.util.Utils;

public class AbsaRegisterCredentialRequest {

	private SessionKey sessionKey;
	private VolleySingleton requestQueue;
	private String deviceId;

	public AbsaRegisterCredentialRequest(final Context context){

		try {
			this.sessionKey = SessionKey.generate(context.getApplicationContext());
			this.requestQueue = VolleySingleton.getInstance();
			this.deviceId = Utils.getAbsaUniqueDeviceID();
		} catch (KeyGenerationFailureException | AsymmetricCryptoHelper.AsymmetricEncryptionFailureException | AsymmetricCryptoHelper.AsymmetricKeyGenerationFailureException e) {
			e.printStackTrace();
		}
	}

	public void make(final String aliasId, final String passcode, final JSession jSession, final AbsaBankingOpenApiResponse.ResponseDelegate<RegisterCredentialResponse> responseDelegate){
		Map<String, String> headers = new HashMap<>();
		headers.put("Accept", "application/json");
		headers.put("action", "registerCredential");
		headers.put("JSESSIONID", jSession.getId());


		final byte[] symmetricKey = sessionKey.getKey();
		final String gatewaySymmetricKey = this.sessionKey.getEncryptedKeyBase64Encoded();
		String encryptedAlias = null;
		RegisterCredentialRequest.CredentialVO[] credentialVOs = new RegisterCredentialRequest.CredentialVO[1];

		try{
			encryptedAlias = SymmetricCipher.Aes256EncryptAndBase64Encode(aliasId, symmetricKey, sessionKey.getIV());

			byte[] derivedKey = Cryptography.PasswordBasedKeyDerivationFunction2(aliasId.concat(passcode), deviceId, 1000, 256);
			byte[] encryptedDerivedKey = SymmetricCipher.Aes256Encrypt(sessionKey.getKey(), derivedKey, sessionKey.getIV());
			String base64EncodedEncryptedDerivedKey = Base64.encodeToString(encryptedDerivedKey, Base64.NO_WRAP);

			 credentialVOs[0] = new RegisterCredentialRequest.CredentialVO(encryptedAlias, "MOBILEAPP_5DIGIT_PIN", base64EncodedEncryptedDerivedKey);
		} catch (DecryptionFailureException | UnsupportedEncodingException | KeyGenerationFailureException e) {
			throw new RuntimeException(e);
		}


		final String body = new RegisterCredentialRequest(encryptedAlias, deviceId, credentialVOs, gatewaySymmetricKey, sessionKey.getEncryptedIVBase64Encoded()).getJson();

		final AbsaBankingOpenApiRequest request = new AbsaBankingOpenApiRequest<>(RegisterCredentialResponse.class, headers, body, true, new AbsaBankingOpenApiResponse.Listener<RegisterCredentialResponse>(){

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
				responseDelegate.onFatalError(error);
			}
		});

		List<String> cookies = new ArrayList<>();
		cookies.add(jSession.getCookie().toString());
		request.setCookies(cookies);

		requestQueue.addToRequestQueue(request, AbsaRegisterCredentialRequest.class);
	}
}
