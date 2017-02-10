package za.co.woolworths.financial.services.android.ui.activities;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.awfs.coordination.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.ProductView;
import za.co.woolworths.financial.services.android.models.dto.WProduct;
import za.co.woolworths.financial.services.android.models.dto.WProductDetail;
import za.co.woolworths.financial.services.android.ui.views.WLoanEditTextView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.Const;
import za.co.woolworths.financial.services.android.util.FusedLocationSingleton;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.PopWindowValidationMessage;

public class EnterBarcodeActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private WLoanEditTextView mEditBarcodeNumber;
    private ProgressBar mProgressBar;
    private LatLng mLocation;
    public static int PAGE_SIZE = 20;
    private static final int PERMS_REQUEST_CODE = 1234;
    private WTextView mTextInfo;
    private PopWindowValidationMessage mPopWindowValidationMessage;
    private EnterBarcodeActivity mContext;
    Handler handler = new Handler();

    private final int DELAY_SOFT_KEYBOARD = 100;
    private final int DELAY_POPUP = 200;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateStatusBarBackground(EnterBarcodeActivity.this);
        mContext = this;
        setContentView(R.layout.enter_barcode_activity);
        mPopWindowValidationMessage = new PopWindowValidationMessage(this);
        initUI();
        setActionBar();
        if (hasPermissions()) {
            startLocationUpdate();
        } else {
            requestPerms();
        }

        mEditBarcodeNumber.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    getProductRequest(mEditBarcodeNumber.getText().toString());
                    handled = true;
                }
                return handled;
            }
        });
    }

    private void initUI() {
        mToolbar = (Toolbar) findViewById(R.id.mToolbar);
        mEditBarcodeNumber = (WLoanEditTextView) findViewById(R.id.editBarcodeNumber);
        mProgressBar = (ProgressBar) findViewById(R.id.mProgressBar);
        mProgressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
        mTextInfo = (WTextView) findViewById(R.id.textInfo);
    }

    private void setActionBar() {
        setSupportActionBar(mToolbar);
        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setDisplayShowTitleEnabled(false);
            mActionBar.setDisplayUseLogoEnabled(false);
            mActionBar.setDefaultDisplayHomeAsUpEnabled(false);
            mActionBar.setHomeAsUpIndicator(R.drawable.back_white);
        }
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onResume() {
        super.onResume();
        showSoftKeyboard();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                return true;
        }
        return false;
    }

    public void showSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mEditBarcodeNumber, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditBarcodeNumber.getWindowToken(), 0);
    }

    public void getProductRequest(final String query) {
        new HttpAsyncTask<String, String, ProductView>() {
            @Override
            protected ProductView httpDoInBackground(String... params) {
                LatLng location1 = new LatLng(mLocation.latitude, mLocation.longitude);
                return ((WoolworthsApplication) getApplication()).getApi()
                        .getProductSearchList(query,
                                location1, true, 1, PAGE_SIZE);
            }

            @Override
            protected ProductView httpError(String errorMessage, HttpErrorCode httpErrorCode) {
                hideProgressBar();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Do something after 100ms
                        hideSoftKeyboard();
                    }
                }, DELAY_SOFT_KEYBOARD);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Do something after 100ms
                        errorScanCode();
                    }
                }, DELAY_POPUP);
                return new ProductView();
            }

            @Override
            protected void onPostExecute(ProductView product) {
                super.onPostExecute(product);
                List<ProductList> mProduct = product.products;
                if (mProduct != null) {
                    if (mProduct.size() > 0) {
                        getProductDetail(mProduct.get(0).productId, mProduct.get(0).sku);
                    } else {
                        hideProgressBar();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //Do something after 100ms
                                hideSoftKeyboard();
                            }
                        }, DELAY_SOFT_KEYBOARD);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //Do something after 100ms
                                errorScanCode();
                            }
                        }, DELAY_POPUP);
                    }
                }
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showProgressBar();
            }

            @Override
            protected Class<ProductView> httpDoInBackgroundReturnType() {
                return ProductView.class;
            }
        }.execute();
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
        boolean permissionIsAllowed = true;
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

    private void startLocationUpdate() {
        // start location updates
        FusedLocationSingleton.getInstance().startLocationUpdates();
        // register observer for location updates
        LocalBroadcastManager.getInstance(EnterBarcodeActivity.this).registerReceiver(mLocationUpdated,
                new IntentFilter(Const.INTENT_FILTER_LOCATION_UPDATE));
    }

    @Override
    public void finish() {
        super.finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void updateStatusBarBackground(Activity activity) {
        Window window = activity.getWindow();
        window.setStatusBarColor(ContextCompat.getColor(activity, R.color.black));
        View decor = activity.getWindow().getDecorView();
        decor.setSystemUiVisibility(0);
    }


    @Override
    protected void onPause() {
        super.onPause();
        // stop location updates
        try {
            FusedLocationSingleton.getInstance().stopLocationUpdates();
            // unregister observer
            LocalBroadcastManager.getInstance(EnterBarcodeActivity.this).unregisterReceiver(mLocationUpdated);
        } catch (NullPointerException ignored) {
        }
    }

    private void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
        mTextInfo.setVisibility(View.GONE);
    }

    private void hideProgressBar() {
        mProgressBar.setVisibility(View.GONE);
        mTextInfo.setVisibility(View.VISIBLE);
    }

    private void errorScanCode() {
        mPopWindowValidationMessage.displayValidationMessage("",
                PopWindowValidationMessage.OVERLAY_TYPE.BARCODE_ERROR);
    }


    private void getProductDetail(final String productId, final String skuId) {
        new HttpAsyncTask<String, String, WProduct>() {
            @Override
            protected WProduct httpDoInBackground(String... params) {
                return ((WoolworthsApplication) getApplication()).getApi().getProductDetailView(productId, skuId);
            }

            @Override
            protected WProduct httpError(String errorMessage, HttpErrorCode httpErrorCode) {
                Log.e("errorMessage", String.valueOf(errorMessage) + " " + httpErrorCode);
                hideProgressBar();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Do something after 100ms
                        hideSoftKeyboard();
                    }
                }, DELAY_SOFT_KEYBOARD);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Do something after 100ms
                        errorScanCode();
                    }
                }, DELAY_POPUP);
                return new WProduct();
            }

            @Override
            protected Class<WProduct> httpDoInBackgroundReturnType() {
                return WProduct.class;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(WProduct product) {
                super.onPostExecute(product);
                hideProgressBar();
                WProductDetail productList = product.product;
                ArrayList<WProductDetail> mProductList = new ArrayList<>();
                if (productList != null) {
                    mProductList.add(productList);
                }
                if (productList != null) {
                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();
                    Intent openDetailView = new Intent(mContext, ProductDetailViewActivity.class);
                    openDetailView.putExtra("product_name", mProductList.get(0).productName);
                    openDetailView.putExtra("product_detail", gson.toJson(mProductList));
                    startActivity(openDetailView);
                    overridePendingTransition(0, 0);
                } else {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //Do something after 100ms
                            hideSoftKeyboard();
                        }
                    }, DELAY_SOFT_KEYBOARD);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //Do something after 100ms
                            errorScanCode();
                        }
                    }, DELAY_POPUP);
                }
            }
        }.execute();
    }
}
