package za.co.woolworths.financial.services.android.util;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.gms.maps.MapView;

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