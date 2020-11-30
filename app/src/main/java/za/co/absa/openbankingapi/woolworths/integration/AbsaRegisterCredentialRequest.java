package za.co.absa.openbankingapi.woolworths.integration;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import za.co.absa.openbankingapi.AsymmetricCryptoHelper;
import za.co.absa.openbankingapi.Cryptography;
import za.co.absa.openbankingapi.DecryptionFailureException;
import za.co.absa.openbankingapi.KeyGenerationFailureException;
import za.co.absa.openbankingapi.SessionKey;
import za.co.absa.openbankingapi.SymmetricCipher;
import za.co.absa.openbankingapi.woolworths.integration.dto.Header;
import za.co.absa.openbankingapi.woolworths.integration.dto.RegisterCredentialRequest;
import za.co.absa.openbankingapi.woolworths.integration.dto.RegisterCredentialResponse;
import za.co.absa.openbankingapi.woolworths.integration.service.AbsaBankingOpenApiRequest;
import za.co.absa.openbankingapi.woolworths.integration.service.AbsaBankingOpenApiResponse;
import za.co.woolworths.financial.services.android.util.FirebaseManager;
import za.co.woolworths.financial.services.android.util.Utils;

public class AbsaRegisterCredentialRequest {

	private SessionKey sessionKey;
	private String deviceId;

	public AbsaRegisterCredentialRequest(){

		try {
			this.sessionKey = SessionKey.generate();
			this.deviceId = Utils.getAbsaUniqueDeviceID();
		} catch (KeyGenerationFailureException | AsymmetricCryptoHelper.AsymmetricEncryptionFailureException | AsymmetricCryptoHelper.AsymmetricKeyGenerationFailureException e) {
			FirebaseManager.Companion.logException(e);
		}
	}

	public void make(final String aliasId, final String passcode, final AbsaBankingOpenApiResponse.ResponseDelegate<RegisterCredentialResponse> responseDelegate){
		Map<String, String> headers = new HashMap<>();
		headers.put("Accept", "application/json");
		headers.put("action", "registerCredential");

		final byte[] symmetricKey = sessionKey.getKey();
		final String gatewaySymmetricKey = this.sessionKey.getEncryptedKeyBase64Encoded();
		String encryptedAlias;
		RegisterCredentialRequest.CredentialVO[] credentialVOs = new RegisterCredentialRequest.CredentialVO[1];

		try{
			encryptedAlias = SymmetricCipher.Aes256EncryptAndBase64Encode(aliasId, symmetricKey, sessionKey.getIV());

			byte[] derivedKey = Cryptography.PasswordBasedKeyDerivationFunction2(aliasId.concat(passcode), deviceId, 1000, 256);
			byte[] encryptedDerivedKey = SymmetricCipher.Aes256Encrypt(sessionKey.getKey(), derivedKey, sessionKey.getIV());
			String base64EncodedEncryptedDerivedKey = Base64.encodeToString(encryptedDerivedKey, Base64.NO_WRAP);

			 credentialVOs[0] = new RegisterCredentialRequest.CredentialVO(encryptedAlias, "MOBILEAPP_5DIGIT_PIN", base64EncodedEncryptedDerivedKey);
		} catch (DecryptionFailureException | UnsupportedEncodingException | KeyGenerationFailureException e) {
			FirebaseManager.Companion.logException(e);
			throw new RuntimeException(e);
		}


		final String body = new RegisterCredentialRequest(encryptedAlias, deviceId, credentialVOs, gatewaySymmetricKey, sessionKey.getEncryptedIVBase64Encoded()).getJson();

		new AbsaBankingOpenApiRequest<>(RegisterCredentialResponse.class, headers, body, true, (response, cookies) -> {
			Header.ResultMessage[] resultMessages = response.getHeader().getResultMessages();

			String statusCode = "0";
			try {
				statusCode = response.getHeader().getStatusCode();
			} catch (Exception e) {
				FirebaseManager.Companion.logException(e);
			}

			if (resultMessages == null || resultMessages.length == 0 && statusCode.equalsIgnoreCase("0")){
				responseDelegate.onSuccess(response, cookies);
			}

			else{
				responseDelegate.onFailure(resultMessages[0].getResponseMessage());
			}
		}, responseDelegate::onFatalError);

		}
}
