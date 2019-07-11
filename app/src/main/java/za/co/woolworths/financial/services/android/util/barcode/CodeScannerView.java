package za.co.woolworths.financial.services.android.util.barcode;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.RequiresApi;
import androidx.annotation.StyleRes;

import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

/**
 * A view to display code scanner preview
 *
 * @see CodeScanner
 */
public class CodeScannerView extends RelativeLayout {
	private static final boolean DEFAULT_AUTO_FOCUS_BUTTON_VISIBLE = false;
	private static final boolean DEFAULT_FLASH_BUTTON_VISIBLE = false;
	private static final int DEFAULT_AUTO_FOCUS_BUTTON_VISIBILITY = GONE;
	private static final int DEFAULT_FLASH_BUTTON_VISIBILITY = GONE;
	private static final int DEFAULT_MASK_COLOR = 0x77000000;
	private static final int DEFAULT_FRAME_COLOR = Color.WHITE;
	private static final int DEFAULT_AUTO_FOCUS_BUTTON_COLOR = Color.WHITE;
	private static final int DEFAULT_FLASH_BUTTON_COLOR = Color.WHITE;
	private static final float DEFAULT_FRAME_THICKNESS_DP = 2f;
	private static final float DEFAULT_FRAME_ASPECT_RATIO_WIDTH = 1f;
	private static final float DEFAULT_FRAME_ASPECT_RATIO_HEIGHT = 1f;
	private static final float DEFAULT_FRAME_CORNER_SIZE_DP = 50f;
	private static final float DEFAULT_FRAME_SIZE = 0.75f;
	private static final float BUTTON_SIZE_DP = 56f;
	private SurfaceView mPreviewView;
	private ViewFinderView mViewFinderView;
	private ImageView mAutoFocusButton;
	private ImageView mFlashButton;
	private Point mPreviewSize;
	private LayoutListener mLayoutListener;
	private CodeScanner mCodeScanner;
	private int mButtonSize;
	private DisplayMetrics mDisplayMetrics;

	/**
	 * A view to display code scanner preview
	 *
	 * @see CodeScanner
	 */
	public CodeScannerView(@NonNull Context context) {
		super(context);
		initialize(context, null, 0, 0);
	}

	/**
	 * A view to display code scanner preview
	 *
	 * @see CodeScanner
	 */
	public CodeScannerView(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		initialize(context, attrs, 0, 0);
	}

