package za.co.absa.openbankingapi.woolworths.integration;

import android.content.Context;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

import za.co.absa.openbankingapi.AsymmetricCryptoHelper;
import za.co.absa.openbankingapi.KeyGenerationFailureException;
import za.co.absa.openbankingapi.SessionKey;
import za.co.absa.openbankingapi.woolworths.integration.dto.ValidateCardAndPinResponse;

public class AbsaValidateCardAndPinRequest {

	private SessionKey sessionKey;
	private RequestQueue requestQueue;

	public AbsaValidateCardAndPinRequest(final Context context){

		try {
			this.sessionKey = SessionKey.generate(context.getApplicationContext());
			this.requestQueue = Volley.newRequestQueue(context.getApplicationContext());
		} catch (KeyGenerationFailureException e) {
			e.printStackTrace();
		} catch (AsymmetricCryptoHelper.AsymmetricEncryptionFailureException e) {
			e.printStackTrace();
		} catch (AsymmetricCryptoHelper.AsymmetricKeyGenerationFailureException e) {
			e.printStackTrace();
		}
	}

	public void make(){

		Map<String, String> headers = new HashMap<>();
		headers.put("action", "validateCardAndPin");

		Response.Listener<ValidateCardAndPinResponse> responseListener = new Response.Listener<ValidateCardAndPinResponse>(){

			@Override
			public void onResponse(ValidateCardAndPinResponse response) {

				Log.d("", "");

			}
		};

		Response.ErrorListener errorListener = new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {

				Log.e("", "");
			}
		};

		requestQueue.add(new AbsaBankingOpenApiRequest<>(ValidateCardAndPinResponse.class, headers, responseListener, errorListener));
	}
}
