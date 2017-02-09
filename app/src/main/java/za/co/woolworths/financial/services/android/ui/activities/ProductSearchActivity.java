package za.co.woolworths.financial.services.android.ui.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.awfs.coordination.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.SearchHistory;
import za.co.woolworths.financial.services.android.ui.views.WEditTextView;
import za.co.woolworths.financial.services.android.ui.views.WProgressDialogFragment;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.BaseActivity;
import za.co.woolworths.financial.services.android.util.Const;
import za.co.woolworths.financial.services.android.util.FusedLocationSingleton;
import za.co.woolworths.financial.services.android.util.PopWindowValidationMessage;
import za.co.woolworths.financial.services.android.util.Utils;

public class ProductSearchActivity extends BaseActivity
        implements View.OnClickListener {
    public RecyclerView productListview;
    public LinearLayoutManager mLayoutManager;
    public Toolbar toolbar;
    private String searchProductBrand;
    private WEditTextView mEditSearchProduct;
    private WTextView mTextNoProductFound;
    private LinearLayout recentSearchLayout;
    private static final int PERMS_REQUEST_CODE = 1234;
    private LinearLayout recentSearchList;
    private boolean permissionIsAllowed = false;
    private LatLng mLocation;

    PopWindowValidationMessage mPopWindowValidationMessage;
    private WProgressDialogFragment mGetProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_search_activity);
        Utils.updateStatusBarBackground(this);
        setActionBar();
        initUI();
        mLocation = new LatLng(0, 0);
        showRecentSearchHistoryView(true);
        if (hasPermissions()) {
            startLocationUpdate();
        } else {
            requestPerms();
        }
    }

    private void initUI() {
        mGetProgressDialog = WProgressDialogFragment.newInstance("v");
        mGetProgressDialog.setCancelable(true);
        mLayoutManager = new LinearLayoutManager(ProductSearchActivity.this);
        productListview = (RecyclerView) findViewById(R.id.productSearchList);
        mEditSearchProduct = (WEditTextView) findViewById(R.id.toolbarText);
        mTextNoProductFound = (WTextView) findViewById(R.id.textNoProductFound);
        recentSearchLayout = (LinearLayout) findViewById(R.id.recentSearchLayout);
        recentSearchList = (LinearLayout) findViewById(R.id.recentSearchList);
        mPopWindowValidationMessage = new PopWindowValidationMessage(this);
    }

    private void setActionBar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_search);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // stop location updates
        try {
            FusedLocationSingleton.getInstance().stopLocationUpdates();
            // unregister observer
            LocalBroadcastManager.getInstance(ProductSearchActivity.this).unregisterReceiver(mLocationUpdated);
        } catch (NullPointerException ex) {
            Log.e("onPauseFusedLoc", ex.toString());
        }
    }

    private void startLocationUpdate() {
        // start location updates
        FusedLocationSingleton.getInstance().startLocationUpdates();
        // register observer for location updates
        LocalBroadcastManager.getInstance(ProductSearchActivity.this).registerReceiver(mLocationUpdated,
                new IntentFilter(Const.INTENT_FILTER_LOCATION_UPDATE));
    }

    @Override
    public void onBackPressed() {
        canGoBack();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                searchProduct();
                return true;
            case R.id.action_search:
                canGoBack();
                break;
        }
        return false;
    }

    private void searchProduct() {
        searchProductBrand = mEditSearchProduct.getText().toString();
        if (searchProductBrand.length() > 2) {
            SearchHistory search = new SearchHistory();
            search.searchedValue = searchProductBrand;
            saveRecentSearch(search);
            Intent searchProduct = new Intent(ProductSearchActivity.this, ProductViewActivity.class);
            searchProduct.putExtra("searchProduct", searchProductBrand);
            startActivity(searchProduct);
        }
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

                sessionDao.value = gson.toJson(histories);
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
                WTextView recentSearchListitem = (WTextView) v.findViewById(R.id.recentSerachListItem);
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
        searchProduct();
    }

    /***********************************************************************************************
     * local broadcast receiver
     **********************************************************************************************/
    /**
     * handle new location
     */
    private BroadcastReceiver mLocationUpdated = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                Location location = intent.getParcelableExtra(Const.LBM_EVENT_LOCATION_UPDATE);
                mLocation = new LatLng(location.getLatitude(), location.getLongitude());
            } catch (NullPointerException e) {
                mLocation = new LatLng(0, 0);
            }
        }
    };

    public boolean hasPermissions() {
        int res;
        //string array of permissions,
        String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION};

        for (String perms : permissions) {
            res = checkCallingOrSelfPermission(perms);
            if (!(res == PackageManager.PERMISSION_GRANTED)) {
                return false;
            }
        }
        return true;
    }

    private void requestPerms() {
        String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, PERMS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionIsAllowed = true;
        switch (requestCode) {
            case PERMS_REQUEST_CODE:
                for (int res : grantResults) {
                    // if user granted all permissions.
                    permissionIsAllowed = permissionIsAllowed && (res == PackageManager.PERMISSION_GRANTED);
                }
                break;
            default:
                // if user not granted permissions.
                permissionIsAllowed = false;
                break;
        }
        if (permissionIsAllowed) {
            //user granted all permissions we can perform our task.
            startLocationUpdate();
            mEditSearchProduct.setFocusable(true);
        } else {
            // we will give warning to user that they haven't granted permissions.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
                        && shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Toast.makeText(this, "Location Permissions denied.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        showRecentSearchHistoryView(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideSoftKeyboard();
    }
}