	/**
	 * A view to display code scanner preview
	 *
	 * @see CodeScanner
	 */
	public CodeScannerView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initialize(context, attrs, defStyleAttr, 0);
	}

	/**
	 * A view to display code scanner preview
	 *
	 * @see CodeScanner
	 */
	@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
	public CodeScannerView(Context context, AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		initialize(context, attrs, defStyleAttr, defStyleRes);
	}

	private void initialize(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr,
							@StyleRes int defStyleRes) {
		mPreviewView = new SurfaceView(context);
		mPreviewView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		mViewFinderView = new ViewFinderView(context);
		mViewFinderView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		mDisplayMetrics = context.getResources().getDisplayMetrics();
		mButtonSize = Math.round(mDisplayMetrics.density * BUTTON_SIZE_DP);
		mAutoFocusButton = new ImageView(context);
		mAutoFocusButton.setLayoutParams(new LayoutParams(mButtonSize, mButtonSize));
		mAutoFocusButton.setScaleType(ImageView.ScaleType.CENTER);
		mAutoFocusButton.setOnClickListener(new AutoFocusClickListener());
		mFlashButton = new ImageView(context);
		mFlashButton.setLayoutParams(new LayoutParams(mButtonSize, mButtonSize));
		mFlashButton.setScaleType(ImageView.ScaleType.CENTER);
		mFlashButton.setOnClickListener(new FlashClickListener());

		if (attrs == null) {
			mViewFinderView.setFrameAspectRatio(DEFAULT_FRAME_ASPECT_RATIO_WIDTH, DEFAULT_FRAME_ASPECT_RATIO_HEIGHT);
			mViewFinderView.setMaskColor(DEFAULT_MASK_COLOR);
			mViewFinderView.setFrameColor(DEFAULT_FRAME_COLOR);
			mViewFinderView.setFrameThickness(Math.round(DEFAULT_FRAME_THICKNESS_DP * mDisplayMetrics.density));
			mViewFinderView.setFrameCornersSize(Math.round(DEFAULT_FRAME_CORNER_SIZE_DP * mDisplayMetrics.density));
			mViewFinderView.setFrameSize(DEFAULT_FRAME_SIZE);
			mAutoFocusButton.setColorFilter(DEFAULT_AUTO_FOCUS_BUTTON_COLOR);
			mFlashButton.setColorFilter(DEFAULT_FLASH_BUTTON_COLOR);
			mAutoFocusButton.setVisibility(VISIBLE);
			mFlashButton.setVisibility(DEFAULT_FLASH_BUTTON_VISIBILITY);
			mFlashButton.setVisibility(GONE);
			mAutoFocusButton.setVisibility(GONE);
		} else {
			TypedArray a = null;
			try {
				a = context.getTheme()
						.obtainStyledAttributes(attrs, R.styleable.CodeScannerView, defStyleAttr, defStyleRes);
				setMaskColor(a.getColor(R.styleable.CodeScannerView_maskColor, DEFAULT_MASK_COLOR));
				setFrameColor(a.getColor(R.styleable.CodeScannerView_frameColor, DEFAULT_FRAME_COLOR));
				setFrameThickness(a.getDimensionPixelOffset(R.styleable.CodeScannerView_frameThickness,
						Math.round(DEFAULT_FRAME_THICKNESS_DP * mDisplayMetrics.density)));
				setFrameCornersSize(a.getDimensionPixelOffset(R.styleable.CodeScannerView_frameCornersSize,
						Math.round(DEFAULT_FRAME_CORNER_SIZE_DP * mDisplayMetrics.density)));
				setFrameAspectRatio(
						a.getFloat(R.styleable.CodeScannerView_frameAspectRatioWidth, DEFAULT_FRAME_ASPECT_RATIO_WIDTH),
						a.getFloat(R.styleable.CodeScannerView_frameAspectRatioHeight,
								DEFAULT_FRAME_ASPECT_RATIO_HEIGHT));
				setFrameSize(a.getFloat(R.styleable.CodeScannerView_frameSize, DEFAULT_FRAME_SIZE));
				setAutoFocusButtonVisible(a.getBoolean(R.styleable.CodeScannerView_autoFocusButtonVisible,
						DEFAULT_AUTO_FOCUS_BUTTON_VISIBLE));
				setFlashButtonVisible(
						a.getBoolean(R.styleable.CodeScannerView_flashButtonVisible, DEFAULT_FLASH_BUTTON_VISIBLE));
				setAutoFocusButtonColor(
						a.getColor(R.styleable.CodeScannerView_autoFocusButtonColor, DEFAULT_AUTO_FOCUS_BUTTON_COLOR));
				setFlashButtonColor(
						a.getColor(R.styleable.CodeScannerView_flashButtonColor, DEFAULT_FLASH_BUTTON_COLOR));
			} finally {
				if (a != null) {
					a.recycle();
				}
			}
		}

		addView(mPreviewView);
		addView(mViewFinderView);
		addView(mAutoFocusButton);
		addView(mFlashButton);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		int width = right - left;
		int height = bottom - top;
		Point previewSize = mPreviewSize;
		if (previewSize == null) {
			mPreviewView.layout(0, 0, width, height);
		} else {
			int frameLeft = 0;
			int frameTop = 0;
			int frameRight = width;
			int frameBottom = height;
			int previewWidth = previewSize.getX();
			if (previewWidth > width) {
				int d = (previewWidth - width) / 2;
				frameLeft -= d;
				frameRight += d;
			}
			int previewHeight = previewSize.getY();
			if (previewHeight > height) {
				int d = (previewHeight - height) / 2;
				frameTop -= d;
				frameBottom += d;
			}
			mPreviewView.layout(frameLeft, frameTop, frameRight, frameBottom);
		}

		mViewFinderView.layout(0, 0, width, height);
		int buttonSize = mButtonSize;
		mAutoFocusButton.layout(0, 0, buttonSize, buttonSize);
		mFlashButton.layout(width - buttonSize, 0, width, buttonSize);
		LayoutListener listener = mLayoutListener;
		if (listener != null) {
			listener.onLayout(width, height);
		}
	}

	/**
	 * Set color of the space outside of the framing rect
	 *
	 * @param color Mask color
	 */
	public void setMaskColor(@ColorInt int color) {
		mViewFinderView.setMaskColor(color);
	}

	/**
	 * Set color of the frame
	 *
	 * @param color Frame color
	 */
	public void setFrameColor(@ColorInt int color) {
		mViewFinderView.setFrameColor(color);
	}

	/**
	 * Set frame thickness
	 *
	 * @param thickness Frame thickness in pixels
	 */
	public void setFrameThickness(@Px int thickness) {
		if (thickness < 0) {
			throw new IllegalArgumentException("Frame thickness can't be negative");
		}
		mViewFinderView.setFrameThickness(thickness);
	}

	/**
	 * Set length on the frame corners
	 *
	 * @param size Length in pixels
	 */
	public void setFrameCornersSize(@Px int size) {
		if (size < 0) {
			throw new IllegalArgumentException("Frame corners size can't be negative");
		}
		mViewFinderView.setFrameCornersSize(size);
	}

	/**
	 * Set relative frame size where 1.0 means full size
	 *
	 * @param size Relative frame size between 0.1 and 1.0
	 */
	public void setFrameSize(@FloatRange(from = 0.1, to = 1) float size) {
		if (size < 0.1 || size > 1) {
			throw new IllegalArgumentException("Max frame size value should be between 0.1 and 1, inclusive");
		}
		mViewFinderView.setFrameSize(size);
	}

	/**
	 * Set frame aspect ratio (ex. 1:1, 15:10, 16:9, 4:3)
	 *
	 * @param ratioWidth  Frame aspect ratio width
	 * @param ratioHeight Frame aspect ratio height
	 */
	public void setFrameAspectRatio(@FloatRange(from = 0, fromInclusive = false) float ratioWidth,
									@FloatRange(from = 0, fromInclusive = false) float ratioHeight) {
		if (ratioWidth <= 0 || ratioHeight <= 0) {
			throw new IllegalArgumentException("Frame aspect ratio values should be greater than zero");
		}
		mViewFinderView.setFrameAspectRatio(ratioWidth, ratioHeight);
	}

	/**
	 * Set whether auto focus button is visible or not
	 *
	 * @param visible Visibility
	 */
	public void setAutoFocusButtonVisible(boolean visible) {
		mAutoFocusButton.setVisibility(visible ? VISIBLE : INVISIBLE);
	}

	/**
	 * Set whether flash button is visible or not
	 *
	 * @param visible Visibility
	 */
	public void setFlashButtonVisible(boolean visible) {
		mFlashButton.setVisibility(visible ? VISIBLE : INVISIBLE);
	}

	/**
	 * Set auto focus button color
	 *
	 * @param color Color
	 */
	public void setAutoFocusButtonColor(@ColorInt int color) {
		mAutoFocusButton.setColorFilter(color);
	}

	/**
	 * Set flash button color
	 *
	 * @param color Color
	 */
	public void setFlashButtonColor(@ColorInt int color) {
		mFlashButton.setColorFilter(color);
	}

	@NonNull
	SurfaceView getPreviewView() {
		return mPreviewView;
	}

	@NonNull
	ViewFinderView getViewFinderView() {
		return mViewFinderView;
	}

	@Nullable
	Rect getFrameRect() {
		return mViewFinderView.getFrameRect();
	}

	void setPreviewSize(@Nullable Point previewSize) {
		mPreviewSize = previewSize;
		requestLayout();
	}

	void setLayoutListener(@Nullable LayoutListener layoutListener) {
		mLayoutListener = layoutListener;
	}

	void setCodeScanner(@NonNull CodeScanner codeScanner) {
		if (mCodeScanner != null) {
			throw new IllegalStateException("Code scanner has already been set");
		}
		mCodeScanner = codeScanner;
		setAutoFocusEnabled(codeScanner.isAutoFocusEnabled());
		//setFlashEnabled(codeScanner.isFlashEnabled());
	}

	void setAutoFocusEnabled(boolean enabled) {
		mAutoFocusButton.setImageResource(
				android.R.drawable.ic_notification_overlay);
	}

//	void setFlashEnabled(boolean enabled) {
//		mFlashButton.setImageResource(android.R.drawable.alert_light_frame);
//	}

	interface LayoutListener {
		void onLayout(int width, int height);
	}

	private final class AutoFocusClickListener implements OnClickListener {
		@Override
		public void onClick(View view) {
			CodeScanner scanner = mCodeScanner;
			if (scanner == null || !scanner.isAutoFocusSupportedOrUnknown()) {
				return;
			}
		}
	}

	private final class FlashClickListener implements OnClickListener {
		@Override
		public void onClick(View view) {
		}
	}
}
