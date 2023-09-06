package za.co.woolworths.financial.services.android.ui.views.tooltip

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.awfs.coordination.R
import uk.co.deanwild.materialshowcaseview.CircularRevealAnimationFactory
import uk.co.deanwild.materialshowcaseview.FadeAnimationFactory
import uk.co.deanwild.materialshowcaseview.IAnimationFactory
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig
import uk.co.deanwild.materialshowcaseview.shape.CircleShape
import uk.co.deanwild.materialshowcaseview.shape.NoShape
import uk.co.deanwild.materialshowcaseview.shape.RectangleShape
import uk.co.deanwild.materialshowcaseview.shape.Shape
import uk.co.deanwild.materialshowcaseview.target.Target
import uk.co.deanwild.materialshowcaseview.target.ViewTarget
import za.co.woolworths.financial.services.android.util.Utils

class WMaterialShowcaseViewV2 : FrameLayout, OnTouchListener, View.OnClickListener, TooltipDialog {

    interface IWalkthroughActionListener {
        fun onWalkthroughActionButtonClick(feature: TooltipDialog.Feature?)
        fun onPromptDismiss(feature: TooltipDialog.Feature?)
    }

    private var mOldHeight = 0
    private var mOldWidth = 0
    private var mBitmap: Bitmap? = null // = new WeakReference<>(null);
    private var mCanvas: Canvas? = null
    private var mEraser: Paint? = null
    private var mTarget: Target? = null
    private var mShape: Shape? = null
    private var mXPosition = 0
    private var mYPosition = 0
    private var mWasDismissed = false
    private var mShapePadding = ShowcaseConfig.DEFAULT_SHAPE_PADDING
    private var mContentBox: View? = null
    private var mGravity = 0
    private var mContentBottomMargin = 0
    private var mContentTopMargin = 0
    private var mDismissOnTouch = false
    private var mShouldRender = true // flag to decide when we should actually render
    private var mMaskColour = 0
    private var mAnimationFactory: IAnimationFactory? = null
    private val mShouldAnimate = true
    private var mUseFadeAnimation = false
    private var mFadeDurationInMillis = ShowcaseConfig.DEFAULT_FADE_TIME
    private var mHandler: Handler? = null
    private var mDelayInMillis: Long = 1000
    private var mLayoutListener: UpdateOnGlobalLayout? = null
    private var mTargetTouchable = false
    private var mDismissOnTargetTouch = true
    private var mTvTap: TextView? = null
    private var mIvLocation: ImageView? = null
    private var mTvTapMessage: TextView? = null
    private var mTvTitle: TextView? = null
    private var mArrowIcon: ImageView? = null
    private var mTvDescription: TextView? = null
    private var mBtnNext: TextView? = null
    private var actionListener: IWalkthroughActionListener? = null
    private var feature: TooltipDialog.Feature? = null
    private var mContentView: View? = null
    private var mTvCounter: TextView? = null

    constructor(context: Context, feature: TooltipDialog.Feature?) : super(context) {
        init()
        this.feature = feature
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        setWillNotDraw(false)

        // make sure we add a global layout listener so we can adapt to changes
        mLayoutListener = UpdateOnGlobalLayout()
        viewTreeObserver.addOnGlobalLayoutListener(mLayoutListener)

        // consume touch events
        setOnTouchListener(this)
        mMaskColour = Color.parseColor(ShowcaseConfig.DEFAULT_MASK_COLOUR)
        visibility = INVISIBLE
        mContentView = LayoutInflater.from(context)
            .inflate(R.layout.layout_tooltip_fulfilment_and_location, this, true)
        mContentBox = mContentView?.findViewById(R.id.rlRootView)
        mTvTitle = mContentView?.findViewById(R.id.tvTitle)
        mTvDescription = mContentView?.findViewById(R.id.tvDescription)
        mBtnNext = mContentView?.findViewById(R.id.btnNext)
        mTvTap = mContentView?.findViewById(R.id.tvTap)
        mTvTapMessage = mContentView?.findViewById(R.id.tvTapMessage)
        mIvLocation = mContentView?.findViewById(R.id.ivLocation)
        mArrowIcon = mContentView?.findViewById(R.id.ivArrow)
        mTvCounter = mContentView?.findViewById(R.id.tvCounter)
        mBtnNext?.setOnClickListener(this)
    }

