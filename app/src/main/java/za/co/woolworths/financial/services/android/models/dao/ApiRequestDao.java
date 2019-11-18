package za.co.woolworths.financial.services.android.models.dao;

import android.util.Log;

import com.awfs.coordination.BuildConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import za.co.absa.openbankingapi.DecryptionFailureException;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
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
    private String appVersion;

    private final long cacheTime;

    public ApiRequestDao(long cacheTime) {
        super();

        this.updateTableSchemaIfNeeded();
        this.cacheTime = cacheTime;
    }

    @Override
    public String getTableName() {
        return "ApiRequest";
    }

    public ApiRequestDao get(String _requestType, String _endpoint, String _headers, String _parameters) {

        final String appVersion = BuildConfig.VERSION_NAME.concat(".").concat(String.valueOf(BuildConfig.VERSION_CODE));

        String query = "SELECT * FROM ApiRequest WHERE endpoint=? AND requestType=? AND headers=? AND parameters=? AND dateExpires > datetime() AND appVersion=? ORDER BY id DESC LIMIT 1;";
        Map<String, String> result = new HashMap<>();
        try {
            String headersEncrypted = Utils.aes256EncryptStringAsBase64String(_headers);
            String parametersEncrypted = Utils.aes256EncryptStringAsBase64String(_parameters);
            String endpointEncrypted = Utils.aes256EncryptStringAsBase64String(_endpoint);
            String requestTypeEncrypted = Utils.aes256EncryptStringAsBase64String("" + _requestType);

            result = PersistenceLayer.getInstance().executeReturnableQuery(query, new String[]{
                    endpointEncrypted, requestTypeEncrypted, headersEncrypted, parametersEncrypted, appVersion
            });

            if (result.size() == 0)
                //throw an exception so that empty
                //cache is handled i.e. new record is created.
                throw new CacheEmptyException("One or more ApiRequest(s) matching your parameters do not exist.");


        } catch (CacheEmptyException e) {
            //record does not exist.
            this.endpoint = _endpoint;
            this.requestType = _requestType;
            this.headers = _headers;
            this.parameters = _parameters;
            this.appVersion = appVersion;
        } catch (DecryptionFailureException e) {
            throw new RuntimeException(e);
        }

        for (Map.Entry<String, String> entry : result.entrySet()) {

            final String key = entry.getKey();
            String value;

            if (key.equals("endpoint") || key.equals("requestType") || key.equals("headers") || key.equals("parameters")){
                try {
                    value = Utils.aes256DecryptBase64EncryptedString(entry.getValue());
                } catch (DecryptionFailureException e) {
                    throw new RuntimeException(e);
                }
            }else{
                value = entry.getValue();
            }

            switch (entry.getKey()) {
                case "id":
                    this.id = value;
                    break;
                case "endpoint":
                    this.endpoint = value;
                    break;
                case "requestType":
                    this.requestType = value;
                    break;
                case "headers":
                    this.headers = value;
                    break;
                case "parameters":
                    this.parameters = value;
                    break;
                case "appVersion":
                    this.appVersion = value;
                    break;
            }
        }
        return this;
    }

    public void save() {
        //Delete expired Cache data
        final PersistenceLayer persistenceLayer = PersistenceLayer.getInstance();

        List<String> deleteQueries = new ArrayList<String>();
        deleteQueries.add("DELETE FROM ApiResponse where apiRequestId in ( SELECT id FROM ApiRequest WHERE dateExpires < CURRENT_TIMESTAMP);");
        deleteQueries.add("DELETE FROM ApiRequest WHERE dateExpires < CURRENT_TIMESTAMP;");
        persistenceLayer.executeSQLStatements(deleteQueries);

        dateExpires = persistenceLayer.executeReturnableQuery("SELECT DATETIME(datetime(), '+" + this.cacheTime + " seconds') as cacheTime", new String[]{}).get("cacheTime");

        Map<String, String> arguments = new HashMap<>();
        arguments.put("dateExpires", dateExpires);
        arguments.put("appVersion", appVersion);

        //ApiRequest will never be updated, only new records will be inserted.
        try {
            arguments.put("endpoint", Utils.aes256EncryptStringAsBase64String(this.endpoint));
            arguments.put("requestType",Utils.aes256EncryptStringAsBase64String("" + this.requestType));
            arguments.put("headers", Utils.aes256EncryptStringAsBase64String(headers));
            arguments.put("parameters", Utils.aes256EncryptStringAsBase64String(parameters));

            this.id = String.valueOf(persistenceLayer.executeInsertQuery(getTableName(), arguments));

        } catch (DecryptionFailureException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateTableSchemaIfNeeded(){
        if (!Utils.isAppUpdated(WoolworthsApplication.getAppContext())){
            return;
        }

        List<String> queries = new ArrayList<String>();
        queries.add("PRAGMA foreign_keys = 0;");
        queries.add("CREATE TABLE sqlitestudio_temp_table AS SELECT * FROM ApiRequest;");
        queries.add("DROP TABLE ApiRequest;");
        queries.add("CREATE TABLE ApiRequest ( id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, endpoint VARCHAR (255) NOT NULL, requestType VARCHAR NOT NULL, headers VARCHAR, parameters VARCHAR, dateCreated DATETIME DEFAULT (datetime() ) NOT NULL, dateUpdated DATETIME NOT NULL DEFAULT (datetime() ), dateExpires DATETIME DEFAULT (datetime() ) NOT NULL, appVersion VARCHAR (50) NOT NULL DEFAULT ('5.16.0.300') );");
        queries.add("INSERT INTO ApiRequest ( id, endpoint, requestType, headers, parameters, dateCreated, dateUpdated, dateExpires ) SELECT id, endpoint, requestType, headers, parameters, dateCreated, dateUpdated, dateExpires FROM sqlitestudio_temp_table;");
        queries.add("DROP TABLE sqlitestudio_temp_table;");
        queries.add("PRAGMA foreign_keys = 1;");

        PersistenceLayer.getInstance().executeSQLStatements(queries);
    }
}
class CacheEmptyException extends Exception {
    public CacheEmptyException(String message) {
        super(message);
    }
}