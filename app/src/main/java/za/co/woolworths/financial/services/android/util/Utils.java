package za.co.woolworths.financial.services.android.util;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.internal.BottomNavigationItemView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.*;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.awfs.coordination.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;

import me.leolin.shortcutbadger.ShortcutBadger;
import za.co.woolworths.financial.services.android.models.JWTDecodedModel;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.Account;
import za.co.woolworths.financial.services.android.models.dto.AccountsResponse;
import za.co.woolworths.financial.services.android.models.dto.DeliveryLocationHistory;
import za.co.woolworths.financial.services.android.models.dto.OtherSkus;
import za.co.woolworths.financial.services.android.models.dto.ShoppingList;
import za.co.woolworths.financial.services.android.models.dto.StoreDetails;
import za.co.woolworths.financial.services.android.models.dto.Transaction;
import za.co.woolworths.financial.services.android.models.dto.TransactionParentObj;
import za.co.woolworths.financial.services.android.models.dto.WProduct;
import za.co.woolworths.financial.services.android.models.dto.statement.SendUserStatementRequest;
import za.co.woolworths.financial.services.android.models.service.event.ProductState;
import za.co.woolworths.financial.services.android.ui.activities.CartActivity;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.activities.StatementActivity;
import za.co.woolworths.financial.services.android.ui.activities.WInternalWebPageActivity;
import za.co.woolworths.financial.services.android.ui.views.WBottomNavigationView;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.ui.views.badgeview.Badge;
import za.co.woolworths.financial.services.android.ui.views.badgeview.QBadgeView;
import za.co.woolworths.financial.services.android.util.tooltip.TooltipHelper;
import za.co.woolworths.financial.services.android.util.tooltip.ViewTooltip;

