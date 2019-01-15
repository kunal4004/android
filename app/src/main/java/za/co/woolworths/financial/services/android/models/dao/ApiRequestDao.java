package za.co.woolworths.financial.services.android.models.dao;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import za.co.woolworths.financial.services.android.util.PersistenceLayer;
import za.co.woolworths.financial.services.android.util.encryption.SymmetricCipher;

/**
 * Created by eesajacobs on 2016/12/29.
 */

public class ApiRequestDao extends BaseDao {
    public static final String TAG = "ApiRequestDao";
    public static final byte[] SYMMETRIC_KEY = "K7MZpM6owN0VIjRbwfN3Xw==".getBytes();

    String requestType = "";
    String endpoint = "";
    String headers;
    String parameters;
    String dateExpires = "";

    private Gson gson;
    private final long cacheTime;

    public ApiRequestDao(long cacheTime) {
        super();

        this.gson = new GsonBuilder().create();
        this.cacheTime = cacheTime;
    }

    @Override
    public String getTableName() {
        return "ApiRequest";
    }

    public ApiRequestDao get(String _requestType, String _endpoint, String _headers, String _parameters) {

        String query = "SELECT * FROM ApiRequest WHERE endpoint=? AND requestType=? AND headers=? AND parameters=? AND dateExpires > datetime() ORDER BY id ASC LIMIT 1;";
        Map<String, String> result = new HashMap<>();
        try {
            String headersEncrypted = new String(SymmetricCipher.Aes256Encrypt(SYMMETRIC_KEY, _headers), StandardCharsets.ISO_8859_1);
            String parametersEncrypted = new String(SymmetricCipher.Aes256Encrypt(SYMMETRIC_KEY, _parameters), StandardCharsets.ISO_8859_1);

            result = PersistenceLayer.getInstance().executeReturnableQuery(query, new String[]{
                    _endpoint, ("" + _requestType), headersEncrypted, parametersEncrypted
            });

            if (result.size() == 0)
                //throw an exception so that empty
                //cache is handled i.e. new record is created.
                throw new CacheEmptyException("One or more ApiRequest(s) matching your parameters do not exist.");


        } catch (Exception e) {
            //record does not exist.
            this.endpoint = _endpoint;
            this.requestType = _requestType;
            this.headers = _headers;
            this.parameters = _parameters;

            Log.d(TAG, e.getMessage());
        }

        for (Map.Entry<String, String> entry : result.entrySet()) {

            if (entry.getKey().equals("id")) {
                this.id = entry.getValue();
            } else if (entry.getKey().equals("endpoint")) {
                this.endpoint = entry.getValue();
            } else if (entry.getKey().equals("headers")) {
                try {
                    String headersDecrypted = new String(SymmetricCipher.Aes256Decrypt(SYMMETRIC_KEY, entry.getValue().getBytes("ISO-8859-1")), "ISO-8859-1");
                    this.headers = headersDecrypted;
                } catch (Exception e) {
                    Log.d(TAG, e.getMessage());
                    this.headers = _headers;
                }
            } else if (entry.getKey().equals("parameters")) {
                try {
                    String parametersDecrypted = new String(SymmetricCipher.Aes256Decrypt(SYMMETRIC_KEY, entry.getValue().getBytes("ISO-8859-1")), "ISO-8859-1");
                    this.parameters = parametersDecrypted;
                } catch (Exception e) {
                    Log.d(TAG, e.getMessage());
                    this.parameters = _parameters;
                }
            }
        }
        return this;
    }

    public void save() {
        //Delete expired Cache data
        try {
            PersistenceLayer.getInstance().executeDeleteQuery("DELETE FROM ApiResponse where apiRequestId in ( SELECT id FROM ApiRequest WHERE dateExpires < CURRENT_TIMESTAMP);");
            PersistenceLayer.getInstance().executeDeleteQuery("DELETE FROM ApiRequest WHERE dateExpires < CURRENT_TIMESTAMP;");
        }catch (Exception e)
        {
            Log.e(TAG,e.getMessage());
        }
        //ApiRequest will never be updated, only new records will be inserted.
        try {
            this.dateExpires = PersistenceLayer.getInstance().executeReturnableQuery("SELECT DATETIME(datetime(), '+" + this.cacheTime + " seconds') as cacheTime",
                    new String[]{}).get("cacheTime");

            Log.d(TAG, dateExpires);

            String headersEncrypted = new String(SymmetricCipher.Aes256Encrypt(SYMMETRIC_KEY, this.headers), StandardCharsets.ISO_8859_1);
            String parametersEncrypted = new String(SymmetricCipher.Aes256Encrypt(SYMMETRIC_KEY, this.parameters), StandardCharsets.ISO_8859_1);

            Map<String, String> arguments = new HashMap<>();
            arguments.put("endpoint", this.endpoint);
            arguments.put("requestType", "" + this.requestType);
            arguments.put("headers", headersEncrypted);
            arguments.put("parameters", parametersEncrypted);
            arguments.put("dateExpires", this.dateExpires);

            long rowid = PersistenceLayer.getInstance().executeInsertQuery(getTableName(), arguments);
            this.id = "" + rowid;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public byte[] stringToBytesASCII(String str) {
        byte[] b = new byte[str.length()];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) str.charAt(i);
        }
        return b;
    }
}

class CacheEmptyException extends Exception {
    public CacheEmptyException(String message) {
        super(message);
    }
}