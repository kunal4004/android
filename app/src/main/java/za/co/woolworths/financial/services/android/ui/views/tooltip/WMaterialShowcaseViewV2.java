package za.co.woolworths.financial.services.android.ui.views.tooltip;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;


import com.awfs.coordination.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import uk.co.deanwild.materialshowcaseview.CircularRevealAnimationFactory;
import uk.co.deanwild.materialshowcaseview.FadeAnimationFactory;
import uk.co.deanwild.materialshowcaseview.IAnimationFactory;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.PrefsManager;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;
import uk.co.deanwild.materialshowcaseview.shape.CircleShape;
import uk.co.deanwild.materialshowcaseview.shape.NoShape;
import uk.co.deanwild.materialshowcaseview.shape.RectangleShape;
import uk.co.deanwild.materialshowcaseview.shape.Shape;
import uk.co.deanwild.materialshowcaseview.target.Target;
import uk.co.deanwild.materialshowcaseview.target.ViewTarget;

public class WMaterialShowcaseViewV2 extends FrameLayout implements View.OnTouchListener, View.OnClickListener {

    public interface IShowcaseListener {
        void onShowcaseDisplayed(WMaterialShowcaseViewV2 showcaseView);

        void onShowcaseDismissed(WMaterialShowcaseViewV2 showcaseView);
    }

    public interface IDetachedListener {
        void onShowcaseDetached(WMaterialShowcaseViewV2 showcaseView, boolean wasDismissed);
    }

    public interface IWalkthroughActionListener {
        void onWalkthroughActionButtonClick(Feature feature);

        void onPromptDismiss(Feature feature);
    }

    private int mOldHeight;
    private int mOldWidth;
    private Bitmap mBitmap;// = new WeakReference<>(null);
    private Canvas mCanvas;
    private Paint mEraser;
    private Target mTarget;
    private Shape mShape;
    private int mXPosition;
    private int mYPosition;
    private boolean mWasDismissed = false;
    private int mShapePadding = ShowcaseConfig.DEFAULT_SHAPE_PADDING;
    private View mContentBox;
    private int mGravity;
    private int mContentBottomMargin;
    private int mContentTopMargin;
    private boolean mDismissOnTouch = false;
    private boolean mShouldRender = true; // flag to decide when we should actually render
    private int mMaskColour;
    private IAnimationFactory mAnimationFactory;
    private final boolean mShouldAnimate = true;
    private boolean mUseFadeAnimation = false;
    private long mFadeDurationInMillis = ShowcaseConfig.DEFAULT_FADE_TIME;
    private Handler mHandler;
    private long mDelayInMillis = 1000;
    private boolean mSingleUse = false; // should display only once
    private PrefsManager mPrefsManager; // used to store state doe single use mode
    List<IShowcaseListener> mListeners; // external listeners who want to observe when we show and dismiss
    private UpdateOnGlobalLayout mLayoutListener;
    private IDetachedListener mDetachedListener;
    private boolean mTargetTouchable = false;
    private boolean mDismissOnTargetTouch = true;

    private TextView mTvTap;

    private ImageView mIvLocation;

    private TextView mTvTapMessage;
    private TextView mTvTitle;

    private ImageView mArrowIcon;
    private TextView mTvDescription;
    private TextView mBtnNext;
    private IWalkthroughActionListener actionListener;
    public Feature feature;
    private View mContentView;
    private TextView mTvCounter;

    public WMaterialShowcaseViewV2(Context context, Feature feature) {
        super(context);
        init(context);
        this.feature = feature;
    }

    public WMaterialShowcaseViewV2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public WMaterialShowcaseViewV2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public WMaterialShowcaseViewV2(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }


