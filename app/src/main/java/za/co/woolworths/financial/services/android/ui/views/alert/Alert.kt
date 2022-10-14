package za.co.woolworths.financial.services.android.ui.views.alert

import android.content.Context
import android.widget.FrameLayout
import android.widget.TextView
import za.co.woolworths.financial.services.android.ui.views.WTextView
import android.view.ViewGroup
import com.awfs.coordination.R
import android.view.MotionEvent
import android.view.HapticFeedbackConstants
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import android.text.TextUtils
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import java.lang.Exception

/**
 * Custom Alert View
 */
class Alert : FrameLayout, View.OnClickListener, Animation.AnimationListener {
    //UI
    private var flClickShield: FrameLayout? = null
    var alertBackground: FrameLayout? = null
        private set
    var title: TextView? = null
        private set
    private var tvText: WTextView? = null
    var icon: ImageView? = null
        private set
    private var rlContainer: ViewGroup? = null
    private var slideInAnimation: Animation? = null
    private var slideOutAnimation: Animation? = null
    private var onShowListener: OnShowAlertListener? = null
    private var onHideListener: OnHideAlertListener? = null
    /**
     * Get the Alert's on screen duration
     *
     * @return The given duration, defaulting to 3000 milliseconds
     */
    /**
     * Set the alert's on screen duation
     *
     * @param duration The duration of alert on screen
     */
    var duration = DISPLAY_TIME_IN_SECONDS
    private var enableIconPulse = true
    private var enableInfiniteDuration = false

    /**
     * Flag to ensure we only set the margins once
     */
    private var marginSet = false

    /**
     * Flag to enable / disable haptic feedback
     */
    private var vibrationEnabled = true

    /**
     * This is the default view constructor. It requires a Context, and holds a reference to it.
     * If not cleaned up properly, memory will leak.
     *
     * @param context The Activity Context
     */
    constructor(context: Context) : super(context, null, R.attr.alertStyle) {
        initView()
    }

