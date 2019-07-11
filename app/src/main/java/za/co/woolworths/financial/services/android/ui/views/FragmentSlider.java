package za.co.woolworths.financial.services.android.ui.views;

import android.content.Context;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import za.co.wigroup.androidutils.Util;

public class FragmentSlider extends FrameLayout {
    private ViewPager mViewPager;
    private WTabIndicator mTabIndicator;
    private int indicatorHeight;
    private int indicatorWidth;
    private FragmentSlider.FragmentSliderAdapter mViewPagerAdapter;
    private int mIndicatorBottomPadding;

    public FragmentSlider(Context context) {
        this(context, null);
    }

    public FragmentSlider(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FragmentSlider(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.indicatorHeight = Util.dpToPx(10);
        this.indicatorWidth = Util.dpToPx(50);
        this.mIndicatorBottomPadding = Util.dpToPx(8);
        this.mViewPager = new ViewPager(this.getContext());
        this.mViewPager.setId(za.co.wigroup.androidutils.R.id.view_pager);
        this.addView(this.mViewPager, 0);
        LayoutParams layoutParams = (LayoutParams) this.mViewPager.getLayoutParams();
        layoutParams.height = this.getHeight() - Util.dpToPx(16);
        layoutParams.width = this.getWidth();
        this.mViewPager.setLayoutParams(layoutParams);
        this.mTabIndicator = new WTabIndicator(this.getContext());
        this.addView(this.mTabIndicator, 1);
        this.mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrolled(int i, float v, int i2) {
                FragmentSlider.this.mTabIndicator.setCurrentIndicatorPosition(i);
                FragmentSlider.this.mTabIndicator.setOffset(v);
            }

            public void onPageSelected(int i) {
            }

            public void onPageScrollStateChanged(int i) {
            }
        });
        this.mTabIndicator.setCurrentIndicatorPosition(0);
    }

    public void setFragmentSliderAdapter(FragmentSlider.FragmentSliderAdapter adapter) {
        this.mViewPagerAdapter = adapter;
        this.mViewPager.setAdapter(this.mViewPagerAdapter);
        this.mTabIndicator.setIndicatorCount(this.mViewPagerAdapter.getCount());
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        this.mViewPager.setLeft(l);
        this.mViewPager.setTop(t);
        this.mViewPager.setRight(r);
        this.mViewPager.setBottom(b);
        this.mTabIndicator.setLeft(r / 2 - this.indicatorWidth / 2);
        this.mTabIndicator.setRight(r / 2 + this.indicatorWidth / 2);
        this.mTabIndicator.setTop(b - this.indicatorHeight - this.mIndicatorBottomPadding);
        this.mTabIndicator.setBottom(b - this.mIndicatorBottomPadding);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        LayoutParams layoutParams = (LayoutParams) this.mViewPager.getLayoutParams();
        layoutParams.height = h;
        layoutParams.width = w;
        this.mViewPager.setLayoutParams(layoutParams);
        LayoutParams mTabIndicatorLayoutParams = (LayoutParams) this.mTabIndicator.getLayoutParams();
        mTabIndicatorLayoutParams.height = this.indicatorHeight;
        mTabIndicatorLayoutParams.width = this.indicatorWidth;
        this.mTabIndicator.setLayoutParams(mTabIndicatorLayoutParams);
    }

    public abstract static class FragmentSliderAdapter extends FragmentPagerAdapter {
        public FragmentSliderAdapter(FragmentManager fm) {
            super(fm);
        }
    }

    public void setPage (int i){
        mViewPager.setCurrentItem(i);
    }
}