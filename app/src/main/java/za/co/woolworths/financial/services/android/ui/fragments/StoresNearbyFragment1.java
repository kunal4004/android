package za.co.woolworths.financial.services.android.ui.fragments;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
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
import android.widget.RelativeLayout;

import com.awfs.coordination.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
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
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.StoreDetails;
import za.co.woolworths.financial.services.android.models.dto.StoreOfferings;
import za.co.woolworths.financial.services.android.ui.activities.SearchStoresActivity;
import za.co.woolworths.financial.services.android.ui.activities.WOneAppBaseActivity;
import za.co.woolworths.financial.services.android.ui.adapters.CardsOnMapAdapter;
import za.co.woolworths.financial.services.android.ui.views.SlidingUpPanelLayout;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.LocationTracker;
import za.co.woolworths.financial.services.android.util.PopWindowValidationMessage;
import za.co.woolworths.financial.services.android.util.SpannableMenuOption;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WCustomViewPager;
import za.co.woolworths.financial.services.android.util.WFormatter;

import static android.content.Context.LOCATION_SERVICE;
import static com.google.android.gms.wearable.DataMap.TAG;

public class StoresNearbyFragment1 extends Fragment implements OnMapReadyCallback, ViewPager.OnPageChangeListener, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMarkerClickListener {

    private static final int REQUEST_CALL = 1;
    WCustomViewPager pager;
    GoogleMap googleMap;
    static int CAMERA_ANIMATION_SPEED = 350;
    BitmapDescriptor unSelectedIcon;
    BitmapDescriptor selectedIcon;
    HashMap<String, Integer> mMarkers;
    ArrayList<Marker> markers;
    Marker previousmarker;
    LocationTracker lTracker;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    SupportMapFragment mapFragment;
    ImageView close;
    int currentStorePostion = 0;
    private SlidingUpPanelLayout mLayout;
    public List<StoreDetails> storeDetailsList;

    Intent callIntent;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    RelativeLayout direction;
    RelativeLayout makeCall;

    //Detail page Views
    LinearLayout detailsLayout;
    LinearLayout timeingsLayout;
    LinearLayout brandsLayout;
    WTextView storeName;
    WTextView storeOfferings;
    WTextView storeAddress;
    WTextView storeDistance;
    WTextView storeNumber;

    WTextView nativeMap;
    WTextView cancel;


    //Location Service Layouts
    LinearLayout layoutLocationServiceOff;
    RelativeLayout layoutLocationServiceOn;
    RelativeLayout relBrandLayout;

    WButton btnOnLocationService;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    private final int DURATION = 2000;
    private boolean updateMap = false;
    //Location Listner
    private LocationManager locationManager;
    private String provider;
    Marker myLocation;
    private Status status;
    private PopWindowValidationMessage mPopWindowValidationMessage;

    public StoresNearbyFragment1() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_stores_nearby1, container, false);
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
        brandsLayout = (LinearLayout) v.findViewById(R.id.brandsLayout);
        relBrandLayout = (RelativeLayout) v.findViewById(R.id.relBrandLayout);
        direction = (RelativeLayout) v.findViewById(R.id.direction);
        makeCall = (RelativeLayout) v.findViewById(R.id.call);
        layoutLocationServiceOff = (LinearLayout) v.findViewById(R.id.layoutLocationServiceOff);
        layoutLocationServiceOn = (RelativeLayout) v.findViewById(R.id.layoutLocationServiceOn);
        btnOnLocationService = (WButton) v.findViewById(R.id.buttonLocationOn);
        // Chcek of location Service Enable
        //  checkLocationServiceAndSetLayout(Utils.isLocationServiceEnabled(getActivity()));
        locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        //  locationManager.requestLocationUpdates(provider, 40000, 10, this);
        pager.addOnPageChangeListener(this);
        pager.setOnItemClickListener(new WCustomViewPager.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                currentStorePostion = position;
                showStoreDetails(currentStorePostion);

            }
        });
        lTracker = new LocationTracker(getActivity());
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
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
                Log.i(TAG, "onPanelStateChanged " + newState);

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
        if (Utils.isLocationServiceEnabled(getActivity()) && Utils.getLastSavedLocation(getActivity()) == null) {
            checkLocationServiceAndSetLayout(false);
        }
        settingsrequest();
        initMap();

/*
        init();
*/
        btnOnLocationService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateMap = true;
                Intent locIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                getActivity().startActivity(locIntent);
                getActivity().overridePendingTransition(0, 0);
            }
        });
        return v;
    }

    public void initMap() {
        if (googleMap == null) {
            mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            mMarkers = new HashMap<>();
            markers = new ArrayList<>();
        }
    }

    //    Function to request permission
    public void requestLocationPermission() {
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_CODE_ASK_PERMISSIONS);
        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_ASK_PERMISSIONS);

    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        //If permission is not granted, request permission.
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
        } else {
            googleMap.setMyLocationEnabled(false);
            googleMap.setOnMarkerClickListener(this);
            unSelectedIcon = BitmapDescriptorFactory.fromResource(R.drawable.unselected_pin);
            selectedIcon = BitmapDescriptorFactory.fromResource(R.drawable.selected_pin);
            //Current location
        }


