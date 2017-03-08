package za.co.woolworths.financial.services.android.util.zxing;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.awfs.coordination.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.MultiFormatReader;
import com.pacific.mvc.Activity;
import com.trello.rxlifecycle.ActivityEvent;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.ProductView;
import za.co.woolworths.financial.services.android.models.dto.WProduct;
import za.co.woolworths.financial.services.android.models.dto.WProductDetail;
import za.co.woolworths.financial.services.android.ui.activities.EnterBarcodeActivity;
import za.co.woolworths.financial.services.android.ui.activities.ProductDetailViewActivity;
import za.co.woolworths.financial.services.android.ui.activities.TransientActivity;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.Utils;

public class QRActivity extends Activity<QRModel> implements View.OnClickListener {
    public static final int CODE_PICK_IMAGE = 0x100;
    private BaseCameraManager cameraManager;
    private final int ZBAR_PERMS_REQUEST_CODE = 12345678;
    private WButton mBtnManual;
    private TextView mTextInfo;
    private ImageView snapImage;
    private ProgressBar mProgressBar;
    private QRCodeView qRview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.updateStatusBarBackground(this, R.color.black);
        setContentView(R.layout.activity_qr);
        setupToolbar();
        if (Build.VERSION_CODES.LOLLIPOP >= Build.VERSION.SDK_INT) {
            cameraManager = new CameraManager(getApplication());
        } else {
            cameraManager = new CameraManager(getApplication());
        }
        model = new QRModel(new QRView(this));
        model.onCreate();


