package za.co.woolworths.financial.services.android.ui.views;

import android.content.Context;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by dimitrij on 2017/01/18.
 */

public class
WViewPager extends ViewPager {

    private static final int MATCH_PARENT = 1073742592;

    private int currentPageNumber;
    private int pageCount;

    public WViewPager(Context context) {
        super(context);
        prepareUI();
    }

    public WViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        prepareUI();
    }

    private void prepareUI() {
        setOffscreenPageLimit(pageCount);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = 0;
        if (getChildCount() != 0) {
            View child = getChildAt(currentPageNumber);
            child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            int h = child.getMeasuredHeight();
            if (h > height) height = h;
        }
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void onPrevPage() {
        measure(MATCH_PARENT, 0);
    }

    public void onNextPage() {
        measure(MATCH_PARENT, 0);
    }}
