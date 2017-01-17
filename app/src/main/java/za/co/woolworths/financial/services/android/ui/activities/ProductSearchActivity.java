package za.co.woolworths.financial.services.android.ui.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import com.awfs.coordination.R;
import com.daimajia.swipe.util.Attributes;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.Product;
import za.co.woolworths.financial.services.android.models.dto.Product_;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.SearchHistory;
import za.co.woolworths.financial.services.android.ui.adapters.ProductListAdapter;
import za.co.woolworths.financial.services.android.ui.views.WEditTextView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.FontHyperTextParser;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.SharePreferenceHelper;
import za.co.woolworths.financial.services.android.util.SlidingUpViewLayout;
import za.co.woolworths.financial.services.android.util.Utils;

public class ProductSearchActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, LocationListener {
    public RecyclerView productListview;
    public LinearLayoutManager mLayoutManager;
    public Toolbar toolbar;
    public SwipeRefreshLayout swipeRefreshLayout;
    public static final int PAGE_SIZE = 20;
    private boolean mIsLoading = false;
    private boolean mIsLastPage = false;
    private int mCurrentPage = 1;
    private ConnectionDetector connectionDetector;
    private SharePreferenceHelper mSharePreferenceHelper;
    private LayoutInflater mLayoutInflater;
    private SlidingUpViewLayout mSlidingUpViewLayout;
    private ArrayList<Product_> productList;
    private ProductListAdapter mProductListAdapter;
    private List<Product_> moreProductList;
    private String searchProductBrand;
    private WEditTextView mEditSearchProduct;
    private WTextView mTextNoProductFound;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private SearchView searchView;
    private LinearLayout recentSearchList;
    private WTextView recentSearchListitem;
    private LinearLayout recentSearchLayout;
    private SearchHistory search;
    private LocationRequest mLocationRequest;
    private LatLng latLng;

    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private int REQUEST_LOCATION = 1;
    private long UPDATE_INTERVAL = 10 * 1000;
    private long FASTEST_INTERVAL = 1 * 1000;
    private ProgressDialog mSearchProductDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_search_activity);
        Utils.updateStatusBarBackground(this);
        setActionBar();
        initUI();
        connectionDetector = new ConnectionDetector();
        mSharePreferenceHelper = SharePreferenceHelper.getInstance(this);
        mLayoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mSlidingUpViewLayout = new SlidingUpViewLayout(this, mLayoutInflater);
        setRecycleListView();
        showRecentSearchHistoryView(true);
        editProduct();
        scrollListener();
        getLocation();
    }

    private void getLocation() {
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            // Create the LocationRequest object
            mLocationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(UPDATE_INTERVAL)        // 10 seconds, in milliseconds
                    .setFastestInterval(FASTEST_INTERVAL); // 1 second, in milliseconds
        }
    }

    private void initUI() {
        mLayoutManager = new LinearLayoutManager(ProductSearchActivity.this);
        productListview = (RecyclerView) findViewById(R.id.productSearchList);
        mEditSearchProduct = (WEditTextView) findViewById(R.id.toolbarText);
        mTextNoProductFound = (WTextView) findViewById(R.id.textNoProductFound);
        recentSearchLayout = (LinearLayout) findViewById(R.id.recentSearchLayout);
    }

    private void setActionBar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
    }

    private void scrollListener() {
        productListview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = mLayoutManager.getChildCount();
                int totalItemCount = mLayoutManager.getItemCount();
                int firstVisibleItemPosition = mLayoutManager.findFirstVisibleItemPosition();
                if (!mIsLoading && !mIsLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= PAGE_SIZE) {
                        getMoreProductRequest(searchProductBrand);
                    }
                }
            }
        });
    }

    public void editProduct() {

        mEditSearchProduct.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length()==0){
                    showRecentSearchHistoryView(true);
                    productListview.setVisibility(View.GONE);
                }else{
                    showRecentSearchHistoryView(false);
                    productListview.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    private void setRecycleListView() {
        productListview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideSoftKeyboard();
                return false;
            }
        });
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeToRefresh);
        recentSearchList = (LinearLayout) findViewById(R.id.recentSearchList);
        productListview.setHasFixedSize(true);
        productListview.setLayoutManager(mLayoutManager);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                if (connectionDetector.isOnline(ProductSearchActivity.this)) {
                    getProductRequest(searchProductBrand);
                } else {
                    mSlidingUpViewLayout.openOverlayView(getString(R.string.connect_to_server),
                            SlidingUpViewLayout.OVERLAY_TYPE.ERROR);
                    hideRefreshView();
                }
            }
        });
    }

    private void hideRefreshView() {
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    public void getProductRequest(final String query) {

        new HttpAsyncTask<String, String, Product>() {
            @Override
            protected Product httpDoInBackground(String... params) {
                Location location = Utils.getLastSavedLocation(ProductSearchActivity.this);
                LatLng location1 = new LatLng(-29.79, 31.0833);
                return ((WoolworthsApplication) getApplication()).getApi()
                        .getProductSearchList(query,
                                location1, false, mCurrentPage, PAGE_SIZE);
            }

            @Override
            protected Product httpError(String errorMessage, HttpErrorCode httpErrorCode) {
                Product product = new Product();
                return product;
            }

            @Override
            protected void onPostExecute(Product product) {
                super.onPostExecute(product);
                productList = null;
                productList = new ArrayList<>();
                productList = product.products;
                if (productList != null) {
                    if (product.products.size() > 0) {
                        bindDataWithUI(productList);
                        mIsLastPage = false;
                        mCurrentPage = 1;
                        mIsLoading = false;
                        showRecentSearchHistoryView(false);
                        hideNoProductFound();
                    } else {
                        showRecentSearchHistoryView(true);
                        showNoProductFound();
                    }
                } else {
                    showNoProductFound();
                }
                hideRefreshView();
                hideProgressDialog();
            }

            @Override
            protected void onPreExecute() {
                mSearchProductDialog = new ProgressDialog(ProductSearchActivity.this);
                mSearchProductDialog.setMessage(FontHyperTextParser.getSpannable(getString(R.string.loading), 1, ProductSearchActivity.this));
                mSearchProductDialog.setCancelable(false);
                mSearchProductDialog.show();
                saveCurrentSearch(query);
                super.onPreExecute();
            }

            @Override
            protected Class<Product> httpDoInBackgroundReturnType() {
                return Product.class;
            }
        }.execute();
    }

    public void bindDataWithUI(List<Product_> product_list) {
        mProductListAdapter = new ProductListAdapter(ProductSearchActivity.this, product_list);
        mProductListAdapter.setMode(Attributes.Mode.Single);
        productListview.setAdapter(mProductListAdapter);
    }

    public void getMoreProductRequest(final String query) {
        new HttpAsyncTask<String, String, Product>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mIsLoading = true;
                mCurrentPage += 1;
            }

            @Override
            protected Product httpDoInBackground(String... params) {
                Location loc = Utils.getLastSavedLocation(ProductSearchActivity.this);
                LatLng location = new LatLng(-29.79, 31.0833);
                return ((WoolworthsApplication) getApplication()).getApi()
                        .getProductSearchList(query,
                                location, false, mCurrentPage, PAGE_SIZE);
            }

            @Override
            protected Class<Product> httpDoInBackgroundReturnType() {
                return Product.class;
            }

            @Override
            protected Product httpError(String errorMessage, HttpErrorCode httpErrorCode) {
                Product productResponse = new Product();
                productResponse.response = new Response();
                mIsLoading = false;
                return productResponse;
            }

            @Override
            protected void onPostExecute(Product productResponse) {
                super.onPostExecute(productResponse);
                mIsLoading = false;
                List<Product_> product_ = null;
                product_ = new ArrayList<>();
                moreProductList = productResponse.products;
                if (moreProductList != null && moreProductList.size() != 0) {
                    if (moreProductList.size() < PAGE_SIZE) {
                        mIsLastPage = true;
                    }
                    productList.addAll(moreProductList);
                    mProductListAdapter.notifyDataSetChanged();
                }
            }
        }.execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        canGoBack();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                canGoBack();
                return true;
            case R.id.action_search:
                searchProductBrand = mEditSearchProduct.getText().toString();
                if (!TextUtils.isEmpty(searchProductBrand)) {
                    if (productList != null) {
                        productList.clear();
                    }
                    productListview.setVisibility(View.VISIBLE);
                    showRecentSearchHistoryView(false);
                    getProductRequest(searchProductBrand);
                } else {
                    productListview.setVisibility(View.GONE);
                    showRecentSearchHistoryView(true);
                }
                break;
        }
        return false;
    }

    private void showNoProductFound() {
        mTextNoProductFound.setVisibility(View.VISIBLE);
        productListview.setVisibility(View.GONE);
    }

    private void hideNoProductFound() {
        mTextNoProductFound.setVisibility(View.GONE);
        productListview.setVisibility(View.VISIBLE);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.e("onConnected==", "connected");
        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)        // 10 seconds, in milliseconds
                .setFastestInterval(FASTEST_INTERVAL); // 1 second, in milliseconds

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);


            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } else {
            handleNewLocation(location);
        }

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            // mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
            // mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
            Log.e("==mLastLocation==", String.valueOf(mLastLocation));
        }


    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e("==mLastLocatisus", String.valueOf(i));

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //Log.e("==mLastLocatifail", connectionResult.getErrorMessage().toString());
//                /*
//         * Google Play services can resolve some errors it detects.
//         * If the error has a resolution, try sending an Intent to
//         * start a Google Play services activity that can resolve
//         * error.
//         */
//        if (connectionResult.hasResolution()) {
//            try {
//                // Start an Activity that tries to resolve the error
//                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
//                /*
//                 * Thrown if Google Play services canceled the original
//                 * PendingIntent
//                 */
//            } catch (IntentSender.SendIntentException e) {
//                // Log the error
//                Log.e("mmSendIntentException", String.valueOf(e));
//            }
//        } else {
//            /*
//             * If no resolution is available, display a dialog to the
//             * user with the error.
//             */
//            Log.i("TAG", "Location services connection failed with code " + connectionResult.getErrorCode());
//        }

    }

    protected void onStart() {
        if (!mGoogleApiClient.isConnecting() || !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_item, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void saveRecentSearch(SearchHistory searchHistory) {
        List<SearchHistory> histories = null;
        histories = new ArrayList<>();
        histories = getRecentSearch();
        SessionDao sessionDao = new SessionDao(ProductSearchActivity.this);
        sessionDao.key = SessionDao.KEY.STORES_PRODUCT_SEARCH;
        Gson gson = new Gson();
        boolean isExist = false;
        if (histories == null) {
            histories = new ArrayList<>();
            histories.add(0, searchHistory);
            String json = gson.toJson(histories);
            sessionDao.value = json;
            try {
                sessionDao.save();
            } catch (Exception e) {
                Log.e("TAG", e.getMessage());
            }
        } else {
            for (SearchHistory s : histories) {
                if (s.searchedValue.equalsIgnoreCase(searchHistory.searchedValue)) {
                    isExist = true;
                }
            }
            if (!isExist) {
                histories.add(0, searchHistory);
                if (histories.size() > 5)
                    histories.remove(5);

                String json = gson.toJson(histories);
                sessionDao.value = json;
                try {
                    sessionDao.save();
                } catch (Exception e) {
                    Log.e("TAG", e.getMessage());
                }
            }
        }
    }

    public List<SearchHistory> getRecentSearch() {
        List<SearchHistory> historyList = null;
        try {
            SessionDao sessionDao = new SessionDao(ProductSearchActivity.this, SessionDao.KEY.STORES_PRODUCT_SEARCH).get();
            if (sessionDao.value == null) {
                historyList = new ArrayList<>();
            } else {
                Gson gson = new Gson();
                Type type = new TypeToken<List<SearchHistory>>() {
                }.getType();
                historyList = gson.fromJson(sessionDao.value, type);
            }
        } catch (Exception e) {
            Log.e("TAG", e.getMessage());
        }
        return historyList;
    }

    public void hideSoftKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    public void showRecentSearchHistoryView(boolean status) {
        recentSearchList.removeAllViews();
        View storeItem = getLayoutInflater().inflate(R.layout.stores_recent_search_header_row, null);
        recentSearchList.addView(storeItem);
        List<SearchHistory> searchHistories = getRecentSearch();
        if (status && searchHistories != null) {
            for (int i = 0; i < searchHistories.size(); i++) {
                View v = getLayoutInflater().inflate(R.layout.recent_search_list_item, null);
                recentSearchListitem = (WTextView) v.findViewById(R.id.recentSerachListItem);
                recentSearchListitem.setText(searchHistories.get(i).searchedValue);
                recentSearchList.addView(v);
                int position = recentSearchList.indexOfChild(v) - 1;
                v.setTag(position);
                v.setOnClickListener(this);
            }
            recentSearchLayout.setVisibility(View.VISIBLE);
        } else {
            recentSearchLayout.setVisibility(View.GONE);
        }
    }

    private void canGoBack() {
        this.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public void onClick(View v) {
        int pos = (Integer) v.getTag();
        showRecentSearchHistoryView(false);
        searchProductBrand = getRecentSearch().get(pos).searchedValue;
        mEditSearchProduct.setText(searchProductBrand);
        mEditSearchProduct.setSelection(searchProductBrand.length());
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    private void handleNewLocation(Location location) {
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        latLng = new LatLng(currentLatitude, currentLongitude);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.e("Req Code", "" + requestCode);

        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // We can now safely use the API we requested access to
                Log.e("TEST", "testdata");
            } else {
                // Permission was denied or request was cancelled
            }
        }

    }

    // Trigger new location updates at interval
    protected void startLocationUpdates() {
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
    }

    public void saveCurrentSearch(String query) {
        if (!TextUtils.isEmpty(query)) {
            search = new SearchHistory();
            search.searchedValue = query;
            saveRecentSearch(search);
        }
    }

    public void hideProgressDialog (){
        if (mSearchProductDialog!=null){
            if (mSearchProductDialog.isShowing()){
                mSearchProductDialog.dismiss();
            }
        }
    }

}
