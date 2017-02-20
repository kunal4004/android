package za.co.woolworths.financial.services.android.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.awfs.coordination.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import me.leolin.shortcutbadger.ShortcutBadger;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.Transaction;
import za.co.woolworths.financial.services.android.models.dto.TransactionParentObj;
import za.co.woolworths.financial.services.android.models.dto.WProduct;

import static android.Manifest.permission_group.STORAGE;

public class Utils {

    public final static float BIG_SCALE = 2.4f;
    public final static float SMALL_SCALE = 1.9f;
    public final static float DIFF_SCALE = BIG_SCALE - SMALL_SCALE;
    public final static int PAGE_SIZE = 60;
    public static int FIRST_PAGE = 0;
    public static int DEFAULT_SELECTED_NAVIGATION_ITEM = 0;

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

    public static final String SILVER_CARD = "400154";
    public static final String GOLD_CARD = "410374";
    public static final String BLACK_CARD = "410375";
    public static final int ACCOUNTS_PROGRESS_BAR_MAX_VALUE = 10000;

    public static void saveLastLocation(Location loc, Context mContext) {

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

    public static Location getLastSavedLocation(Context mContext) {
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


    public static boolean isLocationServiceEnabled(Context context) {
        LocationManager locationManager = null;
        boolean gps_enabled = false, network_enabled = false;

        if (locationManager == null)
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            //do nothing...
        }

        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            //do nothing...
        }

        return gps_enabled || network_enabled;

    }

    public static String getDistance(GoogleMap googleMap) {

        VisibleRegion visibleRegion = googleMap.getProjection().getVisibleRegion();

        LatLng farRight = visibleRegion.farRight;
        LatLng farLeft = visibleRegion.farLeft;
        LatLng nearRight = visibleRegion.nearRight;
        LatLng nearLeft = visibleRegion.nearLeft;

        float[] distanceWidth = new float[2];
        Location.distanceBetween(
                (farRight.latitude + nearRight.latitude) / 2,
                (farRight.longitude + nearRight.longitude) / 2,
                (farLeft.latitude + nearLeft.latitude) / 2,
                (farLeft.longitude + nearLeft.longitude) / 2,
                distanceWidth
        );


        float[] distanceHeight = new float[2];
        Location.distanceBetween(
                (farRight.latitude + nearRight.latitude) / 2,
                (farRight.longitude + nearRight.longitude) / 2,
                (farLeft.latitude + nearLeft.latitude) / 2,
                (farLeft.longitude + nearLeft.longitude) / 2,
                distanceHeight
        );

        float distance;

        if (distanceWidth[0] > distanceHeight[0]) {
            distance = distanceWidth[0];
        } else {
            distance = distanceHeight[0];
        }
        return String.valueOf(distance);
    }

    public static void updateStatusBarBackground(Activity activity) {
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        View decor = activity.getWindow().getDecorView();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor(activity, R.color.black));
            decor.setSystemUiVisibility(0);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.setStatusBarColor(ContextCompat.getColor(activity, R.color.white));
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    public static void updateStatusBarBackground(Activity activity, int color) {
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        View decor = activity.getWindow().getDecorView();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor(activity, R.color.black));
            decor.setSystemUiVisibility(0);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.setStatusBarColor(ContextCompat.getColor(activity, color));
        }
    }

    public static List<TransactionParentObj> getdata(List<Transaction> transactions) {
        List<TransactionParentObj> transactionParentObjList = new ArrayList<>();
        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat outputFormat = new SimpleDateFormat("MMMM");
        TransactionParentObj transactionParentObj;

        for (int i = 0; i < transactions.size(); i++) {
            try {
                Date date = inputFormat.parse(transactions.get(i).date);
                String month = outputFormat.format(date);
                boolean monthFound = false;
                List<Transaction> transactionList = null;
                if (transactionParentObjList.size() == 0) {
                    transactionList = new ArrayList<>();
                    transactionParentObj = new TransactionParentObj();
                    transactionParentObj.setMonth(month);
                    transactionList.add(transactions.get(i));
                    transactionParentObj.setTransactionList(transactionList);
                    transactionParentObjList.add(transactionParentObj);
                } else {
                    for (int j = 0; j < transactionParentObjList.size(); j++) {
                        if (transactionParentObjList.get(j).getMonth().equals(month)) {
                            monthFound = true;
                            transactionParentObjList.get(j).getTransactionList().add(transactions.get(i));
                            break;
                        }
                    }

                    if (monthFound == false) {
                        transactionList = new ArrayList<>();
                        transactionParentObj = new TransactionParentObj();
                        transactionParentObj.setMonth(month);
                        transactionList.add(transactions.get(i));
                        transactionParentObj.setTransactionList(transactionList);
                        transactionParentObjList.add(transactionParentObj);
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }


        return transactionParentObjList;
    }

    public static String objectToJson(Object object) {
        Gson gson = new Gson();

        String response = gson.toJson(object);

        return response;
    }

    public static int getToolbarHeight(Context context) {
        final TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(
                new int[]{R.attr.actionBarSize});
        int toolbarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();

        return toolbarHeight;
    }

    public static int getTabsHeight(Context context) {
        return (int) context.getResources().getDimension(R.dimen.bank_spacing_width);
    }

    public static WProduct stringToJson(Context context, String value) {
        if (TextUtils.isEmpty(value))
            return null;

        try {
            SessionDao sessionDao = new SessionDao(context);
            sessionDao.key = SessionDao.KEY.STORES_LATEST_PAYLOAD;
            sessionDao.value = value;
            try {
                sessionDao.save();
            } catch (Exception e) {
                Log.e("TAG", e.getMessage());
            }
        } catch (Exception e) {
            Log.e("exception", String.valueOf(e));
        }

        TypeToken<WProduct> token = new TypeToken<WProduct>() {
        };
        return new Gson().fromJson(value, token.getType());
    }


    public static void sessionDaoSave(Context context, SessionDao.KEY key, String value) {
        SessionDao sessionDao = new SessionDao(context);
        sessionDao.key = key;
        sessionDao.value = value;
        try {
            sessionDao.save();
        } catch (Exception e) {
            Log.e("TAG", e.getMessage());
        }
    }

    public static String getSessionDaoValue(Context context, SessionDao.KEY key) {
        SessionDao sessionDao = null;
        try {
            sessionDao = new SessionDao(context, key).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sessionDao.value;
    }

    public static void setBadgeCounter(Context context, int badgeCount) {
        ShortcutBadger.applyCount(context, badgeCount);
        sessionDaoSave(context, SessionDao.KEY.UNREAD_MESSAGE_COUNT, String.valueOf(badgeCount));
    }

    public static void removeBadgeCounter(Context context) {
        ShortcutBadger.applyCount(context, 0);
    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }
}
