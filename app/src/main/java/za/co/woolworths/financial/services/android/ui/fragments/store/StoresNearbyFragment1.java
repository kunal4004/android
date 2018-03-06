package za.co.woolworths.financial.services.android.ui.fragments.store;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.LocationResponse;
import za.co.woolworths.financial.services.android.models.dto.StoreDetails;
import za.co.woolworths.financial.services.android.models.dto.StoreOfferings;
import za.co.woolworths.financial.services.android.ui.activities.SearchStoresActivity;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator;
import za.co.woolworths.financial.services.android.ui.adapters.CardsOnMapAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.MapWindowAdapter;
import za.co.woolworths.financial.services.android.ui.views.SlidingUpPanelLayout;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.FusedLocationSingleton;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.PopWindowValidationMessage;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WCustomViewPager;
import za.co.woolworths.financial.services.android.util.WFormatter;


public class StoresNearbyFragment1 extends Fragment implements OnMapReadyCallback, ViewPager.OnPageChangeListener, GoogleMap.OnMarkerClickListener {

	public static final int REQUEST_CALL = 1;
	WCustomViewPager pager;
	GoogleMap googleMap;
	static int CAMERA_ANIMATION_SPEED = 350;
	BitmapDescriptor unSelectedIcon;
	BitmapDescriptor selectedIcon;
	HashMap<String, Integer> mMarkers;
	ArrayList<Marker> markers;
	Marker previousmarker;
	SupportMapFragment mapFragment;
	ImageView close;
	int currentStorePostion = 0;
	private SlidingUpPanelLayout mLayout;
	public List<StoreDetails> storeDetailsList;
	private String TAG = "StoresNearbyFragment1";
	private boolean updateMap = false;

	Intent callIntent;

	RelativeLayout direction;
	RelativeLayout makeCall;

	//Detail page Views
	LinearLayout detailsLayout;
	LinearLayout timeingsLayout;
	LinearLayout brandsLayout;
	RelativeLayout storeTimingView;
	WTextView storeName;
	WTextView storeOfferings;
	WTextView storeAddress;
	WTextView storeDistance;
	WTextView storeNumber;

	ProgressBar progressBar;

	//Location Service Layouts
	LinearLayout layoutLocationServiceOff;
	RelativeLayout layoutLocationServiceOn;
	RelativeLayout relBrandLayout;

	WButton btnOnLocationService;
	protected static final int REQUEST_CHECK_SETTINGS = 99;
	Marker myLocation;
	public static final int PERMS_REQUEST_CODE = 123;
	private boolean navigateMenuState = false;

