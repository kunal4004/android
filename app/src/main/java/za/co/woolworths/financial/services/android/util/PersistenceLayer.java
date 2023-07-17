package za.co.woolworths.financial.services.android.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;

/**
 * Created by W7099877 on 19/12/2016.
 */

public class PersistenceLayer extends SQLiteOpenHelper {
    private final static String TAG = "PersistenceLayer";
    private final Context myContext;
    private static final String DATABASE_NAME = "OneApp.db";
    private static final int DATABASE_VERSION = 1;
    //=======TABLES=========
    private static final String API_REQUEST_TABLE= "ApiRequest";
    private static final String API_RESPONSE_TABLE= "ApiResponse";
    private static final String SESSION_TABLE= "Session";

    //======API_REQUEST Table Columns names=====
    private static final String REQUEST_ID="id";
    public  final String REQUEST_ENDPOINT="endpoint";
    private static final String REQUEST_TYPE="requestType";
    private static final String REQUEST_HEADERS="headers";
    private static final String REQUEST_PARAMETERS="parameters";
    private static final String REQUEST_DATE_CREATED="dateCreated";
    private static final String REQUEST_DATE_UPDATED="dateUpdated";
    private static final String REQUEST_DATE_EXPIRES="dateExpires";

    //======API_RESPONSE Table Columns names=====
    private static final String RESPONSE_ID="id";
    public final String RESPONSE_REQUEST_ID="apiRequestId";
    private static final String RESPONSE_HANDLER="responseHandler";
    private static final String RESPONSE_OBJECT="responseObject";
    private static final String RESPONSE_DATE_CREATED="dateCreated";
    private static final String RESPONSE_DATE_UPDATED="dateUpdated";

    //======SESSION Table Columns names=====
    private static final String SESSION_ID="id";
    private static final String SESSION_KEY="key";
    private static final String SESSION_VALUE="value";
    private static final String SESSION_DATA="data";
    private static final String SESSION_DATE_CREATED="dateCreated";
    private static final String SESSION_DATE_UPDATED="dateUpdated";

    private static PersistenceLayer instance;
    private String pathToSaveDBFile;

    public static PersistenceLayer getInstance(Context context){
        if (instance == null){
            instance = new PersistenceLayer(context, context.getFilesDir().getAbsolutePath());
            try {
                instance.prepareDatabase();
            } catch (IOException e) {
                instance = null;
            }
        }

        return instance;
    }

    public static PersistenceLayer getInstance(){
        if (instance == null){

            Context context = WoolworthsApplication.getInstance().getApplicationContext();
            instance = getInstance(context);
        }

        return instance;
    }

    private SQLiteDatabase openDatabase() {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(pathToSaveDBFile, null, SQLiteDatabase.OPEN_READWRITE);
        db.enableWriteAheadLogging();
        return db;
    }

    public void executeVoidQuery(String query, String[] arguments) throws Exception {
        SQLiteDatabase db = openDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(query, arguments);

            if (cursor.getCount() == 0) {
                throw new SQLiteException("Updated row count was 0. This is considered as a failed 'SQL UPDATE' transaction.");
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    public void executeSQLStatements(final List<String> queries){
        SQLiteDatabase db = openDatabase();

        for (String query : queries){
            db.execSQL(query);
        }

        db.close();
    }

    public void executeDeleteQuery(String query) {
        SQLiteDatabase db = openDatabase();
        db.execSQL(query);
        db.close();
    }

    public Map<String, String> executeReturnableQuery(String query, String[] arguments) {
        HashMap<String, String> result = new HashMap<>();

        SQLiteDatabase db = openDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(query, arguments);

            if (cursor.moveToFirst()) {
                String[] columnNames = cursor.getColumnNames();
                do {
                    for (String columnName : columnNames) {
                        int columnIndex = cursor.getColumnIndex(columnName);
                        String value = cursor.getString(columnIndex);
                        result.put(columnName, value);
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return result;
    }



    public long executeInsertQuery(String tableName, Map<String, String> arguments) {
        SQLiteDatabase db = openDatabase();
        ContentValues row = new ContentValues();

        for(Map.Entry<String, String> entry : arguments.entrySet()){
            row.put(entry.getKey(),entry.getValue());
        }
        long rowid;
        try {
            rowid = db.insert(tableName, null, row);
        } catch (SQLiteException e) {
            rowid = -1;
        }finally {
            db.close();
        }
        return rowid;
    }

    private PersistenceLayer(Context context, String filePath) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.myContext = context;
        pathToSaveDBFile = new StringBuffer(filePath).append("/").append(DATABASE_NAME).toString();
    }

    public void prepareDatabase() throws IOException {
        boolean dbExist = checkDataBase();
        if(dbExist) {
            int currentDBVersion = DATABASE_VERSION;
            if (DATABASE_VERSION > currentDBVersion) {
                deleteDb();
                try {
                    copyDataBase();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        } else {
            try {
                copyDataBase();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }

        //perform table schema updates if needed
    }

    private boolean checkDataBase() {
        try {
            File file = new File(pathToSaveDBFile);
            return file.exists();
        } catch(SQLiteException e) {
            return false;
        }
    }

    private void copyDataBase() throws IOException {
        OutputStream os = new FileOutputStream(pathToSaveDBFile);
        InputStream is = myContext.getAssets().open("databases/"+DATABASE_NAME);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) > 0) {
            os.write(buffer, 0, length);
        }
        is.close();
        os.flush();
        os.close();
    }

    public void deleteDb() {
        File file = new File(pathToSaveDBFile);
        if(file.exists()) {
            file.delete();
        }
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}