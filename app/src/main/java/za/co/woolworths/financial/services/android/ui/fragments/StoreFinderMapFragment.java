package za.co.woolworths.financial.services.android.ui.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.awfs.coordination.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.StoreDetails;
import za.co.woolworths.financial.services.android.models.dto.StoreOfferings;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.adapters.StockFinderCardsOnMapAdapter;
import za.co.woolworths.financial.services.android.ui.views.SlidingUpPanelLayout;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.ui.views.maps.DynamicMapDelegate;
import za.co.woolworths.financial.services.android.ui.views.maps.DynamicMapView;
import za.co.woolworths.financial.services.android.ui.views.maps.model.DynamicMapMarker;
import za.co.woolworths.financial.services.android.util.PopWindowValidationMessage;
import za.co.woolworths.financial.services.android.util.UpdateStoreFinderFragment;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WCustomViewPager;
import za.co.woolworths.financial.services.android.util.WFormatter;

public class StoreFinderMapFragment extends Fragment implements DynamicMapDelegate, ViewPager.OnPageChangeListener, UpdateStoreFinderFragment {


	public static final String CARD_CONTACT_INFO = "CARD_CONTACT_INFO";
	private String mCardContactInfo;

	public static StoreFinderMapFragment newInstance(String cardContactInfo) {
		StoreFinderMapFragment myFragment = new StoreFinderMapFragment();
		Bundle args = new Bundle();
		args.putString(CARD_CONTACT_INFO, cardContactInfo);
		myFragment.setArguments(args);
		return myFragment;
	}

	public interface SlidePanelEvent {
		void slidePanelAnchored();

		void slidePanelCollapsed();
	}

	private SlidePanelEvent slidePanelEvent;
	private WGlobalState wGlobalState;
	WCustomViewPager pager;
	static int CAMERA_ANIMATION_SPEED = 350;
	@DrawableRes
	int unSelectedIcon;
	@DrawableRes
	int selectedIcon;
	HashMap<String, Integer> mMarkers;
	ArrayList<DynamicMapMarker> markers;
	DynamicMapMarker previousmarker;
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
	TextView storeName;
	TextView storeOfferings;
	TextView storeAddress;
	TextView storeDistance;
	TextView storeNumber;

	RelativeLayout layoutLocationServiceOn;
	RelativeLayout relBrandLayout;

	DynamicMapView dynamicMapView;

	DynamicMapMarker myLocation;

	private PopWindowValidationMessage mPopWindowValidationMessage;
	private Location mLocation;
	private StoreFinderMapFragment mFragment;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mMarkers = new HashMap<>();
		markers = new ArrayList<>();

