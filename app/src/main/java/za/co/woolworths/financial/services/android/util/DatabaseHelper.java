package za.co.woolworths.financial.services.android.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.squareup.okhttp.Headers;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.internal.framed.Header;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit.http.Body;

import static com.awfs.coordination.R.drawable.cursor;
import static com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.L;

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


    public int checkApirequest(String endpoint, String requestType, String heardes, String body)
    {
        int requestId=0;
        String[] columns = {REQUEST_ID};
        String selection=REQUEST_ENDPOINT+"=? and "+REQUEST_TYPE+"=? and "+REQUEST_HEADERS+"=? and "+REQUEST_PARAMETERS+"=? and "+REQUEST_DATE_EXPIRES+">?";
        SQLiteDatabase db = SQLiteDatabase.openDatabase(pathToSaveDBFile, null, SQLiteDatabase.OPEN_READONLY);

        Cursor cursor=db.query(API_REQUEST_TABLE,columns,selection, new String[] { endpoint,requestType,heardes,body,getCurrentTime() }, null, null, null);
         if(cursor != null && cursor.moveToFirst()) {
             requestId = cursor.getInt(cursor.getColumnIndex(REQUEST_ID));

         }

         return requestId;
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
    public int addApIRequest(String endpoint, String requestType, String heardes, String body)
    {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(pathToSaveDBFile, null, SQLiteDatabase.OPEN_READWRITE);
        ContentValues row = new ContentValues();
        row.put(REQUEST_ENDPOINT,endpoint);
        row.put(REQUEST_TYPE,requestType);
        row.put(REQUEST_HEADERS,heardes);
        row.put(REQUEST_PARAMETERS,body);
        row.put(REQUEST_DATE_CREATED,getCurrentTime());
        row.put(REQUEST_DATE_EXPIRES,getExpireTime());
        long id=db.insert(API_REQUEST_TABLE,null,row);
         return (int) id;
    }
    public void addApIResponse(String response,int requestId,int responseHandler )
    {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(pathToSaveDBFile, null, SQLiteDatabase.OPEN_READWRITE);
        byte[] contentByte = response.getBytes();
        db.execSQL("insert into "+API_RESPONSE_TABLE+" ("+RESPONSE_REQUEST_ID+","+RESPONSE_OBJECT+","+RESPONSE_HANDLER+") values(?,?,?)",new Object[]{requestId,contentByte,responseHandler});

    }
     public boolean checkResponseHandler(int requestId)
     {

         SQLiteDatabase db = SQLiteDatabase.openDatabase(pathToSaveDBFile, null, SQLiteDatabase.OPEN_READONLY);
         String query = "SELECT * FROM " +API_RESPONSE_TABLE+ " WHERE " +RESPONSE_REQUEST_ID+" = "+requestId;
         Cursor cursor = db.rawQuery(query, null);
         if(cursor!=null && cursor.moveToFirst()) {

             int responseHandler = cursor.getInt(cursor.getColumnIndex(RESPONSE_HANDLER));
             if (responseHandler == 0)
                 return false;
             else
                 return true;
         }
         else return false;
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



    public String getCurrentTime()
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date(System.currentTimeMillis());
        String value=dateFormat.format(date);
        return value;
    }

    public String getExpireTime()
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
        Date date = new Date(System.currentTimeMillis()+30*1000);
        String value=dateFormat.format(date);
        return value;
    }


}