package za.co.woolworths.financial.services.android.ui.activities;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.models.dto.StoreDetails;
import za.co.woolworths.financial.services.android.models.dto.StoreOfferings;
import za.co.woolworths.financial.services.android.ui.views.SlidingUpPanelLayout;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.PopWindowValidationMessage;
import za.co.woolworths.financial.services.android.util.SpannableMenuOption;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WFormatter;

public class StoreDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {
	private static final int REQUEST_CALL = 1;
	GoogleMap googleMap;
	public Toolbar toolbar;
	StoreDetails storeDetails;
	private String TAG = this.getClass().getSimpleName();
	//Detail page Views
	LinearLayout detailsLayout;
	LinearLayout timeingsLayout;
	LinearLayout brandsLayout;

	RelativeLayout direction;
	RelativeLayout makeCall;
	RelativeLayout relBrandLayout;
	RelativeLayout storeTimingView;
	WTextView storeName;
	WTextView storeOfferings;
	WTextView storeAddress;
	WTextView storeDistance;
	WTextView storeNumber;
	WTextView nativeMap;
	WTextView cancel;
	ImageView closePage;

	LinearLayout mapLayout;
	private SlidingUpPanelLayout mLayout;

	Intent callIntent;
	private PopWindowValidationMessage mPopWindowValidationMessage;
	private boolean isFromStockLocator;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.updateStatusBarBackground(this);
		setContentView(R.layout.store_details_activity);

		mPopWindowValidationMessage = new PopWindowValidationMessage(this);
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		storeName = (WTextView) findViewById(R.id.storeName);
		storeOfferings = (WTextView) findViewById(R.id.offerings);
		storeDistance = (WTextView) findViewById(R.id.distance);
		storeAddress = (WTextView) findViewById(R.id.storeAddress);
		timeingsLayout = (LinearLayout) findViewById(R.id.timeingsLayout);
		storeTimingView = (RelativeLayout) findViewById(R.id.storeTimingView);
		brandsLayout = (LinearLayout) findViewById(R.id.brandsLayout);
		mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
		mapLayout = (LinearLayout) findViewById(R.id.mapLayout);
		closePage = (ImageView) findViewById(R.id.closePage);
		//getting height of device
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		int height = displaymetrics.heightPixels;
		int width = displaymetrics.widthPixels;
		//set height of map view to 3/10 of the screen height
		mapLayout.setLayoutParams(new SlidingUpPanelLayout.LayoutParams(width, (height * 3) / 10));
		//set height of store details view to 7/10 of the screen height
		mLayout.setPanelHeight((height * 7) / 10);

		direction = (RelativeLayout) findViewById(R.id.direction);
		storeNumber = (WTextView) findViewById(R.id.storeNumber);
		makeCall = (RelativeLayout) findViewById(R.id.call);
		relBrandLayout = (RelativeLayout) findViewById(R.id.relBrandLayout);
		Gson gson = new Gson();
		storeDetails = gson.fromJson(getIntent().getStringExtra("store"), StoreDetails.class);
		isFromStockLocator = getIntent().getBooleanExtra("FromStockLocator", false);
		initStoreDetailsView(storeDetails);

