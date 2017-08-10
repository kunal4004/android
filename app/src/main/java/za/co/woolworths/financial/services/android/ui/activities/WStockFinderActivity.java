package za.co.woolworths.financial.services.android.ui.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import java.util.List;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.LocationResponse;
import za.co.woolworths.financial.services.android.models.dto.StoreDetails;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.ui.adapters.StockFinderFragmentAdapter;
import za.co.woolworths.financial.services.android.ui.fragments.StoreFinderListFragment;
import za.co.woolworths.financial.services.android.ui.fragments.StoreFinderMapFragment;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.FusedLocationSingleton;
import za.co.woolworths.financial.services.android.util.GoogleMapViewPager;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.UpdateStoreFinderFragment;
import za.co.woolworths.financial.services.android.util.Utils;

public class WStockFinderActivity extends AppCompatActivity implements StoreFinderMapFragment.SlidePanelEvent, View.OnClickListener {

	private LinearLayout layoutLocationServiceOff, layoutNoProductFound;
	private String mSkuID;
	private AppBarLayout mAppBarLayout;
	private int mActionBarSize;
	private List<StoreDetails> storeDetailsList;
	private WGlobalState mWGlobalState;
	private String TAG = this.getClass().getSimpleName();
	private boolean isLocationServiceButtonClicked;
	public GoogleMapViewPager mViewPager;
	public StockFinderFragmentAdapter mPagerAdapter;
	private Location mLocation;
	private int currentPosition = 0;
	private boolean updateMap;
	private ErrorHandlerView mErrorHandlerView;

	public interface RecyclerItemSelected {
		void onRecyclerItemClick(View v, int position, String filterType);
	}

	public static final int PERMS_REQUEST_CODE = 123;

	private final float LIGHTER_TEXT = 0.3f, NORMAL_TEXT = 1.0f;
	private WTextView tvMapView, tvListView;
	private ImageView imListView, imMapView;
	private TabLayout tabLayout;
	protected final int REQUEST_CHECK_SETTINGS = 99;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.updateStatusBarBackground(WStockFinderActivity.this);
		setContentView(R.layout.stock_finder_activity);
		mWGlobalState = ((WoolworthsApplication) WStockFinderActivity.this.getApplication()).getWGlobalState();
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(null);
		getSupportActionBar().setHomeAsUpIndicator(R.drawable.back24);

		String mProductName = "";
		Bundle mBundle = getIntent().getExtras();
		if (mBundle != null) {
			mProductName = mBundle.getString("PRODUCT_NAME");
			mSkuID = mBundle.getString("SELECTED_SKU");
		}

