package za.co.woolworths.financial.services.android.ui.activities;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Switch;

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
import java.util.Locale;

import za.co.woolworths.financial.services.android.models.dto.StoreDetails;
import za.co.woolworths.financial.services.android.models.dto.StoreOfferings;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.PopWindowValidationMessage;
import za.co.woolworths.financial.services.android.util.SpannableMenuOption;
import za.co.woolworths.financial.services.android.util.WFormatter;

public class StoreDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int REQUEST_CALL = 1;
    GoogleMap googleMap;
    public Toolbar toolbar;
    StoreDetails storeDetails;

    //Detail page Views
    LinearLayout detailsLayout;
    LinearLayout timeingsLayout;
    LinearLayout brandsLayout;

    RelativeLayout direction;
    RelativeLayout makeCall;
    RelativeLayout relBrandLayout;
    WTextView storeName;
    WTextView storeOfferings;
    WTextView storeAddress;
    WTextView storeDistance;
    WTextView storeNumber;
    WTextView nativeMap;
    WTextView cancel;

    Intent callIntent;
    private PopWindowValidationMessage mPopWindowValidationMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.white));
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        setContentView(R.layout.store_details_activity);

        mPopWindowValidationMessage = new PopWindowValidationMessage(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        storeName = (WTextView) findViewById(R.id.storeName);
        storeOfferings = (WTextView) findViewById(R.id.offerings);
        storeDistance = (WTextView) findViewById(R.id.distance);
        storeAddress = (WTextView) findViewById(R.id.storeAddress);
        timeingsLayout = (LinearLayout) findViewById(R.id.timeingsLayout);
        brandsLayout = (LinearLayout) findViewById(R.id.brandsLayout);
        direction = (RelativeLayout) findViewById(R.id.direction);
        storeNumber = (WTextView) findViewById(R.id.storeNumber);
        makeCall = (RelativeLayout) findViewById(R.id.call);
        relBrandLayout = (RelativeLayout) findViewById(R.id.relBrandLayout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);
        Gson gson = new Gson();
        storeDetails = gson.fromJson(getIntent().getStringExtra("store"), StoreDetails.class);
        initStoreDetailsView(storeDetails);
        initMap();
       /* direction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNativeMapWindow();
            }
        });*/
        /*makeCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:1234567584"));
                startActivity(callIntent);
            }
        });*/

    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        googleMap.setMyLocationEnabled(false);
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
        //Expected result: Switch to previous state
        //Previous result: The app exits
        //New Result: Switches back to previous state
        super.onBackPressed();
        overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);
        this.finish();
    }

    public void initStoreDetailsView(final StoreDetails storeDetail) {
        timeingsLayout.removeAllViews();
        brandsLayout.removeAllViews();
        storeName.setText(storeDetail.name);
        storeAddress.setText(storeDetail.address);
        if (storeDetail.phoneNumber != null)
            storeNumber.setText(storeDetail.phoneNumber);
        SpannableMenuOption spannableMenuOption = new SpannableMenuOption(this);
        storeDistance.setText(spannableMenuOption.distanceKm(WFormatter.formatMeter(storeDetail.distance)));
        if (storeDetail.offerings != null) {
            storeOfferings.setText(WFormatter.formatOfferingString(getOfferingByType(storeDetail.offerings, "Department")));
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
        if (storeDetail.times != null) {
            for (int i = 0; i < storeDetail.times.size(); i++) {
                View v = getLayoutInflater().inflate(R.layout.opening_hours_textview, null);
                textView = (WTextView) v.findViewById(R.id.openingHours);
                textView.setText(storeDetail.times.get(i).day + " " + storeDetail.times.get(i).hours);
                if (i == 0)
                    textView.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/MyriadPro-Semibold.otf"));
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
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(StoreDetailsActivity.this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL);
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
              //  openNativeMapWindow(storeDetail.latitude,storeDetail.longitude);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case REQUEST_CALL:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
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

    public List<StoreOfferings> getOfferingByType(List<StoreOfferings> offerings, String type)
    {
        List<StoreOfferings> list=new ArrayList<>();
        list.clear();
        for(StoreOfferings d : offerings){
            if(d.type != null && d.type.contains(type))
                list.add(d);
        }
        return list;
    }


}
