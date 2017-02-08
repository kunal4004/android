package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.awfs.coordination.R;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import za.co.woolworths.financial.services.android.models.dto.OtherSku;
import za.co.woolworths.financial.services.android.models.dto.PromotionImages;
import za.co.woolworths.financial.services.android.models.dto.WProductDetail;
import za.co.woolworths.financial.services.android.ui.adapters.ProductColorAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.ProductSizeAdapter;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.ui.views.WViewPager;
import za.co.woolworths.financial.services.android.util.BaseActivity;
import za.co.woolworths.financial.services.android.util.SelectedProductView;
import za.co.woolworths.financial.services.android.util.SimpleDividerItemDecoration;
import za.co.woolworths.financial.services.android.util.WFormatter;

import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.awfs.coordination.R.id.imNewImage;

public class ProductDetailViewActivity extends BaseActivity implements SelectedProductView, View.OnClickListener {

    private WTextView mTextSelectSize;
    private RecyclerView mRecyclerviewSize;
    private ProductDetailViewActivity mContext;
    private ArrayList<WProductDetail> mproductDetail;
    private WTextView mDescription;
    private WTextView mTextTitle;
    private WTextView mTextPrice;
    private WTextView mCategoryName;
    private LinearLayout mRelContainer;
    private WTextView mProductCode;
    private List<OtherSku> otherSkusList;
    private WTextView mTextSelectColor;
    private PopupWindow mPColourWindow;
    private PopupWindow mPSizeWindow;
    private boolean productIsColored = false;
    private RecyclerView mColorRecycleSize;
    private ArrayList<OtherSku> uniqueColorList;
    private ArrayList<OtherSku> uniqueSizeList;
    private SimpleDraweeView mImSelectedColor;
    private ImageView mColorArrow;
    private WTextView mTextProductSize;
    private WViewPager mViewPagerProduct;
    private SimpleDraweeView mImProductView;
    private ImageView mImCloseProduct;
    private RelativeLayout mLinColor;
    private RelativeLayout mLinSize;
    private SimpleDraweeView mImNewImage;
    private SimpleDraweeView mImSave;
    private SimpleDraweeView mImReward;
    private SimpleDraweeView mVitalityView;
    private WButton mBtnShopOnlineWoolies;
    private String mCheckOutLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.product_view_detail);
        mContext = this;
        initUI();
        bundle();
    }

    private void bundle() {
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            String mProductList = bundle.getString("product_detail");
            String mProductName = bundle.getString("product_name");

            TypeToken<List<WProductDetail>> token = new TypeToken<List<WProductDetail>>() {
            };
            mproductDetail = new Gson().fromJson(mProductList, token.getType());
            assert mproductDetail != null;
            otherSkusList = mproductDetail.get(0).otherSkus;
            mCheckOutLink = mproductDetail.get(0).checkOutLink;
            populateView();
            String selectedImage = mproductDetail.get(0).imagePath;
            if (!TextUtils.isEmpty(selectedImage))
                displayProductImage(selectedImage);

            promoImages(mproductDetail.get(0).promotionImages);
            displayProduct(mProductName);
        }
    }

    private void initUI() {

        mTextSelectSize = (WTextView) findViewById(R.id.textSelectSize);
        mTextProductSize = (WTextView) findViewById(R.id.textProductSize);
        mDescription = (WTextView) findViewById(R.id.description);
        mTextTitle = (WTextView) findViewById(R.id.textTitle);
        mViewPagerProduct = (WViewPager) findViewById(R.id.mProductDetailPager);
        mTextPrice = (WTextView) findViewById(R.id.textPrice);
        mCategoryName = (WTextView) findViewById(R.id.textType);
        mTextSelectColor = (WTextView) findViewById(R.id.textSelectColour);
        mProductCode = (WTextView) findViewById(R.id.product_code);
        mRelContainer = (LinearLayout) findViewById(R.id.linProductContainer);
        mLinColor = (RelativeLayout) findViewById(R.id.linColour);
        mLinSize = (RelativeLayout) findViewById(R.id.linSize);
        mBtnShopOnlineWoolies = (WButton) findViewById(R.id.btnShopOnlineWoolies);
        mColorArrow = (ImageView) findViewById(R.id.mColorArrow);
        mImProductView = (SimpleDraweeView) findViewById(R.id.imProductView);
        mImCloseProduct = (ImageView) findViewById(R.id.imCloseProduct);
        mImSelectedColor = (SimpleDraweeView) findViewById(R.id.imSelectedColor);
        ImageView mImColorArrow = (ImageView) findViewById(R.id.imColorArrow);

        mImNewImage = (SimpleDraweeView) findViewById(imNewImage);
        mImSave = (SimpleDraweeView) findViewById(R.id.imSave);
        mImReward = (SimpleDraweeView) findViewById(R.id.imReward);
        mVitalityView = (SimpleDraweeView) findViewById(R.id.imVitality);

        mTextSelectColor.setOnClickListener(this);
        mTextSelectSize.setOnClickListener(this);
        mImColorArrow.setOnClickListener(this);
        mImSelectedColor.setOnClickListener(this);
        mColorArrow.setOnClickListener(this);
        mTextProductSize.setOnClickListener(this);
        mImCloseProduct.setOnClickListener(this);
        mLinColor.setOnClickListener(this);
        mLinSize.setOnClickListener(this);
        mBtnShopOnlineWoolies.setOnClickListener(this);
    }

    private void bindWithUI(List<OtherSku> otherSkus, boolean productIsColored) {
        this.productIsColored = productIsColored;
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        ProductSizeAdapter productSizeAdapter;
        ProductColorAdapter productColorAdapter;
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
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

            productSizeAdapter = new ProductSizeAdapter(uniqueSizeList, mContext);
            mRecyclerviewSize.addItemDecoration(new SimpleDividerItemDecoration(this));
            mRecyclerviewSize.setLayoutManager(mLayoutManager);
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
            productColorAdapter = new ProductColorAdapter(uniqueColorList, mContext);
            mColorRecycleSize.addItemDecoration(new SimpleDividerItemDecoration(this));
            mColorRecycleSize.setLayoutManager(mLayoutManager);
            mColorRecycleSize.setNestedScrollingEnabled(false);
            mColorRecycleSize.setAdapter(productColorAdapter);
        }
    }

    @Override
    public void onSelectedProduct(View v, int position) {
        selectedProduct(position);
    }

    private void selectedProduct(int position) {
        if (productIsColored) {
            if (mPColourWindow != null) {
                if (mPColourWindow.isShowing()) {
                    mPColourWindow.dismiss();
                }
                String selectedProductList = uniqueColorList.get(position).externalColourRef;
                String mImagePath = otherSkusList.get(0).imagePath;
                if (!TextUtils.isEmpty(selectedProductList)) {
                    mImSelectedColor.setVisibility(View.VISIBLE);
                    ImageRequest request = ImageRequest.fromUri(Uri.parse(selectedProductList));
                    DraweeController controller = Fresco.newDraweeControllerBuilder()
                            .setImageRequest(request)
                            .setAutoPlayAnimations(true)
                            .setOldController(mImSelectedColor.getController()).build();
                    mImSelectedColor.setController(controller);
                    if (!TextUtils.isEmpty(mImagePath)) {
                        ImageRequest mRequest = ImageRequest.fromUri(Uri.parse(mImagePath));
                        DraweeController mController = Fresco.newDraweeControllerBuilder()
                                .setImageRequest(mRequest)
                                .setAutoPlayAnimations(true)
                                .setOldController(mImProductView.getController()).build();
                        mImProductView.setController(mController);
                    }
                } else {
                    try {
                        mImSelectedColor.setVisibility(View.GONE);
                        if (!TextUtils.isEmpty(mImagePath)) {
                            ImageRequest mRequest = ImageRequest.fromUri(Uri.parse(mImagePath));
                            DraweeController mController = Fresco.newDraweeControllerBuilder()
                                    .setImageRequest(mRequest)
                                    .setAutoPlayAnimations(true)
                                    .setOldController(mImProductView.getController()).build();
                            mImProductView.setController(mController);
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
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
            }
        }
    }


    private void populateView() {
        WProductDetail productDetail = mproductDetail.get(0);
        mDescription.setText(Html.fromHtml(isEmpty(productDetail.longDescription)));
        mTextTitle.setText(isEmpty(productDetail.productName));
        mProductCode.setText(isEmpty(getString(R.string.product_code) + " : " + productDetail.productId));
        if (productDetail.productType.equalsIgnoreCase("clothingProducts")) {
            mRelContainer.setVisibility(View.VISIBLE);
            mTextPrice.setText(isEmpty(getString(R.string.product_from) + ": "
                    + WFormatter.formatAmount(productDetail.fromPrice)));
        } else {
            mRelContainer.setVisibility(View.GONE);
            mTextPrice.setText(WFormatter.formatAmount(productDetail.fromPrice));
        }
        mCategoryName.setText(productDetail.categoryName);
    }

    private String isEmpty(String value) {
        if (TextUtils.isEmpty(value)) {
            return "";
        } else {
            return value;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        overridePendingTransition(R.anim.anim_slide_up, 0);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, R.anim.anim_side_down);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.textSelectColour:
            case R.id.imSelectedColor:
            case R.id.imColorArrow:
            case R.id.linColour:
                dismissSizeDialog();
                LayoutInflater mlayoutInflater
                        = (LayoutInflater) getBaseContext()
                        .getSystemService(LAYOUT_INFLATER_SERVICE);
                View mPopWindow = mlayoutInflater.inflate(R.layout.product_size_row, null);
                mColorRecycleSize = (RecyclerView) mPopWindow.findViewById(R.id.recyclerviewSize);
                bindWithUI(otherSkusList, true);
                mPColourWindow = new PopupWindow(
                        mPopWindow,
                        LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT);

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
                        LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT);

                mPopLinContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mPSizeWindow.dismiss();
                    }
                });

                mPSizeWindow.showAsDropDown(mTextSelectSize, -50, -180);

                break;

            case R.id.imCloseProduct:
                onBackPressed();
                break;

            case R.id.btnShopOnlineWoolies:
                if (!TextUtils.isEmpty(mCheckOutLink))
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mCheckOutLink)));
                break;
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

    public void displayProductImage(String url) {
        mImProductView.getHierarchy().setPlaceholderImage(ContextCompat
                        .getDrawable(mImProductView.getContext(), R.drawable.rectangle),
                ScalingUtils.ScaleType.CENTER_INSIDE);
        setupImage(mImProductView, url);
    }

    private void setupImage(final SimpleDraweeView simpleDraweeView, final String uri) {
        simpleDraweeView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                simpleDraweeView.getViewTreeObserver().removeOnPreDrawListener(this);
                ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(uri))
                        .setResizeOptions(new ResizeOptions(simpleDraweeView.getWidth(), simpleDraweeView.getHeight()))
                        .build();
                PipelineDraweeController controller = (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
                        .setOldController(simpleDraweeView.getController())
                        .setImageRequest(request)
                        .build();

                simpleDraweeView.setController(controller);
                simpleDraweeView.setImageURI(uri);
                return true;
            }
        });
    }

    private void promoImages(PromotionImages imPromo) {
        if (imPromo != null) {
            String wSave = imPromo.save;
            String wReward = imPromo.wRewards;
            String wVitality = imPromo.vitality;
            String wNewImage = imPromo.newImage;

            if (!TextUtils.isEmpty(wSave)) {
                mImSave.setVisibility(View.VISIBLE);
                setupImage(mImSave, wSave);
            } else {
                mImSave.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(wReward)) {
                mImReward.setVisibility(View.VISIBLE);
                setupImage(mImSave, wReward);
            } else {
                mImReward.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(wVitality)) {
                mVitalityView.setVisibility(View.VISIBLE);
                setupImage(mImSave, wVitality);
            } else {
                mVitalityView.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(wNewImage)) {
                mImNewImage.setVisibility(View.VISIBLE);
                setupImage(mImSave, wNewImage);

            } else {
                mImNewImage.setVisibility(View.GONE);
            }
        }
    }

    private void displayProduct(String mProductName) {
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
}

