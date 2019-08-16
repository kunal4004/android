package za.co.woolworths.financial.services.android.util.barcode;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;

import android.view.View;

final class ViewFinderView extends View {
	private final Paint mMaskPaint;
	private final Paint mFramePaint;
	private final Path mFramePath;
	private Rect mFrameRect;
	private int mFrameCornerSize;
	private float mFrameRatioWidth = 1f;
	private float mFrameRatioHeight = 1f;
	private float mFrameSize = 0.75f;

	public ViewFinderView(@NonNull Context context) {
		super(context);
		mMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mFramePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mFramePaint.setStyle(Paint.Style.STROKE);
		mFramePath = new Path();
	}

	@Override
	protected void onDraw(@NonNull Canvas canvas) {
		Rect frameRect = mFrameRect;
		if (frameRect == null) {
			return;
		}
		int width = canvas.getWidth();
		int height = canvas.getHeight();
		int top = frameRect.getTop();
		int left = frameRect.getLeft();
		int right = frameRect.getRight();
		int bottom = frameRect.getBottom();
		canvas.drawRect(0, 0, width, top, mMaskPaint);
		canvas.drawRect(0, top, left, bottom, mMaskPaint);
		canvas.drawRect(right, top, width, bottom, mMaskPaint);
		canvas.drawRect(0, bottom, width, height, mMaskPaint);
		mFramePath.reset();
		int margin = 80;
		top = frameRect.getTop() - margin;
		left = frameRect.getLeft() - margin;
		right = frameRect.getRight() + margin;
		bottom = frameRect.getBottom() + margin;
		mFramePath.moveTo(left, top + mFrameCornerSize);
		mFramePath.lineTo(left, top);
		mFramePath.lineTo(left + mFrameCornerSize, top);
		mFramePath.moveTo(right - mFrameCornerSize, top);
		mFramePath.lineTo(right, top);
		mFramePath.lineTo(right, top + mFrameCornerSize);
		mFramePath.moveTo(right, bottom - mFrameCornerSize);
		mFramePath.lineTo(right, bottom);
		mFramePath.lineTo(right - mFrameCornerSize, bottom);
		mFramePath.moveTo(left + mFrameCornerSize, bottom);
		mFramePath.lineTo(left, bottom);
		mFramePath.lineTo(left, bottom - mFrameCornerSize);
		canvas.drawPath(mFramePath, mFramePaint);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		invalidateFrameRect(right - left, bottom - top);
	}

	@Nullable
	Rect getFrameRect() {
		return mFrameRect;
	}

	void setFrameAspectRatio(@FloatRange(from = 0, fromInclusive = false) float ratioWidth,
							 @FloatRange(from = 0, fromInclusive = false) float ratioHeight) {
		mFrameRatioWidth = ratioWidth;
		mFrameRatioHeight = ratioHeight;
		invalidateFrameRect();
		if (Utils.isLaidOut(this)) {
			invalidate();
		}
	}

	void setFrameRatioWidth(@FloatRange(from = 0, fromInclusive = false) float ratioWidth) {
		mFrameRatioWidth = ratioWidth;
		invalidateFrameRect();
		if (Utils.isLaidOut(this)) {
			invalidate();
		}
	}

	void setFrameRatioHeight(@FloatRange(from = 0, fromInclusive = false) float ratioHeight) {
		mFrameRatioHeight = ratioHeight;
		invalidateFrameRect();
		if (Utils.isLaidOut(this)) {
			invalidate();
		}
	}

	void setMaskColor(@ColorInt int color) {
		mMaskPaint.setColor(color);
		if (Utils.isLaidOut(this)) {
			invalidate();
		}
	}

	void setFrameColor(@ColorInt int color) {
		mFramePaint.setColor(color);
		if (Utils.isLaidOut(this)) {
			invalidate();
		}
	}

	void setFrameThickness(@Px int thickness) {
		mFramePaint.setStrokeWidth(thickness);
		if (Utils.isLaidOut(this)) {
			invalidate();
		}
	}

	void setFrameCornersSize(@Px int size) {
		mFrameCornerSize = size;
		if (Utils.isLaidOut(this)) {
			invalidate();
		}
	}

	void setFrameSize(@FloatRange(from = 0.1, to = 1.0) float size) {
		mFrameSize = size;
		invalidateFrameRect();
		if (Utils.isLaidOut(this)) {
			invalidate();
		}
	}

	private void invalidateFrameRect() {
		invalidateFrameRect(getWidth(), getHeight());
	}

	private void invalidateFrameRect(int width, int height) {
		if (width > 0 && height > 0) {
			float viewAR = (float) width / (float) height;
			float frameAR = mFrameRatioWidth / mFrameRatioHeight;
			int frameWidth;
			int frameHeight;
			if (viewAR <= frameAR) {
				frameWidth = Math.round(width * mFrameSize);
				frameHeight = Math.round(frameWidth / frameAR);
			} else {
				frameHeight = Math.round(height * mFrameSize);
				frameWidth = Math.round(frameHeight * frameAR);
			}
			int frameLeft = (width - frameWidth) / 2;
			int frameTop = (height - frameHeight) / 2;
			mFrameRect = new Rect(frameLeft, frameTop, frameLeft + frameWidth, frameTop + frameHeight);
		}
	}
}
