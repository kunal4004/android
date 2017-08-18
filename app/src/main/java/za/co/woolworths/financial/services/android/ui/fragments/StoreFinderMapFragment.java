package za.co.woolworths.financial.services.android.ui.fragments;

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
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
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

import za.co.woolworths.financial.services.android.models.dto.StoreDetails;
import za.co.woolworths.financial.services.android.models.dto.StoreOfferings;
import za.co.woolworths.financial.services.android.ui.activities.WOneAppBaseActivity;
import za.co.woolworths.financial.services.android.ui.adapters.MapWindowAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.StockFinderCardsOnMapAdapter;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.ui.views.SlidingUpPanelLayout;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.PopWindowValidationMessage;
import za.co.woolworths.financial.services.android.util.UpdateStoreFinderFragment;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WCustomViewPager;
import za.co.woolworths.financial.services.android.util.WFormatter;

public class StoreFinderMapFragment extends Fragment implements OnMapReadyCallback, ViewPager.OnPageChangeListener, GoogleMap.OnMarkerClickListener, UpdateStoreFinderFragment {

	public static StoreFinderListFragment newInstance(int myValue) {
		// You can add as many values as you need to initialize your fragment
		StoreFinderListFragment fragment = new StoreFinderListFragment();
		Bundle args = new Bundle();
		args.putInt("value_key", myValue);
		fragment.setArguments(args);
		return fragment;
	}

	public interface SlidePanelEvent {
		void slidePanelAnchored();

		void slidePanelCollapsed();
	}

	private SlidePanelEvent slidePanelEvent;

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

	ProgressBar mStoreProgressBar;

	RelativeLayout layoutLocationServiceOn;
	RelativeLayout relBrandLayout;

	protected static final int REQUEST_CHECK_SETTINGS = 99;
	Marker myLocation;
	public static final int PERMS_REQUEST_CODE = 123;

