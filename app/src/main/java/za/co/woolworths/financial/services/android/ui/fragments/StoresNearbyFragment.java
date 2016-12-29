package za.co.woolworths.financial.services.android.ui.fragments;


import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.awfs.coordination.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;

import za.co.woolworths.financial.services.android.ui.activities.StoreLocatorActivity;
import za.co.woolworths.financial.services.android.util.LocationTracker;
import za.co.woolworths.financial.services.android.util.WCustomViewPager;




public class StoresNearbyFragment extends Fragment implements OnMapReadyCallback,ViewPager.OnPageChangeListener,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,GoogleMap.OnMarkerClickListener{


    WCustomViewPager pager;
    GoogleMap googleMap;
    double[] lats={-33.953951,-33.971435,-33.959966};
    double[] longs={18.488592,18.465048,18.470638};
    static  int CAMERA_ANIMATION_SPEED=350;
    BitmapDescriptor unSelectedIcon;
    BitmapDescriptor selectedIcon;
    int lastSelectedLocationPos=0;
    HashMap<String, Integer> mMarkers;
    ArrayList<Marker> markers;
    Marker previousmarker;
    LocationTracker lTracker;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LinearLayout detailsLayout;
    SupportMapFragment mapFragment;
    ImageView close;
    int currentStorePostion=0;
    public StoresNearbyFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.stores_nearby_fragment,container,false);
       pager=(WCustomViewPager)v.findViewById(R.id.cardPager);
        detailsLayout=(LinearLayout)v.findViewById(R.id.detailsView);
        close=(ImageView)v.findViewById(R.id.close);
        pager.setAdapter(new CustomAdapter());
        pager.addOnPageChangeListener(this);
        lTracker=new LocationTracker(getActivity());
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

        detailsLayout.setOnTouchListener(new View.OnTouchListener() {
            private long startClickTime;
            private float x1;
            private float y1;
            private float x2;
            private float y2;
            private float dx;
            private float dy;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case(MotionEvent.ACTION_DOWN):
                        x1 = event.getX();
                        y1 = event.getY();
                        break;
                    case(MotionEvent.ACTION_UP): {
                        x2 = event.getX();
                        y2 = event.getY();
                        dx = x2-x1;
                        dy = y2-y1;

                        if(dy>0)
                        {
                          backToAllStoresPage(currentStorePostion);
                        }

                    }
                }
                return true;
            }
        });
        initMap();
        return v;
    }


    public void initMap()
    {
        if(googleMap==null)
        {
            mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            unSelectedIcon= BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE);
            selectedIcon=BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
            mMarkers = new HashMap<String, Integer>();
            markers=new ArrayList<Marker>();
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap=map;
        googleMap.setMyLocationEnabled(true);
        googleMap.setOnMarkerClickListener(this);
        map.getUiSettings().setMyLocationButtonEnabled(false);
       /* CameraPosition cameraPosition = new CameraPosition.Builder().target(
                new LatLng(lats[0], longs[0])).zoom(13).build();

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));*/
        if(lats.length>0)
        {
            for (int i=0;i<lats.length;i++)
            {
                if (i==0)
                    drawMarker(new LatLng(lats[i],longs[i]),selectedIcon,i);
                else
                    drawMarker(new LatLng(lats[i],longs[i]),unSelectedIcon,i);
            }


        }
    }
    private void drawMarker(LatLng point,BitmapDescriptor bitmapDescriptor,int pos){



        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(point);
        markerOptions.icon(bitmapDescriptor);
        Marker mkr= googleMap.addMarker(markerOptions);
        mMarkers.put(mkr.getId(),pos);
        markers.add(mkr);

        dropPinEffect(mkr);
        if(pos==0)
        {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mkr.getPosition(),13),CAMERA_ANIMATION_SPEED,null);

            previousmarker=mkr;
        }

    }

    @Override
    public void onPageSelected(int position) {

        if(previousmarker!=null)
           previousmarker.setIcon(unSelectedIcon);

        markers.get(position).setIcon(selectedIcon);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markers.get(position).getPosition(),13),CAMERA_ANIMATION_SPEED,null);
        previousmarker=markers.get(position);

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        int id=mMarkers.get(marker.getId());
        if(previousmarker!=null)
            previousmarker.setIcon(unSelectedIcon);

        marker.setIcon(selectedIcon);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(),13),CAMERA_ANIMATION_SPEED,null);
        previousmarker=marker;

        pager.setCurrentItem(id);
        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

      /*  if(!lTracker.canGetLocation())
        {

            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if(mLastLocation!=null)
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()),13));



        }
        else
        {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lTracker.getLatitude(),lTracker.getLongitude()),13));

        }*/

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

    public class  CustomAdapter extends PagerAdapter
    {

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

            container.removeView((View) object);
        }



        @Override
        public Object instantiateItem(ViewGroup container, final int position) {

            View cView=getActivity().getLayoutInflater().inflate(R.layout.store_nearby_item,container,false);
            cView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                   // startActivity(new Intent(getActivity(), StoreDetailsActivity.class));
                    //getActivity().overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
                    currentStorePostion=position;
                    showStoreDetails(currentStorePostion);

                }
            });
            container.addView(cView);
            return cView;
        }



        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public float getPageWidth(int position) {
            return 0.9f;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {
            super.restoreState(state, loader);
        }

        @Override
        public Parcelable saveState() {
            return null;
        }
    }

    public void SlideUP(View view,Context context)
    {
        view.setVisibility(View.VISIBLE);
        view.startAnimation(AnimationUtils.loadAnimation(context,
                R.anim.slid_up));

    }
    public void SlideUPGone(View view,Context context)
    {
        view.setVisibility(View.GONE);
        view.startAnimation(AnimationUtils.loadAnimation(context,
                R.anim.slid_up));

    }

    public void SlideDown(View view,Context context)
    {
        view.startAnimation(AnimationUtils.loadAnimation(context,
                R.anim.slid_down));
        view.setVisibility(View.GONE);
    }

    public void backToAllStoresPage(int position)
    {
        SlideDown(detailsLayout,getActivity());
        SlideUP(pager,getActivity());
        showAllMarkers(markers);
        googleMap.getUiSettings().setScrollGesturesEnabled(true);
        StoreLocatorActivity.toolbar.animate().translationY(StoreLocatorActivity.toolbar.getTop()).setInterpolator(new AccelerateInterpolator()).start();


        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markers.get(position).getPosition(),13),CAMERA_ANIMATION_SPEED,null);
    }
    public void showStoreDetails(int position)
    {
        SlideDown(pager,getActivity());
        SlideUP(detailsLayout,getActivity());
        hideMarkers(markers,position);

        double center = googleMap.getCameraPosition().target.latitude;
        double northmap = googleMap.getProjection().getVisibleRegion().latLngBounds.northeast.latitude;
        double diff = (center - northmap);

        double newLat = markers.get(position).getPosition().latitude + diff/1.5;
        CameraUpdate centerCam = CameraUpdateFactory.newLatLng(new LatLng(newLat,  markers.get(position).getPosition().longitude));

        googleMap.animateCamera(centerCam,CAMERA_ANIMATION_SPEED,null);
        googleMap.getUiSettings().setScrollGesturesEnabled(false);
        //((AppCompatActivity)getActivity()).getSupportActionBar().hide();
        StoreLocatorActivity.toolbar.animate().translationY(-StoreLocatorActivity.toolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();

    }

      public  void hideMarkers(ArrayList<Marker> marKars,int pos)
      {
          for (int i=0;i<marKars.size();i++)
          {
              if(i!=pos)
                  marKars.get(i).setVisible(false);
          }
      }

     public void showAllMarkers(ArrayList<Marker> marKars)
     {
         for (int i=0;i<marKars.size();i++)
         {

                 marKars.get(i).setVisible(true);
         }
     }
    private void dropPinEffect(final Marker marker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final long duration = 1400;

        final Interpolator interpolator = new BounceInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = Math.max(
                        1 - interpolator.getInterpolation((float) elapsed
                                / duration), 0);
                marker.setAnchor(0.5f, 1.0f + 14 * t);

                if (t > 0.0) {
                    // Post again 15ms later.
                    handler.postDelayed(this, 5);
                } else {
                    marker.showInfoWindow();

                }
            }
        });
    }
}
