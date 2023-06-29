package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.awfs.coordination.R;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.models.dto.StoreDetails;
import za.co.woolworths.financial.services.android.models.dto.StoreOfferings;
import za.co.woolworths.financial.services.android.ui.views.SlidingUpPanelLayout;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.ui.views.maps.DynamicMapDelegate;
import za.co.woolworths.financial.services.android.ui.views.maps.DynamicMapView;
import za.co.woolworths.financial.services.android.ui.views.maps.model.DynamicMapMarker;
import za.co.woolworths.financial.services.android.util.PopWindowValidationMessage;
import za.co.woolworths.financial.services.android.util.SpannableMenuOption;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WFormatter;

public class StoreDetailsActivity extends AppCompatActivity implements DynamicMapDelegate {
    private static final int REQUEST_CALL = 1;

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
    TextView storeName;
    TextView storeOfferings;
    TextView storeAddress;
    TextView storeDistance;
    TextView storeNumber;
    WTextView nativeMap;
    WTextView cancel;
    ImageView closePage;
    DynamicMapView dynamicMapView;

    LinearLayout mapLayout;
    private SlidingUpPanelLayout mLayout;

    Intent callIntent;
    private PopWindowValidationMessage mPopWindowValidationMessage;
    private boolean isFromStockLocator;
    private boolean mShouldDisplayBackIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.updateStatusBarBackground(this);
        setContentView(R.layout.store_details_activity);

        dynamicMapView = findViewById(R.id.dynamicMapView);
        dynamicMapView.initializeMap(savedInstanceState, this);

        mPopWindowValidationMessage = new PopWindowValidationMessage(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        storeName = (TextView) findViewById(R.id.storeNameTextView);
        storeOfferings = (TextView) findViewById(R.id.offeringsTextView);
        storeDistance = findViewById(R.id.distanceTextView);
        storeAddress = (TextView) findViewById(R.id.storeAddressTextView);
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
        storeNumber = (TextView) findViewById(R.id.storeNumberTextView);
        makeCall = (RelativeLayout) findViewById(R.id.call);
        relBrandLayout = (RelativeLayout) findViewById(R.id.relBrandLayout);
        Gson gson = new Gson();
        storeDetails = gson.fromJson(getIntent().getStringExtra("store"), StoreDetails.class);
        isFromStockLocator = getIntent().getBooleanExtra("FromStockLocator", false);
        mShouldDisplayBackIcon = getIntent().getBooleanExtra("SHOULD_DISPLAY_BACK_ICON", false);
        initStoreDetailsView(storeDetails);

        if (mShouldDisplayBackIcon) {
            closePage.setImageResource(R.drawable.back_button_circular_icon);
            closePage.setRotation(180);
        }

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
                if (slideOffset == 0.0) {
                    mLayout.setAnchorPoint(1.0f);
                }
            }

            @Override
            public void onPanelStateChanged(final View panel, SlidingUpPanelLayout.PanelState previousState, final SlidingUpPanelLayout.PanelState newState) {

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        dynamicMapView.onResume();
        Utils.setScreenName(this, FirebaseManagerAnalyticsProperties.ScreenNames.STORE_DETAILS);
    }

    @Override
    public void onMapReady() {
        dynamicMapView.setScrollGesturesEnabled(false);
        dynamicMapView.setMyLocationEnabled(false);
        centerCamera();
    }

    public void centerCamera() {
        dynamicMapView.addMarker(this, storeDetails.latitude, storeDetails.longitude, R.drawable.selected_pin);
        dynamicMapView.animateCamera(storeDetails.latitude, storeDetails.longitude, 13);
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
                    TextView textView;
                    relBrandLayout.setVisibility(View.VISIBLE);
                    for (int i = 0; i < brandslist.size(); i++) {
                        View v = getLayoutInflater().inflate(R.layout.opening_hours_textview, null);
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
        TextView textView;
        if (storeDetail.times != null && storeDetail.times.size() != 0) {
            storeTimingView.setVisibility(View.VISIBLE);
            for (int i = 0; i < storeDetail.times.size(); i++) {
                View v = getLayoutInflater().inflate(R.layout.opening_hours_textview, null);
                textView = (TextView) v.findViewById(R.id.openingHoursTextView);
                textView.setText(storeDetail.times.get(i).day + " " + storeDetail.times.get(i).hours);
                if (i == 0)
                    textView.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/OpenSans-SemiBold.ttf"));
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

    @Override
    public void onMarkerClicked(@NonNull DynamicMapMarker marker) { }

    @Override
    protected void onDestroy() {
        dynamicMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        dynamicMapView.onPause();
        super.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        dynamicMapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        dynamicMapView.onSaveInstanceState(outState);
    }
}
