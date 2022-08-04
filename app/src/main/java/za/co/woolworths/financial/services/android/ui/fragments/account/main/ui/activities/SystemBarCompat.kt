package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.setWindowFlag
import javax.inject.Inject

interface ISystemBarCompat {
    fun setLightStatusAndNavigationBar()
    fun setDarkStatusAndNavigationBar()
    fun setStatusBarInset(view: View)
    fun setStatusBarVisibility(isVisible: Boolean)
}

class SystemBarCompat @Inject constructor(private val activity: Activity) : ISystemBarCompat {

    override fun setLightStatusAndNavigationBar() {

        setSystemUiLightStatusBar(activity, true)
        val windowInsetsController = ViewCompat.getWindowInsetsController(activity.window.decorView)
        windowInsetsController?.isAppearanceLightNavigationBars = false
        windowInsetsController?.isAppearanceLightStatusBars = false
        (activity as? AppCompatActivity)?.apply {
            setWindowFlag(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                false
            )
         window?.statusBarColor = Color.TRANSPARENT
        }
    }

    override fun setDarkStatusAndNavigationBar() {
        val windowInsetsController = ViewCompat.getWindowInsetsController(activity.window.decorView)
        windowInsetsController?.isAppearanceLightNavigationBars = true
        windowInsetsController?.isAppearanceLightStatusBars = true
        (activity as? AppCompatActivity)?.window?.statusBarColor = Color.WHITE
    }

    override fun setStatusBarInset(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemGestures())
            // Apply the insets as padding to the view. Here we're setting all of the
            // dimensions, but apply as appropriate to your layout. You could also
            // update the views margin if more appropriate.
            view.updatePadding(insets.left, insets.top, insets.right, insets.bottom)

            // Return CONSUMED if we don't want the window insets to keep being passed
            // down to descendant views.
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun setStatusBarVisibility(isVisible: Boolean) {
        // Hide the system bars.
        val windowInsetsController = ViewCompat.getWindowInsetsController(activity.window.decorView)
        when (isVisible) {
            true -> windowInsetsController?.show(WindowInsetsCompat.Type.systemBars())
            false -> windowInsetsController?.hide(WindowInsetsCompat.Type.systemBars())
        }
    }

    @Suppress("DEPRECATION")
    private fun setSystemUiLightStatusBar(activity: Activity?, isLightStatusBar: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val systemUiAppearance = if (isLightStatusBar) {
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                } else {
                    0
                }
                activity?.window?.insetsController?.setSystemBarsAppearance(systemUiAppearance,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
            } else {
                val systemUiVisibilityFlags = if (isLightStatusBar) {
                    activity?.window?.decorView?.systemUiVisibility?.or(
                        SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    )
                } else {
                    activity?.window?.decorView?.systemUiVisibility?.and(
                        SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                    )
                }
                if (systemUiVisibilityFlags != null) {
                    activity?.window?.decorView?.systemUiVisibility = systemUiVisibilityFlags
                }
            }
        }
    }
}