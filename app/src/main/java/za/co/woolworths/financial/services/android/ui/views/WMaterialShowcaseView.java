package za.co.woolworths.financial.services.android.ui.views;

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
import android.graphics.Typeface;
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

import com.awfs.coordination.R;
import com.daasuu.bl.ArrowDirection;
import com.daasuu.bl.BubbleLayout;

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
import za.co.woolworths.financial.services.android.util.Utils;

/**
 * Created by W7099877 on 2018/08/06.
 */

public class WMaterialShowcaseView extends FrameLayout implements View.OnTouchListener, View.OnClickListener {

    public interface IShowcaseListener {
        void onShowcaseDisplayed(WMaterialShowcaseView showcaseView);

        void onShowcaseDismissed(WMaterialShowcaseView showcaseView);
    }

    public interface IDetachedListener {
        void onShowcaseDetached(WMaterialShowcaseView showcaseView, boolean wasDismissed);
    }

    public interface IWalkthroughActionListener {
        void onWalkthroughActionButtonClick(Feature feature);
        void onPromptDismiss();
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
    private boolean mRenderOverNav = false;
    private int mMaskColour;
    private IAnimationFactory mAnimationFactory;
    private boolean mShouldAnimate = true;
    private boolean mUseFadeAnimation = false;
    private long mFadeDurationInMillis = ShowcaseConfig.DEFAULT_FADE_TIME;
    private Handler mHandler;
    private long mDelayInMillis = 1000;
    private boolean mSingleUse = false; // should display only once
    private PrefsManager mPrefsManager; // used to store state doe single use mode
    List<IShowcaseListener> mListeners; // external listeners who want to observe when we show and dismiss
    private WMaterialShowcaseView.UpdateOnGlobalLayout mLayoutListener;
    private IDetachedListener mDetachedListener;
    private boolean mTargetTouchable = false;
    private boolean mDismissOnTargetTouch = true;
    private ImageView mDismissButton;
    private ImageView mWalkThroughIcon;
    private WTextView mWalkThroughTitle;
    private WTextView mWalkThroughDesc;
    private WTextView mWalkThroughAction;
    private IWalkthroughActionListener actionListener;
    private BubbleLayout windowContainer;
    private WTextView mHideTutorialAction;
    public Feature feature;
    private WTextView mNewFeature;
    private View mContentView;

    public WMaterialShowcaseView(Context context, Feature feature) {
        super(context);
        init(context);
        this.feature = feature;
    }

    public WMaterialShowcaseView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public WMaterialShowcaseView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public WMaterialShowcaseView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }


    private void init(Context context) {
        setWillNotDraw(false);

        mListeners = new ArrayList<>();

        // make sure we add a global layout listener so we can adapt to changes
        mLayoutListener = new WMaterialShowcaseView.UpdateOnGlobalLayout();
        getViewTreeObserver().addOnGlobalLayoutListener(mLayoutListener);

        // consume touch events
        setOnTouchListener(this);

        mMaskColour = Color.parseColor(ShowcaseConfig.DEFAULT_MASK_COLOUR);
        setVisibility(INVISIBLE);


        mContentView = LayoutInflater.from(getContext()).inflate(R.layout.feature_walkthrough_popup, this, true);
        mContentBox = mContentView.findViewById(R.id.content_box);
        mDismissButton = mContentView.findViewById(R.id.close);
        mWalkThroughIcon = mContentView.findViewById(R.id.icon);
        mWalkThroughTitle = mContentView.findViewById(R.id.title);
        mWalkThroughDesc = mContentView.findViewById(R.id.description);
        mWalkThroughAction = mContentView.findViewById(R.id.actionButton);
        windowContainer = mContentView.findViewById(R.id.windowContainer);
        mHideTutorialAction = mContentView.findViewById(R.id.hideFeatureTutorials);
        mNewFeature = mContentView.findViewById(R.id.newFeature);
        mDismissButton.setOnClickListener(this);
        mWalkThroughAction.setOnClickListener(this);
        mHideTutorialAction.setOnClickListener(this);
    }