/*
        googleMap.addMarker(new MarkerOptions().position(new LatLng(lTracker.getLatitude(), lTracker.getLongitude()))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.mapcurrentlocation)));*/
        // map.getUiSettings().setMyLocationButtonEnabled(false);
       /* CameraPosition cameraPosition = new CameraPosition.Builder().target(
                new LatLng(lats[0], longs[0])).zoom(13).build();

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));*/
    }

    private void drawMarker(LatLng point, BitmapDescriptor bitmapDescriptor, int pos) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(point);
        markerOptions.icon(bitmapDescriptor);
        Marker mkr = googleMap.addMarker(markerOptions);
        mMarkers.put(mkr.getId(), pos);
        markers.add(mkr);
        // dropPinEffect(mkr);
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }
    //Location Listner

    @Override
    public void onLocationChanged(Location location) {
        if (getActivity() != null) {
            Utils.saveLastLocation(location, getActivity());
            updateMyCurrentLocationOnMap(location);
            init(location);
            //If permission is not granted, request permission.
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestLocationPermission();
            } else {
                locationManager.removeUpdates(this);
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    public void backToAllStoresPage(int position) {
        googleMap.getUiSettings().setScrollGesturesEnabled(true);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markers.get(position).getPosition(), 13), 500, null);
        WOneAppBaseActivity.appbar.animate().translationY(WOneAppBaseActivity.appbar.getTop()).setInterpolator(new AccelerateInterpolator()).start();
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
            WOneAppBaseActivity.appbar.animate().translationY(-WOneAppBaseActivity.appbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
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
            for (int i = 0; i < storeDetailsList.size(); i++) {
                if (i == 0)
                    drawMarker(new LatLng(storeDetailsList.get(i).latitude, storeDetailsList.get(i).longitude), selectedIcon, i);
                else
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
        SpannableMenuOption spannableMenuOption = new SpannableMenuOption(getActivity());
        storeDistance.setText(spannableMenuOption.distanceKm(WFormatter.formatMeter(storeDetail.distance)));
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
        if (storeDetail.times != null) {
            WTextView textView;
            for (int i = 0; i < storeDetail.times.size(); i++) {
                View v = getActivity().getLayoutInflater().inflate(R.layout.opening_hours_textview, null);
                textView = (WTextView) v.findViewById(R.id.openingHours);
                textView.setText(storeDetail.times.get(i).day + " " + storeDetail.times.get(i).hours);
                if (i == 0)
                    textView.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/MyriadPro-Semibold.otf"));
                timeingsLayout.addView(textView);
            }

        }
        makeCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (storeDetail.phoneNumber != null) {
                    callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + storeDetail.phoneNumber));
                    //Check for permission before calling
                    //The app will ask permission before calling only on first use after installation
                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL);
                    } else {
                        startActivity(callIntent);
                    }
                }

            }
        });
        direction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopWindowValidationMessage.setmLatitude(storeDetail.latitude);
                mPopWindowValidationMessage.setmLongiude(storeDetail.longitude);
                mPopWindowValidationMessage.displayValidationMessage("",
                        PopWindowValidationMessage.OVERLAY_TYPE.STORE_LOCATOR_DIRECTION);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CALL:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivity(callIntent);
                }
        }
    }

    public void init(final Location location) {
        new HttpAsyncTask<String, String, LocationResponse>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
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
                LocationResponse locationResponse = new LocationResponse();
                locationResponse.response = new Response();
                return locationResponse;
            }

            @Override
            protected void onPostExecute(LocationResponse locationResponse) {
                super.onPostExecute(locationResponse);
                storeDetailsList = new ArrayList<>();
                storeDetailsList = locationResponse.Locations;
                if (storeDetailsList != null && storeDetailsList.size() != 0) {
                    bindDataWithUI(storeDetailsList);
                }
            }
        }.execute();


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

        } else {
            layoutLocationServiceOff.setVisibility(View.GONE);
            layoutLocationServiceOn.setVisibility(View.VISIBLE);
        }
    }

    public void settingsrequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true); //this is the key ingredient
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        if (Utils.getLastSavedLocation(getActivity()) != null) {
                            Location location = Utils.getLastSavedLocation(getActivity());
                            updateMyCurrentLocationOnMap(location);
                        }
                        searchForCurrentLocation();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        if (Utils.getLastSavedLocation(getActivity()) == null) {
                            checkLocationServiceAndSetLayout(false);
                            try {
                                status.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException e) {
                                ;
                            }

                        } else {
                            onLocationChanged(Utils.getLastSavedLocation(getActivity()));
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("RequestSETTINGRESULT", String.valueOf(requestCode));
        switch (requestCode) {
// Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // startLocationUpdates();
                        searchForCurrentLocation();
                        break;
                    case Activity.RESULT_CANCELED:
                        settingsrequest();//keep asking if imp or do whatever
                        break;
                }
                break;
        }
    }

    public void searchForCurrentLocation() {
        checkLocationServiceAndSetLayout(true);
        //If permission is not granted, request permission.
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
        } else {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 4000, 1, this);
        }
    }

    public void updateMyCurrentLocationOnMap(Location location) {
        if (myLocation == null) {
            myLocation = googleMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude()))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.mapcurrentlocation)));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13), CAMERA_ANIMATION_SPEED, null);

        } else {
            myLocation.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13), CAMERA_ANIMATION_SPEED, null);
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.w_store_locator_menu, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                startActivity(new Intent(getActivity(), SearchStoresActivity.class));
                break;
            case R.id.action_locate:
                if (Utils.getLastSavedLocation(getActivity()) != null) {
                    Location location = Utils.getLastSavedLocation(getActivity());
                    CameraPosition mLocation =
                            new CameraPosition.Builder().target(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .zoom(13f)
                                    .bearing(0)
                                    .tilt(25)
                                    .build();
                    goToUser(mLocation);
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
        googleMap.animateCamera(update, Math.max(DURATION, 1), callback);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (updateMap) {
            // All location settings are satisfied. The client can initialize location
            // requests here.
            if (Utils.getLastSavedLocation(getActivity()) != null) {
                Location location = Utils.getLastSavedLocation(getActivity());
                updateMyCurrentLocationOnMap(location);
                searchForCurrentLocation();
            }
        }
    }
}

