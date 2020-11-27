package za.co.absa.openbankingapi.woolworths.integration;

import android.content.Context;
import android.security.KeyPairGeneratorSpec;
import android.util.Base64;

import com.awfs.coordination.BuildConfig;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.util.FirebaseManager;

public class AbsaSecureCredentials {

	private String aliasId;
	private String deviceId;
	private KeyStore keyStore;

	private static final String RSA_MODE =  "RSA/ECB/PKCS1Padding";
	private final String AES_MODE = "AES/ECB/PKCS7Padding";

	public AbsaSecureCredentials(){
		super();

		initializeKeyStore();

		aliasId = AppInstanceObject.get().getCurrentUserObject().absaLoginAliasID;
		deviceId = AppInstanceObject.get().getCurrentUserObject().absaDeviceID;

		final Context context = WoolworthsApplication.getAppContext();

		if (aliasId == null)
			aliasId = "";
		else{
			try{
				byte[] base64DecodedAlias = Base64.decode(aliasId.getBytes(), Base64.DEFAULT);
				aliasId = new String(decrypt(context, base64DecodedAlias));
			}catch(Exception e){
				FirebaseManager.Companion.logException(e);
				aliasId = "";
			}
		}

		if (deviceId == null)
			deviceId = "";
		else{
			try{
				byte[] base64DecodedDeviceId = Base64.decode(deviceId.getBytes(), Base64.DEFAULT);
				deviceId = new String(decrypt(context, base64DecodedDeviceId));
			}catch(Exception e){
				FirebaseManager.Companion.logException(e);
				deviceId = "";
			}
		}
	}

	public String getAliasId() {
		return aliasId;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setAliasId(String aliasId) {
		this.aliasId = aliasId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public void save(){

		final Context context = WoolworthsApplication.getAppContext();
		try {
			saveAliasIdToSqlite(context);
			saveDeviceIdToSqlite(context);
		} catch (Exception e) {
			FirebaseManager.Companion.logException(e);
		}
	}

	private void saveAliasIdToSqlite(final Context context) throws Exception{
		if (this.aliasId == null || this.aliasId.isEmpty())
			return;

		final String encryptedAlias = encrypt(context, this.aliasId.getBytes());
		AppInstanceObject.User currentUserObject = AppInstanceObject.get().getCurrentUserObject();
		currentUserObject.absaLoginAliasID = encryptedAlias;
		currentUserObject.save();
	}

	private void saveDeviceIdToSqlite(final Context context) throws Exception{
		if (this.deviceId == null || this.deviceId.isEmpty())
			return;

		final String encryptedDeviceId = encrypt(context, this.deviceId.getBytes());
		AppInstanceObject.User currentUserObject = AppInstanceObject.get().getCurrentUserObject();
		currentUserObject.absaDeviceID = encryptedDeviceId;
		currentUserObject.save();
	}

	private void initializeKeyStore(){
		final Context context = WoolworthsApplication.getAppContext();
		try {
			keyStore = KeyStore.getInstance("AndroidKeyStore");
			keyStore.load(null);

			Enumeration<String> aliases = keyStore.aliases();

			// Create new key if needed
			if (!keyStore.containsAlias(BuildConfig.APPLICATION_ID)) {
				Calendar start = Calendar.getInstance();
				Calendar end = Calendar.getInstance();
				end.add(Calendar.YEAR, 1);

				KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
						.setAlias(BuildConfig.APPLICATION_ID)
						.setSubject(new X500Principal("CN=Woolworths OneApp, O=Woolworths Financial Services, C=South Africa"))
						.setSerialNumber(BigInteger.ONE)
						.setStartDate(start.getTime())
						.setEndDate(end.getTime())
						.build();

				KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
				generator.initialize(spec);

				KeyPair keyPair = generator.generateKeyPair();
			}


			SessionDao sessionDao = SessionDao.getByKey(SessionDao.KEY.ABSA_ENCRYPTION_KEY);
			String enryptedKeyB64 = sessionDao.value;
			if (enryptedKeyB64 == null) {
				byte[] key = new byte[16];
				SecureRandom secureRandom = new SecureRandom();
				secureRandom.nextBytes(key);
				byte[] encryptedKey = rsaEncrypt(key);
				enryptedKeyB64 = Base64.encodeToString(encryptedKey, Base64.DEFAULT);
				sessionDao.value = enryptedKeyB64;
				sessionDao.save();
			}

		} catch (Exception e) {
			FirebaseManager.Companion.logException(e);
		}
	}

	private byte[] rsaEncrypt(byte[] secret) throws Exception{
		KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(BuildConfig.APPLICATION_ID, null);
		// Encrypt the text
		Cipher inputCipher = Cipher.getInstance(RSA_MODE, "AndroidOpenSSL");
		inputCipher.init(Cipher.ENCRYPT_MODE, privateKeyEntry.getCertificate().getPublicKey());

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, inputCipher);
		cipherOutputStream.write(secret);
		cipherOutputStream.close();

		byte[] vals = outputStream.toByteArray();
		return vals;
	}

	private  byte[]  rsaDecrypt(byte[] encrypted) throws Exception {
		KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(BuildConfig.APPLICATION_ID, null);
		Cipher output = Cipher.getInstance(RSA_MODE);
		output.init(Cipher.DECRYPT_MODE, privateKeyEntry.getPrivateKey());
		CipherInputStream cipherInputStream = new CipherInputStream(
				new ByteArrayInputStream(encrypted), output);
		ArrayList<Byte> values = new ArrayList<>();
		int nextByte;
		while ((nextByte = cipherInputStream.read()) != -1) {
			values.add((byte)nextByte);
		}

		byte[] bytes = new byte[values.size()];
		for(int i = 0; i < bytes.length; i++) {
			bytes[i] = values.get(i).byteValue();
		}
		return bytes;
	}

	private Key getSecretKey(Context context) throws Exception{
		String enryptedKeyB64 = SessionDao.getByKey(SessionDao.KEY.ABSA_ENCRYPTION_KEY).value;
		// need to check null, omitted here
		byte[] encryptedKey = Base64.decode(enryptedKeyB64, Base64.DEFAULT);
		byte[] key = rsaDecrypt(encryptedKey);
		return new SecretKeySpec(key, "AES");
	}

	private String encrypt(Context context, byte[] input) throws Exception {
		Cipher c = Cipher.getInstance(AES_MODE, "BC");
		c.init(Cipher.ENCRYPT_MODE, getSecretKey(context));
		byte[] encodedBytes = c.doFinal(input);
		String encryptedBase64Encoded =  Base64.encodeToString(encodedBytes, Base64.DEFAULT);
		return encryptedBase64Encoded;
	}

	private byte[] decrypt(Context context, byte[] encrypted) throws Exception {
		Cipher c = Cipher.getInstance(AES_MODE, "BC");
		c.init(Cipher.DECRYPT_MODE, getSecretKey(context));
		byte[] decodedBytes = c.doFinal(encrypted);
		return decodedBytes;
	}
}