import static android.Manifest.permission_group.STORAGE;
import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;
import static za.co.woolworths.financial.services.android.models.service.event.ProductState.USE_MY_LOCATION;

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
	private static final int POPUP_DELAY_MILLIS = 3000;

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

			locationJson.put("lat", loc.getLatitude());
			locationJson.put("lon", loc.getLongitude());

			sessionDaoSave(mContext, SessionDao.KEY.LAST_KNOWN_LOCATION, locationJson.toString());
		} catch (JSONException e) {
		}

	}

	public static Location getLastSavedLocation(Context mContext) {

		try {
			String json = getSessionDaoValue(mContext, SessionDao.KEY.LAST_KNOWN_LOCATION);

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
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			window.setStatusBarColor(ContextCompat.getColor(activity, R.color.black));
			decor.setSystemUiVisibility(0);
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			window.setStatusBarColor(ContextCompat.getColor(activity, color));
			decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
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
			return sessionDao.value;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public static void setBadgeCounter(Context context, int badgeCount) {
		try {
			ShortcutBadger.applyCount(context, badgeCount);
			sessionDaoSave(context, SessionDao.KEY.UNREAD_MESSAGE_COUNT, String.valueOf(badgeCount));
		} catch (NullPointerException ex) {
		}
	}

	public static void removeBadgeCounter(Context context) {
		try {
			ShortcutBadger.applyCount(context, 0);
		} catch (NullPointerException ex) {
		}
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

	public static void addToShoppingCart(Context context, ShoppingList addtoShoppingCart) {
		List<ShoppingList> addtoShoppingCarts = getShoppingList(context);
		SessionDao sessionDao = new SessionDao(context);
		sessionDao.key = SessionDao.KEY.STORE_SHOPPING_LIST;
		Gson gson = new Gson();
		boolean isExist = false;
		if (addtoShoppingCarts == null) {
			addtoShoppingCarts = new ArrayList<>();
			addtoShoppingCarts.add(0, addtoShoppingCart);
			sessionDao.value = gson.toJson(addtoShoppingCarts);
			try {
				sessionDao.save();
			} catch (Exception e) {
				Log.e("TAG", e.getMessage());
			}
		} else {
			for (ShoppingList s : addtoShoppingCarts) {
				if (s.getProduct_id().equalsIgnoreCase(addtoShoppingCart.getProduct_id())) {
					isExist = true;
				}
			}
			if (!isExist) {
				addtoShoppingCarts.add(0, addtoShoppingCart);
				sessionDao.value = gson.toJson(addtoShoppingCarts);
				try {
					sessionDao.save();
				} catch (Exception e) {
					Log.e("TAG", e.getMessage());
				}
			}
		}
	}

	public static List<ShoppingList> getShoppingList(Context context) {
		List<ShoppingList> historyList = null;
		try {
			SessionDao sessionDao = new SessionDao(context,
					SessionDao.KEY.STORE_SHOPPING_LIST).get();
			if (sessionDao.value == null) {
				historyList = new ArrayList<>();
			} else {
				Gson gson = new Gson();
				Type type = new TypeToken<List<ShoppingList>>() {
				}.getType();
				historyList = gson.fromJson(sessionDao.value, type);
			}
		} catch (Exception e) {
			Log.e("TAG", e.getMessage());
		}
		return historyList;
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

	public static void openExternalLink(Context context, String url) {
		Intent openInternalWebView = new Intent(context, WInternalWebPageActivity.class);
		openInternalWebView.putExtra("externalLink", url);
		context.startActivity(openInternalWebView);
		((AppCompatActivity) context).overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
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

	public static void makeCall(Context context, String number) {
		Uri call = Uri.parse("tel:" + number);
		Intent openNumericKeypad = new Intent(Intent.ACTION_DIAL, call);
		context.startActivity(openNumericKeypad);
		((Activity) context).overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
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
			String firstTime = Utils.getSessionDaoValue(context, key);
			if (firstTime == null) {
				Utils.displayValidationMessage(context, message_key, "");
				Utils.sessionDaoSave(context, key, "1");
			}
		} catch (NullPointerException ignored) {
		}
	}

	public static void showOneTimeTooltip(Context context, SessionDao.KEY key, View view, String message) {
		try {
			String firstTime = Utils.getSessionDaoValue(context, key);
			if (firstTime == null) {
				showTooltip(context, view, message);
				Utils.sessionDaoSave(context, key, "1");
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


	public static void showOneTimePopup(Context context, SessionDao.KEY key, View view) {
		try {
			String firstTime = Utils.getSessionDaoValue(context, key);
			if (firstTime == null) {
				view.setVisibility(View.VISIBLE);
			} else {
				view.setVisibility(View.GONE);
			}
			Utils.sessionDaoSave(context, key, "1");
		} catch (NullPointerException ignored) {
		}
	}

	public static void triggerFireBaseEvents(Context mContext, String eventName, Map<String, String> arguments) {
		FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(mContext);

		Bundle params = new Bundle();
		for (Map.Entry<String, String> entry : arguments.entrySet()) {
			params.putString(entry.getKey(), entry.getValue());
		}

		mFirebaseAnalytics.logEvent(eventName, params);
	}

	public static JWTDecodedModel getJWTDecoded(Context mContext) {
		JWTDecodedModel result = new JWTDecodedModel();
		try {
			SessionDao sessionDao = new SessionDao(mContext, SessionDao.KEY.USER_TOKEN).get();
			if (sessionDao.value != null && !sessionDao.value.equals("")) {
				result = JWTHelper.decode(sessionDao.value);
			}
		} catch (Exception e) {
			Log.e("TAG", e.getMessage());
		}
		return result;
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

	public static void setBackgroundColor(WTextView textView, int drawableId, int value) {
		Context context = textView.getContext();
		textView.setText(context.getResources().getString(value));
		textView.setTextColor(WHITE);
		textView.setBackgroundResource(drawableId);
		Typeface futuraFont = Typeface.createFromAsset(context.getAssets(), "fonts/WFutura-SemiBold.ttf");
		textView.setTypeface(futuraFont);
		textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textView.getContext().getResources().getDimension(R.dimen.rag_rating_sp));
	}

	public static void setBackground(WTextView textView, int drawableId, int value) {
		Context context = textView.getContext();
		textView.setText(context.getResources().getString(value));
		textView.setTextColor(Color.WHITE);
		textView.setBackgroundResource(drawableId);
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

	public static void setRagRating(Context context, WTextView storeOfferings, String status) {
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

	public static void showView(WTextView view, String messageSummary) {
		view.setVisibility(View.VISIBLE);
		view.setText(messageSummary);
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

	public static String getUniqueDeviceID(Context context) {
		String deviceID = null;
		if (deviceID == null) {
			deviceID = getSessionDaoValue(context, SessionDao.KEY.DEVICE_ID);
			if (deviceID == null) {
				deviceID = FirebaseInstanceId.getInstance().getId();
				sessionDaoSave(context, SessionDao.KEY.DEVICE_ID, deviceID);
			}
		}

		return deviceID;
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

	public static void sendEmail(String email, FragmentActivity activity) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		Uri data = Uri.parse("mailto:"
				+ email
				+ "?subject=" + "" + "&body=" + "");
		intent.setData(data);
		activity.startActivity(intent);
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

	public static void showOneTimePopup(Context context) {
		try {
			String firstTime = Utils.getSessionDaoValue(context, SessionDao.KEY.PRODUCTS_ONE_TIME_POPUP);
			if (firstTime == null) {
				Utils.displayValidationMessage(context, CustomPopUpWindow.MODAL_LAYOUT.INFO, context.getResources().getString(R.string.products_onetime_popup_text));
				Utils.sessionDaoSave(context, SessionDao.KEY.PRODUCTS_ONE_TIME_POPUP, "1");
			}
		} catch (NullPointerException ignored) {
		}
	}

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


	public static List<DeliveryLocationHistory> getDeliveryLocationHistory(Context context) {
		List<DeliveryLocationHistory> history = null;
		try {
			SessionDao sessionDao = new SessionDao(context, SessionDao.KEY.DELIVERY_LOCATION_HISTORY).get();
			if (sessionDao.value == null) {
				history = new ArrayList<>();
			} else {
				Gson gson = new Gson();
				Type type = new TypeToken<List<DeliveryLocationHistory>>() {
				}.getType();
				history = gson.fromJson(sessionDao.value, type);
			}
		} catch (Exception e) {
			Log.e("TAG", e.getMessage());
		}
		return history;
	}


	public static String getSessionToken(Context context) {
		try {
			SessionDao sessionDao = new SessionDao(context, SessionDao.KEY.USER_TOKEN).get();
			if (sessionDao.value != null && !sessionDao.value.equals("")) {
				Log.i("SessionToken", sessionDao.value);
				return sessionDao.value;
			}
		} catch (Exception e) {
			Log.e("TAG", e.getMessage());
		}
		return "";
	}

	public static void sendBus(Object object) {
		WoolworthsApplication woolworthsApplication = WoolworthsApplication.getInstance();
		if (woolworthsApplication != null) woolworthsApplication.bus().send(object);
	}

	public static int dp2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	public static void saveRecentDeliveryLocation(DeliveryLocationHistory historyItem, Context context) {
		List<DeliveryLocationHistory> history = getRecentDeliveryLocations(context);
		SessionDao sessionDao = new SessionDao(context);
		sessionDao.key = SessionDao.KEY.DELIVERY_LOCATION_HISTORY;
		Gson gson = new Gson();
		boolean isExist = false;
		if (history == null) {
			history = new ArrayList<>();
			history.add(0, historyItem);
			String json = gson.toJson(history);
			sessionDao.value = json;
			try {
				sessionDao.save();
			} catch (Exception e) {
				Log.e("TAG", e.getMessage());
			}
		} else {
			for (DeliveryLocationHistory item : history) {
				if (item.suburb.id.equals(historyItem.suburb.id)) {
					isExist = true;
				}
			}
			if (!isExist) {
				history.add(0, historyItem);
				if (history.size() > 5)
					history.remove(5);

				sessionDao.value = gson.toJson(history);
				try {
					sessionDao.save();
				} catch (Exception e) {
					Log.e("TAG", e.getMessage());
				}
			}
		}
	}

	public static List<DeliveryLocationHistory> getRecentDeliveryLocations(Context context) {
		List<DeliveryLocationHistory> history = null;
		try {
			SessionDao sessionDao = new SessionDao(context, SessionDao.KEY.DELIVERY_LOCATION_HISTORY).get();
			if (sessionDao.value == null) {
				history = new ArrayList<>();
			} else {
				Gson gson = new Gson();
				Type type = new TypeToken<List<DeliveryLocationHistory>>() {
				}.getType();
				history = gson.fromJson(sessionDao.value, type);
			}
		} catch (Exception e) {
			Log.e("TAG", e.getMessage());
		}
		return history;
	}

	public static PopupWindow showToast(final Activity activity, String message, final boolean viewState) {

		// inflate your xml layout
		if (activity != null) {
			LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.add_to_cart_success, null);
			// set the custom display
			WTextView tvView = layout.findViewById(R.id.tvView);
			WTextView tvCart = layout.findViewById(R.id.tvCart);
			WTextView tvAddToCart = layout.findViewById(R.id.tvAddToCart);
			// initialize your popupWindow and use your custom layout as the view
			final PopupWindow pw = new PopupWindow(layout,
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT, true);

			tvView.setVisibility(viewState ? View.VISIBLE : View.GONE);
			tvAddToCart.setText(message);

			// handle popupWindow click event
			tvView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (viewState) {
						// do anything when popupWindow was clicked
						if (getSessionToken(activity) == null) {
							ScreenManager.presentSSOSignin(activity);
						} else {
							Intent openCartActivity = new Intent(activity, CartActivity.class);
							activity.startActivity(openCartActivity);
							activity.overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
						}
						pw.dismiss(); // dismiss the window
					}
				}
			});

			// dismiss the popup window after 3sec
			new Handler().postDelayed(new Runnable() {
				public void run() {
					if (pw != null)
						pw.dismiss();
				}
			}, POPUP_DELAY_MILLIS);
			return pw;
		}

		return null;
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


	public static void removeFromDb(SessionDao.KEY key, Context context) throws Exception {
		new SessionDao(context, key).delete();
	}

	public static void removeToken(SessionDao.KEY key, Context context) throws Exception {
		SessionDao sessionDao = new SessionDao(context, key).get();
		sessionDao.value = "";
		sessionDao.save();
	}
}