		Bundle argument = getArguments();
		if (argument != null)
			mCardContactInfo = getArguments().getString(CARD_CONTACT_INFO, "");

	}

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
			Activity activity = getActivity();
			if (activity != null) {
				slidePanelEvent = (SlidePanelEvent) activity;
			}
		} catch (ClassCastException ignored) {
		}

		dynamicMapView = v.findViewById(R.id.dynamicMapView);
		dynamicMapView.initializeMap(savedInstanceState, this);

		mPopWindowValidationMessage = new PopWindowValidationMessage(getActivity());
		pager = (WCustomViewPager) v.findViewById(R.id.cardPager);
		detailsLayout = (LinearLayout) v.findViewById(R.id.detailsView);
		mLayout = (SlidingUpPanelLayout) v.findViewById(R.id.sliding_layout);
		TextView tvFlStockFinderMapHeader = (TextView) v.findViewById(R.id.flStockFinderMapHeader);
		tvFlStockFinderMapHeader.setVisibility(View.VISIBLE);
		wGlobalState = ((WoolworthsApplication) getActivity().getApplication()).getWGlobalState();
		close = (ImageView) v.findViewById(R.id.close);
		storeName = (TextView) v.findViewById(R.id.storeNameTextView);
		storeOfferings = (TextView) v.findViewById(R.id.offeringsTextView);
		storeDistance = v.findViewById(R.id.distanceTextView);
		storeAddress = (TextView) v.findViewById(R.id.storeAddressTextView);
		storeNumber = (TextView) v.findViewById(R.id.storeNumberTextView);
		timeingsLayout = (LinearLayout) v.findViewById(R.id.timeingsLayout);
		storeTimingView = (RelativeLayout) v.findViewById(R.id.storeTimingView);
		brandsLayout = (LinearLayout) v.findViewById(R.id.brandsLayout);
		relBrandLayout = (RelativeLayout) v.findViewById(R.id.relBrandLayout);
		direction = (RelativeLayout) v.findViewById(R.id.direction);
		makeCall = (RelativeLayout) v.findViewById(R.id.call);
		layoutLocationServiceOn = (RelativeLayout) v.findViewById(R.id.layoutLocationServiceOn);
		try {
			unSelectedIcon = R.drawable.unselected_pin;
			selectedIcon = R.drawable.selected_pin;
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
		getActivity().registerReceiver(broadcastCall, new IntentFilter("broadcastCall"));

		// handle store card
		if (!TextUtils.isEmpty(mCardContactInfo)) {
			tvFlStockFinderMapHeader.setVisibility(View.VISIBLE);
			tvFlStockFinderMapHeader.setText(mCardContactInfo);
			Linkify.addLinks(tvFlStockFinderMapHeader, Linkify.ALL);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		dynamicMapView.onResume();
		Utils.setScreenName(getActivity(), FirebaseManagerAnalyticsProperties.ScreenNames.STORES_NEARBY);
	}

	public void initLocationCheck() {
		Utils.showOneTimePopup(getActivity(), SessionDao.KEY.STORE_FINDER_ONE_TIME_POPUP, CustomPopUpWindow.MODAL_LAYOUT.INSTORE_AVAILABILITY);
	}

	@Override
	public void onMapReady() {
		try {
			storeDetailsList = wGlobalState.getStoreDetailsArrayList();
			if (storeDetailsList != null && storeDetailsList.size() != 0) {
				bindDataWithUI(storeDetailsList);
			}
		} catch (Exception ex) {
			Log.e("onMapReady", "exPMapIsReady");
		}
		unSelectedIcon = R.drawable.unselected_pin;
		selectedIcon = R.drawable.selected_pin;
	}

	private void drawMarker(Double latitude, Double longitude, @DrawableRes int icon, int pos) {
		DynamicMapMarker marker = dynamicMapView.addMarker(getContext(), latitude, longitude, icon);
		if (marker != null) {
			mMarkers.put(marker.getId(), pos);
			markers.add(marker);
			if (pos == 0) {
				dynamicMapView.animateCamera(latitude, longitude, 13f, null, null, CAMERA_ANIMATION_SPEED);
				previousmarker = marker;
			}
		}
	}

	@Override
	public void onPageSelected(int position) {
		if (previousmarker != null)
			previousmarker.setIcon(getContext(), unSelectedIcon);
		markers.get(position).setIcon(getContext(), selectedIcon);
		dynamicMapView.animateCamera(markers.get(position).getPositionLatitude(), markers.get(position).getPositionLongitude(), 13f, null, null, CAMERA_ANIMATION_SPEED);
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
	public void onMarkerClicked(@NonNull DynamicMapMarker marker) {
		try {
			int id = mMarkers.get(marker.getId());
			if (previousmarker != null)
				previousmarker.setIcon(getContext(), unSelectedIcon);
			marker.setIcon(getContext(), selectedIcon);
			previousmarker = marker;
			pager.setCurrentItem(id);
		} catch (NullPointerException ignored) {
		}
	}


	public void backToAllStoresPage(int position) {
		dynamicMapView.setScrollGesturesEnabled(true);
		showAllMarkers(markers);
	}

	public void showStoreDetails(int position) {
		initStoreDetailsView(storeDetailsList.get(position));
		hideMarkers(markers, position);
		dynamicMapView.getCameraPositionTargetLatitude();
		double center = dynamicMapView.getCameraPositionTargetLatitude();
		double northMap = dynamicMapView.getVisibleRegionNortheastLatitude();
		double diff = (center - northMap);
		double newLat = markers.get(position).getPositionLatitude() + diff / 2.4;
		dynamicMapView.animateCamera(newLat, markers.get(position).getPositionLongitude(), CAMERA_ANIMATION_SPEED);
		dynamicMapView.setScrollGesturesEnabled(false);
		if (mLayout.getAnchorPoint() == 1.0f) {
			mLayout.setAnchorPoint(0.7f);
			mLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
		}
	}

	public void hideMarkers(ArrayList<DynamicMapMarker> marKars, int pos) {
		for (int i = 0; i < marKars.size(); i++) {
			if (i != pos)
				marKars.get(i).setVisible(false);
		}
	}

	public void showAllMarkers(ArrayList<DynamicMapMarker> marKars) {
		for (int i = 0; i < marKars.size(); i++) {
			marKars.get(i).setVisible(true);
		}
	}

	public void bindDataWithUI(List<StoreDetails> storeDetailsList) {
		if (dynamicMapView.isMapInstantiated() && storeDetailsList.size() >= 0) {
			updateMyCurrentLocationOnMap(mLocation);
			for (int i = 0; i < storeDetailsList.size(); i++) {
				if (i == 0) {
					drawMarker(storeDetailsList.get(i).latitude, storeDetailsList.get(i).longitude, selectedIcon, i);
				} else
					drawMarker(storeDetailsList.get(i).latitude, storeDetailsList.get(i).longitude, unSelectedIcon, i);
			}
			pager.setAdapter(new StockFinderCardsOnMapAdapter(getActivity(), storeDetailsList));
		}
	}

	public void initStoreDetailsView(final StoreDetails storeDetail) {
		timeingsLayout.removeAllViews();
		brandsLayout.removeAllViews();
		storeName.setText(storeDetail.name);
		storeAddress.setText(TextUtils.isEmpty(storeDetail.address) ? "" : storeDetail.address);
		Utils.setRagRating(getActivity(), storeOfferings, storeDetail.status);
		storeNumber.setText(TextUtils.isEmpty(storeDetail.phoneNumber) ? "" : storeDetail.phoneNumber);
		storeDistance.setText(WFormatter.formatMeter(storeDetail.distance) + getActivity().getResources().getString(R.string.distance_in_km));
		if (storeDetail.offerings != null) {
			List<StoreOfferings> brandslist = getOfferingByType(storeDetail.offerings, "Brand");
			if (brandslist != null) {
				if (brandslist.size() > 0) {
					TextView textView;
					relBrandLayout.setVisibility(View.VISIBLE);
					for (int i = 0; i < brandslist.size(); i++) {
						View v = getActivity().getLayoutInflater().inflate(R.layout.opening_hours_textview, null);
						textView = (TextView) v.findViewById(R.id.openingHoursTextView);
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
			TextView textView;
			for (int i = 0; i < storeDetail.times.size(); i++) {
				View v = getActivity().getLayoutInflater().inflate(R.layout.opening_hours_textview, null);
				textView = (TextView) v.findViewById(R.id.openingHoursTextView);
				textView.setText(storeDetail.times.get(i).day + " " + storeDetail.times.get(i).hours);
				if (i == 0)
					textView.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/OpenSans-SemiBold.ttf"));
				timeingsLayout.addView(textView);
			}


		} else {
			storeTimingView.setVisibility(View.GONE);
		}

		makeCall.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (storeDetail.phoneNumber != null) {
					Utils.makeCall(storeDetail.phoneNumber);
				}

			}
		});
		direction.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (TextUtils.isEmpty(storeDetail.address))
					return;
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

	private void updateMap(Location location, List<StoreDetails> storeDetailsList) {
		this.storeDetailsList = storeDetailsList;
		if (location != null) {
			updateMyCurrentLocationOnMap(location);
		}
	}

	public void updateMyCurrentLocationOnMap(Location location) {
		try {
			if (myLocation == null) {
				myLocation = dynamicMapView.addMarker(getContext(), location.getLatitude(), location.getLongitude(), R.drawable.mapcurrentlocation);
				dynamicMapView.animateCamera(location.getLatitude(), location.getLongitude(), 13f, null, null, CAMERA_ANIMATION_SPEED);
			} else {
				myLocation.setPosition(location.getLatitude(), location.getLongitude());
				dynamicMapView.animateCamera(location.getLatitude(), location.getLongitude(), 13f, null, null, CAMERA_ANIMATION_SPEED);
			}
		} catch (Exception ex) {
		}
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
	public void onDestroyView() {
		dynamicMapView.onDestroy();
		super.onDestroyView();
	}

	public void update(Location location, List<StoreDetails> storeDetail) {
		updateMap(location, storeDetail);
	}

	@Override
	public void onFragmentUpdate() {

	}

	@Override
	public void onPause() {
		dynamicMapView.onPause();
		super.onPause();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		dynamicMapView.onLowMemory();
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		dynamicMapView.onSaveInstanceState(outState);
	}
}
