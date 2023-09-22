package za.co.woolworths.financial.services.android.util;

import static android.Manifest.permission_group.STORAGE;
import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.awfs.coordination.BuildConfig;
import com.awfs.coordination.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.huawei.hms.api.HuaweiApiAvailability;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import me.leolin.shortcutbadger.ShortcutBadger;
import za.co.absa.openbankingapi.DecryptionFailureException;
import za.co.absa.openbankingapi.SymmetricCipher;
import za.co.absa.openbankingapi.woolworths.integration.AbsaSecureCredentials;
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.models.AppConfigSingleton;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.ApiRequestDao;
import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dao.SessionDao.KEY;
import za.co.woolworths.financial.services.android.models.dto.Account;
import za.co.woolworths.financial.services.android.models.dto.AccountsResponse;
import za.co.woolworths.financial.services.android.models.dto.CartSummary;
import za.co.woolworths.financial.services.android.models.dto.CartSummaryResponse;
import za.co.woolworths.financial.services.android.models.dto.OrderSummary;
import za.co.woolworths.financial.services.android.models.dto.OtherSkus;
import za.co.woolworths.financial.services.android.models.dto.ProductDetailResponse;
import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation;
import za.co.woolworths.financial.services.android.models.dto.StoreDetails;
import za.co.woolworths.financial.services.android.models.dto.statement.SendUserStatementRequest;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.activities.StatementActivity;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity;
import za.co.woolworths.financial.services.android.ui.activities.webview.activities.WInternalWebPageActivity;
import za.co.woolworths.financial.services.android.ui.activities.webview.usercase.WebViewHandler;
import za.co.woolworths.financial.services.android.ui.views.WBottomNavigationView;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WMaterialShowcaseView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.ui.views.actionsheet.ErrorDialogFragment;
import za.co.woolworths.financial.services.android.ui.views.actionsheet.SingleButtonDialogFragment;
import za.co.woolworths.financial.services.android.ui.views.badgeview.Badge;
import za.co.woolworths.financial.services.android.ui.views.badgeview.QBadgeView;
import za.co.woolworths.financial.services.android.util.analytics.AnalyticsManager;
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager;
import za.co.woolworths.financial.services.android.util.tooltip.TooltipHelper;
import za.co.woolworths.financial.services.android.util.tooltip.ViewTooltip;

public class Utils {

    public final static float BIG_SCALE = 2.4f;
    public final static float SMALL_SCALE = 1.9f;
    public final static float DIFF_SCALE = BIG_SCALE - SMALL_SCALE;
    public final static int PAGE_SIZE = 60;
    public static int FIRST_PAGE = 0;
    public static int PRIMARY_CARD_POSITION = 0;
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
    private static final int POPUP_DELAY_MILLIS = 3000;
    public static final String ACCOUNT_CHARGED_OFF = "CHARGED OFF";
    public static final String ACCOUNT_ACTIVE = "ACTIVE";

    public static final String[] CLI_POI_ACCEPT_MIME_TYPES = {
            "application/pdf",
            "application/excel",
            "application/msword",
            "image/png",
            "image/jpeg",
            "image/tiff"
    };

    public static void saveLastLocation(Location loc, Context mContext) {
        try {
            JSONObject locationJson = new JSONObject();

            Double latitude = null;
            Double longitude = null;

            if (loc != null) {
                latitude = loc.getLatitude();
                longitude = loc.getLongitude();
            }

            locationJson.put("lat", latitude);
            locationJson.put("lon", longitude);

            sessionDaoSave(SessionDao.KEY.LAST_KNOWN_LOCATION, locationJson.toString());
        } catch (JSONException e) {
        }

    }