        cameraManager.setOnResultListener(new BaseCameraManager.OnResultListener() {
            @Override
            public void onResult(final QRResult qrResult) {
                final String barcodeNumber = qrResult.getResult().getText();
                String barcodeFormat = qrResult.getResult().getBarcodeFormat().name();
                Log.e("barcodeNumber", barcodeNumber + " format " + barcodeFormat);
                for (String bf : barcodeFormat()) {
                    if (bf.equalsIgnoreCase(barcodeFormat)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e("qrresult", qrResult.getBitmap().toString());
                                cameraManager.stopCamera();
                                qRview.setBackground(new BitmapDrawable(getResources(), qrResult.getBitmap()));
                                //model.resultDialog(qrResult);
                                getProductRequest(barcodeNumber);
                            }
                        });
                    }
                }
            }
        });

        initUI();
    }

    private void initUI() {
        qRview=(QRCodeView)findViewById(R.id.qr_view);
        mTextInfo = (TextView) findViewById(R.id.textInfo);
        mProgressBar = (ProgressBar) findViewById(R.id.ppBar);
        snapImage = (ImageView) findViewById(R.id.snapImage);
        mProgressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
        mBtnManual = (WButton) findViewById(R.id.btnManual);
        mBtnManual.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        model.onResume();
        if(qRview!=null)
            qRview.setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    protected void onPause() {
        super.onPause();
        model.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraManager.releaseCamera();
        cameraManager.shutdownExecutor();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == CODE_PICK_IMAGE) {
            String[] columns = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(data.getData(), columns, null, null, null);
            if (cursor.moveToFirst()) {
                Observable
                        .just(cursor.getString(cursor.getColumnIndex(columns[0])))
                        .observeOn(Schedulers.from(cameraManager.getExecutor()))
                        .compose(this.<String>bindUntilEvent(ActivityEvent.PAUSE))
                        .map(new Func1<String, QRResult>() {
                            @Override
                            public QRResult call(String str) {
                                return QRUtils.decode(str, new MultiFormatReader());
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<QRResult>() {
                            @Override
                            public void call(QRResult qrResult) {
                                model.resultDialog(qrResult);
                            }
                        });
            }
            cursor.close();
        }
    }

    public void onSurfaceCreated(SurfaceHolder surfaceHolder) {
        if (cameraManager.getExecutor().isShutdown()) return;
        Observable
                .just(surfaceHolder)
                .compose(this.<SurfaceHolder>bindUntilEvent(ActivityEvent.PAUSE))
                .observeOn(Schedulers.from(cameraManager.getExecutor()))
                .map(new Func1<SurfaceHolder, Object>() {
                    @Override
                    public Object call(SurfaceHolder holder) {
                        cameraManager.setRotate(getWindowManager().getDefaultDisplay().getRotation());
                        cameraManager.connectCamera(holder);
                        return null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        model.setEmptyViewVisible(false);
                        cameraManager.startCapture();
                    }
                });
    }

    public void onSurfaceDestroyed() {
        cameraManager.releaseCamera();
    }

    public void restartCapture() {
        cameraManager.startCapture();
    }

    public void setHook(boolean hook) {
        cameraManager.setHook(hook);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean allowed = true;
        switch (requestCode) {
            case ZBAR_PERMS_REQUEST_CODE:
                for (int res : grantResults) {
                    // if user granted all permissions.
                    allowed = allowed && (res == PackageManager.PERMISSION_GRANTED);
                }
                break;
            default:
                // if user not granted permissions.
                allowed = false;
                break;
        }
        if (allowed) {
            //user granted all permissions we can perform our task.
            if (Build.VERSION_CODES.LOLLIPOP >= Build.VERSION.SDK_INT) {
                cameraManager = new CameraManager(getApplication());
            } else {
                cameraManager = new CameraManager(getApplication());
            }
            model = new QRModel(new QRView(this));
            model.onCreate();

            cameraManager.setOnResultListener(new BaseCameraManager.OnResultListener() {
                @Override
                public void onResult(QRResult qrResult) {
                    model.resultDialog(qrResult);
                }
            });

        } else {
            // we will give warning to user that they haven't granted permissions.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "Camera Permissions denied.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        WTextView mTextToolbar = (WTextView) findViewById(R.id.toolbarText);
        mTextToolbar.setText(getString(R.string.scan_product));
        mTextToolbar.setGravity(Gravity.LEFT);
        mTextToolbar.setTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowTitleEnabled(false);
            ab.setDisplayUseLogoEnabled(false);
            ab.setDisplayShowTitleEnabled(false);
            ab.setDefaultDisplayHomeAsUpEnabled(false);
            ab.setHomeAsUpIndicator(R.drawable.close_white);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnManual:
                Intent openManual = new Intent(QRActivity.this, EnterBarcodeActivity.class);
                startActivity(openManual);
                break;
        }
    }

    private void getProductDetail(final String productId, final String skuId) {
        ((WoolworthsApplication) getApplication()).getAsyncApi().getProductDetail(productId, skuId, new Callback<String>() {
            @Override
            public void success(String strProduct, retrofit.client.Response response) {
                hideProgressBar();
                WProduct wProduct = Utils.stringToJson(QRActivity.this, strProduct);
                if (wProduct != null) {
                    switch (wProduct.httpCode) {
                        case 200:
                            ArrayList<WProductDetail> mProductList;
                            WProductDetail productList = wProduct.product;
                            mProductList = new ArrayList<>();
                            if (productList != null) {
                                mProductList.add(productList);
                            }
                            GsonBuilder builder = new GsonBuilder();
                            Gson gson = builder.create();
                            Intent openDetailView = new Intent(QRActivity.this, ProductDetailViewActivity.class);
                            openDetailView.putExtra("product_name", mProductList.get(0).productName);
                            openDetailView.putExtra("product_detail", gson.toJson(mProductList));
                            startActivity(openDetailView);
                            overridePendingTransition(0, R.anim.anim_slide_up);
                            break;
                        default:
                            hideProgressBar();
                            break;
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {
                hideProgressBar();
            }
        });
    }

    private void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
        mTextInfo.setVisibility(View.GONE);
        mBtnManual.setEnabled(false);
    }

    private void hideProgressBar() {
        mProgressBar.setVisibility(View.GONE);
        mTextInfo.setVisibility(View.VISIBLE);
        mBtnManual.setEnabled(true);
    }

    private ArrayList<String> barcodeFormat() {
        ArrayList<String> barcodeFormat = new ArrayList<>();
        barcodeFormat.add("EAN_8");
        barcodeFormat.add("UPC_E");
        barcodeFormat.add("UPC_A");
        barcodeFormat.add("EAN_13");
        barcodeFormat.add("ISBN_13");
        barcodeFormat.add("CODE_128");
        return barcodeFormat;
    }

    public void getProductRequest(final String query) {
        new HttpAsyncTask<String, String, ProductView>() {
            @Override
            protected ProductView httpDoInBackground(String... params) {
                return ((WoolworthsApplication) getApplication()).getApi()
                        .getProductSearchList(query, true, 0, Utils.PAGE_SIZE);
            }

            @Override
            protected ProductView httpError(String errorMessage, HttpErrorCode httpErrorCode) {
                hideProgressBar();
                errorScanCode();
                return new ProductView();
            }

            @Override
            protected void onPostExecute(ProductView product) {
                super.onPostExecute(product);
                ArrayList<ProductList> mProduct = product.products;

                if (mProduct != null) {
                    if (mProduct.size() > 0) {
                        getProductDetail(mProduct.get(0).productId, mProduct.get(0).sku);
                    } else {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                hideProgressBar();
                                //resetCamera();
                            }
                        }, 100);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                errorScanCode();
                            }
                        }, 200);
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

    private void errorScanCode() {
        Utils.displayValidationMessage(this, TransientActivity.VALIDATION_MESSAGE_LIST.BARCODE_ERROR, "");
    }
}
