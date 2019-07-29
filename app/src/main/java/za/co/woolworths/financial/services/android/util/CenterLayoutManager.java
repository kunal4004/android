package za.co.woolworths.financial.services.android.util;

import android.content.Context;
import android.graphics.PointF;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

public class CenterLayoutManager extends LinearLayoutManager {

	private int duration = 0;

	public CenterLayoutManager(Context context) {
		super(context);
	}

	public CenterLayoutManager(Context context, int orientation, boolean reverseLayout) {
		super(context, orientation, reverseLayout);
	}

	public CenterLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	public CenterLayoutManager(Context context, int orientation, boolean reverseLayout, int duration) {
		super(context, orientation, reverseLayout);
		this.duration = duration;
	}

	@Override
	public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state,
									   int position) {
		View firstVisibleChild = recyclerView.getChildAt(0);
		if (firstVisibleChild == null) return;
		int itemHeight = firstVisibleChild.getHeight();
		int currentPosition = recyclerView.getChildPosition(firstVisibleChild);
		int distanceInPixels = Math.abs((currentPosition - position) * itemHeight);
		if (distanceInPixels == 0) {
			distanceInPixels = (int) Math.abs(firstVisibleChild.getY());
		}
		SmoothScroller smoothScroller = new SmoothScroller(recyclerView.getContext(), distanceInPixels, duration);
		smoothScroller.setTargetPosition(position);
		startSmoothScroll(smoothScroller);
	}

	private class SmoothScroller extends LinearSmoothScroller {
		private static final int TARGET_SEEK_SCROLL_DISTANCE_PX = 10000;
		private final float distanceInPixels;
		private final float duration;

		public SmoothScroller(Context context, int distanceInPixels, int duration) {
			super(context);
			this.distanceInPixels = distanceInPixels;
			float millisPerPx = calculateSpeedPerPixel(context.getResources().getDisplayMetrics());
			this.duration = distanceInPixels < TARGET_SEEK_SCROLL_DISTANCE_PX ? (int) (Math.abs(distanceInPixels) * millisPerPx) : duration;
		}

		@Override
		public PointF computeScrollVectorForPosition(int targetPosition) {
			return CenterLayoutManager.this.computeScrollVectorForPosition(targetPosition);
		}

		@Override
		protected int calculateTimeForScrolling(int dx) {
			float proportion = (float) dx / distanceInPixels;
			return (int) (duration * proportion);
		}

		@Override
		public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
			return (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2);
		}
	}
}
