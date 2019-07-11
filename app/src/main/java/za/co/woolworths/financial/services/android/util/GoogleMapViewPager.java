package za.co.woolworths.financial.services.android.util;

import android.content.Context;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class GoogleMapViewPager extends ViewPager {
	private boolean canScroll = false;
	public GoogleMapViewPager(Context context) {
		super(context);
	}
	public GoogleMapViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public void setCanScroll(boolean canScroll) {
		this.canScroll = canScroll;
	}
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		return canScroll && super.onTouchEvent(ev);
	}
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return canScroll && super.onInterceptTouchEvent(ev);
	}
}