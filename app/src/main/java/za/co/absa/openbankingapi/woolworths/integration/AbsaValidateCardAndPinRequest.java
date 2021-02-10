package za.co.absa.openbankingapi.woolworths.integration;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.net.HttpCookie;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import za.co.absa.openbankingapi.AsymmetricCryptoHelper;
import za.co.absa.openbankingapi.DecryptionFailureException;
import za.co.absa.openbankingapi.KeyGenerationFailureException;
import za.co.absa.openbankingapi.SessionKey;
import za.co.absa.openbankingapi.SymmetricCipher;
import za.co.absa.openbankingapi.woolworths.integration.dto.Header;
import za.co.absa.openbankingapi.woolworths.integration.dto.ValidateCardAndPinRequest;
import za.co.absa.openbankingapi.woolworths.integration.dto.ValidateCardAndPinResponse;
import za.co.absa.openbankingapi.woolworths.integration.service.AbsaBankingOpenApiRequest;
import za.co.absa.openbankingapi.woolworths.integration.service.AbsaBankingOpenApiResponse;
import za.co.woolworths.financial.services.android.util.FirebaseManager;

public class AbsaValidateCardAndPinRequest {

	private SessionKey sessionKey;

	public AbsaValidateCardAndPinRequest(){

		try {
			this.sessionKey = SessionKey.generate();
		} catch (KeyGenerationFailureException | AsymmetricCryptoHelper.AsymmetricEncryptionFailureException | AsymmetricCryptoHelper.AsymmetricKeyGenerationFailureException e) {
			FirebaseManager.Companion.logException(e);
		}
	}

	public void make(String cardToken, String cardPin, final AbsaBankingOpenApiResponse.ResponseDelegate<ValidateCardAndPinResponse> responseDelegate){
		Map<String, String> headers = new HashMap<>();
		headers.put("Accept", "application/json");
		headers.put("action", "validateCardAndPin");
		String encryptedPin = null;

		try {
			encryptedPin = SymmetricCipher.Aes256EncryptAndBase64Encode(cardPin, sessionKey.getKey(), sessionKey.getIV());
		} catch (DecryptionFailureException e) {
			FirebaseManager.Companion.logException(e);
		}

		if (encryptedPin == null)
			throw new RuntimeException("This should never be null, check what issue occurred and resolve it. Also log this to Firebase.");

		final String gatewaySymmetricKey = this.sessionKey.getEncryptedKeyBase64Encoded();
		final String body = new ValidateCardAndPinRequest(cardToken, encryptedPin, gatewaySymmetricKey, sessionKey.getEncryptedIVBase64Encoded()).getJson();

		new AbsaBankingOpenApiRequest<>(ValidateCardAndPinResponse.class, headers, body, true, new AbsaBankingOpenApiResponse.Listener<ValidateCardAndPinResponse>(){

			@Override
			public void onResponse(ValidateCardAndPinResponse response, List<HttpCookie> cookies) {
				if (response == null || response.getHeader() == null) return;
				Header.ResultMessage[] resultMessages = response.getHeader().getResultMessages();
				String statusCode = "0";
				try {
					statusCode = response.getHeader().getStatusCode();
				} catch (Exception e) {
					FirebaseManager.Companion.logException(e);
				}
				if (resultMessages == null || resultMessages.length == 0 && statusCode.equalsIgnoreCase("0")) {
					responseDelegate.onSuccess(response, cookies);
				} else {
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
