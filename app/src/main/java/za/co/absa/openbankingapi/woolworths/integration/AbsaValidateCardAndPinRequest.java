package za.co.absa.openbankingapi.woolworths.integration;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

import za.co.absa.openbankingapi.AsymmetricCryptoHelper;
import za.co.absa.openbankingapi.DecryptionFailureException;
import za.co.absa.openbankingapi.KeyGenerationFailureException;
import za.co.absa.openbankingapi.SessionKey;
import za.co.absa.openbankingapi.SymmetricCipher;
import za.co.absa.openbankingapi.woolworths.integration.dto.ValidateCardAndPinRequest;
import za.co.absa.openbankingapi.woolworths.integration.dto.ValidateCardAndPinResponse;
import za.co.absa.openbankingapi.woolworths.integration.service.IAbsaBankingOpenApiResponseListener;

public class AbsaValidateCardAndPinRequest {

	private SessionKey sessionKey;
	private RequestQueue requestQueue;

	public AbsaValidateCardAndPinRequest(final Context context){

		try {
			this.sessionKey = SessionKey.generate(context.getApplicationContext());
			this.requestQueue = Volley.newRequestQueue(context.getApplicationContext());
		} catch (KeyGenerationFailureException | AsymmetricCryptoHelper.AsymmetricEncryptionFailureException | AsymmetricCryptoHelper.AsymmetricKeyGenerationFailureException e) {
			e.printStackTrace();
		}
	}

	public void make(String cardToken, String cardPin, final IAbsaBankingOpenApiResponseListener<ValidateCardAndPinResponse> responseListener){
		Map<String, String> headers = new HashMap<>();
		headers.put("Content-Type", "application/json");
		headers.put("action", "validateCardAndPin");
		String encryptedPin = null;

		try {
			encryptedPin = SymmetricCipher.Aes256EncryptAndBase64Encode(cardPin, sessionKey.getKey());
		} catch (DecryptionFailureException e) {
			e.printStackTrace();
		}

		if (encryptedPin == null)
			throw new RuntimeException("This should never be null, check what issue occurred and resolve it. Also log this to Firebase.");

		final String gatewaySymmetricKey = this.sessionKey.getEncryptedKeyBase64Encoded();
		final String body = new ValidateCardAndPinRequest(cardToken, encryptedPin, gatewaySymmetricKey).getJson();

		requestQueue.add(new AbsaBankingOpenApiRequest<>(ValidateCardAndPinResponse.class, headers, body, new Response.Listener<ValidateCardAndPinResponse>(){

			@Override
			public void onResponse(ValidateCardAndPinResponse response) {
				//TODO: perform the appropriate checks
				//to ensure this is in fact the success
				if (true)
					responseListener.onSuccess(response);

				else
					responseListener.onFailure("Something clearly went wrong.");
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				responseListener.onFailure(error.getMessage());
			}
		}));
	}


}
