package za.co.woolworths.financial.services.android.util;

import android.content.Context;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.View;

public class CustomViewPager extends ViewPager {

	public CustomViewPager(Context context) {
		super(context);
	}

	public CustomViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected boolean canScroll(View scrollingView, boolean checkV, int dx, int x, int y) {
		if (scrollingView.getClass().getPackage().getName().startsWith("maps.")) {
			return true;
		}
		return super.canScroll(scrollingView, checkV, dx, x, y);
	}

}