    private void init(Context context) {
        setWillNotDraw(false);

        mListeners = new ArrayList<>();

        // make sure we add a global layout listener so we can adapt to changes
        mLayoutListener = new UpdateOnGlobalLayout();
        getViewTreeObserver().addOnGlobalLayoutListener(mLayoutListener);

        // consume touch events
        setOnTouchListener(this);

        mMaskColour = Color.parseColor(ShowcaseConfig.DEFAULT_MASK_COLOUR);
        setVisibility(INVISIBLE);


        mContentView = LayoutInflater.from(getContext()).inflate(R.layout.layout_tooltip_fulfilment_and_location, this, true);
        mContentBox = mContentView.findViewById(R.id.rlRootView);
        mTvTitle = mContentView.findViewById(R.id.tvTitle);
        mTvDescription = mContentView.findViewById(R.id.tvDescription);
        mBtnNext = mContentView.findViewById(R.id.btnNext);
        mTvTap = mContentView.findViewById(R.id.tvTap);
        mTvTapMessage = mContentView.findViewById(R.id.tvTapMessage);
        mIvLocation = mContentView.findViewById(R.id.ivLocation);
        mArrowIcon = mContentView.findViewById(R.id.ivArrow);
        mTvCounter = mContentView.findViewById(R.id.tvCounter);
        mBtnNext.setOnClickListener(this);
    }

    /**
     * Interesting drawing stuff.
     * We draw a block of semi transparent colour to fill the whole screen then we draw of transparency
     * to create a circular "viewport" through to the underlying content
     *
     * @param canvas, view canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // get current dimensions
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();

        // don't bother drawing if there is nothing to draw on
        if (width <= 0 || height <= 0) return;

        // build a new canvas if needed i.e first pass or new dimensions
        if (mBitmap == null || mCanvas == null || mOldHeight != height || mOldWidth != width) {

            if (mBitmap != null) mBitmap.recycle();

            mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

            mCanvas = new Canvas(mBitmap);
        }

        // save our 'old' dimensions
        mOldWidth = width;
        mOldHeight = height;

        // clear canvas
        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        // draw solid background
        mCanvas.drawColor(mMaskColour);

        // Prepare eraser Paint if needed
        if (mEraser == null) {
            mEraser = new Paint();
            mEraser.setColor(0xFFFFFFFF);
            mEraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            mEraser.setFlags(Paint.ANTI_ALIAS_FLAG);
        }

        // draw (erase) shape
        if (mShouldRender) mShape.draw(mCanvas, mEraser, mXPosition, mYPosition, mShapePadding);
        else mShape.draw(mCanvas, mEraser, 0, 0, 0);

        // Draw the bitmap on our views  canvas.
        canvas.drawBitmap(mBitmap, 0, 0, null);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        /*
         * If we're being detached from the window without the mWasDismissed flag then we weren't purposefully dismissed
         * Probably due to an orientation change or user backed out of activity.
         * Ensure we reset the flag so the showcase display again.
         */
        if (!mWasDismissed && mSingleUse && mPrefsManager != null) {
            mPrefsManager.resetShowcase();
        }


