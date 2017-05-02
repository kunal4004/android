package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.awfs.coordination.R;

import java.io.Serializable;
import java.util.ArrayList;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.ui.adapters.MultipleImageAdapter;
import za.co.woolworths.financial.services.android.ui.views.MultiTouchViewPager;

public class MultipleImageActivity extends AppCompatActivity implements View.OnClickListener {

    private int mCurrentPosition;
    private ArrayList mAuxiliaryImages;
    private MultiTouchViewPager mViewPagerProduct;
    private LinearLayout mLlPagerDots;
    private ImageView[] ivArrayDotsPager;
    private MultipleImageAdapter multipleImageAdapter;
    private int currentPosition = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.product_multiple_images);
        getBundle();
        initView();
        fillAdapter();
        setupPagerIndicatorDots();
        viewPagerListener();

    }

    private void viewPagerListener() {
        mViewPagerProduct.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                updateItem(position);
                for (ImageView anIvArrayDotsPager : ivArrayDotsPager) {
                    anIvArrayDotsPager.setImageResource(R.drawable.unselected_drawable);
                }
                ivArrayDotsPager[position].setImageResource(R.drawable.selected_drawable);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void fillAdapter() {
        mLlPagerDots = (LinearLayout) findViewById(R.id.pager_dots);
        multipleImageAdapter = new MultipleImageAdapter(this, mAuxiliaryImages);
        mViewPagerProduct.setAdapter(multipleImageAdapter);
        mViewPagerProduct.setCurrentItem(mCurrentPosition);
        updateItem(mCurrentPosition);
    }

    private void getBundle() {
        Intent extras = getIntent();
        if (extras != null) {
            mCurrentPosition = extras.getExtras().getInt("position");
            Serializable mSerialiseAuxiliaryImages =
                    getIntent().getSerializableExtra("auxiliaryImages");
            if (mSerialiseAuxiliaryImages != null)
                mAuxiliaryImages = ((ArrayList) mSerialiseAuxiliaryImages);
        }
    }

    private void initView() {
        ImageView mCloseProduct = (ImageView) findViewById(R.id.imCloseProduct);
        mViewPagerProduct = (MultiTouchViewPager) findViewById(R.id.mProductDetailPager);
        mCloseProduct.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        closeView();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imCloseProduct:
                closeView();
                break;
        }
    }

    private void closeView() {
        finish();
        overridePendingTransition(0, 0);
    }

    private void setupPagerIndicatorDots() {
        ivArrayDotsPager = null;
        mLlPagerDots.removeAllViews();
        if (mAuxiliaryImages.size() > 1) {
            ivArrayDotsPager = new ImageView[mAuxiliaryImages.size()];
            for (int i = 0; i < ivArrayDotsPager.length; i++) {
                ivArrayDotsPager[i] = new ImageView(this);
                LinearLayout.LayoutParams params =
                        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(15, 0, 15, 0);
                ivArrayDotsPager[i].setLayoutParams(params);
                ivArrayDotsPager[i].setImageResource(R.drawable.unselected_drawable);
                ivArrayDotsPager[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        view.setAlpha(1);
                    }
                });
                mLlPagerDots.addView(ivArrayDotsPager[i]);
                mLlPagerDots.bringToFront();
            }
            ivArrayDotsPager[mCurrentPosition].setImageResource(R.drawable.selected_drawable);
        }
    }

    public void updateItem(int position) {
        currentPosition = position;
        ((WoolworthsApplication) getApplication()).setMultiImagePosition(currentPosition);//Save current viewpager position
    }
}
