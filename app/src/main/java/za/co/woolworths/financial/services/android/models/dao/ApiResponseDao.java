package za.co.woolworths.financial.services.android.models.dao;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import za.co.woolworths.financial.services.android.util.PersistenceLayer;
import za.co.woolworths.financial.services.android.util.Utils;

/**
 * Created by eesajacobs on 2016/12/29.
 */
public class ApiResponseDao extends BaseDao {
    public static final String TAG = "ApiResponseDao";

    public String apiRequestId;
    public String responseHandler;
    public int code;
    public String message;
    public String body;
    public String headers;
    public String contentType;

    public ApiResponseDao() {
        super();
    }

    @Override
    public String getTableName() {
        return "ApiResponse";
    }

    public ApiResponseDao getByApiRequestId(String apiRequestId) {
        String query = "SELECT * FROM ApiResponse WHERE apiRequestId=? AND code=? ORDER BY id DESC LIMIT 1;";
        Map<String, String> result = new HashMap<>();
        try {
            result = PersistenceLayer.getInstance().executeReturnableQuery(query, new String[]{
                    apiRequestId, "200"
            });
        } catch (Exception e) {
            //record does not exist.
            //this.endpoint = _endpoint;
            //this.requestType = _requestType;
            //this.headers = _headers;
            //this.parameters = _parameters;

            Log.e(TAG, e.getMessage());
        }
        for (Map.Entry<String, String> entry : result.entrySet()) {

            if (entry.getKey().equals("id")) {
                this.id = entry.getValue();
            } else if (entry.getKey().equals("apiRequestId")) {
                this.apiRequestId = entry.getValue();
            } else if (entry.getKey().equals("responseHandler")) {
                this.responseHandler = entry.getValue();
            } else if (entry.getKey().equals("code")) {
                this.code = Integer.parseInt(entry.getValue());
            } else if (entry.getKey().equals("message")) {
                this.message = entry.getValue();
            } else if (entry.getKey().equals("body")) {
                try {
                    this.body = Utils.aes256DecryptBase64EncryptedString(entry.getValue());
                } catch (Exception e) {
                    Log.d(TAG, e.getMessage());
                }
            } else if (entry.getKey().equals("headers")) {
                try {
                    this.headers = Utils.aes256DecryptBase64EncryptedString(entry.getValue());
                } catch (Exception e) {
                    Log.d(TAG, e.getMessage());
                }
            } else if (entry.getKey().equals("contentType")) {
                this.contentType = entry.getValue();
            }
        }

        return this;
    }

    public void save() {
        //ApiRequest will never be updated, only new records will be inserted.
        try {

            PersistenceLayer persistenceLayer = PersistenceLayer.getInstance();

            String headersEncrypted = Utils.aes256EncryptStringAsBase64String(this.headers);
            String bodyEncrypted = Utils.aes256EncryptStringAsBase64String(this.body);

            Map<String, String> arguments = new HashMap<>();
            arguments.put("apiRequestId", this.apiRequestId);
            arguments.put("responseHandler", "-1");
            arguments.put("code", "" + this.code);
            arguments.put("message", this.message);
            arguments.put("contentType", this.contentType);
            arguments.put("headers", headersEncrypted);
            arguments.put("body", bodyEncrypted);

            long rowId = persistenceLayer.executeInsertQuery(getTableName(), arguments);
            this.id = "" + rowId;

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }
}
