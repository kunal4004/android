package za.co.woolworths.financial.services.android.ui.views;

import android.content.Context;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by W7099877 on 25/11/2016.
 */

public class WFragmentViewPager extends ViewPager {
	private boolean isPagingEnabled = true;

	public WFragmentViewPager(Context context) {
		super(context);
	}

	public WFragmentViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return this.isPagingEnabled && super.onTouchEvent(event);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		return this.isPagingEnabled && super.onInterceptTouchEvent(event);
	}

	public void setPagingEnabled(boolean b) {
		this.isPagingEnabled = b;
	}
}