	private PopWindowValidationMessage mPopWindowValidationMessage;
	private ErrorHandlerView mErrorHandlerView;
	private Location mLocation;
	private StoreFinderMapFragment mFragment;
	public boolean isLocationServiceButtonClicked = false, mapReceiveUpdate = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		return inflater.inflate(R.layout.store_finder_map_activity, container, false);
	}

	@RequiresApi(api = Build.VERSION_CODES.M)
	@Override
	public void onViewCreated(View v, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(v, savedInstanceState);
		mFragment = this;

		try {
			slidePanelEvent = (SlidePanelEvent) getActivity();
		} catch (ClassCastException ex) {
			Log.e("initInterface", ex.toString());
		}

		mPopWindowValidationMessage = new PopWindowValidationMessage(getActivity());
		pager = (WCustomViewPager) v.findViewById(R.id.cardPager);
		detailsLayout = (LinearLayout) v.findViewById(R.id.detailsView);
		mLayout = (SlidingUpPanelLayout) v.findViewById(R.id.sliding_layout);
		WTextView tvFlStockFinderMapHeader = (WTextView) v.findViewById(R.id.flStockFinderMapHeader);
		tvFlStockFinderMapHeader.setVisibility(View.VISIBLE);
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
		layoutLocationServiceOn = (RelativeLayout) v.findViewById(R.id.layoutLocationServiceOn);
		mStoreProgressBar = (ProgressBar) v.findViewById(R.id.storesProgressBar);
		mStoreProgressBar.getIndeterminateDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
		mErrorHandlerView = new ErrorHandlerView(getActivity()
				, (RelativeLayout) v.findViewById(R.id.no_connection_layout));
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
				if (slideOffset == 0.0) {
					mLayout.setAnchorPoint(1.0f);
					backToAllStoresPage(currentStorePostion);
				}
			}

			@Override
			public void onPanelStateChanged(final View panel, SlidingUpPanelLayout.PanelState previousState, final SlidingUpPanelLayout.PanelState newState) {
				switch (newState) {
					case ANCHORED:
						slidePanelEvent.slidePanelAnchored();
						break;

					case COLLAPSED:
						slidePanelEvent.slidePanelCollapsed();
						break;

					case EXPANDED:
						break;
					default:
						break;
				}

				if (newState != SlidingUpPanelLayout.PanelState.COLLAPSED) {
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
				}
			}
		});

		initLocationCheck();

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

		showProgressBar();
	}

	public void initLocationCheck() {
		boolean locationServiceIsEnabled = Utils.isLocationServiceEnabled(getActivity());
		boolean lastKnownLocationIsNull = (Utils.getLastSavedLocation(getActivity()) == null);

		if (!locationServiceIsEnabled & lastKnownLocationIsNull) {
			checkLocationServiceAndSetLayout(false);
		} else if (locationServiceIsEnabled && lastKnownLocationIsNull) {
			checkLocationServiceAndSetLayout(true);
			//	startLocationUpdates();
		} else if (!locationServiceIsEnabled && !lastKnownLocationIsNull) {
			updateMap(Utils.getLastSavedLocation(getActivity()));
		} else {
			///startLocationUpdates();
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
		try {
			if (storeDetailsList != null && storeDetailsList.size() != 0) {
				bindDataWithUI(storeDetailsList);
			}
		} catch (Exception ex) {
			Log.e("onMapReady", "exPMapIsReady");
		}
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

	public void backToAllStoresPage(int position) {
		googleMap.getUiSettings().setScrollGesturesEnabled(true);
		//googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markers.get(position).getPosition(), 13), 500, null);
		WOneAppBaseActivity.mToolbar.animate().translationY(WOneAppBaseActivity.mToolbar.getTop()).setInterpolator(new AccelerateInterpolator()).start();
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
			WOneAppBaseActivity.mToolbar.animate().translationY(-WOneAppBaseActivity.mToolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
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
			pager.setAdapter(new StockFinderCardsOnMapAdapter(getActivity(), storeDetailsList));
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
			getActivity().invalidateOptionsMenu();
		} else {
			layoutLocationServiceOn.setVisibility(View.VISIBLE);
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
			mStoreProgressBar.setVisibility(View.VISIBLE);
		} catch (NullPointerException ignored) {
		}
	}

	public void hideProgressBar() {
		if (mStoreProgressBar != null)
			mStoreProgressBar.setVisibility(View.GONE);
	}


	private void updateMap(Location location) {
		if (location != null) {
			initMap();
			updateMyCurrentLocationOnMap(location);

			//locationAPIRequest(location);
		}
		///stopLocationUpdate();
	}

	private void updateMap(Location location, List<StoreDetails> storeDetailsList) {
		this.storeDetailsList = storeDetailsList;
		if (location != null) {
			initMap();
			updateMyCurrentLocationOnMap(location);
		}
	}

	public void updateMyCurrentLocationOnMap(Location location) {
		try {
			if (myLocation == null) {
				myLocation = googleMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude()))
						.icon(BitmapDescriptorFactory.fromResource(R.drawable.mapcurrentlocation)));
				googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13), CAMERA_ANIMATION_SPEED, null);
				//	zoomToLocation(mLocation);
			} else {
				myLocation.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
				googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13), CAMERA_ANIMATION_SPEED, null);
			}
		} catch (Exception ex) {
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
		unregisterReceiver();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver();
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
							//startLocationUpdates();
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

	public void update(Location location, List<StoreDetails> storeDetail) {
		updateMap(location, storeDetail);
	}

	@Override
	public void onFragmentUpdate(final Location location, final List<StoreDetails> storeDetails) {
		try {
			StoreFinderMapFragment.this.getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (storeDetails.size() > 0) {
						if (!mapReceiveUpdate) {
							updateMap(location, storeDetails);
							mapReceiveUpdate = true;
						}
					} else {

					}
					hideProgressBar();
				}
			});
		} catch (NullPointerException ignored) {
		}
	}


}
