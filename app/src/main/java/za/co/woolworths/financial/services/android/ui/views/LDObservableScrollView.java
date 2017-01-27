package za.co.woolworths.financial.services.android.ui.views;

/**
 * Created by dimitrij on 2017/01/15.
 */

import android.content.Context;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;

public class LDObservableScrollView extends NestedScrollView {

    private LDObservableScrollViewListener scrollViewListener = null;

    public LDObservableScrollView(Context context) {
        super(context);
    }

    public LDObservableScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public LDObservableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setScrollViewListener(LDObservableScrollViewListener scrollViewListener) {
        this.scrollViewListener = scrollViewListener;
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        if(scrollViewListener != null) {
            scrollViewListener.onScrollChanged(this, x, y, oldx, oldy);
        }
    }

    public interface LDObservableScrollViewListener {

        void onScrollChanged(LDObservableScrollView scrollView, int x, int y, int oldx, int oldy);

    }


}