		closePage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				overridePendingTransition(R.anim.stay, R.anim.slide_down_anim);
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
		initMap();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Utils.setScreenName(this, FirebaseManagerAnalyticsProperties.ScreenNames.STORE_DETAILS);
	}

	@Override
	public void onMapReady(GoogleMap map) {
		googleMap = map;
		googleMap.getUiSettings().setScrollGesturesEnabled(false);
		googleMap.setMyLocationEnabled(false);
		centerCamera();
	}

	public void centerCamera() {
		googleMap.addMarker(new MarkerOptions().position(new LatLng(storeDetails.latitude, storeDetails.longitude))
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.selected_pin)));
		CameraPosition cameraPosition = new CameraPosition.Builder().target(
				new LatLng(storeDetails.latitude, storeDetails.longitude)).zoom(13).build();
		googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
	}

	public void initMap() {
		if (googleMap == null) {
			SupportMapFragment mapFragment = (SupportMapFragment) this.getSupportFragmentManager().findFragmentById(R.id.map);
			mapFragment.getMapAsync(this);

		}
	}

	@Override
	public void onBackPressed() {
		StoreDetailsActivity.this.finish();
		overridePendingTransition(R.anim.stay, R.anim.slide_down_anim);
	}

	public void initStoreDetailsView(final StoreDetails storeDetail) {
		timeingsLayout.removeAllViews();
		brandsLayout.removeAllViews();
		storeName.setText(storeDetail.name);
		storeAddress.setText(TextUtils.isEmpty(storeDetail.address) ? "" : storeDetail.address);
		if (storeDetail.phoneNumber != null)
			storeNumber.setText(storeDetail.phoneNumber);
		SpannableMenuOption spannableMenuOption = new SpannableMenuOption(this);
		storeDistance.setText(WFormatter.formatMeter(storeDetail.distance) + getResources().getString(R.string.distance_in_km));
		Resources resources = getResources();
		if (isFromStockLocator) {
			Utils.setRagRating(StoreDetailsActivity.this, storeOfferings, storeDetails.status);
		} else {
			if (storeDetail.offerings != null) {
				storeOfferings.setText(WFormatter.formatOfferingString(getOfferingByType(storeDetail.offerings, "Department")));
			}
		}
		if (storeDetail.offerings != null) {
			List<StoreOfferings> brandslist = getOfferingByType(storeDetail.offerings, "Brand");
			if (brandslist != null) {
				if (brandslist.size() > 0) {
					WTextView textView;
					relBrandLayout.setVisibility(View.VISIBLE);
					for (int i = 0; i < brandslist.size(); i++) {
						View v = getLayoutInflater().inflate(R.layout.opening_hours_textview, null);
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
		WTextView textView;
		if (storeDetail.times != null && storeDetail.times.size() != 0) {
			storeTimingView.setVisibility(View.VISIBLE);
			for (int i = 0; i < storeDetail.times.size(); i++) {
				View v = getLayoutInflater().inflate(R.layout.opening_hours_textview, null);
				textView = (WTextView) v.findViewById(R.id.openingHours);
				textView.setText(storeDetail.times.get(i).day + " " + storeDetail.times.get(i).hours);
				if (i == 0)
					textView.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/MyriadPro-Semibold.otf"));
				timeingsLayout.addView(textView);
			}
		} else {
			storeTimingView.setVisibility(View.GONE);
		}

		makeCall.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (storeDetail.phoneNumber != null) {
					Utils.makeCall(StoreDetailsActivity.this, storeDetail.phoneNumber);
				}

			}
		});
		direction.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (TextUtils.isEmpty(storeDetail.address))
					return;
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
				} else {
					////
				}
		}
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
		}
		return super.onOptionsItemSelected(item);
	}

//    public void openNativeMapWindow(final double lat, final double lon) {
//        //darken the current screen
//        View view = getLayoutInflater().inflate(R.layout.open_nativemaps_layout, null);
//        RelativeLayout relPopContainer = (RelativeLayout) view.findViewById(R.id.relPopContainer);
//        final PopupWindow mDarkenScreen = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        mDarkenScreen.setAnimationStyle(R.style.Darken_Screen);
//        mDarkenScreen.showAtLocation(view, Gravity.CENTER, 0, 0);
//        mDarkenScreen.setTouchable(true);
//        mDarkenScreen.setFocusable(false);
//        mDarkenScreen.setOutsideTouchable(true);
//        mDarkenScreen.setBackgroundDrawable (new ColorDrawable());
//        //Then popup window appears
//        final View popupView = getLayoutInflater().inflate(R.layout.popup_view, null);
//        nativeMap = (WTextView) popupView.findViewById(R.id.nativeGoogleMap);
//        cancel = (WTextView) popupView.findViewById(R.id.cancel);
//        final PopupWindow mPopWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        mPopWindow.setAnimationStyle(R.style.Animations_popup);
//        mPopWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
//        mPopWindow.setOutsideTouchable(true);
//        //Dismiss popup when touch outside
//        mPopWindow.setTouchable(false);
//        mPopWindow.setBackgroundDrawable (new ColorDrawable());
//
//        cancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mPopWindow.dismiss();
//                mDarkenScreen.dismiss();
//            }
//        });
//
//        popupView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mPopWindow.dismiss();
//                mDarkenScreen.dismiss();
//            }
//        });
//
//        nativeMap.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String uri = String.format(Locale.ENGLISH,"","http://maps.google.com/maps?daddr=%f,%f (%s)", lat, lon, "");
//                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
//                intent.setClassName("com.google.android.apps.maps","com.google.android.maps.MapsActivity");
//                startActivity(intent);
//                mPopWindow.dismiss();
//                mDarkenScreen.dismiss();
//            }
//        });
//    }

	public List<StoreOfferings> getOfferingByType(List<StoreOfferings> offerings, String type) {
		List<StoreOfferings> list = new ArrayList<>();
		list.clear();
		for (StoreOfferings d : offerings) {
			if (d.type != null && d.type.contains(type))
				list.add(d);
		}
		return list;
	}


}
