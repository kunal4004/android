package za.co.woolworths.financial.services.android.util;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class GoogleMapViewPager extends ViewPager {

	private boolean disable;

	public GoogleMapViewPager(Context context) {
		super(context);
	}

	public GoogleMapViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected boolean canScroll(View scrollingView, boolean checkV, int dx, int x, int y) {
		if (scrollingView.getClass().getPackage().getName().contains("maps.")) {
			return true;
		}
		return super.canScroll(scrollingView, checkV, dx, x, y);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		return disable ? false : super.onInterceptTouchEvent(event);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return disable ? false : super.onTouchEvent(event);
	}

	public void disableScroll(Boolean disable) {
		//When disable = true not work the scroll and when disble = false work the scroll
		this.disable = disable;
	}
}