    /**
     * Interesting drawing stuff.
     * We draw a block of semi transparent colour to fill the whole screen then we draw of transparency
     * to create a circular "viewport" through to the underlying content
     *
     * @param canvas
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
        if (mShouldRender)
            mShape.draw(mCanvas, mEraser, mXPosition, mYPosition, mShapePadding);
        else
            mShape.draw(mCanvas, mEraser, 0, 0, 0);

        // Draw the bitmap on our views  canvas.
        canvas.drawBitmap(mBitmap, 0, 0, null);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        /**
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
        if(actionListener!=null)
            actionListener.onPromptDismiss();

        if (mListeners != null) {
            for (IShowcaseListener listener : mListeners) {
                listener.onShowcaseDismissed(this);
            }

            mListeners.clear();
            mListeners = null;
        }

        /**
         * internal listener used by sequence for storing progress within the sequence
         */
        if (mDetachedListener != null) {
            mDetachedListener.onShowcaseDetached(this, mWasDismissed);
        }
    }

    /**
     * Dismiss button clicked
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.close:
                if (actionListener == null)
                    return;
                hide();
                break;
            case R.id.actionButton:
                if (actionListener == null)
                    return;
                hide();
                actionListener.onWalkthroughActionButtonClick(feature);
                break;
            case R.id.hideFeatureTutorials:
                if (actionListener == null)
                    return;
                hide();
                Utils.enableFeatureWalkThroughTutorials(false);
                break;
            default:
                break;
        }
    }

    /**
     * Tells us about the "Target" which is the view we want to anchor to.
     * We figure out where it is on screen and (optionally) how big it is.
     * We also figure out whether to place our content and dismiss button above or below it.
     *
     * @param target
     */
    public void setTarget(Target target) {
        mTarget = target;

        // update dismiss button state
        updateDismissButton();

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
            FrameLayout.LayoutParams contentLP = (LayoutParams) mContentBox.getLayoutParams();

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

            /**
             * Only apply the layout params if we've actually changed them, otherwise we'll get stuck in a layout loop
             */
            if (layoutParamsChanged)
                mContentBox.setLayoutParams(contentLP);
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
        if (mWalkThroughTitle != null && !contentText.equals("")) {
            mWalkThroughTitle.setText(contentText);
        }
    }

    private void setDescription(CharSequence contentText) {
        if (mWalkThroughDesc != null) {
            mWalkThroughDesc.setText(contentText);
        }
    }

    private void setImage(int resId) {
        if (mWalkThroughIcon != null) {
            mWalkThroughIcon.setBackgroundResource(resId);
        }
    }

    private void setAsNewFeature() {
        if (mNewFeature != null) {
            mNewFeature.setVisibility(VISIBLE);
        }
    }

    private void setArrowPosition(Arrow arrow) {

        if (windowContainer != null) {
            switch (arrow) {
                case TOP_LEFT:
                    windowContainer.setArrowDirection(ArrowDirection.TOP);
                    break;
                case TOP_RIGHT:
                    windowContainer.setArrowDirection(ArrowDirection.TOP);
                    setArrowPosition(windowContainer);
                    break;
                case TOP_CENTER:
                    windowContainer.setArrowDirection(ArrowDirection.TOP_CENTER);
                    break;
                case BOTTOM_LEFT:
                    windowContainer.setArrowDirection(ArrowDirection.BOTTOM);
                    break;
                case BOTTOM_RIGHT:
                    windowContainer.setArrowDirection(ArrowDirection.BOTTOM);
                    setArrowPosition(windowContainer);
                    break;
                case BOTTOM_CENTER:
                    windowContainer.setArrowDirection(ArrowDirection.BOTTOM_CENTER);
                    break;
                case NONE:
                    windowContainer.setArrowHeight(0);
                    windowContainer.setArrowWidth(0);
                    break;
                default:
                    break;
            }
        }
    }


    private void setActionText(CharSequence actionText) {
        if (mWalkThroughAction != null) {
            mWalkThroughAction.setText(actionText);
        }
    }

    private void setDismissStyle(Typeface dismissStyle) {
		/*if (mDismissButton != null) {
			mDismissButton.setTypeface(dismissStyle);

			updateDismissButton();
		}*/
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

    private void setDismissTextColor(int textColour) {
		/*if (mDismissButton != null) {
			mDismissButton.setTextColor(textColour);
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

        if (mListeners != null)
            mListeners.add(showcaseListener);
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
     * Set properties based on a config object
     *
     * @param config
     */
    public void setConfig(ShowcaseConfig config) {
        setDelay(config.getDelay());
        setFadeDuration(config.getFadeDuration());
        setContentTextColor(config.getContentTextColor());
        setDismissTextColor(config.getDismissTextColor());
        setDismissStyle(config.getDismissTextStyle());

        setMaskColour(config.getMaskColor());
        setShape(config.getShape());
        setShapePadding(config.getShapePadding());
        setRenderOverNavigationBar(config.getRenderOverNavigationBar());
    }

    private void updateDismissButton() {
        // hide or show button
		/*if (mDismissButton != null) {
			if (TextUtils.isEmpty(mDismissButton.getText())) {
				mDismissButton.setVisibility(GONE);
			} else {
				mDismissButton.setVisibility(VISIBLE);
			}
		}*/
    }

	/*public boolean hasFired() {
		return mPrefsManager.hasFired();
	}*/

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

        final WMaterialShowcaseView showcaseView;

        private final Activity activity;


        public Builder(Activity activity,Feature feature) {
            this.activity = activity;
            showcaseView = new WMaterialShowcaseView(activity,feature);
        }

        /**
         * Set the title text shown on the ShowcaseView.
         */
        public WMaterialShowcaseView.Builder setTarget(View target) {
            showcaseView.setTarget(new ViewTarget(target));
            return this;
        }

        /**
         * Set the title text shown on the ShowcaseView.
         */
        public WMaterialShowcaseView.Builder setActionText(int resId) {
            return setActionText(activity.getString(resId));
        }

        public WMaterialShowcaseView.Builder setActionText(CharSequence dismissText) {
            showcaseView.setActionText(dismissText);
            return this;
        }

        public WMaterialShowcaseView.Builder setDismissStyle(Typeface dismissStyle) {
            showcaseView.setDismissStyle(dismissStyle);
            return this;
        }

        /**
         * Set the content text shown on the ShowcaseView.
         */
        public WMaterialShowcaseView.Builder setDescription(int resId) {
            return setDescription(activity.getString(resId));
        }

        /**
         * Set the descriptive text shown on the ShowcaseView.
         */
        public WMaterialShowcaseView.Builder setDescription(CharSequence text) {
            showcaseView.setDescription(text);
            return this;
        }

        /**
         * Set the title text shown on the ShowcaseView.
         */
        public WMaterialShowcaseView.Builder setTitle(int resId) {
            return setTitle(activity.getString(resId));
        }

        /**
         * Set the descriptive text shown on the ShowcaseView as the title.
         */
        public WMaterialShowcaseView.Builder setTitle(CharSequence text) {
            showcaseView.setTitle(text);
            return this;
        }

        public WMaterialShowcaseView.Builder setArrowPosition(Arrow arrow) {
            showcaseView.setArrowPosition(arrow);
            return this;
        }

        /**
         * Set the icon shown on the ShowcaseView.
         */
        public WMaterialShowcaseView.Builder setImage(int resId) {
            return setIcon(resId);
        }

        /**
         * Set the icon shown on the ShowcaseView as the icon.
         */
        public WMaterialShowcaseView.Builder setIcon(int resId) {
            showcaseView.setImage(resId);
            return this;
        }

        /**
         * Set AsNewFeature on the ShowcaseView.
         */
        public WMaterialShowcaseView.Builder setAsNewFeature() {
            showcaseView.setAsNewFeature();
            return this;
        }

        /**
         * Set whether or not the target view can be touched while the showcase is visible.
         * <p>
         * False by default.
         */
        public WMaterialShowcaseView.Builder setTargetTouchable(boolean targetTouchable) {
            showcaseView.setTargetTouchable(targetTouchable);
            return this;
        }

        /**
         * Set whether or not the showcase should dismiss when the target is touched.
         * <p>
         * True by default.
         */
        public WMaterialShowcaseView.Builder setDismissOnTargetTouch(boolean dismissOnTargetTouch) {
            showcaseView.setDismissOnTargetTouch(dismissOnTargetTouch);
            return this;
        }

        public WMaterialShowcaseView.Builder setDismissOnTouch(boolean dismissOnTouch) {
            showcaseView.setDismissOnTouch(dismissOnTouch);
            return this;
        }

        public WMaterialShowcaseView.Builder setMaskColour(int maskColour) {
            showcaseView.setMaskColour(maskColour);
            return this;
        }

        public WMaterialShowcaseView.Builder setTitleTextColor(int textColour) {
            showcaseView.setTitleTextColor(textColour);
            return this;
        }

        public WMaterialShowcaseView.Builder setContentTextColor(int textColour) {
            showcaseView.setContentTextColor(textColour);
            return this;
        }

        public WMaterialShowcaseView.Builder setDismissTextColor(int textColour) {
            showcaseView.setDismissTextColor(textColour);
            return this;
        }

        public WMaterialShowcaseView.Builder setDelay(int delayInMillis) {
            showcaseView.setDelay(delayInMillis);
            return this;
        }

        public WMaterialShowcaseView.Builder setFadeDuration(int fadeDurationInMillis) {
            showcaseView.setFadeDuration(fadeDurationInMillis);
            return this;
        }

        public WMaterialShowcaseView.Builder setListener(IShowcaseListener listener) {
            showcaseView.addShowcaseListener(listener);
            return this;
        }

        public WMaterialShowcaseView.Builder setAction(IWalkthroughActionListener listener) {
            showcaseView.setActionListener(listener);
            return this;
        }

        public WMaterialShowcaseView.Builder singleUse(String showcaseID) {
            showcaseView.singleUse(showcaseID);
            return this;
        }

        public WMaterialShowcaseView.Builder setShape(Shape shape) {
            showcaseView.setShape(shape);
            return this;
        }

        public WMaterialShowcaseView.Builder withCircleShape() {
            shapeType = CIRCLE_SHAPE;
            return this;
        }

        public WMaterialShowcaseView.Builder withoutShape() {
            shapeType = NO_SHAPE;
            return this;
        }

        public WMaterialShowcaseView.Builder setShapePadding(int padding) {
            showcaseView.setShapePadding(padding);
            return this;
        }

        public WMaterialShowcaseView.Builder withRectangleShape() {
            return withRectangleShape(false);
        }

        public WMaterialShowcaseView.Builder withRectangleShape(boolean fullWidth) {
            this.shapeType = RECTANGLE_SHAPE;
            this.fullWidth = fullWidth;
            return this;
        }

        public WMaterialShowcaseView.Builder setShouldRender(boolean shouldRender) {
            showcaseView.setShouldRender(shouldRender);
            return this;
        }

        public WMaterialShowcaseView.Builder renderOverNavigationBar() {
            // Note: This only has an effect in Lollipop or above.
            showcaseView.setRenderOverNavigationBar(true);
            return this;
        }

        public WMaterialShowcaseView.Builder useFadeAnimation() {
            showcaseView.setUseFadeAnimation(true);
            return this;
        }

        public WMaterialShowcaseView build() {
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

        public WMaterialShowcaseView show() {
            build().show(activity);
            return showcaseView;
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

        if (mPrefsManager != null)
            mPrefsManager.close();

        mPrefsManager = null;


    }


    /**
     * Reveal the showcaseview. Returns a boolean telling us whether we actually did show anything
     *
     * @param activity
     * @return
     */
    public boolean show(final Activity activity) {

        /**
         * if we're in single use mode and have already shot our bolt then do nothing
         *//*
		if (mSingleUse) {
			if (mPrefsManager.hasFired()) {
				return false;
			} else {
				mPrefsManager.setFired();
			}
		}*/

        ((ViewGroup) activity.getWindow().getDecorView()).addView(this);


        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (mShouldAnimate) {
                    animateIn();
                } else {
                    setVisibility(VISIBLE);
                    notifyOnDisplayed();
                }
            }
        }, mDelayInMillis);

        updateDismissButton();
        Utils.saveFeatureWalkthoughShowcase(feature);
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
                    mAnimationFactory.animateInView(WMaterialShowcaseView.this, mTarget.getPoint(), mFadeDurationInMillis,
                            new IAnimationFactory.AnimationStartListener() {
                                @Override
                                public void onAnimationStart() {
                                    setVisibility(View.VISIBLE);
                                    notifyOnDisplayed();
                                }
                            }
                    );
                }
            }
        });
    }

    public void animateOut() {
        mContentView.post(new Runnable() {
            @Override
            public void run() {
                if (mAnimationFactory != null) {
                    mAnimationFactory.animateOutView(WMaterialShowcaseView.this, mTarget.getPoint(), mFadeDurationInMillis, new IAnimationFactory.AnimationEndListener() {
                        @Override
                        public void onAnimationEnd() {
                            setVisibility(INVISIBLE);
                            removeFromWindow();
                        }
                    });
                }
            }
        });
    }

    public void resetSingleUse() {
        if (mSingleUse && mPrefsManager != null) mPrefsManager.resetShowcase();
    }

    /**
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
            if (realHeight > usableHeight)
                return realHeight - usableHeight;
            else
                return 0;
        }
        return 0;
    }

    private void setRenderOverNavigationBar(boolean mRenderOverNav) {
        this.mRenderOverNav = mRenderOverNav;
    }

    public static float convertDpToPixel(float dp) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return Math.round(px);
    }

    public enum Arrow {
        TOP_LEFT(0),
        BOTTOM_LEFT(1),
        TOP_CENTER(2),
        BOTTOM_CENTER(3),
        TOP_RIGHT(4),
        BOTTOM_RIGHT(5),
        NONE(6);


        private int value;

        Arrow(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public void setArrowPosition(final BubbleLayout view) {
        if (view != null) {
            view.post(new Runnable() {

                          public void run() {
                              float arrowPosition = view.getWidth() - convertDpToPixel(36);
                              view.setArrowPosition(arrowPosition);
                          }
                      }
            );
        }
    }

    public enum Feature{
        BARCODE_SCAN(1),
        FIND_IN_STORE(2),
        DELIVERY_LOCATION(3),
        VOUCHERS(4),
        REFINE(5),
        ACCOUNTS(6),
        SHOPPING_LIST(7),
        STATEMENTS(8),
        CART_REDEEM_VOUCHERS(9),
        CREDIT_SCORE(9);

        private int value;

        Feature(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
