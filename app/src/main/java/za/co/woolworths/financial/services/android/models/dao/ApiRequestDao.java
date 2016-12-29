package za.co.woolworths.financial.services.android.models.dao;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import za.co.woolworths.financial.services.android.util.DatabaseHelper;

/**
 * Created by eesajacobs on 2016/12/29.
 */

public class ApiRequestDao extends BaseDao {
    public static final String TAG = "ApiRequestDao";

    int requestType = 0;
    String endpoint = "";
    String headers;
    String parameters;
    String dateExpires = "";

    private Gson gson;

    public ApiRequestDao(Context mContext) {
        super(mContext);

        this.gson = new GsonBuilder().create();
    }

    @Override
    public String getTableName() {
        return "ApiRequest";
    }

    public ApiRequestDao get(int _requestType, String _endpoint, String _headers, String _parameters) {
        //String headersJson = (headers == null ? "{}" : this.gson.toJson(headers));
        //String parametersJson = (parameters == null ? "{}" : this.gson.toJson(parameters));

        //Log.d(TAG, headers);
        //Log.d(TAG, parameters);

        String query = "SELECT * FROM ApiRequest WHERE endpoint=? AND requestType=? AND headers=? AND parameters=? AND dateExpires > datetime() ORDER BY id ASC LIMIT 1;";
        Map<String, String> result = new HashMap<>();
        try {
            result = DatabaseHelper.getInstance(mContext).executeReturnableQuery(query, new String[]{
                    _endpoint, ("" + _requestType), _headers, _parameters
            });
        } catch (IOException e) {
            //record does not exist.
            this.endpoint = _endpoint;
            this.requestType = _requestType;
            this.headers = _headers;
            this.parameters = _parameters;

            Log.e(TAG, e.getMessage());
        }

        for(Map.Entry<String, String> entry: result.entrySet()){

            if(entry.getKey().equals("id")){
                this.id = entry.getValue();
            }
            else if(entry.getKey().equals("endpoint")){
                this.endpoint = entry.getValue();
            }
            else if(entry.getKey().equals("headers")){
                this.headers = entry.getValue();
            }
            else if(entry.getKey().equals("parameters")){
                this.parameters = entry.getValue();
            }
        }
        return this;
    }

    public int save(){
        //ApiRequest will never be updated, only new records will be inserted.
        String query = "INSERT INTO ApiRequest (endpoint, requestType, headers, parameters, dateExpires) VALUES (?, ?, ?, ?, ?);";
        Map<String, String> result = new HashMap<>();
        try {
            result = DatabaseHelper.getInstance(mContext).executeReturnableQuery(query, new String[]{
                    this.endpoint, "" + this.requestType, this.headers, this.parameters, this.dateExpires
            });
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        for(Map.Entry<String, String> entry: result.entrySet()){

            if(entry.getKey().equals("id")){
                this.id = entry.getValue();
                return Integer.valueOf(entry.getValue());
            }
        }

        return -1;
    }
}