		layoutLocationServiceOff = (LinearLayout) findViewById(R.id.layoutLocationServiceOff);
		layoutNoProductFound = (LinearLayout) findViewById(R.id.layoutNoProductFound);
		NestedScrollView scrollView = (NestedScrollView) findViewById(R.id.nest_scrollview);
		scrollView.setFillViewport(true);
		mViewPager = (GoogleMapViewPager) findViewById(R.id.viewpager);
		WTextView toolbarTextView = (WTextView) findViewById(R.id.toolbarText);
		mAppBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout);
		WButton btnOnLocationService = (WButton) findViewById(R.id.buttonLocationOn);
		WButton buttonBackToProducts = (WButton) findViewById(R.id.buttonBackToProducts);
		buttonBackToProducts.setOnClickListener(this);
		btnOnLocationService.setOnClickListener(this);
		RelativeLayout mRelativeLayout = (RelativeLayout) findViewById(R.id.no_connection_layout);
		mErrorHandlerView = new ErrorHandlerView(WStockFinderActivity.this
				, mRelativeLayout);
		mErrorHandlerView.setMargin(mRelativeLayout, 0, 0, 0, 0);
		WButton btnRetry = (WButton) findViewById(R.id.btnRetry);
		btnRetry.setOnClickListener(this);
		toolbarTextView.setText(mProductName);
		setupViewPager(mViewPager);
		tabLayout = (TabLayout) findViewById(R.id.tabs);
		tabLayout.setupWithViewPager(mViewPager);

		tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				mViewPager.setCurrentItem(tab.getPosition());
			}

			@Override
			public void onTabUnselected(TabLayout.Tab tab) {

			}

			@Override
			public void onTabReselected(TabLayout.Tab tab) {

			}
		});

		setupTabIcons();

		TypedArray attrs = WStockFinderActivity.this.getTheme().obtainStyledAttributes(new int[]{android.R.attr.actionBarSize});
		mActionBarSize = (int) attrs.getDimension(0, 0) * 2;
		initLocationCheck();
	}

	private void setupViewPager(GoogleMapViewPager viewPager) {
		mPagerAdapter = new StockFinderFragmentAdapter(getSupportFragmentManager());
		mPagerAdapter.addFrag(new StoreFinderMapFragment(), getString(R.string.stock_finder_map_view));
		mPagerAdapter.addFrag(new StoreFinderListFragment(), getString(R.string.stock_finder_list_view));
		viewPager.setAdapter(mPagerAdapter);
		viewPager.addOnPageChangeListener(pageChangeListener);
	}

	private void setupTabIcons() {
		tabLayout.getTabAt(0).setCustomView(R.layout.stockfinder_custom_tab);
		tabLayout.getTabAt(1).setCustomView(R.layout.stockfinder_custom_tab);

		View mapView = tabLayout.getTabAt(0).getCustomView();
		View listView = tabLayout.getTabAt(1).getCustomView();

		imMapView = (ImageView) mapView.findViewById(R.id.tabIcon);
		tvMapView = (WTextView) mapView.findViewById(R.id.textIcon);
		imListView = (ImageView) listView.findViewById(R.id.tabIcon);
		tvListView = (WTextView) listView.findViewById(R.id.textIcon);

		tvMapView.setText(getString(R.string.stock_finder_map_view));
		tvListView.setText(getString(R.string.stock_finder_list_view));

		imMapView.setImageResource(R.drawable.mapview);
		imListView.setImageResource(R.drawable.listview);

		tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				switch (tab.getPosition()) {
					case 0:
						enableMapTab();
						break;
					case 1:
						enableListTab();
						break;
					default:
						break;
				}
			}

			@Override
			public void onTabUnselected(TabLayout.Tab tab) {

			}

			@Override
			public void onTabReselected(TabLayout.Tab tab) {

			}
		});

		enableMapTab();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				return true;
		}
		return false;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.stay, R.anim.slide_down_anim);
	}

	private void enableMapTab() {
		tvMapView.setAlpha(NORMAL_TEXT);
		imMapView.setAlpha(NORMAL_TEXT);
		tvListView.setAlpha(LIGHTER_TEXT);
		imListView.setAlpha(LIGHTER_TEXT);
	}

	private void enableListTab() {
		tvMapView.setAlpha(LIGHTER_TEXT);
		imMapView.setAlpha(LIGHTER_TEXT);
		tvListView.setAlpha(NORMAL_TEXT);
		imListView.setAlpha(NORMAL_TEXT);
	}

	private void setLayoutParams(int paramsHeight) {
		CoordinatorLayout.LayoutParams lsp = (CoordinatorLayout.LayoutParams) mAppBarLayout.getLayoutParams();
		lsp.height = paramsHeight;
		mAppBarLayout.setLayoutParams(lsp);
	}

	@Override
	public void slidePanelAnchored() {
		setLayoutParams(0);
	}

	@Override
	public void slidePanelCollapsed() {
		setLayoutParams(mActionBarSize);
	}


	private void locationAPIRequest() {
		init().execute();
	}

	public HttpAsyncTask<String, String, LocationResponse> init() {
		return new HttpAsyncTask<String, String, LocationResponse>() {
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
			}

			@Override
			protected LocationResponse httpDoInBackground(String... params) {
				return ((WoolworthsApplication) WStockFinderActivity.this.getApplication()).getApi().getLocationsItem(mSkuID, String.valueOf(mWGlobalState.getStartRadius()), String.valueOf(mWGlobalState.getEndRadius()));
			}

			@Override
			protected Class<LocationResponse> httpDoInBackgroundReturnType() {
				return LocationResponse.class;
			}

			@Override
			protected LocationResponse httpError(final String errorMessage, HttpErrorCode httpErrorCode) {
				WStockFinderActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mErrorHandlerView.networkFailureHandler(errorMessage);
					}
				});
				return new LocationResponse();
			}

			@Override
			protected void onPostExecute(LocationResponse locationResponse) {
				super.onPostExecute(locationResponse);
				storeDetailsList = locationResponse.Locations;
				if (storeDetailsList.size() > 0) {
					layoutNoProductFound.setVisibility(View.GONE);
					selectPage(currentPosition);
					Utils.showOneTimePopup(WStockFinderActivity.this, SessionDao.KEY.STORE_FINDER_ONE_TIME_POPUP, CustomPopUpDialogManager.VALIDATION_MESSAGE_LIST.INSTORE_AVAILABILITY);
				} else {
					selectPage(currentPosition);
					layoutNoProductFound.setVisibility(View.VISIBLE);
				}
			}
		};
	}


	public void startLocationUpdates() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (checkLocationPermission()) {
				if (ContextCompat.checkSelfPermission(WStockFinderActivity.this,
						Manifest.permission.ACCESS_FINE_LOCATION)
						== PackageManager.PERMISSION_GRANTED) {
					FusedLocationSingleton.getInstance().startLocationUpdates();
					// register observer for location updates
					LocalBroadcastManager.getInstance(WStockFinderActivity.this).registerReceiver(mLocationUpdated,
							new IntentFilter(FusedLocationSingleton.INTENT_FILTER_LOCATION_UPDATE));
				}
			} else {
				checkLocationPermission();
			}
		}
	}

	public void stopLocationUpdate() {

		// stop location updates
		FusedLocationSingleton.getInstance().stopLocationUpdates();
		// unregister observer
		LocalBroadcastManager.getInstance(WStockFinderActivity.this).unregisterReceiver(mLocationUpdated);

	}

	@RequiresApi(api = Build.VERSION_CODES.M)
	public boolean checkLocationPermission() {
		if (ContextCompat.checkSelfPermission(WStockFinderActivity.this,
				Manifest.permission.ACCESS_FINE_LOCATION)
				!= PackageManager.PERMISSION_GRANTED) {
			if (shouldShowRequestPermissionRationale(
					Manifest.permission.ACCESS_FINE_LOCATION)) {
				requestPermissions(
						new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
						PERMS_REQUEST_CODE);
			} else {
				//we can request the permission.
				requestPermissions(
						new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
						PERMS_REQUEST_CODE);
			}
			return false;
		} else {
			return true;
		}
	}

	/**
	 * handle new location
	 */
	private BroadcastReceiver mLocationUpdated = new BroadcastReceiver() {
		@RequiresApi(api = Build.VERSION_CODES.M)
		@Override
		public void onReceive(Context context, final Intent intent) {
			try {
				mLocation = intent.getParcelableExtra(FusedLocationSingleton.LBM_EVENT_LOCATION_UPDATE);
				Utils.saveLastLocation(mLocation, WStockFinderActivity.this);
				locationAPIRequest();
				if (Utils.isLocationServiceEnabled(WStockFinderActivity.this)) {
					checkLocationServiceAndSetLayout(true);
				} else {
					checkLocationServiceAndSetLayout(false);
				}
				stopLocationUpdate();
			} catch (Exception e) {
				Log.e(TAG, e.toString());
			}
		}
	};

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		switch (requestCode) {
			case PERMS_REQUEST_CODE: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {

					// permission was granted. Do the
					// contacts-related task you need to do.
					if (ContextCompat.checkSelfPermission(WStockFinderActivity.this,
							Manifest.permission.ACCESS_FINE_LOCATION)
							== PackageManager.PERMISSION_GRANTED) {
						if (isLocationServiceButtonClicked) {
							Intent locIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
							startActivityForResult(locIntent, REQUEST_CHECK_SETTINGS);
							overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
						} else {
							startLocationUpdates();
//							if (googleMap != null)
//								googleMap.setMyLocationEnabled(false);
						}
					}

				} else {

					// Permission denied, Disable the functionality that depends on this permission.
				}
				return;
			}
			// other 'case' lines to check for other permissions this app might request.
			// You can add here other case statements according to your requirement.
		}

	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopLocationUpdate();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	}

	private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {


		@Override
		public void onPageSelected(int newPosition) {
			switch (newPosition) {
				case 0:
					mViewPager.disableScroll(true);
					break;
				case 1:
					mViewPager.disableScroll(false);
					break;
			}
			selectPage(newPosition);
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		public void onPageScrollStateChanged(int arg0) {
		}
	};

	private void selectPage(int position) {
		UpdateStoreFinderFragment fragmentToShow = (UpdateStoreFinderFragment) mPagerAdapter.getItem(position);
		if (fragmentToShow != null) {
			fragmentToShow.onFragmentUpdate(mLocation, storeDetailsList);
		}
		currentPosition = position;
	}

	public void initLocationCheck() {
		if (Utils.isLocationServiceEnabled(WStockFinderActivity.this)) {
			checkLocationServiceAndSetLayout(true);
			startLocationUpdates();
		} else {
			checkLocationServiceAndSetLayout(false);
		}
	}

	public void checkLocationServiceAndSetLayout(boolean locationServiceStatus) {
		//Check for location service and Last location
		if (!locationServiceStatus) {
			mViewPager.setVisibility(View.GONE);
			layoutLocationServiceOff.setVisibility(View.VISIBLE);
		} else {
			layoutLocationServiceOff.setVisibility(View.GONE);
			mViewPager.setVisibility(View.VISIBLE);
		}
	}

	@RequiresApi(api = Build.VERSION_CODES.M)
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.buttonLocationOn:
				updateMap = true;
				if (checkLocationPermission()) {
					Intent locIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
					startActivity(locIntent);
					overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
				} else {
					isLocationServiceButtonClicked = true;
					checkLocationPermission();
				}
				break;

			case R.id.btnRetry:
				if (new ConnectionDetector().isOnline(WStockFinderActivity.this)) {
					mErrorHandlerView.hideErrorHandlerLayout();
					initLocationCheck();
				}
				break;

			case R.id.buttonBackToProducts:
				finish();
				overridePendingTransition(R.anim.stay, R.anim.slide_down_anim);
				break;
			default:
				break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (updateMap) {
			if (Utils.isLocationEnabled(WStockFinderActivity.this))
				checkLocationServiceAndSetLayout(true);
			startLocationUpdates();
			updateMap = false;
		}
	}
}