    /**
     * Interesting drawing stuff.
     * We draw a block of semi transparent colour to fill the whole screen then we draw of transparency
     * to create a circular "viewport" through to the underlying content
     *
     * @param canvas, view canvas
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // get current dimensions
        val width = measuredWidth
        val height = measuredHeight

        // don't bother drawing if there is nothing to draw on
        if (width <= 0 || height <= 0) return

        // build a new canvas if needed i.e first pass or new dimensions
        if (mBitmap == null || mCanvas == null || mOldHeight != height || mOldWidth != width) {
            if (mBitmap != null) mBitmap?.recycle()
            mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            mCanvas = Canvas(mBitmap!!)
        }

        // save our 'old' dimensions
        mOldWidth = width
        mOldHeight = height

        // clear canvas
        mCanvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        // draw solid background
        mCanvas?.drawColor(mMaskColour)

        // Prepare eraser Paint if needed
        if (mEraser == null) {
            mEraser = Paint()
            mEraser?.color = -0x1
            mEraser?.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            mEraser?.flags = Paint.ANTI_ALIAS_FLAG
        }

        // draw (erase) shape
        if (mShouldRender) mShape?.draw(
            mCanvas,
            mEraser,
            mXPosition,
            mYPosition,
            mShapePadding
        ) else mShape?.draw(mCanvas, mEraser, 0, 0, 0)

        // Draw the bitmap on our views  canvas.
        canvas.drawBitmap(mBitmap!!, 0f, 0f, null)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        notifyOnDismissed()
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (mDismissOnTouch) {
            hide()
        }
        if (mTargetTouchable && mTarget!!.bounds.contains(event.x.toInt(), event.y.toInt())) {
            if (mDismissOnTargetTouch) {
                hide()
            }
            return false
        }
        return true
    }

    private fun notifyOnDismissed() {
        actionListener?.onPromptDismiss(feature)
    }

    /**
     * Dismiss button clicked
     *
     * @param v, view
     */
    override fun onClick(v: View) {
        if (v.id == R.id.btnNext) {
            if (actionListener == null) return
            hide()
            actionListener?.onWalkthroughActionButtonClick(feature)
        }
    }

    /**
     * Tells us about the "Target" which is the view we want to anchor to.
     * We figure out where it is on screen and (optionally) how big it is.
     * We also figure out whether to place our content and dismiss button above or below it.
     *
     * @param target, target view
     */
    fun setTarget(target: Target?) {
        mTarget = target
        if (mTarget != null) {

            // apply the target position
            val targetPoint = mTarget!!.point
            val targetBounds = mTarget!!.bounds
            setPosition(targetPoint)

            // now figure out whether to put content above or below it
            val height = measuredHeight
            val midPoint = height / 2
            val yPos = targetPoint.y
            var radius = Math.max(targetBounds.height(), targetBounds.width()) / 2
            if (mShape != null) {
                mShape!!.updateTarget(mTarget)
                radius = mShape!!.height / 2
            }
            if (yPos > midPoint) {
                // target is in lower half of screen, we'll sit above it
                mContentTopMargin = 0
                mContentBottomMargin = height - yPos + radius + mShapePadding
                mGravity = Gravity.BOTTOM
            } else {
                // target is in upper half of screen, we'll sit below it
                mContentTopMargin = yPos + radius + mShapePadding
                mContentBottomMargin = 0
                mGravity = Gravity.TOP
            }
        }
        applyLayoutParams()
    }

    private fun applyLayoutParams() {
        if (mContentBox != null && mContentBox!!.layoutParams != null) {
            val contentLP = mContentBox!!.layoutParams as LayoutParams
            var layoutParamsChanged = false
            if (contentLP.bottomMargin != mContentBottomMargin) {
                contentLP.bottomMargin = mContentBottomMargin
                layoutParamsChanged = true
            }
            if (contentLP.topMargin != mContentTopMargin) {
                contentLP.topMargin = mContentTopMargin
                layoutParamsChanged = true
            }
            if (contentLP.gravity != mGravity) {
                contentLP.gravity = mGravity
                layoutParamsChanged = true
            }

            /*
             * Only apply the layout params if we've actually changed them, otherwise we'll get stuck in a layout loop
             */if (layoutParamsChanged) mContentBox!!.layoutParams = contentLP
        }
    }

    private fun setPosition(point: Point) {
        setPosition(point.x, point.y)
    }

    private fun setPosition(x: Int, y: Int) {
        mXPosition = x
        mYPosition = y
    }

    private fun setTitle(contentText: CharSequence) {
        if (contentText != "") {
            mTvTitle?.text = contentText
        }
    }

    private fun setDescription(contentText: CharSequence) {
        mTvDescription?.text = contentText
    }

    private fun setActionText(actionText: CharSequence) {
        mBtnNext?.text = actionText
    }

    private fun setShapePadding(padding: Int) {
        mShapePadding = padding
    }

    private fun setDismissOnTouch(dismissOnTouch: Boolean) {
        mDismissOnTouch = dismissOnTouch
    }