        notifyOnDismissed();

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mDismissOnTouch) {
            hide();
        }
        if (mTargetTouchable && mTarget.getBounds().contains((int) event.getX(), (int) event.getY())) {
            if (mDismissOnTargetTouch) {
                hide();
            }
            return false;
        }
        return true;
    }


    private void notifyOnDisplayed() {

        if (mListeners != null) {
            for (IShowcaseListener listener : mListeners) {
                listener.onShowcaseDisplayed(this);
            }
        }
    }

    private void notifyOnDismissed() {
        if (actionListener != null) actionListener.onPromptDismiss(feature);

        if (mListeners != null) {
            for (IShowcaseListener listener : mListeners) {
                listener.onShowcaseDismissed(this);
            }

            mListeners.clear();
            mListeners = null;
        }

        /*
         * internal listener used by sequence for storing progress within the sequence
         */
        if (mDetachedListener != null) {
            mDetachedListener.onShowcaseDetached(this, mWasDismissed);
        }
    }

    /**
     * Dismiss button clicked
     *
     * @param v, view
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnNext) {
            if (actionListener == null) return;
            hide();
            actionListener.onWalkthroughActionButtonClick(feature);
        }
    }

    /**
     * Tells us about the "Target" which is the view we want to anchor to.
     * We figure out where it is on screen and (optionally) how big it is.
     * We also figure out whether to place our content and dismiss button above or below it.
     *
     * @param target, target view
     */
    public void setTarget(Target target) {
        mTarget = target;

        if (mTarget != null) {

            // apply the target position
            Point targetPoint = mTarget.getPoint();
            Rect targetBounds = mTarget.getBounds();
            setPosition(targetPoint);

            // now figure out whether to put content above or below it
            int height = getMeasuredHeight();
            int midPoint = height / 2;
            int yPos = targetPoint.y;

            int radius = Math.max(targetBounds.height(), targetBounds.width()) / 2;
            if (mShape != null) {
                mShape.updateTarget(mTarget);
                radius = mShape.getHeight() / 2;
            }

            if (yPos > midPoint) {
                // target is in lower half of screen, we'll sit above it
                mContentTopMargin = 0;
                mContentBottomMargin = (height - yPos) + radius + mShapePadding;
                mGravity = Gravity.BOTTOM;
            } else {
                // target is in upper half of screen, we'll sit below it
                mContentTopMargin = yPos + radius + mShapePadding;
                mContentBottomMargin = 0;
                mGravity = Gravity.TOP;
            }
        }

        applyLayoutParams();
    }

    private void applyLayoutParams() {

        if (mContentBox != null && mContentBox.getLayoutParams() != null) {
            LayoutParams contentLP = (LayoutParams) mContentBox.getLayoutParams();

            boolean layoutParamsChanged = false;

            if (contentLP.bottomMargin != mContentBottomMargin) {
                contentLP.bottomMargin = mContentBottomMargin;
                layoutParamsChanged = true;
            }

            if (contentLP.topMargin != mContentTopMargin) {
                contentLP.topMargin = mContentTopMargin;
                layoutParamsChanged = true;
            }

            if (contentLP.gravity != mGravity) {
                contentLP.gravity = mGravity;
                layoutParamsChanged = true;
            }

            /*
             * Only apply the layout params if we've actually changed them, otherwise we'll get stuck in a layout loop
             */
            if (layoutParamsChanged) mContentBox.setLayoutParams(contentLP);
        }
    }

    /**
     * SETTERS
     */

    void setPosition(Point point) {
        setPosition(point.x, point.y);
    }

    void setPosition(int x, int y) {
        mXPosition = x;
        mYPosition = y;
    }

    private void setTitle(CharSequence contentText) {
        if (mTvTitle != null && !contentText.equals("")) {
            mTvTitle.setText(contentText);
        }
    }

    private void setDescription(CharSequence contentText) {
        if (mTvDescription != null) {
            mTvDescription.setText(contentText);
        }
    }

    private void setActionText(CharSequence actionText) {
        if (mBtnNext != null) {
            mBtnNext.setText(actionText);
        }
    }

    private void setTitleTextColor(int textColour) {
		/*if (mTitleTextView != null) {
			mTitleTextView.setTextColor(textColour);
		}*/
    }

    private void setContentTextColor(int textColour) {
		/*if (mContentTextView != null) {
			mContentTextView.setTextColor(textColour);
		}*/
    }

    private void setShapePadding(int padding) {
        mShapePadding = padding;
    }

    private void setDismissOnTouch(boolean dismissOnTouch) {
        mDismissOnTouch = dismissOnTouch;
    }

    private void setShouldRender(boolean shouldRender) {
        mShouldRender = shouldRender;
    }

    private void setMaskColour(int maskColour) {
        mMaskColour = maskColour;
    }

    private void setDelay(long delayInMillis) {
        mDelayInMillis = delayInMillis;
    }

    private void setFadeDuration(long fadeDurationInMillis) {
        mFadeDurationInMillis = fadeDurationInMillis;
    }

    private void setTargetTouchable(boolean targetTouchable) {
        mTargetTouchable = targetTouchable;
    }

    private void setDismissOnTargetTouch(boolean dismissOnTargetTouch) {
        mDismissOnTargetTouch = dismissOnTargetTouch;
    }

    private void setUseFadeAnimation(boolean useFadeAnimation) {
        mUseFadeAnimation = useFadeAnimation;
    }

    public void addShowcaseListener(IShowcaseListener showcaseListener) {

        if (mListeners != null) mListeners.add(showcaseListener);
    }

    public void setActionListener(IWalkthroughActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void removeShowcaseListener(MaterialShowcaseSequence showcaseListener) {

        if ((mListeners != null) && mListeners.contains(showcaseListener)) {
            mListeners.remove(showcaseListener);
        }
    }

    void setDetachedListener(IDetachedListener detachedListener) {
        mDetachedListener = detachedListener;
    }

    public void setShape(Shape mShape) {
        this.mShape = mShape;
    }

    public void setAnimationFactory(IAnimationFactory animationFactory) {
        this.mAnimationFactory = animationFactory;
    }

    /**
     * REDRAW LISTENER - this ensures we redraw after activity finishes laying out
     */
    public class UpdateOnGlobalLayout implements ViewTreeObserver.OnGlobalLayoutListener {

        @Override
        public void onGlobalLayout() {
            setTarget(mTarget);
        }
    }


    /**
     * BUILDER CLASS
     * Gives us a builder utility class with a fluent API for eaily configuring showcase views
     */
    public static class Builder {
        private static final int CIRCLE_SHAPE = 0;
        private static final int RECTANGLE_SHAPE = 1;
        private static final int NO_SHAPE = 2;

        private boolean fullWidth = false;
        private int shapeType = CIRCLE_SHAPE;

        final WMaterialShowcaseViewV2 showcaseView;

        private final Activity activity;


        public Builder(Activity activity, Feature feature) {
            this.activity = activity;
            showcaseView = new WMaterialShowcaseViewV2(activity, feature);
            showcaseView.setContentBasedOnFeature();
        }

        /**
         * Set the title text shown on the ShowcaseView.
         */
        public Builder setTarget(View target) {
            showcaseView.setTarget(new ViewTarget(target));
            return this;
        }

        /**
         * Set the title text shown on the ShowcaseView.
         */
        public Builder setActionText(int resId) {
            return setActionText(activity.getString(resId));
        }

        public Builder setActionText(CharSequence dismissText) {
            showcaseView.setActionText(dismissText);
            return this;
        }

        /**
         * Set the content text shown on the ShowcaseView.
         */
        public Builder setDescription(int resId) {
            return setDescription(activity.getString(resId));
        }

        /**
         * Set the descriptive text shown on the ShowcaseView.
         */
        public Builder setDescription(CharSequence text) {
            showcaseView.setDescription(text);
            return this;
        }

        public Builder setMessage(CharSequence text) {
            showcaseView.setMessage(text);
            return this;
        }

        /**
         * Set the title text shown on the ShowcaseView.
         */
        public Builder setTitle(int resId) {
            return setTitle(activity.getString(resId));
        }

        /**
         * Set the descriptive text shown on the ShowcaseView as the title.
         */
        public Builder setTitle(CharSequence text) {
            showcaseView.setTitle(text);
            return this;
        }

        /**
         * Set whether or not the target view can be touched while the showcase is visible.
         * <p>
         * False by default.
         */
        public Builder setTargetTouchable(boolean targetTouchable) {
            showcaseView.setTargetTouchable(targetTouchable);
            return this;
        }

        /**
         * Set whether or not the showcase should dismiss when the target is touched.
         * <p>
         * True by default.
         */
        public Builder setDismissOnTargetTouch(boolean dismissOnTargetTouch) {
            showcaseView.setDismissOnTargetTouch(dismissOnTargetTouch);
            return this;
        }

        public Builder setDismissOnTouch(boolean dismissOnTouch) {
            showcaseView.setDismissOnTouch(dismissOnTouch);
            return this;
        }

        public Builder setMaskColour(int maskColour) {
            showcaseView.setMaskColour(maskColour);
            return this;
        }

        public Builder setDelay(int delayInMillis) {
            showcaseView.setDelay(delayInMillis);
            return this;
        }

        public Builder setFadeDuration(int fadeDurationInMillis) {
            showcaseView.setFadeDuration(fadeDurationInMillis);
            return this;
        }

        public Builder setListener(IShowcaseListener listener) {
            showcaseView.addShowcaseListener(listener);
            return this;
        }

        public Builder setAction(IWalkthroughActionListener listener) {
            showcaseView.setActionListener(listener);
            return this;
        }

        public Builder singleUse(String showcaseID) {
            showcaseView.singleUse(showcaseID);
            return this;
        }

        public Builder setShape(Shape shape) {
            showcaseView.setShape(shape);
            return this;
        }

        public Builder withCircleShape() {
            shapeType = CIRCLE_SHAPE;
            return this;
        }

        public Builder withoutShape() {
            shapeType = NO_SHAPE;
            return this;
        }

        public Builder setShapePadding(int padding) {
            showcaseView.setShapePadding(padding);
            return this;
        }

        public Builder withRectangleShape() {
            return withRectangleShape(false);
        }

        public Builder withRectangleShape(boolean fullWidth) {
            this.shapeType = RECTANGLE_SHAPE;
            this.fullWidth = fullWidth;
            return this;
        }

        public Builder setShouldRender(boolean shouldRender) {
            showcaseView.setShouldRender(shouldRender);
            return this;
        }

        public Builder useFadeAnimation() {
            showcaseView.setUseFadeAnimation(true);
            return this;
        }

        public WMaterialShowcaseViewV2 build() {
            if (showcaseView.mShape == null) {
                switch (shapeType) {
                    case RECTANGLE_SHAPE: {
                        showcaseView.setShape(new RectangleShape(showcaseView.mTarget.getBounds(), fullWidth));
                        break;
                    }
                    case CIRCLE_SHAPE: {
                        showcaseView.setShape(new CircleShape(showcaseView.mTarget));
                        break;
                    }
                    case NO_SHAPE: {
                        showcaseView.setShape(new NoShape());
                        break;
                    }
                    default:
                        throw new IllegalArgumentException("Unsupported shape type: " + shapeType);
                }
            }

            if (showcaseView.mAnimationFactory == null) {
                // create our animation factory
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !showcaseView.mUseFadeAnimation) {
                    showcaseView.setAnimationFactory(new CircularRevealAnimationFactory());
                } else {
                    showcaseView.setAnimationFactory(new FadeAnimationFactory());
                }
            }

            return showcaseView;
        }

        public WMaterialShowcaseViewV2 show() {
            build().show(activity);
            return showcaseView;
        }

        @NotNull
        public Builder setArrowIcon(int arrow) {
            showcaseView.setArrowIcon(arrow);
            return this;
        }
    }

    private void setMessage(CharSequence text) {
        if(mTvTapMessage != null){
            mTvTapMessage.setText(text);
        }
    }

    private void setArrowIcon(int arrow) {
        if (mArrowIcon != null) {
            mArrowIcon.setImageResource(arrow);
        }
    }

    private void setContentBasedOnFeature() {
        if (mTvTap != null && mTvTapMessage != null && mIvLocation != null) {
            if (feature == Feature.SHOP_LOCATION) {
                mTvTap.setVisibility(View.VISIBLE);
                mTvTapMessage.setVisibility(View.VISIBLE);
                mIvLocation.setVisibility(View.VISIBLE);
                mTvCounter.setText("2/2");
            } else {
                mTvTap.setVisibility(View.GONE);
                mTvTapMessage.setVisibility(View.GONE);
                mIvLocation.setVisibility(View.GONE);
                mTvCounter.setText("1/2");
            }
        }
    }

    private void singleUse(String showcaseID) {
        mSingleUse = true;
        mPrefsManager = new PrefsManager(getContext(), showcaseID);
    }

    public void removeFromWindow() {
        if (getParent() != null && getParent() instanceof ViewGroup) {
            ((ViewGroup) getParent()).removeView(this);
        }

        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }

        mEraser = null;
        mAnimationFactory = null;
        mCanvas = null;
        mHandler = null;

        getViewTreeObserver().removeGlobalOnLayoutListener(mLayoutListener);
        mLayoutListener = null;

        if (mPrefsManager != null) mPrefsManager.close();

        mPrefsManager = null;


    }


    /**
     * Reveal the showcaseview. Returns a boolean telling us whether we actually did show anything
     *
     * @param activity
     * @return
     */
    public boolean show(final Activity activity) {
        ((ViewGroup) activity.getWindow().getDecorView()).addView(this);

        mHandler = new Handler();
        mHandler.postDelayed(() -> {

            if (mShouldAnimate) {
                animateIn();
            } else {
                setVisibility(VISIBLE);
                notifyOnDisplayed();
            }
        }, mDelayInMillis);

        return true;
    }

    public void hide() {

        /**
         * This flag is used to indicate to onDetachedFromWindow that the showcase view was dismissed purposefully (by the user or programmatically)
         */
        mWasDismissed = true;

        if (mShouldAnimate) {
            animateOut();
        } else {
            removeFromWindow();
        }
    }

    public boolean isDismissed() {
        return mWasDismissed;
    }

    public void animateIn() {
        setVisibility(INVISIBLE);
        //The runnable will run after the view's creation.
        mContentView.post(new Runnable() {
            @Override
            public void run() {
                if (mAnimationFactory != null) {
                    mAnimationFactory.animateInView(WMaterialShowcaseViewV2.this, mTarget.getPoint(), mFadeDurationInMillis, new IAnimationFactory.AnimationStartListener() {
                        @Override
                        public void onAnimationStart() {
                            setVisibility(View.VISIBLE);
                            notifyOnDisplayed();
                        }
                    });
                }
            }
        });
    }

    public void animateOut() {
        mContentView.post(() -> {
            if (mAnimationFactory != null) {
                mAnimationFactory.animateOutView(WMaterialShowcaseViewV2.this, mTarget.getPoint(), mFadeDurationInMillis, () -> {
                    setVisibility(INVISIBLE);
                    removeFromWindow();
                });
            }
        });
    }

    public void resetSingleUse() {
        if (mSingleUse && mPrefsManager != null) mPrefsManager.resetShowcase();
    }

    /*
     * Static helper method for resetting single use flag
     *
     * @param context
     * @param showcaseID
     */
	/*public static void resetSingleUse(Context context, String showcaseID) {
		PrefsManager.resetShowcase(context, showcaseID);
	}*/

    /**
     * Static helper method for resetting all single use flags
     *
     * @param context
     */
    public static void resetAll(Context context) {
        PrefsManager.resetAll(context);
    }

    public static int getSoftButtonsBarSizePort(Activity activity) {
        // getRealMetrics is only available with API 17 and +
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DisplayMetrics metrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int usableHeight = metrics.heightPixels;
            activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            int realHeight = metrics.heightPixels;
            if (realHeight > usableHeight) return realHeight - usableHeight;
            else return 0;
        }
        return 0;
    }

    public static float convertDpToPixel(float dp) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return Math.round(px);
    }

    public enum Arrow {
        TOP_LEFT(0), BOTTOM_LEFT(1), TOP_CENTER(2), BOTTOM_CENTER(3), TOP_RIGHT(4), BOTTOM_RIGHT(5), NONE(6);


        private int value;

        Arrow(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum Feature {
        SHOP_FULFILMENT(1), SHOP_LOCATION(2);

        private int value;

        Feature(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
