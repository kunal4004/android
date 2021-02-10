package za.co.absa.openbankingapi.woolworths.integration;

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
import za.co.absa.openbankingapi.DecryptionFailureException;
import za.co.absa.openbankingapi.KeyGenerationFailureException;
import za.co.absa.openbankingapi.SessionKey;
import za.co.absa.openbankingapi.SymmetricCipher;
import za.co.absa.openbankingapi.woolworths.integration.dao.JSession;
import za.co.absa.openbankingapi.woolworths.integration.dto.LoginRequest;
import za.co.absa.openbankingapi.woolworths.integration.dto.LoginResponse;
import za.co.absa.openbankingapi.woolworths.integration.service.AbsaBankingOpenApiRequest;
import za.co.absa.openbankingapi.woolworths.integration.service.AbsaBankingOpenApiResponse;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.util.FirebaseManager;

public class AbsaLoginRequest {

	private SessionKey sessionKey;
	public static JSession jsessionCookie;
	public static JSession xfpt;
	public static JSession wfpt;

	public AbsaLoginRequest(){

		try {
			this.sessionKey = SessionKey.generate();
		} catch (KeyGenerationFailureException | AsymmetricCryptoHelper.AsymmetricEncryptionFailureException | AsymmetricCryptoHelper.AsymmetricKeyGenerationFailureException e) {
			FirebaseManager.Companion.logException(e);
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
			FirebaseManager.Companion.logException(e);
			throw new RuntimeException(e);
		}

		String body = null;
		try{
			body = new LoginRequest(encryptedAlias, deviceId, base64EncodedEncryptedDerivedKey, gatewaySymmetricKey, sessionKey.getEncryptedIVBase64Encoded()).getUrlEncodedFormData();
		} catch (UnsupportedEncodingException e) {
			FirebaseManager.Companion.logException(e);
		}

		new AbsaBankingOpenApiRequest<>(WoolworthsApplication.getAbsaBankingOpenApiServices().getBaseURL() + "/wcob/j_pin_security_login", LoginResponse.class, headers, body, true, new AbsaBankingOpenApiResponse.Listener<LoginResponse>() {

			@Override
			public void onResponse(LoginResponse loginResponse, List<HttpCookie> cookies) {
				final String nonce = loginResponse.getNonce();
				final String resultMessage = loginResponse.getResultMessage();
				String statusCode = "0";
				try {
					statusCode = loginResponse.getHeader().getStatusCode();
				} catch (Exception e) {
					FirebaseManager.Companion.logException(e);
				}

				if (resultMessage == null && nonce != null && !nonce.isEmpty() && cookies != null && statusCode.equalsIgnoreCase("0")) {
					for (HttpCookie cookie : cookies) {
						if (cookie.getName().equalsIgnoreCase("jsessionid"))
							jsessionCookie = new JSession(cookie.getName(), cookie);
						else if (cookie.getName().equalsIgnoreCase("wfpt"))
							wfpt = new JSession(cookie.getName(), cookie);
						else if (cookie.getName().equalsIgnoreCase("xfpt"))
							xfpt = new JSession(cookie.getName(), cookie);
					}
					responseDelegate.onSuccess(loginResponse, cookies);
					//clearing up sensitive values
					sessionKey = null;
				}

				else {
					responseDelegate.onFailure(resultMessage);
					//clearing up sensitive values
					sessionKey = null;
				}
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				responseDelegate.onFatalError(error);
				//clearing up sensitive values
				sessionKey = null;
			}
		});

	}
}