    /**
     * This is the default view constructor. It requires a Context, and holds a reference to it.
     * If not cleaned up properly, memory will leak.
     *
     * @param context The Activity Context
     * @param attrs   View Attributes
     */
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs, R.attr.alertStyle) {
        initView()
    }

    /**
     * This is the default view constructor. It requires a Context, and holds a reference to it.
     * If not cleaned up properly, memory will leak.
     *
     * @param context      The Activity Context
     * @param attrs        View Attributes
     * @param defStyleAttr Styles
     */
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView()
    }

    private fun initView() {
        inflate(context, R.layout.alerter_alert_view, this)
        isHapticFeedbackEnabled = true
        alertBackground = findViewById<View>(R.id.flAlertBackground) as FrameLayout
        flClickShield = findViewById<View>(R.id.flClickShield) as FrameLayout
        icon = findViewById<View>(R.id.ivIcon) as ImageView
        title = findViewById<View>(R.id.tvTitle) as TextView
        tvText = findViewById<View>(R.id.tvText) as WTextView
        rlContainer = findViewById<View>(R.id.rlContainer) as ViewGroup
        alertBackground!!.setOnClickListener(this)

        //Setup Enter & Exit Animations
        slideInAnimation = AnimationUtils.loadAnimation(context, R.anim.alerter_slide_in_from_top)
        slideOutAnimation = AnimationUtils.loadAnimation(context, R.anim.alerter_slide_out_to_top)
        slideInAnimation?.setAnimationListener(this)

        //Set Animation to be Run when View is added to Window
        animation = slideInAnimation
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (!marginSet) {
            marginSet = true

            // Add a negative top margin to compensate for overshoot enter animation
            val params = layoutParams as MarginLayoutParams
            params.topMargin =
                context.resources.getDimensionPixelSize(R.dimen.alerter_alert_negative_margin_top)
            requestLayout()
        }
    }

    // Release resources once view is detached.
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        slideInAnimation!!.setAnimationListener(null)
    }

    /* Override Methods */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        performClick()
        return super.onTouchEvent(event)
    }

    override fun onClick(v: View) {
        hide()
    }

    override fun setOnClickListener(listener: OnClickListener?) {
        alertBackground!!.setOnClickListener(listener)
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        for (i in 0 until childCount) {
            getChildAt(i).visibility = visibility
        }
    }

    /* Interface Method Implementations */
    override fun onAnimationStart(animation: Animation) {
        if (!isInEditMode) {
            if (vibrationEnabled) {
                performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
            visibility = VISIBLE
        }
    }

    override fun onAnimationEnd(animation: Animation) {
        //Start the Icon Animation once the Alert is settled
        if (enableIconPulse && icon!!.visibility == VISIBLE) {
            try {
                icon!!.startAnimation(AnimationUtils.loadAnimation(context, R.anim.alerter_pulse))
            } catch (ex: Exception) {
                Log.e(javaClass.simpleName, Log.getStackTraceString(ex))
            }
        }
        if (onShowListener != null) {
            onShowListener!!.onShow()
        }

        //Start the Handler to clean up the Alert
        if (!enableInfiniteDuration) {
            postDelayed({ hide() }, duration)
        }
    }

    override fun onAnimationRepeat(animation: Animation) {
        //Ignore
    }
    /* Clean Up Methods */
    /**
     * Cleans up the currently showing alert view.
     */
    fun hide() {
        try {
            slideOutAnimation!!.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {
                    alertBackground!!.setOnClickListener(null)
                    alertBackground!!.isClickable = false
                }

                override fun onAnimationEnd(animation: Animation) {
                    removeFromParent()
                }

                override fun onAnimationRepeat(animation: Animation) {
                    //Ignore
                }
            })
            startAnimation(slideOutAnimation)
        } catch (ex: Exception) {
            Log.e(javaClass.simpleName, Log.getStackTraceString(ex))
        }
    }

    /**
     * Removes Alert View from its Parent Layout
     */
    private fun removeFromParent() {
        postDelayed(object : Runnable {
            override fun run() {
                try {
                    if (parent == null) {
                        Log.e(javaClass.simpleName, "getParent() returning Null")
                    } else {
                        try {
                            (parent as ViewGroup).removeView(this@Alert)
                            if (onHideListener != null) {
                                onHideListener!!.onHide()
                            }
                        } catch (ex: Exception) {
                            Log.e(javaClass.simpleName, "Cannot remove from parent layout")
                        }
                    }
                } catch (ex: Exception) {
                    Log.e(javaClass.simpleName, Log.getStackTraceString(ex))
                }
            }
        }, CLEAN_UP_DELAY_MILLIS.toLong())
    }
    /* Setters and Getters */
    /**
     * Sets the Alert Background colour
     *
     * @param color The qualified colour integer
     */
    fun setAlertBackgroundColor(@ColorInt color: Int) {
        alertBackground!!.setBackgroundColor(color)
    }

    /**
     * Sets the Alert Background Drawable Resource
     *
     * @param resource The qualified drawable integer
     */
    fun setAlertBackgroundResource(@DrawableRes resource: Int) {
        alertBackground!!.setBackgroundResource(resource)
    }

    /**
     * Sets the Alert Background Drawable
     *
     * @param drawable The qualified drawable
     */
    fun setAlertBackgroundDrawable(drawable: Drawable?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            alertBackground!!.background = drawable
        } else {
            alertBackground!!.setBackgroundDrawable(drawable)
        }
    }

    /**
     * Sets the Title of the Alert
     *
     * @param titleId String resource id of the Alert title
     */
    fun setTitle(@StringRes titleId: Int) {
        setTitle(context.getString(titleId))
    }

    /**
     * Sets the Text of the Alert
     *
     * @param textId String resource id of the Alert text
     */
    fun setText(@StringRes textId: Int) {
        setText(context.getString(textId))
    }

    /**
     * Sets the Gravity of the Alert
     *
     * @param contentGravity Gravity of the Alert
     */
    var contentGravity: Int
        get() = (rlContainer!!.layoutParams as LayoutParams).gravity
        set(contentGravity) {
            (rlContainer!!.layoutParams as LayoutParams).gravity = contentGravity
            rlContainer!!.requestLayout()
        }

    /**
     * Disable touches while the Alert is showing
     */
    fun disableOutsideTouch() {
        flClickShield!!.isClickable = true
    }

    /**
     * Sets the Title of the Alert
     *
     * @param title String object to be used as the Alert title
     */
    fun setTitle(name: String) {
        if (!TextUtils.isEmpty(name)) {
            title?.visibility = VISIBLE
            title?.text = name
        }
    }

    val text: TextView?
        get() = tvText

    /**
     * Sets the Text of the Alert
     *
     * @param text String resource id of the Alert text
     */
    fun setText(text: String?) {
        if (!TextUtils.isEmpty(text)) {
            tvText!!.visibility = VISIBLE
            tvText!!.setText(text)
        }
    }

    /**
     * Set the inline icon for the Alert
     *
     * @param iconId Drawable resource id of the icon to use in the Alert
     */
    fun setIcon(@DrawableRes iconId: Int) {
        val iconDrawable: Drawable? = VectorDrawableCompat.create(
            context.resources, iconId, null
        )
        icon!!.setImageDrawable(iconDrawable)
    }

    /**
     * Set the inline icon for the Alert
     *
     * @param bitmap Bitmap image of the icon to use in the Alert.
     */
    fun setIcon(bitmap: Bitmap) {
        icon!!.setImageBitmap(bitmap)
    }

    /**
     * Set whether to show the icon in the alert or not
     *
     * @param showIcon True to show the icon, false otherwise
     */
    fun showIcon(showIcon: Boolean) {
        icon!!.visibility = if (showIcon) VISIBLE else GONE
    }

    /**
     * Set if the Icon should pulse or not
     *
     * @param shouldPulse True if the icon should be animated
     */
    fun pulseIcon(shouldPulse: Boolean) {
        enableIconPulse = shouldPulse
    }

    /**
     * Set if the duration of the alert is infinite
     *
     * @param enableInfiniteDuration True if the duration of the alert is infinite
     */
    fun setEnableInfiniteDuration(enableInfiniteDuration: Boolean) {
        this.enableInfiniteDuration = enableInfiniteDuration
    }

    /**
     * Set the alert's listener to be fired on the alert being fully shown
     *
     * @param listener Listener to be fired
     */
    fun setOnShowListener(listener: OnShowAlertListener) {
        onShowListener = listener
    }

    /**
     * Set the alert's listener to be fired on the alert being fully hidden
     *
     * @param listener Listener to be fired
     */
    fun setOnHideListener(listener: OnHideAlertListener) {
        onHideListener = listener
    }

    /**
     * Enable or Disable haptic feedback
     *
     * @param vibrationEnabled True to enable, false to disable
     */
    fun setVibrationEnabled(vibrationEnabled: Boolean) {
        this.vibrationEnabled = vibrationEnabled
    }

    companion object {
        private const val CLEAN_UP_DELAY_MILLIS = 100

        /**
         * The amount of time the alert will be visible on screen in seconds
         */
        private const val DISPLAY_TIME_IN_SECONDS: Long = 3000
    }
}