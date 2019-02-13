package za.co.absa.openbankingapi.woolworths.integration;

import za.co.absa.openbankingapi.SessionKey;

public class AbsaLoginRequest {

	private final SessionKey sessionKey;

	public AbsaLoginRequest(SessionKey sessionKey){
		this.sessionKey = sessionKey;
	}

	public void make(final String userPin, final String aliasId, final String deviceId, final String cookie){
		/*let headers = ["Content-Type": "application/x-www-form-urlencoded"]

		let encryptedUserPin = SymmetricCipher.aes256EncryptString(userPin, withKey: symmetricKey)!
				let encryptedAliasId = SymmetricCipher.aes256EncryptString(aliasId, withKey: symmetricKey)!

				let request = Request(credential: encryptedUserPin, aliasId: encryptedAliasId, deviceId: deviceId, symmetricKey: String(sessionKey.encryptedKey))
		let body = request.getUrlEncodedFormData()*/


		//Object encryptedUserPin = SymmetricCipher.Aes256Encrypt(symmetricKey, "");
	}
}