	private PopWindowValidationMessage mPopWindowValidationMessage;
	private ErrorHandlerView mErrorHandlerView;
	private Location mLocation;
	private StoresNearbyFragment1 mFragment;
	MenuItem searchMenu;
	private boolean isSearchMenuEnabled = true;
	public boolean isLocationServiceButtonClicked = false;
	private BottomNavigator mBottomNavigator;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		return inflater.inflate(R.layout.fragment_stores_nearby1, container, false);
	}

	@RequiresApi(api = Build.VERSION_CODES.M)
	@Override
	public void onViewCreated(View v, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(v, savedInstanceState);
		mFragment = this;
		mPopWindowValidationMessage = new PopWindowValidationMessage(getActivity());
		pager = (WCustomViewPager) v.findViewById(R.id.cardPager);
		detailsLayout = (LinearLayout) v.findViewById(R.id.detailsView);
		mLayout = (SlidingUpPanelLayout) v.findViewById(R.id.sliding_layout);
		close = (ImageView) v.findViewById(R.id.close);
		storeName = (WTextView) v.findViewById(R.id.storeName);
		storeOfferings = (WTextView) v.findViewById(R.id.offerings);
		storeDistance = (WTextView) v.findViewById(R.id.distance);
		storeAddress = (WTextView) v.findViewById(R.id.storeAddress);
		storeNumber = (WTextView) v.findViewById(R.id.storeNumber);
		timeingsLayout = (LinearLayout) v.findViewById(R.id.timeingsLayout);
		storeTimingView = (RelativeLayout) v.findViewById(R.id.storeTimingView);
		brandsLayout = (LinearLayout) v.findViewById(R.id.brandsLayout);
		relBrandLayout = (RelativeLayout) v.findViewById(R.id.relBrandLayout);
		direction = (RelativeLayout) v.findViewById(R.id.direction);
		makeCall = (RelativeLayout) v.findViewById(R.id.call);
		layoutLocationServiceOff = (LinearLayout) v.findViewById(R.id.layoutLocationServiceOff);
		layoutLocationServiceOn = (RelativeLayout) v.findViewById(R.id.layoutLocationServiceOn);
		btnOnLocationService = (WButton) v.findViewById(R.id.buttonLocationOn);
		progressBar = (ProgressBar) v.findViewById(R.id.storesProgressBar);
		progressBar.getIndeterminateDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
		RelativeLayout relNoConnectionLayout = (RelativeLayout) v.findViewById(R.id.no_connection_layout);
		mErrorHandlerView = new ErrorHandlerView(getActivity()
				, relNoConnectionLayout);
		mErrorHandlerView.setMargin(relNoConnectionLayout, 0, 0, 0, 0);
		try {
			mBottomNavigator = (BottomNavigator) getActivity();
			mBottomNavigator.setTitle(getString(R.string.stores_nearby));
			mBottomNavigator.showBackNavigationIcon(true);
			mBottomNavigator.displayToolbar();
		} catch (ClassCastException ex) {
		}
		try {
			unSelectedIcon = BitmapDescriptorFactory.fromResource(R.drawable.unselected_pin);
			selectedIcon = BitmapDescriptorFactory.fromResource(R.drawable.selected_pin);
		} catch (NullPointerException ignored) {
		}
		pager.addOnPageChangeListener(this);
		pager.setOnItemClickListener(new WCustomViewPager.OnItemClickListener() {
			@Override
			public void onItemClick(int position) {
				currentStorePostion = position;
				showStoreDetails(currentStorePostion);

			}
		});

		close.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				backToAllStoresPage(currentStorePostion);
			}
		});
		mLayout.setFadeOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
			}
		});
		mLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
			@Override
			public void onPanelSlide(View panel, float slideOffset) {
				Log.i(TAG, "onPanelSlide, offset " + slideOffset);
				if (slideOffset == 0.0) {
					mLayout.setAnchorPoint(1.0f);
					backToAllStoresPage(currentStorePostion);
				}
			}

			@Override
			public void onPanelStateChanged(final View panel, SlidingUpPanelLayout.PanelState previousState, final SlidingUpPanelLayout.PanelState newState) {
				switch (newState) {
					case COLLAPSED:
							/*
					 * Previous result: Application would exit completely when back button is pressed
                     * New result: Panel just returns to its previous position (Panel collapses)
                     */
						mLayout.setFocusableInTouchMode(true);
						mLayout.setOnKeyListener(new View.OnKeyListener() {
							@Override
							public boolean onKey(View v, int keyCode, KeyEvent event) {
								if (keyCode == KeyEvent.KEYCODE_BACK) {
									mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
									mLayout.setFocusable(false);
									return true;
								}
								return true;
							}
						});
						mBottomNavigator.showBottomNavigationMenu();
						break;

					case DRAGGING:
						mBottomNavigator.hideBottomNavigationMenu();
						break;

					case ANCHORED:

						break;
					default:
						break;

				}
			}
		});

		initLocationCheck();

		/*
		 init();
         */
		btnOnLocationService.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				updateMap = true;
				if (checkLocationPermission()) {
					Intent locIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
					getActivity().startActivity(locIntent);
					getActivity().overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
				} else {
					isLocationServiceButtonClicked = true;
					checkLocationPermission();
				}
			}
		});

		v.findViewById(R.id.btnRetry).setOnClickListener(new View.OnClickListener() {
			@RequiresApi(api = Build.VERSION_CODES.M)
			@Override
			public void onClick(View v) {
				if (new ConnectionDetector().isOnline(getActivity())) {
					mErrorHandlerView.hideErrorHandlerLayout();
					initLocationCheck();
				}
			}
		});

		getActivity().registerReceiver(broadcastCall, new IntentFilter("broadcastCall"));
	}

	public void initLocationCheck() {
		boolean locationServiceIsEnabled = Utils.isLocationServiceEnabled(getActivity());
		boolean lastKnownLocationIsNull = (Utils.getLastSavedLocation(getActivity()) == null);

		if (!locationServiceIsEnabled & lastKnownLocationIsNull) {
			checkLocationServiceAndSetLayout(false);
		} else if (locationServiceIsEnabled && lastKnownLocationIsNull) {
			checkLocationServiceAndSetLayout(true);
			startLocationUpdates();
		} else if (!locationServiceIsEnabled && !lastKnownLocationIsNull) {
			updateMap(Utils.getLastSavedLocation(getActivity()));
		} else {
			startLocationUpdates();
		}
	}

	public void initMap() {
		if (googleMap == null) {
			mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
			mapFragment.getMapAsync(this);
			mMarkers = new HashMap<>();
			markers = new ArrayList<>();
		}
	}

	@RequiresApi(api = Build.VERSION_CODES.M)

	@Override
	public void onMapReady(GoogleMap map) {
		googleMap = map;
		onMapReady();
	}

	private void onMapReady() {
		//If permission is not granted, request permission.
		googleMap.setInfoWindowAdapter(new MapWindowAdapter(getContext()));
		googleMap.setOnMarkerClickListener(mFragment);
		unSelectedIcon = BitmapDescriptorFactory.fromResource(R.drawable.unselected_pin);
		selectedIcon = BitmapDescriptorFactory.fromResource(R.drawable.selected_pin);
	}

	private void drawMarker(LatLng point, BitmapDescriptor bitmapDescriptor, int pos) {
		MarkerOptions markerOptions = new MarkerOptions();
		markerOptions.position(point);
		markerOptions.icon(bitmapDescriptor);
		Marker mkr = googleMap.addMarker(markerOptions);
		mMarkers.put(mkr.getId(), pos);
		markers.add(mkr);
		if (pos == 0) {
			googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mkr.getPosition(), 13), CAMERA_ANIMATION_SPEED, null);
			previousmarker = mkr;
		}
	}

	@Override
	public void onPageSelected(int position) {
		if (previousmarker != null)
			previousmarker.setIcon(unSelectedIcon);
		markers.get(position).setIcon(selectedIcon);
		googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markers.get(position).getPosition(), 13), CAMERA_ANIMATION_SPEED, null);
		previousmarker = markers.get(position);
		/*
		 *InfoWindow shows description above a marker.
         *Make info window invisible to make selected marker come in front of unselected marker.
         */
		previousmarker.showInfoWindow();

	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
	}

	@Override
	public void onPageScrollStateChanged(int state) {
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		try {
			int id = mMarkers.get(marker.getId());
			if (previousmarker != null)
				previousmarker.setIcon(unSelectedIcon);
			marker.setIcon(selectedIcon);
			//googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 13), CAMERA_ANIMATION_SPEED, null);
			previousmarker = marker;
			pager.setCurrentItem(id);
		} catch (NullPointerException ignored) {
		}
		return true;
	}

	private void locationAPIRequest(Location location) {
		if (getActivity() != null) {
			Utils.saveLastLocation(location, getActivity());
			init(location).execute();
			getActivity().invalidateOptionsMenu();
		}
	}

	public void backToAllStoresPage(int position) {
		googleMap.getUiSettings().setScrollGesturesEnabled(true);
		googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markers.get(position).getPosition(), 13), 500, null);
		BottomNavigationActivity.mToolbar.animate().translationY(BottomNavigationActivity.mToolbar.getTop()).setInterpolator(new AccelerateInterpolator()).start();
		showAllMarkers(markers);

	}

	public void showStoreDetails(int position) {
		initStoreDetailsView(storeDetailsList.get(position));
		hideMarkers(markers, position);
		double center = googleMap.getCameraPosition().target.latitude;
		double northmap = googleMap.getProjection().getVisibleRegion().latLngBounds.northeast.latitude;
		double diff = (center - northmap);
		double newLat = markers.get(position).getPosition().latitude + diff / 2.4;
		CameraUpdate centerCam = CameraUpdateFactory.newLatLng(new LatLng(newLat, markers.get(position).getPosition().longitude));
		googleMap.animateCamera(centerCam, CAMERA_ANIMATION_SPEED, null);
		googleMap.getUiSettings().setScrollGesturesEnabled(false);
		if (mLayout.getAnchorPoint() == 1.0f) {
			BottomNavigationActivity.mToolbar.animate().translationY(-BottomNavigationActivity.mToolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
			mLayout.setAnchorPoint(0.7f);
			mLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);

		}

	}

	public void hideMarkers(ArrayList<Marker> marKars, int pos) {
		for (int i = 0; i < marKars.size(); i++) {
			if (i != pos)
				marKars.get(i).setVisible(false);
		}
	}

	public void showAllMarkers(ArrayList<Marker> marKars) {
		for (int i = 0; i < marKars.size(); i++) {
			marKars.get(i).setVisible(true);
		}
	}

	public void bindDataWithUI(List<StoreDetails> storeDetailsList) {
		if (googleMap != null && storeDetailsList.size() >= 0) {
			updateMyCurrentLocationOnMap(mLocation);
			for (int i = 0; i < storeDetailsList.size(); i++) {
				if (i == 0) {
					drawMarker(new LatLng(storeDetailsList.get(i).latitude, storeDetailsList.get(i).longitude), selectedIcon, i);
				} else
					drawMarker(new LatLng(storeDetailsList.get(i).latitude, storeDetailsList.get(i).longitude), unSelectedIcon, i);
			}
			pager.setAdapter(new CardsOnMapAdapter(getActivity(), storeDetailsList));
		}
	}

	public void initStoreDetailsView(final StoreDetails storeDetail) {
		timeingsLayout.removeAllViews();
		brandsLayout.removeAllViews();
		storeName.setText(storeDetail.name);
		storeAddress.setText(storeDetail.address);
		if (storeDetail.phoneNumber != null)
			storeNumber.setText(storeDetail.phoneNumber);
		storeDistance.setText(WFormatter.formatMeter(storeDetail.distance) + getActivity().getResources().getString(R.string.distance_in_km));
		if (storeDetail.offerings != null) {
			storeOfferings.setText(WFormatter.formatOfferingString(getOfferingByType(storeDetail.offerings, "Department")));
			List<StoreOfferings> brandslist = getOfferingByType(storeDetail.offerings, "Brand");
			if (brandslist != null) {
				if (brandslist.size() > 0) {
					WTextView textView;
					relBrandLayout.setVisibility(View.VISIBLE);
					for (int i = 0; i < brandslist.size(); i++) {
						View v = getActivity().getLayoutInflater().inflate(R.layout.opening_hours_textview, null);
						textView = (WTextView) v.findViewById(R.id.openingHours);
						textView.setText(brandslist.get(i).offering);
						brandsLayout.addView(textView);
					}
				} else {
					relBrandLayout.setVisibility(View.GONE);
				}
			} else {
				relBrandLayout.setVisibility(View.GONE);
			}
		} else {
			relBrandLayout.setVisibility(View.GONE);
		}
		if (storeDetail.times != null && storeDetail.times.size() != 0) {
			storeTimingView.setVisibility(View.VISIBLE);
			WTextView textView;
			for (int i = 0; i < storeDetail.times.size(); i++) {
				View v = getActivity().getLayoutInflater().inflate(R.layout.opening_hours_textview, null);
				textView = (WTextView) v.findViewById(R.id.openingHours);
				textView.setText(storeDetail.times.get(i).day + " " + storeDetail.times.get(i).hours);
				if (i == 0)
					textView.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/MyriadPro-Semibold.otf"));
				timeingsLayout.addView(textView);
			}


		} else {
			storeTimingView.setVisibility(View.GONE);
		}

		makeCall.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (storeDetail.phoneNumber != null) {
					Utils.makeCall(getActivity(), storeDetail.phoneNumber);
				}

			}
		});
		direction.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mPopWindowValidationMessage.setmName(storeDetail.name);
				mPopWindowValidationMessage.setmLatitude(storeDetail.latitude);
				mPopWindowValidationMessage.setmLongiude(storeDetail.longitude);
				mPopWindowValidationMessage.displayValidationMessage("",
						PopWindowValidationMessage.OVERLAY_TYPE.STORE_LOCATOR_DIRECTION);
			}
		});
	}


	public HttpAsyncTask<String, String, LocationResponse> init(final Location location) {
		return new HttpAsyncTask<String, String, LocationResponse>() {
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				disableSearchMenu();
				showProgressBar();
				mErrorHandlerView.hideErrorHandlerLayout();
			}

			@Override
			protected LocationResponse httpDoInBackground(String... params) {
				return ((WoolworthsApplication) getActivity().getApplication()).getApi().getLocations(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()), "", "50000");
			}

			@Override
			protected Class<LocationResponse> httpDoInBackgroundReturnType() {
				return LocationResponse.class;
			}

			@Override
			protected LocationResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						enableSearchMenu();
						hideProgressBar();
						Log.d(TAG, "mProgress");
					}
				});

				mErrorHandlerView.networkFailureHandler(errorMessage);
				return new LocationResponse();
			}

			@Override
			protected void onPostExecute(LocationResponse locationResponse) {
				super.onPostExecute(locationResponse);
				enableSearchMenu();
				hideProgressBar();
				storeDetailsList = new ArrayList<>();
				storeDetailsList = locationResponse.Locations;
				if (storeDetailsList != null && storeDetailsList.size() != 0) {
					bindDataWithUI(storeDetailsList);
				}
			}
		};
	}

	public List<StoreOfferings> getOfferingByType(List<StoreOfferings> offerings, String type) {
		List<StoreOfferings> list = new ArrayList<>();
		list.clear();
		for (StoreOfferings d : offerings) {
			if (d.type != null && d.type.contains(type))
				list.add(d);
		}
		return list;
	}

	public void checkLocationServiceAndSetLayout(boolean locationServiceStatus) {
		//Check for location service and Last location
		if (!locationServiceStatus) {
			layoutLocationServiceOn.setVisibility(View.GONE);
			layoutLocationServiceOff.setVisibility(View.VISIBLE);
			navigateMenuState = false;
			getActivity().invalidateOptionsMenu();
		} else {
			layoutLocationServiceOff.setVisibility(View.GONE);
			layoutLocationServiceOn.setVisibility(View.VISIBLE);
			navigateMenuState = true;
			getActivity().invalidateOptionsMenu();

		}
	}

	@RequiresApi(api = Build.VERSION_CODES.M)
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d("RequestSETTINGRESULT", String.valueOf(requestCode));
		switch (requestCode) {
			// Check for the integer request code originally supplied to startResolutionForResult().
			case REQUEST_CHECK_SETTINGS:
				initLocationCheck();
				break;
		}
	}

	public void updateMyCurrentLocationOnMap(Location location) {
		try {
			if (myLocation == null) {
				myLocation = googleMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude()))
						.icon(BitmapDescriptorFactory.fromResource(R.drawable.mapcurrentlocation)));
				googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13), CAMERA_ANIMATION_SPEED, null);

			} else {
				myLocation.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
				googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13), CAMERA_ANIMATION_SPEED, null);
			}
		} catch (Exception ex) {
		}

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.w_store_locator_menu, menu);
		searchMenu = menu.findItem(R.id.action_search).setVisible(true);
		//Disable until finding location
		if (navigateMenuState) {
			menu.findItem(R.id.action_locate).setVisible(true);
		} else {
			menu.findItem(R.id.action_locate).setVisible(false);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_search:
				if (isSearchMenuEnabled)
					startActivity(new Intent(getActivity(), SearchStoresActivity.class));
				break;
			case R.id.action_locate:
				if (Utils.getLastSavedLocation(getActivity()) != null) {
					Location location = Utils.getLastSavedLocation(getActivity());
					zoomToLocation(location);
				}
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void goToUser(CameraPosition mLocation) {
		changeCamera(CameraUpdateFactory.newCameraPosition(mLocation), new GoogleMap.CancelableCallback() {
			@Override
			public void onFinish() {
			}

			@Override
			public void onCancel() {
			}
		});
	}

	/**
	 * Change the camera position by moving or animating the camera depending on the state of the
	 * animate toggle button.
	 */
	private void changeCamera(CameraUpdate update, GoogleMap.CancelableCallback callback) {
		// The duration must be strictly positive so we make it at least 1.
		googleMap.animateCamera(update, Math.max(2000, 1), callback);
	}

	public void showProgressBar() {
		try {
			progressBar.setVisibility(View.VISIBLE);
		} catch (NullPointerException ignored) {
		}
	}

	public void hideProgressBar() {
		if (progressBar != null)
			progressBar.setVisibility(View.GONE);
	}

	public void startLocationUpdates() {
		if (checkLocationPermission()) {
			if (ContextCompat.checkSelfPermission(getActivity(),
					Manifest.permission.ACCESS_FINE_LOCATION)
					== PackageManager.PERMISSION_GRANTED) {
				FusedLocationSingleton.getInstance().startLocationUpdates();
				// register observer for location updates
				LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mLocationUpdated,
						new IntentFilter(FusedLocationSingleton.INTENT_FILTER_LOCATION_UPDATE));
			}
		} else {
			checkLocationPermission();
		}

	}

	public void stopLocationUpdate() {

		// stop location updates
		FusedLocationSingleton.getInstance().stopLocationUpdates();
		// unregister observer
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mLocationUpdated);
	}

	/**
	 * handle new location
	 */
	private BroadcastReceiver mLocationUpdated = new BroadcastReceiver() {
		@RequiresApi(api = Build.VERSION_CODES.M)
		@Override
		public void onReceive(Context context, final Intent intent) {
			try {
				Location location = intent.getParcelableExtra(FusedLocationSingleton.LBM_EVENT_LOCATION_UPDATE);
				mLocation = location;
				updateMap(location);
			} catch (Exception e) {
				Log.e(TAG, e.toString());
			}
		}
	};

	private void updateMap(Location location) {
		if (location != null) {
			Utils.saveLastLocation(location, getActivity());
			checkLocationServiceAndSetLayout(true);
			initMap();
			updateMyCurrentLocationOnMap(location);
			locationAPIRequest(location);
		}
		stopLocationUpdate();
	}

	public boolean checkLocationPermission() {
		if (ContextCompat.checkSelfPermission(getActivity(),
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

	private void zoomToLocation(Location location) {
		CameraPosition mLocation =
				new CameraPosition.Builder().target(new LatLng(location.getLatitude(), location
						.getLongitude()))
						.zoom(13f)
						.bearing(0)
						.tilt(25)
						.build();
		goToUser(mLocation);
	}

	BroadcastReceiver broadcastCall = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			startActivity(callIntent);
		}
	};

	public void unregisterReceiver() {
		try {
			getActivity().unregisterReceiver(broadcastCall);
		} catch (Exception ex) {
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		if (mBottomNavigator != null) {
			mBottomNavigator.removeToolbar();
		}
		unregisterReceiver();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (updateMap) {
			checkLocationServiceAndSetLayout(true);
			initLocationCheck();
			updateMap = false;
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,
										   String permissions[], int[] grantResults) {
		switch (requestCode) {
			case PERMS_REQUEST_CODE: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {

					// permission was granted. Do the
					// contacts-related task you need to do.
					if (ContextCompat.checkSelfPermission(getActivity(),
							Manifest.permission.ACCESS_FINE_LOCATION)
							== PackageManager.PERMISSION_GRANTED) {
						if (isLocationServiceButtonClicked) {
							Intent locIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
							startActivityForResult(locIntent, REQUEST_CHECK_SETTINGS);
							getActivity().overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
						} else {
							startLocationUpdates();
							if (googleMap != null)
								googleMap.setMyLocationEnabled(false);
						}
					}

				} else {

					// Permission denied, Disable the functionality that depends on this permission.
				}
				return;
			}

			case REQUEST_CALL:
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
					startActivity(callIntent);
				break;
			// other 'case' lines to check for other permissions this app might request.
			// You can add here other case statements according to your requirement.
		}
	}

	public void enableSearchMenu() {
		if (searchMenu != null) {
			isSearchMenuEnabled = true;
			searchMenu.getIcon().setAlpha(255);
			if (activityIsNull())
				getActivity().invalidateOptionsMenu();
		}
	}

	public void disableSearchMenu() {
		if (searchMenu != null) {
			isSearchMenuEnabled = false;
			searchMenu.getIcon().setAlpha(130);
			if (activityIsNull())
				getActivity().invalidateOptionsMenu();
		}
	}

	private boolean activityIsNull() {
		return getActivity() != null;
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {
			if (mBottomNavigator != null) {
				mBottomNavigator.setTitle(getString(R.string.stores_nearby));
				mBottomNavigator.showBackNavigationIcon(true);
				mBottomNavigator.displayToolbar();
			}
		}
	}
}
