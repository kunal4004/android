package za.co.woolworths.financial.services.android.models.dao;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import za.co.woolworths.financial.services.android.util.PersistenceLayer;
import za.co.woolworths.financial.services.android.util.Utils;

/**
 * Created by eesajacobs on 2016/12/29.
 */

public class ApiRequestDao extends BaseDao {
    public static final String TAG = "ApiRequestDao";
    public static final byte[] SYMMETRIC_KEY = "K7MZpM6owN0VIjRbwfN3Xw==".getBytes();

    public String requestType = "";
    private String endpoint = "";
    private String headers;
    private String parameters;
    private String dateExpires = "";

    private final long cacheTime;

    public ApiRequestDao(long cacheTime) {
        super();

        this.cacheTime = cacheTime;
    }

    @Override
    public String getTableName() {
        return "ApiRequest";
    }

    public ApiRequestDao get(String _requestType, String _endpoint, String _headers, String _parameters) {

        String query = "SELECT * FROM ApiRequest WHERE endpoint=? AND requestType=? AND headers=? AND parameters=? AND dateExpires > datetime() ORDER BY id DESC LIMIT 1;";
        Map<String, String> result = new HashMap<>();
        try {
            String headersEncrypted = Utils.encryptCipher(_headers);
            String parametersEncrypted = Utils.encryptCipher(_parameters);
            String endpointEncrypted = Utils.encryptCipher(_endpoint);
            String requestTypeEncrypted = Utils.encryptCipher("" + _requestType);

            result = PersistenceLayer.getInstance().executeReturnableQuery(query, new String[]{
                    endpointEncrypted, requestTypeEncrypted, headersEncrypted, parametersEncrypted
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

            switch (entry.getKey()) {
                case "id":
                    this.id = entry.getValue();
                    break;
                case "endpoint":
                    try {
                    this.endpoint = Utils.decryptCipher(entry.getValue());
                    } catch (Exception e) {
                        Log.d(TAG, e.getMessage());
                        this.endpoint = _endpoint;
                    }
                    break;
                case "requestType":
                    try {
                        this.requestType = Utils.decryptCipher(entry.getValue());
                    } catch (Exception e) {
                        Log.d(TAG, e.getMessage());
                        this.requestType = _requestType;
                    }
                    break;
                case "headers":
                    try {
                        this.headers = Utils.decryptCipher(entry.getValue());
                    } catch (Exception e) {
                        Log.d(TAG, e.getMessage());
                        this.headers = _headers;
                    }
                    break;
                case "parameters":
                    try {
                        this.parameters = Utils.decryptCipher(entry.getValue());
                    } catch (Exception e) {
                        Log.d(TAG, e.getMessage());
                        this.parameters = _parameters;
                    }
                    break;
            }
        }
        return this;
    }

    public void save() {
        //Delete expired Cache data
        PersistenceLayer persistenceLayer = PersistenceLayer.getInstance();
        try {
           persistenceLayer.executeDeleteQuery("DELETE FROM ApiResponse where apiRequestId in ( SELECT id FROM ApiRequest WHERE dateExpires < CURRENT_TIMESTAMP);");
           persistenceLayer.executeDeleteQuery("DELETE FROM ApiRequest WHERE dateExpires < CURRENT_TIMESTAMP;");
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        //ApiRequest will never be updated, only new records will be inserted.
        try {
            this.dateExpires = persistenceLayer.executeReturnableQuery("SELECT DATETIME(datetime(), '+" + this.cacheTime + " seconds') as cacheTime", new String[]{}).get("cacheTime");

            String headersEncrypted = Utils.encryptCipher(this.headers);
            String parametersEncrypted = Utils.encryptCipher(this.parameters);
            String endpointsEncrypted = Utils.encryptCipher(this.endpoint);
            String requestTypeEncrypted = Utils.encryptCipher("" + this.requestType);

            Map<String, String> arguments = new HashMap<>();
            arguments.put("endpoint", endpointsEncrypted);
            arguments.put("requestType",requestTypeEncrypted);
            arguments.put("headers", headersEncrypted);
            arguments.put("parameters", parametersEncrypted);
            arguments.put("dateExpires", dateExpires);

            long rowId = persistenceLayer.executeInsertQuery(getTableName(), arguments);
            this.id = "" + rowId;

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }
}
class CacheEmptyException extends Exception {
    public CacheEmptyException(String message) {
        super(message);
    }
}