    private fun setShouldRender(shouldRender: Boolean) {
        mShouldRender = shouldRender
    }

    private fun setMaskColour(maskColour: Int) {
        mMaskColour = maskColour
    }

    private fun setDelay(delayInMillis: Long) {
        mDelayInMillis = delayInMillis
    }

    private fun setFadeDuration(fadeDurationInMillis: Long) {
        mFadeDurationInMillis = fadeDurationInMillis
    }

    private fun setTargetTouchable(targetTouchable: Boolean) {
        mTargetTouchable = targetTouchable
    }

    private fun setDismissOnTargetTouch(dismissOnTargetTouch: Boolean) {
        mDismissOnTargetTouch = dismissOnTargetTouch
    }

    private fun setUseFadeAnimation(useFadeAnimation: Boolean) {
        mUseFadeAnimation = useFadeAnimation
    }

    fun setActionListener(actionListener: IWalkthroughActionListener?) {
        this.actionListener = actionListener
    }

    fun setShape(mShape: Shape?) {
        this.mShape = mShape
    }

    fun setAnimationFactory(animationFactory: IAnimationFactory?) {
        mAnimationFactory = animationFactory
    }

    /**
     * REDRAW LISTENER - this ensures we redraw after activity finishes laying out
     */
    inner class UpdateOnGlobalLayout : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            setTarget(mTarget)
        }
    }

    /**
     * BUILDER CLASS
     * Gives us a builder utility class with a fluent API for eaily configuring showcase views
     */
    class Builder(private val activity: Activity, feature: TooltipDialog.Feature?) {
        private var fullWidth = false
        private var shapeType = CIRCLE_SHAPE
        private val showcaseView: WMaterialShowcaseViewV2

        init {
            showcaseView = WMaterialShowcaseViewV2(activity, feature)
            showcaseView.setContentBasedOnFeature()
        }

        /**
         * Set the title text shown on the ShowcaseView.
         */
        fun setTarget(target: View?): Builder {
            showcaseView.setTarget(ViewTarget(target))
            return this
        }

        /**
         * Set the title text shown on the ShowcaseView.
         */
        fun setActionText(resId: Int): Builder {
            return setActionText(activity.getString(resId))
        }

        fun setActionText(dismissText: CharSequence): Builder {
            showcaseView.setActionText(dismissText)
            return this
        }

        /**
         * Set the content text shown on the ShowcaseView.
         */
        fun setDescription(resId: Int): Builder {
            return setDescription(activity.getString(resId))
        }

        /**
         * Set the descriptive text shown on the ShowcaseView.
         */
        fun setDescription(text: CharSequence): Builder {
            showcaseView.setDescription(text)
            return this
        }

        fun setMessage(text: CharSequence): Builder {
            showcaseView.setMessage(text)
            return this
        }

        /**
         * Set the title text shown on the ShowcaseView.
         */
        fun setTitle(resId: Int): Builder {
            return setTitle(activity.getString(resId))
        }

        /**
         * Set the descriptive text shown on the ShowcaseView as the title.
         */
        fun setTitle(text: CharSequence): Builder {
            showcaseView.setTitle(text)
            return this
        }

        /**
         * Set whether or not the target view can be touched while the showcase is visible.
         *
         *
         * False by default.
         */
        fun setTargetTouchable(targetTouchable: Boolean): Builder {
            showcaseView.setTargetTouchable(targetTouchable)
            return this
        }

        /**
         * Set whether or not the showcase should dismiss when the target is touched.
         *
         *
         * True by default.
         */
        fun setDismissOnTargetTouch(dismissOnTargetTouch: Boolean): Builder {
            showcaseView.setDismissOnTargetTouch(dismissOnTargetTouch)
            return this
        }

        fun setDismissOnTouch(dismissOnTouch: Boolean): Builder {
            showcaseView.setDismissOnTouch(dismissOnTouch)
            return this
        }

        fun setMaskColour(maskColour: Int): Builder {
            showcaseView.setMaskColour(maskColour)
            return this
        }

        fun setDelay(delayInMillis: Int): Builder {
            showcaseView.setDelay(delayInMillis.toLong())
            return this
        }

        fun setFadeDuration(fadeDurationInMillis: Int): Builder {
            showcaseView.setFadeDuration(fadeDurationInMillis.toLong())
            return this
        }

        fun setAction(listener: IWalkthroughActionListener?): Builder {
            showcaseView.setActionListener(listener)
            return this
        }

        fun setShapePadding(padding: Int): Builder {
            showcaseView.setShapePadding(padding)
            return this
        }

        @JvmOverloads
        fun withRectangleShape(fullWidth: Boolean = false): Builder {
            shapeType = RECTANGLE_SHAPE
            this.fullWidth = fullWidth
            return this
        }

        fun setShouldRender(shouldRender: Boolean): Builder {
            showcaseView.setShouldRender(shouldRender)
            return this
        }

        fun useFadeAnimation(): Builder {
            showcaseView.setUseFadeAnimation(true)
            return this
        }

        fun build(): WMaterialShowcaseViewV2 {
            if (showcaseView.mShape == null) {
                when (shapeType) {
                    RECTANGLE_SHAPE -> {
                        showcaseView.setShape(
                            RectangleShape(
                                showcaseView.mTarget!!.bounds,
                                fullWidth
                            )
                        )
                    }

                    CIRCLE_SHAPE -> {
                        showcaseView.setShape(CircleShape(showcaseView.mTarget))
                    }

                    NO_SHAPE -> {
                        showcaseView.setShape(NoShape())
                    }

                    else -> throw IllegalArgumentException("Unsupported shape type: $shapeType")
                }
            }
            if (showcaseView.mAnimationFactory == null) {
                // create our animation factory
                if (!showcaseView.mUseFadeAnimation) {
                    showcaseView.setAnimationFactory(CircularRevealAnimationFactory())
                } else {
                    showcaseView.setAnimationFactory(FadeAnimationFactory())
                }
            }
            return showcaseView
        }

        fun show(): WMaterialShowcaseViewV2 {
            build().show(activity)
            return showcaseView
        }

        fun setArrowIcon(arrow: Int): Builder {
            showcaseView.setArrowIcon(arrow)
            return this
        }

        companion object {
            private const val CIRCLE_SHAPE = 0
            private const val RECTANGLE_SHAPE = 1
            private const val NO_SHAPE = 2
        }
    }

    private fun setMessage(text: CharSequence) {
        mTvTapMessage?.text = text
    }

    private fun setArrowIcon(arrow: Int) {
        mArrowIcon?.setImageResource(arrow)
    }

    private fun setContentBasedOnFeature() {
        if (feature == TooltipDialog.Feature.SHOP_LOCATION) {
            mTvTap?.visibility = VISIBLE
            mTvTapMessage?.visibility = VISIBLE
            mIvLocation?.visibility = VISIBLE
            mTvCounter?.text = "2/2"
        } else {
            mTvTap?.visibility = GONE
            mTvTapMessage?.visibility = GONE
            mIvLocation?.visibility = GONE
            mTvCounter?.text = "1/2"
        }
    }

    override fun removeFromWindow() {
        if (parent != null && parent is ViewGroup) {
            (parent as ViewGroup).removeView(this)
        }
        mBitmap?.recycle()
        mBitmap = null
        mEraser = null
        mAnimationFactory = null
        mCanvas = null
        mHandler = null
        viewTreeObserver.removeGlobalOnLayoutListener(mLayoutListener)
        mLayoutListener = null
    }

    override fun getFeature() = feature

    /**
     * Reveal the showcaseview. Returns a boolean telling us whether we actually did show anything
     *
     * @param activity
     * @return
     */
    override fun show(activity: Activity): Boolean {
        (activity.window.decorView as ViewGroup).addView(this)
        mHandler = Handler(Looper.getMainLooper())
        mHandler?.postDelayed({
            if (mShouldAnimate) {
                animateIn()
            } else {
                visibility = VISIBLE
            }
        }, mDelayInMillis)
        Utils.saveFeatureWalkthoughShowcase(feature)
        return true
    }

    override fun isDismissed() = mWasDismissed

    override fun hide() {
        /**
         * This flag is used to indicate to onDetachedFromWindow that the showcase view was dismissed purposefully (by the user or programmatically)
         */
        mWasDismissed = true
        if (mShouldAnimate) {
            animateOut()
        } else {
            removeFromWindow()
        }
    }

    private fun animateIn() {
        visibility = INVISIBLE
        //The runnable will run after the view's creation.
        mContentView?.post {
            if (mAnimationFactory != null) {
                mAnimationFactory?.animateInView(
                    this@WMaterialShowcaseViewV2,
                    mTarget!!.point,
                    mFadeDurationInMillis
                ) {
                    visibility = VISIBLE
                }
            }
        }
    }

    private fun animateOut() {
        mContentView?.post {
            if (mAnimationFactory != null) {
                mAnimationFactory?.animateOutView(
                    this@WMaterialShowcaseViewV2,
                    mTarget!!.point,
                    mFadeDurationInMillis
                ) {
                    visibility = INVISIBLE
                    removeFromWindow()
                }
            }
        }
    }
}