package za.co.woolworths.financial.services.android.util.zxing;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.awfs.coordination.R;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.MultiFormatReader;
import com.pacific.mvc.Activity;
import com.trello.rxlifecycle.ActivityEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import retrofit.Callback;
import retrofit.RetrofitError;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.OtherSku;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.ProductView;
import za.co.woolworths.financial.services.android.models.dto.PromotionImages;
import za.co.woolworths.financial.services.android.models.dto.WProduct;
import za.co.woolworths.financial.services.android.models.dto.WProductDetail;
import za.co.woolworths.financial.services.android.ui.activities.EnterBarcodeActivity;
import za.co.woolworths.financial.services.android.ui.views.NestedScrollableViewHelper;
import za.co.woolworths.financial.services.android.ui.activities.TransientActivity;
import za.co.woolworths.financial.services.android.ui.adapters.ProductColorAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.ProductSizeAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.ProductViewPagerAdapter;
import za.co.woolworths.financial.services.android.ui.views.SlidingUpPanelLayout;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.ui.views.WrapContentWebView;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.DrawImage;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.SelectedProductView;
import za.co.woolworths.financial.services.android.util.SimpleDividerItemDecoration;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WFormatter;

public class QRActivity extends Activity<QRModel> implements View.OnClickListener, SelectedProductView {
    public final int IMAGE_QUALITY = 85;
    public static final int CODE_PICK_IMAGE = 0x100;
    private BaseCameraManager cameraManager;
    public final int ZBAR_PERMS_REQUEST_CODE = 12345678;
    private WButton mBtnManual;
    private TextView mTextInfo;
    private ProgressBar mProgressBar;
    private QRCodeView qRview;
    private SlidingUpPanelLayout mSlideUpPanelLayout;
    private WTextView mTextSelectSize;
    private RecyclerView mRecyclerviewSize;
    private ArrayList<WProductDetail> mproductDetail;
    private WTextView mTextTitle;
    private WTextView mTextPrice;
    private LinearLayout mRelContainer;
    private WTextView mProductCode;
    private List<OtherSku> otherSkusList;
    private WTextView mTextSelectColor;
    private PopupWindow mPColourWindow;
    private PopupWindow mPSizeWindow;
    private boolean productIsColored = false;
    private ArrayList<OtherSku> uniqueColorList;
    public RecyclerView mColorRecycleSize;
    public ArrayList<OtherSku> uniqueSizeList;
    public ViewPager mViewPagerProduct;
    public ImageView mImCloseProduct;
    public RelativeLayout mLinSize;
    public SimpleDraweeView mImNewImage;
    public SimpleDraweeView mImSave;
    public SimpleDraweeView mImReward;
    public SimpleDraweeView mVitalityView;
    public String mCheckOutLink;
    private ArrayList<String> mAuxiliaryImages;
    private LinearLayout mLlPagerDots;
    private ImageView[] ivArrayDotsPager;
    private String mDefaultImage;
    private SimpleDraweeView mImSelectedColor;
    private View mColorView;
    private WTextView mTextPromo;
    private WTextView mTextActualPrice;
    private WTextView mTextColour;
    private WrapContentWebView mWebDescription;
    private WTextView mIngredientList;
    private LinearLayout mLinIngredient;
    private View ingredientLine;
    public String mProductJSON;
    public NestedScrollView mScrollProductDetail;
    private int mPreviousState;
    private ViewPager mTouchTarget;
    private WProductDetail mObjProductDetail;
    private SlidingUpPanelLayout.PanelState mPanelState = SlidingUpPanelLayout.PanelState.COLLAPSED;
    private String mDefaultColor;
    private String mDefaultColorRef;
    public String mDefaultSize;
    public ProductViewPagerAdapter mProductViewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.updateStatusBarBackground(this, R.color.black);
        setContentView(R.layout.barcode_scanner_layout);
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
                String barcodeFormat = qrResult.getResult().getBarcodeFormat().name();
                for (String bf : barcodeFormat()) {
                    if (bf.equalsIgnoreCase(barcodeFormat)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                cameraManager.stopCamera();
                                String barcodeNumber = qrResult.getResult().getText();
                                if (new ConnectionDetector().isOnline(QRActivity.this)) {
                                    getProductRequest(barcodeNumber);
                                } else {
                                    Utils.displayValidationMessage(QRActivity.this,
                                            TransientActivity.VALIDATION_MESSAGE_LIST.ERROR,
                                            getString(R.string.connect_to_server));
                                }
                            }
                        });
                    }
                }
            }
        });

        initUI();
        initProductDetailUI();
        slideUpPanel();
    }

    private void slideUpPanel() {

        mSlideUpPanelLayout.setFadeOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSlideUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });
        mSlideUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                if (slideOffset == 0.0) {
                    mSlideUpPanelLayout.setAnchorPoint(1.0f);
                }
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState,
                                            SlidingUpPanelLayout.PanelState newState) {
                switch (newState) {
                    case COLLAPSED:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mPanelState = SlidingUpPanelLayout.PanelState.COLLAPSED;
                                QRActivity.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                                dismissPopWindow();
                                QRActivity.this.onResume();
                            }
                        });
                        break;

                    case DRAGGING:
                        model.setEmptyViewVisible(false);
                        break;

                    case EXPANDED:
                        mPanelState = SlidingUpPanelLayout.PanelState.EXPANDED;
                        model.setEmptyViewVisible(false);
                        QRActivity.this.onPause();
                        QRActivity.this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void initUI() {
        qRview = (QRCodeView) findViewById(R.id.qr_view);
        mSlideUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mTextInfo = (TextView) findViewById(R.id.textInfo);
        mProgressBar = (ProgressBar) findViewById(R.id.ppBar);
        mProgressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
        mBtnManual = (WButton) findViewById(R.id.btnManual);
        mBtnManual.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumeScan();
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
            assert cursor != null;
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
            case R.id.textSelectColour:
            case R.id.imSelectedColor:
            case R.id.imColorArrow:
            case R.id.linColour:
                dismissSizeDialog();
                LayoutInflater mSlideUpPanelLayoutInflater
                        = (LayoutInflater) getBaseContext()
                        .getSystemService(LAYOUT_INFLATER_SERVICE);
                View mPopWindow = mSlideUpPanelLayoutInflater.inflate(R.layout.product_size_row, null);
                mColorRecycleSize = (RecyclerView) mPopWindow.findViewById(R.id.recyclerviewSize);
                bindWithUI(otherSkusList, true);
                mPColourWindow = new PopupWindow(
                        mPopWindow,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

                mPopWindow.setOnTouchListener(new View.OnTouchListener() {

                    @Override
                    public boolean onTouch(View arg0, MotionEvent arg1) {

                        return true;
                    }
                });

                mPColourWindow.setTouchable(true);
                mPColourWindow.showAsDropDown(mTextSelectColor, -50, -180);
                break;

            case R.id.textProductSize:
            case R.id.mColorArrow:
            case R.id.textSelectSize:
            case R.id.linSize:
                dismissColourDialog();
                LayoutInflater layoutInflater
                        = (LayoutInflater) getBaseContext()
                        .getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = layoutInflater.inflate(R.layout.product_size_row, null);
                mRecyclerviewSize = (RecyclerView) popupView.findViewById(R.id.recyclerviewSize);
                LinearLayout mPopLinContainer = (LinearLayout) popupView.findViewById(R.id.linPopUpContainer);

                bindWithUI(otherSkusList, false);

                mPSizeWindow = new PopupWindow(
                        popupView,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

                mPopLinContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mPSizeWindow.dismiss();
                    }
                });

                mPSizeWindow.showAsDropDown(mTextSelectSize, -50, -180);

                break;

            case R.id.imCloseProduct:
                if (mPanelState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    mSlideUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                } else {
                    onBackPressed();
                }
                break;

            case R.id.btnShopOnlineWoolies:
                if (!TextUtils.isEmpty(mCheckOutLink))
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mCheckOutLink)));
                break;

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
                            displayProductDetail(mProductList.get(0).productName,
                                    new GsonBuilder().create().toJson(mProductList), skuId);

                            mSlideUpPanelLayout.setAnchorPoint(1.0f);
                            mSlideUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                            mSlideUpPanelLayout.setScrollableViewHelper(new NestedScrollableViewHelper(mScrollProductDetail));
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

    private void resumeScan() {
        model.onResume();
        if (qRview != null)
            qRview.setBackgroundColor(Color.TRANSPARENT);
    }

    protected void initProductDetailUI() {
        mScrollProductDetail = (NestedScrollView) findViewById(R.id.scrollProductDetail);
        mColorView = findViewById(R.id.colorView);
        mTextSelectSize = (WTextView) findViewById(R.id.textSelectSize);
        mTextColour = (WTextView) findViewById(R.id.textColour);
        WTextView mTextProductSize = (WTextView) findViewById(R.id.textProductSize);
        mTextTitle = (WTextView) findViewById(R.id.textTitle);
        mTextActualPrice = (WTextView) findViewById(R.id.textActualPrice);
        mViewPagerProduct = (ViewPager) findViewById(R.id.mProductDetailPager);
        mTextPrice = (WTextView) findViewById(R.id.textPrice);
        mLinIngredient = (LinearLayout) findViewById(R.id.linIngredient);
        mIngredientList = (WTextView) findViewById(R.id.ingredientList);
        mTextPromo = (WTextView) findViewById(R.id.textPromo);
        mTextSelectColor = (WTextView) findViewById(R.id.textSelectColour);
        mProductCode = (WTextView) findViewById(R.id.product_code);
        mRelContainer = (LinearLayout) findViewById(R.id.linProductContainer);
        RelativeLayout mLinColor = (RelativeLayout) findViewById(R.id.linColour);
        mLinSize = (RelativeLayout) findViewById(R.id.linSize);
        WButton mBtnShopOnlineWoolies = (WButton) findViewById(R.id.btnShopOnlineWoolies);
        ImageView mColorArrow = (ImageView) findViewById(R.id.mColorArrow);
        mImCloseProduct = (ImageView) findViewById(R.id.imCloseProduct);
        mImSelectedColor = (SimpleDraweeView) findViewById(R.id.imSelectedColor);
        mLlPagerDots = (LinearLayout) findViewById(R.id.pager_dots);
        ImageView mImColorArrow = (ImageView) findViewById(R.id.imColorArrow);
        mWebDescription = (WrapContentWebView) findViewById(R.id.webDescription);
        ingredientLine = findViewById(R.id.ingredientLine);

        mImNewImage = (SimpleDraweeView) findViewById(R.id.imNewImage);
        mImSave = (SimpleDraweeView) findViewById(R.id.imSave);
        mImReward = (SimpleDraweeView) findViewById(R.id.imReward);
        mVitalityView = (SimpleDraweeView) findViewById(R.id.imVitality);

        mTextSelectColor.setOnClickListener(this);
        mTextSelectSize.setOnClickListener(this);
        mImCloseProduct.setOnClickListener(this);
        mImSelectedColor.setOnClickListener(this);
        mLinSize.setOnClickListener(this);

        mImColorArrow.setOnClickListener(this);
        mColorArrow.setOnClickListener(this);
        mTextProductSize.setOnClickListener(this);
        mLinColor.setOnClickListener(this);
        mBtnShopOnlineWoolies.setOnClickListener(this);
    }

    protected void displayProductDetail(String mProductName, String mProductList, String skuId) {
        try {
            SessionDao sessionDao = new SessionDao(QRActivity.this,
                    SessionDao.KEY.STORES_LATEST_PAYLOAD).get();
            mProductJSON = sessionDao.value;
        } catch (Exception e) {
            e.printStackTrace();
        }
        TypeToken<List<WProductDetail>> token = new TypeToken<List<WProductDetail>>() {
        };

        mproductDetail = new Gson().fromJson(mProductList, token.getType());
        assert mproductDetail != null;
        WProductDetail mProduct = mproductDetail.get(0);
        otherSkusList = mProduct.otherSkus;
        getDefaultColor(otherSkusList, skuId);
        mCheckOutLink = mProduct.checkOutLink;
        mDefaultImage = getImageByWidth(mProduct.externalImageRef);
        populateView();
        promoImages(mProduct.promotionImages);
        displayProduct(mProductName);
        initColorParam(mDefaultColor);
        mScrollProductDetail.scrollTo(0, 0);
        String saveText = mProduct.saveText;
        if (TextUtils.isEmpty(saveText)) {
            mTextPromo.setVisibility(View.GONE);
        } else {
            mTextPromo.setVisibility(View.VISIBLE);
            mTextPromo.setText(mProduct.saveText);
        }

        if (otherSkusList.size() > 1) {
            mColorView.setVisibility(View.VISIBLE);
            mRelContainer.setVisibility(View.VISIBLE);
        } else {
            mColorView.setVisibility(View.GONE);
            mRelContainer.setVisibility(View.GONE);
        }
    }

    protected void populateView() {
        mObjProductDetail = mproductDetail.get(0);
        String headerTag = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\">" +
                "<style  type=\"text/css\">body {text-align: justify;font-size:15px !important;text:#50000000 !important;}" +
                "</style></head><body>";
        String footerTag = "</body></html>";
        String descriptionWithoutExtraTag = "";
        if (!TextUtils.isEmpty(mObjProductDetail.longDescription)) {
            descriptionWithoutExtraTag = mObjProductDetail.longDescription
                    .replaceAll("</ul>\n\n<ul>\n", " ")
                    .replaceAll("<p>&nbsp;</p>", "")
                    .replaceAll("<ul><p>&nbsp;</p></ul>", " ");
        }
        mWebDescription.loadDataWithBaseURL("file:///android_res/drawable/",
                headerTag + isEmpty(descriptionWithoutExtraTag) + footerTag,
                "text/html; charset=UTF-8", "UTF-8", null);
        mTextTitle.setText(Html.fromHtml(isEmpty(mObjProductDetail.productName)));
        mProductCode.setText(getString(R.string.product_code)
                + ": "
                + mObjProductDetail.productId);
        updatePrice();
    }


    public void updatePrice() {
        String fromPrice = String.valueOf(mObjProductDetail.fromPrice);
        String wasPrice = highestSKUWasPrice();
        //set size based on highest normal price
        if (TextUtils.isEmpty(wasPrice)) {
            highestSKUPrice();
        }
        productDetailPriceList(mTextPrice, mTextActualPrice, fromPrice, wasPrice, mObjProductDetail.productType);
    }

    public void productDetailPriceList(WTextView wPrice, WTextView WwasPrice,
                                       String price, String wasPrice, String productType) {
        switch (productType) {
            case "clothingProducts":
                if (TextUtils.isEmpty(wasPrice)) {
                    wPrice.setText(WFormatter.formatAmount(price));
                    wPrice.setPaintFlags(0);
                    WwasPrice.setText("");
                } else {
                    if (wasPrice.equalsIgnoreCase(price)) {
                        //wasPrice equals currentPrice
                        wPrice.setText(WFormatter.formatAmount(price));
                        WwasPrice.setText("");
                        wPrice.setPaintFlags(0);
                    } else {
                        wPrice.setText(WFormatter.formatAmount(wasPrice));
                        wPrice.setPaintFlags(wPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        WwasPrice.setText(WFormatter.formatAmount(price));
                    }
                }
                break;

            default:
                if (TextUtils.isEmpty(wasPrice)) {
                    if (Utils.isLocationEnabled(QRActivity.this)) {
                        ArrayList<Double> priceList = new ArrayList<>();
                        for (OtherSku os : mObjProductDetail.otherSkus) {
                            if (!TextUtils.isEmpty(os.price)) {
                                priceList.add(Double.valueOf(os.price));
                            }
                        }
                        if (priceList.size() > 0) {
                            price = String.valueOf(Collections.max(priceList));
                        }
                    }
                    wPrice.setText(WFormatter.formatAmount(price));
                    wPrice.setPaintFlags(0);
                    WwasPrice.setText("");
                } else {
                    if (Utils.isLocationEnabled(QRActivity.this)) {
                        ArrayList<Double> priceList = new ArrayList<>();
                        for (OtherSku os : mObjProductDetail.otherSkus) {
                            if (!TextUtils.isEmpty(os.price)) {
                                priceList.add(Double.valueOf(os.price));
                            }
                        }
                        if (priceList.size() > 0) {
                            price = String.valueOf(Collections.max(priceList));
                        }
                    }

                    if (wasPrice.equalsIgnoreCase(price)) { //wasPrice equals currentPrice
                        wPrice.setText(WFormatter.formatAmount(price));
                        WwasPrice.setText("");
                    } else {
                        wPrice.setText(WFormatter.formatAmount(wasPrice));
                        wPrice.setPaintFlags(wPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        WwasPrice.setText(WFormatter.formatAmount(price));
                    }
                }
                break;
        }
    }


    protected String isEmpty(String value) {
        if (TextUtils.isEmpty(value)) {
            return "";
        } else {
            return value;
        }
    }

    protected void initColorParam(String colour) {
        if (TextUtils.isEmpty(colour)) {
            colour = "";
        }
        mTextColour.setText(colour);
        mAuxiliaryImages = null;
        mAuxiliaryImages = new ArrayList<>();
        mAuxiliaryImages.add(mDefaultImage);
        selectedColor(mDefaultColorRef);
        retrieveJson(colour);
    }

    protected void setIngredients(String ingredients) {
        if (TextUtils.isEmpty(ingredients)) {
            mLinIngredient.setVisibility(View.GONE);
            ingredientLine.setVisibility(View.GONE);
        } else {
            mIngredientList.setText(ingredients);
            mLinIngredient.setVisibility(View.VISIBLE);
            ingredientLine.setVisibility(View.VISIBLE);
        }
    }

    protected void colorParams(int position) {
        String colour = uniqueColorList.get(position).colour;
        String defaultUrl = uniqueColorList.get(position).externalColourRef;
        if (TextUtils.isEmpty(colour)) {
            colour = "";
        }
        mTextColour.setText(colour);
        mAuxiliaryImages = null;
        mAuxiliaryImages = new ArrayList<>();
        mDefaultImage = getSkuExternalImageRef(colour);
        //show default image when imageUrl is empty
        selectedColor(defaultUrl);
        getSKUDefaultSize(colour);
        retrieveJson(colour);
        String size = mTextSelectSize.getText().toString();
        String price = updatePrice(colour, size);
        String wasPrice = updateWasPrice(colour, size);
        retrieveJson(colour);
        if (!TextUtils.isEmpty(price)) {
            productDetailPriceList(mTextPrice, mTextActualPrice,
                    price, wasPrice, mObjProductDetail.productType);
        }
    }


    public String getSkuExternalImageRef(String colour) {
        if (otherSkusList != null) {
            if (otherSkusList.size() > 0) {
                List<OtherSku> otherSku = otherSkusList;
                for (OtherSku sku : otherSku) {
                    if (sku.colour.equalsIgnoreCase(colour)) {
                        return getImageByWidth(sku.externalImageRef);
                    }
                }
            }
        }
        return "";
    }

    public void getSKUDefaultSize(String colour) {
        if (otherSkusList != null) {
            if (otherSkusList.size() > 0) {
                List<OtherSku> otherSku = otherSkusList;
                for (OtherSku sku : otherSku) {
                    if (sku.colour.equalsIgnoreCase(colour)) {
                        if (!TextUtils.isEmpty(sku.size))
                            setSelectedTextSize(sku.size);
                        else
                            setSelectedTextSize("");
                        break;
                    }
                }
            }
        }
    }

    public void setSelectedTextSize(String size) {
        mTextSelectSize.setText(size);
        mTextSelectSize.setTextColor(Color.BLACK);
    }


    protected void selectedColor(String url) {
        if (TextUtils.isEmpty(url)) {
            mImSelectedColor.setImageAlpha(0);
        } else {
            mImSelectedColor.setImageAlpha(255);
            DrawImage drawImage = new DrawImage(this);
            drawImage.displayImage(mImSelectedColor, url);
        }
    }

    protected void retrieveJson(String colour) {
        JSONObject jsProduct;
        if (mAuxiliaryImages != null) {
            mAuxiliaryImages.clear();
        }
        try {
            // Instantiate a JSON object from the request response
            jsProduct = new JSONObject(mProductJSON);
            String mProduct = jsProduct.getString("product");
            JSONObject jsProductList = new JSONObject(mProduct);
            if (jsProductList.has("ingredients")) {
                setIngredients(jsProductList.getString("ingredients"));
            } else {
                setIngredients("");
            }

            //display default image
            if (mAuxiliaryImages != null) {
                if (!TextUtils.isEmpty(mDefaultImage))
                    mAuxiliaryImages.add(0, mDefaultImage);
            }

            String auxiliaryImages = jsProductList.getString("auxiliaryImages");
            JSONObject jsAuxiliaryImages = new JSONObject(auxiliaryImages);
            Iterator<String> keysIterator = jsAuxiliaryImages.keys();
            while (keysIterator.hasNext()) {
                String keyStr = keysIterator.next();
                if (keyStr.toLowerCase().contains(colour.toLowerCase())) {
                    String valueStr = jsAuxiliaryImages.getString(keyStr);
                    JSONObject jsonObject = new JSONObject(valueStr);
                    if (jsonObject.has("externalImageRef")) {
                        mAuxiliaryImages.add(getImageByWidth(jsonObject.getString("externalImageRef")));
                    }
                }
            }

            Set<String> removeAuxiliaryImageDuplicate = new LinkedHashSet<>(mAuxiliaryImages);
            mAuxiliaryImages.clear();
            mAuxiliaryImages.addAll(removeAuxiliaryImageDuplicate);

            mProductViewPagerAdapter = new ProductViewPagerAdapter(this, mAuxiliaryImages);
            mViewPagerProduct.setAdapter(mProductViewPagerAdapter);
            mProductViewPagerAdapter.notifyDataSetChanged();
            setupPagerIndicatorDots();
            mViewPagerProduct.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    for (ImageView anIvArrayDotsPager : ivArrayDotsPager) {
                        anIvArrayDotsPager.setImageResource(R.drawable.unselected_drawable);
                    }
                    ivArrayDotsPager[position].setImageResource(R.drawable.selected_drawable);
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                    // All of this is to inhibit any scrollable container from consuming our touch events as the user is changing pages
                    if (mPreviousState == ViewPager.SCROLL_STATE_IDLE) {
                        if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                            mTouchTarget = mViewPagerProduct;
                        }
                    } else {
                        if (state == ViewPager.SCROLL_STATE_IDLE || state == ViewPager.SCROLL_STATE_SETTLING) {
                            mTouchTarget = null;
                        }
                    }

                    mPreviousState = state;
                }
            });

        } catch (JSONException e) {
        }
    }

    protected void selectedProduct(int position) {
        if (productIsColored) {
            if (mPColourWindow != null) {
                if (mPColourWindow.isShowing()) {
                    mPColourWindow.dismiss();
                }
            }
            colorParams(position);

        } else {
            if (mPSizeWindow != null) {
                if (mPSizeWindow.isShowing()) {
                    mPSizeWindow.dismiss();
                }
            }
            if (uniqueSizeList != null) {
                String selectedSize = uniqueSizeList.get(position).size;
                mTextSelectSize.setText(selectedSize);
                mTextSelectSize.setTextColor(Color.BLACK);
                String colour = mTextColour.getText().toString();
                String price = updatePrice(colour, selectedSize);
                String wasPrice = updateWasPrice(colour, selectedSize);
                retrieveJson(colour);
                if (!TextUtils.isEmpty(price)) {
                    productDetailPriceList(mTextPrice, mTextActualPrice,
                            price, wasPrice, mObjProductDetail.productType);
                }
            }
        }
    }

    protected void bindWithUI(List<OtherSku> otherSkus, boolean productIsColored) {
        this.productIsColored = productIsColored;
        LinearLayoutManager mSlideUpPanelLayoutManager = new LinearLayoutManager(this);
        ProductSizeAdapter productSizeAdapter;
        ProductColorAdapter productColorAdapter;
        mSlideUpPanelLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        if (!productIsColored) {

            //sort ascending
            Collections.sort(otherSkus, new Comparator<OtherSku>() {
                @Override
                public int compare(OtherSku lhs, OtherSku rhs) {
                    return lhs.size.compareToIgnoreCase(rhs.size);
                }
            });

            //remove duplicates
            uniqueSizeList = new ArrayList<>();
            for (OtherSku os : otherSkus) {
                if (!sizeValueExist(uniqueSizeList, os.size)) {
                    uniqueSizeList.add(os);
                }
            }

            productSizeAdapter = new ProductSizeAdapter(uniqueSizeList, this);
            mRecyclerviewSize.addItemDecoration(new SimpleDividerItemDecoration(this));
            mRecyclerviewSize.setLayoutManager(mSlideUpPanelLayoutManager);
            mRecyclerviewSize.setNestedScrollingEnabled(false);
            mRecyclerviewSize.setAdapter(productSizeAdapter);
        } else {

            //sort ascending
            Collections.sort(otherSkus, new Comparator<OtherSku>() {
                @Override
                public int compare(OtherSku lhs, OtherSku rhs) {
                    return lhs.colour.compareToIgnoreCase(rhs.colour);
                }
            });

            //remove duplicates
            uniqueColorList = new ArrayList<>();
            for (OtherSku os : otherSkus) {
                if (!colourValueExist(uniqueColorList, os.colour)) {
                    uniqueColorList.add(os);
                }
            }
            productColorAdapter = new ProductColorAdapter(uniqueColorList, this);
            mColorRecycleSize.addItemDecoration(new SimpleDividerItemDecoration(this));
            mColorRecycleSize.setLayoutManager(mSlideUpPanelLayoutManager);
            mColorRecycleSize.setNestedScrollingEnabled(false);
            mColorRecycleSize.setAdapter(productColorAdapter);
        }
    }

    protected void setupPagerIndicatorDots() {
        ivArrayDotsPager = null;
        mLlPagerDots.removeAllViews();
        if (mAuxiliaryImages.size() > 1) {
            ivArrayDotsPager = new ImageView[mAuxiliaryImages.size()];
            for (int i = 0; i < ivArrayDotsPager.length; i++) {
                ivArrayDotsPager[i] = new ImageView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(10, 0, 10, 0);
                ivArrayDotsPager[i].setLayoutParams(params);
                ivArrayDotsPager[i].setImageResource(R.drawable.unselected_drawable);
                //ivArrayDotsPager[i].setAlpha(0.4f);
                ivArrayDotsPager[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        view.setAlpha(1);
                    }
                });
                mLlPagerDots.addView(ivArrayDotsPager[i]);
                mLlPagerDots.bringToFront();
            }
            ivArrayDotsPager[0].setImageResource(R.drawable.selected_drawable);
        }
    }

    protected void displayProduct(String mProductName) {
        if (TextUtils.isEmpty(mProductName)) {
            return;
        }
        int index = 0;
        for (WProductDetail prod : mproductDetail) {
            if (prod.productName.equals(mProductName)) {
                selectedProduct(index);
            }
            index++;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mTouchTarget != null) {
            boolean wasProcessed = mTouchTarget.onTouchEvent(ev);

            if (!wasProcessed) {
                mTouchTarget = null;
            }

            return wasProcessed;
        }
        return super.dispatchTouchEvent(ev);
    }


    protected void promoImages(PromotionImages imPromo) {
        if (imPromo != null) {
            String wSave = imPromo.save;
            String wReward = imPromo.wRewards;
            String wVitality = imPromo.vitality;
            String wNewImage = imPromo.newImage;
            DrawImage drawImage = new DrawImage(this);
            if (!TextUtils.isEmpty(wSave)) {
                mImSave.setVisibility(View.VISIBLE);
                drawImage.displayImage(mImSave, wSave);
            } else {
                mImSave.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(wReward)) {
                mImReward.setVisibility(View.VISIBLE);
                drawImage.displayImage(mImReward, wReward);
            } else {
                mImReward.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(wVitality)) {
                mVitalityView.setVisibility(View.VISIBLE);
                drawImage.displayImage(mVitalityView, wVitality);
            } else {
                mVitalityView.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(wNewImage)) {
                mImNewImage.setVisibility(View.VISIBLE);
                drawImage.displayImage(mImNewImage, wNewImage);

            } else {
                mImNewImage.setVisibility(View.GONE);
            }
        }
    }

    private void dismissSizeDialog() {
        if (mPSizeWindow != null) {
            if (mPSizeWindow.isShowing()) {
                mPSizeWindow.dismiss();
            }
        }
    }

    private void dismissColourDialog() {
        if (mPColourWindow != null) {
            if (mPColourWindow.isShowing()) {
                mPColourWindow.dismiss();
            }
        }
    }

    private boolean colourValueExist(ArrayList<OtherSku> list, String name) {
        for (OtherSku item : list) {
            if (item.colour.equals(name)) {
                return true;
            }
        }
        return false;
    }

    private boolean sizeValueExist(ArrayList<OtherSku> list, String name) {
        for (OtherSku item : list) {
            if (item.size.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public void dismissPopWindow() {
        if (mPColourWindow != null) {
            if (mPColourWindow.isShowing()) {
                mPColourWindow.dismiss();
            }
        }
        if (mPSizeWindow != null) {
            if (mPSizeWindow.isShowing()) {
                mPSizeWindow.dismiss();
            }
        }
    }

    protected String getImageByWidth(String imageUrl) {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        return imageUrl + "?w=" + width + "&q=" + IMAGE_QUALITY;
    }

    protected void getDefaultColor(List<OtherSku> otherSkus, String skuId) {
        for (OtherSku otherSku : otherSkus) {
            if (skuId.equalsIgnoreCase(otherSku.sku)) {
                mDefaultColor = otherSku.colour;
                mDefaultColorRef = otherSku.externalColourRef;
                mDefaultSize = otherSku.size;
            }
        }
    }

    @Override
    public void onSelectedProduct(View v, int position) {

    }

    @Override
    public void onLongPressState(View v, int position) {

    }

    @Override
    public void onSelectedColor(View v, int position) {
        selectedProduct(position);
    }


    private String updatePrice(String colour, String size) {
        String price = "";
        if (otherSkusList != null) {
            if (otherSkusList.size() > 0) {
                for (OtherSku option : otherSkusList) {
                    if (colour.equalsIgnoreCase(option.colour) &&
                            size.equalsIgnoreCase(option.size)) {
                        return option.price;
                    }
                }
            }
        }
        return price;
    }

    private String updateWasPrice(String colour, String size) {
        String wasPrice = "";
        if (otherSkusList != null) {
            if (otherSkusList.size() > 0) {
                for (OtherSku option : otherSkusList) {
                    if (colour.equalsIgnoreCase(option.colour) &&
                            size.equalsIgnoreCase(option.size)) {
                        return option.wasPrice;
                    }
                }
            }
        }
        return wasPrice;
    }


    public String highestSKUWasPrice() {
        String wasPrice = "";
        ArrayList<Double> priceList = new ArrayList<>();
        for (OtherSku os : mObjProductDetail.otherSkus) {
            if (!TextUtils.isEmpty(os.wasPrice)) {
                priceList.add(Double.valueOf(os.wasPrice));
            }
        }
        if (priceList.size() > 0) {
            wasPrice = String.valueOf(Collections.max(priceList));
            for (OtherSku os : mObjProductDetail.otherSkus) {
                if (wasPrice.equalsIgnoreCase(os.wasPrice)) {
                    setSelectedTextSize(os.size);
                }
            }
            return wasPrice;
        }
        return wasPrice;
    }

    public String highestSKUPrice() {
        String price = "";
        ArrayList<Double> priceList = new ArrayList<>();
        for (OtherSku os : mObjProductDetail.otherSkus) {
            if (!TextUtils.isEmpty(os.price)) {
                priceList.add(Double.valueOf(os.price));
            }
        }
        if (priceList.size() > 0) {
            price = String.valueOf(Collections.max(priceList));
            for (OtherSku os : mObjProductDetail.otherSkus) {
                if (price.equalsIgnoreCase(os.price)) {
                    setSelectedTextSize(os.size);
                }
            }
            return price;
        }
        return price;
    }
}
