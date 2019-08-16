package za.co.woolworths.financial.services.android.ui.views;

import android.content.Context;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class WCustomPager extends ViewPager {

    public boolean viewPagerIsScrollable = false;

    public WCustomPager(Context context) {
        super(context);
    }

    public WCustomPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    int getMeasureExactly(View child, int widthMeasureSpec) {
        child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        int height = child.getMeasuredHeight();
        return MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        boolean wrapHeight = MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST;

        final View tab = getChildAt(0);
        if (tab == null) {
            return;
        }

        int width = getMeasuredWidth();
        if (wrapHeight) {
            // Keep the current measured width.
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        }
        Fragment fragment = ((Fragment) getAdapter().instantiateItem(this, getCurrentItem()));
        heightMeasureSpec = getMeasureExactly(fragment.getView(), widthMeasureSpec);

        // super has to be called again so the new specs are treated as
        // exact measurements.
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        // Never allow swiping to switch between pages
        return isViewPagerIsScrollable();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Never allow swiping to switch between pages
        return isViewPagerIsScrollable();
    }

    public boolean isViewPagerIsScrollable() {
        return viewPagerIsScrollable;
    }

    public void setViewPagerIsScrollable(boolean viewPagerIsScrollable) {
        this.viewPagerIsScrollable = viewPagerIsScrollable;
    }
}
