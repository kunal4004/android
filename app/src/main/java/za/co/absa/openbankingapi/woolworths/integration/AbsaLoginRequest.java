package za.co.absa.openbankingapi.woolworths.integration;

import android.content.Context;
import android.util.Base64;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import za.co.absa.openbankingapi.AsymmetricCryptoHelper;
import za.co.absa.openbankingapi.Cryptography;
import za.co.absa.openbankingapi.DecryptionFailureException;
import za.co.absa.openbankingapi.KeyGenerationFailureException;
import za.co.absa.openbankingapi.SessionKey;
import za.co.absa.openbankingapi.SymmetricCipher;
import za.co.absa.openbankingapi.woolworths.integration.dto.LoginRequest;
import za.co.absa.openbankingapi.woolworths.integration.dto.LoginResponse;
import za.co.absa.openbankingapi.woolworths.integration.service.AbsaBankingOpenApiRequest;
import za.co.absa.openbankingapi.woolworths.integration.service.AbsaBankingOpenApiResponse;

public class AbsaLoginRequest {

	private SessionKey sessionKey;
	private RequestQueue requestQueue;

	public AbsaLoginRequest(final Context context){

		try {
			this.sessionKey = SessionKey.generate(context.getApplicationContext());
			this.requestQueue = Volley.newRequestQueue(context.getApplicationContext());
		} catch (KeyGenerationFailureException | AsymmetricCryptoHelper.AsymmetricEncryptionFailureException | AsymmetricCryptoHelper.AsymmetricKeyGenerationFailureException e) {
			e.printStackTrace();
		}
	}

	public void make(final String passcode, final String aliasId, final String deviceId, final AbsaBankingOpenApiResponse.ResponseDelegate<LoginResponse> responseDelegate){
		Map<String, String> headers = new HashMap<>();
		headers.put("Content-Type", "application/x-www-form-urlencoded");
		headers.put("Accept", "application/json");

		final String gatewaySymmetricKey = this.sessionKey.getEncryptedKeyBase64Encoded();
		String base64EncodedEncryptedDerivedKey = null;
		String encryptedAlias = null;

		try{
			encryptedAlias = Base64.encodeToString(SymmetricCipher.Aes256Encrypt(sessionKey.getKey(),aliasId),Base64.NO_WRAP);

			byte[] derivedKey = Cryptography.PasswordBasedKeyDerivationFunction2(aliasId.concat(passcode), deviceId, 1000, 256);
			byte[] encryptedDerivedKey = SymmetricCipher.Aes256Encrypt(sessionKey.getKey(), derivedKey);
			base64EncodedEncryptedDerivedKey = Base64.encodeToString(encryptedDerivedKey, Base64.NO_WRAP);

		} catch (DecryptionFailureException | UnsupportedEncodingException | KeyGenerationFailureException e) {
			throw new RuntimeException(e);
		}

		String body = null;
		try{
			body = new LoginRequest(encryptedAlias, deviceId, base64EncodedEncryptedDerivedKey, gatewaySymmetricKey, sessionKey.getEncryptedIVBase64Encoded()).getUrlEncodedFormData();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		final AbsaBankingOpenApiRequest request = new AbsaBankingOpenApiRequest<>("https://eu.absa.co.za/wcob/j_pin_security_login", LoginResponse.class, headers, body, new AbsaBankingOpenApiResponse.Listener<LoginResponse>(){

			@Override
			public void onResponse(LoginResponse loginResponse, List<HttpCookie> cookies) {
				final String nonce = loginResponse.getNonce();
				final String resultMessage = loginResponse.getResultMessage();

				if (resultMessage == null && nonce != null && !nonce.isEmpty())
					responseDelegate.onSuccess(loginResponse, cookies);

				else
					responseDelegate.onFailure(resultMessage);
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				responseDelegate.onFailure(error.getMessage());
			}
		});

		requestQueue.add(request);
	}
}
