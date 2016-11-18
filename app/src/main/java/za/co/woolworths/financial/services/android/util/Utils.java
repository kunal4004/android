package za.co.woolworths.financial.services.android.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.SearchHistory;
import za.co.woolworths.financial.services.android.models.dto.StoreOfferings;

import static android.Manifest.permission_group.STORAGE;
import static android.R.attr.key;
import static android.R.attr.type;
import static android.R.id.edit;
import static android.webkit.ConsoleMessage.MessageLevel.LOG;
import static com.awfs.coordination.R.id.offerings;
import static com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.L;

/**
 * Created by W7099877 on 26/10/2016.
 */

public class Utils {

    public final static float BIG_SCALE = 2.4f;
    public final static float SMALL_SCALE = 1.9f;
    public final static float DIFF_SCALE = BIG_SCALE - SMALL_SCALE;
    public static int FIRST_PAGE=0;


    //Firebase Messaging service

    // global topic to receive app wide push notifications
    public static final String TOPIC_GLOBAL = "global";

    // broadcast receiver intent filters
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String PUSH_NOTIFICATION = "pushNotification";

    // id to handle the notification in the notification tray
    public static final int NOTIFICATION_ID = 100;
    public static final int NOTIFICATION_ID_BIG_IMAGE = 101;

    public static final String SHARED_PREF = "ah_firebase";


    public static void saveLastLocation(Location loc,Context mContext)
    {

        try {
            JSONObject locationJson = new JSONObject();

            locationJson.put("lat", loc.getLatitude());
            locationJson.put("lon", loc.getLongitude());
            SharedPreferences mPrefs = mContext.getSharedPreferences("lastLocation", mContext.MODE_PRIVATE);
            SharedPreferences.Editor prefsEditor = mPrefs.edit();
            prefsEditor.putString("location", locationJson.toString());
            prefsEditor.commit();
        } catch (JSONException e) {
        }
       /* SharedPreferences mPrefs = mContext.getSharedPreferences("lastLocation", mContext.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        prefsEditor.putString("lat",String.valueOf(loc.getLatitude()));
        prefsEditor.putString("lon",String.valueOf(loc.getLongitude()));
        prefsEditor.commit();*/

    }
    public static Location getLastSavedLocation(Context mContext)
    {
       // Location location=new Location("");
        SharedPreferences mPrefs = mContext.getSharedPreferences("lastLocation", mContext.MODE_PRIVATE);
      /*  location.setLatitude(Double.parseDouble(mPrefs.getString("lat","0")));
        location.setLongitude(Double.parseDouble(mPrefs.getString("lon","0")));*/
      //  return  location;

        try {
            String json = mPrefs.getString("location", null);

            if (json != null) {
                JSONObject locationJson = new JSONObject(json);
                Location location = new Location(STORAGE);
                location.setLatitude(locationJson.getDouble("lat"));
                location.setLongitude(locationJson.getDouble("lon"));
                return location;
            }
        } catch (JSONException e) {
        }


        return null;

    }

    public static void addToRecentSearchedHistory(SearchHistory searchHistory, Context mContext)
    {
        List<SearchHistory> histories=null;
        histories=new ArrayList<>();
        histories=getRecentSearchedHistory(mContext);
        SharedPreferences mPrefs = mContext.getSharedPreferences("history", mContext.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        boolean isExist=false;
        if(histories==null)
        {
            histories.add(0,searchHistory);
            String json = gson.toJson(histories);
            prefsEditor.putString("myJson", json);
            prefsEditor.commit();
        }
        else {
            for (SearchHistory s : histories) {
                if ( s.searchedValue.equalsIgnoreCase(searchHistory.searchedValue))
                {
                     isExist=true;
                }
            }
            if(!isExist)
            {
                histories.add(0,searchHistory);
                if(histories.size()>5)
                    histories.remove(5);

                String json = gson.toJson(histories);
                prefsEditor.putString("myJson", json);
                prefsEditor.commit();
            }
        }



    }
    public static List<SearchHistory> getRecentSearchedHistory(Context mContext)
    {
        List<SearchHistory> historyList ;
        SharedPreferences mPrefs = mContext.getSharedPreferences("history", mContext.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = mPrefs.getString("myJson", "");
        if (json.isEmpty()) {
            historyList = new ArrayList<>();
        } else {
            Type type = new TypeToken<List<SearchHistory>>() {
            }.getType();
            historyList = gson.fromJson(json, type);
        }
        return historyList;
    }

    public static boolean isLocationServiceEnabled( Context context){
        LocationManager locationManager = null;
        boolean gps_enabled= false,network_enabled = false;

        if(locationManager ==null)
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        try{
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }catch(Exception ex){
            //do nothing...
        }

        try{
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }catch(Exception ex){
            //do nothing...
        }

        return gps_enabled || network_enabled;

    }

    public static String getDistance(GoogleMap googleMap)
    {

        VisibleRegion visibleRegion = googleMap.getProjection().getVisibleRegion();

        LatLng farRight = visibleRegion.farRight;
        LatLng farLeft = visibleRegion.farLeft;
        LatLng nearRight = visibleRegion.nearRight;
        LatLng nearLeft = visibleRegion.nearLeft;

        float[] distanceWidth = new float[2];
        Location.distanceBetween(
                (farRight.latitude+nearRight.latitude)/2,
                (farRight.longitude+nearRight.longitude)/2,
                (farLeft.latitude+nearLeft.latitude)/2,
                (farLeft.longitude+nearLeft.longitude)/2,
                distanceWidth
        );


        float[] distanceHeight = new float[2];
        Location.distanceBetween(
                (farRight.latitude+nearRight.latitude)/2,
                (farRight.longitude+nearRight.longitude)/2,
                (farLeft.latitude+nearLeft.latitude)/2,
                (farLeft.longitude+nearLeft.longitude)/2,
                distanceHeight
        );

        float distance;

        if (distanceWidth[0]>distanceHeight[0]){
            distance = distanceWidth[0];
        } else {
            distance = distanceHeight[0];
        }
        return  String.valueOf(distance);
    }


}
