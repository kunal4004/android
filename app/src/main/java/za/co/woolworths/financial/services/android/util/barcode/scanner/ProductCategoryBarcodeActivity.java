package za.co.woolworths.financial.services.android.util.barcode.scanner;

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
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.awfs.coordination.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.Product;
import za.co.woolworths.financial.services.android.models.dto.Product_;
import za.co.woolworths.financial.services.android.models.dto.WProduct;
import za.co.woolworths.financial.services.android.models.dto.WProductDetail;
import za.co.woolworths.financial.services.android.ui.activities.EnterBarcodeActivity;
import za.co.woolworths.financial.services.android.ui.activities.ProductDetailViewActivity;
import za.co.woolworths.financial.services.android.util.Const;
import za.co.woolworths.financial.services.android.util.FusedLocationSingleton;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.PopWindowValidationMessage;
import za.co.woolworths.financial.services.android.util.barcode.core.IViewFinder;
import za.co.woolworths.financial.services.android.util.barcode.core.ViewFinderView;

public class ProductCategoryBarcodeActivity extends BaseScannerActivity implements
        ZBarScannerView.ResultHandler,
        CameraSelectorDialogFragment.CameraSelectorDialogListener, View.OnClickListener {

    public static int PAGE_SIZE = 20;
    private static final String FLASH_STATE = "FLASH_STATE";
    private static final String AUTO_FOCUS_STATE = "AUTO_FOCUS_STATE";
    private static final String SELECTED_FORMATS = "SELECTED_FORMATS";
    private static final String CAMERA_ID = "CAMERA_ID";
    private ZBarScannerView mScannerView;
    private boolean mFlash;
    private boolean mAutoFocus;
    private ArrayList<Integer> mSelectedIndices;
    private int mCameraId = -1;
    private static final int PERMS_REQUEST_CODE = 1234;
    private LatLng mLocation;
    private TextView mTextInfo;
    private RelativeLayout mRelProgressBar;
    private PopWindowValidationMessage mPopWindowValidationMessage;
    private ProductCategoryBarcodeActivity mContext;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        updateStatusBarBackground(this);
        mContext = this;
        mPopWindowValidationMessage = new PopWindowValidationMessage(this);
        if (state != null) {
            mFlash = state.getBoolean(FLASH_STATE, false);
            mAutoFocus = state.getBoolean(AUTO_FOCUS_STATE, true);
            mSelectedIndices = state.getIntegerArrayList(SELECTED_FORMATS);
            mCameraId = state.getInt(CAMERA_ID, -1);
        } else {
            mFlash = false;
            mAutoFocus = true;
            mSelectedIndices = null;
            mCameraId = -1;
        }

        setContentView(R.layout.activity_full_scanner);
        Button mBtnManual = (Button) findViewById(R.id.btnManual);
        mBtnManual.setOnClickListener(this);
        setupToolbar();
        ViewGroup contentFrame = (ViewGroup) findViewById(R.id.content_frame);

        mTextInfo = (TextView) findViewById(R.id.textInfo);
        mRelProgressBar = (RelativeLayout) findViewById(R.id.relProgressContainer);
        ProgressBar mProgressBar = (ProgressBar) findViewById(R.id.ppBar);
        mProgressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
        mScannerView = new ZBarScannerView(this) {
            @Override
            protected IViewFinder createViewFinderView(Context context) {
                ViewFinderView finderView = new ViewFinderView(context);
                finderView.setLaserColor(Color.TRANSPARENT);
                finderView.setMaskColor(ContextCompat.getColor(ProductCategoryBarcodeActivity.this,
                        R.color.black_desc_opacity));
                finderView.setBorderColor(Color.WHITE);
                finderView.setBorderStrokeWidth(4);
                return finderView;
            }
        };
        setupFormats();
        contentFrame.addView(mScannerView);
        if (hasPermissions()) {
            startLocationUpdate();
        } else {
            requestPerms();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera(mCameraId);
        mScannerView.setFlash(mFlash);
        mScannerView.setAutoFocus(mAutoFocus);
    }

    @Override
    public void handleResult(Result rawResult) {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            Log.e("Exception", e.toString());
        }

        getProductRequest(rawResult.getContents());
        // Toast.makeText(this, rawResult.getContents(), Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onCameraSelected(int cameraId) {
        mCameraId = cameraId;
        mScannerView.startCamera(mCameraId);
        mScannerView.setFlash(mFlash);
        mScannerView.setAutoFocus(mAutoFocus);
    }

    public void setupFormats() {
        List<BarcodeFormat> formats = new ArrayList<>();
        if (mSelectedIndices == null || mSelectedIndices.isEmpty()) {
            mSelectedIndices = new ArrayList<>();
            for (int i = 0; i < BarcodeFormat.ALL_FORMATS.size(); i++) {
                mSelectedIndices.add(i);
            }
        }

        for (int index : mSelectedIndices) {
            formats.add(BarcodeFormat.ALL_FORMATS.get(index));
        }
        if (mScannerView != null) {
            mScannerView.setFormats(formats);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnManual:
                Intent openManual = new Intent(ProductCategoryBarcodeActivity.this, EnterBarcodeActivity.class);
                startActivity(openManual);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(0, 0);
    }

    public void getProductRequest(final String query) {
        new HttpAsyncTask<String, String, Product>() {
            @Override
            protected Product httpDoInBackground(String... params) {
                LatLng location1 = new LatLng(mLocation.latitude, mLocation.longitude);
                return ((WoolworthsApplication) getApplication()).getApi()
                        .getProductSearchList(query,
                                location1, true, 1, PAGE_SIZE);
            }

            @Override
            protected Product httpError(String errorMessage, HttpErrorCode httpErrorCode) {
                hideProgressBar();
                try {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mScannerView.setResultHandler(mContext);
                            mScannerView.startCamera(mCameraId);
                            mScannerView.setFlash(mFlash);
                            mScannerView.setAutoFocus(mAutoFocus);
                        }
                    }, 50);
                } catch (Exception ignored) {
                }
                errorScanCode();
                return new Product();
            }

            @Override
            protected void onPostExecute(Product product) {
                super.onPostExecute(product);
                ArrayList<Product_> mProduct = product.products;
                if (mProduct != null) {
                    if (mProduct.size() > 0) {
                        getProductDetail(mProduct.get(0).productId, mProduct.get(0).sku);
                    } else {
                        hideProgressBar();
                        errorScanCode();
                    }
                }
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showProgressBar();
            }

            @Override
            protected Class<Product> httpDoInBackgroundReturnType() {
                return Product.class;
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
        LocalBroadcastManager.getInstance(ProductCategoryBarcodeActivity.this).registerReceiver(mLocationUpdated,
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
        mScannerView.stopCamera();
        try {
            FusedLocationSingleton.getInstance().stopLocationUpdates();
            // unregister observer
            LocalBroadcastManager
                    .getInstance(ProductCategoryBarcodeActivity.this)
                    .unregisterReceiver(mLocationUpdated);
        } catch (NullPointerException ignored) {
        }
    }

    private void showProgressBar() {
        mRelProgressBar.setVisibility(View.VISIBLE);
        mTextInfo.setVisibility(View.GONE);
    }

    private void hideProgressBar() {
        mRelProgressBar.setVisibility(View.GONE);
        mTextInfo.setVisibility(View.VISIBLE);
    }

    private void errorScanCode() {
        mPopWindowValidationMessage.displayValidationMessage("",
                PopWindowValidationMessage.OVERLAY_TYPE.BARCODE_ERROR)
                .setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mScannerView.setResultHandler(mContext);
                                mScannerView.startCamera(mCameraId);
                                mScannerView.setFlash(mFlash);
                                mScannerView.setAutoFocus(mAutoFocus);
                            }
                        }, 500);
                    }
                });
    }

    private void getProductDetail(final String productId, final String skuId) {
        new HttpAsyncTask<String, String, WProduct>() {
            @Override
            protected WProduct httpDoInBackground(String... params) {
                return ((WoolworthsApplication) getApplication()).getApi().getProductDetailView(productId, skuId);
            }

            @Override
            protected WProduct httpError(String errorMessage, HttpErrorCode httpErrorCode) {
                hideProgressBar();
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
                WProductDetail productList = product.product;
                ArrayList<WProductDetail> mProductList = new ArrayList<>();
                if (productList != null) {
                    mProductList.add(productList);
                }
                if (productList != null) {
                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();
                    Intent openDetailView = new Intent(mContext, ProductDetailViewActivity.class);
                    openDetailView.putExtra("product_detail", gson.toJson(mProductList));
                    startActivity(openDetailView);
                    overridePendingTransition(0, 0);
                }
                hideProgressBar();
            }
        }.execute();
    }
}