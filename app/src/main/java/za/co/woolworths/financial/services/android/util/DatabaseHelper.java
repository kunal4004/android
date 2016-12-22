package za.co.woolworths.financial.services.android.util;

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
import java.io.UnsupportedEncodingException;

/**
 * Created by W7099877 on 19/12/2016.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    private final static String TAG = "DatabaseHelper";
    private final Context myContext;
    private static final String DATABASE_NAME = "OneApp.db";
    private static final int DATABASE_VERSION = 1;
    //=======TABLES=========
    private static final String API_REQUEST_TABLE= "ApiRequest";
    private static final String API_RESPONSE_TABLE= "ApiResponse";
    private static final String SESSION_TABLE= "Session";

    //======API_REQUEST Table Columns names=====
    private static final String REQUEST_ID="id";
    private static final String REQUEST_ENDPOINT="endpoint";
    private static final String REQUEST_TYPE="requestType";
    private static final String REQUEST_HEADERS="headers";
    private static final String REQUEST_PARAMETERS="parameters";
    private static final String REQUEST_DATE_CREATED="dateCreated";
    private static final String REQUEST_DATE_UPDATED="dateUpdated";
    private static final String REQUEST_DATE_EXPIRES="dateExpires";

    //======API_RESPONSE Table Columns names=====
    private static final String RESPONSE_ID="id";
    private static final String RESPONSE_REQUEST_ID="apiRequestId";
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





    private String pathToSaveDBFile;
    public DatabaseHelper(Context context, String filePath) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.myContext = context;
        pathToSaveDBFile = new StringBuffer(filePath).append("/").append(DATABASE_NAME).toString();
    }
    public void prepareDatabase() throws IOException {
        boolean dbExist = checkDataBase();
        if(dbExist) {
            Log.d(TAG, "Database exists.");
            int currentDBVersion = DATABASE_VERSION;
            if (DATABASE_VERSION > currentDBVersion) {
                Log.d(TAG, "Database version is higher than old.");
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
    }
    private boolean checkDataBase() {
        boolean checkDB = false;
        try {
            File file = new File(pathToSaveDBFile);
            checkDB = file.exists();
        } catch(SQLiteException e) {
            Log.d(TAG, e.getMessage());
        }
        return checkDB;
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
            Log.d(TAG, "Database deleted.");
        }
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }


    public void getApirequest()
    {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(pathToSaveDBFile, null, SQLiteDatabase.OPEN_READONLY);
        String query = "SELECT * FROM ApiRequest";
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        do{
            System.out.println("AAAAAAAAAAAAAAAAAAAA"+cursor.getString(0));

        }
        while(cursor.moveToNext());

    }
    public String getApiResponse(int requestId)
    {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(pathToSaveDBFile, null, SQLiteDatabase.OPEN_READONLY);
        String query = "SELECT * FROM " +API_RESPONSE_TABLE+ " WHERE " +RESPONSE_REQUEST_ID+" = "+requestId;
        Cursor cursor = db.rawQuery(query, null);
        if (cursor!=null)
              cursor.moveToFirst();
        String response="";
        try {
            response=new String (cursor.getBlob(cursor.getColumnIndex(RESPONSE_OBJECT)),"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return  response;

    }

    public void getSession()
    {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(pathToSaveDBFile, null, SQLiteDatabase.OPEN_READONLY);
        String query = "SELECT * FROM Session";
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        do{
            System.out.println("AAAAAAAAAAAAAAAAAAAA"+cursor.getString(0));
        }
        while(cursor.moveToNext());
    }
    public boolean addApiRequest()
    {
        return false;
    }
    public boolean addApiResponse()
    {
        return false;
    }
    public boolean addSession()
    {
        return false;
    }
}