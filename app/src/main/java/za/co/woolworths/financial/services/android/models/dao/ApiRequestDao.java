package za.co.woolworths.financial.services.android.models.dao;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;

import za.co.woolworths.financial.services.android.util.PersistenceLayer;

/**
 * Created by eesajacobs on 2016/12/29.
 */

public class ApiRequestDao extends BaseDao {
    public static final String TAG = "ApiRequestDao";

    String requestType = "";
    String endpoint = "";
    String headers;
    String parameters;
    String dateExpires = "";

    private Gson gson;
    private final long cacheTime;

    public ApiRequestDao(Context mContext, long cacheTime) {
        super(mContext);

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
            result = PersistenceLayer.getInstance(mContext).executeReturnableQuery(query, new String[]{
                    _endpoint, ("" + _requestType), _headers, _parameters
            });
        } catch (Exception e) {
            //record does not exist.
            this.endpoint = _endpoint;
            this.requestType = _requestType;
            this.headers = _headers;
            this.parameters = _parameters;

            Log.e(TAG, e.getMessage());
        }

        for (Map.Entry<String, String> entry : result.entrySet()) {

            if (entry.getKey().equals("id")) {
                this.id = entry.getValue();
            } else if (entry.getKey().equals("endpoint")) {
                this.endpoint = entry.getValue();
            } else if (entry.getKey().equals("headers")) {
                this.headers = entry.getValue();
            } else if (entry.getKey().equals("parameters")) {
                this.parameters = entry.getValue();
            }
        }
        return this;
    }

    public void save() {
        //Delete expired Cache data
        try {
            PersistenceLayer.getInstance(mContext).executeDeleteQuery("delete from ApiResponse where apiRequestId in ( select id from apiRequest where dateExpires < CURRENT_TIMESTAMP)");
            PersistenceLayer.getInstance(mContext).executeDeleteQuery("delete from ApiRequest where dateExpires < CURRENT_TIMESTAMP");
        }catch (Exception e)
        {
            Log.e(TAG,e.getMessage());
        }
        //ApiRequest will never be updated, only new records will be inserted.
        try {
            this.dateExpires = PersistenceLayer.getInstance(mContext).executeReturnableQuery("SELECT DATETIME(datetime(), '+" + this.cacheTime + " seconds') as cacheTime",
                    new String[]{}).get("cacheTime");

            Log.d(TAG, dateExpires);

            Map<String, String> arguments = new HashMap<>();
            arguments.put("endpoint", this.endpoint);
            arguments.put("requestType", "" + this.requestType);
            arguments.put("headers", this.headers);
            arguments.put("parameters", this.parameters);
            arguments.put("dateExpires", this.dateExpires);

            long rowid = PersistenceLayer.getInstance(mContext).executeInsertQuery(getTableName(), arguments);
            this.id = "" + rowid;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }
}
