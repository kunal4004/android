package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.awfs.coordination.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.OtherSku;
import za.co.woolworths.financial.services.android.models.dto.PromotionImages;
import za.co.woolworths.financial.services.android.models.dto.WProductDetail;
import za.co.woolworths.financial.services.android.ui.adapters.ProductColorAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.ProductSizeAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.ProductViewPagerAdapter;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.ui.views.WrapContentWebView;
import za.co.woolworths.financial.services.android.util.BaseActivity;
import za.co.woolworths.financial.services.android.util.CircularImageView;
import za.co.woolworths.financial.services.android.util.DrawImage;
import za.co.woolworths.financial.services.android.util.SelectedProductView;
import za.co.woolworths.financial.services.android.util.SimpleDividerItemDecoration;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WFormatter;

import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class ProductDetailViewActivity extends BaseActivity implements SelectedProductView, View.OnClickListener {

    private WTextView mTextSelectSize;
    private RecyclerView mRecyclerviewSize;
    private ProductDetailViewActivity mContext;
    private ArrayList<WProductDetail> mproductDetail;
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
    private ArrayList<OtherSku> uniqueColorList;
    public RecyclerView mColorRecycleSize;
    public ArrayList<OtherSku> uniqueSizeList;
    public ViewPager mViewPagerProduct;
    public ImageView mImCloseProduct;
    public RelativeLayout mLinSize;
    public ImageView mImNewImage;
    public ImageView mImSave;
    public ImageView mImReward;
    public ImageView mVitalityView;
    public String mCheckOutLink;
    private ArrayList<String> mAuxiliaryImages;
    private String mProductJSON;
    private LinearLayout mLlPagerDots;
    private ImageView[] ivArrayDotsPager;
    private String mDefaultImage;
    private CircularImageView mImSelectedColor;
    private View mColorView;
    private WTextView mTextPromo;
    private WTextView mTextActualPrice;
    private WTextView mTextLabelPrice;
    private WTextView mTextColour;
    private WrapContentWebView mWebDescription;
    private WTextView mIngredientList;
    private LinearLayout mLinIngredient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.updateStatusBarBackground(ProductDetailViewActivity.this, R.color.black);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.product_view_detail);
        mContext = this;
        SessionDao sessionDao;

        try {
            sessionDao = new SessionDao(ProductDetailViewActivity.this, SessionDao.KEY.STORES_LATEST_PAYLOAD).get();
            mProductJSON = sessionDao.value;
        } catch (Exception e) {
            e.printStackTrace();
        }
        initUI();
        bundle();
    }

    private void retrieveJson(String colour) {
        JSONObject jsProduct;
        try {
            // Instantiate a JSON object from the request response
            jsProduct = new JSONObject(mProductJSON);
            String mProduct = jsProduct.getString("product");
            JSONObject jsProductList = new JSONObject(mProduct);
            setIngredients(jsProductList.getString("ingredients"));
            String auxiliaryImages = jsProductList.getString("auxiliaryImages");
            JSONObject jsAuxiliaryImages = new JSONObject(auxiliaryImages);
            Iterator<String> keysIterator = jsAuxiliaryImages.keys();
            while (keysIterator.hasNext()) {
                String keyStr = keysIterator.next();
                if (keyStr.toLowerCase().contains(colour.toLowerCase())) {
                    String valueStr = jsAuxiliaryImages.getString(keyStr);
                    JSONObject jsonObject = new JSONObject(valueStr);
                    mAuxiliaryImages.add(jsonObject.getString("imagePath"));
                }
            }
            ProductViewPagerAdapter mProductViewPagerAdapter = new ProductViewPagerAdapter(this, mAuxiliaryImages);
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

                }
            });

        } catch (Exception e) {
            Log.e("sessionDao", e.toString());
        }

    }

    private void setIngredients(String ingredients) {
        if (TextUtils.isEmpty(ingredients)) {
            mLinIngredient.setVisibility(View.GONE);

        } else {
            mLinIngredient.setVisibility(View.VISIBLE);
            mIngredientList.setText(ingredients);
        }
    }

    private void selectedColor(String url) {
        if (TextUtils.isEmpty(url)) {
            mImSelectedColor.setImageAlpha(0);
        } else {
            mImSelectedColor.setImageAlpha(255);
            DrawImage drawImage = new DrawImage(this);
            drawImage.displayImage(mImSelectedColor, url);
        }
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
            mDefaultImage = mproductDetail.get(0).imagePath;
            populateView();
            promoImages(mproductDetail.get(0).promotionImages);
            displayProduct(mProductName);
            initColorParam(0);

            String saveText = mproductDetail.get(0).saveText;
            if (TextUtils.isEmpty(saveText)) {

                mTextPromo.setVisibility(View.GONE);
            } else {
                mTextPromo.setVisibility(View.VISIBLE);
                mTextPromo.setText(mproductDetail.get(0).saveText);
            }
        }
    }

    private void initUI() {
        mColorView = findViewById(R.id.colorView);
        mTextSelectSize = (WTextView) findViewById(R.id.textSelectSize);
        mTextColour = (WTextView) findViewById(R.id.textColour);
        WTextView mTextProductSize = (WTextView) findViewById(R.id.textProductSize);
        mTextTitle = (WTextView) findViewById(R.id.textTitle);
        mTextLabelPrice = (WTextView) findViewById(R.id.textLabelPrice);
        mTextActualPrice = (WTextView) findViewById(R.id.textActualPrice);
        mViewPagerProduct = (ViewPager) findViewById(R.id.mProductDetailPager);
        mTextPrice = (WTextView) findViewById(R.id.textPrice);
        mLinIngredient = (LinearLayout) findViewById(R.id.linIngredient);
        mCategoryName = (WTextView) findViewById(R.id.textType);
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
        mImSelectedColor = (CircularImageView) findViewById(R.id.imSelectedColor);
        mLlPagerDots = (LinearLayout) findViewById(R.id.pager_dots);
        ImageView mImColorArrow = (ImageView) findViewById(R.id.imColorArrow);
        mWebDescription = (WrapContentWebView) findViewById(R.id.webDescription);

        mImNewImage = (ImageView) findViewById(R.id.imNewImage);
        mImSave = (ImageView) findViewById(R.id.imSave);
        mImReward = (ImageView) findViewById(R.id.imReward);
        mVitalityView = (ImageView) findViewById(R.id.imVitality);

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
            }
        }
    }

    private void colorParams(int position) {
        String colour = uniqueColorList.get(position).colour;
        String defaultUrl = uniqueColorList.get(position).externalColourRef;
        String imageUrl = uniqueColorList.get(position).imagePath;
        if (TextUtils.isEmpty(colour)) {
            colour = "";
        }
        mTextColour.setText(colour);
        mAuxiliaryImages = null;
        mAuxiliaryImages = new ArrayList<>();
        //show default image when imageUrl is empty
        if (TextUtils.isEmpty(imageUrl)) {
            mAuxiliaryImages.add(mDefaultImage);
        } else {
            mAuxiliaryImages.add(imageUrl);
        }
        selectedColor(defaultUrl);
        retrieveJson(colour);
    }

    private void initColorParam(int position) {
        String colour = mproductDetail.get(position).otherSkus.get(position).colour;
        String mPSize = mproductDetail.get(position).otherSkus.get(position).size;
        String defaultUrl = mproductDetail.get(position).otherSkus.get(position).externalColourRef;
        String imageUrl = mproductDetail.get(position).otherSkus.get(position).imagePath;
        if (TextUtils.isEmpty(colour)) {
            colour = "";
        }
        if (!TextUtils.isEmpty(mPSize)) {
            mTextSelectSize.setText(mPSize);
        }
        mTextColour.setText(colour);
        mAuxiliaryImages = null;
        mAuxiliaryImages = new ArrayList<>();
        //show default image when imageUrl is empty
        if (TextUtils.isEmpty(imageUrl)) {
            mAuxiliaryImages.add(mDefaultImage);
        } else {
            mAuxiliaryImages.add(imageUrl);
        }
        selectedColor(defaultUrl);
        retrieveJson(colour);
    }

    private void populateView() {
        WProductDetail productDetail = mproductDetail.get(0);

        String headerTag = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\">" +
                "<style  type=\"text/css\">body {text-align: justify;font-size:15px !important;text:#50000000 !important;}" +
                "</style></head><body>";
        String footerTag = "</body></html>";

        mWebDescription.loadData(headerTag + isEmpty(productDetail.longDescription) + footerTag, "text/html; charset=UTF-8", null);
        mTextTitle.setText(isEmpty(productDetail.productName));
        mProductCode.setText(getString(R.string.product_code) + ": " + productDetail.productId);
        String mWasPrice = productDetail.otherSkus.get(0).wasPrice;
        if (productDetail.productType.equalsIgnoreCase("clothingProducts")) {
            mRelContainer.setVisibility(View.VISIBLE);
            mColorView.setVisibility(View.VISIBLE);
            mTextPrice.setText(isEmpty(WFormatter.formatAmount(productDetail.fromPrice)));
            if (!TextUtils.isEmpty(mWasPrice)) {
                mTextActualPrice.setText(WFormatter.formatAmount(productDetail.fromPrice));
                mTextPrice.setText("From: " + WFormatter.formatAmount(mWasPrice));
                mTextPrice.setPaintFlags(mTextPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                mTextLabelPrice.setVisibility(View.GONE);
            } else {
                mTextActualPrice.setText("");
                mTextPrice.setText("From: " + WFormatter.formatAmount(productDetail.fromPrice));
                mTextLabelPrice.setVisibility(View.GONE);
            }
        } else {
            mColorView.setVisibility(View.GONE);
            mRelContainer.setVisibility(View.GONE);
            mTextPrice.setText(WFormatter.formatAmount(productDetail.otherSkus.get(0).price));
            if (!TextUtils.isEmpty(mWasPrice)) {
                mTextActualPrice.setText(WFormatter.formatAmount(productDetail.otherSkus.get(0).price));
                mTextPrice.setText(WFormatter.formatAmount(mWasPrice));
                mTextPrice.setPaintFlags(mTextPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                mTextLabelPrice.setVisibility(View.GONE);
            } else {
                mTextActualPrice.setText("");
                mTextLabelPrice.setVisibility(View.GONE);
            }
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

    private void promoImages(PromotionImages imPromo) {
        if (imPromo != null) {
            String wSave = imPromo.save;
            String wReward = imPromo.wRewards;
            String wVitality = imPromo.vitality;
            String wNewImage = imPromo.newImage;
            DrawImage drawImage = new DrawImage(mContext);
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

    private void setupPagerIndicatorDots() {
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
}



