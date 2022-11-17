package za.co.woolworths.financial.services.android.ui.views.alert


import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import android.graphics.Bitmap
import android.app.Activity
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import java.lang.Exception
import java.lang.NullPointerException
import java.lang.ref.WeakReference

/**
 * Alert helper class. Will attach a temporary layout to the current activity's content, on top of
 * all other views. It should appear under the status bar.
 *
 */
class Alerter
/**
 * Constructor
 */
private constructor() {
    /**
     * Gets the Alert associated with the Alerter
     *
     * @return The current Alert
     */
    private var alert: Alert? = null

    /**
     * Shows the Alert, after it's built
     *
     * @return An Alert object check can be altered or hidden
     */
    fun show(): Alert? {
        //This will get the Activity Window's DecorView
        if (activityWeakReference != null) {
            activityWeakReference?.get()?.runOnUiThread { //Add the new Alert to the View Hierarchy
                    val decorView = activityDecorView
                    if (decorView != null && alert?.parent == null) {
                        decorView.addView(alert)
                    }
                }
        }
        return alert
    }

    /**
     * Hides currently showing alert.
     */
    fun hide() {
            alert?.hide()
    }

    /**
     * Sets the title of the Alert
     *
     * @param titleId Title String Resource
     * @return This Alerter
     */
    fun setTitle(@StringRes titleId: Int): Alerter {
            alert?.setTitle(titleId)
        return this
    }

    /**
     * Set Title of the Alert
     *
     * @param title Title as a String
     * @return This Alerter
     */
    fun setTitle(title: String?): Alerter {
        title?.let { alert?.setTitle(it) }
        return this
    }

    /**
     * Set Gravity of the Alert
     *
     * @param gravity Gravity of Alert
     * @return This Alerter
     */
    fun setContentGravity(gravity: Int): Alerter {
            alert?.contentGravity = gravity
        return this
    }

    /**
     * Sets the Alert Text
     *
     * @param textId Text String Resource
     * @return This Alerter
     */
    fun setText(@StringRes textId: Int?): Alerter {
        textId?.let { alert?.setText(it) }
        return this
    }

    /**
     * Sets the Alert Text
     *
     * @param text String of Alert Text
     * @return This Alerter
     */
    fun setText(text: String?): Alerter {
            alert?.setText(text)
        return this
    }

    /**
     * Set the Alert's Background Colour
     *
     * @param colorResId Colour Resource Id
     * @return This Alerter
     */
    fun setBackgroundColor(@ColorRes colorResId: Int): Alerter {
        if (alert != null && activityWeakReference != null) {
            activityWeakReference?.get()?.let {
                ContextCompat.getColor(
                    it, colorResId
                )
            }?.let {
                alert?.setAlertBackgroundColor(
                    it
                )
            }
        }
        return this
    }

    /**
     * Set the Alert's Background Drawable
     *
     * @param drawable Drawable
     * @return This Alerter
     */
    fun setBackgroundDrawable(drawable: Drawable?): Alerter {
        if (alert != null) {
            alert!!.setAlertBackgroundDrawable(drawable)
        }
        return this
    }

    /**
     * Set the Alert's Background Drawable Resource
     *
     * @param drawableResId Drawable Resource Id
     * @return This Alerter
     */
    fun setBackgroundResource(@DrawableRes drawableResId: Int): Alerter {
            alert?.setAlertBackgroundResource(drawableResId)
        return this
    }

    /**
     * Set the Alert's Icon
     *
     * @param iconId The Drawable's Resource Idw
     * @return This Alerter
     */
    fun setIcon(@DrawableRes iconId: Int?): Alerter {
        if (iconId != null) {
            alert?.setIcon(iconId)
        }
        return this
    }

    /**
     * Set the Alert's Icon
     *
     * @param bitmap The Bitmap object to use for the icon.
     * @return This Alerter
     */
    fun setIcon(bitmap: Bitmap?): Alerter {
        if (bitmap != null) {
            alert?.setIcon(bitmap)
        }
        return this
    }

    /**
     * Hide the Icon
     *
     * @return This Alerter
     */
    fun hideIcon(): Alerter {
            alert?.icon?.visibility = View.GONE
        return this
    }

    /**
     * Set the onClickListener for the Alert
     *
     * @param onClickListener The onClickListener for the Alert
     * @return This Alerter
     */
    fun setOnClickListener(onClickListener: View.OnClickListener): Alerter {
            alert?.setOnClickListener(onClickListener)
        return this
    }

    /**
     * Set the on screen duration of the alert
     *
     * @param milliseconds The duration in milliseconds
     * @return This Alerter
     */
    fun setDuration(milliseconds: Long): Alerter {
        alert?.duration = milliseconds
        return this
    }

    /**
     * Enable or Disable Icon Pulse Animations
     *
     * @param pulse True if the icon should pulse
     * @return This Alerter
     */
    fun enableIconPulse(pulse: Boolean): Alerter {
            alert?.pulseIcon(pulse)
        return this
    }

    /**
     * Set whether to show the icon in the alert or not
     *
     * @param showIcon True to show the icon, false otherwise
     * @return This Alerter
     */
    fun showIcon(showIcon: Boolean): Alerter {
            alert?.showIcon(showIcon)
        return this
    }

    /**
     * Enable or disable infinite duration of the alert
     *
     * @param infiniteDuration True if the duration of the alert is infinite
     * @return This Alerter
     */
    fun enableInfiniteDuration(infiniteDuration: Boolean): Alerter {
            alert?.setEnableInfiniteDuration(infiniteDuration)
        return this
    }

    /**
     * Sets the Alert Shown Listener
     *
     * @param listener OnShowAlertListener of Alert
     * @return This Alerter
     */
    fun setOnShowListener(listener: OnShowAlertListener): Alerter {
            alert?.setOnShowListener(listener)
        return this
    }

    /**
     * Sets the Alert Hidden Listener
     *
     * @param listener OnHideAlertListener of Alert
     * @return This Alerter
     */
    fun setOnHideListener(listener: OnHideAlertListener): Alerter {
            alert?.setOnHideListener(listener)
        return this
    }

    /**
     * Enable or Disable Vibration
     *
     * @param enable True to enable, False to disable
     * @return This Alerter
     */
    fun enableVibration(enable: Boolean): Alerter {
            alert?.setVibrationEnabled(enable)
        return this
    }

    /**
     * Disable touch events outside of the Alert
     *
     * @return This Alerter
     */
    fun disableOutsideTouch(): Alerter {
            alert?.disableOutsideTouch()
        return this
    }

    /**
     * Sets the Alert
     *
     * @param alert The Alert to be references and maintained
     */
    private fun setAlert(alert: Alert) {
        this.alert = alert
    }

    private val activityWeakReference: WeakReference<Activity?>?
        get() = Companion.activityWeakReference

    /**
     * Get the enclosing Decor View
     *
     * @return The Decor View of the Activity the Alerter was called from
     */
    private val activityDecorView: ViewGroup?
        get() {
            var decorView: ViewGroup? = null
            if (activityWeakReference != null && activityWeakReference?.get() != null) {
                decorView = activityWeakReference?.get()?.window?.decorView as? ViewGroup
            }
            return decorView
        }

    /**
     * Creates a weak reference to the calling Activity
     *
     * @param activity The calling Activity
     */
    private fun setActivity(activity: Activity) {
        Companion.activityWeakReference = WeakReference(activity)
    }

    companion object {
        private var activityWeakReference: WeakReference<Activity?>? = null

        /**
         * Creates the Alert, and maintains a reference to the calling Activity
         *
         * @param activity The calling Activity
         * @return This Alerter
         */
        @JvmStatic
        fun create(activity: Activity): Alerter {
            val alerter = Alerter()
            //Clear Current Alert, if one is Active
            clearCurrent(activity)
            alerter.setActivity(activity)
            alerter.setAlert(Alert(activity))
            return alerter
        }

        /**
         * Cleans up the currently showing alert view, if one is present
         */
        private fun clearCurrent(activity: Activity?) {
            activity ?: return
            try {
                val decorView = activity.window.decorView as ViewGroup

                //Find all Alert Views in Parent layout
                for (i in 0 until decorView.childCount) {
                    val childView =
                        if (decorView.getChildAt(i) is Alert) decorView.getChildAt(i) as Alert else null
                    if (childView != null && childView.windowToken != null) {
                        ViewCompat.animate(childView).alpha(0f)
                            .withEndAction(getRemoveViewRunnable(childView))
                    }
                }
            } catch (ex: Exception) {
                Log.e(Alerter::class.java.javaClass.simpleName, Log.getStackTraceString(ex))
            }
        }

        private fun getRemoveViewRunnable(childView: Alert): Runnable {
            return Runnable {
                try {
                    (childView.parent as ViewGroup).removeView(childView)
                } catch (ex: NullPointerException) {
                }
            }
        }
    }
}