package za.co.woolworths.financial.services.android.ui.views;

import androidx.core.widget.NestedScrollView;
import android.view.View;

public class NestedScrollableViewHelper extends ScrollableViewHelper {

    private final NestedScrollView mScroll;

     public NestedScrollableViewHelper(NestedScrollView scroll) {
        this.mScroll = scroll;
    }


    public int getScrollableViewScrollPosition(View scrollableView, boolean isSlidingUp) {
        if (mScroll instanceof NestedScrollView) {
            if (isSlidingUp) {
                return mScroll.getScrollY();
            } else {
                NestedScrollView nsv = ((NestedScrollView) mScroll);
                View child = nsv.getChildAt(0);
                return (child.getBottom() - (nsv.getHeight() + nsv.getScrollY()));
            }
        } else {
            return 0;
        }
    }
}
