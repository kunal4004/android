package za.co.woolworths.financial.services.android.ui.views;

import android.content.Context;
import androidx.core.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by dimitrij on 2017/01/23.
 */
public class WScrollView extends NestedScrollView {

    public WScrollView(Context context) {
        super(context);
    }

    public WScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void requestChildFocus(View child, View focused) {
        if (focused instanceof WButton )
            return;
        super.requestChildFocus(child, focused);
    }
}
