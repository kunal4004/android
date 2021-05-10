package za.co.woolworths.financial.services.android.ui.activities.vtc;


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
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension;

public class SelectStoreDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int REQUEST_CALL = 1;
    GoogleMap googleMap;
    public Toolbar toolbar;
    StoreDetails storeDetails;
    private final String TAG = this.getClass().getSimpleName();
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
    TextView selectStoreBtn;
    WTextView nativeMap;
    WTextView cancel;
    ImageView closePage;

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
        setContentView(R.layout.select_store_details_activity);

        mPopWindowValidationMessage = new PopWindowValidationMessage(this);
        toolbar = findViewById(R.id.toolbar);
        storeName = findViewById(R.id.storeNameTextView);
        storeOfferings = findViewById(R.id.offeringsTextView);
        storeDistance = findViewById(R.id.distanceTextView);
        storeAddress = findViewById(R.id.storeAddressTextView);
        timeingsLayout = findViewById(R.id.timeingsLayout);
        storeTimingView = findViewById(R.id.storeTimingView);
        brandsLayout = findViewById(R.id.brandsLayout);
        mLayout = findViewById(R.id.sliding_layout);
        mapLayout = findViewById(R.id.mapLayout);
        closePage = findViewById(R.id.closePage);
        //getting height of device
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;
        int width = displaymetrics.widthPixels;
        //set height of map view to 3/10 of the screen height
        mapLayout.setLayoutParams(new SlidingUpPanelLayout.LayoutParams(width, (height * 3) / 10));
        //set height of store details view to 7/10 of the screen height
        mLayout.setPanelHeight((height * 7) / 10);

        direction = findViewById(R.id.direction);
        storeNumber = findViewById(R.id.storeNumberTextView);
        makeCall = findViewById(R.id.call);
        relBrandLayout = findViewById(R.id.relBrandLayout);
        selectStoreBtn = findViewById(R.id.selectStoreTextViewBtn);
        AnimationUtilExtension.Companion.animateViewPushDown(selectStoreBtn);

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
        SelectStoreDetailsActivity.this.finish();
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
            Utils.setRagRating(SelectStoreDetailsActivity.this, storeOfferings, storeDetails.status);
        } else {
            if (storeDetail.offerings != null) {
                storeOfferings.setText(WFormatter.formatOfferingString(storeDetail.offerings));
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
                        textView = v.findViewById(R.id.openingHoursTextView);
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
                textView = v.findViewById(R.id.openingHoursTextView);
                textView.setText(storeDetail.times.get(i).day + " " + storeDetail.times.get(i).hours);
                if (i == 0)
                    textView.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/MyriadPro-Semibold.otf"));
                timeingsLayout.addView(textView);
            }
        } else {
            storeTimingView.setVisibility(View.GONE);
        }

        makeCall.setOnClickListener(v -> {
            if (storeDetail.phoneNumber != null) {
                Utils.makeCall(storeDetail.phoneNumber);
            }

        });
        direction.setOnClickListener(v -> {
            if (TextUtils.isEmpty(storeDetail.address))
                return;
            mPopWindowValidationMessage.setmLatitude(storeDetail.latitude);
            mPopWindowValidationMessage.setmLongiude(storeDetail.longitude);
            mPopWindowValidationMessage.displayValidationMessage("",
                    PopWindowValidationMessage.OVERLAY_TYPE.STORE_LOCATOR_DIRECTION);
        });

        selectStoreBtn.setOnClickListener(v -> {
            navigateToConfirmStore();
        });
    }

    private void navigateToConfirmStore() {
        Intent confirmStore = new Intent(this, ConfirmStoreActivity.class);
        confirmStore.putExtra("store", new Gson().toJson(storeDetails));
        startActivity(confirmStore);
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
