package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.OtherSku;
import za.co.woolworths.financial.services.android.models.dto.PromotionImages;
import za.co.woolworths.financial.services.android.models.dto.ShoppingList;
import za.co.woolworths.financial.services.android.models.dto.WProductDetail;
import za.co.woolworths.financial.services.android.ui.adapters.ProductColorAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.ProductSizeAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.ProductViewPagerAdapter;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.ui.views.WrapContentWebView;
import za.co.woolworths.financial.services.android.util.CircularImageView;
import za.co.woolworths.financial.services.android.util.DrawImage;
import za.co.woolworths.financial.services.android.util.SelectedProductView;
import za.co.woolworths.financial.services.android.util.SimpleDividerItemDecoration;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WFormatter;

public class WProductDetailActivity extends AppCompatActivity implements View.OnClickListener, SelectedProductView {

    private WTextView mTextSelectSize;
    private RecyclerView mRecyclerviewSize;
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
    private LinearLayout mLlPagerDots;
    private ImageView[] ivArrayDotsPager;
    private String mDefaultImage;
    private CircularImageView mImSelectedColor;
    private View mColorView;
    private WTextView mTextPromo;
    private WTextView mTextActualPrice;
    private WTextView mTextColour;
    private WrapContentWebView mWebDescription;
    private WButton mBtnAddShoppingList;
    private WTextView mIngredientList;
    private LinearLayout mLinIngredient;
    private View ingredientLine;
    public String mProductJSON;
    public NestedScrollView mScrollProductDetail;
    private int mPreviousState;
    private ViewPager mTouchTarget;

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
        mCategoryName = (WTextView) findViewById(R.id.textType);
        mIngredientList = (WTextView) findViewById(R.id.ingredientList);
        mTextPromo = (WTextView) findViewById(R.id.textPromo);
        mTextSelectColor = (WTextView) findViewById(R.id.textSelectColour);
        mProductCode = (WTextView) findViewById(R.id.product_code);
        mRelContainer = (LinearLayout) findViewById(R.id.linProductContainer);
        RelativeLayout mLinColor = (RelativeLayout) findViewById(R.id.linColour);
        mLinSize = (RelativeLayout) findViewById(R.id.linSize);
        mBtnAddShoppingList = (WButton) findViewById(R.id.btnAddShoppingList);
        WButton mBtnShopOnlineWoolies = (WButton) findViewById(R.id.btnShopOnlineWoolies);
        ImageView mColorArrow = (ImageView) findViewById(R.id.mColorArrow);
        mImCloseProduct = (ImageView) findViewById(R.id.imCloseProduct);
        mImSelectedColor = (CircularImageView) findViewById(R.id.imSelectedColor);
        mLlPagerDots = (LinearLayout) findViewById(R.id.pager_dots);
        ImageView mImColorArrow = (ImageView) findViewById(R.id.imColorArrow);
        mWebDescription = (WrapContentWebView) findViewById(R.id.webDescription);
        ingredientLine = findViewById(R.id.ingredientLine);

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

    protected void displayProductDetail(String mProductName, String mProductList) {
        try {
            SessionDao sessionDao = new SessionDao(WProductDetailActivity.this,
                    SessionDao.KEY.STORES_LATEST_PAYLOAD).get();
            mProductJSON = sessionDao.value;
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        mScrollProductDetail.scrollTo(0, 0);

        String saveText = mproductDetail.get(0).saveText;
        if (TextUtils.isEmpty(saveText)) {

            mTextPromo.setVisibility(View.GONE);
        } else {
            mTextPromo.setVisibility(View.VISIBLE);
            mTextPromo.setText(mproductDetail.get(0).saveText);
        }
    }

    protected void populateView() {
        WProductDetail productDetail = mproductDetail.get(0);

        String headerTag = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\">" +
                "<style  type=\"text/css\">body {text-align: justify;font-size:15px !important;text:#50000000 !important;}" +
                "</style></head><body>";
        String footerTag = "</body></html>";
        String descriptionWithoutExtraTag = "";
        if (!TextUtils.isEmpty(productDetail.longDescription)) {
            descriptionWithoutExtraTag = productDetail.longDescription.replaceAll("</ul>\n\n<ul>\n", " ");
        }
        mWebDescription.loadData(headerTag + isEmpty(descriptionWithoutExtraTag) + footerTag, "text/html; charset=UTF-8", null);
        mTextTitle.setText(isEmpty(productDetail.productName));
        mProductCode.setText(getString(R.string.product_code) + ": " + productDetail.productId);
        String mWasPrice = productDetail.otherSkus.get(0).wasPrice;
        if (productDetail.productType.equalsIgnoreCase("clothingProducts")) {
            mRelContainer.setVisibility(View.VISIBLE);
            mColorView.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(mWasPrice)) {
                mTextActualPrice.setText(WFormatter.formatAmount(productDetail.fromPrice));
                mTextPrice.setText("From: " + WFormatter.formatAmount(mWasPrice));
                mTextPrice.setPaintFlags(mTextPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                mTextActualPrice.setText("");
                mTextPrice.setText("From: " + WFormatter.formatAmount(productDetail.fromPrice));
            }
        } else {
            mColorView.setVisibility(View.GONE);
            mRelContainer.setVisibility(View.GONE);
            mTextPrice.setText(WFormatter.formatAmount(productDetail.otherSkus.get(0).price));
            if (!TextUtils.isEmpty(mWasPrice)) {
                mTextActualPrice.setText(WFormatter.formatAmount(productDetail.otherSkus.get(0).price));
                mTextPrice.setText(WFormatter.formatAmount(mWasPrice));
                mTextPrice.setPaintFlags(mTextPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                mTextActualPrice.setText("");
            }
        }
        mCategoryName.setText(productDetail.categoryName);
    }

    protected String isEmpty(String value) {
        if (TextUtils.isEmpty(value)) {
            return "";
        } else {
            return value;
        }
    }

    protected void initColorParam(int position) {
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

    protected void addButton() {
        mBtnAddShoppingList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.addToShoppingCart(WProductDetailActivity.this, new ShoppingList(
                        mproductDetail.get(0).productId,
                        mproductDetail.get(0).productName, false));

                Utils.displayValidationMessage(WProductDetailActivity.this,
                        TransientActivity.VALIDATION_MESSAGE_LIST.SHOPPING_LIST_INFO,
                        "viewShoppingList");
            }
        });
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
        try {
            // Instantiate a JSON object from the request response
            jsProduct = new JSONObject(mProductJSON);
            String mProduct = jsProduct.getString("product");
            JSONObject jsProductList = new JSONObject(mProduct);
            if (jsProductList.has("ingredients")) {

                setIngredients(jsProductList.getString("ingredients"));
            } else {
                ingredientLine.setVisibility(View.GONE);
                mLinIngredient.setVisibility(View.GONE);
            }
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
            Log.e("bling bling", e.toString());
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
                onBackPressed();
                break;

            case R.id.btnShopOnlineWoolies:
                if (!TextUtils.isEmpty(mCheckOutLink))
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mCheckOutLink)));
                break;
        }
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
}
