package za.co.woolworths.financial.services.android.models;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import za.co.woolworths.financial.services.android.models.dao.BaseDao;
import za.co.woolworths.financial.services.android.util.DatabaseHelper;

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

    private Gson gson;

    public ApiResponseDao(Context mContext) {
        super(mContext);

        this.gson = new GsonBuilder().create();
    }

    public ApiResponseDao getByApiRequestId(String apiRequestId) {
        String query = "SELECT * FROM ApiResponse WHERE apiRequestId=? AND responseHandler=? ORDER BY id ASC LIMIT 1;";
        try {
            Map<String, String> result = DatabaseHelper.getInstance(mContext).executeReturnableQuery(query, new String[]{
                    apiRequestId, "1"
            });
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        return this;
    }

    public int save(){
        //ApiResponse will never be updated, only new records will be inserted.
        String query = "INSERT INTO ApiResponse (apiRequestId, responseHandler, code, message, contentType, headers, body) VALUES (?, ?, ?, ?, ?, ?, ?);";
        Map<String, String> result = new HashMap<>();
        try {
            result = DatabaseHelper.getInstance(mContext).executeReturnableQuery(query, new String[]{
                    this.apiRequestId, this.responseHandler, "" + this.code, this.message, this.contentType, this.headers, this.body
            });
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        for(Map.Entry<String, String> entry: result.entrySet()){

            if(entry.getKey().equals("id")){
                return Integer.valueOf(entry.getValue());
            }
        }

        return -1;
    }
}
