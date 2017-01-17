package za.co.woolworths.financial.services.android.util.barcode.scanner;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.awfs.coordination.R;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.Product;
import za.co.woolworths.financial.services.android.models.dto.Product_;
import za.co.woolworths.financial.services.android.ui.activities.EnterBarcodeActivity;
import za.co.woolworths.financial.services.android.ui.activities.ProductSearchActivity;
import za.co.woolworths.financial.services.android.util.FontHyperTextParser;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.barcode.core.IViewFinder;
import za.co.woolworths.financial.services.android.util.barcode.core.ViewFinderView;

import static za.co.woolworths.financial.services.android.ui.activities.ProductSearchActivity.PAGE_SIZE;


public class ProductCategoryBarcodeActivity extends BaseScannerActivity implements MessageDialogFragment.MessageDialogListener,
        ZBarScannerView.ResultHandler, FormatSelectorDialogFragment.FormatSelectorDialogListener,
        CameraSelectorDialogFragment.CameraSelectorDialogListener, View.OnClickListener {
    private static final String FLASH_STATE = "FLASH_STATE";
    private static final String AUTO_FOCUS_STATE = "AUTO_FOCUS_STATE";
    private static final String SELECTED_FORMATS = "SELECTED_FORMATS";
    private static final String CAMERA_ID = "CAMERA_ID";
    private ZBarScannerView mScannerView;
    private boolean mFlash;
    private boolean mAutoFocus;
    private ArrayList<Integer> mSelectedIndices;
    private int mCameraId = -1;
    private Button mBtnManual;
    private ProgressDialog mSearchProductDialog;
    private ArrayList<Product_> productList;
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        Utils.updateStatusBarBackground(this, R.color.black);
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
        mBtnManual = (Button) findViewById(R.id.btnManual);
        mBtnManual.setOnClickListener(this);
        setupToolbar();
        ViewGroup contentFrame = (ViewGroup) findViewById(R.id.content_frame);
        mScannerView = new ZBarScannerView(this) {
            @Override
            protected IViewFinder createViewFinderView(Context context) {
                ViewFinderView finderView = new ViewFinderView(context);
                finderView.setLaserColor(Color.TRANSPARENT);
                finderView.setMaskColor(ContextCompat.getColor(ProductCategoryBarcodeActivity.this, R.color.black));
                finderView.setBorderColor(Color.WHITE);
                finderView.setBorderStrokeWidth(6);
                return finderView;
            }
        };
        setupFormats();
        contentFrame.addView(mScannerView);
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
        }
        getProductRequest(rawResult.getBarcodeFormat().getName());
        Toast.makeText(this,rawResult.getBarcodeFormat().getName(),Toast.LENGTH_SHORT).show();
      //  showMessageDialog("Contents = " + rawResult.getContents() + ", Format = " + rawResult.getBarcodeFormat().getName());
    }

    public void showMessageDialog(String message) {
        DialogFragment fragment = MessageDialogFragment.newInstance("Scan Results", message, this);
        fragment.show(getSupportFragmentManager(), "scan_results");
    }

    public void closeMessageDialog() {
        closeDialog("scan_results");
    }

    public void closeFormatsDialog() {
        closeDialog("format_selector");
    }

    public void closeDialog(String dialogName) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        DialogFragment fragment = (DialogFragment) fragmentManager.findFragmentByTag(dialogName);
        if (fragment != null) {
            fragment.dismiss();
        }
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // Resume the camera
        mScannerView.resumeCameraPreview(this);
    }

    @Override
    public void onFormatsSaved(ArrayList<Integer> selectedIndices) {
        mSelectedIndices = selectedIndices;
        setupFormats();
    }

    @Override
    public void onCameraSelected(int cameraId) {
        mCameraId = cameraId;
        mScannerView.startCamera(mCameraId);
        mScannerView.setFlash(mFlash);
        mScannerView.setAutoFocus(mAutoFocus);
    }

    public void setupFormats() {
        List<BarcodeFormat> formats = new ArrayList<BarcodeFormat>();
        if (mSelectedIndices == null || mSelectedIndices.isEmpty()) {
            mSelectedIndices = new ArrayList<Integer>();
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
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
        closeMessageDialog();
        closeFormatsDialog();
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
        overridePendingTransition(0,0);
    }

    public void getProductRequest(final String query) {

        new HttpAsyncTask<String, String, Product>() {
            @Override
            protected Product httpDoInBackground(String... params) {
                Location location = Utils.getLastSavedLocation(ProductCategoryBarcodeActivity.this);
                LatLng location1 = new LatLng(-29.79, 31.0833);
                return ((WoolworthsApplication) getApplication()).getApi()
                        .getProductSearchList(query,
                                location1, true, 1, PAGE_SIZE);
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
            }

            @Override
            protected void onPreExecute() {
                mSearchProductDialog = new ProgressDialog(ProductCategoryBarcodeActivity.this);
                mSearchProductDialog.setMessage(FontHyperTextParser.getSpannable(getString(R.string.loading), 1, ProductCategoryBarcodeActivity.this));
                mSearchProductDialog.setCancelable(false);
                mSearchProductDialog.show();
                super.onPreExecute();
            }

            @Override
            protected Class<Product> httpDoInBackgroundReturnType() {
                return Product.class;
            }
        }.execute();
    }
}