    public static Location getLastSavedLocation() {

        try {
            String json = getSessionDaoValue(SessionDao.KEY.LAST_KNOWN_LOCATION);

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

    public static void updateStatusBarBackground(Activity activity) {
        if (activity == null) return;
        Window window = activity.getWindow();
        View decor = activity.getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(activity, R.color.black));
            decor.setSystemUiVisibility(0);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(activity, R.color.white));
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    public static void updateStatusBarBackground(Activity activity, int color) {
        Window window = activity.getWindow();

        View decor = activity.getWindow().getDecorView();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(activity, R.color.black));
        decor.setSystemUiVisibility(0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(activity, color));
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    public static void updateStatusBarBackground(Activity activity, int color, boolean enableDecor) {
        Window window = activity.getWindow();

        View decor = activity.getWindow().getDecorView();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(activity, R.color.black));
        decor.setSystemUiVisibility(0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(activity, color));
        }
    }

    public static String objectToJson(Object object) {
        Gson gson = new Gson();

        String response = gson.toJson(object);

        return response;
    }

    public static ProductDetailResponse stringToJson(Context context, String value) {
        if (TextUtils.isEmpty(value))
            return null;

        try {
            SessionDao sessionDao = SessionDao.getByKey(SessionDao.KEY.STORES_LATEST_PAYLOAD);
            sessionDao.value = value;
            try {
                sessionDao.save();
            } catch (Exception e) {
                Log.e("TAG", e.getMessage());
            }
        } catch (Exception e) {
            Log.e("exception", String.valueOf(e));
        }

        TypeToken<ProductDetailResponse> token = new TypeToken<ProductDetailResponse>() {
        };
        return new Gson().fromJson(value, token.getType());
    }

    public static void setBadgeCounter(int badgeCount) {

        if (badgeCount == 0) {
            removeBadgeCounter();
            return;
        }

        Context context = WoolworthsApplication.getAppContext();
        if (ShortcutBadger.isBadgeCounterSupported(context)) {
            ShortcutBadger.applyCount(context, badgeCount);
        } else {
            //fallback solution if ShortcutBadger is not supported
            BadgeUtils.setBadge(context, badgeCount);
        }

        sessionDaoSave(SessionDao.KEY.UNREAD_MESSAGE_COUNT, String.valueOf(badgeCount));
    }

    public static void removeBadgeCounter() {
        Context context = WoolworthsApplication.getAppContext();
        if (ShortcutBadger.isBadgeCounterSupported(context)) {
            ShortcutBadger.removeCount(context);
        } else {
            //fallback solution if ShortcutBadger is not supported
            BadgeUtils.clearBadge(context);
        }
    }

    public static boolean isLocationEnabled(Context context) {

        try {
            int locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } catch (Settings.SettingNotFoundException e) {
            return false;
        }
    }

    public static void displayValidationMessage(Context context, CustomPopUpWindow.MODAL_LAYOUT key, SendUserStatementRequest susr) {
        Intent openMsg = new Intent(context, CustomPopUpWindow.class);
        Bundle args = new Bundle();
        args.putSerializable("key", key);
        args.putString("description", "");
        String strSendUserStatement = new Gson().toJson(susr);
        args.putString(StatementActivity.SEND_USER_STATEMENT, strSendUserStatement);
        openMsg.putExtras(args);
        context.startActivity(openMsg);
        ((AppCompatActivity) context).overridePendingTransition(0, 0);
    }

    public static void displayValidationMessage(Context context, CustomPopUpWindow.MODAL_LAYOUT key, String description) {
        Intent openMsg = new Intent(context, CustomPopUpWindow.class);
        Bundle args = new Bundle();
        args.putSerializable("key", key);
        args.putString("description", description);
        openMsg.putExtras(args);
        context.startActivity(openMsg);
        if (context instanceof AppCompatActivity) {
            ((AppCompatActivity) context).overridePendingTransition(0, 0);
        } else if (context instanceof Activity){
            ((Activity) context).overridePendingTransition(0, 0);
        }
    }

    public static void displayDialog(Context context, CustomPopUpWindow.MODAL_LAYOUT key, String description, int requestCode) {
        Intent openMsg = new Intent(context, CustomPopUpWindow.class);
        Bundle args = new Bundle();
        args.putSerializable("key", key);
        args.putString("description", description);
        openMsg.putExtras(args);
        if (((Activity) context) != null) {
            Activity activity = ((Activity) context);
            activity.startActivityForResult(openMsg, requestCode);
            ((AppCompatActivity) activity).overridePendingTransition(0, 0);
        }
    }

    public static void displayValidationMessage(Context context, CustomPopUpWindow.MODAL_LAYOUT key, String description, boolean closeView) {
        Intent openMsg = new Intent(context, CustomPopUpWindow.class);
        Bundle args = new Bundle();
        args.putSerializable("key", key);
        args.putString("description", description);
        args.putBoolean("closeSlideUpPanel", closeView);
        openMsg.putExtras(args);
        ((AppCompatActivity) context).startActivityForResult(openMsg, 0);
        ((AppCompatActivity) context).overridePendingTransition(0, 0);
    }

    public static void displayValidationMessage(Context context, CustomPopUpWindow.MODAL_LAYOUT key, String title, String description) {
        Intent openMsg = new Intent(context, CustomPopUpWindow.class);
        Bundle args = new Bundle();
        args.putSerializable("key", key);
        args.putString("title", title);
        args.putString("description", description);
        openMsg.putExtras(args);
        context.startActivity(openMsg);
        ((AppCompatActivity) context).overridePendingTransition(0, 0);
    }

    public static void alertErrorMessage(Context context, String message) {
        if ( context  instanceof  Activity && ((Activity) context).isFinishing()) {
            return;
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
            dialog.show();


    }

    public static String addUTMCode(String link) {
        return link + "&utm_source=oneapp&utm_medium=referral&utm_campaign=product";
    }

    public static void openLinkInInternalWebView(String url) {
        openLinkInInternalWebView(url, false);
    }

    public static void openLinkInInternalWebView(String url, boolean mustRedirectBlankTargetLinkToExternal) {
        Context context = WoolworthsApplication.getAppContext();
        Intent openInternalWebView = new Intent(context, WInternalWebPageActivity.class);
        openInternalWebView.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        openInternalWebView.putExtra("externalLink", url);
        openInternalWebView.putExtra(WebViewHandler.ARG_REDIRECT_BLANK_TARGET_LINK_EXTERNAL, mustRedirectBlankTargetLinkToExternal);
        context.startActivity(openInternalWebView);
    }

    public static BroadcastReceiver connectionBroadCast(final Activity activity, final NetworkChangeListener networkChangeListener) {
        //IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                // Hey I received something, let's put it on some toast
                networkChangeListener.onConnectionChanged();
            }
        };
        return mBroadcastReceiver;
    }

    public static void makeCall(String number) {
        Context context = WoolworthsApplication.getInstance().getApplicationContext();
        Uri call = Uri.parse("tel:" + number);
        Intent openNumericKeypad = new Intent(Intent.ACTION_DIAL, call);
        openNumericKeypad.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(openNumericKeypad);
    }

    public static String getScope(String scope) {
        return scope.replaceAll("scope=", "");
    }

    public static String removeUnicodesFromString(String value) {
        value = value.replaceAll("[^a-zA-Z0-9 &*|_!@#$%^.,\\[\\]:;\"~{}<>()\\-+?]+", "");
        return value;
    }

    public static void clearSharedPreferences(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File dir = new File(context.getFilesDir().getParent() + "/shared_prefs/");
                    String[] children = dir.list();
                    for (int i = 0; i < children.length; i++) {
                        context.getSharedPreferences(children[i].replace(".xml", ""), Context.MODE_PRIVATE).edit().clear().commit();
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                    for (int i = 0; i < children.length; i++) {
                        new File(dir, children[i]).delete();
                    }
                } catch (Exception e) {
                    Log.e("TAG", e.getMessage());
                }
            }
        }).start();
    }

    public static void showOneTimePopup(Context context, SessionDao.KEY key, CustomPopUpWindow.MODAL_LAYOUT message_key) {
        try {
            String firstTime = Utils.getSessionDaoValue(key);
            if (firstTime == null) {
                Utils.displayValidationMessage(context, message_key, "");
                Utils.sessionDaoSave(key, "1");
            }
        } catch (NullPointerException ignored) {
        }
    }

    public static void showOneTimeTooltip(Context context, SessionDao.KEY key, View view, String message) {
        try {
            String firstTime = Utils.getSessionDaoValue(key);
            if (firstTime == null) {
                showTooltip(context, view, message);
                Utils.sessionDaoSave(key, "1");
            }
        } catch (NullPointerException ignored) {
        }
    }

    public static void showTooltip(Context context, View view, String message) {
        TooltipHelper tooltipHelper = new TooltipHelper(context);
        ViewTooltip mTooltip = tooltipHelper.showToolTipView(view, message,
                ContextCompat.getColor(context, R.color.tooltip_bg_color));
        mTooltip.show();
    }

    public static void triggerFireBaseEvents(String eventName, Map<String, String> arguments, Activity activity) {
        Bundle params = new Bundle();
        for (Map.Entry<String, String> entry : arguments.entrySet()) {
            params.putString(entry.getKey(), entry.getValue());
        }

        AnalyticsManager.Companion.logEvent(eventName, params);
        RequestInAppReviewKt.requestInAppReview(eventName, activity);
    }

    public static void triggerFireBaseEvents(String eventName, Activity activity) {
        AnalyticsManager.Companion.logEvent(eventName, null);
        RequestInAppReviewKt.requestInAppReview(eventName, activity);
    }

    public static void setScreenName(Activity activity, String screenName) {
        AnalyticsManager.Companion.setCurrentScreen(activity, screenName);
    }
    public static void setScreenName(String screenName) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName);
        AnalyticsManager.Companion.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
    }
    public static void sendEmail(String emailId, String subject, Context mContext) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse(emailId +
                "?subject=" + Uri.encode(subject) +
                "&body=" + Uri.encode("")));

        PackageManager pm = mContext.getPackageManager();
        List<ResolveInfo> listOfEmail = pm.queryIntentActivities(emailIntent, 0);
        if (listOfEmail.size() > 0) {
            mContext.startActivity(emailIntent);
        } else {
            Utils.displayValidationMessage(mContext,
                    CustomPopUpWindow.MODAL_LAYOUT.INFO,
                    mContext.getResources().getString(R.string.contact_us_no_email_error)
                            .replace("email_address", emailId).replace("subject_line", subject));
        }
    }

    public static void setBackgroundColor(TextView textView, int drawableId, int value) {
        Context context = textView.getContext();
        textView.setText(context.getResources().getString(value));
        textView.setTextColor(WHITE);
        textView.setBackgroundResource(drawableId);
        Typeface futuraFont = Typeface.createFromAsset(context.getAssets(), "fonts/WFutura-SemiBold.ttf");
        textView.setTypeface(futuraFont);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textView.getContext().getResources().getDimension(R.dimen.rag_rating_sp));
    }

    public static void setBackgroundColor(WTextView textView, int drawableId, int value) {
        Context context = textView.getContext();
        textView.setText(context.getResources().getString(value));
        textView.setTextColor(WHITE);
        textView.setBackgroundResource(drawableId);
        Typeface futuraFont = Typeface.createFromAsset(context.getAssets(), "fonts/WFutura-SemiBold.ttf");
        textView.setTypeface(futuraFont);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textView.getContext().getResources().getDimension(R.dimen.rag_rating_sp));
    }

    public static ListIterator removeObjectFromArrayList(Context context, List<StoreDetails> storeDetails) {
        ListIterator listIterator = storeDetails.listIterator();
        for (Iterator<StoreDetails> it = storeDetails.iterator(); it.hasNext(); ) {
            StoreDetails itStoreDetails = it.next();
            String status = itStoreDetails.status;
            if (!(status.equalsIgnoreCase(getString(context, R.string.status_amber)) ||
                    status.equalsIgnoreCase(getString(context, R.string.status_green)) ||
                    status.equalsIgnoreCase(getString(context, R.string.status_red)))) {
                it.remove();
            }
        }
        return listIterator;
    }

    public static String getString(Context context, int id) {
        Resources resources = context.getResources();
        return resources.getString(id);
    }

    public static void setRagRating(Context context, TextView storeOfferings, String status) {
        Resources resources = context.getResources();
        if (status.equalsIgnoreCase(resources.getString(R.string.status_red))) {
            setBackgroundColor(storeOfferings, R.drawable.round_red_corner, R.string.status_red_desc);
        } else if (status.equalsIgnoreCase(resources.getString(R.string.status_amber))) {
            setBackgroundColor(storeOfferings, R.drawable.round_amber_corner, R.string.status_amber_desc);
        } else if (status.equalsIgnoreCase(resources.getString(R.string.status_green))) {
            setBackgroundColor(storeOfferings, R.drawable.round_green_corner, R.string.status_green_desc);
        } else {
        }
    }

    public static void hideView(View view) {
        view.setVisibility(View.GONE);
    }

    public static ArrayList<OtherSkus> commonSizeList(String colour, boolean productHasColor, List<OtherSkus> mOtherSKU) {
        ArrayList<OtherSkus> commonSizeList = new ArrayList<>();
        if (productHasColor) { //product has color
            // filter by colour
            ArrayList<OtherSkus> sizeList = new ArrayList<>();
            for (OtherSkus sku : mOtherSKU) {
                if (sku.colour.equalsIgnoreCase(colour)) {
                    sizeList.add(sku);
                }
            }

            //remove duplicates
            for (OtherSkus os : sizeList) {
                if (!sizeValueExist(commonSizeList, os.colour)) {
                    commonSizeList.add(os);
                }
            }
        } else { // no color found
            ArrayList<OtherSkus> sizeList = new ArrayList<>();
            for (OtherSkus sku : mOtherSKU) {
                if (sku.colour.contains(colour)) {
                    sizeList.add(sku);
                }
            }

            //remove duplicates
            for (OtherSkus os : sizeList) {
                if (!sizeValueExist(commonSizeList, os.size)) {
                    commonSizeList.add(os);
                }
            }
        }
        return commonSizeList;
    }

    public static boolean sizeValueExist(ArrayList<OtherSkus> list, String name) {
        for (OtherSkus item : list) {
            if (item.size.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public static int numericFieldOnly(String text) {
        return Integer.valueOf(text.replaceAll("[\\D.]", ""));
    }

    public static Object strToJson(String jsonString, Class<?> className) {
        return new Gson().fromJson(jsonString, className);
    }

    public static String getUniqueDeviceID() {
        AtomicReference<String> deviceID = new AtomicReference<>(getSessionDaoValue(SessionDao.KEY.DEVICE_ID));
        if (deviceID.get() == null) {
            FirebaseInstallations.getInstance().getId().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    deviceID.set(task.getResult());
                    sessionDaoSave(SessionDao.KEY.DEVICE_ID, deviceID.get());
                } else if (!task.isSuccessful()) {
                    FirebaseManager.logException("Utils.getUniqueDeviceID() task failed");
                }
            });
        }
        return deviceID.get();
    }

    public static void disableEnableChildViews(View view, boolean enabled) {
        view.setEnabled(enabled);
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                disableEnableChildViews(child, enabled);
            }
        }
    }

    public static boolean checkCLIAccountNumberValidation(String value) {
        String regex = "[0-9]+";
        if (value != null && value.matches(regex) && value.length() > 4)
            return true;
        else
            return false;
    }

    public static Bitmap encodeAsBitmap(String contents, BarcodeFormat format, int img_width, int img_height) throws WriterException {

        String contentsToEncode = contents;
        if (contentsToEncode == null) {
            return null;
        }
        Map<EncodeHintType, Object> hints = null;
        String encoding = guessAppropriateEncoding(contentsToEncode);
        if (encoding != null) {
            hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, encoding);
        }
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result;
        try {
            result = writer.encode(contentsToEncode, format, img_width, img_height, hints);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    private static String guessAppropriateEncoding(CharSequence contents) {
        // Very crude at the moment
        for (int i = 0; i < contents.length(); i++) {
            if (contents.charAt(i) > 0xFF) {
                return "UTF-8";
            }
        }
        return null;
    }

    public static void sendEmail(String email) {
        Context context = WoolworthsApplication.getAppContext();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri data = Uri.parse("mailto:"
                + email
                + "?subject=" + "" + "&body=" + "");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(data);
        context.startActivity(intent);
    }

    public static Badge addBadgeAt(Context context, WBottomNavigationView mBottomNav, int position, int number) {
        BottomNavigationItemView bottomNavItem = mBottomNav.getBottomNavigationItemView(position);
        String tagPosition = "BADGE_POSITION_" + position;
        QBadgeView badge = ((ViewGroup) bottomNavItem.getParent()).findViewWithTag(tagPosition);
        if (badge != null) {
            return badge.setBadgeNumber(number);
        } else {
            badge = new QBadgeView(context);
            badge.setTag(tagPosition);
            return badge
                    .setBadgeNumber(number)
                    .setBadgeBackgroundColor(R.color.black)
                    .setGravityOffset(15, 2, true)
                    .bindTarget(mBottomNav.getBottomNavigationItemView(position));
        }
    }

//	public static void updateStatusBar(Activity activity, int color) {
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//			Window window = activity.getWindow();
//			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//			window.setStatusBarColor(activity.getResources().getColor(color));
//		}
//	}

    private static Calendar getCurrentInstance() {
        return Calendar.getInstance(Locale.ENGLISH);
    }

    public static String getDate(int month) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        cal.add(Calendar.MONTH, -month);
        return sdf.format(cal.getTime());
    }


    public static boolean deleteDirectory(File path) {
        // TODO Auto-generated method stub
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    public static String getProductOfferingId(AccountsResponse accountResponse, String productGroupCode) {
        List<Account> accountList = accountResponse.accountList;
        if (accountList != null) {
            for (Account account : accountList) {
                if (account.productGroupCode.equalsIgnoreCase(productGroupCode)) {
                    int productOfferingId = account.productOfferingId;
                    setProductOfferingId(productOfferingId);
                    return String.valueOf(productOfferingId);
                }
            }
        }
        setProductOfferingId(0);
        return "0";
    }

    private static void setProductOfferingId(int productOfferingId) {
        WoolworthsApplication.getInstance().setProductOfferingId(productOfferingId);
    }

    public static void sendBus(Object object) {
        WoolworthsApplication woolworthsApplication = WoolworthsApplication.getInstance();
        if (woolworthsApplication != null) woolworthsApplication.bus().send(object);
    }

    public static int dp2px(float dpValue) {
        Context context = WoolworthsApplication.getAppContext();
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static ShoppingDeliveryLocation getPreferredDeliveryLocation() {
        ShoppingDeliveryLocation preferredDeliveryLocation = null;
        AppInstanceObject.User currentUserObject = AppInstanceObject.get().getCurrentUserObject();
        return (currentUserObject.preferredShoppingDeliveryLocation != null && currentUserObject.preferredShoppingDeliveryLocation.fulfillmentDetails != null) ? currentUserObject.preferredShoppingDeliveryLocation : preferredDeliveryLocation;
    }

    public static void savePreferredDeliveryLocation(ShoppingDeliveryLocation shoppingDeliveryLocation) {
        AppInstanceObject.User currentUserObject = AppInstanceObject.get().getCurrentUserObject();
        currentUserObject.preferredShoppingDeliveryLocation = shoppingDeliveryLocation;
        currentUserObject.save();

        if (shoppingDeliveryLocation != null && shoppingDeliveryLocation.fulfillmentDetails != null) {
            AnalyticsManager.Companion.setUserProperty(FirebaseManagerAnalyticsProperties.PropertyNames.LIQUOR_DELIVERABLE,
                    "" + shoppingDeliveryLocation.fulfillmentDetails.getLiquorDeliverable());
        }

        Map<String, String> fulfillmentStoreType = KotlinUtils.Companion.retriveFulfillmentStoreIdList();
        if (fulfillmentStoreType != null && !fulfillmentStoreType.isEmpty()) {
            for (String type : fulfillmentStoreType.keySet()) {
                if (type.length() == 1)
                    type = "0$type";
                switch (type) {
                    case "01": {
                        AnalyticsManager.Companion.setUserProperty(FirebaseManagerAnalyticsProperties.PropertyNames.FULFILLMENT_FOOD_STORE_KEY_01, retrieveStoreId(type));
                        break;
                    }
                    case "02": {
                        AnalyticsManager.Companion.setUserProperty(FirebaseManagerAnalyticsProperties.PropertyNames.FULFILLMENT_FBH_STORE_KEY_02, retrieveStoreId(type));
                        break;
                    }
                    case "04": {
                        AnalyticsManager.Companion.setUserProperty(FirebaseManagerAnalyticsProperties.PropertyNames.FULFILLMENT_FBH_STORE_KEY_04, retrieveStoreId(type));
                        break;
                    }
                    case "07": {
                        AnalyticsManager.Companion.setUserProperty(FirebaseManagerAnalyticsProperties.PropertyNames.FULFILLMENT_FBH_STORE_KEY_07, retrieveStoreId(type));
                        break;
                    }
                }
            }
        }
    }

    public static void clearPreferredDeliveryLocation() {
        AppInstanceObject.User currentUserObject = AppInstanceObject.get().getCurrentUserObject();
        currentUserObject.preferredShoppingDeliveryLocation = null;
        currentUserObject.save();
    }

    public static void addToShoppingDeliveryLocationHistory(ShoppingDeliveryLocation shoppingDeliveryLocation) {
        AppInstanceObject.User currentUserObject = AppInstanceObject.get().getCurrentUserObject();
        currentUserObject.shoppingDeliveryLocationHistory.add(shoppingDeliveryLocation);
        if (currentUserObject.shoppingDeliveryLocationHistory.size() > AppInstanceObject.MAX_DELIVERY_LOCATION_HISTORY)
            currentUserObject.shoppingDeliveryLocationHistory.remove(0);
        currentUserObject.save();
    }

    public static ArrayList<ShoppingDeliveryLocation> getShoppingDeliveryLocationHistory() {
        AppInstanceObject.User currentUserObject = AppInstanceObject.get().getCurrentUserObject();
        return currentUserObject.shoppingDeliveryLocationHistory;
    }

    public static OrderSummary[] getCachedOrdersPendingPicking() {
        AppInstanceObject.User currentUserObject = AppInstanceObject.get().getCurrentUserObject();
        return currentUserObject.myOrdersPendingPicking;
    }

    public static void setCachedOrdersPendingPicking(OrderSummary[] ordersSummary) {
        AppInstanceObject.User currentUserObject = AppInstanceObject.get().getCurrentUserObject();
        currentUserObject.myOrdersPendingPicking = ordersSummary;
        currentUserObject.save();
    }

    public static void clearCachedOrdersPendingPicking() {
        AppInstanceObject.User currentUserObject = AppInstanceObject.get().getCurrentUserObject();
        currentUserObject.myOrdersPendingPicking = new OrderSummary[0];
        currentUserObject.save();
    }

    public static void fadeInFadeOutAnimation(final View view, final boolean editMode) {
        Animation animation;
        if (!editMode) {
            animation = android.view.animation.AnimationUtils.loadAnimation(view.getContext(), R.anim.edit_mode_fade_in);
        } else {
            animation = android.view.animation.AnimationUtils.loadAnimation(view.getContext(), R.anim.edit_mode_fade_out);
        }

        if (view instanceof WButton) {
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    view.setEnabled(!editMode);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
        view.startAnimation(animation);
    }


    public static void removeFromDb(SessionDao.KEY key) {
        try {
            SessionDao.getByKey(key).delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void clearCacheHistory() {
        QueryBadgeCounter.getInstance().notifyBadgeCounterUpdate(BottomNavigationActivity.REMOVE_ALL_BADGE_COUNTER);
        Utils.removeFromDb(SessionDao.KEY.DELIVERY_LOCATION_HISTORY);
        Utils.removeFromDb(SessionDao.KEY.STORES_USER_SEARCH);
        Utils.removeFromDb(SessionDao.KEY.STORES_USER_LAST_LOCATION);
        Utils.removeFromDb(SessionDao.KEY.LIVE_CHAT_EXTRAS);
        Utils.removeFromDb(SessionDao.KEY.CARD_NOT_RECEIVED_DIALOG_WAS_SHOWN);
        Utils.removeFromDb(KEY.SHOP_OPTIMISER_SQLITE_MODEL);

        AppInstanceObject appInstanceObject = AppInstanceObject.get();
        appInstanceObject.setDefaultInAppChatTipAcknowledgements();
        appInstanceObject.save();
    }

    public static void truncateMaxLine(final TextView tv) {
        tv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                tv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                if (tv.getLineCount() > 2) {
                    int lineEndIndex = tv.getLayout().getLineEnd(1);
                    String text = tv.getText().subSequence(0, lineEndIndex - 6) + "..."; //TODO:: truncate 3 characters at end
                    tv.setText(text);
                }
            }
        });
    }

    public static String toJson(Object jsonObject) {
        return new Gson().toJson(jsonObject);
    }




    public static Object jsonStringToObject(String value, Class cl) {
        if (TextUtils.isEmpty(value)) return null;
        return new Gson().fromJson(value, cl);// json to Model
    }

    @Nullable
    public static String retrieveStoreId(String fulFillmentType) {
        if (fulFillmentType!=null&&fulFillmentType.length() == 1)
            fulFillmentType = "0" + fulFillmentType;
        return KotlinUtils.Companion.retrieveFulfillmentStoreId(fulFillmentType);
    }

    public static void toggleStatusBarColor(final Activity activity, int color) {
        if (activity != null) {
            updateStatusBarBackground(activity, color, true);
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    updateStatusBarBackground(activity);
                }
            };
            new Handler().postDelayed(runnable, 4000);
        }
    }

    public static void toggleStatusBarColor(final Activity activity, int toggleColor, final int defaultColor) {
        if (activity != null) {
            updateStatusBarBackground(activity, toggleColor, true);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateStatusBarBackground(activity, defaultColor);
                }
            }, 4000);
        }
    }

    public static void deliveryLocationEnabled(Context context, boolean enabled, final View view) {
        if(context==null)
            return;
        Animation animFadeOut = android.view.animation.AnimationUtils.loadAnimation(context, R.anim.edit_mode_fade_out);
        animFadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setEnabled(false);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        Animation animFadeIn = android.view.animation.AnimationUtils.loadAnimation(context, R.anim.edit_mode_fade_in);
        animFadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        if (enabled) {
            view.startAnimation(animFadeIn);
        } else {
            view.startAnimation(animFadeOut);
        }
    }

    public static void showOneTimePopup(Context context, SessionDao.KEY key, CustomPopUpWindow.MODAL_LAYOUT message_key, String message) {
        try {
            String firstTime = Utils.getSessionDaoValue(key);
            if (firstTime == null) {
                Utils.displayValidationMessage(context, message_key, message);
                Utils.sessionDaoSave(key, "1");
            }
        } catch (NullPointerException ignored) {
        }
    }

    public static void displayValidationMessageForResult(Activity context, CustomPopUpWindow.MODAL_LAYOUT key, String description, int requestCode) {
        if(context != null){
            Intent openMsg = new Intent(context, CustomPopUpWindow.class);
            Bundle args = new Bundle();
            args.putSerializable("key", key);
            args.putString("description", description);
            openMsg.putExtras(args);
            context.startActivityForResult(openMsg, requestCode);
            ((AppCompatActivity) context).overridePendingTransition(0, 0);
        }
    }

    public static void displayValidationMessageForResult(Fragment fragment, Activity activity, CustomPopUpWindow.MODAL_LAYOUT key, String title, String description, String buttonTitle, int requestCode) {
        Intent openMsg = new Intent(activity, CustomPopUpWindow.class);
        Bundle args = new Bundle();
        args.putSerializable("key", key);
        args.putString("title", title);
        args.putString("description", description);
        args.putString("buttonTitle", buttonTitle);
        openMsg.putExtras(args);
        fragment.startActivityForResult(openMsg, requestCode);
        ((AppCompatActivity) activity).overridePendingTransition(0, 0);
    }

    public static String toTitleCase(String givenString) {
        String words[] = givenString.replaceAll("\\s+", " ").trim().split(" ");
        String newSentence = "";
        for (String word : words) {
            for (int i = 0; i < word.length(); i++)
                newSentence = newSentence + ((i == 0) ? word.substring(i, i + 1).toUpperCase() :
                        (i != word.length() - 1) ? word.substring(i, i + 1).toLowerCase() : word.substring(i, i + 1).toLowerCase() + " ");
        }

        return newSentence;
    }

    public static void saveFeatureWalkthoughShowcase(WMaterialShowcaseView.Feature feature) {
        AppInstanceObject appInstanceObject = AppInstanceObject.get();
        switch (feature) {
            case BARCODE_SCAN:
                appInstanceObject.featureWalkThrough.barcodeScan = true;
                break;
            case FIND_IN_STORE:
                appInstanceObject.featureWalkThrough.findInStore = true;
                break;
            case DELIVERY_LOCATION:
                appInstanceObject.featureWalkThrough.deliveryLocation = true;
                break;
            case VOUCHERS:
                appInstanceObject.featureWalkThrough.vouchers = true;
                break;
            case REFINE:
                appInstanceObject.featureWalkThrough.refineProducts = true;
                break;
            case ACCOUNTS:
                appInstanceObject.featureWalkThrough.account = true;
                break;
            case SHOPPING_LIST:
                appInstanceObject.featureWalkThrough.shoppingList = true;
                break;
            case STATEMENTS:
                appInstanceObject.featureWalkThrough.statements = true;
                break;
            case CART_REDEEM_VOUCHERS:
                appInstanceObject.featureWalkThrough.cartRedeemVoucher = true;
            case CREDIT_SCORE:
                appInstanceObject.featureWalkThrough.creditScore = true;
                break;
            case VTO_TRY_IT:
                appInstanceObject.featureWalkThrough.isTryItOn = true;
                break;
            case SHOPPING:
                appInstanceObject.featureWalkThrough.shopping = true;
                break;
            case DASH:
                appInstanceObject.featureWalkThrough.dash = true;
                break;
            case DELIVERY_DETAILS:
                appInstanceObject.featureWalkThrough.delivery_details = true;
                break;
            case MY_LIST:
                appInstanceObject.featureWalkThrough.my_lists = true;
                break;
            case PARGO_STORE:
                appInstanceObject.featureWalkThrough.pargo_store = true;
                break;
            case NEW_FBH_CNC:
                appInstanceObject.featureWalkThrough.new_fbh_cnc = true;
                break;
            default:
                break;
        }
        appInstanceObject.save();
    }

    public static void enableFeatureWalkThroughTutorials(boolean enable) {
        AppInstanceObject appInstanceObject = AppInstanceObject.get();
        appInstanceObject.featureWalkThrough.showTutorials = enable;
        appInstanceObject.save();
    }

    public static boolean isFeatureWalkThroughTutorialsEnabled() {
        return AppInstanceObject.get().featureWalkThrough.showTutorials;
    }

    public static boolean isFeatureTutorialsDismissed(WMaterialShowcaseView wMaterialShowcaseView) {
        if (wMaterialShowcaseView == null)
            return true;
        else
            return wMaterialShowcaseView.isDismissed() ? true : false;
    }

    public static String ellipsizeVoucherDescription(String input) {
        if (input.length() > 99)
            return input.substring(0, 96) + "...";
        else
            return input;
    }

    public static void setViewHeightToRemainingBottomSpace(final Activity activity, final View view) {
        view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (view.getViewTreeObserver().isAlive())
                    view.getViewTreeObserver().removeOnPreDrawListener(this);

                int[] locations = new int[2];
                view.getLocationOnScreen(locations);
                int viewYPositionOnScreen = locations[1];

                if (activity != null) {
                    Display display = activity.getWindowManager().getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);
                    int screenHeight = size.y;

                    ViewGroup.LayoutParams params = view.getLayoutParams();
                    params.height = screenHeight - viewYPositionOnScreen + getSoftButtonsBarHeight(activity);
                    view.setLayoutParams(params);
                }

                return false;
            }
        });
    }

    public static int getSoftButtonsBarHeight(Activity activity) {
        // getRealMetrics is only available with API 17 and +
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DisplayMetrics metrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int usableHeight = metrics.heightPixels;
            activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            int realHeight = metrics.heightPixels;
            if (realHeight > usableHeight)
                return realHeight - usableHeight;
            else
                return 0;
        }
        return 0;
    }


    //add negative sign before currency value
    public static String removeNegativeSymbol(String amount) {
        return formatAmount(amount);
    }

    //add negative sign before currency value
    public static String removeNegativeSymbol(SpannableString amount) {
        return formatAmount(amount.toString());
    }

    @NonNull
    private static String formatAmount(String currentAmount) {
        if (currentAmount.contains("-")) {
            currentAmount = currentAmount.replaceAll("-", "");
            currentAmount = currentAmount.replace("R", "- R");
        }
        return currentAmount;
    }

    public static void displayDialogActionSheet(Activity activity, int description, int buttonText) {
        if (activity == null) return;
        AppCompatActivity appCompatActivity = (AppCompatActivity) activity;
        FragmentManager fm = appCompatActivity.getSupportFragmentManager();
        SingleButtonDialogFragment singleButtonDialogFragment =
                SingleButtonDialogFragment.newInstance(appCompatActivity.getString(description), appCompatActivity.getString(buttonText));
        singleButtonDialogFragment.show(fm, SingleButtonDialogFragment.class.getSimpleName());
    }

    public static void displayValidationMessageForResult(Fragment fragment, Activity activity, CustomPopUpWindow.MODAL_LAYOUT key, String title, String description, String buttonTitle) {
        Intent openMsg = new Intent(activity, CustomPopUpWindow.class);
        Bundle args = new Bundle();
        args.putSerializable("key", key);
        args.putString("title", title);
        args.putString("description", description);
        args.putString("buttonTitle", buttonTitle);
        openMsg.putExtras(args);
        fragment.startActivity(openMsg);
        ((AppCompatActivity) activity).overridePendingTransition(0, 0);
    }

    public static void setUserKMSIState(Boolean state) {
        AppInstanceObject.User currentUserObject = AppInstanceObject.get().getCurrentUserObject();
        currentUserObject.kmsi = state;
        currentUserObject.save();
    }

    public static Boolean getUserKMSIState() {
        AppInstanceObject.User currentUserObject = AppInstanceObject.get().getCurrentUserObject();
        return currentUserObject.kmsi;
    }

    public static void setLinkConfirmationShown(Boolean isShown) {
        AppInstanceObject.User currentUserObject = AppInstanceObject.get().getCurrentUserObject();
        currentUserObject.isLinkConfirmationScreenShown = isShown;
        currentUserObject.save();
    }

    public static Boolean getLinkDeviceConfirmationShown() {
        AppInstanceObject.User currentUserObject = AppInstanceObject.get().getCurrentUserObject();
        return currentUserObject.isLinkConfirmationScreenShown;
    }

    public static String getAbsaUniqueDeviceID() {

        AbsaSecureCredentials absaSecureCredentials = new AbsaSecureCredentials();

        String deviceID = absaSecureCredentials.getDeviceId();
        if (TextUtils.isEmpty(deviceID)) {
            absaSecureCredentials.setDeviceId(UUID.randomUUID().toString().replace("-", ""));
            absaSecureCredentials.save();
        }

        return absaSecureCredentials.getDeviceId();
    }

    public static void sessionDaoSave(SessionDao.KEY key, String value) {
        SessionDao sessionDao = SessionDao.getByKey(key);
        sessionDao.value = value;
        try {
            sessionDao.save();
        } catch (Exception e) {
            Log.e("TAG", e.getMessage());
        }
    }

    public static String getSessionDaoValue(SessionDao.KEY key) {
        SessionDao sessionDao = SessionDao.getByKey(key);
        return sessionDao.value;
    }

    public static String getAccountNumber(AccountsResponse accountResponse, String productGroupCode) {
        String accountNumber = "";
        List<Account> accountList = accountResponse.accountList;
        if (accountList != null) {
            for (Account account : accountList) {
                if (account.productGroupCode.equalsIgnoreCase(productGroupCode)) {
                    accountNumber = account.accountNumber;
                    break;
                }
            }
        }
        return TextUtils.isEmpty(accountNumber) ? "" : accountNumber;
    }

    public static String getCurrentDay() {
        return new SimpleDateFormat("EEEE").format(new Date());
    }

    public static String convertToCurrencyWithoutCent(Long amount) {
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        formatter.applyPattern("#,###,###,###");
        return formatter.format(amount).replace(",", " ");
    }

    public static void hideSoftKeyboard(Activity activity) {
        final InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager.isActive()) {
            if (activity.getCurrentFocus() != null) {
                inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
            }
        }
    }

    public static Integer getAppBuildNumber() {
        return BuildConfig.VERSION_CODE;
    }

    public static Boolean isFeatureEnabled(Integer minimumSupportedAppBuildNumber) {
        // if minimumSupportedAppBuildNumber is not present in AppConfig, then we consider the feature to be disabled
        if (minimumSupportedAppBuildNumber == null) return false;
        return getAppBuildNumber() >= minimumSupportedAppBuildNumber;
    }

    public static boolean checkForBinarySu() {
        boolean found = false;
        String[] places = {
                "/sbin/",
                "/system/bin/",
                "/system/xbin/",
                "/data/local/xbin/",
                "/data/local/bin/",
                "/system/sd/xbin/",
                "/system/bin/which",
                "/system/bin/failsafe/",
                "/data/local/"};
        for (String where : places) {
            if (new File(where + "su").exists()) {
                found = true;

                break;
            }
        }
        return found;
    }

    public static String aes256DecryptBase64EncryptedString(String entry) throws DecryptionFailureException {
        return new String(SymmetricCipher.Aes256Decrypt(ApiRequestDao.SYMMETRIC_KEY, Base64.decode(entry, Base64.DEFAULT)), StandardCharsets.UTF_8);
    }

    public static String aes256EncryptStringAsBase64String(String entry) throws DecryptionFailureException {
        return Base64.encodeToString(SymmetricCipher.Aes256Encrypt(ApiRequestDao.SYMMETRIC_KEY, entry), Base64.DEFAULT);
    }

    public static void updateUserVirtualTempCardState(Boolean state) {
        AppInstanceObject.User currentUserObject = AppInstanceObject.get().getCurrentUserObject();
        currentUserObject.isVirtualTemporaryStoreCardPopupShown = state;
        currentUserObject.save();
    }

    public static Boolean isVirtualTemporaryStoreCardPopupShown() {
        AppInstanceObject.User currentUserObject = AppInstanceObject.get().getCurrentUserObject();
        return currentUserObject.isVirtualTemporaryStoreCardPopupShown;
    }

    public static int calculateNoOfColumns(Context context, float columnWidthDp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;
        int noOfColumns = (int) (screenWidthDp / columnWidthDp + 0.5); // +0.5 for correct rounding to int.
        return noOfColumns;
    }

    public static Boolean isCartSummarySuburbIDEmpty(CartSummaryResponse cartSummaryResponse) {
        if (cartSummaryResponse.data != null) {
            List<CartSummary> cartSummaryList = cartSummaryResponse.data;
            if (cartSummaryList.get(0) != null) {
                CartSummary cartSummary = cartSummaryList.get(0);
                return TextUtils.isEmpty(cartSummary.suburbId);
            }
        }
        return true;
    }

    public static void showGeneralErrorDialog(FragmentManager fragmentManager, String message) {
        ErrorDialogFragment minAmountDialog = ErrorDialogFragment.Companion.newInstance(message);
        if (fragmentManager != null) {
            minAmountDialog.show(fragmentManager, ErrorDialogFragment.class.getSimpleName());
        }
    }

    public static void showGeneralErrorDialog(Activity activity, String message) {
        if (activity != null && !TextUtils.isEmpty(message)) {
            ErrorDialogFragment minAmountDialog = ErrorDialogFragment.Companion.newInstance(message);
            minAmountDialog.show(((AppCompatActivity) activity).getSupportFragmentManager(), ErrorDialogFragment.class.getSimpleName());
        }
    }

    public static boolean isValidLuhnNumber(String ccNumber) {
        int sum = 0;
        boolean alternate = false;
        for (int i = ccNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(ccNumber.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }

    public static boolean isAppUpdated(Context context) {
        if (context == null) {
            context = WoolworthsApplication.getAppContext();
        }
        String appVersionFromDB = Utils.getSessionDaoValue(SessionDao.KEY.APP_VERSION);
        String appLatestVersion = null;
        try {
            appLatestVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (appVersionFromDB == null || !appVersionFromDB.equalsIgnoreCase(appLatestVersion)) {
            return true;
        } else {
            return false;
        }
    }

    public static Boolean isCreditCardActivationEndpointAvailable() {
        String startTime = AppConfigSingleton.INSTANCE.getCreditCardActivation().getEndpointAvailabilityTimes().getStartTime();
        String endTime = AppConfigSingleton.INSTANCE.getCreditCardActivation().getEndpointAvailabilityTimes().getEndTime();
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY); // Get hour in 24 hour format
        int minute = now.get(Calendar.MINUTE);

        Date currentTime = WFormatter.parseDate(hour + ":" + minute);
        Date openingTime = WFormatter.parseDate(startTime);
        Date closingTime = WFormatter.parseDate(endTime);

        return (currentTime.after(openingTime) && currentTime.before(closingTime));
    }

    public static void deliverySelectionModalShown() {
        try {
            String firstTime = Utils.getSessionDaoValue(KEY.DELIVERY_OPTION);
            if (firstTime == null) {
                Utils.sessionDaoSave(KEY.DELIVERY_OPTION, "1");
            }
        } catch (NullPointerException ignored) {
        }
    }

    public static Boolean isDeliverySelectionModalShown() {
        String firstTime = Utils.getSessionDaoValue(KEY.DELIVERY_OPTION);
        return (firstTime != null);
    }

    public static HashSet<String> getDaySet() {
        HashSet dayNumber = new HashSet<String>();
        dayNumber.add("01");
        dayNumber.add("02");
        dayNumber.add("03");
        dayNumber.add("04");
        dayNumber.add("05");
        dayNumber.add("06");
        dayNumber.add("07");
        dayNumber.add("08");
        dayNumber.add("09");
        return dayNumber;
    }

    public static void setToken(String value) {
        try {
            if (TextUtils.isEmpty(value)) {
                return;
            }
            String firstTime = Utils.getSessionDaoValue(KEY.FCM_TOKEN);
            if (firstTime == null) {
                Utils.sessionDaoSave(KEY.FCM_TOKEN, value);
            }
        } catch (Exception ignored) {
            FirebaseManager.Companion.logException(ignored);
        }
    }

    public static String getToken() {
        String token = "";
        try {
            token = Utils.getSessionDaoValue(KEY.FCM_TOKEN);
        } catch (Exception ignored) {
            return null;
        }

        return token;
    }

    public static void setOCChatFCMToken(String value) {
        try {
            if (TextUtils.isEmpty(value)) {
                return;
            }
            String firstTime = Utils.getSessionDaoValue(KEY.OC_CHAT_FCM_TOKEN);
            if (firstTime == null) {
                Utils.sessionDaoSave(KEY.OC_CHAT_FCM_TOKEN, value);
            }
        } catch (Exception ignored) {
            FirebaseManager.Companion.logException(ignored);
        }
    }

    public static String getOCChatFCMToken() {
        String token = "";
        try {
            token = Utils.getSessionDaoValue(KEY.OC_CHAT_FCM_TOKEN);
        } catch (Exception ignored) {
            return null;
        }

        return token;
    }

    public static String getOCFCMToken() {
        String token;
        if (getOCChatFCMToken() != null && (!getOCChatFCMToken().isEmpty())) {
            token = getOCChatFCMToken();
        } else {
            token = "token_not_received";
        }
        return token;
    }




    public static void setInAppReviewRequested() {
        Utils.sessionDaoSave(KEY.IN_APP_REVIEW, "1");
    }

    public static boolean isInAppReviewRequested() {
        String firstTime = Utils.getSessionDaoValue(KEY.IN_APP_REVIEW);
        return (firstTime != null);
    }

    public static Boolean isGooglePlayOrHuaweiMobileServicesAvailable() {
        return isGooglePlayServicesAvailable() || isHuaweiMobileServicesAvailable();
    }

    public static Boolean isGooglePlayServicesAvailable() {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(WoolworthsApplication.getAppContext()) == ConnectionResult.SUCCESS;
    }

    public static Boolean isHuaweiMobileServicesAvailable() {
        return HuaweiApiAvailability.getInstance().isHuaweiMobileServicesAvailable(WoolworthsApplication.getAppContext()) == ConnectionResult.SUCCESS;
    }

   public static String formatAnalyticsButtonText(String btnName){
       String  btnText =  btnName.replaceAll("[^a-zA-Z0-9\\s]", "").trim();
       return btnText.replace(" ", "_").toLowerCase();
   }

    public static int calculatePercentage(int count, int totalCount){
        return (count*100)/totalCount;
    }

    public static void saveMonetateId(String monetateId) {
        AppInstanceObject.User currentUserObject = AppInstanceObject.get().getCurrentUserObject();
        currentUserObject.mId = monetateId;
        currentUserObject.save();
    }

    public static String getMonetateId() {
        AppInstanceObject.User currentUserObject = AppInstanceObject.get().getCurrentUserObject();
        return currentUserObject.mId;
    }

}